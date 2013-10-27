package com.techcrunch.milesandmore.api;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class Company implements Parcelable {
	
	private String name;
	private String image;
	private String distance;
	
	private List<Department> departments;
	
	public Company() {
		departments = new ArrayList<Department>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getDistance() {
		return distance;
	}

	public void setDistance(String distance) {
		this.distance = distance;
	}

	public List<Department> getDepartments() {
		return departments;
	}

	public void addDepartment(Department department) {
		this.departments.add(department);
	}
	

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeString(image);
		dest.writeString(distance);
	}

	public int describeContents() {
		return 0;
	}
	
	public static final Parcelable.Creator<Company> CREATOR = new Parcelable.Creator<Company>() {

        public Company createFromParcel(Parcel in) {
            return new Company(in);
        }

        public Company[] newArray(int size) {
            return new Company[size];
        }
    };

    protected Company(Parcel in) {
    	name = in.readString();
    	image = in.readString();
    	distance = in.readString();
    }
	
	

}
