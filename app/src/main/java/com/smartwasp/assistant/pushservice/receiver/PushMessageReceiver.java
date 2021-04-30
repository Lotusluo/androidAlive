package com.smartwasp.assistant.pushservice.receiver;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.smartwasp.assistant.pushservice.App;
import com.smartwasp.assistant.pushservice.service.SwPushService;
import com.smartwasp.assistant.pushservice.util.AppExecutors;
import com.smartwasp.assistant.pushservice.util.DeviceUtils;

import java.util.HashSet;
import java.util.Set;

import cn.jpush.android.api.CmdMessage;
import cn.jpush.android.api.CustomMessage;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.JPushMessage;
import cn.jpush.android.api.NotificationMessage;
import cn.jpush.android.service.JPushMessageReceiver;

public class PushMessageReceiver extends JPushMessageReceiver{
    private static final String TAG = "PushMessageReceiver";
    @Override
    public void onMessage(Context context, CustomMessage customMessage) {
        Intent intent = new Intent(SwPushService.BROADCAST_CMD);
        intent.putExtra("data",customMessage.message);
        context.sendBroadcast(intent);
    }

    @Override
    public void onNotifyMessageOpened(Context context, NotificationMessage message) {
        Log.d(TAG,"[onNotifyMessageOpened] "+message);
    }

    @Override
    public void onMultiActionClicked(Context context, Intent intent) {
    }

    @Override
    public void onNotifyMessageArrived(Context context, NotificationMessage message) {
    }

    @Override
    public void onNotifyMessageDismiss(Context context, NotificationMessage message) {
        Log.d(TAG,"[onNotifyMessageDismiss] "+message);
    }

    @Override
    public void onRegister(Context context, String registrationId) {
        Log.d(TAG,"[onRegister] "+registrationId);
    }

    @Override
    public void onConnected(Context context, boolean isConnected) {
        if(isConnected){
            Intent intent = new Intent(SwPushService.BROADCAST_CONNECTED);
            context.sendBroadcast(intent);
        }
    }

    @Override
    public void onCommandResult(Context context, CmdMessage cmdMessage) {
        Log.d(TAG,"[onCommandResult] "+cmdMessage);
    }

    private int errTag = 0;
    @Override
    public void onTagOperatorResult(Context context,JPushMessage jPushMessage) {
        super.onTagOperatorResult(context, jPushMessage);
        Log.d(TAG,"onTagOperatorResult:" + jPushMessage.toString());
        if(jPushMessage.getErrorCode() == 0){
            errTag = 0;
            return;
        }
        if(jPushMessage.getErrorCode() == 6002 && errTag < 3){
            errTag++;
            Set<String> sets = new HashSet();
            sets.add("69");
            JPushInterface.setTags(App._thiz,101, sets);
        }
    }
    @Override
    public void onCheckTagOperatorResult(Context context,JPushMessage jPushMessage){
        super.onCheckTagOperatorResult(context, jPushMessage);
    }

    private int errTag1 = 0;
    @Override
    public void onAliasOperatorResult(Context context, JPushMessage jPushMessage) {
        super.onAliasOperatorResult(context, jPushMessage);
        Log.d(TAG,"onAliasOperatorResult:" + jPushMessage.toString());
        if(jPushMessage.getErrorCode() == 0){
            errTag1 = 0;
            return;
        }
        if(jPushMessage.getErrorCode() == 6002 && errTag1 < 3){
            errTag1++;
            JPushInterface.setAlias(App._thiz,102, DeviceUtils.INSTANCE.getDeviceId());
        }
    }

    @Override
    public void onMobileNumberOperatorResult(Context context, JPushMessage jPushMessage) {
        super.onMobileNumberOperatorResult(context, jPushMessage);
    }

    @Override
    public void onNotificationSettingsCheck(Context context, boolean isOn, int source) {
        super.onNotificationSettingsCheck(context, isOn, source);
        Log.d(TAG,"[onNotificationSettingsCheck] isOn:"+isOn+",source:"+source);
    }
    
}
