package com.jerry_mar.picuz;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.jerry_mar.picuz.config.Config;
import com.jerry_mar.picuz.config.Shape;
import com.jerry_mar.picuz.utils.ImageDataSource;
import com.jerry_mar.picuz.view.CropImageView;

import java.io.File;

public class OperatorScene {
    private View root;
    private LayoutInflater inflater;

    private View shapeView;
    private CropImageView target;

    private View plasticView;

    public OperatorScene(Context context, int id) {
        inflater = LayoutInflater.from(context);
        root = inflater.inflate(id, null);
    }

    public View getView() {
        return root;
    }

    public void initView(int width, int height, Shape shape, String path, CropImageView.Callback c) {
        plasticView = root.findViewById(R.id.picuz_plastic);
        shapeView = root.findViewById(R.id.picuz_shape);
        target = (CropImageView) root.findViewById(R.id.picuz_target);
        target.setWidth(width);
        target.setHeight(height);
        target.setCallback(c);
        if (shape != null) {
            target.changeShape(shape);
            target.changeStyle(true);
            root.findViewById(R.id.operator_bar).setVisibility(View.GONE);
        }
        ImageDataSource.load(path, target, "operator:");
    }

    public void plastic(boolean b) {
        if (b) {
            shapeView.setVisibility(View.VISIBLE);
        } else {
            shapeView.setVisibility(View.GONE);
        }
    }

    public void shape(int index) {
        target.changeShape(index);
        plasticView.setTag("0");
        shapeView.setVisibility(View.GONE);
    }

    public void save(File file, int width, int height, int border, int color) {
        target.save(file, width, height, border, color);
        root.findViewById(R.id.picuz_loading).setVisibility(View.VISIBLE);
    }
}
