package com.example.zjt.nioserver.utils;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zjt on 18-1-31.
 */

public class Header implements IHeader {
    private Map<String,String> headerMap = null;

    public Header(String header){
        headerMap = new HashMap<>();
        try {
            this.header(header);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public Map<String, String> header(String header){
        try {
            // method
            int index = header.indexOf(' ');
            String method = header.substring(0, index);
            headerMap.put("method", method);// method
            // url
            int newIndex = header.indexOf(' ', index + 1);
            String url = header.substring(index + 1, newIndex);
            headerMap.put("url2", url);
            url = java.net.URLDecoder.decode(url, "utf-8");
            headerMap.put("url", url);// url
            // Accept-Encoding
            index = header.indexOf("accept-encoding");
            if (index != -1) {
                index += "accept-encoding".length();
                String encoding = header.substring(index, header.indexOf("\r\n", index));
                headerMap.put("accept-ancoding", encoding);
            }// Accept-Encoding
            // boundary
            if(method.equalsIgnoreCase("post")){
                index = header.indexOf("boundary=") + "boundary=".length();
                newIndex = header.indexOf("\r\n",index);
                headerMap.put("boundary",header.substring(index,newIndex));
            }
        }catch (Exception e){
//            Log.d("exception:" + getClass().getName(),"header: " + e);
        }
        return headerMap;
    }

    public String get(String key){
        String str = headerMap.get(key.toLowerCase());
        return str == null ? "" : str.toLowerCase();
    }
}
