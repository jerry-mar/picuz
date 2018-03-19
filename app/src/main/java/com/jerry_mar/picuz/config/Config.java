package com.jerry_mar.picuz.config;

import com.jerry_mar.picuz.model.Image;

import java.io.Serializable;
import java.util.List;

public class Config implements Serializable {
    public static final String CONFIG = "picuz_config";
    public static final String RESULT = "picuz_result";

    public Config() {}

    public Config(boolean force, Shape shape, int max) {
        this(force, shape);
        this.max = max;
    }

    public Config(boolean force, Shape shape) {
        this.force = force;
        this.shape = shape;
    }

    public Config(boolean force, Shape shape, int width, int height) {
        this(force, shape);
        this.width = width;
        this.height = height;
    }

    public Config(boolean force, Shape shape, int width, int height, int max) {
        this(force, shape, width, height);
        this.max = max;
    }

    private int max = 9;
    private int width;
    private int height;
    private int border;
    private int borderColor;
    private Shape shape;
    private boolean force;
    private boolean real;
    private List<Image> images;

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Shape getShape() {
        return shape;
    }

    public void setShape(Shape shape) {
        this.shape = shape;
    }

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public boolean isReal() {
        return real;
    }

    public void setReal(boolean real) {
        this.real = real;
    }

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    public int getBorder() {
        return border;
    }

    public void setBorder(int border) {
        this.border = border;
    }

    public int getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(int borderColor) {
        this.borderColor = borderColor;
    }
}
