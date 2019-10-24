package com.kingbird.loraterminal.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;

import com.socks.library.KLog;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

/**
 * 网络工具类
 *
 * @author panyingdao
 * @date 2018-1-22.
 */
public class NetUtil {

    private NetUtil() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    /**
     * 判断网络是否连接
     *
     * @param context 内容
     * @return 返回
     */
    public static boolean isConnected(Context context) {

        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (null != connectivity) {

            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (null != info && info.isConnected()) {
                if (info.getState() == NetworkInfo.State.CONNECTED) {

                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 检测网络是否连接
     * Context context 对象
     */
    public static boolean isNetConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            assert cm != null;
            Network[] networks = cm.getAllNetworks();
            NetworkInfo networkInfo;
            for (Network mNetwork : networks) {
                networkInfo = cm.getNetworkInfo(mNetwork);
                if (networkInfo.getState().equals(NetworkInfo.State.CONNECTED)) {
                    return true;
                }
            }
        } else {
            if (cm != null) {
                NetworkInfo[] infos = cm.getAllNetworkInfo();
                if (infos != null) {
                    for (NetworkInfo ni : infos) {
                        if (ni.isConnected()) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * 判断是否是wifi连接
     */
    public static boolean isWifi(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm != null && cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI;

    }

    /**
     * 打开网络设置界面
     */
    public static void openSetting(Activity activity) {
        Intent intent = new Intent("/");
        ComponentName cm = new ComponentName("com.android.settings",
                "com.android.settings.WirelessSettings");
        intent.setComponent(cm);
        intent.setAction("android.intent.action.VIEW");
        activity.startActivityForResult(intent, 0);
    }

    /**
     * 获取应用程序名称
     */
    public static void getAppName(Context context, String apkPath) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
            ApplicationInfo appInfo = info.applicationInfo;
            String appName = pm.getApplicationLabel(appInfo).toString();
            Plog.e("app名", appName);
            //得到安装包名称
            String packageName = appInfo.packageName;
            Plog.e("包名", packageName);
            //得到版本信息
            String version = info.versionName;
            Plog.e("版本号", version);
            //得到图标信息
            Drawable icon = pm.getApplicationIcon(appInfo);
            Plog.e("图标", icon);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新apk信息获取
     */
    public static String getApkInfo(Context context, String apkPath) {
        String packageName = null;
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
        if (info != null) {
            ApplicationInfo appInfo = info.applicationInfo;
            //得到安装包名称
            packageName = appInfo.packageName;
            //获取安装包的版本号
//            String version = info.versionName;
            Plog.e("getApkPackageName: " + packageName);
        }
        return packageName;
    }

    /**
     * 更新apk版本
     */
    public static String getApkVersion(Context context, String apkPath) {
        String version = null;
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
        if (info != null) {
            //获取安装包的版本号
            String version2 = info.versionName;
            version = version2.substring(version2.lastIndexOf(".") + 1);
            Plog.e("getApkVersion: " + "-------" + version2);
        }
        return version;
    }

    /**
     * 获取MAC地址
     *
     * @return 返回值
     */
    public static String getMac() {
        StringBuilder macSerial = new StringBuilder();
        try {
            Process pp = Runtime.getRuntime().exec(
                    "cat /sys/class/net/wlan0/address");
            //读取MAC地址
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            //读取MAC文件（按行读取）
            LineNumberReader input = new LineNumberReader(ir);

            String line;
            while ((line = input.readLine()) != null) {
                macSerial.append(line.trim());
            }
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return macSerial.toString().replace(":", "");
    }

}
