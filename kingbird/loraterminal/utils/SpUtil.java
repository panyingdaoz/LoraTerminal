package com.kingbird.loraterminal.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.kingbird.loraterminal.activity.LoraApplication;

/**
 * 数据存储类
 * @author panyingdao
 * @date 2018-1-22.
 */
public class SpUtil {

    private static SharedPreferences getSharePreferences() {
        return LoraApplication.getInstance().getSharedPreferences(Const.APP_NAME, Context.MODE_PRIVATE);
    }

    public static boolean readBoolean(String key) {
        return getSharePreferences().getBoolean(key, false);
    }

    public static boolean readBooleanByTrueDefualt(String key) {
        return getSharePreferences().getBoolean(key, true);
    }

    public static void writeBoolean(String key, boolean value) {
        getSharePreferences().edit().putBoolean(key, value).apply();
    }

    public static int readInt(String key) {
        return getSharePreferences().getInt(key, 0);
    }

    public static void writeInt(String key, int value) {
        getSharePreferences().edit().putInt(key, value).apply();
    }

    public static String readString(String key) {
        return getSharePreferences().getString(key, "");
    }

    public static void writeString(String key, String value) {
        getSharePreferences().edit().putString(key, value).apply();
    }
}
