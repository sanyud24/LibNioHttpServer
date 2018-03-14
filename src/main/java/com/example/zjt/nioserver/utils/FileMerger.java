package com.example.zjt.nioserver.utils;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by zjt on 18-3-6.
 */

public class FileMerger {

    // 左闭右开
    public static void merge(String resultPath, String mergePath, int start, int end) {
        // nio 的方式合并文件
        try {
            Log.d("filemerger","merge " + resultPath + "  " + mergePath);
            File resultFile = new File(resultPath);
            if (resultFile.exists()) resultFile.delete(); // 非常危险，如果弄错了路径就不知道会删除什么了。
            resultFile.createNewFile(); // 同样非常危险。
            FileOutputStream resultOutputStream = new FileOutputStream(resultFile);
            FileChannel resultChannel = resultOutputStream.getChannel();
            ByteBuffer mergeBuffer = ByteBuffer.allocate(1024);
            for (int i = start; i < end; i++) {
                File inFile = new File(mergePath + i);
                if (inFile.exists()) {
                    FileInputStream inStream = new FileInputStream(inFile);
                    FileChannel inChannel = inStream.getChannel();
                    mergeBuffer.clear();
                    while(inChannel.read(mergeBuffer) >= 0){
                        mergeBuffer.flip();
                        resultChannel.write(mergeBuffer);
                        mergeBuffer.clear();
                    }
                    inStream.close();
                    inChannel.close();
                    inFile.delete();
                }
            }
            resultOutputStream.flush();
            resultOutputStream.close();
            resultChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
