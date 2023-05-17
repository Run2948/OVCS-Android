package com.oraycn.ovcs.utils.io;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.util.Size;
import android.util.TypedValue;

import java.io.ByteArrayOutputStream;

public class BitmapUtil {

    /**
     * 根据路径获取图片的尺码
     *
     * @param pathName  ：源图片资源
     * @return
     */
    public static Size getImgSize(String pathName)
    {
        BitmapFactory.Options options = new BitmapFactory.Options();
        /**
         * 最关键在此，把options.inJustDecodeBounds = true;
         * 这里再decodeFile()，返回的bitmap为空，但此时调用options.outHeight时，已经包含了图片的高了
         */
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName,options);
        return new Size(options.outWidth,options.outHeight);
    }

    /**
     * 获取图片的指定最大长度的尺寸
     *
     * @param bitmap  ：源图片资源
     * @param maxLength ：图片长、宽最大的长度  单位:px
     * @return
     */
    public static Size getZoomSize (Bitmap bitmap, int maxLength)
    {
        int imgWidth = bitmap.getWidth();
        int imgHeight = bitmap.getHeight();
        return getZoomSize(imgWidth,imgHeight,maxLength);
    }

    /**
     * 获取图片的指定最大长度的尺寸
     *
     * @param imgWidth  ：源宽度
     * @param imgHeight  ：源长度
     * @param maxLength ：图片长、宽最大的长度  单位:px
     * @return
     */
    public static Size getZoomSize(int imgWidth,int imgHeight,int maxLength)
    {
        if (maxLength <= 0) {
            return new Size(0, 0);
        }
        int width = imgWidth, height = imgHeight;
        if (imgWidth > maxLength && imgHeight > maxLength) {
            if (imgWidth > imgHeight) {
                width = maxLength;
                height = (int) (imgHeight * (1.0* width / imgWidth));
            } else {
                height = maxLength;
                width = (int) (imgWidth * (1.0* height / imgHeight));
            }
        } else if (imgWidth > maxLength) {
            width = maxLength;
            height = (int) (imgHeight * (1.0* width / imgWidth));
        } else if (imgHeight > maxLength) {
            height = maxLength;
            width = (int) (imgWidth * (1.0* height / imgHeight));
        }
        return new Size(width, height);
    }


//    public static byte[]  Bytes2yuvBytes(byte[] data) {
//
//        byte[] jdata = null;
//        // 把二进制图片转成位图
//        YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21, 200,
//                200, null); // 20、20分别是图的宽度与高度
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        // 80--JPG图片的质量[0-100],100最高
//        yuvimage.compressToJpeg( new Rect(0, 0, yuvimage.getWidth(), yuvimage.getHeight()), 80, baos);
//        jdata = baos.toByteArray();
//        return jdata;
//    }


    public static Bitmap Bytes2Bimap(byte[] b) {
        Bitmap bitmap = null;
        if (b.length != 0) {
            bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
        }
        return bitmap;
    }

    private static BitmapFactory.Options getBitmapOption(int inSampleSize)

    {
        System.gc();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPurgeable = true;
        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;//inJustDecodeBounds 需要设置为false，如果设置为true，那么将返回null

        return options;
    }
//
//    public static Bitmap Bytes2Bimap(byte[] b) {
//
//        ByteArrayInputStream bais = new ByteArrayInputStream(b);
//        BufferedImage bi1 = ImageIO.read(bais);
//            return BitmapFactory.decodeByteArray(b, 0, b.length, opts);
//        } else {
//            return null;
//        }
//    }

    public static Bitmap
    decodeResource(Resources resources, int id) {
        TypedValue
                value = new TypedValue();
        resources.openRawResource(id,
                value);
        BitmapFactory.Options
                opts = new BitmapFactory.Options();
        opts.inTargetDensity
                = value.density;
        return BitmapFactory.decodeResource(resources,
                id, opts);
    }

    public static byte[] Bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    public static Drawable bitmap2Drawable(Bitmap bitmap) {
        return new BitmapDrawable(bitmap);
    }

    public static Bitmap drawable2Bitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof NinePatchDrawable) {
            Bitmap bitmap = Bitmap
                    .createBitmap(
                            drawable.getIntrinsicWidth(),
                            drawable.getIntrinsicHeight(),
                            drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                                    : Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            return bitmap;
        } else {
            return null;
        }
    }

}
