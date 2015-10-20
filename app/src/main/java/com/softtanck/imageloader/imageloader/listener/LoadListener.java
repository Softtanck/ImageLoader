package com.softtanck.imageloader.imageloader.listener;

import android.graphics.Bitmap;
import android.view.View;

/**
 * @author : Tanck
 * @Description : TODO 图片加载接口
 * @date 10/20/2015
 */
public interface LoadListener<T extends View> {

    /**
     * 加载中
     *
     * @param view
     * @param path
     * @param <T>
     */
    public <T> void Loading(View view, String path);

    /**
     * 加载成功
     *
     * @param view
     * @param bitmap
     * @param path
     * @param <T>
     */
    public <T> void LoadSuccess(View view, Bitmap bitmap, String path);

    /**
     * 加载失败
     *
     * @param view
     * @param path
     * @param errorMsg
     * @param <T>
     */
    public <T> void LoadError(View view, String path, String errorMsg);
}
