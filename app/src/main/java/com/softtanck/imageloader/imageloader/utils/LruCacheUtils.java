package com.softtanck.imageloader.imageloader.utils;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

/**
 * @author : Tanck
 * @Description : TODO
 * @date 10/19/2015
 */
public class LruCacheUtils implements BaseCache {

    /**
     * 内存缓存
     */
    private LruCache<String, Bitmap> mImageCache;

    /**
     * 磁盘缓存
     */
    private DiskLruCacheUtils diskLruCacheUtils;

    private static LruCacheUtils lruCacheUtils;

    private LruCacheUtils() {

        initCache();

    }

    private void initCache() {
        //内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        maxMemory = maxMemory / 4;
        mImageCache = new LruCache<String, Bitmap>(maxMemory) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight() / 1024;
            }
        };

    }

    public static LruCacheUtils getInstance() {
        if (null == lruCacheUtils) {
            lruCacheUtils = new LruCacheUtils();
        }
        return lruCacheUtils;
    }

    public void put(String key, Bitmap value) {
        if (null == key)
            throw new RuntimeException("this key is null");
        synchronized (LruCacheUtils.class) {
            //判断是否需要缓存到磁盘
            mImageCache.put(key, value);
        }
    }

    public Bitmap get(String key) {
        if (null == key)
            throw new RuntimeException("this key is null");
        synchronized (LruCacheUtils.class) {
            //判断是否需要从磁盘中获取
            return mImageCache.get(key);
        }
    }

}
