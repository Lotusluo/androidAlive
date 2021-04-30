package com.smartwasp.assistant.daemonservice.util;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

import com.smartwasp.assistant.daemonservice.receiver.SwAdminReceiver;

import java.lang.reflect.Method;

public class DeviceAdminUtil {
    private static DeviceAdminUtil instance;
    private DevicePolicyManager devicePolicyManager;
    private ComponentName componentName;
    private Context mContext;
    public static synchronized DeviceAdminUtil getInstance() {
        if (instance == null) {
            instance = new DeviceAdminUtil();
        }
        return instance;
    }

    public void init(Context context) {
        mContext = context.getApplicationContext();
        devicePolicyManager = (DevicePolicyManager) mContext.getSystemService(Context.DEVICE_POLICY_SERVICE);
        componentName = new ComponentName(mContext, SwAdminReceiver.class);
        setDeviceAdminActive(true);
    }

    private void setDeviceAdminActive(boolean active) {
        try {
            if (devicePolicyManager != null && componentName != null) {
                Method setActiveAdmin = devicePolicyManager.getClass().getDeclaredMethod("setActiveAdmin", ComponentName.class, boolean.class);
                setActiveAdmin.invoke(devicePolicyManager, componentName, true);
            }
        } catch (Exception e) {
            Log.e("DeviceAdminUtil",e.toString());
        }
    }
}
