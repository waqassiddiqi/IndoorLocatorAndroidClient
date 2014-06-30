package org.redpin.android.ui;

import org.redpin.android.ApplicationContext;
import org.redpin.android.R;
import org.redpin.android.ui.list.UserListActivity;
import org.redpin.android.util.ExceptionReporter;
import org.redpin.android.util.PreferenceUtil;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class SelectionActivity extends ActionBarActivity {

	private ActionBar actionBar;
	private Button btnAdmin, btnUser;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ApplicationContext.init(getApplicationContext());
		ExceptionReporter.register(this);

		setContentView(R.layout.main_layout);

		actionBar = getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.show();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME,
				ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setTitle("Indoor Location Tracker");
		actionBar.setSubtitle("beta");

		((TextView) findViewById(R.id.txtServerIp)).setText(PreferenceUtil.getServerIP());
		
		btnAdmin = (Button) findViewById(R.id.btnAdmin);
		btnAdmin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				ApplicationContext.serverIP = ((TextView) findViewById(R.id.txtServerIp))
						.getText().toString().trim();
				ApplicationContext.serverPort = ((TextView) findViewById(R.id.txtServerPort))
						.getText().toString().trim();

				PreferenceUtil.setServerIP(ApplicationContext.serverIP);
				
				Intent intent = new Intent(SelectionActivity.this,
						AdminActivity.class);
				startActivity(intent);
			}
		});

		btnUser = (Button) findViewById(R.id.btnUser);
		btnUser.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				ApplicationContext.serverIP = ((TextView) findViewById(R.id.txtServerIp))
						.getText().toString().trim();
				ApplicationContext.serverPort = ((TextView) findViewById(R.id.txtServerPort))
						.getText().toString().trim();

				PreferenceUtil.setServerIP(ApplicationContext.serverIP);
				
				Intent intent = new Intent(SelectionActivity.this, UserListActivity.class);
				startActivity(intent);
			}
		});
	}

}
