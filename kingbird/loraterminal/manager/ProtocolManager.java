package com.kingbird.loraterminal.manager;

import com.kingbird.loraterminal.utils.Plog;

/**
 * 协议管理类
 *
 * @author panyingdao
 * @date 2017-8-16.
 */
public class ProtocolManager {

    private ProtocolManager() {
    }

    private static class HolderClass {
        private final static ProtocolManager INSTANCE = new ProtocolManager();
    }

    public static ProtocolManager getInstance() {
        return HolderClass.INSTANCE;
    }

    public byte[] parseParameter(byte[] receive, int index, int length) {
        byte[] data = new byte[length];
        try {
            System.arraycopy(receive, index, data, 0, length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    /**
     * 通讯类型应答
     */
    public void netDataAnser(byte[] data) {
        int sendLength = SocketManager.getInstance().sendJt(data);
        Plog.e("发送回复结果：" + sendLength);
        if (sendLength <= 0) {
            SocketManager.getInstance().closeJt();
            SocketManager.getInstance().connectJt();
            SocketManager.getInstance().sendJt(data);
        }
    }
}
