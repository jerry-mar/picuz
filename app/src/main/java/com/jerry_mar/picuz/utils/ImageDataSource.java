package com.jerry_mar.picuz.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.SparseArray;

import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.jerry_mar.picuz.config.Config;
import com.jerry_mar.picuz.model.Folder;
import com.jerry_mar.picuz.model.Image;

import java.io.File;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ImageDataSource implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = 2 * CPU_COUNT - 1;
    private static final int MAXIMUM_POOL_SIZE = CORE_POOL_SIZE;
    private static final int KEEP_ALIVE_SECONDS = 60;
    private static ThreadPoolExecutor executor;
    private static Cache cache;

    public static final String SORT = MediaStore.Images.Media.DATE_ADDED +" DESC";
    public static final String[] CONDITION = {
            MediaStore.Images.Media.DATA,                       //图片路径
            MediaStore.Images.Media.DISPLAY_NAME,               //图片名称
            MediaStore.Images.Media.WIDTH,                      //图片宽度
            MediaStore.Images.Media.HEIGHT,                     //图片高度
            MediaStore.Images.Media.BUCKET_ID,                  //文件夹ID
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,        //文件夹名称
            MediaStore.Images.Media.DATE_ADDED                  //图片添加时间
    };

    @SuppressLint("HandlerLeak")
    private static final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0 : {
                    Task task = (Task) msg.obj;
                    task.finish();
                }
                break;
                case 1 : {
                    Keeper task = (Keeper) msg.obj;
                    task.finish();
                }
                break;
            }
        }
    };

    public static void init(ThreadPoolExecutor executor) {
        if (ImageDataSource.executor != null) {
            ImageDataSource.executor.shutdownNow();
        }
        ImageDataSource.executor = executor;
    }

    static ThreadPoolExecutor getThreadPoolExecutor(Context context) {
        if (executor == null || cache == null) {
            synchronized (ImageDataSource.class) {
                if (executor == null) {
                    executor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
                            KEEP_ALIVE_SECONDS, TimeUnit.SECONDS,
                            new PriorityBlockingQueue<Runnable>());
                    executor.allowCoreThreadTimeOut(true);
                }
                if (cache == null) {
                    cache = new LruCache(context);
                }
            }
        }
        return executor;
    }

    public static void clear() {
        if (cache != null) {
            cache.clear();
            cache = null;
        }
    }

    private Activity context;
    private Callback c;

    public ImageDataSource(Activity activity, Callback c) {
        this.context = activity;
        this.c = c;
    }

    public void scan(String path) {
        LoaderManager manager = context.getLoaderManager();
        Bundle bundle = new Bundle();
        bundle.putString(getClass().getSimpleName(), path);
        manager.initLoader(hashCode(), bundle, this);
    }


    public static void load(final String path, final ImageView imageView) {
        load(path, imageView, null);
        imageView.requestLayout();
    }

    public static void load(final String path, final ImageView imageView, final String newKey) {
        final ThreadPoolExecutor executor = getThreadPoolExecutor(imageView.getContext());
        String key = path;
        if (newKey != null) {
            key = newKey + path;
        }
        Bitmap bitmap = cache.get(key);
        imageView.setTag(path);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            if (imageView.isShown()) {
                executor.execute(new Task(path, newKey, imageView, cache, handler));
            } else {
                ViewTreeObserver.OnGlobalLayoutListener listener = new ViewTreeObserver.OnGlobalLayoutListener() {
                    private boolean intercept;
                    @Override
                    public void onGlobalLayout() {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            imageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        } else {
                            intercept = true;
                        }
                        if (!intercept) {
                            executor.execute(new Task(path, newKey, imageView, cache, handler));
                        }
                    }
                };
                if (imageView.getWidth() != 0 && imageView.getHeight() != 0) {
                    listener.onGlobalLayout();
                } else {
                    imageView.getViewTreeObserver().addOnGlobalLayoutListener(listener);
                }
            }
        }
    }

    public static void save(ContentResolver resolver, File cache, Config config, ImageDataSource.SaveCallback c) {
        ThreadPoolExecutor executor = getThreadPoolExecutor(null);
        executor.execute(new Keeper(resolver, cache, config, c, handler));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sql = args.getString(getClass().getSimpleName());
        if (sql != null) {
            sql = CONDITION[0] + " like '%" + sql + "%'";
        }
        return new CursorLoader(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                CONDITION, sql,null, SORT);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        SparseArray<Folder> result = new SparseArray<>();
        if (data != null && data.getCount() > 0) {
            result.put(0, new Folder("全部图片", true));
            while (data.moveToNext()) {
                Image image = new Image();
                image.setPath(data.getString(data.getColumnIndexOrThrow(CONDITION[0])));
                image.setName(data.getString(data.getColumnIndexOrThrow(CONDITION[1])));
                image.setWidth(data.getInt(data.getColumnIndexOrThrow(CONDITION[2])));
                image.setHeight(data.getInt(data.getColumnIndexOrThrow(CONDITION[3])));
                int id = Math.abs(data.getInt(data.getColumnIndexOrThrow(CONDITION[4])));
                Folder folder = result.get(id);
                if (folder == null) {
                    result.put(id, folder = new Folder(data.getString(
                            data.getColumnIndexOrThrow(CONDITION[5])), false));
                }
                folder.add(image);
                folder = result.get(0);
                folder.add(image);
            }
        }
        c.onFinish(result);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}

    public interface Callback {
        void onFinish(SparseArray<Folder> result);
    }

    public interface SaveCallback {
        void onFinish(List<Image> result);
    }
}
