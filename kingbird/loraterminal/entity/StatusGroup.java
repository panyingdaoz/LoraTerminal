package com.kingbird.loraterminal.entity;

import java.io.Serializable;

/**
 * 说明：状态组类
 *
 * @author Pan Yingdao
 * @time : 2019/7/20/020
 */
public class StatusGroup implements Serializable {

    private BeforceStatus beforceStatus;
    private CurrentStatus currentStatus;
    private String beforce;

    public BeforceStatus getBeforceStatus() {
        return beforceStatus;
    }

    public void setBeforceStatus(BeforceStatus beforceStatus) {
        this.beforceStatus = beforceStatus;
    }

    public CurrentStatus getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(CurrentStatus currentStatus) {
        this.currentStatus = currentStatus;
    }

    public String getBeforce() {
        return beforce;
    }

    public void setBeforce(String beforce) {
        this.beforce = beforce;
    }
}
