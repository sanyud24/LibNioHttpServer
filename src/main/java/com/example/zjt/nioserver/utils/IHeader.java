package com.example.zjt.nioserver.utils;

import android.util.SparseArray;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Created by zjt on 18-1-31.
 */

public interface IHeader {
    public Map<String,String> header(String header) throws UnsupportedEncodingException;
}
