package com.jerry_mar.picuz.utils;

import android.graphics.Bitmap;

public interface Cache {
    Bitmap get(String key);

    void set(String key, Bitmap bitmap);

    int size();

    int maxSize();

    void clear();

    void clearKeyUri(String keyPrefix);
}
