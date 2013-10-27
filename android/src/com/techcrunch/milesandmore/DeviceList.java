package com.techcrunch.milesandmore;


import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gcm.GCMRegistrar;
import com.techcrunch.milesandmore.api.Company;

public class DeviceList extends Activity {
	
	private static final String TAG = "BlouApp";
	
	private ServiceLayer mService;
	private boolean mConnected = false;
	
	private ListView mListView;
	private List<BluetoothDevice> mGattServices = new ArrayList<BluetoothDevice>();
	private List<Company> mCompanies = new ArrayList<Company>();
	private BaseAdapter mListAdapter;
	
	private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
        	mService = ((ServiceLayer.MyBinder) service).getService();
			Log.i(TAG, "Connected");
			mGattServices.clear();
            mGattServices.addAll(mService.getDevices());
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mService = null;
        }
    };
    
    private final BroadcastReceiver mScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (ServiceLayer.ACTION_NEW_DEVICE.equals(action)) {
            	Log.d(TAG, "Add device");
            	mGattServices.clear();
            	mGattServices.addAll(mService.getDevices());
            	mListAdapter.notifyDataSetChanged();
            }
        }
    };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_device_list);
		mListView = (ListView) findViewById(R.id.list);
		mListAdapter = new DeviceAdapter();
		mListView.setAdapter(mListAdapter);
		startService(new Intent(this, ServiceLayer.class));
		
		registerGCM();
	}
	
	@Override
	public void onStart() {
		super.onStart();
		if(mService == null) {
			bindService(new Intent(this, ServiceLayer.class), mServiceConnection, Context.BIND_AUTO_CREATE);
			registerReceiver(mScanReceiver, getIntentFilter());
		}
	}
	
	@Override
	public void onStop() {
		super.onStop();
		if(mService != null) {
			unbindService(mServiceConnection);
			unregisterReceiver(mScanReceiver);
			mService = null;
		}
	}
	
	private void registerGCM() {
		try {
			GCMRegistrar.checkDevice(this);
			GCMRegistrar.checkManifest(this);
			final String regId = GCMRegistrar.getRegistrationId(this);
			if (TextUtils.isEmpty(regId)) {
				Log.d(TAG, "Send regId request");
				GCMRegistrar.register(this, "1019372707521");
			} else {
				getSharedPreferences("preference", 0).edit().putString("deviceToken", regId)
						.commit();
				if (regId == null && mService!=null) {
					mService.sendGcmDeviceToken(regId);
				}
			}
		} catch (UnsupportedOperationException e) {
			Log.e(TAG, e.getLocalizedMessage());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.device_list, menu);
		return true;
	}
	
	private IntentFilter getIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ServiceLayer.ACTION_NEW_DEVICE);
        intentFilter.addAction(ServiceLayer.ACTION_COMPANY);
        return intentFilter;
    }
	
	private class DeviceAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mGattServices.size();
		}

		@Override
		public Object getItem(int position) {
			return mGattServices.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup group) {
			DeviceViewHolder holder = new DeviceViewHolder();
			if(convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.list_device_item, null);
				holder = new DeviceViewHolder();
				holder.title = (TextView) convertView.findViewById(R.id.device_title);
				holder.address = (TextView) convertView.findViewById(R.id.device_address);
				convertView.setTag(holder);
			} else {
				holder = (DeviceViewHolder) convertView.getTag();
			}
			holder.title.setText(mGattServices.get(position).getName());
			holder.address.setText(mGattServices.get(position).getAddress());
			return convertView;
		}
	}
	
	private class DeviceViewHolder {
		TextView title;
		TextView address;
	}

}
