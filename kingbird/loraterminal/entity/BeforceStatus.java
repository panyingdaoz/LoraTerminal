package com.kingbird.loraterminal.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 说明：之前状态，用于更新时长
 *
 * @author Pan Yingdao
 * @time : 2019/7/20/020
 */
public class BeforceStatus implements Serializable {

    private String nodeId;
    private String status;
    private Date actionTime;
    private String stringTime;
    private long durationTime;
    private String beforceData;
    private String clientId;

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

    public String getBeforceData() {
        return beforceData;
    }

    public void setBeforceData(String beforceData) {
        this.beforceData = beforceData;
    }

    public String getStringTime() {
        return stringTime;
    }

    public void setStringTime(String stringTime) {
        this.stringTime = stringTime;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
