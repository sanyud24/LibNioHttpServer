package com.example.zjt.nioserver.server;

import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;

import com.example.zjt.nioserver.constant.Constant;
import com.example.zjt.nioserver.object.JsonFile;
import com.example.zjt.nioserver.response.Response;
import com.example.zjt.nioserver.response.Status;
import com.example.zjt.nioserver.response.VueAssets;
import com.example.zjt.nioserver.thread.ThreadPool;
import com.example.zjt.nioserver.utils.Header;
import com.example.zjt.nioserver.utils.PostFileSaver;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

public class VueRunnable implements Runnable {
    private SparseArray<String> mBoundaryArray;
    private SocketChannel mSocketChannel;
    private byte[] mReaderArray;
    private VueAssets mVueAssets;
    private Response mResponse;
    private int mHash, mIndexNum;

    private final String rootPath = Environment.getExternalStorageDirectory().getPath();

    public VueRunnable(AssetManager assetManager, SparseArray<String> boundaryArray,
                       int hash, int indexNum, SocketChannel sc, byte[] mReaderArray) {
        this.mSocketChannel = sc;
        this.mReaderArray = mReaderArray;
        this.mHash = hash;
        this.mIndexNum = indexNum;
        this.mBoundaryArray = boundaryArray;
        mVueAssets = new VueAssets(assetManager);
        mResponse = new Response();
    }

    @Override
    public void run() {
        try {
            final String receive = new String(mReaderArray, Constant.getInstance().charset);
            Header header = new Header(receive);
            String method = header.get("method");
            String url = header.get("url");
            switch (method) {
                case "get":
                    String filename = "index.html";
                    if (!url.equals("/")) {
                        filename = url.substring(url.lastIndexOf("/") + 1);
                        if (!isJson(url)) {
                            // 文件
                            File downloadFile = new File(url);
                            if (downloadFile.exists()) {
                                FileInputStream inputFile = new FileInputStream(downloadFile);
                                mResponse.response(mSocketChannel, inputFile, Status.OK, mimeType(filename));
                                inputFile.close();
                            }
                        }
                    }
                    InputStream rootStream = mVueAssets.isRootPage(filename);
                    rootStream = rootStream == null ? mVueAssets.isRootPage(filename = "index.html") : rootStream;
                    if (rootStream != null) {
                        mResponse.response(mSocketChannel, rootStream, Status.OK, mimeType(filename));
                        rootStream.close();
                    }
                    break;
                case "post":
                    mResponse.response(mSocketChannel, "success", Status.OK, "text/html");
                    final String boundary = "--" + header.get("boundary");
                    mBoundaryArray.append(mHash, boundary);// 保存分隔符
                    if (mIndexNum == 1) {
                        clearTempFolder();
                    }
                    // 对于post的文件不能很好的接收
//                    ThreadPool.execute(new Runnable() {
//                        public void run() {
//                            try {
                                PostFileSaver.fileSaver(receive, mIndexNum, boundary, mReaderArray, mBoundaryArray);
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    });
                    break;
                default:
                    // 既不是get方法也不是post方法 说明是post的内容
                    // 取出分隔符
//                    ThreadPool.execute(new Runnable() {
//                        public void run() {
//                            try {
                                PostFileSaver.fileSaver(receive, mIndexNum, mBoundaryArray.get(mHash), mReaderArray, mBoundaryArray);
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    });
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearTempFolder() {
        File file = new File(Constant.getInstance().getTempFilepath());
        File[] files = file.listFiles();
        for (File f : files) {
            f.delete();
        }
    }

    private boolean isJson(String url) throws IOException {
        if (url.startsWith("/root/")) {
            if (url.equalsIgnoreCase("/root/"))
                url = rootPath + url.substring(5);
            else
                url = url.substring(5);
            File file = new File(url);
            File[] files = file.listFiles();
            List<JsonFile> list = new ArrayList<>();
            if (files != null) {
                for (File file1 : files) {
                    JsonFile jsonFile = new JsonFile();
                    jsonFile.filename = file1.getName();
                    if (file1.isDirectory()) {
                        jsonFile.type = "folder";
                        jsonFile.filepath = "/root" + file1.getAbsolutePath();
                    } else {
                        jsonFile.type = "file";
                        jsonFile.filepath = file1.getAbsolutePath();
                    }
                    list.add(jsonFile);
                }
            }
            JSONArray jsonArray = new JSONArray();
            for (JsonFile jsonFile : list) {
                JSONObject object = new JSONObject();
                try {
                    object.put("fileName", jsonFile.filename);
                    object.put("fileType", jsonFile.type);
                    object.put("fileRoute", jsonFile.filepath);
                    jsonArray.put(object);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            mResponse.response(mSocketChannel, jsonArray.toString(), Status.OK, "application/json");
            return true;
        }
        return false;
    }

    private String mimeType(String filename) {
        if (filename != null) {
            if (filename.endsWith(".js")) return "application/x-javascript";
            else if (filename.endsWith(".jpg")) return "image/jpeg";
            else if (filename.endsWith(".jpeg")) return "image/jpeg";
            else if (filename.endsWith(".html")) return "text/html";
            else if (filename.endsWith(".png")) return "image/png";
            else if (filename.endsWith(".css")) return "text/css";
            return "application/octet-stream";
        }
        return null;
    }
}