package com.techcrunch.milesandmore;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.techcrunch.milesandmore.BitmapCache.BitmapCallback;
import com.techcrunch.milesandmore.api.AddTag;
import com.techcrunch.milesandmore.api.Company;
import com.techcrunch.milesandmore.api.Tag;
import com.techcrunch.milesandmore.api.TagData;

public class ServiceLayer extends Service implements BitmapCallback {

	private final static String TAG = "BlouService";

	private BluetoothManager mBluetoothManager;
	private BluetoothAdapter mBluetoothAdapter;
	private ServiceInterface mBlouService;
	private String mGcmRegId;

	public final static String ACTION_NEW_DEVICE = "com.techcrunch.milesandmore.ACTION_NEW_DEVICE";
	public final static String ACTION_COMPANY = "com.techcrunch.milesandmore.ACTION_COMPANY";
	public final static String ACTION_NOTIFICATION = "com.techcrunch.milesandmore.ACTION_NOTIFICATION";
	public final static String ACTION_REGISTRATION = "com.techcrunch.milesandmore.ACTION_REGISTRATION";
	
	private TagData mTagData;
	private Tag mTag;

	private LeScanCallback mScanCallback = new LeScanCallback() {

		@Override
		public void onLeScan(BluetoothDevice device, int i, byte[] data) {
			Log.i(TAG, "Device: " + device.getName());
			boolean isScanned = false;
			for (BluetoothDevice scannedDevice : mDevices) {
				if (scannedDevice.getAddress().equals(device.getAddress())) {
					isScanned = true;
				}
			}
			if (!isScanned) {
				mDevices.add(device);
				mBlouService.getTag(device.getAddress(), mTagCallback);
				broadcastUpdate(ACTION_NEW_DEVICE);
			}
		}
	};

	private Callback<TagData> mTagCallback = new Callback<TagData>() {

		@Override
		public void failure(RetrofitError error) {
			if (error.getResponse() == null || error.getResponse().getStatus() == HttpStatus.SC_NOT_FOUND) {
				Log.e(TAG, "Device not found: " + error.getUrl());
			}
		}

		@Override
		public void success(TagData tagData, Response response) {
			Log.i(TAG, response.toString());
			AddTag tag = new AddTag(tagData.getTag().getId(), getSharedPreferences("preference", 0).getString("deviceToken", null));
			mBlouService.register(tag, new Callback<Response>() {
				
				@Override
				public void success(Response arg0, Response arg1) {
					Toast.makeText(getBaseContext(), "Registered", Toast.LENGTH_SHORT).show();
				}
				
				@Override
				public void failure(RetrofitError arg0) {
					// TODO Auto-generated method stub
					
				}
			});
			Toast.makeText(getBaseContext(), "Find " + tagData.getTag().getName(), Toast.LENGTH_SHORT).show();
		}
	};

	private List<BluetoothDevice> mDevices = new ArrayList<BluetoothDevice>();

	@Override
	public void onCreate() {
		super.onCreate();
		mGcmRegId = getSharedPreferences("preference", 0).getString("deviceToken", null);
		RestAdapter adapter = new RestAdapter.Builder().setServer("http://198.245.54.12:8000/").build();
		mBlouService = adapter.create(ServiceInterface.class);
	}

	public void onDestroy() {
		super.onDestroy();
		mBluetoothAdapter.stopLeScan(mScanCallback);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			if (ACTION_REGISTRATION.equals(intent.getAction())) {
				mGcmRegId = intent.getStringExtra("deviceToken");
				Log.d(TAG, mGcmRegId);
				getSharedPreferences("preference", 0).edit().putString("deviceToken", mGcmRegId).commit();
			} else if (ACTION_NOTIFICATION.equals(intent.getAction())) {
				String data = intent.getStringExtra("data");
				Log.d("Tag", data);
				Gson gson = new Gson();
				mTagData = gson.fromJson(data, TagData.class);
				mTag = mTagData.getTag();

				if (mTag.getImageUrl() == null) {
					showInputStyleNotification();
				} else {
					BitmapCache.getInstance(getBaseContext()).loadImage(
							mTag.getImageUrl(), this);
				}
			}
		}
		initialize();
		return START_STICKY;
	}

	public List<BluetoothDevice> getDevices() {
		return mDevices;
	}

	private void broadcastUpdate(final String action) {
		final Intent intent = new Intent(action);
		sendBroadcast(intent);
	}

	public boolean initialize() {
		if (mBluetoothManager == null) {
			mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			if (mBluetoothManager == null) {
				Log.e(TAG, "Unable to initialize BluetoothManager.");
				return false;
			}
		}

		mBluetoothAdapter = mBluetoothManager.getAdapter();
		if (mBluetoothAdapter == null) {
			Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
			return false;
		}

		mHandler.sendMessageDelayed(Message.obtain(null, MyHandler.START_SCANNING), 500);

		return true;
	}

	private static class MyHandler extends Handler {

		private WeakReference<ServiceLayer> service;

		public MyHandler(ServiceLayer service) {
			this.service = new WeakReference<ServiceLayer>(service);
		}

		static final int START_SCANNING = 1;
		static final int STOP_SCANNING = 2;

		public void handleMessage(Message msg) {
			ServiceLayer service = this.service.get();
			if (service != null) {
				switch (msg.what) {
				case START_SCANNING:
					if (service.mBluetoothAdapter.isEnabled()) {
						service.mBluetoothAdapter.startLeScan(service.mScanCallback);
					} else {
						service.mBluetoothAdapter.enable();
						service.mHandler.sendMessageDelayed(Message.obtain(msg), 500);
						Log.d(TAG, "Resend message");
					}
					break;
				case STOP_SCANNING:
					service.mBluetoothAdapter.stopLeScan(service.mScanCallback);
					break;
				default:
					break;
				}
			}
		}
	}

	public void sendGcmDeviceToken(String deviceToken) {

	}

	public class MyBinder extends Binder {
		ServiceLayer getService() {
			return ServiceLayer.this;
		}
	}

	private final IBinder mBinder = new MyBinder();
	private final MyHandler mHandler = new MyHandler(this);

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	private class PartnersDownloader extends AsyncTask<String, Void, Object> {

		@Override
		protected Object doInBackground(String... url) {
			HttpGet get = new HttpGet(url[0]);
			AndroidHttpClient client = AndroidHttpClient.newInstance("Windows");
			try {
				HttpResponse response = client.execute(get);
				String result = EntityUtils.toString(response.getEntity());
				String parsed = result.substring(result.indexOf("<article"), result.lastIndexOf("</article>") + 10);

				return parsed;

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public void onPostExecute(Object object) {
			try {
				Log.i(TAG, "Start parse");
				List<Company> companies = MMXmlParser.parseCompanies((String) object);
				for (Company company : companies) {
					Intent intent = new Intent(ACTION_COMPANY);
					intent.putExtra("company", company);
					sendBroadcast(intent);
				}
				Log.i(TAG, "End parse ");
			} catch (Exception e) {
				Log.e(TAG, e.getLocalizedMessage());
			}
		}

	}
	@Override
	public void onComplete(Bitmap bitmap) {
		if (bitmap == null) {
			showInputStyleNotification();
		} else {
			showPictureNotification(bitmap);
		}
	}

	private void showInputStyleNotification() {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setSmallIcon(R.drawable.ic_launcher);
		builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
		builder.setAutoCancel(true);
		builder.setContentTitle(mTagData.getTitle());
		builder.setContentText(mTagData.getDescription());
		builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
		if (mTag.getLatitude()>0 && mTag.getLongitude()>0) {
			String uri = String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f(%s)", mTag.getLatitude(), mTag.getLongitude(),
					mTag.getLatitude(), mTag.getLongitude(), mTag.getName());
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
			PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
			builder.addAction(R.drawable.ic_launcher, "Maps", pendingIntent);
		}

		NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
		style.setBigContentTitle(mTag.getName());
		String[] text = mTag.getDescription().split("\n");
		for (int i = 0; i < text.length && i < 6; i++) {
			style.addLine(text[i]);
		}
		builder.setStyle(style);

		showNotification(builder);
	}

	private void showPictureNotification(Bitmap bitmap) {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setSmallIcon(R.drawable.ic_launcher);
		builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
		builder.setAutoCancel(true);
		builder.setContentTitle(mTagData.getTitle());
		builder.setContentText(mTagData.getDescription());
		builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
		if (mTag.getLatitude()>0 && mTag.getLongitude()>0) {
			String uri = String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f(%s)", mTag.getLatitude(), mTag.getLongitude(),
					mTag.getLatitude(), mTag.getLongitude(), mTag.getName());
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
			PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
			builder.addAction(R.drawable.ic_launcher, "Maps", pendingIntent);
		}

		NotificationCompat.BigPictureStyle style = new NotificationCompat.BigPictureStyle();
		style.bigPicture(bitmap);
		style.setBigContentTitle(mTag.getName());
		style.setSummaryText(null);
		builder.setStyle(style);

		showNotification(builder);
	}

	private void showNotification(NotificationCompat.Builder builder) {
		Intent messageIntent = new Intent(this, TagDetails.class);
		messageIntent.putExtra("tag", mTag);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, messageIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(pendingIntent);
		NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		manager.notify("MAndM", 34, builder.build());
	}

	@Override
	public void onProgress(int i) {
		// TODO Auto-generated method stub
		
	}

}
