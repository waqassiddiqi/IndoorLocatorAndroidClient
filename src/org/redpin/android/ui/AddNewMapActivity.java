package org.redpin.android.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.channels.FileChannel;

import org.json.JSONException;
import org.redpin.android.ApplicationContext;
import org.redpin.android.R;
import org.redpin.android.core.Map;
import org.redpin.android.db.EntityHomeFactory;
import org.redpin.android.json.GsonFactory;
import org.redpin.android.net.HttpPostCommand;
import org.redpin.android.net.UploadImageTask;
import org.redpin.android.net.UploadImageTask.UploadImageTaskCallback;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class AddNewMapActivity extends ActionBarActivity implements UploadImageTaskCallback {

	ActionBar actionBar;
	Button btnAdd;
	ProgressDialog mProgress;
	final static int REQ_CODE_PICK_IMAGE = 1;
	String attachedFilePath = "";
	ImageView imgViewMap;
	EditText txtMapName;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.add_new_map_layout);
		
		imgViewMap = (ImageView) findViewById(R.id.imgViewMap);
		
		txtMapName = (EditText) findViewById(R.id.txtMapName);
		
		mProgress = new ProgressDialog(this);
		mProgress.setMessage("Uploading file...");
		
		actionBar = getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.show();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME,
				ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setTitle("Indoor Location Tracker");
		actionBar.setSubtitle("Add new map");
		
		findViewById(R.id.btnSelectImage).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
				photoPickerIntent.setType("image/*");
				startActivityForResult(photoPickerIntent, REQ_CODE_PICK_IMAGE);
			}
		});
		
		findViewById(R.id.btnAdd).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if(txtMapName.getText().toString().trim().length() <= 0) {
					Toast.makeText(AddNewMapActivity.this, "Please select image to upload", 
							Toast.LENGTH_SHORT).show();
					return ;
				}
				
				mProgress.show();
				
				UploadImageTask task = new UploadImageTask(AddNewMapActivity.this);
				task.execute(attachedFilePath);
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent imageReturnedIntent) {
		super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

		switch (requestCode) {
		case REQ_CODE_PICK_IMAGE:
			if (resultCode == RESULT_OK) {
				Uri selectedImage = imageReturnedIntent.getData();
				String[] filePathColumn = { MediaStore.Images.Media.DATA };

				Cursor cursor = getContentResolver().query(selectedImage,
						filePathColumn, null, null, null);
				cursor.moveToFirst();

				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				String filePath = cursor.getString(columnIndex);
				cursor.close();

				attachedFilePath = filePath;
				
				Drawable image = Drawable.createFromPath(attachedFilePath);
				imgViewMap.setImageDrawable(image);
			}
		}
	}

	@Override
	public void onImageUploaded(String path) {
		
		Map m = new Map();
		m.setMapName(txtMapName.getText().toString().trim());
		m.setMapURL(path);
		
		new ServerTask("http://" + ApplicationContext.serverIP + ":" 
				+ ApplicationContext.serverPort + ApplicationContext.applicationName
				+ ApplicationContext.apiPath
				+ "/map", m).execute();
	}

	@Override
	public void onImageUploadFailure() {
	}
	
	private class ServerTask extends AsyncTask<Void, Void, Map> {

		private Gson gson = GsonFactory.getGsonInstance();

		String url;
		Map m;

		public ServerTask(String url, Map m) {
			this.url = url;
			this.m = m;
		}

		@Override
		protected Map doInBackground(Void... params) {

			try {

				String str = new HttpPostCommand<String>(this.url, gson.toJson(this.m, new TypeToken<Map>() {}.getType())) {

					@Override
					public String execute() throws IOException, JSONException {
						return streamToString(requestStream());
					}
				}.execute();

				return gson.fromJson(str, new TypeToken<Map>() {}.getType());

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
		protected void onPostExecute(Map m) {
			mProgress.hide();
			
			if(m != null && m.getRemoteId() > 0) {
				m = EntityHomeFactory.getMapHome().add(m);
				Toast.makeText(AddNewMapActivity.this, "New map has been added", Toast.LENGTH_SHORT).show();
				
				try {
					copyMapImage(m.getMapURL());
				} catch (Exception e) { }
			}
		}
	}
	
	private void copyMapImage(String destFileName) throws IOException {
		
		File direct = new File(Environment.getExternalStorageDirectory() + "/indoormaps");
		if (!direct.exists()) {
			direct.mkdirs();
		}
		
		FileInputStream inStream = new FileInputStream(new File(attachedFilePath));
	    FileOutputStream outStream = new FileOutputStream(new 
	    		File(Environment.getExternalStorageDirectory() + "/indoormaps/" + destFileName));
	    FileChannel inChannel = inStream.getChannel();
	    FileChannel outChannel = outStream.getChannel();
	    inChannel.transferTo(0, inChannel.size(), outChannel);
	    inStream.close();
	    outStream.close();
	}
}