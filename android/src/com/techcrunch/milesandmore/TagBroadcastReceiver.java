package com.techcrunch.milesandmore;

import android.content.Context;

import com.google.android.gcm.GCMBroadcastReceiver;

public class TagBroadcastReceiver extends GCMBroadcastReceiver {
	@Override
	protected String getGCMIntentServiceClassName(Context context) {
		return "com.techcrunch.milesandmore.MmGCMService";
	}
}
