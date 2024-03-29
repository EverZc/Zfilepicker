package me.pandazhang.filepicker.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bm.library.PhotoView;
import com.bumptech.glide.Glide;
import me.pandazhang.filepicker.FilePicker;
import me.pandazhang.filepicker.R;
import me.pandazhang.filepicker.Util;
import me.pandazhang.filepicker.filter.entity.ImageFile;



import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * 选择单张以后，裁剪单张，常用于选择头像，修改头像操作
 * Created by Zwj
 * Date: 2019/07/05
 */

public class ImageBrowserOneActivity extends PickerBaseActivity {
    public static final String IMAGE_BROWSER_INIT_INDEX = "ImageBrowserInitIndex";
    public static final String IMAGE_BROWSER_LIST = "ImageBrowserList";
    public static final String IMAGE_BROWSER_SELECTED_NUMBER = "ImageBrowserSelectedNumber";
    private int mMaxNumber;
    private ViewPager mViewPager;
    private Toolbar mTbImagePick;
    private ArrayList<ImageFile> mSelectedList = new ArrayList<>();
    private int mCurrentIndex = 1;
    private TextView mConfrimView;
    private TextView mCropView;
    private ImageFile mCropImage;
    private ImageBrowserAdapter mAdapter;
    private int[] mWithAspectRatio;
    private LinearLayout ll_container;

    public static void launchActivity(FragmentActivity activity, int maxNumber, ArrayList<ImageFile> selectList, int[] withAspectRatio){
        Intent intent = new Intent(activity, ImageBrowserOneActivity.class);
        intent.putExtra(FilePicker.MAX_NUMBER, maxNumber);
        intent.putParcelableArrayListExtra(IMAGE_BROWSER_LIST, selectList);
        intent.putExtra(FilePicker.WITH_ASPECT_RATIO, withAspectRatio);
        activity.startActivityForResult(intent, FilePicker.REQUEST_CODE_BROWSER_IMAGE);
    }

    @Override
    void permissionGranted() {
        initView();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        setContentView(R.layout.activity_image_browser);
        mMaxNumber = getIntent().getIntExtra(FilePicker.MAX_NUMBER, ImagePickActivityPicker.DEFAULT_MAX_NUMBER);
        mSelectedList = getIntent().getParcelableArrayListExtra(IMAGE_BROWSER_LIST);
        mWithAspectRatio = getIntent().getIntArrayExtra(FilePicker.WITH_ASPECT_RATIO);
        super.onCreate(savedInstanceState);
        intoPick();
        initView();
    }

    private void initView() {
        ll_container= (LinearLayout) findViewById(R.id.ll_container);
        //ll_container.getBackground().setAlpha(51);
        mTbImagePick = (Toolbar) findViewById(R.id.tb_image_pick);
        mTbImagePick.setTitle("  ");
        //mTbImagePick.setTitle(mCurrentIndex + "/" + mMaxNumber);
        setSupportActionBar(mTbImagePick);
        mTbImagePick.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mConfrimView = (TextView) findViewById(R.id.btn_confrim);
        mConfrimView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putParcelableArrayListExtra(FilePicker.RESULT_PICK_IMAGE, mSelectedList);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        mCropView = (TextView) findViewById(R.id.btn_crop);
        mCropView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intoPick();
            }
        });
        mViewPager = (ViewPager) findViewById(R.id.vp_image_pick);
        mViewPager.setPageMargin((int) (getResources().getDisplayMetrics().density * 15));
        mAdapter = new ImageBrowserAdapter();
        mViewPager.setAdapter(mAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mCurrentIndex = position + 1;
               // mTbImagePick.setTitle(mCurrentIndex + "/" + mMaxNumber);
                mTbImagePick.setTitle("  ");
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void intoPick(){
        mCropImage = mSelectedList.get(mViewPager.getCurrentItem());
        Uri sourceUri = Util.path2Uri(ImageBrowserOneActivity.this, mCropImage.getPath());

        //裁剪后保存到文件中
        // TODO 路径需要修改
        String path = mCropImage.getPath();
        String destination =  path.substring(path.lastIndexOf("/") + 1) + "crop_.jpeg";
        Uri destinationUri = Uri.fromFile(new File(getCacheDir(), destination));
        /*UCrop uCrop = UCrop.of(sourceUri, destinationUri);
        UCrop.Options options = new UCrop.Options();
        if (mWithAspectRatio != null) {
            uCrop.withAspectRatio(mWithAspectRatio[0], mWithAspectRatio[1]);
        }else{
            options.setFreeStyleCropEnabled(true);
        }
        options.setHideBottomControls(true);
        options.setToolbarTitle(" ");
        options.setAllowedGestures(UCropActivity.SCALE, UCropActivity.NONE, UCropActivity.ALL);
        options.setToolbarColor(getResources().getColor(R.color.black));
        options.setStatusBarColor(getResources().getColor(R.color.black));
        uCrop.withOptions(options);
        uCrop.start(ImageBrowserOneActivity.this);*/
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK){
            Uri result = UCrop.getOutput(data);
            if (result != null){
                String newPath = result.getPath();
                if (mCropImage != null) {
                    mCropImage.setPath(newPath);//这里只修改了路径
                    mAdapter.notifyDataSetChanged();
                    Intent intent = new Intent();
                    intent.putParcelableArrayListExtra(FilePicker.RESULT_PICK_IMAGE, mSelectedList);
                    setResult(RESULT_OK, intent);
                    finish();
                    try {
                        String s = MediaStore.Images.Media.insertImage(getContentResolver(), newPath, "", "");
                        //Log.e("onActivityResult", s);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }*/
    }

    private class ImageBrowserAdapter extends PagerAdapter {
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            PhotoView view = new PhotoView(ImageBrowserOneActivity.this);
            view.enable();
            view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            Glide.with(ImageBrowserOneActivity.this)
                    .load(mSelectedList.get(position).getPath())
                    .into(view);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return mSelectedList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_image_pick, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_done) {
//            finishThis();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
