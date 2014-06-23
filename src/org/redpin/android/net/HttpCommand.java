package org.redpin.android.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.redpin.android.Constants;

import android.util.Log;

public abstract class HttpCommand<T> {
	private static HttpClient client;
	protected boolean canceled = false;
	protected static HttpContext localContext;
	
	public HttpCommand() {
		if(client == null) {
			client = new DefaultHttpClient();
			
			CookieStore cookieStore = new BasicCookieStore();
			localContext = new BasicHttpContext();
			localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
			
			HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000);
		}
	}
	
	protected abstract InputStream requestStream() throws IOException;
	
	public void cancel() {
		canceled = true;
	}
	
	protected HttpClient getClient() {
		return client;
	}
	
	protected HttpContext getLocalContext() {
		return localContext;
	}
	
	final protected Object streamToJson(InputStream stream) throws IOException, JSONException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "iso-8859-1"), 8);
		StringBuilder sb = new StringBuilder();
        String line = null;
        
        while ((line = reader.readLine()) != null) {
            sb.append(line + "\n");
        }

        Log.d(Constants.TAG, "HttpCommand.streamToJson() -> " + sb.toString());
        
        if(sb.subSequence(0, 1).equals("[")) {
        	return new JSONArray(sb.toString());
        } else {
        	return new JSONObject(sb.toString());
        }
	}
	
	final protected String streamToString(InputStream stream) throws IOException, JSONException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "iso-8859-1"), 8);
		StringBuilder sb = new StringBuilder();
        String line = null;
        
        while ((line = reader.readLine()) != null) {
            sb.append(line + "\n");
        }

        Log.d(Constants.TAG, "HttpCommand.streamToJson() -> " + sb.toString());
        
        return sb.toString();
	}
	
	public abstract T execute() throws IOException, JSONException;
}
