package com.jerry_mar.picuz.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.jerry_mar.picuz.R;
import com.jerry_mar.picuz.model.Folder;
import com.jerry_mar.picuz.model.FolderHolder;

public class FolderAdapter extends RecyclerView.Adapter<FolderHolder> {
    private LayoutInflater inflater;
    private SparseArray<Folder> res;
    private Folder curFolder;
    private boolean once;

    public FolderAdapter(LayoutInflater inflater) {
        this.inflater = inflater;
    }

    @Override
    public FolderHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FolderHolder(inflater.inflate(R.layout.item_folder,
                parent, false));
    }

    @Override
    public void onBindViewHolder(FolderHolder holder, int position) {
        Folder folder = res.valueAt(position);
        holder.setFolderName(folder.getName());
        holder.setImageNum(folder.getImages().size());
        holder.setUsed(folder.isUsed());
        holder.setTag(folder);
        holder.setImage(folder.getCover());
        if (folder.isUsed()) {
            curFolder = folder;
        }
    }

    @Override
    public int getItemCount() {
        return res == null ? 0 : res.size();
    }

    public void update(SparseArray<Folder> res) {
        this.res = res;
        notifyItemRangeInserted(0, res.size());
    }

    public Folder getCurFolder() {
        return curFolder;
    }
}
