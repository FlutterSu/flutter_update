package com.bond520.plugins.flutter_updater;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.IPackageInstallObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.RemoteException;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.ActivityResultListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.content.pm.PackageInstaller;
import android.util.Log;

//import java.io.File;
//import java.lang.reflect.Method;
//
//import android.app.Activity;
//import android.content.pm.PackageManager;

//import android.net.Uri;


public final class FlutterUpdatePlugin implements FlutterPlugin, ActivityAware, MethodCallHandler {
  private ActivityPluginBinding activityBinding;
  private MethodChannel channel;
  private final FlutterUpdatePlugin.Callback listener = new FlutterUpdatePlugin.Callback();
  private MethodCall callBack;
  private Result resultCallBack;
  private static final int REQ_CODE = 10671;

    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    this.channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "flutter_updater");
    MethodChannel channel = this.channel;
      channel.setMethodCallHandler(this);

  }

  public void onDetachedFromEngine(@NonNull FlutterPluginBinding plugin) {
    MethodChannel channel = this.channel;
    if (channel != null) {
      channel.setMethodCallHandler(null);
    }

    this.callBack = (MethodCall)null;
    this.resultCallBack = (Result)null;
    this.channel = (MethodChannel)null;
  }

  public void onDetachedFromActivityForConfigChanges() {
    ActivityPluginBinding activityBinding = this.activityBinding;
    if (activityBinding != null) {
      activityBinding.removeActivityResultListener((ActivityResultListener)this.listener);
    }

    this.callBack = (MethodCall)null;
    this.resultCallBack = (Result)null;
    this.activityBinding = (ActivityPluginBinding)null;
  }

  public void onAttachedToActivity(@NonNull ActivityPluginBinding plugin) {
    this.activityBinding = plugin;
    ActivityPluginBinding activityBinding = this.activityBinding;
      activityBinding.addActivityResultListener((ActivityResultListener)this.listener);
  }

  public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding plugin) {
    this.onAttachedToActivity(plugin);
  }

  public void onDetachedFromActivity() {
    this.onDetachedFromActivityForConfigChanges();
  }


  private Map<String,Object> _toMap(String reason,int flag){
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("reason",reason);
    map.put("flag",flag);
    return map;
  }

  Activity activity;

  public void onMethodCall(@NonNull MethodCall call, @NonNull final Result result) {
    ActivityPluginBinding activityBinding = this.activityBinding;
    if (activityBinding != null) {
      activity = activityBinding.getActivity();
        String method = call.method;
        if (method != null) {
          if (method.equals("install")) {
                String path = (String)call.argument("path");
                if (path == null) {
                  if(channel==null)return;
                  channel.invokeMethod("failure", _toMap("文件为空",1));
                  result.success("文件为空");
                  return;
                }
                if (VERSION.SDK_INT >= 26) {
                  boolean canRequestPackageInstalls = activity.getPackageManager().canRequestPackageInstalls();
                  if (canRequestPackageInstalls) {
                    this.installFromPath(activity, path, result);
                  } else {
                    this.resultCallBack = result;
                    this.callBack = call;
                    Uri packageURI = Uri.parse("package:" + activity.getPackageName());
                    Intent intent = new Intent("android.settings.MANAGE_UNKNOWN_APP_SOURCES", packageURI);
                    activity.startActivityForResult(intent, REQ_CODE);
                  }
                } else {
                  this.installFromPath(activity, path, result);
                }
          } else if (method.equals("install2")) {
            String path = (String) call.argument("path");
            if (path == null) {
              if(channel==null)return;
              channel.invokeMethod("failure", _toMap("文件为空",1));
              result.success("文件为空");
              return;
            }

//            Class<?>[] installTypes = {
//                    Uri.class, IPackageInstallObserver.class, int.class,
//                    String.class,
//            };
//
//            final Uri apkUri = Uri.fromFile(new File(path));
////            final InstallObserver  installObserver = new InstallObserver();
//
//            IPackageInstallObserver.Stub installObserver = new IPackageInstallObserver.Stub() {
//              @Override
//              public void packageInstalled(String packageName, int returnCode) throws RemoteException {
//                Log.e(TAG, "packageInstalled");
//                // forward this internal callback to our callback
////                try {
////                  callback.handleResult(packageName, returnCode);
////                } catch (RemoteException e1) {
////                  Log.e(TAG, "RemoteException", e1);
////                }
//              }
//            };

//            PackageManager pm = activity.getPackageManager();
//            Method installMethod = null;
//            try {
//              installMethod = pm.getClass().getMethod("installPackage", installTypes);
//            } catch (NoSuchMethodException e) {
//              e.printStackTrace();
//            }
//
//            try {
//              installMethod.invoke(pm, apkUri, installObserver, 0, activity.getPackageName());
//            } catch (IllegalAccessException e) {
//              e.printStackTrace();
//            } catch (InvocationTargetException e) {
//              e.printStackTrace();
//            }

//
//
//
//              PackageManager packageManager = activity.getPackageManager();
//              packageManager.installPackage(apkUri, new InstallObserver(), 0, activity.getPackageName());
//            PackageInstaller packageInstaller = new PackageInstaller();
//            packageInstaller.createSession(new SessionParams());

            jIntentActionInstallApk(activity, path);
//            try {
//
//
//
////              final String installerPackageName = activity.getPackageName();
////
//
//
//                final PackageInfo info = packageManager.getPackageInfo(activity.getPackageName(), 0);
//                int versionCode = info.versionCode;
//                result.success("versionCode" + versionCode);
//
////                _registrar.context().getPackageManager().installPackage(apkUri, installObserver, 0, installerPackageName);
//            } catch (Exception e) {
//              e.printStackTrace();
//            }
          }
        }
    }
  }

  private static final String TAG = "TasksSample";
  public static final String ACTION_INSTALL_COMPLETE = "cm.android.intent.action.INSTALL_COMPLETE";

  public void jIntentActionInstallApk(Activity activity, final String filename)
  {
    PackageInstaller.Session session = null;
    try {
      Log.i(TAG, "jIntentActionInstallApk " + filename);

      if(VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        //as PackageInstaller was added in API 21, let's use the old way of doing it prior to 21
        Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
        Uri apkUri = Uri.parse(filename);
        Context context = this.activity;
//        ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo();
        intent.setData(apkUri);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);


         activity.grantUriPermission(activity.getPackageName() + ".FileProvider", apkUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
         activity.grantUriPermission(activity.getPackageName() + ".FileProvider", apkUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
         activity.grantUriPermission(activity.getPackageName() + ".FileProvider", apkUri, Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, false);
        intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
        intent.putExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME,
                context.getPackageName());
        this.activity.startActivity(intent);
      } else  {
        // API level 21 or higher, we need to use PackageInstaller
        PackageInstaller packageInstaller = this.activity.getPackageManager().getPackageInstaller();
        Log.i(TAG, "jIntentActionInstallApk - got packageInstaller");
        PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(
                PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        Log.i(TAG, "jIntentActionInstallApk - set SessionParams");
        int sessionId = packageInstaller.createSession(params);
        session = packageInstaller.openSession(sessionId);
        Log.i(TAG, "jIntentActionInstallApk - session opened");

        // Create an install status receiver.
        Context context = this.activity;
          File file = new File(filename);

          Intent intent = new Intent("android.intent.action.VIEW");
          if (VERSION.SDK_INT >= Build.VERSION_CODES.N) {
              Uri apkUri = FileProvider.getUriForFile((Context)activity, activity.getPackageName() + ".FileProvider", file);
              intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
              activity.grantUriPermission(activity.getPackageName() + ".FileProvider", apkUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
              activity.grantUriPermission(activity.getPackageName() + ".FileProvider", apkUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
              activity.grantUriPermission(activity.getPackageName() + ".FileProvider", apkUri, Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
              intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
              intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
              intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
              intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
          } else {
              intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK);
              intent.setDataAndType(Uri.parse("file://" + filename), "application/vnd.android.package-archive");
          }

        addApkToInstallSession(context, filename, session);
        Log.i(TAG, "jIntentActionInstallApk - apk added to session");

//        Intent intent = new Intent(context, activity.getClass());
//        intent.setAction(PACKAGE_INSTALLED_ACTION);

//        Intent intent = new Intent("android.intent.action.VIEW");
//
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        IntentSender statusReceiver = pendingIntent.getIntentSender();
        // Commit the session (this will start the installation workflow).
        session.commit(statusReceiver);
//        session.commit(new IntentSender(ReflectionHelpers.createNullProxy(IntentSender.class)));

//        session.commit(intent);
//        session.commit(createIntentSender(context, sessionId));

        Log.i(TAG, "jIntentActionInstallApk - commited");
      }
    } catch (IOException e) {
      Log.i(TAG, "Couldn't install package" + e.getMessage());

      throw new RuntimeException("Couldn't install package", e);
    } catch (RuntimeException e) {
      Log.i(TAG, "RuntimeException" + e);

      if (session != null) {
          if (VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
              session.abandon();
          }
      }
      throw e;
    }
  }

  private static IntentSender createIntentSender(Context context, int sessionId) {
    PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            sessionId,
            new Intent(ACTION_INSTALL_COMPLETE),
            0);
    return pendingIntent.getIntentSender();
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  private static void addApkToInstallSession(Context context, String filename, PackageInstaller.Session session)
  {
    Log.i(TAG, "addApkToInstallSession " + filename);
    // It's recommended to pass the file size to openWrite(). Otherwise installation may fail
    // if the disk is almost full.
    try {
      OutputStream packageInSession = session.openWrite("package", 0, -1);
      InputStream input;
      Uri uri = Uri.parse(filename);
      input = context.getContentResolver().openInputStream(uri);

      if(input != null) {
        Log.i(TAG, "input.available: " + input.available());
        byte[] buffer = new byte[16384];
        int n;
        while ((n = input.read(buffer)) >= 0) {
          packageInSession.write(buffer, 0, n);
        }
      }
      else {
        Log.i(TAG, "addApkToInstallSession failed");
        throw new IOException ("addApkToInstallSession");
      }
      packageInSession.close();  //need to close this stream
      input.close();             //need to close this stream
    }
    catch (Exception e) {
      Log.w(TAG, "addApkToInstallSession failed2 " + e.toString(), e);
    }
  }

  private void installFromPath(Activity activity, String path, Result result) {
    File file = new File(path);
    if (!this.isAPK(file)) {
      if(channel==null)return;
      channel.invokeMethod("failure", _toMap("安装包不合法",4));
      result.success("安装包不合法");
    } else {
      Intent intent = new Intent("android.intent.action.VIEW");
      if (VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Uri apkUri = FileProvider.getUriForFile((Context)activity, activity.getPackageName() + ".FileProvider", file);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.grantUriPermission(activity.getPackageName() + ".FileProvider", apkUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        activity.grantUriPermission(activity.getPackageName() + ".FileProvider", apkUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        activity.grantUriPermission(activity.getPackageName() + ".FileProvider", apkUri, Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
      } else {
        intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.parse("file://" + file), "application/vnd.android.package-archive");
      }
      if(channel==null)return;
      channel.invokeMethod("success","文件路径"+file.getAbsolutePath());
      result.success("开始安装"+file.getAbsolutePath());
      activity.startActivity(intent);
    }
  }

  public final boolean isAPK(@NonNull File file) {
    FileInputStream fis = null;
    ZipInputStream zipIs = null;
    ZipEntry zEntry = null;
    String dexFile = "classes.dex";
    String manifestFile = "AndroidManifest.xml";
    boolean hasDex = false;
    boolean hasManifest = false;
    try {
      fis = new FileInputStream(file);
      zipIs = new ZipInputStream(new BufferedInputStream(fis));

      zEntry=zipIs.getNextEntry();
      while (zEntry!=null&&(!hasDex || !hasManifest)){
        if(zEntry.getName().equalsIgnoreCase(dexFile)){
          hasDex = true;
        }else  if(zEntry.getName().equalsIgnoreCase(manifestFile)){
          hasManifest = true;
        }
        zEntry=zipIs.getNextEntry();
      }

      zipIs.close();
      fis.close();
      return true;
    } catch (FileNotFoundException var10) {
      return false;
    } catch (IOException var11) {
      return false;
    }
  }

  public final class Callback implements ActivityResultListener {
    public boolean onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
      if (REQ_CODE != requestCode) {
        return false;
      } else {
        Result result = FlutterUpdatePlugin.this.resultCallBack;
        if (result != null) {
          ActivityPluginBinding activityBinding = FlutterUpdatePlugin.this.activityBinding;
          MethodCall callBack = FlutterUpdatePlugin.this.callBack;
          if (resultCode == -1) {
            if(channel==null)return true;
            channel.invokeMethod("failure", _toMap("没有安装权限",2));
            result.success("没有安装权限");
          }
          if(activityBinding==null||callBack==null){
            if(channel==null)return true;
            channel.invokeMethod("failure", _toMap("页面已销毁",3));
            result.success("页面已销毁");
          }else  {
            String path = (String) callBack.argument("path");
            if(!TextUtils.isEmpty(path)){
              Activity activity = activityBinding.getActivity();
              installFromPath(activity, path, result);
            }else {
              if(channel==null)return true;
              channel.invokeMethod("failure", _toMap("文件为空",1));
              result.success("文件为空");
            }
          }
        }
        return  true;
      }
    }
  }

}