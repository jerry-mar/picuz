package com.jerry_mar.picuz.model;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.jerry_mar.picuz.R;
import com.jerry_mar.picuz.utils.ImageDataSource;

public class ImageHolder extends RecyclerView.ViewHolder {
    private ImageView image;
    private ImageView select;
    private View del;

    public ImageHolder(View itemView) {
        super(itemView);
        image = (ImageView) itemView.findViewById(R.id.image_target);
        select = (ImageView) itemView.findViewById(R.id.image_selected);
        del = itemView.findViewById(R.id.picuz_del);
    }

    public void setImage(Image image) {
        if (image != null) {
            this.image.setPadding(0, 0, 0, 0);
            ImageDataSource.load(image.getPath(), this.image);
            this.image.requestLayout();
            if (select != null) {
                select.setImageResource(image.isUsed() ? R.drawable.pizuc_en : R.drawable.picuz_un);
                select.setVisibility(View.VISIBLE);
            }
        } else {
            int padding = this.image.getWidth() / 4;
            this.image.setPadding(padding, padding, padding, padding);
            this.image.setImageResource(R.drawable.picuz_camera);
            select.setVisibility(View.GONE);
        }
    }

    public void setTag(Image image) {
        if (del != null) {
            del.setTag(image);
        }
        itemView.setTag(image);
    }
}
