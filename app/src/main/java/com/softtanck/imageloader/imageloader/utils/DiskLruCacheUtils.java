package com.softtanck.imageloader.imageloader.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Tanck
 * @Description 硬盘缓存工具<br>
 * <b>注：以DiskLruCache框架为底层</b>
 * @date Jan 20, 2015 8:40:31 PM
 */
public class DiskLruCacheUtils {

    public static final String object = "object";
    public static final String bitmap = "bitmap";

    /**
     * 储存缓存数据至手机
     *
     * @param context       context
     * @param cacheDirName  缓存目录
     * @param cacheFileName 缓存文件名
     * @param inputStream   缓存内容
     * @return 储存是否成功
     */
    public static boolean save(Context context, String cacheDirName,
                               String cacheFileName, InputStream inputStream) {
        OutputStream output = null;
        try {
            DiskLruCache mDiskLruCache = open(context, cacheDirName);
            DiskLruCache.Editor mDiskLruEditor;
            String key = hashKeyForDisk(cacheFileName);
            mDiskLruEditor = mDiskLruCache.edit(key);
            output = mDiskLruEditor.newOutputStream(0);

            if (inputStreamToOutputStream(inputStream, output))
                mDiskLruEditor.commit();
            else
                mDiskLruEditor.abort();

            mDiskLruCache.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (output != null)
                    output.close();
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 获取缓存数据
     *
     * @param context
     * @param cacheDirName  缓存目录
     * @param cacheFileName 缓存文件名
     * @return 缓存内容
     */
    public static InputStream get(Context context, String cacheDirName,
                                  String cacheFileName) {
        InputStream input = null;
        try {
            DiskLruCache mDiskLruCache = open(context, cacheDirName);
            String key = hashKeyForDisk(cacheFileName);
            DiskLruCache.Snapshot snapShot = mDiskLruCache.get(key);
            if (snapShot != null) {
                input = snapShot.getInputStream(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return input;
    }

    /**
     * 获取缓存数据
     *
     * @param context
     * @param cacheDirName  缓存目录
     * @param cacheFileName 缓存文件名
     * @return String 缓存内容
     */
    public static String getAsString(Context context, String cacheDirName,
                                     String cacheFileName) {

        String result = null;
        try {
            DiskLruCache mDiskLruCache = open(context, cacheDirName);
            String key = hashKeyForDisk(cacheFileName);
            DiskLruCache.Snapshot snapShot = mDiskLruCache.get(key);
            if (snapShot != null) {
                result = snapShot.getString(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;

    }

    /**
     * 删除缓存数据
     *
     * @param context       context
     * @param cacheDirName  缓存目录
     * @param cacheFileName 缓存文件名
     * @return 删除是否成功
     */
    public static boolean remove(Context context, String cacheDirName,
                                 String cacheFileName) {
        try {
            DiskLruCache mDiskLruCache = open(context, cacheDirName);
            String key = hashKeyForDisk(cacheFileName);
            mDiskLruCache.remove(key);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 开启缓存工具
     *
     * @param context      context
     * @param cacheDirName 缓存目录
     * @return
     */
    private static DiskLruCache open(Context context, String cacheDirName) {
        DiskLruCache mDiskLruCache = null;
        try {
            File cacheDir = getDiskCacheDir(context, cacheDirName);
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            mDiskLruCache = DiskLruCache.open(cacheDir, getAppVersion(context),
                    1, 10 * 1024 * 1024);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mDiskLruCache;
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private static File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueName);
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
            return info.versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }

    private static String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    /**
     * 更新数据 onPause() in activity
     */
    public static void flush(Context context, String cacheDirName) {
        File cacheDir = getDiskCacheDir(context, cacheDirName);
        try {
            DiskLruCache mDiskLruCache = DiskLruCache.open(cacheDir, getAppVersion(context), 1, 10 * 1024 * 1024);
            mDiskLruCache.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static boolean inputStreamToOutputStream(InputStream input, OutputStream output) {
        BufferedOutputStream out = null;
        BufferedInputStream in = null;
        in = new BufferedInputStream(input, 16 * 1024);
        out = new BufferedOutputStream(output, 16 * 1024);
        int b;
        try {
            while ((b = in.read()) != -1) {
                out.write(b);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null)
                    out.close();
                if (in != null)
                    in.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
