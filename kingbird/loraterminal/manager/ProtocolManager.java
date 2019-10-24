package com.kingbird.loraterminal.manager;

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
}
