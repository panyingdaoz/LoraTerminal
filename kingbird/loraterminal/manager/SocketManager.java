package com.kingbird.loraterminal.manager;


import com.kingbird.loraterminal.service.MonitorService;
import com.kingbird.loraterminal.utils.Const;
import com.kingbird.loraterminal.utils.Plog;
import com.kingbird.loraterminal.utils.SpUtil;
import com.socks.library.KLog;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import static com.kingbird.loraterminal.utils.BaseUtil.intentActivity;
import static com.kingbird.loraterminal.utils.Config.CONSTANT_ONE_HUNDRED;

/**
 * Socket类
 *
 * @author panyingdao
 * @date 2018-1-5.
 */
public class SocketManager {
    private static final String TAG = "SocketManager";
    private static SocketManager instance;
    private static Socket socket;
    private static ObjectInputStream din;
    private static ObjectOutputStream dout;
    private String mIp = "47.92.87.218";
    private int mPort = 10011;
    private boolean mConnected;
    private int count;

    private SocketManager() {
    }

    public static SocketManager getInstance() {
        if (instance == null) {
            instance = new SocketManager();
        }
        return instance;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(String ip, int port) {
        this.mIp = ip;
        this.mPort = port;
    }

    public boolean connect() {
        try {
            if (socket == null) {
                socket = new Socket(mIp, mPort);
//                socket = new Socket("47.92.87.218", 10011);
                dout = new ObjectOutputStream(socket.getOutputStream());
                din = new ObjectInputStream(socket.getInputStream());

                Plog.e(TAG, socket.toString());
            }
            mConnected = true;
        } catch (Exception e) {
            e.printStackTrace();
            Plog.e("connect", e.toString());
            count++;
            if (count < CONSTANT_ONE_HUNDRED) {
                ExecutorServiceManager.getInstance().schedule(new Runnable() {
                    @Override
                    public void run() {
                        dealWithHeartBeat();
                    }
                }, 1, TimeUnit.SECONDS);
            }
            mConnected = false;
        }
        return mConnected;
    }

    public synchronized int send(String data) {
        if (!mConnected) {
            return 0;
        }
        try {
            if (dout != null) {
                dout.writeObject(data);
                Plog.e("发送的数据", data);
                SpUtil.writeString(Const.TRIGGER_DATA, data);
                intentActivity("3");
            }
        } catch (Exception e) {
            Plog.e(e.toString());
//            MonitorService monitorService = new MonitorService();
//            monitorService.loraCertification();
            e.printStackTrace();
            return 0;
        }
        return data.length();
    }

    public String receive3() {
        String str = null;
        try {
            str = din.readObject().toString();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return str;
    }

    private void dealWithHeartBeat() {
        close();
        boolean connect = connect();
        if (connect) {
            Plog.e("断开重连");
            MonitorService monitorService = new MonitorService();
            monitorService.loraCertification();
        }
    }

    public void close() {
        try {
            if (din != null) {
                din.close();
                Plog.e(TAG, "din 已关闭");
            }
            if (dout != null) {
                dout.close();
                Plog.e(TAG, "dout 已关闭");
            }
            if (socket != null) {
                socket.close();
                Plog.e(TAG, "socket 已关闭");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Plog.e(TAG, e.toString());
        } finally {
            socket = null;
            dout = null;
            din = null;
        }
        mConnected = false;
    }
}
