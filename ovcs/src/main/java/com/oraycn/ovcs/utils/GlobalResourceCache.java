package com.oraycn.ovcs.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.oraycn.esbasic.objectManagement.managers.ObjectManager;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GlobalResourceCache {
    private static GlobalResourceCache instance;
    private Context mContext;
    private List<String> staticFacesList;
    private ObjectManager<String ,Bitmap> headDic=new ObjectManager<>();

    public List<String> getStaticFacesList() {
        return this.staticFacesList;
    }

    private ObjectManager<String, Bitmap> emotionDic = new ObjectManager<>();

    public static GlobalResourceCache getInstance() {
        if (instance == null) {
            instance = new GlobalResourceCache();
        }
        return instance;
    }

    public void initialize(Context context) {
        try {
            this.mContext = context;
            initStaticFaces();
            initStaticHead();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initStaticFaces() {
        try {
            staticFacesList = new ArrayList<String>();
            String[] faces = mContext.getAssets().list("face");
            for (int i = 0; i < faces.length; i++) {
                String index = faces[i].substring(0, 3);
                staticFacesList.add(index);
                emotionDic.add(index, BitmapFactory.decodeStream(mContext.getAssets().open("face/" + faces[i])));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void initStaticHead() {
        try {
            for (int i = 1; i < 4; i++) {
                String iconName = i + ".png";
                Bitmap bitmap = BitmapFactory.decodeStream(mContext.getAssets().open("head/" + iconName));
                headDic.add(String.valueOf(i) , bitmap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Bitmap getEmotion(String emotionName) {
        return this.emotionDic.get(emotionName);
    }

    /**
     * 根据UserID获取对应的头像
     * @param userID
     * @return
     */
    public Bitmap getUserHead4UserID(String userID) {
        if (StringHelper.isNullOrEmpty(userID)) {
            return null;
        }
        String index = "1";
        String str = String.valueOf(userID.charAt(userID.length() - 1));
        Pattern _numericregex = Pattern.compile("^[-]?[0-9]+(\\.[0-9]+)?$");
        Matcher matcher = _numericregex.matcher(str);
        if (matcher.find()) {
            int last = Integer.parseInt(str);
            if (last % 2 == 0) {
                index = "2";
            } else {
                index = "3";
            }
        }
        return headDic.get(index);
    }
}
