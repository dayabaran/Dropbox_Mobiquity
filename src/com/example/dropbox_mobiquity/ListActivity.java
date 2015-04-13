package com.example.dropbox_mobiquity;

import java.util.ArrayList;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;

public class ListActivity extends Activity {

	ListView lv;
	ProgressDialog mProgressDialog;
	public DropboxAPI<AndroidAuthSession> mdropboxApi;
	PhotoAdapter adapter;
	ArrayList<Photo> list;	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list);
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		lv = (ListView) findViewById(R.id.listView1);
		String path = getIntent().getStringExtra("Path");
		Log.d("Daya-path", path);

		AppKeyPair appKeyPair = new AppKeyPair(getResources().getString(R.string.APP_KEY), getResources().getString(R.string.APP_SECRET));

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
		new PhotoAsyncTask(this, mProgressDialog, mdropboxApi, path).execute();
	}

	public void setupData(ArrayList<Photo> files) {
		final ArrayList<Photo> titles = new ArrayList<Photo>();
		for (Photo str : files) {
			Log.d("Daya-mainfile", str.getTitle());
			Log.d("Daya-mainfile-path", str.getImagepath());
			titles.add(str);
		}

		adapter = new PhotoAdapter(ListActivity.this, R.layout.imglayout,
				files, mdropboxApi);
		lv.setAdapter(adapter);

		list = new ArrayList<Photo>();
		list = files;
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String path = list.get(position).getImagepath();

				Intent i = new Intent(getApplicationContext(),
						ViewImageActivity.class);
				i.putExtra("imgpath", path);
				startActivity(i);

			}
		});

	}
}
