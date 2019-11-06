package com.kingbird.loraterminal.utils;

import com.alibaba.fastjson.JSON;
import com.kingbird.loraterminal.entity.BeforceStatus;
import com.kingbird.loraterminal.entity.BeforceStatusLitePal;
import com.kingbird.loraterminal.entity.CboxId;
import com.kingbird.loraterminal.entity.CurrentStatus;
import com.kingbird.loraterminal.entity.LocalData;
import com.kingbird.loraterminal.entity.LoraParameter;
import com.kingbird.loraterminal.entity.StatusGroup;
import com.kingbird.loraterminal.entity.Temporary;
import com.kingbird.loraterminal.manager.ProtocolDao;
import com.kingbird.loraterminal.manager.ProtocolManager;
import com.kingbird.loraterminal.manager.SocketManager;
import com.kingbird.loraterminal.manager.ThreadManager;
import com.kingbird.loraterminal.service.MonitorService;

import org.litepal.LitePal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import android_serialport_api.SerialPort;

import static com.kingbird.loraterminal.utils.BaseUtil.bytes2HexString;
import static com.kingbird.loraterminal.utils.BaseUtil.cboxIdQuery;
import static com.kingbird.loraterminal.utils.BaseUtil.cboxdQuery;
import static com.kingbird.loraterminal.utils.BaseUtil.getAnIntHex;
import static com.kingbird.loraterminal.utils.BaseUtil.getAnString;
import static com.kingbird.loraterminal.utils.BaseUtil.getCertification;
import static com.kingbird.loraterminal.utils.BaseUtil.intentActivity;
import static com.kingbird.loraterminal.utils.BaseUtil.localDataSave;
import static com.kingbird.loraterminal.utils.BaseUtil.newBeforceStartus;
import static com.kingbird.loraterminal.utils.BaseUtil.readTemporarySize;
import static com.kingbird.loraterminal.utils.BaseUtil.saveBeforceStartus;
import static com.kingbird.loraterminal.utils.BaseUtil.updateCboxaAtionTime;
import static com.kingbird.loraterminal.utils.BaseUtil.updateCboxaSatus;
import static com.kingbird.loraterminal.utils.BaseUtil.updateCboxaState;
import static com.kingbird.loraterminal.utils.Config.BUSY;
import static com.kingbird.loraterminal.utils.Config.DRIVE_STATUS;
import static com.kingbird.loraterminal.utils.Config.FREE;
import static com.kingbird.loraterminal.utils.Config.HEAD;
import static com.kingbird.loraterminal.utils.Config.OFF;
import static com.kingbird.loraterminal.utils.Config.STRING_01;
import static com.kingbird.loraterminal.utils.Config.STRING_02;
import static com.kingbird.loraterminal.utils.Config.TAIL;

/**
 * 串口辅助类
 *
 * @author Pan yingdao
 * @date 2019/7/19/027.
 */
public class SerialPortUtil {
    /**
     * 标记当前串口状态(true:打开,false:关闭)
     **/
    private static boolean isFlagSerial = false;
    private static InputStream inputStream = null;
    private static OutputStream outputStream = null;
    private static Thread receiveThread = null;
    private static long current = 0;
    private static int baudrate = 9600;
    private static byte[] byteFinal;

    /**
     * 打开串口
     */
    public static boolean open() {
        boolean isopen;
        if (isFlagSerial) {
            Plog.e("串口已经打开,打开失败");
            return false;
        }
        try {
            int baudrate2 = SpUtil.readInt(Const.BAUDRATE);
            if (baudrate2 != 0) {
                baudrate = baudrate2;
            }
            Plog.e("波特率", baudrate);
            SerialPort serialPort = new SerialPort(new File("/dev/ttyS6"), baudrate);
//            SerialPort serialPort = new SerialPort(new File("/dev/ttyS6"), 115200);
            inputStream = serialPort.getInputStream();
            outputStream = serialPort.getOutputStream();
            receive();
            isopen = true;
            isFlagSerial = true;
        } catch (IOException e) {
            e.printStackTrace();
            isopen = false;
        }
        return isopen;
    }

    /**
     * 关闭串口
     */
    public static boolean close() {
        if (isFlagSerial) {
            Plog.e("串口关闭失败");
            return false;
        }
        boolean isClose;
        Plog.e("关闭串口");
        try {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
            isClose = true;
            //关闭串口时，连接状态标记为false
            isFlagSerial = false;
//            if (sendList.size() > 0) {
//                sendList.clear();
//            }
        } catch (IOException e) {
            e.printStackTrace();
            isClose = false;
        }
        return isClose;
    }

    /**
     * 发送串口指令
     */
    private static void sendPort(byte[] data) {
        if (!isFlagSerial) {
            Plog.e("串口未打开,发送失败", bytes2HexString(data));
            return;
        }
        try {
            outputStream.write(data, 0, data.length);
            Plog.e("回复数据", bytes2HexString(data));
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            Plog.e("发送指令出现异常");
        }
    }

    /**
     * 接收串口数据的方法
     */
    private static void receive() {
        if (receiveThread != null && !isFlagSerial) {
            return;
        }
        ThreadManager.getInstance().doExecute(() -> {
            while (isFlagSerial) {
                try {
                    byte[] data = new byte[inputStream.available()];
                    if (inputStream == null) {
                        return;
                    }
                    int size = inputStream.read(data);
                    if (size > 0 && isFlagSerial) {
                        current = System.currentTimeMillis();
                        if (byteFinal == null) {
                            byteFinal = data;
                        } else {
                            byteFinal = BaseUtil.mergerArray(byteFinal, data);
                        }
                        Plog.e("当前数据", bytes2HexString(byteFinal));
                    } else if (size == 0) {
                        long time = System.currentTimeMillis();
                        long difference = time - current;
                        if (difference > 100 && byteFinal != null) {
                            Plog.e("最终数据", bytes2HexString(byteFinal));
                            serialPortParse(byteFinal);
                            byteFinal = null;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Cbox 数据校验
     */
    public static void serialPortParse(byte[] data) {
        String recData = bytes2HexString(data);
        Plog.e("解析的数据", recData);
        String head = bytes2HexString(ProtocolManager.getInstance().parseParameter(data, 0, 1));
        String tail = bytes2HexString(ProtocolManager.getInstance().parseParameter(data, data.length - 1, 1));
        if (HEAD.equals(head) && TAIL.equals(tail)) {
            int cboxId = getAnIntHex(data, 1, 1, 16);
            List<CboxId> cboxIdList = LitePal.findAll(CboxId.class);
            Plog.e("CboxId 大小", cboxIdList.size());
            for (CboxId cboxId1 : cboxIdList) {
                int readId = cboxId1.getCboxId();
                Plog.e("接收CboxID", cboxId);
                Plog.e("查询ID", readId);
                if (readId == cboxId) {
                    String function = bytes2HexString(ProtocolManager.getInstance().parseParameter(data, 2, 1));
                    Plog.e("功能码", function);
                    switch (function) {
                        case "01":
                            Plog.e("注册回复");
                            SerialPortUtil.sendPort(ProtocolDao.loarRegisterAnswer(cboxId, true));
                            long time = System.currentTimeMillis();
                            updateCboxaAtionTime(time, Integer.toString(cboxId));
                            break;
                        case "02":
                            long time2 = System.currentTimeMillis();
                            updateCboxaAtionTime(time2, Integer.toString(cboxId));
                            updateCboxaState(2, Integer.toString(cboxId));
                            updateCboxaSatus(2, Integer.toString(cboxId));
                            int dataLength = getAnIntHex(data, 3, 1, 16);
                            String dataType = getAnString(data, 4, 1);
                            Plog.e("数据类型", dataType);
                            byte[] dataUseful = ProtocolManager.getInstance().parseParameter(data, 6, dataLength - 2);
                            SpUtil.writeString(Const.RECEIVE_CBOX, bytes2HexString(dataUseful));
                            SpUtil.writeString(Const.CBOXID, Integer.toString(cboxId));
                            intentActivity("1");
                            switch (dataType) {
                                case "00":
                                    SerialPortUtil.sendPort(ProtocolDao.loarDataAnswer(cboxId, Integer.parseInt(function)));
                                    parseArray(dataUseful, cboxId, function);
                                    break;
                                case "01":
                                    SerialPortUtil.sendPort(ProtocolDao.loarDataAnswer(cboxId, Integer.parseInt(function)));
                                    parseArray(dataUseful, cboxId, function);
                                    break;
                                default:
                            }
                            break;
                        case "03":
                            long time3 = System.currentTimeMillis();
                            updateCboxaAtionTime(time3, Integer.toString(cboxId));
                            updateCboxaSatus(2, Integer.toString(cboxId));
                            SerialPortUtil.sendPort(ProtocolDao.loarDataAnswer(cboxId, Integer.parseInt(function)));
                            List<CboxId> cboxIdList1 = cboxIdQuery("state", Integer.toString(cboxId));
                            for (CboxId cboxId2 : cboxIdList1) {
                                if (cboxId2.getState() != 3) {
                                    updateCboxaState(3, Integer.toString(cboxId));
                                    int dataLength2 = getAnIntHex(data, 3, 1, 16);
                                    byte[] dataUseful2 = ProtocolManager.getInstance().parseParameter(data, 6, dataLength2 - 2);
                                    parseArray(dataUseful2, cboxId, function);
                                }
                            }
                        case "05":
                            SerialPortUtil.sendPort(data);
                            SpUtil.writeString(Const.HEARTBEAT_DATA, bytes2HexString(data));
                            SpUtil.writeString(Const.CBOXID, Integer.toString(cboxId));
                            intentActivity("2");
                            long time5 = System.currentTimeMillis();
                            updateCboxaAtionTime(time5, Integer.toString(cboxId));
                            updateCboxaSatus(2, Integer.toString(cboxId));
                            loraFunction5(cboxId, time5);
                            break;
                        default:
                    }
                }
            }
        }
    }

    /**
     * 如果上一次是关机状态就回复空闲状态
     */
    private static void loraFunction5(int readId, long time5) {
        List<BeforceStatusLitePal> statusList = cboxdQuery("status", Integer.toString(readId));
        for (BeforceStatusLitePal statusLists : statusList) {
            if (OFF.equals(statusLists.getStatus())) {
                updateCboxaState(3, Integer.toString(readId));
                List<BeforceStatusLitePal> nodeIdList = cboxdQuery("nodeId", Integer.toString(readId));
                for (BeforceStatusLitePal nodeIdLists : nodeIdList) {
                    List<BeforceStatusLitePal> actionTimeList = cboxdQuery("actionTime", Integer.toString(readId));
                    for (BeforceStatusLitePal actionTimeLists : actionTimeList) {
                        List<BeforceStatusLitePal> clientIdList = cboxdQuery("clientId", Integer.toString(readId));
                        for (BeforceStatusLitePal clientIdLists : clientIdList) {
                            String nodeId = nodeIdLists.getNodeId();
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
                            try {
                                String actionTime = actionTimeLists.getActionTime();
                                Date date = sdf.parse(actionTime);
                                assert date != null;
                                long dateLong = date.getTime();
                                long dt = time5 - dateLong;
                                Plog.e("关机到空闲时间差", dt);
                                BeforceStatus beforce = new BeforceStatus();
                                beforce.setNodeId(nodeId);
                                beforce.setStatus(OFF);
                                beforce.setActionTime(date);
                                beforce.setDurationTime(dt);
                                beforce.setClientId(clientIdLists.getClientId());

                                String clientId = UUID.randomUUID().toString().replace("-", "");
                                CurrentStatus current = new CurrentStatus();
                                current.setNodeId(nodeId);
                                current.setStatus(FREE);
                                current.setActionTime(new Date(time5));
                                current.setDurationTime(0);
                                current.setClientId(clientId);

                                StatusGroup statusGroup = new StatusGroup();
                                statusGroup.setBeforceStatus(beforce);
                                statusGroup.setCurrentStatus(current);

                                String requestId = UUID.randomUUID().toString().replace("-", "");
                                LoraParameter lora = new LoraParameter();
                                lora.setConnTypeEnum(DRIVE_STATUS);
                                lora.setCertification(getCertification());
                                lora.setStatusGroup(statusGroup);
                                lora.setRequestId(requestId);
                                lora.setResult(false);

                                String sendData = JSON.toJSONString(lora);
                                SocketManager.getInstance().send(sendData);

                                Plog.e("记录action", sdf.format(time5));
                                saveBeforceStartus(readId, nodeId, FREE, sdf.format(time5), dt, clientId);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Cbox 数据解析
     */
    private static void parseArray(byte[] dataUseful, int readId, String function) {
        try {
            String currentStatu;
            Date beforceAction = null;
            boolean isSend = false;
            Plog.e("有效数据", bytes2HexString(dataUseful));
            String onoffId = getAnString(dataUseful, 0, 1);
            Plog.e("开关ID和C端ID", onoffId, readId);
            if (STRING_01.equals(onoffId)) {
                List<CboxId> loraIdList = cboxIdQuery("nodeId", Integer.toString(readId));
                for (CboxId loraIds : loraIdList) {
                    String loraId = loraIds.getNodeId();
                    Plog.e("查询的nodeId", loraId);
                    int year = getAnIntHex(dataUseful, 1, 2, 16);
                    int month = getAnIntHex(dataUseful, 3, 1, 16);
                    int day = getAnIntHex(dataUseful, 4, 1, 16);
                    int hour = getAnIntHex(dataUseful, 5, 1, 16);
                    int minue = getAnIntHex(dataUseful, 6, 1, 16);
                    int second = getAnIntHex(dataUseful, 7, 1, 16);
                    String endTime = year + "-" + month + "-" + day + " " + hour + ":" + minue + ":" + second;
                    Plog.e("actionTime时间：", endTime);
                    Date date;
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
                    if (STRING_02.equals(function)) {
                        date = sdf.parse(endTime);
                    } else {
                        date = new Date();
                    }
                    Plog.e("启动时间", date);
                    int statuTime = getAnIntHex(dataUseful, 8, 4, 16);
                    Plog.e("状态时长", statuTime);
                    int statuType = getAnIntHex(dataUseful, 12, 1, 16);
                    Plog.e("状态类型", statuType);
                    if (statuType == 1) {
                        currentStatu = BUSY;
                    } else {
                        currentStatu = FREE;
                    }

                    String requestId = UUID.randomUUID().toString().replace("-", "");
                    String clientId = UUID.randomUUID().toString().replace("-", "");

                    BeforceStatus beforce = new BeforceStatus();
                    List<BeforceStatusLitePal> nodeIdList = cboxdQuery("nodeId", Integer.toString(readId));
                    Plog.e("上次数据大小", nodeIdList.size());
                    if (nodeIdList.size() > 0) {
                        for (BeforceStatusLitePal nodeIdLists : nodeIdList) {
                            List<BeforceStatusLitePal> statusList = cboxdQuery("status", Integer.toString(readId));
                            for (BeforceStatusLitePal statusLists : statusList) {
                                List<BeforceStatusLitePal> actionTimeList = cboxdQuery("actionTime", Integer.toString(readId));
                                for (BeforceStatusLitePal actionTimeLists : actionTimeList) {
                                    List<BeforceStatusLitePal> clientIdList = cboxdQuery("clientId", Integer.toString(readId));
                                    for (BeforceStatusLitePal clientIdLists : clientIdList) {
                                        String beforceNodeId = nodeIdLists.getNodeId();
                                        String beforceStatu = statusLists.getStatus();
                                        beforceAction = sdf.parse(actionTimeLists.getActionTime());
                                        assert beforceAction != null;
                                        long bAction = beforceAction.getTime();
                                        String beforceClient = clientIdLists.getClientId();
                                        Plog.e("上一次", beforceNodeId);
                                        Plog.e("上一次", beforceStatu);
                                        Plog.e("上一次", beforceAction);
                                        Plog.e("上一次", beforceClient);
                                        beforce.setNodeId(beforceNodeId);
                                        beforce.setStatus(beforceStatu);
                                        beforce.setActionTime(beforceAction);
                                        if (STRING_02.equals(function)) {
                                            beforce.setDurationTime(statuTime);
                                        } else {
                                            long current = date.getTime();
                                            long newDurationTime = current - bAction;
                                            Plog.e("空闲状态时长", newDurationTime);
                                            beforce.setDurationTime(newDurationTime);
                                        }
                                        beforce.setClientId(beforceClient);
                                    }
                                }
                            }
                        }
                    } else {
                        Plog.e("不存在开始添加");
                        newBeforceStartus(readId, loraId, currentStatu, endTime, statuTime, clientId);
                        beforce = null;
                    }

                    assert date != null;
                    assert beforceAction != null;
                    if (date.getTime() != 0 && beforceAction.getTime() != 0) {
                        if (date.getTime() == beforceAction.getTime()) {
                            isSend = false;
                            Plog.e("本条数据是重复数据。取消发送");
                        } else {
                            isSend = true;
                            Plog.e("本条数据不是是重复数据。可以发送");
                        }
                    } else {
                        isSend = true;
                        Plog.e("要发送数据！");
                    }

                    if (isSend) {
                        CurrentStatus current = new CurrentStatus();
                        current.setNodeId(loraId);
                        current.setStatus(currentStatu);
                        current.setActionTime(date);
                        current.setDurationTime(0);
                        current.setClientId(clientId);

                        StatusGroup statusGroup = new StatusGroup();
                        statusGroup.setBeforceStatus(beforce);
                        statusGroup.setCurrentStatus(current);

                        LoraParameter lora = new LoraParameter();
                        lora.setConnTypeEnum(DRIVE_STATUS);
                        lora.setCertification(getCertification());
                        lora.setStatusGroup(statusGroup);
                        lora.setRequestId(requestId);
                        lora.setResult(false);

                        String sendData = JSON.toJSONString(lora);

                        //保存current记录
                        saveBeforceStartus(readId, loraId, currentStatu, endTime, statuTime, clientId);
                        //检查本地数据是否发送完成
                        inspectLocalData(sendData, readId, loraId, currentStatu, date, statuTime, requestId, clientId);
//                    // 保存发送的当前数据
//                    localDataSave(readId, loraId, currentStatu, date, statuTime, requestId, clientId);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 检查本地数据
     */
    public static void inspectLocalData(String sendData, int readId, String loraId, String currentStatu, Date date,
                                        int statuTime, String requestId, String clientId) {
        List<LocalData> localData = LitePal.findAll(LocalData.class);
        Plog.e("本地数据大小", localData.size());
        if (localData.size() > 0) {
            if (sendData != null && requestId != null) {
                Temporary temporary = new Temporary();
                temporary.temPoraryData(requestId, sendData);
                temporary.save();
                MonitorService monitor = new MonitorService();
                monitor.uploadLocalData();
            }
        } else {
            int temSize = readTemporarySize();
            Plog.e("临时数据大小", temSize);
            if (temSize > 0) {
                if (sendData != null && requestId != null) {
                    Temporary temporary = new Temporary();
                    temporary.temPoraryData(requestId, sendData);
                    temporary.save();
                }
                List<Temporary> temporaryList = LitePal.findAll(Temporary.class);
                for (Temporary temporaryLists : temporaryList) {
                    String requstId2 = temporaryLists.getRequestId();
                    Plog.e("获取requstId2", requstId2);
                    String data = temporaryLists.getSendData();
                    if (data != null) {
                        int sendLength = SocketManager.getInstance().send(data);
                        Plog.e("发送数据长度", sendLength);
                        if (sendLength > 0) {
                            int result = LitePal.deleteAll(Temporary.class, "requestId = ?", requestId);
                            Plog.e("删除临时数据结果", result);
                        }
                    }
                }
            } else {
                if (sendData != null) {
                    Plog.e("直接发送消息");
                    SocketManager.getInstance().send(sendData);
//                    // 保存发送的当前数据
//                    localDataSave(readId, loraId, currentStatu, date, statuTime, requestId, clientId);
                }
            }
            if (sendData != null) {
                // 保存发送的当前数据
                localDataSave(readId, loraId, currentStatu, date, statuTime, requestId, clientId);
            }
        }
    }

//    private static class TemporaryData {
//        private String requestId;
//        private String sendData;
//
//        String getSendData() {
//            return sendData;
//        }
//
//        String getRequestId() {
//            return requestId;
//        }
//
//        TemporaryData(String requestId, String sendData) {
//            Plog.e("添加的 sendData", sendData);
//            this.requestId = requestId;
//            this.sendData = sendData;
//        }
//    }
}