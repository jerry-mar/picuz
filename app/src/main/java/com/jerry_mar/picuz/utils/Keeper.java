package com.jerry_mar.picuz.utils;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.Process;

import com.jerry_mar.picuz.config.Config;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public class Keeper implements Runnable, ThreadLevel {
    private ContentResolver resolver;
    private File cache;
    private Config config;
    private ImageDataSource.SaveCallback c;
    private Handler handler;

    public Keeper(ContentResolver resolver, File cache, Config config, ImageDataSource.SaveCallback c, Handler handler) {
        this.resolver = resolver;
        this.cache = cache;
        this.config = config;
        this.c = c;
        this.handler = handler;
    }

    @Override
    public void run() {
        int count = config.getImages().size();
        File files[] = cache.listFiles();
        for (int i = 0; i < files.length; i++) {
            files[i].delete();
        }
        for (int i = 0; i < count; i++) {
            int width = config.getWidth();
            int height = config.getHeight();
            String path = config.getImages().get(i).getPath();
            Bitmap bitmap = ImageUtils.compress(new File(path),
                    width == 0 ? 600 : width, height == 0 ? 600 : height, 0);
            File saveFile = new File(cache, "picuz_0" + i + "_temp_" + System.currentTimeMillis());
            OutputStream stream = null;
            try {
                saveFile.createNewFile();
                stream = resolver.openOutputStream(Uri.fromFile(saveFile));
                if (stream != null)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                else
                    throw new RuntimeException();
                config.getImages().get(i).setPath(saveFile.getAbsolutePath());
            } catch (IOException e) {
                config.getImages().remove(i);
            } finally {
                try {
                    stream.close();
                } catch (IOException e) {
                }
            }
        }
        Message msg = handler.obtainMessage();
        msg.obj = this;
        msg.what = 1;
        handler.sendMessage(msg);
    }

    public void finish() {
        c.onFinish(config.getImages());
    }

    @Override
    public int compareTo(ThreadLevel task) {
        int priority = task.getThreadPriority();
        return priority - task.getThreadPriority();
    }

    public int getThreadPriority() {
        return Process.THREAD_PRIORITY_BACKGROUND;
    }
}
