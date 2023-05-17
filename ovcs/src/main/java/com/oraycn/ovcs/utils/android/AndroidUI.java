package com.oraycn.ovcs.utils.android;

import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.widget.ImageView;

public class AndroidUI {
    /**
     *
     *  // 将ImageView变成灰色
     * @param imageView
     */
    public static void toGrayImage(ImageView imageView) {

        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);//饱和度 0灰色 100过度彩色，50正常
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        imageView.setColorFilter(filter);
    }

    public static void toNormalImage(ImageView imageView) {
        imageView.setColorFilter(null);
    }

    public static void toHighImage(ImageView imageView) {

        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(100);//饱和度 0灰色 100过度彩色，50正常
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        imageView.setColorFilter(filter);
    }
}
