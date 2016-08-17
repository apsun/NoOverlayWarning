package com.crossbowffs.nooverlaywarning;

import android.util.Log;
import android.view.MotionEvent;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Hook implements IXposedHookLoadPackage {
    private static final String TAG = "NoOverlayWarning";
    private static final String[] INSTALLER_PACKAGE_NAMES = {
        "com.android.packageinstaller",        // Pre-Marshmallow
        "com.google.android.packageinstaller", // Marshmallow
        "com.mokee.packageinstaller"           // MoKee
    };

    private static boolean arrayContains(String[] array, String value) {
        for (String test : array) {
            if (test.equals(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!arrayContains(INSTALLER_PACKAGE_NAMES, lpparam.packageName)) {
            return;
        }

        XposedHelpers.findAndHookMethod(MotionEvent.class, "getFlags", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                int flags = (Integer)param.getResult();
                if ((flags & MotionEvent.FLAG_WINDOW_IS_OBSCURED) != 0) {
                    flags &= ~MotionEvent.FLAG_WINDOW_IS_OBSCURED;
                    Log.i(TAG, "Cleared FLAG_WINDOW_IS_OBSCURED flag");
                }
                param.setResult(flags);
            }
        });

        Log.i(TAG, "NoOverlayWarning successfully initialized!");
    }
}
