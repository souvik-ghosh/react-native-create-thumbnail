package com.reactlibrary;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.HashMap;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Video.Thumbnails;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;

public class CreateThumbnailModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;

    public CreateThumbnailModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "CreateThumbnail";
    }

    @ReactMethod
    public void create(ReadableMap options, Promise promise) {
        String filePath = options.getString("url");
        int timeStamp = options.hasKey("timeStamp") ? options.getInt("timeStamp") : 0;
        filePath = filePath.replace("file://", "");
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        String fullPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/thumb";
        String fileName = "thumb-" + UUID.randomUUID().toString() + ".jpeg";

        try {
            retriever.setDataSource(filePath, new HashMap<String, String>());
            Bitmap image = retriever.getFrameAtTime(timeStamp * 1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            retriever.release();

            File dir = new File(fullPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            OutputStream fOut = null;
            File file = new File(fullPath, fileName);
            file.createNewFile();
            fOut = new FileOutputStream(file);

            // 100 means no compression, the lower you go, the stronger the compression
            image.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();

            WritableMap map = Arguments.createMap();
            map.putString("path", "file://" + fullPath + '/' + fileName);
            map.putDouble("width", image.getWidth());
            map.putDouble("height", image.getHeight());

            promise.resolve(map);
        } catch (Exception e) {
            promise.reject("CreateThumbnail_ERROR", e);
        }
    }
}
