package com.techcrunch.milesandmore;

import java.util.Locale;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.techcrunch.milesandmore.api.Tag;

public class TagDetails extends Activity {

	private Tag mTag;

	private TextView mTitle;
	private TextView mOpeningHours;
	private TextView mDescription;
	private ImageView mLogo;
	private ImageView mImage;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_tag_details);

		mTag = getIntent().getParcelableExtra("tag");

		mTitle = (TextView) findViewById(R.id.title);
		mTitle.setText(mTag.getName());
		mDescription = (TextView) findViewById(R.id.description);
		mDescription.setText(mTag.getDescription());
		mOpeningHours = (TextView) findViewById(R.id.openingHours);
		String[] opening = mTag.getOpeningHours().split(",");
		StringBuilder builder = new StringBuilder();
		for (String time : opening) {
			builder.append(time.trim()).append("\n");
		}
		mOpeningHours.setText(builder.toString().trim());
		mLogo = (ImageView) findViewById(R.id.logo);
		BitmapCache.getInstance(getBaseContext()).loadImage(mTag.getLogoUrl(), mLogo, null);
		mImage = (ImageView) findViewById(R.id.image);
		BitmapCache.getInstance(getBaseContext()).loadImage(mTag.getImageUrl(), mImage, null);

		findViewById(R.id.checkIn).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse("http://foursquare.com/venue/" + mTag.getFoursquareId()));
				startActivity(intent);
			}
		});

		findViewById(R.id.openInMaps).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				String uri = String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f(%s)", mTag.getLatitude(), mTag.getLongitude(),
						mTag.getLatitude(), mTag.getLongitude(), mTag.getName());
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
				startActivity(intent);
			}
		});
	}
}
