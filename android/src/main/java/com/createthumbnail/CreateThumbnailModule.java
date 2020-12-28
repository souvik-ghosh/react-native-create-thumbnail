package com.reactlibrary.createthumbnail;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build.VERSION;
import android.webkit.URLUtil;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;

import org.apache.commons.io.comparator.LastModifiedFileComparator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
        String filePath = options.hasKey("url") ? options.getString("url") : "";
        String format = options.hasKey("format") ? options.getString("format") : "jpeg";
        int timeStamp = options.hasKey("timeStamp") ? options.getInt("timeStamp") : 0;
        int dirSize = options.hasKey("dirSize") ? options.getInt("dirSize") : 100;
        HashMap headers = options.hasKey("headers") ? options.getMap("headers").toHashMap() : new HashMap<String, String>();
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        String thumbnailDir = options.hasKey("tempDir") ? options.getString("tempDir") : reactContext.getApplicationContext().getCacheDir().getAbsolutePath() + "/thumbnails";
        String fileName = "thumb-" + UUID.randomUUID().toString() + "." + format;
        long cacheDirSize = dirSize * 1024 * 1024;
        OutputStream fOut = null;

        try {
            File dir = createDirIfNotExists(thumbnailDir);
            Bitmap image = getBitmapAtTime(filePath, timeStamp, headers);
            File file = new File(thumbnailDir, fileName);
            file.createNewFile();
            fOut = new FileOutputStream(file);

            // 100 means no compression, the lower you go, the stronger the compression
            if (format == "png") {
                image.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            } else {
                image.compress(Bitmap.CompressFormat.JPEG, 90, fOut);
            }

            fOut.flush();
            fOut.close();

            long newSize = image.getByteCount() + getDirSize(dir);
            // free up some cached data if size of cache dir exceeds CACHE_DIR_MAX_SIZE
            if (newSize > cacheDirSize) {
                cleanDir(dir, cacheDirSize / 2);
            }

            WritableMap map = Arguments.createMap();
            map.putString("path", "file://" + thumbnailDir + '/' + fileName);
            map.putDouble("width", image.getWidth());
            map.putDouble("height", image.getHeight());

            promise.resolve(map);
        } catch (Exception e) {
            promise.reject("CreateThumbnail_ERROR", e);
        }
    }

    // delete previously added files one by one untill requred space is available
    private static void cleanDir(File dir, long bytes) {
        long bytesDeleted = 0;
        File[] files = dir.listFiles();
        Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_COMPARATOR);

        for (File file : files) {
            bytesDeleted += file.length();
            file.delete();

            if (bytesDeleted >= bytes) {
                break;
            }
        }
    }

    private static File createDirIfNotExists(String path) {
        File dir = new File(path);
        if (dir.exists()) {
            return dir;
        }

        try {
            dir.mkdirs();
            // Add .nomedia to hide the thumbnail directory from gallery
            File noMedia = new File(path, ".nomedia");
            noMedia.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return dir;
    }

    private static Bitmap getBitmapAtTime(String filePath, int time, Map headers) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        if (URLUtil.isFileUrl(filePath)) {
            retriever.setDataSource(Uri.decode(filePath).replace("file://", ""));
        } else {
            if (VERSION.SDK_INT < 14) {
                throw new IllegalStateException("Remote videos aren't supported on sdk_version < 14");
            }
            retriever.setDataSource(filePath, headers);
        }
  
        Bitmap image = retriever.getFrameAtTime(time * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
        retriever.release();
        if (image == null) {
            throw new IllegalStateException("File doesn't exist or not supported");
        }
        return image;
    }

    private static long getDirSize(File dir) {
        long size = 0;
        File[] files = dir.listFiles();

        for (File file : files) {
            if (file.isFile()) {
                size += file.length();
            }
        }

        return size;
    }
}
