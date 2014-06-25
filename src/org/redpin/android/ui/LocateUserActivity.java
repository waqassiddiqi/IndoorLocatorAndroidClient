package org.redpin.android.ui;

import org.redpin.android.R;
import org.redpin.android.core.Location;
import org.redpin.android.core.Measurement;
import org.redpin.android.core.Vector;
import org.redpin.android.core.measure.WiFiReading;
import org.redpin.android.net.wifi.WifiSniffer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

public class LocateUserActivity extends ActionBarActivity {
	
	WifiSniffer mWifiService;
	Location mLocation;
	
	Vector<WiFiReading> vectorWifi;
	Measurement measurement;
	
	private static final String TAG = LocateUserActivity.class.getSimpleName();
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.locate_user_layout);
		
		startWifiSniffer();
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
		}
	};
}
