package com.kingbird.loraterminal.manager;


import com.kingbird.loraterminal.service.MonitorService;
import com.kingbird.loraterminal.utils.Const;
import com.kingbird.loraterminal.utils.Plog;
import com.kingbird.loraterminal.utils.SpUtil;
import com.socks.library.KLog;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import static com.kingbird.loraterminal.utils.BaseUtil.bytes2HexString;
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
    private static Socket socketJt;
    private static DataInputStream dinJt;
    private static DataOutputStream doutJt;
    private String mJtIp = "log.jtymedia.com";
    private int mTjPort = 8011;
//    private String mJtIp = "220.231.191.19";
//    private int mTjPort = 33254;
    private boolean mConnected;
    private boolean mConnectedJt;
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

    public Socket getSocketJt() {
        return socketJt;
    }

    public void setSocket(String ip, int port) {
        this.mIp = ip;
        this.mPort = port;
    }

    /**
     * socket 连接
     */
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
                ExecutorServiceManager.getInstance().schedule(this::dealWithHeartBeat, 1, TimeUnit.SECONDS);
            }
            mConnected = false;
        }
        return mConnected;
    }

    /**
     * 发送数据
     */
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

    /**
     * 建立即投socket连接
     */
    public boolean connectJt() {
        try {
            if (socketJt == null) {
//                Plog.e("socket连接+ " + "IP= " + mJtIp + "  , port= " + mTjPort);
                socketJt = new Socket(mJtIp, mTjPort);
                doutJt = new DataOutputStream(socketJt.getOutputStream());
                dinJt = new DataInputStream(socketJt.getInputStream());

                Plog.e(TAG, socketJt.toString());
            }
            mConnectedJt = true;
        } catch (Exception e) {
            e.printStackTrace();
            Plog.e("connect", e.toString());
            mConnectedJt = false;
        }
        return mConnectedJt;
    }

    /**
     * 给即投发送数据
     */
    public synchronized int sendJt(byte[] data) {
        if (!mConnectedJt) {
            return 0;
        }
        try {
            if (doutJt != null) {
                doutJt.write(data);
                Plog.e("发送log数据", bytes2HexString(data));
            }
        } catch (Exception e) {
            Plog.e(e.toString());
            e.printStackTrace();
            return 0;
        }
        return data.length;
    }

    /**
     * 接收数据
     */
    public String receive3() {
        String str = null;
        try {
            str = din.readObject().toString();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return str;
    }

    public byte[] receive() {
        if (!mConnectedJt || dinJt == null) {
            return null;
        }
        try {
            if (dinJt.available() > 0) {
                byte[] data = new byte[dinJt.available()];
                Plog.e("接收数据：" + bytes2HexString(data));
                int ret = dinJt.available();
                if (dinJt.read(data) != ret) {
                    return null;
                }
                return data;
            } else {
                return null;
            }
        } catch (Exception e) {
            Plog.e("log接收异常原因：" + e.toString());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * socket重连
     */
    private void dealWithHeartBeat() {
        close();
        boolean connect = connect();
        if (connect) {
            Plog.e("断开重连");
            MonitorService monitorService = new MonitorService();
            monitorService.loraCertification();
        }
    }

    /**
     * 断开socket连接
     */
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

    /**
     * 断开即投socket连接
     */
    public void closeJt() {
        try {
            if (dinJt != null) {
                dinJt.close();
                Plog.e(TAG, "din 已关闭");
            }
            if (doutJt != null) {
                doutJt.close();
                Plog.e(TAG, "dout 已关闭");
            }
            if (socketJt != null) {
                socketJt.close();
                Plog.e(TAG, "socket 已关闭");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Plog.e(TAG, e.toString());
        } finally {
            socketJt = null;
            doutJt = null;
            dinJt = null;
        }
        mConnectedJt = false;
    }
}
