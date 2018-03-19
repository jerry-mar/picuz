package com.jerry_mar.picuz.view;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

public class ImageView extends AppCompatImageView {
    public ImageView(Context context) {
        this(context, null);
    }

    public ImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = MeasureSpec.getSize(heightMeasureSpec);
        int mode = MeasureSpec.getMode(heightMeasureSpec);
        if (size == 0 && mode == MeasureSpec.EXACTLY) {
            heightMeasureSpec = (int) (widthMeasureSpec);
        } else {
            size = MeasureSpec.getSize(widthMeasureSpec);
            mode = MeasureSpec.getMode(widthMeasureSpec);

            if (size == 0 && mode == MeasureSpec.EXACTLY) {
                widthMeasureSpec = (int) (heightMeasureSpec);
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
