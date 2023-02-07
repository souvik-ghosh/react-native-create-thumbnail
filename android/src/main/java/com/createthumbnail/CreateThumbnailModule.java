package com.reactlibrary.createthumbnail;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build.VERSION;
import android.text.TextUtils;
import android.webkit.URLUtil;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.GuardedResultAsyncTask;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
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
import java.lang.ref.WeakReference;
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
        new ProcessDataTask(reactContext, promise, options).execute();
    }

    private static class ProcessDataTask extends GuardedResultAsyncTask<ReadableMap> {
        private final WeakReference<Context> weakContext;
        private final Promise promise;
        private final ReadableMap options;

        protected ProcessDataTask(ReactContext reactContext, Promise promise, ReadableMap options) {
            super(reactContext.getExceptionHandler());
            this.weakContext = new WeakReference<>(reactContext.getApplicationContext());
            this.promise = promise;
            this.options = options;
        }

        @Override
        protected ReadableMap doInBackgroundGuarded() {
            String format = options.hasKey("format") ? options.getString("format") : "jpeg";
            String cacheName = options.hasKey("cacheName") ? options.getString("cacheName") : "";

            String thumbnailDir = weakContext.get().getApplicationContext().getCacheDir().getAbsolutePath() + "/thumbnails";
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
            HashMap headers = options.hasKey("headers") ? options.getMap("headers").toHashMap() : new HashMap<String, String>();
            String fileName = TextUtils.isEmpty(cacheName) ? ("thumb-" + UUID.randomUUID().toString()) : cacheName + "." + format;
            OutputStream fOut = null;
            try {
                File file = new File(cacheDir, fileName);
                Context context = weakContext.get();
                Bitmap image = getBitmapAtTime(context, filePath, timeStamp, headers);
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
            } catch (Exception e) {
                promise.reject("CreateThumbnail_ERROR", e);
            }
            return null;
        }

        @Override
        protected void onPostExecuteGuarded(ReadableMap readableArray) {
            promise.resolve(readableArray);
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

    private static Bitmap getBitmapAtTime(Context context, String filePath, int time, Map headers) {
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
  
        Bitmap image = retriever.getFrameAtTime(time * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
        try {
            retriever.release();
        } catch (IOException ex) {
            // handle the exception here
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
