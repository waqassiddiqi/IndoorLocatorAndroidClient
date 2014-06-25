package org.redpin.android.ui;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import org.json.JSONException;
import org.redpin.android.ApplicationContext;
import org.redpin.android.Constants;
import org.redpin.android.R;
import org.redpin.android.core.Location;
import org.redpin.android.core.Measurement;
import org.redpin.android.core.Vector;
import org.redpin.android.core.measure.WiFiReading;
import org.redpin.android.json.GsonFactory;
import org.redpin.android.net.HttpPostCommand;
import org.redpin.android.net.wifi.WifiSniffer;
import org.redpin.android.util.PreferenceUtil;
import org.redpin.base.core.History;
import org.redpin.base.core.User;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.util.FloatMath;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class LocateUserActivity extends ActionBarActivity implements SensorEventListener {
	
	WifiSniffer mWifiService;
	Location mLocation;
	
	Vector<WiFiReading> vectorWifi;
	Measurement measurement;
	TextView txtLocationName;


	private SensorManager sensorMan;
	private Sensor accelerometer;
	
	private float[] mGravity;
	private float mAccel;
	private float mAccelCurrent;
	private float mAccelLast;
	
	private static final String TAG = LocateUserActivity.class.getSimpleName();
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.locate_user_layout);
		
		txtLocationName = (TextView) findViewById(R.id.txtLocationName);
		
		startWifiSniffer();
		
		sensorMan = (SensorManager)getSystemService(SENSOR_SERVICE);
		accelerometer = sensorMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mAccel = 0.00f;
		mAccelCurrent = SensorManager.GRAVITY_EARTH;
		mAccelLast = SensorManager.GRAVITY_EARTH;
	}
	
	@Override
	public void onResume() {
	    super.onResume();
	    sensorMan.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
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
	protected void onPause() {
		sensorMan.unregisterListener(this);
		super.onPause();
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
			
			mWifiService.stopMeasuring();
			
			new ServerTask("http://" + ApplicationContext.serverIP + ":" 
					+ ApplicationContext.serverPort + ApplicationContext.applicationName
					+ "/location/find", m).execute();
		}
	};
	
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
			
			Location loc = null;
			
			try {
				
				String str = new HttpPostCommand<String>(this.url, gson.toJson(this.m, new TypeToken<Measurement>() { }.getType())) {
					@Override
					public String execute() throws IOException, JSONException {
						return streamToString(requestStream());
					}
				}.execute();
				
				
				loc = gson.fromJson(str, new TypeToken<Location>() { }.getType());
				
				if(loc != null && loc.getRemoteId() > 0) {
					History history = new History();
					history.setDate(new Date());
					history.setLocation(loc);
					
					User user = new User();
					user.setUserName(PreferenceUtil.getUsername());				
					history.setUser(user);
					
					try {
						new HttpPostCommand<String>(Constants.ADD_HISTORY_URL, gson.toJson(history, new TypeToken<History>() { }.getType())) {
							@Override
							public String execute() throws IOException, JSONException {
								return streamToString(requestStream());
							}
						}.execute();
					} catch(Exception e) { }
				}
				
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			return loc;
		}
		
		@Override
		protected void onPostExecute(Location location) {
			
			if(location == null) {
				txtLocationName.setTag("Unknown");
			} else {
				txtLocationName.setText(location.getSymbolicID());				
			}
		}
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
	    if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
	        mGravity = event.values.clone();
	        float x = mGravity[0];
	        float y = mGravity[1];
	        float z = mGravity[2];
	        mAccelLast = mAccelCurrent;
	        mAccelCurrent = FloatMath.sqrt(x*x + y*y + z*z);
	        float delta = mAccelCurrent - mAccelLast;
	        mAccel = mAccel * 0.9f + delta;
	        
	        Log.d("onSensorChanged", System.currentTimeMillis()+","+mAccelCurrent +","+mAccel);
	        
	        if(mAccel > 3) {
	        	Toast.makeText(this, "Moved", Toast.LENGTH_SHORT).show();
	        }
	    }

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		
	}
}
