package com.kingbird.loraterminal.entity;

import java.io.Serializable;

/**
 * 说明：验证器
 *
 * @author Pan Yingdao
 * @time : 2019/7/20/020
 */
public class Certification implements Serializable {

    private String companyId;
    private String relayId;

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getRelayId() {
        return relayId;
    }

    public void setRelayId(String relayId) {
        this.relayId = relayId;
    }

    @Override
    public String toString() {
        return "Certification{" +
                "companyId='" + companyId + '\'' +
                ", relayId='" + relayId + '\'' +
                '}';
    }
}
