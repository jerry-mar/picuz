package com.jerry_mar.picuz;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.jerry_mar.picuz.adapter.FolderAdapter;
import com.jerry_mar.picuz.adapter.ImageAdapter;
import com.jerry_mar.picuz.config.Config;
import com.jerry_mar.picuz.model.Folder;
import com.jerry_mar.picuz.model.Image;
import com.jerry_mar.picuz.utils.FileUtils;
import com.jerry_mar.picuz.utils.ImageDataSource;
import com.jerry_mar.picuz.utils.PermissionUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ImaryController extends Activity implements
        ImageDataSource.Callback, PermissionUtils.Callback {
    public static final String[] STORAGE = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public static final String[] CAMERA = new String[]{
            Manifest.permission.CAMERA,
    };
    public static final int STORAGE_CODE = 0x01;
    public static final int CAMERA_CODE = 0x10;

    private ImaryScene scene;
    private FolderAdapter folderAdapter;
    private ImageAdapter imageAdapter;
    private ImageDataSource source;
    private Config config;

    private List<Image> result;
    private Intent cameraIntent;
    private File cameraFile;

    public ImaryController() {
        result = new ArrayList();
        source = new ImageDataSource(this, this);
    }

    @Override
    protected void onCreate(Bundle bundle) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(bundle);
        scene = new ImaryScene(this, R.layout.scene_imary);
        config = (Config) getIntent().getSerializableExtra(Config.CONFIG);
        if (config == null) {
            config = new Config();
        }
        setContentView(scene.getView());
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        scene.initView(config);
        scene.setFolderAdapter(folderAdapter = new FolderAdapter(LayoutInflater.from(this)));
        scene.setImageAdapter(imageAdapter = new ImageAdapter(LayoutInflater.from(this), false));
        PermissionUtils.requestPermission(this, STORAGE, STORAGE_CODE, this);
        PermissionUtils.requestPermission(this, CAMERA, CAMERA_CODE, this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] result) {
        boolean allow = false;
        for (int code : result) {
            if (code == PackageManager.PERMISSION_GRANTED) {
                allow = true;
                break;
            }
        }
        if (allow) {
            onFinish(requestCode + RESULT_OK, null);
        } else {
            onFinish(requestCode, null);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            onFinish(requestCode + resultCode, data);
        }
    }

    @Override
    protected void onDestroy() {
        ImageDataSource.clear();
        super.onDestroy();
        System.gc();
    }

    private void initCamera() {
        cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
                cameraFile = new File(Environment.getExternalStorageDirectory(), "/DCIM/camera/");
            else
                cameraFile = Environment.getDataDirectory();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.CHINA);
            StringBuffer buff = new StringBuffer("IMG_");
            buff.append(dateFormat.format(new Date(System.currentTimeMillis())))
                    .append(".jpg");
            cameraFile = FileUtils.createFile(cameraFile, buff.toString());
            if (cameraFile != null) {
                Uri uri;
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M){
                    uri = Uri.fromFile(cameraFile);
                }else{
                    ActivityInfo info = null;
                    try {
                        info = this.getPackageManager()
                                .getActivityInfo(getComponentName(),
                                        PackageManager.GET_META_DATA);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    String provider = info.metaData.getString("provider");
                    uri = FileProvider.getUriForFile(this, provider, cameraFile);
                }
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            }
        }
    }

    public void back(View view) {
        finish();
    }

    public void showFolder(View view) {
        Boolean show = (Boolean) view.getTag();
        if (show == null)
            show = Boolean.FALSE;
        view.setTag(scene.showFolder(show, view));
    }

    public void selectFolder(View view) {
        Folder folder = (Folder) view.getTag();
        imageAdapter.update(folder.getImages());
        folderAdapter.getCurFolder().setUsed(false);
        folder.setUsed(true);
        folderAdapter.notifyDataSetChanged();
        scene.hideFolder();
        scene.setFolderName(folder.getName());
    }

    public void selectImage(View view) {
        Image image = (Image) view.getTag();
        if (image == null) {
            if (cameraIntent != null) {
                startActivityForResult(cameraIntent, 0xF1);
            } else {
                Toast.makeText(this, "请添加照相权限",
                        Toast.LENGTH_SHORT).show();
            }
            return;
        }
        if (config.isForce()) {
            gotoOperator(image);
            return;
        }
        if (result.remove(image)) {
            image.setUsed(false);
        } else {
            if (config.getMax() > result.size()) {
                image.setUsed(true);
                result.add(image);
            } else {
                Toast.makeText(this, "已选择最大数量",
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }
        scene.setSelectNum(result.size(), config.getMax());
        imageAdapter.notifyItemChanged(image.getIndex());
    }

    public void real(View view) {
        Boolean real = (Boolean) view.getTag();
        if (real == null)
            real = Boolean.FALSE;
        real = scene.real(real);
        view.setTag(real);
        config.setReal(real);
    }

    public void submit(View view) {
        if (result.size() == 0) {
            Toast.makeText(this, "请选择图片",
                    Toast.LENGTH_SHORT).show();
        } else {
            gotoPreview(null);
        }
    }

    private void gotoOperator(Image image) {
        Intent intent = new Intent(this, OperatorController.class);
        intent.putExtra("width", config.getWidth());
        intent.putExtra("height", config.getHeight());
        intent.putExtra("shape", config.getShape());
        intent.putExtra("path", image.getPath());
        startActivityForResult(intent, 0xFA);
    }

    private void gotoPreview(Image image) {
        if (image != null) {
            result.clear();
            result.add(image);
        }
        Intent intent = new Intent(this, PreviewController.class);
        config.setImages(result);
        intent.putExtra(Config.CONFIG, config);
        startActivityForResult(intent, 0xFA);
    }

    @Override
    public void onFinish(SparseArray<Folder> result) {
        if (result.size() > 0) {
            imageAdapter.update(result.get(0).getImages());
        }
        folderAdapter.update(result);
    }

    @Override
    public void onFinish(int code, Intent intent) {
        switch (code) {
            case STORAGE_CODE + RESULT_OK : {
                source.scan(null);
            }
            break;
            case STORAGE_CODE : {
                Toast.makeText(this, "权限被禁止，无法选择本地图片",
                        Toast.LENGTH_SHORT).show();
            }
            break;
            case CAMERA_CODE + RESULT_OK : {
                initCamera();
            }
            break;
            case CAMERA_CODE : {
                Toast.makeText(this, "权限被禁止，无法打开相机",
                        Toast.LENGTH_SHORT).show();
            }
            break;
            case 0xF0 : {
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(cameraFile);
                mediaScanIntent.setData(contentUri);
                sendBroadcast(mediaScanIntent);
                Image image = new Image();
                image.setPath(cameraFile.getAbsolutePath());
                if (!config.isForce()) {
                    gotoPreview(image);
                } else {
                    gotoOperator(image);
                }
            }
            break;
            case 0xF9 : {
                setResult(RESULT_OK, intent);
                finish();
            }
            break;
        }
    }
}
