package com.oraycn.ovcs.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Iterator;

public class StringHelper {
    public StringHelper() {
    }

    public static String md5(String string) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException var7) {
            throw new RuntimeException("Huh, MD5 should be supported?", var7);
        } catch (UnsupportedEncodingException var8) {
            throw new RuntimeException("Huh, UTF-8 should be supported?", var8);
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        byte[] var3 = hash;
        int var4 = hash.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            byte b = var3[var5];
            if ((b & 255) < 16) {
                hex.append("0");
            }

            hex.append(Integer.toHexString(b & 255));
        }

        return hex.toString();
    }

    public static boolean isNullOrEmpty(String string) {
        return string == null || string.length() == 0;
    }

    public static String ContactString(Collection<String> objList, String contactor) {
        if (objList == null) {
            return null;
        } else {
            String result = "";

            String str;
            for(Iterator var3 = objList.iterator(); var3.hasNext(); result = result + contactor + str) {
                str = (String)var3.next();
            }

            if (result.length() > 0) {
                result = result.substring(1);
            }

            return result;
        }
    }

    public static String getEncryptPhoneNumber(String phone) {
        return isNullOrEmpty(phone) ? phone : phone.substring(0, 3) + "****" + phone.substring(7, phone.length());
    }
}
