package com.techcrunch.milesandmore.api;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Tag implements Parcelable {
	
	@SerializedName("tag_id")
	private String mId;
	@SerializedName("title")
	private String mTitle;
	@SerializedName("description")
	private String mDescription;
	@SerializedName("address")
	private String mAddress;
	@SerializedName("logo_url")
	private String mLogoUrl;
	@SerializedName("image_url")
	private String mImageUrl;
	@SerializedName("opening_hours")
	private String mOpeningHours;
	@SerializedName("phone")
	private String mPhone;
	@SerializedName("foursquare_id")
	private String mFoursquareId;
	@SerializedName("latitude")
	private double mLatitude;
	@SerializedName("longitude")
	private double mLongitude;
	
	public Tag() {
		
	}
	
	public String getId() {
		return mId;
	}
	public void setId(String id) {
		this.mId = id;
	}
	public String getName() {
		return mTitle;
	}
	public void setName(String name) {
		this.mTitle = name;
	}
	public String getDescription() {
		return mDescription;
	}
	public void setDescription(String description) {
		this.mDescription = description;
	}
	public String getLink() {
		return mAddress;
	}
	public void setLink(String link) {
		this.mAddress = link;
	}
	public String getLogoUrl() {
		return mLogoUrl;
	}
	public String getImageUrl() {
		return mImageUrl;
	}
	public String getOpeningHours() {
		return mOpeningHours;
	}
	public String getPhone() {
		return mPhone;
	}
	public String getFoursquareId() {
		return mFoursquareId;
	}
	public double getLatitude() {
		return mLatitude;
	}
	public double getLongitude() {
		return mLongitude;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mId);
		dest.writeString(mTitle);
		dest.writeString(mDescription);
		dest.writeString(mAddress);
		dest.writeString(mLogoUrl);
		dest.writeString(mImageUrl);
		dest.writeString(mOpeningHours);
		dest.writeString(mPhone);
		dest.writeString(mFoursquareId);
		dest.writeDouble(mLatitude);
		dest.writeDouble(mLongitude);
	}

	public int describeContents() {
		return 0;
	}
	
	public static final Parcelable.Creator<Tag> CREATOR = new Parcelable.Creator<Tag>() {

        public Tag createFromParcel(Parcel in) {
            return new Tag(in);
        }

        public Tag[] newArray(int size) {
            return new Tag[size];
        }
    };

    protected Tag(Parcel in) {
    	mId = in.readString();
    	mTitle = in.readString();
    	mDescription = in.readString();
    	mAddress = in.readString();
    	mLogoUrl = in.readString();
    	mImageUrl = in.readString();
    	mOpeningHours = in.readString();
    	mPhone = in.readString();
    	mFoursquareId = in.readString();
    	mLatitude = in.readDouble();
    	mLongitude = in.readDouble();
    }
	

}
