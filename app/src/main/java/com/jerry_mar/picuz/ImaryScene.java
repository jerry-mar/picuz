package com.jerry_mar.picuz;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.jerry_mar.picuz.adapter.FolderAdapter;
import com.jerry_mar.picuz.adapter.ImageAdapter;
import com.jerry_mar.picuz.config.Config;

public class ImaryScene {
    private View root;
    private LayoutInflater inflater;

    private View view;
    private TextView submit;
    private TextView folder;
    private ImageView real;
    private RecyclerView imageList;
    private RecyclerView folderList;

    public ImaryScene(Context context, int id) {
        inflater = LayoutInflater.from(context);
        root = inflater.inflate(id, null);
    }

    public View getView() {
        return root;
    }

    public void initView(Config config) {
        submit = (TextView) root.findViewById(R.id.picuz_submit);
        folder = (TextView) root.findViewById(R.id.folder_name);
        real = (ImageView) root.findViewById(R.id.picuz_real) ;
        imageList = (RecyclerView) root.findViewById(R.id.picuz_list);
        imageList.setLayoutManager(new GridLayoutManager(inflater.getContext(), 4));
        folderList = (RecyclerView) root.findViewById(R.id.folder_list);
        folderList.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        if (config.isForce()) {
            submit.setVisibility(View.GONE);
        } else {
            setSelectNum(0, config.getMax());
        }
    }

    public Boolean showFolder(Boolean show, View view) {
        this.view = view;
        if (show) {
            folderList.setAnimation(anim(false));
            folderList.setVisibility(View.GONE);
            return Boolean.FALSE;
        } else {
            folderList.setAnimation(anim(true));
            folderList.setVisibility(View.VISIBLE);
            return Boolean.TRUE;
        }
    }

    private Animation anim(boolean show) {
        Animation a;
        if (show) {
            a = AnimationUtils.loadAnimation(inflater.getContext(),
                    R.anim.picuz_show);
        } else {
            a = AnimationUtils.loadAnimation(inflater.getContext(),
                    R.anim.picuz_hide);
        }

        a.setInterpolator(new DecelerateInterpolator());
        a.setStartTime(AnimationUtils.currentAnimationTimeMillis());
        return a;
    }

    public void setFolderAdapter(FolderAdapter adapter) {
        folderList.setAdapter(adapter);
    }

    public void setImageAdapter(ImageAdapter imageAdapter) {
        imageList.setAdapter(imageAdapter);
    }

    public void hideFolder() {
        view.performClick();
    }

    public void setSelectNum(int selected, int max) {
        submit.setText("完成(" + selected+ "/" + max + ")");
    }

    public boolean real(Boolean real) {
        this.real.setImageResource(real ? R.drawable.picuz_real : R.drawable.picuz_really);
        return !real;
    }

    public void setFolderName(String folderName) {
        folder.setText(folderName);
    }
}
