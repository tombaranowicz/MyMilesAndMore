package com.techcrunch.milesandmore;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

import com.techcrunch.milesandmore.api.AddTag;
import com.techcrunch.milesandmore.api.TagData;

public interface ServiceInterface {
	@POST("/objects/add_android_device")
	public void register(@Body AddTag register, Callback<Response> callback);
	
	@GET("/objects/get_tag_details/{tagAddress}")
	public void getTag(@Path("tagAddress") String tagAddress, Callback<TagData> callback);

}
