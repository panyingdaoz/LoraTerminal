package com.kingbird.loraterminal.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.kingbird.loraterminal.R;
import com.kingbird.loraterminal.adapter.CboxAdapter;
import com.kingbird.loraterminal.entity.CboxId;
import com.kingbird.loraterminal.entity.CboxStatuEntity;
import com.kingbird.loraterminal.manager.CustomActivityManager;
import com.kingbird.loraterminal.manager.ExecutorServiceManager;
import com.kingbird.loraterminal.manager.ProtocolDao;
import com.kingbird.loraterminal.manager.SocketManager;
import com.kingbird.loraterminal.manager.ThreadManager;
import com.kingbird.loraterminal.service.MonitorService;
import com.kingbird.loraterminal.service.StartAppService;
import com.kingbird.loraterminal.utils.BaseUtil;
import com.kingbird.loraterminal.utils.Const;
import com.kingbird.loraterminal.utils.SerialPortUtil;
import com.kingbird.loraterminal.utils.SpUtil;

import org.litepal.LitePal;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.kingbird.loraterminal.utils.BaseUtil.cboxIdQuery;
import static com.kingbird.loraterminal.utils.BaseUtil.localDataSave;
import static com.kingbird.loraterminal.utils.Config.CONSTANT_TEN;
import static com.kingbird.loraterminal.utils.Plog.e;

/**
 * @author Pan Yingdao
 */
public class MainActivity extends AppCompatActivity {

    private static final String MY_BROADCAST_TAG = "tcpServerReceiver";
    MonitorService monitorService;
    private TextView mTextView, mHeartBeat,mTriggerData;
    private Button mCertification;
    private IntentFilter filter;
    @SuppressLint("StaticFieldLeak")
    public static Context context;
    private final MyHandler myHandler = new MyHandler(this);
    private MyBroadcastReceiver myBroadcastReceiver = new MyBroadcastReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        if (BaseUtil.isFirstStart(this)) {
            CboxId id = new CboxId();
            id.setCboxId(1);
            id.save();
            e("第一次安装");
        }

        mTextView = findViewById(R.id.textView);
        mHeartBeat = findViewById(R.id.Heartbeat);
        mTriggerData = findViewById(R.id.Triggerdata);
        mCertification = findViewById(R.id.Certification);

        Intent intent = new Intent(MainActivity.this, MonitorService.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
        ThreadManager.getInstance().doExecute(() -> startService(new Intent(MainActivity.this, StartAppService.class)));
        bindReceiver();

        ExecutorServiceManager.getInstance().scheduleAtFixedRate(() -> {
            e("循环扫描");
            updateCboxState();
        }, 30, 30, TimeUnit.SECONDS);

    }

    public void addLocal(View view) {
        String requestId = UUID.randomUUID().toString().replace("-", "");
        String clientId = UUID.randomUUID().toString().replace("-", "");
        e("手动添加本地数据");
        // 保存发送的当前数据
        localDataSave(2, "402880e36bba49c4016bba5f31390029", "BUSY", new Date(), 8, requestId, clientId);
    }

    public void addLocal2(View view) {
        e("手动发送C-1");
        SerialPortUtil.serialPortParse(ProtocolDao.test(1, 2, 8));
    }

    public void addLocal3(View view) {
        e("手动发送C-2");
        SerialPortUtil.serialPortParse(ProtocolDao.test(2, 2, 20));
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String mAction = intent.getAction();
            if (mAction != null) {
                if (MY_BROADCAST_TAG.equals(mAction)) {
                    String msg = intent.getStringExtra(MY_BROADCAST_TAG);
                    e("传过来的值", msg);
                    Message message = Message.obtain();
                    assert msg != null;
                    message.what = Integer.parseInt(msg);
                    message.obj = msg;
                    myHandler.sendMessage(message);
                }
            }
        }
    }

    @SuppressLint("HandlerLeak")
    private class MyHandler extends Handler {

        private WeakReference<MainActivity> mActivity;

        private MyHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            MainActivity activity = mActivity.get();
            if (activity != null) {
                int msgWhat = msg.what;
                if (msgWhat < CONSTANT_TEN) {
                    handleMessage2(msg);
                }
            }
        }

        private void handleMessage2(Message msg) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
            switch (msg.what) {
                case 0:
                    runOnUiThread(MainActivity.this::updateCboxState);
                    break;
                case 1:
                    String recData = SpUtil.readString(Const.RECEIVE_CBOX);
//                    Plog.e("要展示的数据", recData);
                    if (!TextUtils.isEmpty(recData)) {
                        String id = SpUtil.readString(Const.CBOXID);
                        String text = sdf.format(new Date()) + " 接收到的C端 = " + id + "的数据 =" + recData;
                        runOnUiThread(() -> mTextView.setText(text));
                    }
                    break;
                case 2:
                    String heartbeatData = SpUtil.readString(Const.HEARTBEAT_DATA);
//                    Plog.e("要展示的数据", heartbeatData);
                    if (!TextUtils.isEmpty(heartbeatData)) {
                        String id = SpUtil.readString(Const.CBOXID);
                        String text = sdf.format(new Date()) + " 接收到的C端 = " + id + "的心跳数据 =" + heartbeatData;
                        runOnUiThread(() -> mHeartBeat.setText(text));
                    }
                    break;
                case 3:
                    String sendData = SpUtil.readString(Const.TRIGGER_DATA);
                    String text = sdf.format(new Date()) + " 发送服务器数据：" + sendData;
                    runOnUiThread(() -> mTriggerData.setText(text));
                    break;
                case 4:
                    boolean result = SpUtil.readBoolean(Const.CERTIFICATION);
                    String str;
                    if (result) {
                        str = "成功！";
                    } else {
                        str = "失败！";
                    }
                    String text4 = sdf.format(new Date()) + " 认证结果：" + str;
                    mCertification.setText(text4);
                    break;
                default:
            }
        }

    }

    /**
     * 更新Cbox与Abox连接状态
     */
    private void updateCboxState() {
        ArrayList<CboxStatuEntity> list = new ArrayList<>();
        List<CboxId> cboxIdList = LitePal.findAll(CboxId.class);
        for (CboxId cboxIdLists : cboxIdList) {
            int id = cboxIdLists.getCboxId();
            List<CboxId> getCboxList = cboxIdQuery("onLineStatus", Integer.toString(id));
            for (CboxId getCboxLists : getCboxList) {
                String status = null;
                int state = getCboxLists.getOnLineStatus();
                if (state == 1) {
                    status = "离线";
                } else if (state == 2) {
                    status = "在线";
                }
                CboxStatuEntity cboxList = new CboxStatuEntity(Integer.toString(id), status);
                list.add(cboxList);
            }
            e("CboxStatuEntity", JSON.toJSON(list));
        }
        try {
            runOnUiThread(() -> {
                CboxAdapter cboxAdapter = new CboxAdapter(MainActivity.this, R.layout.listitem, list);
                ListView listView = findViewById(R.id.lv_state);
                listView.setAdapter(cboxAdapter);
            });
        } catch (NullPointerException e) {
            e.printStackTrace();
            e("异常");
        }
    }

    /**
     * 广播绑定
     */
    private void bindReceiver() {
        filter = new IntentFilter(MY_BROADCAST_TAG);
        filter.addAction(MY_BROADCAST_TAG);
    }

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            monitorService = ((MonitorService.ServiceBinder) iBinder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            monitorService = null;
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        CustomActivityManager.getInstance().setTopActivity(this);
        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(myBroadcastReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        e("关闭串口");
        SerialPortUtil.close();
        this.unbindService(conn);
        SocketManager.getInstance().close();
        SocketManager.getInstance().closeJt();
        LocalBroadcastManager.getInstance(MainActivity.this).unregisterReceiver(myBroadcastReceiver);
        System.exit(0);
    }

}
