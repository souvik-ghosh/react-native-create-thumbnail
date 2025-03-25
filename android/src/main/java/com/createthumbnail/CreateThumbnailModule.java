package com.reactlibrary.createthumbnail;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
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
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CreateThumbnailModule extends ReactContextBaseJavaModule {
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
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
        executor.execute(() -> {
            try {
                ReadableMap data = processData(options);
                handler.post(() -> {
                    promise.resolve(data);
                });
            } catch (IOException e) {
                handler.post(() -> {
                    promise.reject("CreateThumbnail_ERROR", e);
                });
            }
        });
    }

    private ReadableMap processData(ReadableMap options) throws IOException {
        String format = options.hasKey("format") ? options.getString("format") : "jpeg";
        String cacheName = options.hasKey("cacheName") ? options.getString("cacheName") : "";

        String thumbnailDir = reactContext.getApplicationContext().getCacheDir().getAbsolutePath() + "/thumbnails";
        File cacheDir = createDirIfNotExists(thumbnailDir);
        if (!TextUtils.isEmpty(cacheName)) {
            File file = new File(thumbnailDir, cacheName + "." + format);
            if (file.exists()) {
                WritableMap map = Arguments.createMap();
                map.putString("path", "file://" + file.getAbsolutePath());
                Bitmap image = BitmapFactory.decodeFile(file.getAbsolutePath());
                map.putDouble("size", image.getByteCount());
                map.putString("mime", "image/" + format);
                map.putDouble("width", image.getWidth());
                map.putDouble("height", image.getHeight());
                return map;
            }
        }

        String filePath = options.hasKey("url") ? options.getString("url") : "";
        int dirSize = options.hasKey("dirSize") ? options.getInt("dirSize") : 100;
        int timeStamp = options.hasKey("timeStamp") ? options.getInt("timeStamp") : 0;
        int maxWidth = options.hasKey("maxWidth") ? options.getInt("maxWidth") : 512;
        int maxHeight = options.hasKey("maxHeight") ? options.getInt("maxHeight") : 512;
        boolean onlySyncedFrames = options.hasKey("onlySyncedFrames") ? options.getBoolean("onlySyncedFrames") : true;
        HashMap headers = options.hasKey("headers") ? options.getMap("headers").toHashMap() : new HashMap<String, String>();
        String fileName = TextUtils.isEmpty(cacheName) ? ("thumb-" + UUID.randomUUID().toString()) : cacheName + "." + format;
        OutputStream fOut = null;
        File file = new File(cacheDir, fileName);
        Context context = reactContext;
        Bitmap image = getBitmapAtTime(context, filePath, timeStamp, maxWidth, maxHeight, onlySyncedFrames, headers);
        file.createNewFile();
        fOut = new FileOutputStream(file);

        // 100 means no compression, the lower you go, the stronger the compression
        if (format.equals("png")) {
            image.compress(Bitmap.CompressFormat.PNG, 100, fOut);
        } else {
            image.compress(Bitmap.CompressFormat.JPEG, 90, fOut);
        }

        fOut.flush();
        fOut.close();

        long cacheDirSize = (long) dirSize * 1024 * 1024;
        long newSize = image.getByteCount() + getDirSize(cacheDir);
        // free up some cached data if size of cache dir exceeds CACHE_DIR_MAX_SIZE
        if (newSize > cacheDirSize) {
            cleanDir(cacheDir, cacheDirSize / 2);
        }

        WritableMap map = Arguments.createMap();
        map.putString("path", "file://" + file.getAbsolutePath());
        map.putDouble("size", image.getByteCount());
        map.putString("mime", "image/" + format);
        map.putDouble("width", image.getWidth());
        map.putDouble("height", image.getHeight());
        return map;
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

    private static Bitmap getBitmapAtTime(Context context, String filePath, int time, int maxWidth, int maxHeight, boolean onlySyncedFrames, Map headers) throws IOException, IllegalStateException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        if (URLUtil.isFileUrl(filePath)) {
            String decodedPath;
            try {
                decodedPath = URLDecoder.decode(filePath, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                decodedPath = filePath;
            }

            retriever.setDataSource(decodedPath.replace("file://", ""));
        } else if (filePath.contains("content://")) {
            retriever.setDataSource(context, Uri.parse(filePath));
        } else {
            if (VERSION.SDK_INT < 14) {
                throw new IllegalStateException("Remote videos aren't supported on sdk_version < 14");
            }
            retriever.setDataSource(filePath, headers);
        }

        Bitmap image;
        if (Build.VERSION.SDK_INT >= 27) {
            image = retriever.getScaledFrameAtTime(time * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC, maxWidth, maxHeight);
        } else {
            // If on versions lower than API 27, use other methods to get frames
            image = retriever.getFrameAtTime(time * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            if (image != null) {
                image = Bitmap.createScaledBitmap(image, maxWidth, maxHeight, true);
            }
        }
        try {
            retriever.release();
        } catch(IOException e) {
            e.printStackTrace();
        }
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
