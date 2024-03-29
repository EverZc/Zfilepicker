package me.pandazhang.filepicker.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.LogUtils;

import me.pandazhang.filepicker.DividerGridItemDecoration;
import me.pandazhang.filepicker.FilePicker;
import me.pandazhang.filepicker.R;
import me.pandazhang.filepicker.adapter.ImagePickAdapter;
import me.pandazhang.filepicker.adapter.OnSelectStateListener;
import me.pandazhang.filepicker.filter.FileFilter;
import me.pandazhang.filepicker.filter.callback.FilterResultCallback;
import me.pandazhang.filepicker.filter.entity.Directory;
import me.pandazhang.filepicker.filter.entity.ImageFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static me.pandazhang.filepicker.FilePicker.RESULT_PICK_IMAGE;

/**
 * 图片浏览列表 发布文章
 * Created by Zwj
 * Date: 2019/07/05
 */

public class ImagePickActivityPicker extends PickerBaseActivity {
    public static final String IS_NEED_CAMERA = "IsNeedCamera";
    public static final int DEFAULT_MAX_NUMBER = 9; //最大放置图片的数量
    public static final int COLUMN_NUMBER = 4;  //列的数量
    private int mMaxNumber;
    private int mCurrentNumber = 0;
    private Toolbar mTbImagePick;
    private RecyclerView mRecyclerView;
    private ImagePickAdapter mAdapter;
    private boolean isNeedCamera;
    private ArrayList<ImageFile> mSelectedList = new ArrayList<>(); //选择的图片的集合
    private long imageSize;
    private TextView mAlbum; //相簿
    private TextView mPreview; //点击预览
    private TextView mConfirm;
    private int[] mWithAspectRatio;
    private TextView mTitle;
    private boolean isload = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_image_pick);
        mMaxNumber = getIntent().getIntExtra(FilePicker.MAX_NUMBER, DEFAULT_MAX_NUMBER);
        isNeedCamera = getIntent().getBooleanExtra(IS_NEED_CAMERA, false);
        mWithAspectRatio = getIntent().getIntArrayExtra(FilePicker.WITH_ASPECT_RATIO);
        //已经选中了的图片
        ArrayList<ImageFile> selecteds = getIntent().getParcelableArrayListExtra(RESULT_PICK_IMAGE);
        if (selecteds != null) {
            mSelectedList.addAll(selecteds);
        }
        initView();
        super.onCreate(savedInstanceState);
    }

    @Override
    void permissionGranted() {
        if (isload) {
            //loadData();
        }
    }

    private void initView() {
        mTbImagePick = (Toolbar) findViewById(R.id.tb_image_pick);
        mTitle = (TextView) findViewById(R.id.tv_title);
        mTbImagePick.setTitle("");
        //显示当前的数量
        if (mMaxNumber > 9) {
            mTitle.setText(" ");
        } else {
            mTitle.setText(mCurrentNumber + "/" + mMaxNumber);
        }
        setSupportActionBar(mTbImagePick);
        mTbImagePick.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mAlbum = (TextView) findViewById(R.id.btn_album);
        mAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //打开相册
                Intent intent = new Intent(ImagePickActivityPicker.this, ImageAlbumActivity.class);
                intent.putParcelableArrayListExtra(FilePicker.RESULT_PICK_IMAGE, mSelectedList);
                startActivityForResult(intent, FilePicker.REQUEST_CODE_ALBUM_IMAGE);
                loadData();
            }
        });
        mConfirm = (TextView) findViewById(R.id.btn_confrim);
        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putParcelableArrayListExtra(RESULT_PICK_IMAGE, mSelectedList);
                for (int i = 0; i < mSelectedList.size(); i++) {
                    Log.e("点击了确认 :", mSelectedList.get(i).getPath());
                }
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        mPreview = (TextView) findViewById(R.id.btn_preview);
        mPreview.setEnabled(false);
        mPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageBrowserMoreActivity.launchActivity(ImagePickActivityPicker.this, mSelectedList.size(), mSelectedList, mWithAspectRatio);
            }
        });
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_image_pick);
        GridLayoutManager layoutManager = new GridLayoutManager(this, COLUMN_NUMBER);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new DividerGridItemDecoration(this));
        mAdapter = new ImagePickAdapter(this, isNeedCamera, mMaxNumber);
        mAdapter.setCurrentNumber(mCurrentNumber);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnSelectStateListener(new OnSelectStateListener<ImageFile>() {
            @Override
            public void OnSelectStateChanged(boolean state, ImageFile file) {
                if (mMaxNumber == 1) {//单选
                    if (state) {
                        mSelectedList.clear();
                        mSelectedList.add(file);
                    } else {
                        mSelectedList.remove(file);
                    }
                    mCurrentNumber = state ? 1 : 0;
                    mTitle.setText(mCurrentNumber + "/" + mMaxNumber);
                } else if (mMaxNumber > 9) {
                    if (state) {
                        imageSize = imageSize + file.getSize();
                        mSelectedList.add(file);
                        mCurrentNumber++;
                        if (mCurrentNumber == mMaxNumber) {
                            Toast.makeText(ImagePickActivityPicker.this, "已选中的图片太多,请确认以后继续添加图片",
                                    Toast.LENGTH_LONG).show();
                        }
                        Log.e("imageSize  : ", imageSize / 1024 + "KB");
                    } else {
                        imageSize = imageSize - file.getSize();
                        mSelectedList.remove(file);
                        mCurrentNumber--;
                        Log.e("imageSize  : ", imageSize / 1024 + "KB");
                    }
                    mTitle.setText(" ");

                } else if (mMaxNumber == 9) {
                    if (state) {
                        imageSize = imageSize + file.getSize();
                        mSelectedList.add(file);
                        mCurrentNumber++;

                    } else {
                        imageSize = imageSize - file.getSize();
                        mSelectedList.remove(file);
                        mCurrentNumber--;
                    }
                    mTitle.setText(mCurrentNumber + "/" + mMaxNumber);
                } else {
                    if (state) {
                        imageSize = imageSize + file.getSize();
                        mSelectedList.add(file);
                        mCurrentNumber++;
                        Log.e("path OnSelectState : ", file.getPath());
                    } else {
                        imageSize = imageSize - file.getSize();
                        mSelectedList.remove(file);
                        mCurrentNumber--;
                    }
                    mTitle.setText(mCurrentNumber + "/" + mMaxNumber);
                    if (mCurrentNumber == mMaxNumber) {

                    }
                }
                mConfirm.setEnabled(mSelectedList.size() > 0);
                mPreview.setEnabled(mSelectedList.size() > 0);
            }
        });
        loadData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case FilePicker.REQUEST_CODE_TAKE_IMAGE:
                LogUtils.e("REQUEST_CODE_TAKE_IMAGE");
                if (resultCode == RESULT_OK) {
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    File file = new File(mAdapter.mImagePath);
                    Uri contentUri = Uri.fromFile(file);
                    mediaScanIntent.setData(contentUri);
                    sendBroadcast(mediaScanIntent);
                    loadDataPhone();
                }
                break;
            case FilePicker.REQUEST_CODE_BROWSER_IMAGE:
                LogUtils.e("REQUEST_CODE_BROWSERE");
                if (resultCode == RESULT_OK) {
                    setResult(RESULT_OK, data);
                    finish();
                }
            case FilePicker.REQUEST_CODE_ALBUM_IMAGE:
                LogUtils.e("REQUEST_CODE_ALBUM");
                if (resultCode == RESULT_OK) {
                    ArrayList<ImageFile> list = data.getParcelableArrayListExtra(RESULT_PICK_IMAGE);
                    LogUtils.e("list:", list.size() + "");
                    mData.clear();
                    mData.addAll(list);


                }
                break;
        }
    }

    private  List<ImageFile> mData = new ArrayList<>();
    private void loadData() {
        LogUtils.e("执行 loadData 来了");
         FileFilter.getImages(this, new FilterResultCallback<ImageFile>() {
            @Override
            public void onResult(List<Directory<ImageFile>> directories) {
                LogUtils.e("onResult也执行了哈:" + directories.size() + "");
                //只显示相机拍照的图片
               /* for (int i = 0; i < directories.size(); i++) {
                    if (i == 0) {
                        Directory<ImageFile> directory=directories.get(0);
                        mData.addAll(directory.getFiles());
                    }
                }*/
                //以下注释 是 打开相册添加所有图片
                if (mData.size()==0){
                    for (Directory<ImageFile> directory : directories) {
                        mData.addAll(directory.getFiles());
                    }
                }

//                if (mSelectedList != null) {
//                    for (int i = 0; i < mSelectedList.size(); i++) {
//                        ImageFile imageFile = mSelectedList.get(i);
//                        for (int j = 0; j < list.size(); j++) {
//                            ImageFile imageFile1 = list.get(i);
//                            if (TextUtils.equals(imageFile.getPath(), imageFile1.getPath())) {
//                                mCurrentNumber++;
//                                imageFile1.setSelected(true);
//                            }
//                        }
//                    }
//                }
                Collections.sort(mData, new FileComparator()); //这个方法以后fileList里的数据是已经排好序的
                mAdapter.refresh(mData);
            }
        });
    }

    private void loadDataPhone() {
        LogUtils.e("执行 loadData 来了");
        FileFilter.getImages(this, new FilterResultCallback<ImageFile>() {
            @Override
            public void onResult(List<Directory<ImageFile>> directories) {
                LogUtils.e("onResult也执行了哈:" + directories.size() + "");
                //只显示相机拍照的图片
               /* for (int i = 0; i < directories.size(); i++) {
                    if (i == 0) {
                        Directory<ImageFile> directory=directories.get(0);
                        mData.addAll(directory.getFiles());
                    }
                }*/
                //以下注释 是 打开相册添加所有图片

                    for (Directory<ImageFile> directory : directories) {
                        mData.addAll(directory.getFiles());
                    }


//                if (mSelectedList != null) {
//                    for (int i = 0; i < mSelectedList.size(); i++) {
//                        ImageFile imageFile = mSelectedList.get(i);
//                        for (int j = 0; j < list.size(); j++) {
//                            ImageFile imageFile1 = list.get(i);
//                            if (TextUtils.equals(imageFile.getPath(), imageFile1.getPath())) {
//                                mCurrentNumber++;
//                                imageFile1.setSelected(true);
//                            }
//                        }
//                    }
//                }
                Collections.sort(mData, new FileComparator()); //这个方法以后fileList里的数据是已经排好序的
                mAdapter.refresh(mData);
            }
        });
    }


    private class FileComparator implements Comparator {
        @Override
        public int compare(Object o1, Object o2) {
            ImageFile s1 = (ImageFile) o1;
            ImageFile s2 = (ImageFile) o2;
            if (s1.getDate() < s2.getDate()) {
                return 1;//最后修改的照片在前
            } else if (s1.getDate() == s2.getDate()) {
                return 0;
            } else {
                return -1;
            }
        }
    }
}
