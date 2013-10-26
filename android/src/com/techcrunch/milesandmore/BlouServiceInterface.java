package com.techcrunch.milesandmore;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;

import com.techcrunch.milesandmore.api.TagData;

public interface BlouServiceInterface {
	
	@GET("/objects/get_tag_details/{tagAddress}")
	public void getTag(@Path("tagAddress") String tagAddress, Callback<TagData> callback);

}
