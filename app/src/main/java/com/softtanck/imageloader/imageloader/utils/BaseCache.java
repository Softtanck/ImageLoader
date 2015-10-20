package com.softtanck.imageloader.imageloader.utils;

import android.graphics.Bitmap;

/**
 * @author : Tanck
 * @Description : TODO
 * @date 10/19/2015
 */
public interface BaseCache {

    /**
     * 获取缓存
     *
     * @param key
     * @return
     */
    public Bitmap get(String key);

    /**
     * 设置缓存
     *
     * @param key
     * @param value
     * @return
     */
    public void put(String key, Bitmap value);
}
