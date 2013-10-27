package com.techcrunch.milesandmore;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.techcrunch.milesandmore.api.Tag;

public class TagDetailsFragment extends Fragment {

	public TagDetailsFragment getInstance(Tag tag) {
		TagDetailsFragment fragment = new TagDetailsFragment();
		Bundle args = new Bundle();
		args.putParcelable("tag", tag);
		fragment.setArguments(args);
		return fragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle savedInstanceState) {
		return null;
	}
	
	public void onActivityCreated(Bundle savedInstanceState) {
		
	}
}
