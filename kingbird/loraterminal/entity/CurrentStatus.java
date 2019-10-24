package com.kingbird.loraterminal.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 说明：当前状态，最新状态
 *
 * @author Pan Yingdao
 * @time : 2019/7/20/020
 */
public class CurrentStatus implements Serializable {

    private String nodeId;
    private String status;
    private Date actionTime;
    private long durationTime;
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

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
