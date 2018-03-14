package com.example.zjt.nioserver.constant;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.nio.charset.Charset;

/**
 * Created by zjt on 18-2-27.
 */

public class Constant {
    private static Constant constant = null;
    public Charset charset;
    private String filepath;
    private String tempFilepath;

    private Constant() {
        charset = Charset.forName("UTF-8");
        filepath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/filesaver/";
        tempFilepath = filepath + "temp/";
        File file = new File(filepath);
        if (!file.exists())
            file.mkdir();
        File tempFile = new File(tempFilepath);
        if (!tempFile.exists()) {
            tempFile.mkdir();
        }
    }

    public static Constant getInstance() {
        if (constant == null) {
            constant = new Constant();
        }
        return constant;
    }

    public String getFilepath(){
        File file = new File(filepath);
        if(!file.exists()){
            file.mkdir();
        }
        return filepath;
    }

    public String getTempFilepath(){
        File file = new File(tempFilepath);
        if(!file.exists()){
            file.mkdir();
        }
        return tempFilepath;
    }
}
