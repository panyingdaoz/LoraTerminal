//package com.kingbird.loraterminal.service;
//
//import android.accessibilityservice.AccessibilityService;
//import android.annotation.SuppressLint;
//import android.content.Intent;
//import android.view.accessibility.AccessibilityEvent;
//import android.view.accessibility.AccessibilityNodeInfo;
//
//import com.kingbird.advertisting.activity.HomePageActivity;
//import com.kingbird.advertisting.base.Base;
//import com.kingbird.advertisting.utils.Const;
//import com.kingbird.advertisting.utils.SpUtil;
//import com.socks.library.KLog;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import static com.kingbird.advertisting.utils.Config.FILE_SAVE_URL;
//
///**
// * 类具体作用
// *
// * @author 升级专用服务
// * @date 2018/10/16/016.
// */
//public class MyAccessibilityService extends AccessibilityService {
//
//    @SuppressLint("UseSparseArrays")
//    Map<Integer, Boolean> handledMap = new HashMap<>();
//    private Intent intent = new Intent(HomePageActivity.ACTION_SERVICE_STATE_CHANGE);
//
//    public MyAccessibilityService() {
//    }
//
//    @Override
//    public void onAccessibilityEvent(AccessibilityEvent event) {
//        //不写完整包名，是因为某些手机(如小米)安装器包名是自定义的
//        if (event == null || !event.getPackageName().toString()
//                .contains("packageinstaller")) {
//            return;
//        }
//        AccessibilityNodeInfo nodeInfo = event.getSource();
//        if (nodeInfo != null) {
//            int eventType = event.getEventType();
//            Plog.e("接收数据类型", eventType);
//            if (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ||
//                    eventType == AccessibilityEvent.TYPE_VIEW_HOVER_ENTER ||
//                    eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
//                Plog.e("nodeInfo", nodeInfo);
//                if (handledMap.get(event.getWindowId()) == null) {
//                    boolean handled = iterateNodesAndHandle(nodeInfo);
//                    if (handled) {
//                        handledMap.put(event.getWindowId(), true);
//                    }
//                }
//            }
//        }
//    }
//
//    private boolean iterateNodesAndHandle(AccessibilityNodeInfo nodeInfo) {
//        String button = "android.widget.Button", scrollView = "android.widget.ScrollView", textView = "android.widget.TextView",
//                install = "安装", install2 = "确认安装";
//        if (nodeInfo != null) {
//            int childCount = nodeInfo.getChildCount();
//            Plog.e("未知数据", nodeInfo.getClassName());
//            if (button.contentEquals(nodeInfo.getClassName())) {
//                String nodeContent = nodeInfo.getText().toString();
//                Plog.e("content is " + nodeContent);
//                if (install.equals(nodeContent)
//                        || install2.equals(nodeContent)
//                        || "确定".equals(nodeContent)
//                        || "重新安装".equals(nodeContent)
//                        || "打开".equals(nodeContent)) {
//                    Plog.e("Button-文字内容:" + nodeContent);
//                    nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
//                    return true;
//                }
//            } else if (scrollView.contentEquals(nodeInfo.getClassName())) {
//                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
//            } else if (textView.contentEquals(nodeInfo.getClassName())) {
//                CharSequence dgg = nodeInfo.getText();
//                if (dgg != null) {
//                    String nodeContent = dgg.toString();
//                    Plog.e("textView-文字内容 " + nodeContent);
//                }
//            }
//            for (int i = 0; i < childCount; i++) {
//                AccessibilityNodeInfo childNodeInfo = nodeInfo.getChild(i);
//                if (iterateNodesAndHandle(childNodeInfo)) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }
//
//    @Override
//    public void onInterrupt() {
//    }
//
//    @Override
//    protected void onServiceConnected() {
//        super.onServiceConnected();
//        sendAction(true);
//        Plog.e("打开");
//        String updateName = SpUtil.readString(Const.UPDATE_APP_NAME);
//        String filePath = FILE_SAVE_URL + updateName;
//        if (Base.fileIsExists(filePath)) {
//            Plog.e("开始安装");
//            MonitorService monitorService = new MonitorService();
//            monitorService.installApp(filePath, this);
//        }
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        sendAction(false);
//        Plog.e("关闭");
//    }
//
//    private void sendAction(boolean state) {
//        intent.putExtra("state", state);
//        sendBroadcast(intent);
//    }
//
//}
