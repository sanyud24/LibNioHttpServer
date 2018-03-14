package com.example.zjt.nioserver.utils;

import android.util.Log;
import android.util.SparseArray;

import com.example.zjt.nioserver.constant.Constant;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class PostFileSaver {

    public static void fileSaver(String receive, int indexNum, String boundary, byte[] readerArray,
                                 SparseArray<String> mBoundaryArray) throws IOException {
        int boundaryIndex = indexOf(readerArray, boundary.getBytes(Constant.getInstance().charset), 0),
                boundaryEndIndex = indexOf(readerArray, (boundary + "--").getBytes(Constant.getInstance().charset), boundaryIndex);
        boolean isEnd = false;
        LogUtils.w("最初的消息：\n" + receive + "\n");
        // 文件名称
        if (boundaryEndIndex == 0) return;
        if (boundaryIndex >= 0 && boundaryIndex != boundaryEndIndex) {
            int postNameIndex = receive.indexOf("name=\"") + "name=\"".length(),
                    postNameEnd = receive.indexOf("\"", postNameIndex);
            String postName = "";
            if (postNameIndex >= 0 && postNameEnd >= 0) {
                postName = receive.substring(postNameIndex, postNameEnd);
                // 保存文件的名称
                mBoundaryArray.append(boundary.hashCode(), postName);
            }
            boundaryIndex = indexOf(readerArray, new byte[]{13, 10, 13, 10}, postNameEnd) + 4;
            int mergeEndIndex;
            if (postName.startsWith("file-") && (mergeEndIndex = postName.lastIndexOf("-success")) >= 5) {
                // 合并文件
                FileMerger.merge(Constant.getInstance().getFilepath() + postName.substring(5, mergeEndIndex),
                        Constant.getInstance().getTempFilepath(), 0, indexNum);
                return;
            }
        }
        String postName = "" + indexNum;
        // 数据
        boundaryIndex = (boundaryIndex == boundaryEndIndex || boundaryIndex < 0) ? 0 : boundaryIndex;
        // boundaryEndIndex 应该是数组中无效部分的第一个
        isEnd = boundaryEndIndex >= 0;
        boundaryEndIndex = (isEnd ? boundaryEndIndex - 2 : indexOfValid(readerArray));
        LogUtils.w("index 的位置：" + boundaryIndex + " " + boundaryEndIndex + "\n");
        // 保存文件
        if (boundaryEndIndex > boundaryIndex) {
            LogUtils.w("存储的数据：\n" + new String(readerArray, boundaryIndex, boundaryEndIndex - boundaryIndex) + "\n");
            File file = new File(Constant.getInstance().getTempFilepath() + postName);
            file.createNewFile();
            RandomAccessFile raf = new RandomAccessFile(file, "rws");
            raf.seek(file.length());
            raf.write(readerArray, boundaryIndex, boundaryEndIndex - boundaryIndex);
            raf.close();
        }
    }

    private static int indexOfValid(byte[] readerArray) {
        // 这里应该是从后往前找的
        if (readerArray != null) {
            int i;
            for (i = readerArray.length - 1; i >= 0; i++) {
                if (readerArray[i] != 0)
                    break;
            }
            return i + 1;
        }
        return 0;
    }

    private static int indexOf(byte[] main, byte[] key, int start) {
        // 在main 中查找key
        if (main != null && key != null) {
            start = start > 0 ? start : 0;
            int location = start;
            for (int i = start, mainLength = main.length, j = 0,
                 keyLength = key.length; i < mainLength; location++) {
                i = location;
                for (j = 0; i < mainLength && j < keyLength; ) {
                    if (main[i] == key[j]) {
                        i++;
                        j++;
                    } else {
                        break;
                    }
                }
                if (j == keyLength)
                    return location;
            }
        }
        return -1;
    }
}
