package org.redpin.android.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.redpin.android.Constants;

import android.net.Uri;
import android.util.Log;

public abstract class HttpGetCommand<T> extends HttpCommand<T> {
	
	private HttpGet httpGet;
	
	public HttpGetCommand(String fullUrl, String... params) throws UnsupportedEncodingException {
		super();
		
		Uri.Builder builder = Uri.parse(fullUrl).buildUpon();
		if(params != null && params.length > 0 && params.length % 2 == 0) {
			for(int i=0; i<params.length; i+=2) {
				builder.appendQueryParameter(params[i], params[i+1]);
			}
		}
		
		fullUrl = builder.toString();
		httpGet = new HttpGet(fullUrl);
	}

	@Override
	protected InputStream requestStream() throws IOException {
		HttpResponse response;
		try {
			response = getClient().execute(httpGet, getLocalContext());
			return response.getEntity().getContent();
		} catch (IOException e) {
			if (!canceled) {
				Log.e(Constants.TAG, "HttpGetCommand.requestStream() -> IOException", e);
			}
			throw e;
		}
	}
	
	@Override
	public void cancel() {
		super.cancel();
		
		if(httpGet != null)
			httpGet.abort();
	}	
}