package com.example.dropbox_mobiquity;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

public class ViewImageActivity extends Activity {	
	ProgressDialog mProgressDialog;

	public DropboxAPI<AndroidAuthSession> mdropboxApi;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_image);
		String path = getIntent().getStringExtra("imgpath");
		Log.d("DayaDetail", path);
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		ImageView iv = (ImageView) findViewById(R.id.imageView1);

		AppKeyPair appKeyPair = new AppKeyPair(getResources().getString(
				R.string.APP_KEY), getResources()
				.getString(R.string.APP_SECRET));

		AndroidAuthSession session;
		session = new AndroidAuthSession(appKeyPair);
		SharedPreferences prefs = getSharedPreferences(
				MainActivity.DROPBOX_NAME, 0);
		String key = prefs.getString(MainActivity.ACCESS_KEY_NAME, null);
		String secret = prefs.getString(MainActivity.ACCESS_SECRET_NAME, null);
		if (key == null || secret == null || key.length() == 0
				|| secret.length() == 0) {
			return;
		} else {
			session.setOAuth2AccessToken(secret);
		}

		mdropboxApi = new DropboxAPI<AndroidAuthSession>(session);
		new FetchAsyncTask(this, mProgressDialog, mdropboxApi, path, iv)
				.execute();
		/*
		 * mDrawable = Drawable.createFromPath(cachepath);
		 * iv.setImageDrawable(mDrawable);
		 */
	}
}
