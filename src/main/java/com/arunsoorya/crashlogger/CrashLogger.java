package com.arunsoorya.crashlogger;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by T10778 on 27/01/2016.
 */
public class CrashLogger implements ExceptionLogInterface {

    private static String fileName;
    private static String filePath;
    private static CrashLogger crashLogger;
    private ExceptionHandler exceptionHandler;
    private static Context context;
    private PackageInfo packageInfo;


    public static CrashLogger getInstance(Context con) {
        context = con;
        if (crashLogger == null) {
            crashLogger = new CrashLogger();
        }
        return crashLogger;
    }


    public void initCrashLogger() {
        try {
            filePath = context.getFilesDir().getAbsolutePath().concat("/log");
            PackageManager localPackageManager = context.getPackageManager();
            packageInfo = localPackageManager.getPackageInfo(context.getPackageName(), 0);
            checkFileLogVersion();
        } catch (Exception e) {
            e.printStackTrace();
        }

        exceptionHandler = new ExceptionHandler(this);
//        try {
//            new Thread() {
//                @Override
//                public void run() {

        Thread.setDefaultUncaughtExceptionHandler(exceptionHandler);
//                }
//            }.start();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    }


    @Override
    public void onExceptionOccurred(String errorMessage) {
        try {
            String jsonObjStr = createJsonObject(errorMessage);
            Log.e("errorLog", errorMessage);
            checkLogIntervalAndSent(jsonObjStr);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void checkLogIntervalAndSent(String errorMessage) throws IOException {

        Log.i("errorLog", errorMessage);
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(context);
        boolean instantLog = SP.getBoolean(LoggerConstants.LOG_INSTANT, false);
        if (instantLog) {
            sentLogToServer(errorMessage);
        } else {
            saveCrashLog(errorMessage);
        }

    }

    private void sentLogToServer(String errorMessage) {

        Log.i("errorLog2", "" + errorMessage);

    }

    public void sentBulkLogToServer() {
        String log = null;
        try {
            log = readFromCrashLog();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sentLogToServer(log);
    }

    private void checkFileLogVersion() throws IOException {
        fileName = "0";
        String version = "" + packageInfo.versionCode;
        if (!TextUtils.isEmpty(version))
            fileName = version;

        File fileRoot = new File(filePath);
        if (!fileRoot.isDirectory())
            fileRoot.mkdir();

        //means this is a new version or first time install
        File file = new File(fileRoot.getAbsolutePath(), fileName);
        if (!file.exists()) {
            File fileDir = new File(fileRoot.getAbsolutePath());
            for (File f : fileDir.listFiles())
                f.delete();
            file.createNewFile();
        }
    }

    private String createJsonObject(String errorLog) throws JSONException, PackageManager.NameNotFoundException {


        JSONObject jsonObject = new JSONObject();
        jsonObject.put("error", errorLog);
        jsonObject.put("packageName", packageInfo.packageName);
        jsonObject.put("versionCode", packageInfo.versionCode);
        jsonObject.put("versionName", packageInfo.versionName);
        jsonObject.put("osVersion", Build.VERSION.RELEASE);
        jsonObject.put("deviceBrand", Build.BRAND);
        jsonObject.put("deviceModel", Build.MODEL);
        jsonObject.put("formattedDate", new SimpleDateFormat().format(new Date(System.currentTimeMillis())));
        jsonObject.put("time", System.currentTimeMillis());
        jsonObject.put("timeZone", TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT));

        return jsonObject.toString();
    }

    private void saveCrashLog(String crashContent) throws IOException {

        File file = new File(filePath, fileName);
        if (file.exists()) {

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file.getAbsolutePath(), true));
            bufferedWriter.write(crashContent);
            bufferedWriter.newLine();
            bufferedWriter.close();
            Log.i("log_write-inside", crashContent);
        }

    }

    private void clearCacheAfterUpload() throws IOException {
        File file = new File(filePath);
        if (file.exists())
            file.delete();
    }

    private String readFromCrashLog() throws IOException, JSONException {
        File file = new File(filePath, fileName);
        if (!file.exists())
            return null;
        BufferedReader bufferedInputStream = new BufferedReader(new FileReader(file.getAbsolutePath()));

        StringBuilder stringBuilder = new StringBuilder();
        String line;
        long selectedPeriod = getUploadInterval();
        while ((line = bufferedInputStream.readLine()) != null) {
            if (selectedPeriod == 0) {
                stringBuilder.append(line);
                continue;
            }
            JSONObject jsonObject = new JSONObject(line);
            long logTime = jsonObject.getLong("time");
            if (logTime >= selectedPeriod)
                stringBuilder.append(line);
        }
        bufferedInputStream.close();
        return stringBuilder.toString();
    }

    private long getUploadInterval() {
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(context);
        String logIntervalStr = SP.getString(LoggerConstants.LOG_INTERVAL, String.valueOf(LogIntervalType.LAST_HOUR.ordinal()));

        int interval = Integer.parseInt(logIntervalStr);
        long logInterval = 60 * 60 * 1000;
        if (interval == LogIntervalType.LAST_HOUR.ordinal()) {
            logInterval = System.currentTimeMillis() - logInterval;
        } else if (interval == LogIntervalType.TODAY.ordinal()) {
            logInterval = System.currentTimeMillis() - logInterval * 24;
        } else if (interval == LogIntervalType.LAST_2_DAYS.ordinal()) {
            logInterval = System.currentTimeMillis() - logInterval * 24 * 2;
        } else if (interval == LogIntervalType.lAST_WEEK.ordinal()) {
            logInterval = System.currentTimeMillis() - logInterval * 24 * 7;
        } else if (interval == LogIntervalType.ALL.ordinal()) {
            logInterval = 0;
        }
        return logInterval;
    }
}
