package com.example.zjt.nioserver.response;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Created by zjt on 18-1-31.
 */

public class VueAssets {
    private AssetManager mAssetManager;

    public VueAssets(Context context) {
        mAssetManager = context.getResources().getAssets();
    }

    public VueAssets(AssetManager mAssetManager) {
        this.mAssetManager = mAssetManager;
    }

    public InputStream isRootPage(String filename) {
        try {
            return mAssetManager.open(filename);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("assets", "" + e);
        } catch (Exception e) {
            Log.d("assets", "" + e);
        }
        return null;
    }
}
