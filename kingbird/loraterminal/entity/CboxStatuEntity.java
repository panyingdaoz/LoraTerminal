package com.kingbird.loraterminal.entity;

import java.io.Serializable;

/**
 * 说明：
 *
 * @author Pan Yingdao
 * @time : 2019/8/14/014
 */
public class CboxStatuEntity implements Serializable {

    private String cboxId;
    private String state;

    public String getCboxId() {
        return cboxId;
    }

    public String getState() {
        return state;
    }

    public CboxStatuEntity(String cboxId, String state) {
        this.cboxId = cboxId;
        this.state = state;
    }
}
