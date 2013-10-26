package com.techcrunch.milesandmore;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.techcrunch.milesandmore.api.TagData;

public class BlouService extends Service {

	private final static String TAG = "BlouService";

	private BluetoothManager mBluetoothManager;
	private BluetoothAdapter mBluetoothAdapter;
	private String mBluetoothDeviceAddress;
	private BluetoothGatt mBluetoothGatt;
	private BlouServiceInterface mBlouService;

	public final static String ACTION_NEW_DEVICE = "com.techcrunch.milesandmore.ACTION_NEW_DEVICE";

	private static final int STATE_DISCONNECTED = 0;
	private static final int STATE_CONNECTING = 1;
	private static final int STATE_CONNECTED = 2;

	private static int mConnectionState = 0;

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
			if(error.getResponse().getStatus() == HttpStatus.SC_NOT_FOUND) {
				Log.e(TAG, "Device not found: " + error.getUrl());
			}
		}

		@Override
		public void success(TagData tagData, Response response) {
			Log.i(TAG,  response.toString());
			Toast.makeText(getBaseContext(), "Find " + tagData.getTag().getName(), Toast.LENGTH_SHORT).show();
		}
	};

	private List<BluetoothDevice> mDevices = new ArrayList<BluetoothDevice>();

	@Override
	public void onCreate() {
		super.onCreate();
		RestAdapter adapter = new RestAdapter.Builder().setServer("http://198.245.54.12:8000/").build();
		mBlouService = adapter.create(BlouServiceInterface.class);
		initialize();
	}

	public void onDestroy() {
		super.onDestroy();
		mBluetoothAdapter.stopLeScan(mScanCallback);
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

		mHandler.sendMessageDelayed(Message.obtain(null, MyHandler.START_SCANNING), 1000);

		return true;
	}

	private static class MyHandler extends Handler {

		private WeakReference<BlouService> service;

		public MyHandler(BlouService service) {
			this.service = new WeakReference<BlouService>(service);
		}

		static final int START_SCANNING = 1;
		static final int STOP_SCANNING = 2;

		public void handleMessage(Message msg) {
			BlouService service = this.service.get();
			if (service != null) {
				switch (msg.what) {
				case START_SCANNING:
					service.mBluetoothAdapter.startLeScan(service.mScanCallback);
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

	public class MyBinder extends Binder {
		BlouService getService() {
			return BlouService.this;
		}
	}

	private final IBinder mBinder = new MyBinder();
	private final MyHandler mHandler = new MyHandler(this);

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

}
