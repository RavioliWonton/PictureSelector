package com.wildma.wildmaselectpicture;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.wildma.pictureselector.PictureBean;
import com.wildma.pictureselector.PictureSelector;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {
    public static final String    TAG = "PictureSelector";
    private             ImageView mIvImage;
    private final ActivityResultCallback<ActivityResult> callback = new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getData() != null) {
                PictureBean pictureBean = result.getData().getParcelableExtra(PictureSelector.PICTURE_RESULT);
                Log.i(TAG, "是否裁剪: " + pictureBean.isCut());
                Log.i(TAG, "原图地址: " + pictureBean.getPath());
                Log.i(TAG, "图片 Uri: " + pictureBean.getUri());
                if (pictureBean.isCut()) {
                    mIvImage.setImageBitmap(BitmapFactory.decodeFile(pictureBean.getPath()));
                } else {
                    mIvImage.setImageURI(pictureBean.getUri());
                }

                //使用 Glide 加载图片
                            /*Glide.with(this)
                            .load(pictureBean.isCut() ? pictureBean.getPath() : pictureBean.getUri())
                            .apply(RequestOptions.centerCropTransform()).into(mIvImage);*/

                //实际开发中将图片上传到服务器成功后需要删除全部缓存图片（即裁剪后的无用图片）
                //FileUtils.deleteAllCacheImage(this);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mIvImage = (ImageView) findViewById(R.id.iv_image);
    }

    /**
     * 不裁剪
     */
    public void selectPicture(View view) {
        PictureSelector
                .create(MainActivity.this, callback)
                .selectPicture(false);
    }

    /**
     * 自由裁剪
     */
    public void selectPicture2(View view) {
        PictureSelector
                .create(MainActivity.this, callback)
                .selectPicture(true);
    }

    /**
     * 指定宽高及宽高比例裁剪
     */
    public void selectPicture3(View view) {
        PictureSelector
                .create(MainActivity.this, callback)
                .selectPicture(true, 200, 200, 1, 1);
    }

}
