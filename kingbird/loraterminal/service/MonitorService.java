package com.kingbird.loraterminal.service;


import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.kingbird.loraterminal.entity.BeforceStatus;
import com.kingbird.loraterminal.entity.BeforceStatusLitePal;
import com.kingbird.loraterminal.entity.CboxId;
import com.kingbird.loraterminal.entity.CurrentStatus;
import com.kingbird.loraterminal.entity.LocalData;
import com.kingbird.loraterminal.entity.LoraParameter;
import com.kingbird.loraterminal.entity.StatusGroup;
import com.kingbird.loraterminal.entity.Temporary;
import com.kingbird.loraterminal.manager.ExecutorServiceManager;
import com.kingbird.loraterminal.manager.SocketManager;
import com.kingbird.loraterminal.manager.ThreadManager;
import com.kingbird.loraterminal.utils.BaseUtil;
import com.kingbird.loraterminal.utils.Const;
import com.kingbird.loraterminal.utils.Plog;
import com.kingbird.loraterminal.utils.SerialPortUtil;
import com.kingbird.loraterminal.utils.SpUtil;
import com.socks.library.KLog;

import org.litepal.LitePal;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.kingbird.loraterminal.utils.BaseUtil.cboxIdQuery;
import static com.kingbird.loraterminal.utils.BaseUtil.getCertification;
import static com.kingbird.loraterminal.utils.BaseUtil.intentActivity;
import static com.kingbird.loraterminal.utils.BaseUtil.localQueryRequestId;
import static com.kingbird.loraterminal.utils.BaseUtil.localQueryStatu;
import static com.kingbird.loraterminal.utils.BaseUtil.loraIdQuery;
import static com.kingbird.loraterminal.utils.BaseUtil.nodeIdQuery;
import static com.kingbird.loraterminal.utils.BaseUtil.saveBeforceStartus;
import static com.kingbird.loraterminal.utils.BaseUtil.setLoraSendParameter;
import static com.kingbird.loraterminal.utils.BaseUtil.updateCboxaSatus2;
import static com.kingbird.loraterminal.utils.BaseUtil.updateStatus;
import static com.kingbird.loraterminal.utils.Config.CERTIFICATION;
import static com.kingbird.loraterminal.utils.Config.DRIVE_NODE;
import static com.kingbird.loraterminal.utils.Config.DRIVE_STATUS;
import static com.kingbird.loraterminal.utils.Config.HEARTBEAT;
import static com.kingbird.loraterminal.utils.Config.LOCAL_DATA;
import static com.kingbird.loraterminal.utils.Config.MODEL;
import static com.kingbird.loraterminal.utils.Config.OFF;
import static com.kingbird.loraterminal.utils.Config.SUCCEE;

/**
 * 通讯服务 class
 *
 * @author panyingdao
 * @date 2017/12/15.
 */
public class MonitorService extends Service {

    private int period = 30;
    private int offTime = 90;
    private int heartbeat = 60;
    private long heartbeatTime;

    @Override
    public IBinder onBind(Intent intent) {
        IBinder result = new ServiceBinder();
        Plog.e("onBind");
        return result;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Plog.e("MonitorService 创建");

        if (Build.MODEL.equals(MODEL)) {
            SerialPortUtil.close();
            if (!SerialPortUtil.open()) {
                SerialPortUtil.open();
                Plog.e("串口初始化");
            }
        }
        heartbeatTime = System.currentTimeMillis() / 1000;
        loraHeartbeat();
        loraCertification();

    }

    /**
     * lora服务器回复监听
     */
    public void loraCertification() {
        ThreadManager.getInstance().doExecute(() -> {
            Plog.e("socket连接");
            SocketManager.getInstance().close();
            if (SocketManager.getInstance().connect()) {
                LoraParameter lora = setLoraSendParameter(CERTIFICATION);
                Plog.e("联网认证发送结果", SocketManager.getInstance().send(JSON.toJSONString(lora)));
                ExecutorServiceManager.getInstance().scheduleAtFixedRate(() -> {
                    String data = SocketManager.getInstance().receive3();
                    if (data.length() > 0) {
                        JSONObject json = JSONObject.parseObject(data);
                        String connTypeEnum = json.getString("connTypeEnum");
                        Plog.e("connTypeEnum", connTypeEnum);
                        loraReply(data, json, connTypeEnum);
                    }
                }, 10, 1000, TimeUnit.MILLISECONDS);
            }
        });
    }

    /**
     * 服务器回复类型 处理
     */
    private void loraReply(final String data, final JSONObject json, final String connTypeEnum) {
        switch (connTypeEnum) {
            case CERTIFICATION:
                if (json.getBoolean(SUCCEE)) {
                    Plog.e("联网认证 成功！");
                    sendHeartbeat();
                    SpUtil.writeBoolean(Const.CERTIFICATION, true);
                    LoraParameter lora = setLoraSendParameter(DRIVE_NODE);
                    SocketManager.getInstance().send(JSON.toJSONString(lora));
                    uploadLocalData();
                    inspectionTiming();
                } else {
                    SpUtil.writeBoolean(Const.CERTIFICATION, false);
                    Plog.e("联网认证 失败！");
                    LoraParameter lora = setLoraSendParameter(CERTIFICATION);
                    Plog.e("联网认证发送结果", SocketManager.getInstance().send(JSON.toJSONString(lora)));
                }
                intentActivity("4");
                break;
            case DRIVE_NODE:
                Plog.e("获取CBoxID 成功", data);
                heartbeatTime = System.currentTimeMillis() / 1000;
                parseDriveStatus(json);
                break;
            case DRIVE_STATUS:
                heartbeatTime = System.currentTimeMillis() / 1000;
                if (json.getBoolean(SUCCEE)) {
                    Plog.e("上传成功", data);
                    String requstId = json.getString("requestId");
                    int result = LitePal.deleteAll(LocalData.class, "requestId = ?", requstId);
                    int result2 = LitePal.deleteAll(Temporary.class, "requestId = ?", requstId);
                    Plog.e("删除ID和结果", requstId + "---" + result, result2);
                } else {
                    Plog.e("采集数据上传失败");
                }
                break;
            case LOCAL_DATA:
                heartbeatTime = System.currentTimeMillis() / 1000;
                if (json.getBoolean(SUCCEE)) {
                    Plog.e("本地数据上传成功", data);
                    uploadLocalSuccee();
                } else {
                    Plog.e("本地数据上传失败");
                }
                break;
            case HEARTBEAT:
                Plog.e("心跳返回数据", data);
                break;
            default:
        }
    }

    /**
     * 本地数据上传成功
     */
    private void uploadLocalSuccee() {
        List<LocalData> localData = LitePal.findAll(LocalData.class);
        if (localData.size() > 0) {
            for (LocalData localData1 : localData) {
                String requstId = localData1.getRequestId();
                Plog.e("查询的 requstId", requstId);
                List<LocalData> statu = localQueryStatu("uploadStatu", requstId);
                for (LocalData status : statu) {
                    if (status.getUploadStatu() == 2) {
                        int result = LitePal.deleteAll(LocalData.class, "requestId = ?", requstId);
                        if (result > 0) {
                            Plog.e("删除ID和结果", requstId + "---" + result);
                        }
                    }
                }
            }
            List<LocalData> localData2 = LitePal.findAll(LocalData.class);
            if (localData2.size() == 0) {
                Plog.e("已经没有本地数据可传了");
                SerialPortUtil.inspectLocalData(null, 0, null, null, null, 0, null, null);
            }
        } else {
            Plog.e("已经没有本地数据可传了");
            SerialPortUtil.inspectLocalData(null, 0, null, null, null, 0, null, null);
        }
    }

    /**
     * CboxId 解析
     */
    private void parseDriveStatus(final JSONObject json) {
        ThreadManager.getInstance().doExecute(() -> {
            List<Duration> durationList = new ArrayList<>();
            List<CboxId> cboxIdList = LitePal.findAll(CboxId.class);
            for (CboxId cboxIdLists : cboxIdList) {
                int cboxId = cboxIdLists.getCboxId();
                List<CboxId> cboxIdQuery = cboxIdQuery("durationTime", Integer.toString(cboxId));
                List<CboxId> stateQuery = cboxIdQuery("state", Integer.toString(cboxId));
                List<CboxId> statusQuery = cboxIdQuery("onLineStatus", Integer.toString(cboxId));
                for (CboxId cboxId1 : cboxIdQuery) {
                    for (CboxId state1 : stateQuery) {
                        for (CboxId status : statusQuery) {
                            try {
                                long durationTime = cboxId1.getDurationTime();
                                int state = state1.getState();
                                int statu = status.getOnLineStatus();
                                Plog.e("存储时间", cboxId + "---" + durationTime + "--状态" + state);
                                Duration duration = new Duration(cboxId, durationTime, state, statu);
                                durationList.add(duration);
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
            Plog.e("数据", durationList);
            Plog.e("删除结果", LitePal.deleteAll(CboxId.class));
            try {
                JSONArray jsonArray = JSONArray.parseArray(json.get("collectNodesList").toString());
                int jsonSize = jsonArray.size();
                Plog.e("数组大小", jsonSize);
                for (int i = 0; i < jsonSize; i++) {
                    String parseData = jsonArray.get(i).toString();
                    JSONObject json2 = JSONObject.parseObject(parseData);
                    int cboxId = json2.getInteger("cBoxID");
                    String id = json2.getString("id");
                    Plog.e("CBoxID", cboxId);
                    Plog.e("id", id);
                    Plog.e("新建");
                    CboxId cboxId1 = new CboxId();
                    cboxId1.setCboxId(cboxId);
                    cboxId1.setNodeId(id);
                    cboxId1.save();
                    ContentValues values = new ContentValues();
                    values.put("nodeId", id);
                    LitePal.updateAll(BeforceStatusLitePal.class, values, "cboxId = ?", Integer.toString(cboxId));
                    ContentValues valuesLocal = new ContentValues();
                    values.put("nodeId", id);
                    LitePal.updateAll(LocalData.class, valuesLocal, "cboxId = ?", Integer.toString(cboxId));
                }

                for (int j = 0; j < durationList.size(); j++) {
                    int cd = durationList.get(j).getId();
                    long dt = durationList.get(j).getDurationTime();
                    int state = durationList.get(j).getState();
                    int statu = durationList.get(j).getOnLineStatus();
                    Plog.e("id", cd);
                    Plog.e("获取的id对应的时间", cd + "---" + dt);
                    Plog.e("获取的C的状态", state);
                    ContentValues values = new ContentValues();
                    values.put("durationTime", dt);
                    values.put("state", state);
                    values.put("onLineStatus", statu);
                    LitePal.updateAll(CboxId.class, values, "cboxId = ?", Integer.toString(cd));
                }

                BaseUtil.intentActivity("0");
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * durationTime 内部类
     */
    class Duration {
        private int id;
        private long durationTime;
        private int state;
        private int onLineStatus;

        private int getId() {
            return id;
        }

        private long getDurationTime() {
            return durationTime;
        }

        private int getState() {
            return state;
        }

        public int getOnLineStatus() {
            return onLineStatus;
        }

        Duration(int id, long durationTime, int state, int onLineStatus) {
            this.id = id;
            this.durationTime = durationTime;
            this.state = state;
            this.onLineStatus = onLineStatus;
        }
    }

    /**
     * 准备本地数据
     */
    public void uploadLocalData() {
        List<CboxId> localData = LitePal.findAll(CboxId.class);
        if (localData.size() > 0) {
            String requestId = null;
            long durationTime;
            int count = 0;
            for (CboxId localData1 : localData) {
                ArrayList<CurrentStatus> localList = new ArrayList<>();
                ArrayList<String> request = new ArrayList<>();
                int cboxId = localData1.getCboxId();
                Plog.e("查询C ID", cboxId);
                List<LocalData> requestList = localQueryRequestId("requestId", Integer.toString(cboxId));
                for (LocalData requestLists : requestList) {
                    request.add(requestLists.getRequestId());
                }
                Plog.e("requestId 数组", request);
                int requestSize = request.size();
                if (requestSize > 0) {
                    for (int i = 0; i < requestSize; i++) {
                        requestId = request.get(i);
                        List<LocalData> local1 = localQueryStatu("nodeId", requestId);
                        List<LocalData> local2 = localQueryStatu("status", requestId);
                        List<LocalData> local3 = localQueryStatu("actionTime", requestId);
                        List<LocalData> local4 = localQueryStatu("durationTime", requestId);
                        List<LocalData> local5 = localQueryStatu("clientId", requestId);
                        for (LocalData local1s : local1) {
                            for (LocalData local2s : local2) {
                                for (LocalData local3s : local3) {
                                    for (LocalData local4s : local4) {
                                        for (LocalData local5s : local5) {
                                            CurrentStatus current = new CurrentStatus();
                                            String nodeId = local1s.getNodeId();
                                            String status = local2s.getStatus();
                                            Date actionTime = local3s.getActionTime();
                                            durationTime = local4s.getDurationTime();

                                            current.setNodeId(nodeId);
                                            current.setStatus(status);
                                            current.setActionTime(actionTime);
                                            current.setDurationTime(durationTime);
                                            current.setClientId(local5s.getClientId());

                                            count++;
                                            if (count <= 20) {
                                                updateStatus(2, requestId);
                                                localList.add(current);
                                                Plog.e("依次添加数据");
                                            } else {
                                                Plog.e("超过20条后 发送数据");
                                                uploadLoraParameter(localList, requestId);
                                                count = 0;
                                            }

                                        }
                                    }
                                }
                            }
                        }
                    }
                    uploadLoraParameter(localList, requestId);
                }
            }
        } else {
            Plog.e("数据为null");
        }
    }

    /**
     * lora本地数据上报
     */
    private void uploadLoraParameter(ArrayList<CurrentStatus> localList, String requestId) {
        LoraParameter lora = new LoraParameter();
        lora.setConnTypeEnum(LOCAL_DATA);
        lora.setCertification(getCertification());
        lora.setLocalSaveStatusList(localList);
        lora.setRequestId(requestId);
        lora.setResult(false);
        Plog.e("最后发送数据", JSON.toJSONString(lora));
        SocketManager.getInstance().send(JSON.toJSONString(lora));
    }

    /**
     * lora心跳周期
     */
    private void loraHeartbeat() {
        int heartbeat2 = SpUtil.readInt(Const.HEART_TIME_A);
        if (heartbeat2 != 0) {
            heartbeat = heartbeat2;
        }
        ExecutorServiceManager.getInstance().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis() / 1000;
                long d = currentTime - heartbeatTime;
                Plog.e("时间差", d);
                boolean result = SpUtil.readBoolean(CERTIFICATION);
                if (d >= heartbeat && result) {
                    Plog.e("可以发送心跳");
                    sendHeartbeat();
                }
            }
        }, 10, heartbeat, TimeUnit.SECONDS);
    }

    /**
     * 发送心跳数据
     */
    private void sendHeartbeat() {
        LoraParameter lora = setLoraSendParameter(HEARTBEAT);
        int sendLength = SocketManager.getInstance().send(JSON.toJSONString(lora));
        if (sendLength == 0) {
            loraCertification();
        }
    }

    /**
     * 定期检查Cbox
     */
    private void inspectionTiming() {
        int period2 = SpUtil.readInt(Const.HEART_TIME_C);
        if (period2 != 0) {
            period = period2;
        }
        int offTime2 = SpUtil.readInt(Const.DOWN_TIME);
        if (offTime2 != 0) {
            offTime = offTime2;
        }
        ExecutorServiceManager.getInstance().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                long time = System.currentTimeMillis();
                List<CboxId> cboxIdList = LitePal.findAll(CboxId.class);
                for (CboxId cboxIdLists : cboxIdList) {
                    String loraId = cboxIdLists.getNodeId();
                    List<CboxId> getTime = loraIdQuery("durationTime", loraId);
                    for (CboxId getTimes : getTime) {
                        long durationTime = getTimes.getDurationTime();
                        long difference = time - durationTime;
                        long differenceTime = difference / 1000;
                        Plog.e("两次数据时间差", differenceTime + "---" + loraId);
                        if (differenceTime >= offTime) {
                            updateCboxaSatus2(1, loraId);
                            List<CboxId> getCboxIdState = loraIdQuery("state", loraId);
                            for (CboxId getCboxIdStates : getCboxIdState) {
                                int state = getCboxIdStates.getState();
                                if (state != 4) {
                                    List<CboxId> getCboxIdCboxId = loraIdQuery("cboxId", loraId);
                                    for (CboxId getCboxIdCboxIds : getCboxIdCboxId) {
                                        List<BeforceStatusLitePal> beforceList = nodeIdQuery("actionTime", loraId);
                                        for (BeforceStatusLitePal beforceLists : beforceList) {
                                            List<BeforceStatusLitePal> statusList = nodeIdQuery("status", loraId);
                                            for (BeforceStatusLitePal statusLists : statusList) {
                                                List<BeforceStatusLitePal> clientIdList = nodeIdQuery("clientId", loraId);
                                                for (BeforceStatusLitePal clientIdLists : clientIdList) {
                                                    try {
                                                        LoraParameter lora = getLoraData(getCboxIdCboxIds.getCboxId(), loraId, beforceLists, statusLists, time, clientIdLists);
                                                        SocketManager.getInstance().send(JSON.toJSONString(lora));
                                                        ContentValues values = new ContentValues();
                                                        values.put("state", 4);
                                                        LitePal.updateAll(CboxId.class, values, "nodeId = ?", loraId);

                                                    } catch (ParseException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }, 10, period, TimeUnit.SECONDS);
    }

    /**
     * LoraParameter 添加数据
     */
    private LoraParameter getLoraData(int cboxId, String loraId, BeforceStatusLitePal beforceLists, BeforceStatusLitePal statusLists,
                                      long time, BeforceStatusLitePal clientIdLists) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        Date date, date2;
        date = sdf.parse(beforceLists.getActionTime());
        Plog.e("启动时间", date);
        String bfStatu = statusLists.getStatus();
        Plog.e("状态", bfStatu);
        String bfClient = clientIdLists.getClientId();
        Plog.e("bfClient", bfClient);
        String currentActionTime = sdf.format(time);
        Plog.e("关机的动作时间", currentActionTime);
        assert date != null;
        long startTime = date.getTime();
        date2 = sdf.parse(currentActionTime);
        assert date2 != null;
        long endTime = date2.getTime();
        long durationTime = endTime - startTime;
        Plog.e("关机动作时长", durationTime);

        BeforceStatus beforce = new BeforceStatus();
        beforce.setNodeId(loraId);
        beforce.setStatus(bfStatu);
        beforce.setActionTime(date);
        beforce.setDurationTime(durationTime);
        beforce.setClientId(bfClient);

        String clientId = UUID.randomUUID().toString().replace("-", "");
        CurrentStatus current = new CurrentStatus();
        current.setNodeId(loraId);
        current.setStatus(OFF);
        current.setActionTime(sdf.parse(currentActionTime));
        current.setDurationTime(0);
        current.setClientId(clientId);

        StatusGroup statusGroup = new StatusGroup();
        statusGroup.setBeforceStatus(beforce);
        statusGroup.setCurrentStatus(current);

        LoraParameter lora = new LoraParameter();
        lora.setConnTypeEnum(DRIVE_STATUS);
        lora.setCertification(getCertification());
        lora.setStatusGroup(statusGroup);
        lora.setResult(false);

        saveBeforceStartus(cboxId, loraId, OFF, currentActionTime, durationTime, clientId);

        return lora;
    }

    /**
     * MonitorService绑定
     */
    public class ServiceBinder extends Binder {
        public MonitorService getService() {
            return MonitorService.this;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SocketManager.getInstance().close();
        Plog.e("销毁广播");
        System.exit(0);
    }
}

