package com.kingbird.loraterminal.utils;

import android.os.Environment;

/**
 * @author Pan yingdao
 */
public class Config {

    /**
     * 根目录
     */
    public static final String ROOT_DIRECTORY_URL = Environment.getExternalStorageDirectory() + "/";
    /**
     * 包名
     */
    public static final String PACKAGE_NAME2 = "com.kingbird.loraterminal";
    public static final String YZDJ_PACKAGE_NAME = "com.yzdj.tt";
    public static final String MY_LOG_URL = ROOT_DIRECTORY_URL + "Mylog/";
    public static final String APP_LOG = "http://log.jtymedia.com/api/log";
    /**
     * 头
     */
    public static final String HEAD = "55";
    /**
     * 尾
     */
    public static final String TAIL = "AA";
    /**
     * 功能码
     */
    public static final String NUMBER_0B = "0B";
    /**
     * 功能码
     */
    public static final String NUMBER_0C = "0C";
    /**
     * 主板型号
     */
    public static final String MODEL = "v40";
    /**
     * 成功字符串
     */
    public static final String SUCCEE = "succee";
    /**
     * 字符串 1
     */
    public static final String STRING_01 = "01";
    /**
     * 字符串2
     */
    public static final String STRING_02 = "02";
    /**
     * A端与服务器连接认证
     */
    public static final String CERTIFICATION = "CERTIFICATION";
    /**
     * C端ID获取
     */
    public static final String DRIVE_NODE = "DRIVE_NODE";
    /**
     * C端状态数据
     */
    public static final String DRIVE_STATUS = "DRIVE_STATUS";
    /**
     * C端本地数据
     */
    public static final String LOCAL_DATA = "LOCAL_DATA";
    /**
     * C端心跳数据
     */
    public static final String HEARTBEAT = "HEARTBEAT";
    /**
     * C端空闲状态
     */
    public static final String FREE = "FREE";
    /**
     * C端忙碌状态
     */
    public static final String BUSY = "BUSY";
    /**
     * C端关机状态
     */
    public static final String OFF = "OFF";

    public static final int CONSTANT_ONE = 1;
    public static final int CONSTANT_TWO = 2;
    public static final int CONSTANT_THREE = 3;
    public static final int CONSTANT_FOUR = 4;
    public static final int CONSTANT_FIVE = 5;
    public static final int CONSTANT_SIX = 6;
    public static final int CONSTANT_EIGHT = 8;
    public static final int CONSTANT_TEN = 10;
    public static final int CONSTANT_ELEVEN = 11;
    public static final int CONSTANT_THIRTEEN = 13;
    public static final int CONSTANT_TWENTY = 20;
    public static final int TWENTY_FIVE = 25;
    public static final int CONSTANT_FORTY = 40;
    public static final int CONSTANT_ONE_HUNDRED = 100;
    public static final int CONSTANT_BUTTON = 122;
    public static final int CONSTANT_FIVE_HUNDRED = 500;
    public static final int VOICE_CONTENT_LENGTH = 99;
}
