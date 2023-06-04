package sh.tablet.bgclipboard;

import static de.robv.android.xposed.XposedHelpers.*;

import android.util.Log;
import java.lang.String;
import java.lang.reflect.Method;

import de.robv.android.xposed.*;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class BGClipboard implements IXposedHookLoadPackage {
    static String logtag = "===tablettttttt===";
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if(!lpparam.packageName.equals("android")) {
            return;
        }
        Log.d(BGClipboard.logtag, "loading " + lpparam.packageName);
        Log.d(BGClipboard.logtag, "proc = " + lpparam.processName);
        XposedBridge.log(BGClipboard.logtag + " - Loaded app: " + lpparam.packageName);
        Class<?> cb = findClass("com.android.server.clipboard.ClipboardService",lpparam.classLoader);
        Method[] m = cb.getDeclaredMethods();
        for (Method method : m) {
            Log.d(BGClipboard.logtag, "... " + method.getName());
            if (method.getName().contains("Allowed") && !method.getName().contains("$")) {
                XposedBridge.hookMethod(method, XC_MethodReplacement.returnConstant(10000, true));
                Log.d(BGClipboard.logtag, "found it!!!!! " + method.getName());
            }
        }
    }
}
