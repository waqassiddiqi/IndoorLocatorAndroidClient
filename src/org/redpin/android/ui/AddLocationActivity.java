package org.redpin.android.ui;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.redpin.android.ApplicationContext;
import org.redpin.android.R;
import org.redpin.android.core.Fingerprint;
import org.redpin.android.core.Location;
import org.redpin.android.core.Map;
import org.redpin.android.core.Measurement;
import org.redpin.android.core.Vector;
import org.redpin.android.core.measure.WiFiReading;
import org.redpin.android.json.GsonFactory;
import org.redpin.android.net.HttpPostCommand;
import org.redpin.android.net.wifi.WifiSniffer;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class AddLocationActivity extends ActionBarActivity {

	ProgressDialog progressDialog;

	WifiSniffer mWifiService;
	Location mLocation;
	
	//WiFiReading wfreading, wfreading2, wfreading3;
	Vector<WiFiReading> vectorWifi;
	Measurement measurement;
	
	private static final String TAG = AddLocationActivity.class.getSimpleName();
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.add_location_layout);
		
		startWifiSniffer();
		
		progressDialog = new ProgressDialog(this);
		progressDialog.setCancelable(true);
		progressDialog.setIndeterminate(true);
		progressDialog.setMessage("Taking readings...");
		
		findViewById(R.id.btnAdd).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				addNewLocation();
			}
		});
	}
	
	private void addNewLocation() {
		
		EditText txtLocationName = (EditText) findViewById(R.id.txtLocationName);
		EditText txtFloor = (EditText) findViewById(R.id.txtFloor);
		
		if(txtLocationName.getText().toString().trim().length() <= 0) {
			new AlertDialog.Builder(this).setPositiveButton(
					android.R.string.ok, null)
					.setTitle("Add new location").setMessage("Location name can't be empty").create().show();
			Log.w(TAG, "addNewLocation: location name not provided");
			return;
		}
		
		progressDialog.show();
		
		Location location = new Location();
		location.setSymbolicID(txtLocationName.getText().toString().trim());
		
		Map map = new Map();
		map.setRemoteId(1);
		map.setLocalId(1);
		
		location.setMap(map);
		
		mLocation = location;
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
	protected void onDestroy() {
		stopWifiSniffer();
		
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
			
			if (mLocation != null) {
				
				mWifiService.stopMeasuring();
				
				Fingerprint fp = new Fingerprint(mLocation, m);
				
				new ServerTask("http://" + ApplicationContext.serverIP + ":" 
						+ ApplicationContext.serverPort + ApplicationContext.applicationName
						+ "/fingerprint", fp).execute();
			}
		}
	};
	
	private class ServerTask extends AsyncTask<Void, Void, Fingerprint> {

		private Gson gson = GsonFactory.getGsonInstance();
		
		String url;
		Fingerprint p;
		
		public ServerTask(String url, Fingerprint p) {
			this.url = url;
			this.p = p;
		}
		
		@Override
		protected Fingerprint doInBackground(Void... params) {
			
			try {
				
				String str = new HttpPostCommand<String>(this.url, gson.toJson(this.p, new TypeToken<Fingerprint>() { }.getType())) {
					@Override
					public String execute() throws IOException, JSONException {
						return streamToString(requestStream());
					}
				}.execute();
				
				
				gson.fromJson(str, new TypeToken<Fingerprint>() { }.getType());
				
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
		protected void onPostExecute(Fingerprint obj) {
			progressDialog.cancel();
		}
	}
}