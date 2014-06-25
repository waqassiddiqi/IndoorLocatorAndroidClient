package org.redpin.android.ui;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.redpin.android.ApplicationContext;
import org.redpin.android.R;
import org.redpin.android.json.GsonFactory;
import org.redpin.android.net.HttpPostCommand;
import org.redpin.base.core.User;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class AddUserActivity extends ActionBarActivity {

	ProgressDialog progressDialog;
	
	private static final String TAG = AddUserActivity.class.getSimpleName();
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.add_user_layout);
		
		progressDialog = new ProgressDialog(this);
		progressDialog.setCancelable(true);
		progressDialog.setIndeterminate(true);
		progressDialog.setMessage("Please wait...");
		
		findViewById(R.id.btnAdd).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				addNewUser();
			}
		});
	}
	
	
	private void addNewUser() {
		
		EditText txtUsername = (EditText) findViewById(R.id.txtUsername);
		EditText txtName = (EditText) findViewById(R.id.txtName);
		
		if(txtUsername.getText().toString().trim().length() <= 0) {
			new AlertDialog.Builder(this).setPositiveButton(
					android.R.string.ok, null)
					.setTitle("Add new user").setMessage("Username can't be empty").create().show();
			return;
		}
		
		if(txtName.getText().toString().trim().length() <= 0) {
			new AlertDialog.Builder(this).setPositiveButton(
					android.R.string.ok, null)
					.setTitle("Add new user").setMessage("Name can't be empty").create().show();
			return;
		}
		
		progressDialog.show();
		
		User user = new User();
		user.setName(txtName.getText().toString().trim());
		user.setUserName(txtUsername.getText().toString().trim());
		
		new ServerTask("http://" + ApplicationContext.serverIP + ":" 
				+ ApplicationContext.serverPort + ApplicationContext.applicationName
				+ ApplicationContext.apiPath
				+ "/user", user).execute();
	}
	
	private class ServerTask extends AsyncTask<Void, Void, User> {

		private Gson gson = GsonFactory.getGsonInstance();
		
		String url;
		User u;
		
		public ServerTask(String url, User u) {
			this.url = url;
			this.u = u;
		}
		
		@Override
		protected User doInBackground(Void... params) {
			
			try {
				
				String str = new HttpPostCommand<String>(this.url, gson.toJson(this.u, new TypeToken<User>() { }.getType())) {
					@Override
					public String execute() throws IOException, JSONException {
						return streamToString(requestStream());
					}
				}.execute();
				
				
				gson.fromJson(str, new TypeToken<User>() { }.getType());
				
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
		protected void onPostExecute(User user) {
			progressDialog.cancel();
			
			
		}
	}
}