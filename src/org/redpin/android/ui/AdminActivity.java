package org.redpin.android.ui;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.redpin.android.ApplicationContext;
import org.redpin.android.Constants;
import org.redpin.android.R;
import org.redpin.android.core.Location;
import org.redpin.android.core.Map;
import org.redpin.android.core.Measurement;
import org.redpin.android.db.EntityHomeFactory;
import org.redpin.android.json.GsonFactory;
import org.redpin.android.net.HttpGetCommand;
import org.redpin.android.net.HttpPostCommand;
import org.redpin.android.net.wifi.WifiSniffer;
import org.redpin.android.provider.RedpinContract;
import org.redpin.android.ui.list.MapListActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class AdminActivity extends ActionBarActivity {

	private ActionBar actionBar;
	private Button btnAdd, btnDetect, btnManageMap, btnAddMap;

	ProgressDialog progressDialog;

	WifiSniffer mWifiService;
	Location mLocation;

	private static final String TAG = AdminActivity.class.getSimpleName();

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.admin_layout);

		actionBar = getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.show();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME,
				ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setTitle("Indoor Location Tracker");
		actionBar.setSubtitle("beta");

		btnAdd = (Button) findViewById(R.id.btnAdd);
		btnAdd.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(AdminActivity.this,
						AddLocationActivity.class);
				startActivity(intent);
			}
		});

		btnDetect = (Button) findViewById(R.id.btnDetect);
		btnDetect.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				detectCurrentLocation();
			}
		});

		btnManageMap = (Button) findViewById(R.id.btnManageMap);
		btnManageMap.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (getContentResolver().query(RedpinContract.Map.CONTENT_URI,
						new String[] { RedpinContract.Map.NAME }, null, null,
						null).getCount() == 0) {

					new AlertDialog.Builder(AdminActivity.this)
							.setPositiveButton(android.R.string.ok,
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {

											progressDialog
													.setMessage("Fetching maps");
											progressDialog.show();
											new FetchMapTask(
													Constants.FETCH_MAP_URL)
													.execute();

										}
									})
							.setNegativeButton("Cancel",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
										}

									})
							.setTitle("Maps")
							.setMessage(
									"No map founds, search for map on server?")
							.create().show();

				} else {
					Intent intent = new Intent(AdminActivity.this,
							MapListActivity.class);
					startActivity(intent);
				}
			}
		});

		btnAddMap = (Button) findViewById(R.id.btnAddMap);
		btnAddMap.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(AdminActivity.this,
						AddNewMapActivity.class);
				startActivity(intent);
			}
		});

		progressDialog = new ProgressDialog(this);
		progressDialog.setCancelable(true);
		progressDialog.setIndeterminate(true);
		progressDialog.setMessage("Detecting location...");
	}

	private void detectCurrentLocation() {
		progressDialog.show();

		mWifiService.forceMeasurement();
	}

	private void startWifiSniffer() {
		bindService(new Intent(this, WifiSniffer.class), mWifiConnection,
				Context.BIND_AUTO_CREATE);
		registerReceiver(wifiReceiver,
				new IntentFilter(WifiSniffer.WIFI_ACTION));
		Log.i(TAG, "Started WifiSniffer");
	}

	private void stopWifiSniffer() {
		if (mWifiService != null) {
			mWifiService.stopMeasuring();
		}
		unbindService(mWifiConnection);
		unregisterReceiver(wifiReceiver);
		Log.i(TAG, "Stopped WifiSniffer");
	}

	@Override
	protected void onResume() {
		super.onResume();
		startWifiSniffer();
	}
	
	@Override
	protected void onPause() {
		stopWifiSniffer();
		
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		try {
			stopWifiSniffer();
		} catch (Exception e) { }

		super.onDestroy();
	}

	private ServiceConnection mWifiConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			mWifiService = ((WifiSniffer.LocalBinder) service).getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mWifiService = null;
		}
	};

	private BroadcastReceiver wifiReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Measurement m = mWifiService.retrieveLastMeasurement();

			if (m == null)
				return;

			mWifiService.stopMeasuring();

			new ServerTask("http://" + ApplicationContext.serverIP + ":"
					+ ApplicationContext.serverPort
					+ ApplicationContext.applicationName + "/location/find", m)
					.execute();
		}
	};

	private class FetchMapTask extends AsyncTask<Void, Void, List<Map>> {

		private Gson gson = GsonFactory.getGsonInstance();

		String url;

		public FetchMapTask(String url) {
			this.url = url;
		}

		@Override
		protected List<Map> doInBackground(Void... params) {

			List<Map> maps = new ArrayList<Map>();

			try {

				String str = new HttpGetCommand<String>(this.url,
						new String[] {}) {
					@Override
					public String execute() throws IOException, JSONException {
						return streamToString(requestStream());
					}
				}.execute();

				JSONArray arr = new JSONObject(str).getJSONArray("map");
				for (int i = 0; i < arr.length(); i++) {
					Map m = gson.fromJson(arr.getJSONObject(i).toString(),
							new TypeToken<Map>() {
							}.getType());

					maps.add(m);
				}

			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}

			return maps;
		}

		@Override
		protected void onPostExecute(List<Map> maps) {
			progressDialog.cancel();

			if (maps != null && maps.size() > 0) {

				EntityHomeFactory.getMapHome().add(maps);
				Intent intent = new Intent(AdminActivity.this,
						MapListActivity.class);
				startActivity(intent);

			} else {
				Toast.makeText(AdminActivity.this,
						"No maps found, please add new", Toast.LENGTH_SHORT)
						.show();
			}
		}
	}

	private class ServerTask extends AsyncTask<Void, Void, Location> {

		private Gson gson = GsonFactory.getGsonInstance();

		String url;
		Measurement m;

		public ServerTask(String url, Measurement m) {
			this.url = url;
			this.m = m;
		}

		@Override
		protected Location doInBackground(Void... params) {

			try {

				String str = new HttpPostCommand<String>(this.url, gson.toJson(
						this.m, new TypeToken<Measurement>() {
						}.getType())) {
					@Override
					public String execute() throws IOException, JSONException {
						return streamToString(requestStream());
					}
				}.execute();

				return gson.fromJson(str, new TypeToken<Location>() {
				}.getType());

			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Location location) {

			if (location == null) {
				new AlertDialog.Builder(AdminActivity.this)
						.setPositiveButton(android.R.string.ok, null)
						.setTitle("Add new location")
						.setMessage("No matching location find").create()
						.show();
			} else {
				new AlertDialog.Builder(AdminActivity.this)
						.setPositiveButton(android.R.string.ok, null)
						.setTitle("Add new location")
						.setMessage(
								"You are at:" + location.getSymbolicID()
										+ " [accuracy: "
										+ location.getAccuracy() + "]")
						.create().show();
			}
			progressDialog.cancel();
		}
	}
}
