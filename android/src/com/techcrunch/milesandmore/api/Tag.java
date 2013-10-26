package com.techcrunch.milesandmore.api;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Tag implements Parcelable {
	
	@SerializedName("tag_id")
	private String mId;
	@SerializedName("name")
	private String mName;
	@SerializedName("description")
	private String mDescription;
	@SerializedName("link")
	private String mLink;
	
	public Tag() {
		
	}
	
	public String getId() {
		return mId;
	}
	public void setId(String id) {
		this.mId = id;
	}
	public String getName() {
		return mName;
	}
	public void setName(String name) {
		this.mName = name;
	}
	public String getDescription() {
		return mDescription;
	}
	public void setDescription(String description) {
		this.mDescription = description;
	}
	public String getLink() {
		return mLink;
	}
	public void setLink(String link) {
		this.mLink = link;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mId);
		dest.writeString(mName);
		dest.writeString(mDescription);
		dest.writeString(mLink);
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
    	mName = in.readString();
    	mDescription = in.readString();
    	mLink = in.readString();
    }
	

}
