package com.oraycn.ovcs.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class NetUtil {
	public static boolean isNetConnected(Context context) {
		boolean isNetConnected;
		// 获得网络连接服务
		ConnectivityManager connManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		@SuppressLint("MissingPermission") NetworkInfo info = connManager.getActiveNetworkInfo();
		if (info != null && info.isAvailable()) {
			// String name = info.getTypeName();
			// L.i("当前网络名称：" + name);
			isNetConnected = true;
		} else {
			Toast.makeText(context, "没有可用网络,请先连接网络！", 10).show();
			isNetConnected = false;
		}
		return isNetConnected;
	}


}
