package org.redpin.android.ui;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.redpin.android.Constants;
import org.redpin.android.R;
import org.redpin.android.core.Location;
import org.redpin.android.core.Measurement;
import org.redpin.android.core.User;
import org.redpin.android.core.Vector;
import org.redpin.android.core.measure.WiFiReading;
import org.redpin.android.json.GsonFactory;
import org.redpin.android.net.HttpPostCommand;
import org.redpin.android.net.InternetConnectionManager;
import org.redpin.android.net.wifi.WifiSniffer;
import org.redpin.base.core.History;
import org.redpin.base.core.Task;

import android.app.AlertDialog;
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
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
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
	Spinner spinnerStatus, spinnerPriority, spinnerTransportType;
	Button btnRefresh;
	
	User mCurrentUser;
	
	private boolean isOnline = false;
	
	private SensorManager sensorMan;
	private Sensor accelerometer;
	
	private float[] mGravity;
	private float mAccel;
	private float mAccelCurrent;
	private float mAccelLast;
	private Timer mTimer;
	
	private static final String TAG = LocateUserActivity.class.getSimpleName();
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.locate_user_layout);
		
		txtLocationName = (TextView) findViewById(R.id.txtLocationName);
		spinnerStatus = (Spinner) findViewById(R.id.spinnerStatus);
		spinnerPriority = (Spinner) findViewById(R.id.spinnerPriority);
		spinnerTransportType = (Spinner) findViewById(R.id.spinnerTransportType);
		btnRefresh = (Button) findViewById(R.id.btnRefresh);
		
		mTimer = new Timer();
		
		btnRefresh.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				mTimer.cancel();
				
				if(mWifiService != null) {
					mWifiService.forceMeasurement();
				}
			}
		});
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item) {

		    @Override
		    public View getView(int position, View convertView, ViewGroup parent) {

		        View v = super.getView(position, convertView, parent);
		        if (position == getCount()) {
		            ((TextView)v.findViewById(android.R.id.text1)).setText("");
		            ((TextView)v.findViewById(android.R.id.text1)).setHint(getItem(getCount())); //"Hint to be displayed"
		        }

		        ((TextView)v.findViewById(android.R.id.text1)).setGravity(Gravity.CENTER);
		        
		        return v;
		    }       

		    @Override
		    public int getCount() {
		        return super.getCount()-1; // you dont display last item. It is used as hint.
		    }

		};

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		adapter.add("Porter dispatched");
		adapter.add("Porter arrives");
		adapter.add("Begin move");
		adapter.add("Complete move");
		adapter.add("Porter clears");
		
		adapter.add("Job Status");

		spinnerStatus.setAdapter(adapter);
		spinnerStatus.setSelection(adapter.getCount()); 
		
		mCurrentUser = new User();
		
		if(getIntent().hasExtra("userId")) {
			mCurrentUser.setRemoteId(getIntent().getIntExtra("userId", 0));
			mCurrentUser.setUserName(getIntent().getStringExtra("userName"));
			mCurrentUser.setName(getIntent().getStringExtra("name"));
		}
		
		bindTransportTypeSpinner();
		bindPrioritySpinner();
		startWifiSniffer();
		
		new AlertDialog.Builder(this).setPositiveButton(
			android.R.string.ok, null)
			.setTitle("User Tracking").setMessage("Once connection to server is established, select task, press Start and roam around").create().show();
		
		bindService(new Intent(this, InternetConnectionManager.class), mConnection, Context.BIND_AUTO_CREATE);
		
		registerReceiver(connectionChangeReceiver, new IntentFilter(
				InternetConnectionManager.CONNECTIVITY_ACTION));
		
		btnRefresh.setEnabled(false);
		
		/*sensorMan = (SensorManager)getSystemService(SENSOR_SERVICE);
		accelerometer = sensorMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mAccel = 0.00f;
		mAccelCurrent = SensorManager.GRAVITY_EARTH;
		mAccelLast = SensorManager.GRAVITY_EARTH;*/
	}
	
	private void bindPrioritySpinner() {
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item) {

		    @Override
		    public View getView(int position, View convertView, ViewGroup parent) {

		        View v = super.getView(position, convertView, parent);
		        if (position == getCount()) {
		            ((TextView)v.findViewById(android.R.id.text1)).setText("");
		            ((TextView)v.findViewById(android.R.id.text1)).setHint(getItem(getCount())); //"Hint to be displayed"
		        }

		        ((TextView)v.findViewById(android.R.id.text1)).setGravity(Gravity.CENTER);
		        
		        return v;
		    }       

		    @Override
		    public int getCount() {
		        return super.getCount()-1; // you dont display last item. It is used as hint.
		    }

		};

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		adapter.add("STAT");
		adapter.add("ASAP");
		adapter.add("Routine");
		
		adapter.add("Priority Level");

		spinnerPriority.setAdapter(adapter);
		spinnerPriority.setSelection(adapter.getCount());
	}
	
	private void bindTransportTypeSpinner() {
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item) {

		    @Override
		    public View getView(int position, View convertView, ViewGroup parent) {

		        View v = super.getView(position, convertView, parent);
		        if (position == getCount()) {
		            ((TextView)v.findViewById(android.R.id.text1)).setText("");
		            ((TextView)v.findViewById(android.R.id.text1)).setHint(getItem(getCount())); //"Hint to be displayed"
		        }

		        ((TextView)v.findViewById(android.R.id.text1)).setGravity(Gravity.CENTER);
		        
		        return v;
		    }       

		    @Override
		    public int getCount() {
		        return super.getCount()-1; // you dont display last item. It is used as hint.
		    }

		};

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		adapter.add("Patient in wheel chair");
		adapter.add("Patient in bed");
		adapter.add("Chart");
		adapter.add("Blood products");
		adapter.add("Equipment");
		
		adapter.add("Transport Type");

		spinnerTransportType.setAdapter(adapter);
		spinnerTransportType.setSelection(adapter.getCount());
	}
	
	@Override
	public void onResume() {
	    super.onResume();
	    //sensorMan.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
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
		//sensorMan.unregisterListener(this);
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		stopWifiSniffer();
		
		unbindService(mConnection);
		unregisterReceiver(connectionChangeReceiver);
		
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
			
			scheduleScan();
			
			new ServerTask(Constants.FIND_LOCATION_URL, m).execute();
		}
	};
	
	private void scheduleScan() {
		
		mTimer = null;
		mTimer = new Timer();
		mTimer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				mWifiService.forceMeasurement();
			}
		}, 2000);
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
					
					
					history.setUser(mCurrentUser);
					
					Task task = new Task();
					task.setComment("");
					task.setTransportType(spinnerTransportType.getSelectedItem() == null ? "" : spinnerTransportType.getSelectedItem().toString());
					task.setPriority(spinnerPriority.getSelectedItem() == null ? "" : spinnerPriority.getSelectedItem().toString());
					task.setJobStatus(spinnerStatus.getSelectedItem() == null ? "" : spinnerStatus.getSelectedItem().toString());
					
					history.setTask(task);
					
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
			
			if(location != null && location.getSymbolicID() != null) {
				txtLocationName.setText(location.getSymbolicID());
			} else {
				txtLocationName.setText("Unknown Location");				
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

	/**
	 * {@link InternetConnectionManager} {@link BroadcastReceiver} for
	 * retrieving Internet connection changes.
	 */
	private BroadcastReceiver connectionChangeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.hasExtra("isOnline")) {
				isOnline = intent.getBooleanExtra("isOnline", false);
				
				if(isOnline) {
					btnRefresh.setEnabled(true);
				}
			}
		}
	};
	
	/**
	 * {@link InternetConnectionManager} {@link ServiceConnection} to check current online state
	 */
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			InternetConnectionManager mManager = ((InternetConnectionManager.LocalBinder)service).getService();
			isOnline = mManager.isOnline();
			
			if(isOnline) {
					btnRefresh.setEnabled(true);
			}
		}
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
		}
		
	};
}