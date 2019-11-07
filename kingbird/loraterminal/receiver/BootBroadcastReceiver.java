package com.kingbird.loraterminal.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.kingbird.loraterminal.activity.MainActivity;
import com.kingbird.loraterminal.manager.CustomActivityManager;
import com.kingbird.loraterminal.manager.ExecutorServiceManager;
import com.kingbird.loraterminal.utils.Plog;
import com.socks.library.KLog;

import java.util.concurrent.TimeUnit;

/**
 * 开机广播接收器
 *
 * @author panyingdao
 * @date 2017/8/30.
 */

public class BootBroadcastReceiver extends BroadcastReceiver {

    private static final String ACTION = "android.intent.action.BOOT_COMPLETED";
    private static final String OPEN_ACTION = "android.kingbird.action.OPEN_ADVERTISTING";

    @Override
    public void onReceive(final Context context, @NonNull Intent intent) {
        Plog.e("接收到的广播："+ intent.getAction());
        if (intent.getAction() != null) {
            switch (intent.getAction()) {
                case ACTION:
                    Plog.e("VideoTextureActivity的状态："+ CustomActivityManager.getInstance().getTopActivity());
                    if (CustomActivityManager.getInstance().getTopActivity() == null) {
                        //后边的XXX.class就是要启动的服务
                        ExecutorServiceManager.getInstance().schedule(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent1 = new Intent(context, MainActivity.class);
                                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intent1);
                                Plog.e("延迟10秒启动");
                            }
                        },10, TimeUnit.SECONDS);
                    }
                    break;
                case OPEN_ACTION:
                    Plog.e("收到其他APP发来启动即投视播广播："+ intent.getAction());
                    Intent noteList = new Intent(context, MainActivity.class);
                    noteList.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(noteList);
                    break;
                default:
            }
        }
    }
}
