package com.example.dropbox_mobiquity;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;

public class PhotoAsyncTask extends AsyncTask<Void, Void, ArrayList<Photo>> {

	ListActivity activity;
	ProgressDialog mProgressDialog;

	private String mPath;
	private DropboxAPI<?> mApi;
	private String mErrorMsg;
	ArrayList<Photo> photolist;

	public PhotoAsyncTask(ListActivity mactivity,
			ProgressDialog mProgressDialog,
			DropboxAPI<AndroidAuthSession> mdropboxApi, String path) {
		super();
		this.activity = mactivity;
		this.mProgressDialog = mProgressDialog;

		this.mApi = mdropboxApi;
		this.mPath = path;
	}

	@Override
	protected ArrayList<Photo> doInBackground(Void... params) {

		try {
			// Get the metadata for a directory
			Entry dirent = mApi.metadata(mPath, 1000, null, true, null);
			if (!dirent.isDir || dirent.contents == null) {
				// It's not a directory, or there's nothing in it
				mErrorMsg = "File or empty directory";
				return null;
			}

			// Make a list of everything in it that we can get a thumbnail for
			photolist = new ArrayList<Photo>();
			for (Entry ent : dirent.contents) {
				if (ent.thumbExists) {
					Photo photo = new Photo();
					photo.setTitle(ent.fileName());
					photo.setImagepath(ent.path);
					photolist.add(photo);

				}
			}

			if (photolist.size() == 0) {
				// No thumbs in that directory
				mErrorMsg = "No pictures in that directory";
				return null;
			}
		} catch (DropboxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return photolist;
	}

	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
		mProgressDialog.setMessage("Loading Data...");
		mProgressDialog.show();
	}

	@Override
	protected void onPostExecute(ArrayList<Photo> result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		mProgressDialog.dismiss();
		activity.setupData(result);
		// result.clear();
	}

}
