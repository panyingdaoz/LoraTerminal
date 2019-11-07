package com.kingbird.loraterminal.service;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.kingbird.loraterminal.manager.CustomActivityManager;
import com.kingbird.loraterminal.manager.ExecutorServiceManager;
import com.kingbird.loraterminal.manager.ThreadManager;
import com.kingbird.loraterminal.utils.Plog;
import com.socks.library.KLog;

import java.util.concurrent.TimeUnit;

import static com.kingbird.loraterminal.utils.Config.PACKAGE_NAME2;
import static com.kingbird.loraterminal.utils.Config.YZDJ_PACKAGE_NAME;

/**
 * APP保护服务
 *
 * @author panyingdao
 * @date 2018/2/05.
 */
public class StartAppService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Plog.e("保护服务启动");

//        NetWorkReceiver.register(StartAppService.this);
        selfStartApp();
    }

    private void selfStartApp() {
        ThreadManager.getInstance().doExecute(new Runnable() {
            @Override
            public void run() {
                ExecutorServiceManager.getInstance().schedule(new Runnable() {
                    @Override
                    public void run() {
                        Plog.e("软件自动启动");
                        startAPP();
                    }
                }, 2, TimeUnit.SECONDS);
            }
        });
    }

    /**
     * 启动APP
     */
    private void startAPP() {
        Activity activityState = CustomActivityManager.getInstance().getTopActivity();
        Plog.e("VideoTextureActivity的状态："+ activityState);
        if (activityState != null) {
            String appPackageName = getTopPackageName(activityState.toString());
            Plog.e("最上层应用包名："+ appPackageName);
            if (!PACKAGE_NAME2.equals(appPackageName) && !YZDJ_PACKAGE_NAME.equals(appPackageName)) {
                startApp();
                Plog.e("启动成功");
            } else {
                Plog.e("启动失败");
            }
        } else {
            startApp();
        }
    }

    private void startApp() {
        final Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        startActivity(intent);
    }

    private static String getTopPackageName(String data) {
        return data.substring(0, data.indexOf(".", data.indexOf(".", data.indexOf(".") + 1) + 1));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Plog.e("StartAppService销毁");
        ThreadManager.getInstance().shutdown();
        ExecutorServiceManager.getInstance().shutdown();
        stopSelf();
    }
}
