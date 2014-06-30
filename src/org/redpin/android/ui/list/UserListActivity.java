package org.redpin.android.ui.list;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.redpin.android.Constants;
import org.redpin.android.R;
import org.redpin.android.core.User;
import org.redpin.android.json.GsonFactory;
import org.redpin.android.net.HttpGetCommand;
import org.redpin.android.net.InternetConnectionManager;
import org.redpin.android.ui.LocateUserActivity;

import android.app.ListActivity;
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
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class UserListActivity extends ListActivity implements OnItemClickListener {

	ProgressDialog progressDialog;
	List<User> userList;
	private boolean isOnline = false;
	private boolean bLoaded = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.list_view);
		
		progressDialog = new ProgressDialog(this);
		progressDialog.setCancelable(true);
		progressDialog.setIndeterminate(true);
		progressDialog.setMessage("Fetching list of users...");
		
		bindService(new Intent(this, InternetConnectionManager.class), mConnection, Context.BIND_AUTO_CREATE);
		
		registerReceiver(connectionChangeReceiver, new IntentFilter(
				InternetConnectionManager.CONNECTIVITY_ACTION));
	}
	
	@Override
	protected void onDestroy() {
		unbindService(mConnection);
		unregisterReceiver(connectionChangeReceiver);
		
		super.onDestroy();
	}
	
	private class FetchUserTask extends AsyncTask<Void, Void, List<User>> {

		private Gson gson = GsonFactory.getGsonInstance();

		String url;

		public FetchUserTask(String url) {
			this.url = url;
		}

		@Override
		protected void onPreExecute() {
			progressDialog.show();
		}
		
		@Override
		protected List<User> doInBackground(Void... params) {

			List<User> users = new ArrayList<User>();

			try {

				String str = new HttpGetCommand<String>(this.url,
						new String[] {}) {
					@Override
					public String execute() throws IOException, JSONException {
						return streamToString(requestStream());
					}
				}.execute();

				JSONArray arr = new JSONObject(str).getJSONArray("user");
				for (int i = 0; i < arr.length(); i++) {
					User u = gson.fromJson(arr.getJSONObject(i).toString(),
							new TypeToken<User>() {
							}.getType());

					users.add(u);
				}
				
				bLoaded = true;

			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}

			return users;
		}

		@Override
		protected void onPostExecute(List<User> users) {
			progressDialog.cancel();

			if (users != null && users.size() > 0) {
				userList = users;
				setListAdapter(new ArrayAdapter<User>(UserListActivity.this, 
						R.layout.list_row, R.id.list_row_label, users));

				ListView lv = getListView();
				lv.setClickable(true);
				lv.setOnItemClickListener(UserListActivity.this);
				
			} else {
				Toast.makeText(UserListActivity.this,
						"No users found on server", Toast.LENGTH_SHORT)
						.show();
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		
		if(this.userList != null && this.userList.size() > 0) {
			Intent intent = new Intent(UserListActivity.this, LocateUserActivity.class);
			intent.putExtra("userId", this.userList.get(position).getRemoteId());
			intent.putExtra("userName", this.userList.get(position).getUserName());
			intent.putExtra("name", this.userList.get(position).getName());
			
			startActivity(intent);
			
		}
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
				
				if(isOnline && !bLoaded) {
					new FetchUserTask(Constants.FETCH_USER_URL).execute();
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
		}
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
		}
	};
}