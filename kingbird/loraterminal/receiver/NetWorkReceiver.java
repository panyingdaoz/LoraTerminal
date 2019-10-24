//package com.kingbird.loraterminal.receiver;
//
//import android.content.BroadcastReceiver;
//import android.content.ContentValues;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.net.ConnectivityManager;
//
//import com.kingbird.advertisting.base.Base;
//import com.kingbird.advertisting.litepal.Parameter;
//import com.kingbird.advertisting.manager.ExecutorServiceManager;
//import com.kingbird.advertisting.manager.ProtocolManager;
//import com.kingbird.advertisting.manager.SocketManager;
//import com.kingbird.advertisting.manager.ThreadManager;
//import com.kingbird.advertisting.utils.Const;
//import com.kingbird.advertisting.utils.SpUtil;
//import com.socks.library.KLog;
//
//import org.litepal.LitePal;
//
//import java.util.List;
//import java.util.concurrent.TimeUnit;
//
//import static com.kingbird.advertisting.utils.NetUtil.isNetConnected;
//
///**
// * 网络广播
// *
// * @author panyingdao
// * @date 2017/8/24.
// */
//public class NetWorkReceiver extends BroadcastReceiver {
//
//    public static final String REGISTER_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
//    private static NetWorkReceiver receiver;
//    private int netType;
//
//    public static void register(Context context) {
//        Plog.e("注册");
//        IntentFilter filter = new IntentFilter(REGISTER_ACTION);
//        receiver = new NetWorkReceiver();
//        context.registerReceiver(receiver, filter);
//    }
//
//    public static void unRegister(Context context) {
//        Plog.e("注销");
//        context.unregisterReceiver(receiver);
//        receiver = null;
//    }
//
//    @Override
//    public void onReceive(final Context context, Intent intent) {
//        final ConnectivityManager connectManager = (ConnectivityManager) context
//                .getSystemService(Context.CONNECTIVITY_SERVICE);
//        try {
//            if (!isNetConnected(context)) {
//                Plog.e("断网");
//                SocketManager.getInstance().close();
//            } else {
//                Plog.e("有网");
//                ThreadManager.getInstance().doExecute(new Runnable() {
//                    @Override
//                    public void run() {
//                        List<Parameter> parameter = LitePal.findAll(Parameter.class);
//                        for (Parameter parameters : parameter) {
//                            netType = parameters.getProtocolType();
//                            boolean isConntect = SocketManager.getInstance().mConnected;
//                            Plog.e("连接状况", isConntect);
//                            assert connectManager != null;
//                            android.net.NetworkInfo type = connectManager.getActiveNetworkInfo();
//                            ContentValues netWork = new ContentValues();
//                            if (type.getType() == ConnectivityManager.TYPE_MOBILE) {
//                                Plog.e("4G网络");
//                                netWork.put("networkType", 1);
//                                //2018-11-2新增连接状况判断
//                                if (!isConntect) {
//                                    tcpReconnect();
//                                }
//                            } else if (type.getType() == ConnectivityManager.TYPE_WIFI) {
//                                Plog.e("WIFI网络");
//                                netWork.put("networkType", 2);
//                                if (!isConntect) {
//                                    tcpReconnect();
//                                }
//                            } else if (type.getType() == ConnectivityManager.TYPE_ETHERNET) {
//                                Plog.e("以太网网络");
//                                netWork.put("networkType", 3);
//                                if (!isConntect) {
//                                    tcpReconnect();
//                                }
//                            } else {
//                                netWork.put("networkType", 4);
//                                Plog.e("无网络连接");
//                            }
//                            LitePal.updateAll(Parameter.class, netWork);
//                            ExecutorServiceManager.getInstance().schedule(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Base.intentActivity("12");
//                                }
//                            }, 1, TimeUnit.SECONDS);
//                        }
//                    }
//                });
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void tcpReconnect() {
//        Plog.e("网络协议类型", netType);
//        SocketManager.getInstance().close();
//        SocketManager.getInstance().connect();
//        Plog.e("socket已经重连");
//        if (SocketManager.getInstance().getSocket() != null) {
//            SpUtil.writeString(Const.NET_TYPE, "tcp");
//            ProtocolManager.getInstance().sendHeartBeat();
//        }
//    }
//
//    public static boolean hasActiveNetWork(ConnectivityManager connectManager) {
//        return connectManager.getActiveNetworkInfo().isConnected();
//    }
//}
