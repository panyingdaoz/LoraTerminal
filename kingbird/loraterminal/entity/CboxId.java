package com.kingbird.loraterminal.entity;

import org.litepal.crud.LitePalSupport;

/**
 * 说明： CboxIdz类
 *
 * @author Pan Yingdao
 * @time : 2019/7/22/022
 */
public class CboxId extends LitePalSupport {

    private int cboxId;
    private String nodeId;
    private long durationTime;
    private int state;
    private int onLineStatus;

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

    public long getDurationTime() {
        return durationTime;
    }

    public void setDurationTime(long durationTime) {
        this.durationTime = durationTime;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getOnLineStatus() {
        return onLineStatus;
    }

    public void setOnLineStatus(int onLineStatus) {
        this.onLineStatus = onLineStatus;
    }

}
