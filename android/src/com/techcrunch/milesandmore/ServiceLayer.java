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
	public final static String ACTION_START_SCANNING = "com.techcrunch.milesandmore.ACTION_START_SCANNING";
	public final static String ACTION_STOP_SCANNING = "com.techcrunch.milesandmore.ACTION_STOP_SCANNING";
	public final static String ACTION_NEW_TAG = "com.techcrunch.milesandmore.ACTION_NEW_TAG";

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
			if (mConnected) {
				Intent intent = new Intent(ACTION_NEW_TAG);
				intent.putExtra("tag", tagData.getTag());
				sendBroadcast(intent);
			} else {
				Intent intent = new Intent(ServiceLayer.this, DeviceList.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.putExtra("tagDialog", tagData.getTag());
				startActivity(intent);
			}
		}
	};

	private boolean mConnected = false;
	private List<BluetoothDevice> mDevices = new ArrayList<BluetoothDevice>();

	@Override
	public void onCreate() {
		super.onCreate();
		mGcmRegId = getSharedPreferences("preference", 0).getString("deviceToken", null);
		RestAdapter adapter = new RestAdapter.Builder().setServer("http://198.245.54.12:8000/").build();
		mBlouService = adapter.create(ServiceInterface.class);
		initialize();
	}

	public void onDestroy() {
		super.onDestroy();
		mBluetoothAdapter.stopLeScan(mScanCallback);
		mBluetoothAdapter.disable();
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
					BitmapCache.getInstance(getApplicationContext()).loadImage(mTag.getImageUrl(), this);
				}
			}
		}
		return START_STICKY;
	}

	public void setConnected(boolean connected) {
		this.mConnected = connected;
	}

	public List<BluetoothDevice> getDevices() {
		return mDevices;
	}

	private void broadcastUpdate(final String action) {
		final Intent intent = new Intent(action);
		sendBroadcast(intent);
	}

	private boolean initialize() {
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
		
		mBluetoothAdapter.enable();

		mHandler.sendEmptyMessageDelayed(MyHandler.START_SCANNING, 500);
		mHandler.sendEmptyMessageDelayed(MyHandler.STOP_SCANNING, 120000);

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
					service.mBluetoothAdapter.startLeScan(service.mScanCallback);
					break;
				case STOP_SCANNING:
					service.broadcastUpdate(ACTION_STOP_SCANNING);
					service.mBluetoothAdapter.disable();
					service.mBluetoothAdapter.enable();
					service.mHandler.sendEmptyMessageDelayed(START_SCANNING, 1000);
					service.mHandler.sendEmptyMessageDelayed(STOP_SCANNING, 120000);
					break;
				default:
					break;
				}
			}
		}
	}

	public void sendGcmDeviceToken(String deviceToken) {

	}

	public void registerTag(Tag tag) {
		AddTag addTag = new AddTag(tag.getId(), getSharedPreferences("preference", 0).getString("deviceToken", null));
		mBlouService.register(addTag, new Callback<Response>() {

			@Override
			public void success(Response arg0, Response arg1) {
				Toast.makeText(getBaseContext(), "You are registered to offer list", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void failure(RetrofitError arg0) {
				// TODO Auto-generated method stub

			}
		});
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
		builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
		builder.setAutoCancel(true);
		builder.setSmallIcon(R.drawable.ic_launcher);
		builder.setContentTitle(mTagData.getTitle());
		builder.setContentText(mTagData.getDescription());
		builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
		if (mTag.getLatitude() > 0 && mTag.getLongitude() > 0) {
			String uri = String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f(%s)", mTag.getLatitude(), mTag.getLongitude(),
					mTag.getLatitude(), mTag.getLongitude(), mTag.getName());
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
			PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
			builder.addAction(R.drawable.ic_maps, "Maps", pendingIntent);
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
		builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
		builder.setSmallIcon(R.drawable.ic_launcher);
		builder.setAutoCancel(true);
		builder.setContentTitle(mTagData.getTitle());
		builder.setContentText(mTagData.getDescription());
		builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
		if (mTag.getLatitude() > 0 && mTag.getLongitude() > 0) {
			String uri = String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f(%s)", mTag.getLatitude(), mTag.getLongitude(),
					mTag.getLatitude(), mTag.getLongitude(), mTag.getName());
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
			PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
			builder.addAction(R.drawable.ic_maps, "Maps", pendingIntent);
		}

		NotificationCompat.BigPictureStyle style = new NotificationCompat.BigPictureStyle();
		style.bigPicture(bitmap);
		style.setBigContentTitle(mTag.getName());
		style.setSummaryText(null);
		builder.setStyle(style);

		showNotification(builder);
	}

	private void showNotification(NotificationCompat.Builder builder) {
		Log.d(TAG, "Show notification");
		Intent messageIntent = new Intent(this, DeviceList.class);
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
