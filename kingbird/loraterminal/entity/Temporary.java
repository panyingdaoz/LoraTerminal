package com.kingbird.loraterminal.entity;

import com.kingbird.loraterminal.utils.Plog;
import com.socks.library.KLog;

import org.litepal.crud.LitePalSupport;

/**
 * 说明：
 *
 * @author Pan Yingdao
 * @time : 2019/8/12/012
 */
public class Temporary extends LitePalSupport {

    private String requestId;
    private String sendData;

    public String getSendData() {
        return sendData;
    }

    public String getRequestId() {
        return requestId;
    }

    public void temPoraryData(String requestId, String sendData) {
        Plog.e("添加的 sendData", sendData);
        this.requestId = requestId;
        this.sendData = sendData;
    }
}
