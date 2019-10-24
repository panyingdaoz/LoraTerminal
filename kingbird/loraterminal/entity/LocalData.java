package com.kingbird.loraterminal.entity;

import org.litepal.crud.LitePalSupport;

import java.util.Date;

/**
 * 说明：本地数据
 *
 * @author Pan Yingdao
 * @time : 2019/7/22/022
 */
public class LocalData extends LitePalSupport {

    private int dataId;
    private int cboxId;
    private String nodeId;
    private String status;
    private Date actionTime;
    private long durationTime;
    private String localData;
    private String requestId;
    private String clientId;
    private int uploadStatu;

    public int getDataId() {
        return dataId;
    }

    public void setDataId(int dataId) {
        this.dataId = dataId;
    }

    public int getCboxId() {
        return cboxId;
    }

    public void setCboxId(int cboxId) {
        this.cboxId = cboxId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getActionTime() {
        return actionTime;
    }

    public void setActionTime(Date actionTime) {
        this.actionTime = actionTime;
    }

    public long getDurationTime() {
        return durationTime;
    }

    public void setDurationTime(long durationTime) {
        this.durationTime = durationTime;
    }

    public String getLocalData() {
        return localData;
    }

    public void setLocalData(String localData) {
        this.localData = localData;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public int getUploadStatu() {
        return uploadStatu;
    }

    public void setUploadStatu(int uploadStatu) {
        this.uploadStatu = uploadStatu;
    }
}
