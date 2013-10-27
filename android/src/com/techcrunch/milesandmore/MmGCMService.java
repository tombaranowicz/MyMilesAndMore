package com.techcrunch.milesandmore;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;

public class MmGCMService extends GCMBaseIntentService {

	@Override
	protected void onError(Context context, String errorId) {
		Log.e(TAG, errorId);
	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		Intent serviceIntent = new Intent(context, ServiceLayer.class);
		serviceIntent.setAction(ServiceLayer.ACTION_NOTIFICATION);
		serviceIntent.putExtras(intent.getExtras());
		context.startService(serviceIntent);
	}

	@Override
	protected void onRegistered(Context context, String registrationId) {
		Log.d("Blou", "Reg: " + registrationId);
		Intent intent = new Intent(context, ServiceLayer.class);
		intent.setAction(ServiceLayer.ACTION_REGISTRATION);
		intent.putExtra("deviceToken", registrationId);
		context.startService(intent);
	}

	@Override
	protected void onUnregistered(Context context, String registrationId) {
		// TODO Auto-generated method stub

	}

}
