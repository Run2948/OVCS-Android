package com.oraycn.ovcs.utils;
import com.oraycn.ovcs.OrayApplication;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SDKUtil {
	/*
	 * 获取系统版本号
	 */
	public static int getAndroidSDKVersion() {
		int version = 0;
		try {
			version = Integer.valueOf(android.os.Build.VERSION.SDK);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return version;
	}

	public static String getSystemDataATime() {
		// 24小时制
		SimpleDateFormat dateFormat24 = new SimpleDateFormat("MM-dd HH:mm");
		// 12小时制
		// SimpleDateFormat dateFormat12 = new
		// SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		return dateFormat24.format(Calendar.getInstance().getTime());
	}


	public static boolean checkConnection(){
		boolean connected=  true;
		if(!OrayApplication.getInstance().getEngine().connected()){
			ToastUtils.showShort(OrayApplication.getInstance(), "您已掉线！");
			connected= false;
		}
		return connected;
	}

	/**
	 * 序列化对象
	 *
	 * @param object
	 * @return
	 */
	public static byte[] serialize(Object object) {
		ObjectOutputStream oos = null;
		ByteArrayOutputStream baos = null;
		try {
			// 序列化
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			oos.writeObject(object);
			byte[] bytes = baos.toByteArray();


			return bytes;
		} catch (Exception e) {
			String s="";

		} finally {
			if (oos != null) {
				try {
					oos.flush();
					oos.close();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
		return null;
	}

	/**
	 * 反序列化
	 *
	 * @param bytes
	 * @return
	 */
	public static Object deSerialize(byte[] bytes) {
		ByteArrayInputStream bais = null;
		ObjectInputStream ois = null;
		try {
			// 反序列化
			bais = new ByteArrayInputStream(bytes);
			ois = new ObjectInputStream(bais);
			Object obj = ois.readObject();
			return obj;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (ois != null) {
				try {
					ois.close();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}

		}
		return null;
	}
}
