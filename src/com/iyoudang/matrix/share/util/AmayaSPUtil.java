package com.iyoudang.matrix.share.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

public class AmayaSPUtil {

    public static SharedPreferences sp;

    public static void initSP(Context context) {
        if (sp == null) {
            sp = PreferenceManager.getDefaultSharedPreferences(context);
        }
    }

    public static void checkSP() {
        if (sp == null) {
//            initSP(MatrixApplication.mContext);
        }
    }

    public static boolean getBoolean(String key, boolean value) {
        checkSP();
        return sp.getBoolean(key, value);
    }

    public static int getInt(String key, int defValue) {
        checkSP();
        return sp.getInt(key, defValue);
    }

    public static String getString(String key, String defValue) {
        checkSP();
        return sp.getString(key, defValue);
    }

    public static long getLong(String key, long defValue) {
        checkSP();
        return sp.getLong(key, defValue);
    }

    public static boolean save(String key, boolean value) {
        checkSP();
        return sp.edit().putBoolean(key, value).commit();
    }

    public static boolean save(String key, int value) {
        checkSP();
        return sp.edit().putInt(key, value).commit();
    }

    public static boolean save(String key, long value) {
        checkSP();
        return sp.edit().putLong(key, value).commit();
    }

    public static boolean save(String key, float value) {
        checkSP();
        return sp.edit().putFloat(key, value).commit();
    }

    public static boolean save(String key, String value) {
        checkSP();
        return sp.edit().putString(key, value).commit();
    }

    public static boolean remove(String key) {
        checkSP();
        return sp.edit().remove(key).commit();
    }

    public static boolean checkUserId() {
        checkSP();
        String uid = sp.getString("matrix_user_id", "");
        if (TextUtils.isEmpty(uid)) {
            return false;
        } else {
            return checkUserToken();
        }

    }

    public static boolean checkUserToken() {
        checkSP();
        String token = sp.getString("matrix_user_token", "");
        if (TextUtils.isEmpty(token)) {
            return false;
        } else {
            return true;
        }
    }

    public static void saveLoginType(Integer loginType) {
        checkSP();
        sp.edit().putInt("matrix_user_logintype", loginType).commit();
    }

    public static boolean saveTheme(int theme) {
        return save("amaya_theme", theme);
    }

    public static int getTheme() {
        return getInt("amaya_theme", 0);
    }

    public static void saveUserToken(String userToken) {
        checkSP();
        sp.edit().putString("matrix_user_token", userToken).commit();
    }

    public static String getUserToken() {
        checkSP();
        return sp.getString("matrix_user_token", null);
    }

    public static float getFloat(String key) {
        checkSP();
        return sp.getFloat(key, 0f);
    }
}
