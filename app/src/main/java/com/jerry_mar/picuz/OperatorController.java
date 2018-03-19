package com.jerry_mar.picuz;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import com.jerry_mar.picuz.config.Config;
import com.jerry_mar.picuz.config.Shape;
import com.jerry_mar.picuz.view.CropImageView;

import java.io.File;

public class OperatorController extends Activity implements CropImageView.Callback {
    private OperatorScene scene;

    private boolean execute = true;
    private int width;
    private int height;
    private int border;
    private int color;
    private Shape shape;
    private String path;

    @Override
    protected void onCreate(Bundle bundle) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(bundle);
        handle(getIntent());
        scene = new OperatorScene(this, R.layout.scene_operator);
        setContentView(scene.getView());
    }

    private void handle(Intent intent) {
        width = intent.getIntExtra("width", 0);
        height = intent.getIntExtra("height", 0);
        border = intent.getIntExtra("border", 0);
        color = intent.getIntExtra("color", 0);
        path = intent.getStringExtra("path");
        shape = (Shape) intent.getSerializableExtra("shape");
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        scene.initView(width, height, shape, path, this);
    }

    public void back(View view) {
        if (!execute)
            return;
        finish();
    }

    public void plastic(View view) {
        String tag = (String) view.getTag();
        int index = Integer.parseInt(tag);
        switch (index) {
            case -1 : {
                scene.plastic(false);
                index = 0;
            }
            break;
            case 0 : {
                scene.plastic(true);
                index = -1;
            }
            break;
            default:
                scene.shape(index - 1);
            break;
        }
        view.setTag(Integer.toString(index));
    }

    public void submit(View view) {
        File cache = getExternalCacheDir();
        if (!cache.exists() || !cache.isDirectory()) {
            cache.mkdirs();
        }
        scene.save(cache, width, height, border, color);
    }

    @Override
    public void onFinish(File file) {
        if (file != null) {
            Intent intent = new Intent();
            intent.putExtra(Config.RESULT, file.getAbsolutePath());
            setResult(RESULT_OK, intent);
        }
        finish();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        boolean result = true;
        if (keyCode != KeyEvent.KEYCODE_BACK || execute) {
            result = super.onKeyUp(keyCode, event);
        }
        return result;
    }
}
