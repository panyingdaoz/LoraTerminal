package com.kingbird.loraterminal.manager;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Calendar;

import static com.kingbird.loraterminal.utils.Config.CONSTANT_TWO;

/**
 * 协议管理
 *
 * @author panyingdao
 * @date 2017-8-16.
 */

public class ProtocolDao {
    /**
     * 帧头
     */
    private static final short CONFIGURE_HEADER_NORMAL_LORA = (byte) 0x55;
    private static final short HEADER_NORMAL = (short) 0xA991;
    /**
     * 包尾
     */
    private static final short END_NORMAL_LORA = (byte) 0xAA;

    private static class HolderClass {
        private final static ProtocolDao INSTANCE = new ProtocolDao();
    }

    public static ProtocolDao getInstance() {
        return HolderClass.INSTANCE;
    }

    /**
     * 校验
     */
    private static byte calcCheckSum(byte[] data, int len) {
        byte sum = 0;
        for (int i = 0; i < len; i++) {
            sum += data[i];
        }
        return sum;
    }

    /**
     * 16进制补零
     *
     * @param a   参数长度
     * @param len 可以存储长度
     * @return 返回
     */
    private static String intToHexString(int a, int len) {
        len <<= 1;
        String hexString = Integer.toHexString(a);
        int b = len - hexString.length();
        if (b > 0) {
            for (int i = 0; i < b; i++) {
                hexString = "0" + hexString;
            }
        }
        return hexString;
    }

    /**
     * 验证码 16进制字符转换为字节字符
     */
    private static byte[] hexString2Bytes(String src) {
        if (null == src || 0 == src.length()) {
            return null;
        }
        byte[] ret = new byte[src.length() / 2];
        byte[] tmp = src.getBytes();
        for (int i = 0; i < (tmp.length / CONSTANT_TWO); i++) {
            ret[i] = uniteBytes(tmp[i * 2], tmp[i * 2 + 1]);
        }
        return ret;
    }

    /**
     * 字节合并
     */
    private static byte uniteBytes(byte src0, byte src1) {
        byte byteValue = Byte.decode("0x" + new String(new byte[]{src0}));
        byteValue = (byte) (byteValue << 4);
        byte byteValue1 = Byte.decode("0x" + new String(new byte[]{src1}));

        return (byte) (byteValue ^ byteValue1);
    }

    /**
     * Cbox 注册回复
     */
    public static byte[] loarRegisterAnswer(int cboxId, boolean isResult) {
        byte[] data = new byte[14];

        //帧头
        data[0] = CONFIGURE_HEADER_NORMAL_LORA;

        //cbox ID
        data[1] = (byte) cboxId;

        //功能码
        data[2] = (byte) 0x01;

        //数据长度
        data[3] = (byte) 8;

        //认证结果
        data[4] = (byte) (isResult ? 1 : 0);

        //时间
        calender(data, 5, 7, 8, 9, 10, 11);

        //校验
        data[12] = calcCheckSum(data, data.length - 2);

        //包尾
        data[13] = END_NORMAL_LORA;

        return data;
    }

    /**
     * Cbox 数据接收回复
     */
    public static byte[] loarDataAnswer(int cboxId, int function) {
        byte[] data = new byte[13];

        //帧头
        data[0] = CONFIGURE_HEADER_NORMAL_LORA;

        //cbox ID
        data[1] = (byte) cboxId;
//        System.arraycopy(cboxId.getBytes(), 0, data, 1, 1);

        //功能码
//        data[2] = (byte) (function ? 2 : 3);
        data[2] = (byte) function;

        //数据长度
        data[3] = (byte) 7;

        //时间
        calender(data, 4, 6, 7, 8, 9, 10);

        //校验
        data[11] = calcCheckSum(data, data.length - 2);

        //包尾
        data[12] = END_NORMAL_LORA;

        return data;
    }

    /**
     * Cbox 测试数据
     */
    public static byte[] test(int cboxId, int function, int statuTime) {
        byte[] data = new byte[21];

        //帧头
        data[0] = CONFIGURE_HEADER_NORMAL_LORA;

        //cbox ID
        data[1] = (byte) cboxId;

        //功能码
        data[2] = (byte) function;

        //数据长度
        data[3] = (byte) 15;

        // 数据类型
        data[4] = (byte) 1;

        //记录数组
        data[5] = (byte) 1;

        //开关ID
        data[6] = (byte) 1;

        //启动时间
        calender(data, 7, 9, 10, 11, 12, 13);

        // 状态时长
        System.arraycopy(intToButeArray(statuTime), 0, data, 14, 4);

        //状态类型
        data[18] = (byte) 1;

        //校验
        data[19] = calcCheckSum(data, data.length - 2);

        //包尾
        data[20] = END_NORMAL_LORA;

        return data;
    }

    /**
     * 时间获取
     */
    private static void calender(byte[] data, int i, int i2, int i3, int i4, int i5, int i6) {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int date = cal.get(Calendar.DATE);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);

        byte[] yearBytes = hexString2Bytes(intToHexString(year, 2));
        System.arraycopy(yearBytes, 0, data, i, 2);
        data[i2] = (byte) month;
        data[i3] = (byte) date;
        data[i4] = (byte) hour;
        data[i5] = (byte) minute;
        data[i6] = (byte) second;
    }

    public static byte[] loraHeartBeat(String deviceId) {

        byte[] data = new byte[37];

        //帧头
        data[0] = (byte) ((HEADER_NORMAL >> 8) & 0xFF);
        data[1] = (byte) (HEADER_NORMAL & 0xFF);

        //设备ID
        System.arraycopy(deviceId.getBytes(), 0, data, 2, 32);

        //功能码
        data[34] = (byte) 0x0A;

        //数据长度
        data[35] = (byte) 0;

        //校验
        data[36] = calcCheckSum(data, data.length - 1);

        return data;
    }

    /**
     * 终端log上传回应
     */
    public static byte[] appLogAnswer(String deviceId, boolean isResult) {

        byte[] data = new byte[17];

        //帧头
        data[0] = (byte) ((HEADER_NORMAL >> 8) & 0xFF);
        data[1] = (byte) (HEADER_NORMAL & 0xFF);

        //设备ID
        System.arraycopy(deviceId.getBytes(), 0, data, 2, 11);

        //功能码
        data[13] = (byte) 0x0B;

        //数据长度
        data[14] = (byte) 1;

        //LOG 上传结果
        data[15] = (byte) (isResult ? 1 : 0);

        //校验
        data[16] = calcCheckSum(data, data.length - 1);

        return data;
    }

    /**
     * 终端更新回应
     */
    public static byte[] appUpdateAswer(String deviceId, boolean isResult) {

        byte[] data = new byte[17];

        //帧头
        data[0] = (byte) ((HEADER_NORMAL >> 8) & 0xFF);
        data[1] = (byte) (HEADER_NORMAL & 0xFF);

        //设备ID
        System.arraycopy(deviceId.getBytes(), 0, data, 2, 11);

        //功能码
        data[13] = (byte) 0x0C;

        //数据长度
        data[14] = (byte) 1;

        //LOG 上传结果
        data[15] = (byte) (isResult ? 1 : 0);

        //校验
        data[16] = calcCheckSum(data, data.length - 1);

        return data;
    }

    /**
     * @param n 需要转换整数
     * @return 返回
     */
    public static byte[] intToButeArray(int n) {
        byte[] byteArray = null;
        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            DataOutputStream dataOut = new DataOutputStream(byteOut);
            dataOut.writeInt(n);
            byteArray = byteOut.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteArray;
    }

    public static boolean parseRecvRegisterData(byte[] data, String userID) {

        if (data[0] != (byte) ((CONFIGURE_HEADER_NORMAL_LORA >> 8) & 0xFF)) {
            return false;
        }

        if (!checkEquals(data, 1, 1, userID)) {
            return false;
        }

        if (data[2] != (byte) 1) {
            return false;
        }

        if (data[3] != 0) {
            return false;
        }

        if (data[5] != (byte) ((END_NORMAL_LORA >> 8) & 0xFF)) {
            return false;
        }

        return true;

    }

    private static boolean checkEquals(byte[] data, int start, int len, String expectData) {
        if (expectData.length() != len) {
            return false;
        }
        byte[] dest = new byte[len];
        System.arraycopy(expectData.getBytes(), 0, dest, 0, len);

        for (int i = 0; i < len; i++) {
            if (dest[i] != data[start + i]) {
                return false;
            }
        }
        return true;
    }
}
