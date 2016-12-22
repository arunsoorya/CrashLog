package com.arunsoorya.crashlogger;

import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by T10778 on 27/01/2016.
 */
public class ExceptionHandler implements Thread.UncaughtExceptionHandler {

    ExceptionLogInterface exceptionLogInterface;
    public ExceptionHandler(ExceptionLogInterface ExceptionLogInterf){
        this.exceptionLogInterface = ExceptionLogInterf;
    }
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {

        try {
            Log.e("Alert","Lets See if it Works !!!");
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            ex.printStackTrace(printWriter);
            exceptionLogInterface.onExceptionOccurred(stringWriter.toString());
            System.exit(0);

        } catch (Exception e) {
        }
    }
}
