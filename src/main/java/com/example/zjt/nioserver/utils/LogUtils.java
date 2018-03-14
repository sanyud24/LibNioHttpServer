package com.example.zjt.nioserver.utils;

import com.example.zjt.nioserver.constant.Constant;

import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by zjt on 18-3-4.
 */

public class LogUtils {
    private FileOutputStream fos;
    private static LogUtils instance = null;

    private LogUtils(){
        File file = new File(Constant.getInstance().getTempFilepath() + "LogInfo.txt");
        try {
            file.createNewFile();
            fos = new FileOutputStream(file);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void w(Object obj){
        if(instance == null){
            instance = new LogUtils();
        }
        try {
            instance.fos.write(obj == null ? "".getBytes() : obj.toString().getBytes());
            instance.fos.flush();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void destroy(){
        try {
            instance.fos.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
