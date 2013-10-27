package com.techcrunch.milesandmore;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class BitmapCache {
	private static String TAG = "BitmapCache";
	private static final String CACHE_DIR = "images";
	private static final int VERSION = 1;
	private static final int IO_BUFFER_SIZE = 8 * 1024;
	private static final int MAX_RETRIES = 5;
	
	private static BitmapCache mInstance;

	private LruCache<String, Bitmap> mMemoryCache;
	private SimpleDiskCache mDiskCache;
	
	public static BitmapCache getInstance(Context context) {
		if(mInstance == null) {
			mInstance = new BitmapCache(context);
		}
		return mInstance;
	}

	private BitmapCache(Context context) {

		final int memClass = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
		final int cacheSize = 1024 * 1024 * memClass / 10;

		mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
			@SuppressLint("NewApi")
			@Override
			protected int sizeOf(String key, Bitmap value) {
				if (Integer.valueOf(android.os.Build.VERSION.SDK_INT) >= 12)
					return value.getByteCount();
				else
					return (value.getRowBytes() * value.getHeight());
			}
		};

		try {
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				mDiskCache = SimpleDiskCache.open(context.getExternalFilesDir(CACHE_DIR), VERSION, 1024 * 1024 * 6);
			} else {
				mDiskCache = SimpleDiskCache.open(context.getDir(CACHE_DIR, Context.MODE_PRIVATE), VERSION, 1024 * 1024 * 4);
			}
		} catch (IOException e) {
			Log.e(TAG, e.getLocalizedMessage());
		}
	}

	public void loadImage(String key, BitmapCallback callback) {
		Bitmap bitmap = mMemoryCache.get(key + Integer.toString(0));
		if (bitmap != null) {
			callback.onComplete(bitmap);
		} else {
			BitmapWorkerTask worker = new BitmapWorkerTask(callback);
			worker.execute(key, Integer.valueOf(0));
		}
	}

	public void loadImage(String key, ImageView imageView, View emptyView) {
		loadImage(key, imageView, emptyView, 0);
	}

	public void loadImage(String key, ImageView imageView, View emptyView, int cornerRadius) {
		if (key == null) {
			return;
		}
		Bitmap bitmap = mMemoryCache.get(key + Integer.toString(cornerRadius));
		if (bitmap != null) {
			setImage(bitmap, imageView, emptyView);
		} else {
			cancelPotentialWork(key + Integer.toString(cornerRadius), imageView);
			imageView.setVisibility(View.INVISIBLE);
			if (emptyView != null) {
				emptyView.setVisibility(View.VISIBLE);
			}
			BitmapWorkerTask worker = new BitmapWorkerTask(imageView, emptyView);
			AsyncDrawable drawable = new AsyncDrawable(imageView.getResources(), null, worker);
			imageView.setImageDrawable(drawable);
			worker.execute(key, Integer.valueOf(cornerRadius));
		}
	}
	
	private void setImage(Bitmap bitmap, ImageView imageView, View emptyView) {
		imageView.setVisibility(View.VISIBLE);
		if (emptyView != null) {
			emptyView.setVisibility(View.GONE);
		}
		imageView.setImageBitmap(bitmap);
	}

	public void loadBackground(String key, View backgroundView) {
		if (key == null) {
			return;
		}
		Bitmap bitmap = mMemoryCache.get(key + "0");
		if (bitmap != null) {
			backgroundView.setBackgroundDrawable(new BitmapDrawable(bitmap));
		} else {
			BitmapWorkerTask worker = new BitmapWorkerTask(backgroundView);
			AsyncDrawable drawable = new AsyncDrawable(backgroundView.getResources(), null, worker);
			backgroundView.setBackgroundDrawable(drawable);
			worker.execute(key, Integer.valueOf(0));
		}
	}

	public class AsyncDrawable extends BitmapDrawable {
		private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

		public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask) {
			super(res, bitmap);
			bitmapWorkerTaskReference = new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
		}

		public BitmapWorkerTask getBitmapWorkerTask() {
			return bitmapWorkerTaskReference.get();
		}
	}

	public class BitmapWorkerTask extends AsyncTask<Object, Void, Bitmap> {

		private String url;
		private WeakReference<View> backgroundReference;
		private WeakReference<ImageView> imageReference;
		private WeakReference<View> emptyReference;
		private BitmapCallback callback;

		public BitmapWorkerTask(ImageView imageView, View emptyView) {
			this.imageReference = new WeakReference<ImageView>(imageView);
			this.emptyReference = new WeakReference<View>(emptyView);
		}

		public BitmapWorkerTask(View backgroundView) {
			backgroundReference = new WeakReference<View>(backgroundView);
		}

		public BitmapWorkerTask(BitmapCallback callback) {
			this.callback = callback;
		}

		@Override
		protected Bitmap doInBackground(Object... params) {
			Log.d(TAG, "Start downloading image");
			url = (String) params[0];
			final int cornerRadius = (Integer) params[1];
			Bitmap bitmap = null;
			synchronized (mDiskCache) {
				try {
					if (mDiskCache.contains(url + Integer.toString(cornerRadius))) {
						bitmap = mDiskCache.getBitmap(url + Integer.toString(cornerRadius)).getBitmap();
					} else {
						if (!isCancelled()) {
							OutputStream outputStream = mDiskCache.openStream(url + Integer.toString(cornerRadius));
							if (outputStream != null) {
								if (downloadUrlToStream(url, outputStream)) {
									bitmap = mDiskCache.getBitmap(url + Integer.toString(cornerRadius)).getBitmap();
								} else {
									Log.d(TAG, "Error with downloading bitmap");
								}
							}
						}
					}
					if (bitmap != null && cornerRadius > 0) {
						bitmap = getRoundedCornerBitmap(bitmap, cornerRadius);
					}
					if (bitmap != null) {
						mMemoryCache.put(url + Integer.toString(cornerRadius), bitmap);
					}
				} catch (IOException e) {
					Log.e(TAG, "BitmapWorkerTask:\n" + e.getLocalizedMessage());
				}
			}
			Log.d(TAG, "End downloading image");
			return bitmap;
		}

		private boolean downloadUrlToStream(String urlString, OutputStream outputStream) {
			HttpURLConnection urlConnection = null;
			BufferedOutputStream out = null;
			BufferedInputStream in = null;

			try {
				final URL url = new URL(urlString);
				urlConnection = (HttpURLConnection) url.openConnection();
				in = new BufferedInputStream(urlConnection.getInputStream(), IO_BUFFER_SIZE);
				out = new BufferedOutputStream(outputStream, IO_BUFFER_SIZE);

				int b;
				while ((b = in.read()) != -1) {
					out.write(b);
				}
				return true;
			} catch (final IOException e) {
				Log.e(TAG, "Error in downloadBitmap - " + e);
			} finally {
				if (urlConnection != null) {
					urlConnection.disconnect();
				}
				try {
					if (out != null) {
						out.close();
					}
					if (in != null) {
						in.close();
					}
				} catch (final IOException e) {
				}
			}
			return false;
		}

		@Override
		protected void onPostExecute(Bitmap value) {
			if (this.imageReference != null) {
				setImageDrawable(value);
			}
			if (this.backgroundReference != null) {
				setBackgroundDrawable(value);
			}
			if(this.callback !=null) {
				callback.onComplete(value);
			}
		}

		private void setImageDrawable(Bitmap value) {
			final ImageView imageView = this.imageReference.get();
			final View emptyView = this.emptyReference.get();
			if (imageView != null) {
				Drawable drawable = imageView.getDrawable();
				if (drawable instanceof AsyncDrawable) {
					AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
					if (asyncDrawable.getBitmapWorkerTask() == this) {
						if (value != null) {
							imageView.setImageBitmap(value);
							imageView.setVisibility(View.VISIBLE);
						}
						if (emptyView != null) {
							emptyView.setVisibility(View.GONE);
						}
					}
				}
			}
		}

		private void setBackgroundDrawable(Bitmap value) {
			final View backgroundView = backgroundReference.get();
			if (backgroundView != null && value != null) {
				Drawable drawable = backgroundView.getBackground();
				if (drawable instanceof AsyncDrawable) {
					AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
					if (asyncDrawable.getBitmapWorkerTask() == this) {
						Log.d(TAG, "Set bitmap");
						backgroundView.setBackgroundDrawable(new BitmapDrawable(value));
					}
				}
			} else if(callback !=null) {
				callback.onComplete(value);
			}
		}
	}

	private Bitmap getRoundedCornerBitmap(Bitmap bitmap, int cornerRadius) {
		if (bitmap == null) {
			return bitmap;
		}

		Bitmap output = null;
		if (bitmap.getWidth() > bitmap.getHeight()) {
			output = Bitmap.createBitmap(bitmap.getHeight(), bitmap.getHeight(), Config.ARGB_8888);
		} else {
			output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getWidth(), Config.ARGB_8888);
		}
		Canvas canvas = new Canvas(output);

		final int color = 0xFF424242;
		final Paint paint = new Paint();
		Rect rect;
		if (bitmap.getWidth() > bitmap.getHeight()) {
			rect = new Rect(0, 0, bitmap.getHeight(), bitmap.getHeight());
		} else {
			rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getWidth());
		}
		final RectF rectF = new RectF(rect);
		final float roundPx = cornerRadius;

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		if (bitmap != null) {
			bitmap.recycle();
		}
		return output;
	}

	public void cancelPotentialWork(String data, ImageView imageView) {
		if (imageView != null) {
			Drawable drawable = imageView.getDrawable();
			if (drawable instanceof AsyncDrawable) {
				AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
				if (asyncDrawable.getBitmapWorkerTask() != null) {
					if (asyncDrawable.getBitmapWorkerTask().url == null || !asyncDrawable.getBitmapWorkerTask().url.equals(data)) {
						asyncDrawable.getBitmapWorkerTask().cancel(true);
					}
				}
			}
		}
	}
	
	public interface BitmapCallback {
		public void onProgress(int i);
		public void onComplete(Bitmap bitmap);
	}
}
