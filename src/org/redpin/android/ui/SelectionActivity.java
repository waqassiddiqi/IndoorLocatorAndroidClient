package org.redpin.android.ui;

import org.redpin.android.ApplicationContext;
import org.redpin.android.R;
import org.redpin.android.util.ExceptionReporter;
import org.redpin.android.util.PreferenceUtil;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
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

		btnAdmin = (Button) findViewById(R.id.btnAdmin);
		btnAdmin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				ApplicationContext.serverIP = ((TextView) findViewById(R.id.txtServerIp))
						.getText().toString().trim();
				ApplicationContext.serverPort = ((TextView) findViewById(R.id.txtServerPort))
						.getText().toString().trim();

				Intent intent = new Intent(SelectionActivity.this,
						AdminActivity.class);
				startActivity(intent);
			}
		});

		btnUser = (Button) findViewById(R.id.btnUser);
		btnUser.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				AlertDialog.Builder alert = new AlertDialog.Builder(
						SelectionActivity.this);

				alert.setTitle("Select user");

				final LinearLayout layout = new LinearLayout(
						SelectionActivity.this);
				
				LinearLayout.LayoutParams p =new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.MATCH_PARENT,
						LinearLayout.LayoutParams.WRAP_CONTENT);
				
				layout.setLayoutParams(p);
				layout.setOrientation(LinearLayout.VERTICAL);
				layout.setGravity(Gravity.CENTER_HORIZONTAL);
				
				
				String[] listOfUsers = new String[] { "user1", "user2", "user3" };
				
				final Spinner spinner = new Spinner(SelectionActivity.this);
				ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(SelectionActivity.this, 
						android.R.layout.simple_spinner_item, listOfUsers);
				spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				spinner.setAdapter(spinnerArrayAdapter);
				
				layout.addView(spinner);
				
				alert.setView(layout);

				alert.setPositiveButton("Ok",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								PreferenceUtil.setUsername(spinner.getSelectedItem().toString());
								
								Intent intent = new Intent(SelectionActivity.this, LocateUserActivity.class);
								startActivity(intent);
							}
						});

				alert.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
							}
						});

				alert.show();
			}
		});
	}

}
