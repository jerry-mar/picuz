package com.jerry_mar.picuz;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.jerry_mar.picuz.adapter.ImageAdapter;
import com.jerry_mar.picuz.config.Config;
import com.jerry_mar.picuz.model.Image;
import com.jerry_mar.picuz.utils.ImageDataSource;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PreviewController extends Activity implements ImageDataSource.SaveCallback {
    private Config config;

    private PreviewScene scene;
    private ImageAdapter adapter;
    private boolean execute = true;
    private Image image;

    @Override
    protected void onCreate(Bundle bundle) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(bundle);
        scene = new PreviewScene(this, R.layout.scene_preview);
        config = (Config) getIntent().getSerializableExtra(Config.CONFIG);
        setContentView(scene.getView());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0x01 && resultCode == RESULT_OK && data != null) {
            image.setPath(data.getStringExtra(Config.RESULT));
            adapter.notifyItemChanged(image.getIndex());
            scene.show(image);
        }
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        scene.initView(config);
        scene.setImageAdapter(adapter = new ImageAdapter(LayoutInflater.from(this), true));
        adapter.update(config.getImages());
        scene.show(image = config.getImages().get(0));
    }

    public void back(View view) {
        if (!execute)
            return;
        finish();
    }

    public void real(View view) {
        if (!execute)
            return;
        Boolean real = (Boolean) view.getTag();
        if (real == null)
            real = Boolean.FALSE;
        real = scene.real(real);
        view.setTag(real);
        config.setReal(real);
    }

    public void submit(View view) {
        if (!execute)
            return;
        if (config.getImages().size() == 0) {
            finish();
        } else {
            if (config.isReal()) {
                complete();
            } else {
                File cache = new File(getExternalCacheDir(), "picuz");
                if (!cache.exists() || !cache.isDirectory()) {
                    cache.mkdirs();
                }
                execute = false;
                scene.loading();
                ImageDataSource.save(getContentResolver(), cache, config, this);
            }
        }
    }

    public void imageSelect(View view) {
        if (!execute)
            return;
        Image image = (Image) view.getTag();
        scene.show(image);
        this.image = image;
    }

    public void delele(View view) {
        Image image = (Image) view.getTag();
        image = adapter.delete(image);
        if (image == null) {
            Toast.makeText(this, "不能全部删除",
                    Toast.LENGTH_SHORT).show();
        } else {
            scene.show(image);
            this.image = image;
        }
    }

    public void operate(View view) {
        Intent intent = new Intent(this, OperatorController.class);
        intent.putExtra("width", config.getWidth());
        intent.putExtra("height", config.getHeight());
        intent.putExtra("shape", config.getShape());
        intent.putExtra("path", image.getPath());
        startActivityForResult(intent, 0x01);
    }

    private void complete() {
        Intent intent = new Intent();
        int count = config.getImages().size();
        ArrayList<String> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            result.add(config.getImages().get(i).getPath());
        }
        intent.putStringArrayListExtra(Config.RESULT, result);
        setResult(RESULT_OK, intent);
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

    @Override
    public void onFinish(List<Image> result) {
        config.setImages(result);
        complete();
    }
}