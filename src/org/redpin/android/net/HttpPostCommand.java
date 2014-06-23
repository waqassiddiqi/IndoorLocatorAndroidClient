package org.redpin.android.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;
import org.redpin.android.Constants;

import android.util.Log;

public abstract class HttpPostCommand<T> extends HttpCommand<T> {
	
	private HttpPost httpPost;
	
	public HttpPostCommand(String fullUrl, JSONObject params) throws UnsupportedEncodingException {
		super();
		
		httpPost = new HttpPost(fullUrl);
		httpPost.setHeader("Content-Type", "application/json");
		httpPost.setEntity(new StringEntity(params.toString(), "UTF8"));
	}
	
	public HttpPostCommand(String fullUrl, String params) throws UnsupportedEncodingException {
		super();
		
		httpPost = new HttpPost(fullUrl);
		httpPost.setHeader("Content-Type", "application/json");
		httpPost.setEntity(new StringEntity(params.toString(), "UTF8"));
	}

	@Override
	protected InputStream requestStream() throws IOException {
		HttpResponse response;
		try {
			response = getClient().execute(httpPost, getLocalContext());
			return response.getEntity().getContent();
		} catch (IOException e) {
			if (!canceled) {
				Log.e(Constants.TAG, "HttpPostCommand.requestStream() -> IOException", e);
			}
			throw e;
		}
	}
	
	@Override
	public void cancel() {
		super.cancel();
		
		if(httpPost != null)
			httpPost.abort();
	}	
}