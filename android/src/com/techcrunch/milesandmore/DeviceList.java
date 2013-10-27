package com.techcrunch.milesandmore;


import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.android.gcm.GCMRegistrar;
import com.techcrunch.milesandmore.api.Company;
import com.techcrunch.milesandmore.api.Tag;

public class DeviceList extends Activity {
	
	private static final String TAG = "BlouApp";
	
	private ServiceLayer mService;
	private boolean mConnected = false;
	
	private ListView mListView;
	private List<BluetoothDevice> mGattServices = new ArrayList<BluetoothDevice>();
	private List<Company> mCompanies = new ArrayList<Company>();
	private BaseAdapter mListAdapter;
	
	private ImageView mLufthansa;
	private ImageView mLufthansaIn1;
	private ImageView mLufthansaIn2;
	private ImageView mLufthansaMm;
	
	private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
        	mService = ((ServiceLayer.MyBinder) service).getService();
			Log.i(TAG, "Connected");
			mService.setConnected(true);
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
            } else if(ServiceLayer.ACTION_START_SCANNING.equals(action)) {
            	Log.d(TAG, "Refreshing");
            	setProgressBarIndeterminateVisibility(true);
            	setProgressBarIndeterminate(true);
            } else if(ServiceLayer.ACTION_STOP_SCANNING.equals(action)) {
            	Log.d(TAG, "Stop refreshing");
            	setProgressBarIndeterminateVisibility(false);
            	setProgressBarIndeterminate(false);
            } else if(ServiceLayer.ACTION_NEW_TAG.equals(action)) {
            	Tag tag = intent.getParcelableExtra("tag");
            	RegisterToShop dialog = RegisterToShop.newInstance(tag);
            	dialog.show(getFragmentManager(), "dialog");
            }
        }
    };
    
    
    private String[] mImages = {"http://www.lufthansa.com/mediapool/jpg/10/media_118393410.jpg", "http://www.lufthansa.com/mediapool/jpg/07/media_1471703107.jpg"};


	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS); 
		super.onCreate(savedInstanceState);

		getActionBar().setBackgroundDrawable(new ColorDrawable(0xFFFFBD26));
		getActionBar().setTitle("My Miles&More");
		
		setContentView(R.layout.activity_device_list);
		mLufthansa = (ImageView) findViewById(R.id.lufthansa);
		BitmapCache.getInstance(getBaseContext()).loadImage("http://www.catch.de/files/1_lh_menuekarten_logo_2.jpg", mLufthansa, null);
		mLufthansaIn1 = (ImageView) findViewById(R.id.lufthansaIn1);
		mLufthansaIn2 = (ImageView) findViewById(R.id.lufthansaIn2);
		mLufthansaMm = (ImageView) findViewById(R.id.lufthansaMM);
		BitmapCache.getInstance(getBaseContext()).loadImage(mImages[0], mLufthansaIn1, null);
		BitmapCache.getInstance(getBaseContext()).loadImage(mImages[1], mLufthansaIn2, null);
		BitmapCache.getInstance(getBaseContext()).loadImage("http://upload.wikimedia.org/wikipedia/de/thumb/5/50/Miles_%26_More_Lufthansa_Logo.svg/744px-Miles_%26_More_Lufthansa_Logo.svg.png", mLufthansaMm, null);
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
		if(getIntent().hasExtra("tag")) {
			Intent intent = new Intent(getIntent());
			intent.setClass(this, TagDetails.class);
			setIntent(new Intent());
			startActivity(intent);
		} else if(getIntent().hasExtra("tagDialog")) {
			Tag tag = getIntent().getParcelableExtra("tagDialog");
        	RegisterToShop dialog = RegisterToShop.newInstance(tag);
        	dialog.show(getFragmentManager(), "dialog");
			setIntent(new Intent());
		}
	}
	
	@Override
	public void onStop() {
		super.onStop();
		if(mService != null) {
			mService.setConnected(false);
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
		getMenuInflater().inflate(R.menu.device_list, menu);
		return true;
	}
	
	private IntentFilter getIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ServiceLayer.ACTION_NEW_DEVICE);
        intentFilter.addAction(ServiceLayer.ACTION_COMPANY);
        intentFilter.addAction(ServiceLayer.ACTION_START_SCANNING);
        intentFilter.addAction(ServiceLayer.ACTION_STOP_SCANNING);
        intentFilter.addAction(ServiceLayer.ACTION_NEW_TAG);
        return intentFilter;
    }
	
	public static class RegisterToShop extends DialogFragment {

	    public static RegisterToShop newInstance(Tag tag) {
	    	RegisterToShop frag = new RegisterToShop();
	        Bundle args = new Bundle();
	        args.putParcelable("tag", tag);
	        frag.setArguments(args);
	        return frag;
	    }

	    @Override
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
	        final Tag tag = getArguments().getParcelable("tag");

	        return new AlertDialog.Builder(getActivity())
	                .setIcon(R.drawable.ic_launcher)
	                .setTitle("Offer registration")
	                .setMessage(String.format("You are near %s (Miles and More partner). Do you want to receive an information about offers?", tag.getName()))
	                .setPositiveButton(android.R.string.ok,
	                    new DialogInterface.OnClickListener() {
	                        public void onClick(DialogInterface dialog, int whichButton) {
	                            ((DeviceList)getActivity()).mService.registerTag(tag);
	                        }
	                    }
	                ).setNegativeButton(android.R.string.cancel, null).create();
	    }
	}

}
