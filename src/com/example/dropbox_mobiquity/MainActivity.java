package com.example.dropbox_mobiquity;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;

public class MainActivity extends Activity implements LocationListener {

	// Activity constants
	final static public String DROPBOX_NAME = "Mobiquity_Dropbox";
	final static public String ACCESS_TOKEN = "";
	//final static public String APP_KEY = "lozx5cn70lwu2yd";
	//final static public String APP_SECRET = "nolr1uooyddcaq0";
	final static public AccessType ACCESS_TYPE = AccessType.DROPBOX;
	public static final String ACCESS_KEY_NAME = "ACCESS_KEY";
	public static final String ACCESS_SECRET_NAME = "ACCESS_SECRET";
	public final String PHOTO_DIR = "/Photos/";
	public final String PATH_KEY = "Path";
	String newPicFile;
	Button loginBtn, cameraBtn, listBtn;
	public DropboxAPI<AndroidAuthSession> dropboxApi;
	private boolean isUserLoggedIn;
	protected LocationManager locationManager;
	protected LocationListener locationListener;

	String lat;
	String provider;
	protected static double latitude, longitude;
	protected boolean gps_enabled, network_enabled;

	private static final String TAG = "Mobiquity";
	private static final int NEW_PICTURE = 1;
	private String mCameraFileName;
	private String cityName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			mCameraFileName = savedInstanceState.getString("mCameraFileName");
		}
		setContentView(R.layout.activity_main);
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
				0, this);
		loginBtn = (Button) findViewById(R.id.login);

		cameraBtn = (Button) findViewById(R.id.takePhoto);
		listBtn = (Button) findViewById(R.id.listPhotos);

		loggedIn(false);

		AppKeyPair appKeyPair = new AppKeyPair(getResources().getString(R.string.APP_KEY), getResources().getString(R.string.APP_SECRET));

		AndroidAuthSession session;
		session = new AndroidAuthSession(appKeyPair);
		loadAuth(session);

		dropboxApi = new DropboxAPI<AndroidAuthSession>(session);
		loginBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isUserLoggedIn) {
					logOut();
				} else {
					dropboxApi.getSession().startOAuth2Authentication(
							MainActivity.this);
				}
			}
		});

		cameraBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent intent = new Intent();
				// Picture from camera
				intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
				Date date = new Date();
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd-kk-mm-ss",
						Locale.US);
				Geocoder geocoder = new Geocoder(MainActivity.this, Locale
						.getDefault());
				List<Address> addresses;
				try {
					addresses = geocoder
							.getFromLocation(latitude, longitude, 1);
					if (addresses.isEmpty()) {
						Log.d("Daya", "Waiting for Location");
					} else {
						if (addresses.size() > 0) {
							cityName = addresses.get(0).getLocality();
							String stateName = addresses.get(0).getAddressLine(
									1);
							// String countryName = addresses.get(0);
							Log.d("DayaCity", " " + cityName);
							Log.d("DayaState", " " + stateName);
						}

					}

				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				if (cityName != null) {
					newPicFile = cityName+"-"+df.format(date)+ ".jpg";
				} else {
					newPicFile = df.format(date) + ".jpg";
				}

				String outPath = new File(Environment
						.getExternalStorageDirectory(), newPicFile).getPath();
				File outFile = new File(outPath);

				mCameraFileName = outFile.toString();
				Uri outuri = Uri.fromFile(outFile);

				intent.putExtra(MediaStore.EXTRA_OUTPUT, outuri);
				Log.i("Daya", "Importing New Picture: " + mCameraFileName);
				try {
					startActivityForResult(intent, NEW_PICTURE);
				} catch (ActivityNotFoundException e) {
					showToast("There doesn't seem to be a camera.");
				}

			}
		});

		listBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,
						ListActivity.class);
				intent.putExtra(PATH_KEY, PHOTO_DIR);
				intent.putExtra("oauth_key", ACCESS_SECRET_NAME);
				startActivity(intent);
			}
		});
		// Display the proper UI state if logged in or not
		loggedIn(dropboxApi.getSession().isLinked());
	}

	private void loggedIn(boolean userLoggedIn) {
		isUserLoggedIn = userLoggedIn;
		cameraBtn.setEnabled(userLoggedIn);
		cameraBtn.setBackgroundColor(userLoggedIn ? Color.BLUE : Color.GRAY);
		listBtn.setEnabled(userLoggedIn);
		listBtn.setBackgroundColor(userLoggedIn ? Color.BLUE : Color.GRAY);
		loginBtn.setText(userLoggedIn ? "Logout" : "Log in");

	}

	@Override
	protected void onResume() {
		super.onResume();
		AndroidAuthSession session = dropboxApi.getSession();

		if (session.authenticationSuccessful()) {
			try {

				session.finishAuthentication();

				// Store it locally in our app for later use
				storeAuth(session);
				loggedIn(true);
			} catch (IllegalStateException e) {
				showToast("Couldn't authenticate with Dropbox:"
						+ e.getLocalizedMessage());
				Log.i(TAG, "Error authenticating", e);
			}
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString("mCameraFileName", mCameraFileName);
		super.onSaveInstanceState(outState);
	}

	private void showToast(String msg) {
		Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
		error.show();
	}

	private void clearKeys() {
		SharedPreferences prefs = getSharedPreferences(DROPBOX_NAME, 0);
		Editor edit = prefs.edit();
		edit.clear();
		edit.commit();
	}

	private void loadAuth(AndroidAuthSession session) {
		SharedPreferences prefs = getSharedPreferences(DROPBOX_NAME, 0);
		String key = prefs.getString(ACCESS_KEY_NAME, null);
		String secret = prefs.getString(ACCESS_SECRET_NAME, null);
		if (key == null || secret == null || key.length() == 0
				|| secret.length() == 0) {
			return;
		} else {
			session.setOAuth2AccessToken(secret);
		}
	}

	private void storeAuth(AndroidAuthSession session) {
		// Store the OAuth 2 access token, if there is one.
		String oauth2AccessToken = session.getOAuth2AccessToken();
		if (oauth2AccessToken != null) {
			SharedPreferences prefs = getSharedPreferences(DROPBOX_NAME, 0);
			Editor edit = prefs.edit();
			edit.putString(ACCESS_KEY_NAME, "oauth2:");
			edit.putString(ACCESS_SECRET_NAME, oauth2AccessToken);
			edit.commit();
			return;
		}
	}

	// convert from decimal degrees to DMS
	String DMSconv(double coord) {
		coord = (coord > 0) ? coord : (-1) * coord; // -105.9876543 ->
													// 105.9876543
		String sOut = Integer.toString((int) coord) + "/1,"; // 105/1,
		coord = (coord % 1) * 60; // .987654321 * 60 = 59.259258
		sOut = sOut + Integer.toString((int) coord) + "/1,"; // 105/1,59/1,
		coord = (coord % 1) * 6000; // .259258 * 6000 = 1555
		sOut = sOut + Integer.toString((int) coord) + "/1000"; // 105/1,59/1,15555/1000
		return sOut;
	}

	private void logOut() {
		// Remove credentials from the session
		dropboxApi.getSession().unlink();
		// Clear our stored keys
		clearKeys();
		// Change UI state to display logged out version
		loggedIn(false);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == NEW_PICTURE) {
			// return from file upload
			if (resultCode == Activity.RESULT_OK) {
				Uri uri = null;
				if (data != null) {
					uri = data.getData();

				}
				if (uri == null && mCameraFileName != null) {
					uri = Uri.fromFile(new File(mCameraFileName));

				}
				File file = new File(mCameraFileName);
				Log.d("Dayafile", mCameraFileName);
				Log.d("DayaURI", uri.toString());
				writeGPStoExif(file);

				if (uri != null) {
					UploadPicture upload = new UploadPicture(this, dropboxApi,
							PHOTO_DIR, file);
					upload.execute();
				}
			} else {
				Log.w(TAG, "Unknown Activity Result from mediaImport: "
						+ resultCode);
			}
		}
	}

	private void writeGPStoExif(File photo) {
		ExifInterface exif = null;

		try {
			Log.d("latiDouble", "" + latitude);
			Log.d("longiDouble", "" + longitude);
			Log.d("DayaAbspath", "" + photo.getAbsolutePath());
			exif = new ExifInterface(photo.getCanonicalPath());
			Log.d("Photo path", photo.getCanonicalPath());
			if (exif != null) {
				exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE,
						DMSconv(latitude));
				exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE,
						DMSconv(longitude));
				if (latitude > 0)
					exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "N");
				else
					exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "S");
				if (longitude > 0)
					exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "E");
				else
					exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "W");
				exif.saveAttributes();
				String lati = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
				String longi = exif
						.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
				//String make = exif.getAttribute(ExifInterface.TAG_MAKE);
				Log.v("latiResult", "" + lati);
				Log.v("longiResult", "" + longi);

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}

	}

	@Override
	public void onLocationChanged(Location location) {
		Log.d(TAG, "Latitude:" + location.getLatitude() + ", Longitude:"
				+ location.getLongitude());
		latitude = location.getLatitude();
		longitude = location.getLongitude();

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.d("Latitude", "status");
	}

	@Override
	public void onProviderEnabled(String provider) {
		Log.d("Latitude", "enable");

	}

	@Override
	public void onProviderDisabled(String provider) {
		Log.d("Latitude", "disable");
	}

}
