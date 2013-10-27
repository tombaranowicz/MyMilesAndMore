package com.techcrunch.milesandmore.api;

public class AddTag {
	private String tag_id;
	private String android_device_token;
	
	public AddTag(String tag_id, String android_device_token) {
		this.tag_id = tag_id;
		this.android_device_token = android_device_token;
	}

}
