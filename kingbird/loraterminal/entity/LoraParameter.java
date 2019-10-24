package com.kingbird.loraterminal.entity;


import java.io.Serializable;
import java.util.List;

/**
 * 说明： lora上传数据
 *
 * @author :Pan Yingdao
 * @date : 2019/7/23/023
 */
public class LoraParameter implements Serializable {

    private String connTypeEnum;
    private Certification certification;
    private StatusGroup statusGroup;
    private List<String> collectNodesList;
    private List<CurrentStatus> localSaveStatusList;
    private boolean result;
    private boolean succee;
    private String requestId;

    public String getConnTypeEnum() {
        return connTypeEnum;
    }

    public void setConnTypeEnum(String connTypeEnum) {
        this.connTypeEnum = connTypeEnum;
    }

    public Certification getCertification() {
        return certification;
    }

    public void setCertification(Certification certification) {
        this.certification = certification;
    }

    public StatusGroup getStatusGroup() {
        return statusGroup;
    }

    public void setStatusGroup(StatusGroup statusGroup) {
        this.statusGroup = statusGroup;
    }

    public List<String> getCollectNodesList() {
        return collectNodesList;
    }

    public void setCollectNodesList(List<String> collectNodesList) {
        this.collectNodesList = collectNodesList;
    }

    public List<CurrentStatus> getLocalSaveStatusList() {
        return localSaveStatusList;
    }

    public void setLocalSaveStatusList(List<CurrentStatus> localSaveStatusList) {
        this.localSaveStatusList = localSaveStatusList;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public boolean isSuccee() {
        return succee;
    }

    public void setSuccee(boolean succee) {
        this.succee = succee;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
