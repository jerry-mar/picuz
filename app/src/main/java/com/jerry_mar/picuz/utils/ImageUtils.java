package com.jerry_mar.picuz.utils;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

public class ImageUtils {
    /**
     * @since 1.0
     * @param bitmap 图片的Bitmap
     * @param format 图片压缩格式
     * @return 图片二进制数组
     */
    public static byte[] toBinary(Bitmap bitmap, CompressFormat format) {
        byte[] binary = null;
        if (bitmap != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(format, 100, stream);
            binary = stream.toByteArray();
        }
        return binary;
    }

    /**
     * @since 1.0
     * @param bitmap 图片的Bitmap
     * @return 图片二进制数组
     */
    public static byte[] toBinary(Bitmap bitmap) {
        return toBinary(bitmap, CompressFormat.PNG);
    }

    /**
     * @since 1.0
     * @param binary 图片二进制数组
     * @return 图片的Bitmap
     */
    public static Bitmap toBitmap(byte[] binary) {
        return toBitmap(binary, null);
    }

    public static Bitmap toBitmap(File file, BitmapFactory.Options options) {
        Bitmap bitmap = null;
        if (file != null && file.exists()) {
            bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        }
        return bitmap;
    }

    /**
     * @since 1.0
     * @param binary 图片二进制数组
     * @param options 图片信息
     * @return 图片的Bitmap
     */
    public static Bitmap toBitmap(byte[] binary, BitmapFactory.Options options) {
        Bitmap bitmap = null;
        if (binary != null && binary.length > 0) {
            bitmap = BitmapFactory.decodeByteArray(binary, 0, binary.length, options);
        }
        return bitmap;
    }

    /**
     * @since 1.0
     * @param drawable 图片的Drawable
     * @return 图片的Bitmap
     */
    public static Bitmap toBitmap(Drawable drawable) {
        Bitmap bitmap = null;
        if (drawable != null && drawable instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) drawable).getBitmap();
        }
        return bitmap;
    }

    /**
     * @since 1.0
     * @param src 原图Bitmap
     * @param scaleWidth 宽度缩放比例
     * @param scaleHeight 高度缩放比例
     * @return 缩放后Bitmap
     */
    public static Bitmap scale(Bitmap src, float scaleWidth, float scaleHeight) {
        Bitmap bitmap = null;
        if (src != null) {
            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);
            bitmap = Bitmap.createBitmap(src, 0, 0, src.getWidth(),
            src.getHeight(), matrix, true);
        }
        return bitmap;
    }

    /**
     * @since 1.0
     * @param src 原图Bitmap
     * @param newWidth 缩放后的宽度
     * @param newHeight 缩放后的高度
     * @return 缩放后Bitmap
     */
    public static Bitmap scale(Bitmap src, int newWidth, int newHeight) {
        Bitmap bitmap = null;
        if (src != null) {
            float width = newWidth * 1.0F;
            float height = newHeight * 1.0F;
            bitmap = scale(src, width / src.getWidth(), height / src.getHeight());
        }
        return bitmap;
    }

    public static Bitmap thumb(Bitmap src, int newWidth, int newHeight) {
        Bitmap bitmap = null;
        if (src != null) {
            float width = newWidth * 1.0F;
            float height = newHeight * 1.0F;
            float scaleWidth = width / src.getWidth();
            float scaleHeight = height / src.getHeight();
            float scale = Math.min(scaleWidth, scaleHeight);
            bitmap = scale(src, scale, scale);
        }
        return bitmap;
    }

    /**
     * @since 1.0
     * @param src 原图Bitmap
     * @param degress 旋转角度
     * @return 旋转后Bitmap
     */
    public static Bitmap rotate(Bitmap src, float degress) {
        Bitmap bitmap = null;
        if (src != null) {
            Matrix matrix = new Matrix();
            matrix.postRotate(degress);
            bitmap = Bitmap.createBitmap(src, 0, 0, src.getWidth(),
                    src.getHeight(), matrix, true);
        }
        return bitmap;
    }

    /**
     * @since 1.0
     * @param src 原图Bitmap
     * @param precent 马赛克模糊等级
     * @return 打码后的Bitmap
     */
    public static Bitmap mosaics(Bitmap src, double precent) {
        int width = src.getWidth();
        int height = src.getHeight();
        int[] pixels = new int[height * width];
        src.getPixels(pixels, 0, width, 0, 0, width, height);
        int raw = (int) (width * precent);
        int unit;
        if (raw == 0) {
            unit = width;
        } else {
            unit = width / raw;
        }
        if (unit >= width || unit >= height) {
            return mosaics(src, precent);
        }
        for (int h = 0; h < height; h += unit) {
            for (int w = 0; w < width; w += unit) {
                int leftTopPoint = h * width + w;
                for (int x = 0; x < unit; x++) {
                    for (int y = 0; y < unit; y++) {
                        int point = (h + x) * width + (w + y);
                        if (point < pixels.length) {
                            pixels[point] = pixels[leftTopPoint];
                        }
                    }
                }
            }
        }
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);
    }

    /**
     * @since 1.0
     * @param src 原图Bitmap
     * @param kb 压缩后的最大内存(图片质量压缩)
     * @return 压缩后的Bitmap
     */
    public static Bitmap compress(Bitmap src, long kb) {
        Bitmap bitmap = null;
        if (src != null) {
            ByteArrayOutputStream writer = new ByteArrayOutputStream();
            src.compress(CompressFormat.JPEG, 100, writer);
            int options = 100;
            while (writer.toByteArray().length / 1024 > kb) {
                writer.reset();
                src.compress(CompressFormat.JPEG, options, writer);
                options -= 10;
            }
            ByteArrayInputStream reader = new ByteArrayInputStream(writer.toByteArray());
            bitmap = BitmapFactory.decodeStream(reader, null, null);
        }
        return bitmap;
    }

    /**
     * @since 1.0
     * @param binary 图片二进制信息
     * @param width 图片缩放宽度
     * @param height 图片缩放高度
     * @param kb 图片最大内存
     * @return 图片压缩后Bitmap
     */
    public static Bitmap compress(byte[] binary, int width, int height, long kb) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(binary, 0, binary.length, options);
        float sourceWidth = options.outWidth * 1.0F;
        float sourceHeight = options.outHeight * 1.0F;
        options.inJustDecodeBounds = false;
        int inSampleSize = 1;
        if (width == 0 || height == 0) {
            width = (int) (sourceWidth / 2);
            height = (int) (sourceHeight / 2);
        }
        if (width > sourceWidth || height > sourceHeight) {
            inSampleSize = (int) Math.min(Math.rint(sourceWidth / width),
                    Math.rint(sourceHeight / height));
        }
        options.inSampleSize = inSampleSize;
        Bitmap bitmap = toBitmap(binary, options);
        Bitmap temp = bitmap;
        bitmap = thumb(temp, width, height);
        if (temp != bitmap)
            temp.recycle();
        if(kb > 0) {
            temp = bitmap;
            bitmap = compress(bitmap, kb);
            if (temp != bitmap)
                temp.recycle();
        }
        return bitmap;
    }

    public static Bitmap compress(File file, int width, int height, long kb) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        float sourceWidth = options.outWidth * 1.0F;
        float sourceHeight = options.outHeight * 1.0F;
        options.inJustDecodeBounds = false;
        int inSampleSize = 1;
        if (width == 0 || height == 0) {
            width = (int) (sourceWidth / 2);
            height = (int) (sourceHeight / 2);
        }
        if (width < sourceWidth || height < sourceHeight) {
            inSampleSize = (int) Math.min(Math.rint(sourceWidth / width),
                    Math.rint(sourceHeight / height));
        }
        options.inSampleSize = inSampleSize;
        Bitmap bitmap = toBitmap(file, options);
        Bitmap temp = bitmap;
        bitmap = thumb(temp, width, height);
        if (temp != bitmap)
            temp.recycle();
        if(kb > 0) {
            temp = bitmap;
            bitmap = compress(bitmap, kb);
            if (temp != bitmap)
                temp.recycle();
        }
        return bitmap;
    }
}
