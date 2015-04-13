package com.example.dropbox_mobiquity;

import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;

public class PhotoAdapter extends ArrayAdapter<Photo>{


	Context context;
	int mresource;
	ArrayList<Photo> data;
	private DropboxAPI<?> mApi;
	   
	public PhotoAdapter(Context context, int resource, ArrayList<Photo> objects, DropboxAPI<AndroidAuthSession> mdropboxApi) {
		super(context, resource, objects);
		this.context = context;
		this.mresource = resource;
		this.data = objects;
		this.mApi =mdropboxApi;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null)
		{
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(mresource, parent, false);
		}
		Photo photo = data.get(position);
		TextView desc = (TextView) convertView.findViewById(R.id.imagetext);
		desc.setText(photo.getTitle());
		//ImageView thumbnail = (ImageView) convertView.findViewById(R.id.rowimage);		
		//Picasso.with(this.context).load(photo.getImagepath()).into(thumbnail);
		
		return convertView;
	}

}
