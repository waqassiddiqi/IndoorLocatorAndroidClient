package org.redpin.android;

public class Constants {
	public final static String TAG = "redpin-android-client";
	
	public final static String ADD_HISTORY_URL = "http://" + ApplicationContext.serverIP + ":" 
									+ ApplicationContext.serverPort + ApplicationContext.applicationName + ApplicationContext.apiPath + "/history";
	
	public final static String ADD_LOCATION_WITH_FINGERPRINT_URL = "http://" + ApplicationContext.serverIP + ":" 
			+ ApplicationContext.serverPort + ApplicationContext.applicationName + ApplicationContext.apiPath + "/fingerprint";
	
	public final static String FETCH_MAP_URL = "http://" + ApplicationContext.serverIP + ":" 
			+ ApplicationContext.serverPort + ApplicationContext.applicationName + ApplicationContext.apiPath + "/map";
	
	public final static String FETCH_USER_URL = "http://" + ApplicationContext.serverIP + ":" 
			+ ApplicationContext.serverPort + ApplicationContext.applicationName + ApplicationContext.apiPath + "/user";
	
	public final static String FIND_LOCATION_URL = "http://" + ApplicationContext.serverIP + ":" 
			+ ApplicationContext.serverPort + ApplicationContext.applicationName + ApplicationContext.apiPath + "/location/find";
}