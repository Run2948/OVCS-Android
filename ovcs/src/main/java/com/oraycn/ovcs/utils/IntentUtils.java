package com.oraycn.ovcs.utils;

import android.content.Context;
import android.content.Intent;

import java.io.Serializable;

public class IntentUtils {
	public static void startActivity(Context context, Class activityClass,String action,String[] categories,int[] flags) {
		Intent intent = new Intent(context, activityClass);

		if (categories != null) {
			for (String category : categories) {
				intent.addCategory(category);
			}
		}

		if (flags != null) {
			for (int flag : flags) {
				intent.addFlags(flag);
			}
		}

		if (action != null) {
			intent.setAction(action);
		}

		context.startActivity(intent);
	}


	public static void startActivity(Context context, Class activituclass) {

		Intent  intent = new Intent(context, activituclass);
		context.startActivity(intent);
	}

	public static void startActivity(Context context, Class activituclass,String key, Serializable value) {

		Intent intent = new Intent(context, activituclass);
		intent.putExtra(key, value);
		intent.putExtra(key, value);
		context.startActivity(intent);
	}


	public static void startActivityForString(Context context,
			Class activituclass, String key, String value) {

		Intent 	intent = new Intent(context, activituclass);
		intent.putExtra(key, value);
		context.startActivity(intent);
	}

	public static void startActivityForString_Dubble(Context context,
			Class activituclass, String key, String value, String key1,
			String value1) {

		Intent intent = new Intent(context, activituclass);
		intent.putExtra(key, value);
		intent.putExtra(key1, value1);
		context.startActivity(intent);
	}

	public static void startActivityForString_Three(Context context,
													 Class activituclass, String key, String value, String key1,
													 String value1, String key2,
													String value2) {

		Intent intent = new Intent(context, activituclass);
		intent.putExtra(key, value);
		intent.putExtra(key1, value1);
		intent.putExtra(key2, value2);
		context.startActivity(intent);
	}
}
