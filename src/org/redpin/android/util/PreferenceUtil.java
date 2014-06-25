package org.redpin.android.util;

import org.redpin.android.ApplicationContext;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferenceUtil {
	public static void setUsername(String username) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ApplicationContext.get());
		SharedPreferences.Editor editor = prefs.edit();

		editor.putString("username", username);

		editor.commit();
	}

	public static String getUsername() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ApplicationContext.get());

		return prefs.getString("username", "");
	}
	
	public static void setName(String name) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ApplicationContext.get());
		SharedPreferences.Editor editor = prefs.edit();

		editor.putString("name", name);

		editor.commit();
	}

	public static String getName() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ApplicationContext.get());

		return prefs.getString("name", "");
	}
	
	public static void setDefaultMap(long mapId) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ApplicationContext.get());
		SharedPreferences.Editor editor = prefs.edit();

		editor.putLong("default_map", mapId);

		editor.commit();
	}

	public static long getDefaultMap() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ApplicationContext.get());

		return prefs.getLong("default_map", 0);
	}
}
