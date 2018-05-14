package com.jerry_mar.picuz;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.jerry_mar.picuz.adapter.ImageAdapter;
import com.jerry_mar.picuz.config.Config;
import com.jerry_mar.picuz.model.Image;
import com.jerry_mar.picuz.utils.ImageDataSource;

public class PreviewScene {
    private View root;
    private LayoutInflater inflater;

    private ImageView image;
    private RecyclerView recycler;
    private ImageView real;

    public PreviewScene(Context context, int id) {
        inflater = LayoutInflater.from(context);
        root = inflater.inflate(id, null);
    }

    public View getView() {
        return root;
    }

    public void initView(Config config) {
        image = (ImageView) root.findViewById(R.id.preview_target);
        recycler = (RecyclerView) root.findViewById(R.id.preview_list);
        real = (ImageView) root.findViewById(R.id.picuz_real);
        if (config.isReal()) {
            real.setImageResource(R.drawable.picuz_really);
        }
        LinearLayoutManager ms = new LinearLayoutManager(inflater.getContext());
        ms.setOrientation(LinearLayoutManager.HORIZONTAL);
        recycler.setLayoutManager(ms);
    }

    public void setImageAdapter(ImageAdapter adapter) {
        recycler.setAdapter(adapter);
    }

    public boolean real(Boolean real) {
        this.real.setImageResource(real ? R.drawable.picuz_real : R.drawable.picuz_really);
        return !real;
    }

    public void show(Image image) {
        ImageDataSource.load(image.getPath(), this.image, "preview:");
    }

    public void loading() {
        root.findViewById(R.id.picuz_loading).setVisibility(View.VISIBLE);
    }
}
