package com.crossbowffs.nooverlaywarning;

import android.util.Log;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Hook implements IXposedHookLoadPackage {
    private static final String TAG = "NoOverlayWarning";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!"com.google.android.packageinstaller".equals(lpparam.packageName)) {
            return;
        }

        XposedHelpers.findAndHookMethod(
            "com.android.packageinstaller.permission.ui.OverlayTouchActivity", lpparam.classLoader,
            "isObscuredTouch", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    Log.i(TAG, "isObscuredTouch() -> false");
                    param.setResult(false);
                }
            });

        Log.i(TAG, "NoOverlayWarning successfully initialized!");
    }
}
