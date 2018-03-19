package com.jerry_mar.picuz.utils;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Process;

public class PermissionUtils {
    public static void requestPermission(Activity context, String[] permissiones,
                             int requestCode, Callback c) {
        if (checkPermission(context, permissiones)) {
            c.onFinish(requestCode + Activity.RESULT_OK, null);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                context.requestPermissions(permissiones, requestCode);
            } else {
                c.onFinish(requestCode + Activity.RESULT_OK, null);
            }
        }
    }

    public static boolean checkPermission(Activity context, String[] permissiones) {
        boolean result = false;
        int size = permissiones.length;
        for (int i = 0; i < size; i++) {
            if (checkPermission(context, permissiones[i])) {
                result = true;
                break;
            }
        }
        return result;
    }

    public static boolean checkPermission(Activity context, String permission) {
        return context.checkPermission(permission, Process.myPid(),
                Process.myUid()) == PackageManager.PERMISSION_GRANTED;
    }

    public interface Callback {
        void onFinish(int code, Intent intent);
    }
}
