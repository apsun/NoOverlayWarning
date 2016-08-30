package com.crossbowffs.nooverlaywarning;

import android.util.Log;
import android.view.MotionEvent;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Hook implements IXposedHookLoadPackage {
    private static final String TAG = "NoOverlayWarning";

    // Whether to hook all packages with name ending in ".packageinstaller",
    // since people enjoy changing the package name for some reason.
    private static final boolean HOOK_ALL_PACKAGE_INSTALLERS = true;

    // Known package installer package name whitelist, used when
    // HOOK_ALL_PACKAGE_INSTALLERS is false.
    private static final String[] KNOWN_PACKAGE_INSTALLERS = {
        "com.android.packageinstaller",         // Pre-Marshmallow
        "com.google.android.packageinstaller",  // Marshmallow
        "com.samsung.android.packageinstaller", // Samsung
        "com.mokee.packageinstaller"            // MoKee
    };

    // New flag secretly added in Android 6.0.1 it seems
    // https://android.googlesource.com/platform/frameworks/native/+/03a53d1c7765eeb3af0bc34c3dff02ada1953fbf%5E!/
    private static final int FLAG_WINDOW_IS_PARTIALLY_OBSCURED = 0x2;

    private static boolean arrayContains(String[] array, String value) {
        for (String test : array) {
            if (test.equals(value)) {
                return true;
            }
        }
        return false;
    }

    private static boolean shouldHook(String packageName) {
        if (arrayContains(KNOWN_PACKAGE_INSTALLERS, packageName)) {
            return true;
        }
        if (packageName.endsWith(".packageinstaller")) {
            Log.w(TAG, "Unknown package installer name: " + packageName);
            return HOOK_ALL_PACKAGE_INSTALLERS;
        }
        return false;
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!shouldHook(lpparam.packageName)) {
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

                if ((flags & FLAG_WINDOW_IS_PARTIALLY_OBSCURED) != 0) {
                    flags &= ~FLAG_WINDOW_IS_PARTIALLY_OBSCURED;
                    Log.i(TAG, "Cleared FLAG_WINDOW_IS_PARTIALLY_OBSCURED flag");
                }
                param.setResult(flags);
            }
        });

        Log.i(TAG, "NoOverlayWarning initialized in package: " + lpparam.packageName);
    }
}
