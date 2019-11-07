package com.kingbird.loraterminal.activity;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import androidx.multidex.MultiDex;

import com.kingbird.loraterminal.manager.SocketManager;
import com.kingbird.loraterminal.utils.Const;
import com.kingbird.loraterminal.utils.Plog;
import com.kingbird.loraterminal.utils.SpUtil;

import org.litepal.LitePal;
import org.litepal.tablemanager.Connector;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

import static com.kingbird.loraterminal.utils.Config.ROOT_DIRECTORY_URL;

/**
 * 说明：Application
 *
 * @author Pan Yingdao
 * @time : 2019/7/23/023
 */
public class LoraApplication extends Application {

    private static LoraApplication instance;

    public static LoraApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        LitePal.initialize(this);
        Connector.getDatabase();
        Plog.init(true);
        readConfig();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        // 主要是添加下面这句代码
        MultiDex.install(LoraApplication.this);
    }

    /**
     * 读取配置文件信息
     */
    private void readConfig() {
        String filePathName = ROOT_DIRECTORY_URL + "config.prop";
        Plog.e("配置文件路径：" + filePathName);
        File fileCheck = new File(filePathName);
        Plog.e("文件是否存在：" + fileCheck.exists(), fileCheck.isFile());
        if (fileCheck.exists() && fileCheck.isFile()) {
            try {
                Properties pro = new Properties();
                FileInputStream in = new FileInputStream(fileCheck);
                pro.load(in);
                String companyId = pro.getProperty("companyID");
                String relayId = pro.getProperty("relayID");
                Plog.e("或取到的公司ID和继电器ID：" + companyId + "\n" + relayId);
                if (companyId != null && companyId.length() > 0 && relayId != null && relayId.length() > 0) {
                    SpUtil.writeString(Const.COMPANY_ID, companyId);
                    SpUtil.writeString(Const.RELAY_ID, relayId);
                } else {
                    Plog.e("配置文件格式错误!");
                }
                String ip = pro.getProperty("ip");
                int port = Integer.parseInt(Objects.requireNonNull(pro.get("port")).toString());
                Plog.e("ip：" + ip);
                Plog.e("port：" + port);
                if (ip != null && ip.length() > 0 && port != 0) {
                    SocketManager.getInstance().setSocket(ip, port);
                }
                int heartTimeAbox = Integer.parseInt(Objects.requireNonNull(pro.get("heartTimeA")).toString());
                Plog.e("心跳：" + heartTimeAbox);
                if (heartTimeAbox != 0) {
                    SpUtil.writeInt(Const.HEART_TIME_A, heartTimeAbox);
                }
                int heartTimeCbox = Integer.parseInt(Objects.requireNonNull(pro.get("heartTimeC")).toString());
                Plog.e("关机心跳：" + heartTimeCbox);
                if (heartTimeCbox != 0) {
                    SpUtil.writeInt(Const.HEART_TIME_C, heartTimeCbox);
                }
                int baudrate = Integer.parseInt(Objects.requireNonNull(pro.get("baudrate")).toString());
                Plog.e("波特率：" + baudrate);
                if (baudrate != 0) {
                    SpUtil.writeInt(Const.BAUDRATE, baudrate);
                }
                int downTime = Integer.parseInt(Objects.requireNonNull(pro.get("downTime")).toString());
                Plog.e("关机时间：" + downTime);
                if (downTime != 0) {
                    SpUtil.writeInt(Const.DOWN_TIME, downTime);
                }
                in.close();
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
            }
        } else {
            Plog.e("无配置文件!");
            Toast.makeText(getApplicationContext(), "无配置文件,请拷贝文件到本地根目录。", Toast.LENGTH_LONG).show();
        }
    }
}
