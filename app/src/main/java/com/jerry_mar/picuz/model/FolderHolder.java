package com.jerry_mar.picuz.model;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jerry_mar.picuz.R;
import com.jerry_mar.picuz.utils.ImageDataSource;

public class FolderHolder extends RecyclerView.ViewHolder {
    private TextView name;
    private TextView num;
    private ImageView cursor;
    private ImageView image;

    public FolderHolder(View itemView) {
        super(itemView);
        name = (TextView) itemView.findViewById(R.id.folder_name);
        num = (TextView) itemView.findViewById(R.id.image_num);
        cursor = (ImageView) itemView.findViewById(R.id.folder_cursor);
        image = (ImageView) itemView.findViewById(R.id.folder_cover);
    }

    public void setFolderName(String folderName) {
        name.setText(folderName);
    }

    public void setImageNum(int imageNum) {
        num.setText(Integer.toString(imageNum) + "张");
    }

    public void setUsed(boolean used) {
        if (used) {
            cursor.setImageResource(R.drawable.picuz_really);
        } else {
            cursor.setImageDrawable(null);
        }
    }

    public void setTag(Folder tag) {
        itemView.setTag(tag);
    }

    public void setImage(Image image) {
        ImageDataSource.load(image.getPath(), this.image);
    }
}
