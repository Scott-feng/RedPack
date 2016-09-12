package com.test.redpack;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.PendingIntent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

/**
 * Created by 90969 on 2016/09/04/0004.
 */
public class RedPack extends AccessibilityService{
    //flag judge weather clicked
    static boolean isReceive = false;
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            handleNotification(event);
            Log.d("Notification", "Notification");
        } else if
                (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                        || eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED){
            String className = event.getClassName().toString();
            Log.d("classname", className);
            //simplify the className,while can add "com.adnroid.ListView"
            if (className.equals("com.tencent.mm.ui.LauncherUI")) {

                //after notification,enter chat Message list,click [微信红包"] to enter signal chat message
                AccessibilityNodeInfo info = event.getSource();
                List<AccessibilityNodeInfo> chat=info.findAccessibilityNodeInfosByText("[微信红包]");
                if (!chat.isEmpty()) {
                    int total = chat.size();
                    Log.d("chat_Size", String.valueOf(total));
                    //find first "[微信红包]"
                    AccessibilityNodeInfo node = chat.get(0);
                    if (node.isClickable() && !isReceive) {
                        node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                }
                getPacket(event);
            } else if (className.equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI")) {
                Log.d("UiLuck", "callOpenPack");
                openPacket(event);
            } else if (className.equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI")) {
                //return by hand,no auto back
//                performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
            }
        }
    }

    private void openPacket(AccessibilityEvent event) {
        AccessibilityNodeInfo nodeInfo = event.getSource();
        Log.d("openPacket","in openPacket");
        if (nodeInfo !=null){
            List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("给你发了一个红包");
            AccessibilityNodeInfo button =list.get(0).getParent();
            //find "開"button
            if (button.getChild(3).isClickable()) {
                button.getChild(3).performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }
    }

    private void getPacket(AccessibilityEvent event) {
        AccessibilityNodeInfo rootNode = event.getSource();
        Log.d("rootName",String.valueOf(rootNode.getText()));
        List<AccessibilityNodeInfo> list = rootNode.findAccessibilityNodeInfosByText("领取红包");
        if (!list.isEmpty()) {
            int total = list.size();
            Log.d("total", String.valueOf(total));
            //find last "领取红包" text
            AccessibilityNodeInfo parent = list.get(total - 1).getParent();
            if (parent.isClickable() && !isReceive) {
                parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                isReceive = true;
            } else {
                isReceive = false;
            }
        }

    }


    private void handleNotification(AccessibilityEvent event) {
        List<CharSequence> texts=event.getText();
        if (!texts.isEmpty()){
            for (CharSequence text:texts){
                String content=text.toString();
                if (content.contains("[微信红包]")){
                    if (event.getParcelableData() !=null && event.getParcelableData() instanceof Notification){
                        Notification notification=(Notification) event.getParcelableData();
                        PendingIntent pendingIntent=notification.contentIntent;
                        try {
                            pendingIntent.send();
                        }  catch (PendingIntent.CanceledException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

    }

    @Override
    public void onInterrupt(){

    }
}
