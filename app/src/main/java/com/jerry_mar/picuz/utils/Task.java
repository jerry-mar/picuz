package com.jerry_mar.picuz.utils;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.jerry_mar.picuz.R;

import java.io.File;

public class Task implements Runnable, ThreadLevel {
    private String path;
    private String newKey;
    private ImageView view;
    private Cache cache;
    private Drawable drawable;
    private Handler handler;

    public Task(String path, String newKey, ImageView view, Cache cache, Handler handler) {
        this.path = path;
        this.newKey = newKey;
        this.view = view;
        this.cache = cache;
        this.handler = handler;
    }

    @Override
    public void run() {
        try {
            Bitmap bitmap = null;
            File file = new File(path);
            if (file.exists() && !file.isHidden()) {
                int width = view.getWidth();
                int height = view.getHeight();
                if (width == 0 || height == 0) {
                    ViewGroup.LayoutParams params = view.getLayoutParams();
                    width = params.width;
                    height = params.height;
                }
                if (width > 0 && height > 0) {
                    bitmap = ImageUtils.compress(file, width, height, 0);
                } else {
                    bitmap = ImageUtils.toBitmap(file, null);
                }
            }
            if (bitmap == null) {
                throw new RuntimeException();
            }
            String key = path;
            if (newKey != null)
                key = newKey + path;
            cache.set(key, bitmap);
            drawable = new BitmapDrawable(bitmap);
        } catch (Exception e) {
            drawable = view.getResources().getDrawable(R.drawable.picuz_fail);
        }
        Message msg = handler.obtainMessage();
        msg.obj = this;
        msg.what = 0;
        handler.sendMessage(msg);
    }

    public void finish() {
        String tag = (String) view.getTag();
        if (path.equals(tag)) {
            view.setImageDrawable(drawable);
        }
    }

    @Override
    public int compareTo(ThreadLevel task) {
        int priority = task.getThreadPriority();
        return priority - task.getThreadPriority();
    }

    public int getThreadPriority() {
        return Process.THREAD_PRIORITY_FOREGROUND;
    }
}
