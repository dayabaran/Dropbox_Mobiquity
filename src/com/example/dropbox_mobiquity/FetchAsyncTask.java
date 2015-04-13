package com.example.dropbox_mobiquity;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import android.app.ProgressDialog;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.ThumbFormat;
import com.dropbox.client2.DropboxAPI.ThumbSize;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;

public class FetchAsyncTask extends AsyncTask<Void, Void, Void> {

	private final static String IMAGE_FILE_NAME = "Mobiquity.png";
	private FileOutputStream mFos;

	ViewImageActivity activity;
	Drawable mDrawable;
	ProgressDialog mProgressDialog;	
	private String mPath;
	private DropboxAPI<?> mApi;
	ImageView mView;

	public FetchAsyncTask(ViewImageActivity mactivity,
			ProgressDialog mProgressDialog,
			DropboxAPI<AndroidAuthSession> mdropboxApi, String path,
			ImageView iv) {
		super();
		this.activity = mactivity;
		this.mProgressDialog = mProgressDialog;
		this.mApi = mdropboxApi;
		this.mPath = path;
		this.mView = iv;
	}

	@Override
	protected Void doInBackground(Void... params) {
		String cachePath = activity.getCacheDir().getAbsolutePath() + "/"
				+ IMAGE_FILE_NAME;
		try {
			mFos = new FileOutputStream(cachePath);
		} catch (FileNotFoundException e) {
			Log.d("DayaMobiquity", "No File found");

		}

		// This downloads a smaller, thumbnail version of the file. The
		// API to download the actual file is roughly the same.
		try {
			mApi.getThumbnail(mPath, mFos, ThumbSize.BESTFIT_960x640,
					ThumbFormat.JPEG, null);
			mDrawable = Drawable.createFromPath(cachePath);
		} catch (DropboxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
		mProgressDialog.setMessage("Loading Data...");
		mProgressDialog.show();
	}

	@Override
	protected void onPostExecute(Void result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		mProgressDialog.dismiss();
		mView.setImageDrawable(mDrawable);
	}

}
