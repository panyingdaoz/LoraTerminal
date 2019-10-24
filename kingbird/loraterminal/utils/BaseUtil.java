package com.kingbird.loraterminal.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.ListView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.alibaba.fastjson.JSON;
import com.kingbird.loraterminal.R;
import com.kingbird.loraterminal.activity.MainActivity;
import com.kingbird.loraterminal.adapter.CboxAdapter;
import com.kingbird.loraterminal.entity.BeforceStatusLitePal;
import com.kingbird.loraterminal.entity.CboxId;
import com.kingbird.loraterminal.entity.CboxStatuEntity;
import com.kingbird.loraterminal.entity.Certification;
import com.kingbird.loraterminal.entity.LocalData;
import com.kingbird.loraterminal.entity.LoraParameter;
import com.kingbird.loraterminal.entity.Temporary;
import com.kingbird.loraterminal.manager.ProtocolManager;
import com.socks.library.KLog;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.kingbird.loraterminal.activity.MainActivity.context;

/**
 * 说明：公共类
 *
 * @author :Pan Yingdao
 * @date : 2019/7/25/025
 */
public class BaseUtil {

    /**
     * 2字节byte[]转16进制字符串
     *
     * @param data 数据源
     * @return 返回
     */
    public static String bytes2HexString(byte[] data) {
        StringBuilder sb = new StringBuilder();
        String hex;
        for (byte aData : data) {
            hex = Integer.toHexString(aData & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

    /**
     * 解析整型数据
     */
    public static int getAnInt(byte[] buff, int index, int length) {
        return Integer.parseInt(bytes2HexString(ProtocolManager.getInstance().parseParameter(buff, index, length)));
    }

    /**
     * 解析字符串数据
     */
    public static String getAnString(byte[] buff, int index, int length) {
        return bytes2HexString(ProtocolManager.getInstance().parseParameter(buff, index, length));
    }

    /**
     * 解析16Hex整型数据
     */
    public static int getAnIntHex(byte[] buff, int index, int length, int radix) {
        return Integer.parseInt(bytes2HexString(ProtocolManager.getInstance().parseParameter(buff, index, length)), radix);
    }

    /**
     * 设置lora配置信息
     */
    public static LoraParameter setLoraSendParameter(String connTypeEnum) {
        Certification certification = getCertification();
        LoraParameter lora = new LoraParameter();
        lora.setConnTypeEnum(connTypeEnum);
        lora.setCertification(certification);
        return lora;
    }

    /**
     * 获取lora配置信息
     */
    public static Certification getCertification() {
        Certification certification = new Certification();
        String companyId = SpUtil.readString(Const.COMPANY_ID);
        certification.setCompanyId(companyId);
        certification.setRelayId(SpUtil.readString(Const.RELAY_ID));
        return certification;
    }

    /**
     * 读取Temporary表数据大小
     */
    public static int readTemporarySize() {
        List<Temporary> temporaryList = LitePal.findAll(Temporary.class);
        return temporaryList.size();
    }

    /**
     * 通过Cboxid 查询CboxId数据
     */
    public static List<CboxId> cboxIdQuery(String str1, String cboxId) {
        return LitePal.select(str1).where("cboxId = ?", cboxId)
                .find(CboxId.class);
    }

    /**
     * 通过LoraId 查询CboxId数据
     */
    public static List<CboxId> loraIdQuery(String str1, String nodeId) {
        return LitePal.select(str1).where("nodeId = ?", nodeId)
                .find(CboxId.class);
    }

    /**
     * 通过RequestId 查询LocalData数据
     */
    public static List<LocalData> localQueryStatu(String str1, String requestId) {
        return LitePal.select(str1).where("requestId = ?", requestId)
                .find(LocalData.class);
    }

    /**
     * 通过Cboxid 查询LocalData数据
     */
    public static List<LocalData> localQueryRequestId(String str1, String cboxId) {
        return LitePal.select(str1).where("cboxId = ?", cboxId)
                .find(LocalData.class);
    }

    /**
     * 通过cboxId 查询BeforceStatusLitePal数据
     */
    public static List<BeforceStatusLitePal> cboxdQuery(String str1, String cboxId) {
        return LitePal.select(str1).where("cboxId = ?", cboxId)
                .find(BeforceStatusLitePal.class);
    }

    /**
     * nodeId 查询BeforceStatusLitePal数据
     */
    public static List<BeforceStatusLitePal> nodeIdQuery(String str1, String nodeId) {
        return LitePal.select(str1).where("nodeId = ?", nodeId)
                .find(BeforceStatusLitePal.class);
    }

    /**
     * 更新每个Cbox 的操作时间 action
     */
    public static void updateCboxaAtionTime(long durationTime, String cboxId) {
        Plog.e("存储时间", durationTime);
        ContentValues values = new ContentValues();
        values.put("durationTime", durationTime);
        LitePal.updateAll(CboxId.class, values, "cboxId = ?", cboxId);
    }

    /**
     * 更新每个Cbox 的状态
     */
    public static void updateCboxaState(int state, String cboxId) {
        Plog.e("Cbox的状态", state);
        ContentValues values = new ContentValues();
        values.put("state", state);
        LitePal.updateAll(CboxId.class, values, "cboxId = ?", cboxId);
    }

    /**
     * 更新每个Cbox 的通讯状态
     */
    public static void updateCboxaSatus(int state, String cboxId) {
        ContentValues values = new ContentValues();
        values.put("onLineStatus", state);
        LitePal.updateAll(CboxId.class, values, "cboxId = ?", cboxId);
    }
    /**
     * 更新每个Cbox 的通讯状态
     */
    public static void updateCboxaSatus2(int state, String nodeId) {
        ContentValues values = new ContentValues();
        values.put("onLineStatus", state);
        LitePal.updateAll(CboxId.class, values, "nodeId = ?", nodeId);
    }

    /**
     * 更新每个LocalData 的状态 更新状态
     */
    public static void updateStatus(int uploadStatu, String requestId) {
        ContentValues values = new ContentValues();
        values.put("uploadStatu", uploadStatu);
        LitePal.updateAll(LocalData.class, values, "requestId = ?", requestId);
    }

    /**
     * 读取CboxID
     */
    public static ArrayList<Integer> readCboxId(int cboxId) {
        List<LocalData> playLists = LitePal.findAll(LocalData.class);
        ArrayList<Integer> arrList = new ArrayList<>();
        for (LocalData playList : playLists) {
            int playId = playList.getCboxId();
            if (playId == cboxId) {
                arrList.add(cboxId);
            }
            Collections.sort(arrList);
        }
        return arrList;
    }

    /**
     * 本地本地数据
     */
    public static void localDataSave(int cboxId, String loraId, String currentStatu, Date date, long statuTime, String requestId, String clientId) {
        int lastPlayId;
        List<LocalData> mpList = LitePal.findAll(LocalData.class);
        if (mpList.size() > 0) {
            LocalData lastNews = LitePal.findLast(LocalData.class);
            lastPlayId = lastNews.getDataId();
        } else {
            lastPlayId = 0;
        }
        LocalData localData = new LocalData();
        localData.setDataId(lastPlayId + 1);
        localData.setCboxId(cboxId);
        localData.setNodeId(loraId);
        localData.setStatus(currentStatu);
        localData.setActionTime(date);
        localData.setDurationTime(statuTime);
        localData.setRequestId(requestId);
        localData.setClientId(clientId);
        localData.setUploadStatu(1);
        localData.save();
        Plog.e("新增本地数据");
    }

    /**
     * 更新上传记录表
     */
    public static void saveBeforceStartus(int cboxId, String nodeId, String status, String actionTime, long durationTime, String clientId) {
        Plog.e("修改", nodeId, cboxId);
        ContentValues values = new ContentValues();
        values.put("nodeId", nodeId);
        values.put("status", status);
        values.put("actionTime", actionTime);
        values.put("durationTime", durationTime);
        values.put("clientId", clientId);
        LitePal.updateAll(BeforceStatusLitePal.class, values, "cboxId = ?", Integer.toString(cboxId));
    }

    /**
     * 创建上次上传记录表
     */
    public static void newBeforceStartus(int cboxId, String nodeId, String status, String actionTime, long durationTime, String clientId) {
        Plog.e("新建", nodeId);
        BeforceStatusLitePal beforce = new BeforceStatusLitePal();
        beforce.setCboxId(cboxId);
        beforce.setNodeId(nodeId);
        beforce.setStatus(status);
        beforce.setActionTime(actionTime);
        beforce.setDurationTime(durationTime);
        beforce.setClientId(clientId);
        beforce.save();
    }

    /**
     *  通知MainActivity
     */
    public static void intentActivity(String value) {
        Intent intent = new Intent();
        intent.setAction("tcpServerReceiver");
        intent.putExtra("tcpServerReceiver", value);
        //将消息发送给主界面
        //安全性更好，同时拥有更高的运行效率
        Plog.e("context对象", context);
        if (context != null) {
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            Plog.e("将消息发送给主界面", value);
        }
    }

    /**
     * 判断是否第一次安装
     */
    public static boolean isFirstStart(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(
                "SHARE_APP_TAG", 0);
        boolean isFirst = preferences.getBoolean("FIRSTStart", true);
        if (isFirst) {
            // 第一次
            preferences.edit().putBoolean("FIRSTStart", false).apply();
            return true;
        } else {
            return false;
        }
    }

    /**
     * 两个字节数组拼接
     *
     * @param paramArrayOfByte1 字节数组1
     * @param paramArrayOfByte2 字节数组2
     * @return 拼接后的数组
     */
    public static byte[] mergerArray(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2) {
        byte[] arrayOfByte = new byte[paramArrayOfByte1.length + paramArrayOfByte2.length];
        if (paramArrayOfByte1.length == 0) {
            arrayOfByte = paramArrayOfByte2;
        } else {
            System.arraycopy(paramArrayOfByte1, 0, arrayOfByte, 0, paramArrayOfByte1.length);
            System.arraycopy(paramArrayOfByte2, 0, arrayOfByte, paramArrayOfByte1.length, paramArrayOfByte2.length);
        }
        Plog.e("数组", bytes2HexString(arrayOfByte));
        return arrayOfByte;
    }
}
