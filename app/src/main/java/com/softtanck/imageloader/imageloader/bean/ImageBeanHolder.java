package com.softtanck.imageloader.imageloader.bean;

import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * @author : Tanck
 * @Description : TODO
 * @date 10/19/2015
 */
public class ImageBeanHolder {

    private String path;

    private Bitmap bitmap;

    private ImageView imageView;

    public ImageView getImageView() {
        return imageView;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
