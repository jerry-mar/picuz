package com.jerry_mar.picuz.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.jerry_mar.picuz.R;
import com.jerry_mar.picuz.model.Image;
import com.jerry_mar.picuz.model.ImageHolder;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageHolder> {
    private LayoutInflater inflater;
    private List<Image> res;
    private boolean single;

    public ImageAdapter(LayoutInflater inflater, boolean single) {
        this.inflater = inflater;
        this.single = single;
    }

    @Override
    public ImageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ImageHolder(inflater.inflate(viewType,
                parent, false));
    }

    @Override
    public void onBindViewHolder(ImageHolder holder, int position) {
        int index = position;
        if (!single) {
            if (position == 0) {
                holder.setImage(null);
                holder.setTag(null);
                return;
            } else {
                index = position - 1;
            }
        }
        Image image = res.get(index);
        holder.setImage(image);
        holder.setTag(image);
        image.setIndex(position);
    }

    @Override
    public int getItemViewType(int position) {
        return single ? R.layout.item_preview : R.layout.item_image;
    }

    @Override
    public int getItemCount() {
        int ex = single ? 0 : 1;
        return res == null ? ex : res.size() + ex;
    }

    public void update(List<Image> res) {
        this.res = res;
        notifyDataSetChanged();
    }

    public Image delete(Image image) {
        if (res.size() == 1) {
            return null;
        } else {
            res.remove(image);
            notifyItemRemoved(image.getIndex());
            return res.get(0);
        }
    }
}
