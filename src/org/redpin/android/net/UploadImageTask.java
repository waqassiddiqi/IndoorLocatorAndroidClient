/**
 *  Filename: UploadImageTask.java (in org.repin.android.net)
 *  This file is part of the Redpin project.
 * 
 *  Redpin is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published
 *  by the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  Redpin is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Redpin. If not, see <http://www.gnu.org/licenses/>.
 *
 *  (c) Copyright ETH Zurich, Luba Rogoleva, Pascal Brogle, Philipp Bolliger, 2010, ALL RIGHTS RESERVED.
 * 
 *  www.redpin.org
 */
package org.redpin.android.net;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.redpin.android.ApplicationContext;

import android.os.AsyncTask;
import android.util.Log;
import android.view.WindowManager.BadTokenException;

/**
 * {@link AsyncTask} for uploading images in the background
 * 
 * @author Pascal Brogle (broglep@student.ethz.ch)
 * @author Luba Rogoleva (lubar@student.ethz.ch)
 *
 */
public class UploadImageTask extends AsyncTask<String, Void, String> {
	
	private static final String TAG = DownloadImageTask.class.getSimpleName();

	private UploadImageTaskCallback callback;

	public UploadImageTask() {
	}

	public UploadImageTask(UploadImageTaskCallback callback) {
		this.callback = callback;
	}

	/**
	 * Uploads a local image to the redpin server.
	 * 
	 * @param params
	 *            path of the local image to be uploaded (only first is considered)
	 * @return URL of the uploaded image
	 */
	@Override
	protected String doInBackground(String... params) {
		String localFilePath = params[0];
		if (localFilePath == null) {
			return null;
		}
		
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("filename", localFilePath));
		String response = "";
		try {
			
			response = uploadToSever("http://" + ApplicationContext.serverIP + ":" 
					+ ApplicationContext.serverPort + ApplicationContext.applicationName
					+ "/MapUpload", nameValuePairs);


			return response;
			
		} catch (MalformedURLException ex) {
			Log.w(TAG, "error: " + ex.getMessage(), ex);
		} catch (IOException ioe) {
			Log.w(TAG, "error: " + ioe.getMessage(), ioe);
		} finally {
			
		}
		return null;
	}
	
	
	/**
	 * Calls the callback (if supplied) after the image is uploaded
	 * 
	 * @param result
	 *            URL of the uploaded image
	 */
	@Override
	protected void onPostExecute(String result) {
		if (callback != null) {
			try {
				if (result != null && result.startsWith("OK")) {
					callback.onImageUploaded(result.replace("OK|", ""));
				} else {
					callback.onImageUploadFailure();
				}
			} catch (BadTokenException e) {
				Log.w(TAG, "Callback failed, caught BadTookenException: " + e.getMessage(), e);
			} catch (Exception e) {
				Log.w(TAG, "Callback failed, caught Exception: " + e.getMessage(), e);
			}
			callback = null;
		}
	}

	/**
	 * Callback Interface for {@link UploadImageTask}
	 * 
	 * @author Pascal Brogle (broglep@student.ethz.ch)
	 * 
	 */
	public interface UploadImageTaskCallback {
		public void onImageUploaded(String path);
		public void onImageUploadFailure();
	}

	private static String uploadToSever(String url, List<NameValuePair> nameValuePairs) throws IOException {
		HttpClient httpClient = new DefaultHttpClient();
	    HttpContext localContext = new BasicHttpContext();
	    HttpPost httpPost = new HttpPost(url);

	    try {
	    	MultipartEntityBuilder builder = MultipartEntityBuilder.create();    
	    	builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

	        for(int index=0; index < nameValuePairs.size(); index++) {
	            if(nameValuePairs.get(index).getName().equalsIgnoreCase("filename")) {
	            	builder.addPart(nameValuePairs.get(index).getName(), new FileBody(new File (nameValuePairs.get(index).getValue())));
	            } else {
	            	builder.addPart(nameValuePairs.get(index).getName(), new StringBody(nameValuePairs.get(index).getValue()));
	            }
	        }

	        HttpEntity entity = builder.build();
	        httpPost.setEntity(entity);

	        HttpResponse response = httpClient.execute(httpPost, localContext);
	        
	        HttpEntity responseEntity = response.getEntity();
	        
	        String strResponse = "";
	        
	        if(responseEntity != null) {
	        	strResponse = EntityUtils.toString(responseEntity);
	        }
	        
	        return strResponse;
	        
	    } catch (IOException e) {
	        throw e;
	    } finally {
	    	httpPost = null;
	    	httpClient = null;
	    }
	}
}