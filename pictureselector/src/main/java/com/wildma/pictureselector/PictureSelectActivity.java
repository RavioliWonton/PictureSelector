package com.wildma.pictureselector;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;

import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

import static com.wildma.pictureselector.PictureSelectUtils.CROP;
import static com.wildma.pictureselector.PictureSelectUtils.GET_BY_ALBUM;
import static com.wildma.pictureselector.PictureSelectUtils.GET_BY_CAMERA;
import static com.wildma.pictureselector.PictureSelectUtils.crop;
import static com.wildma.pictureselector.PictureSelectUtils.cropPictureTempUri;
import static com.wildma.pictureselector.PictureSelectUtils.dealCrop;
import static com.wildma.pictureselector.PictureSelectUtils.takePictureFile;
import static com.wildma.pictureselector.PictureSelectUtils.takePictureUri;

/**
 * Author   wildma
 * Github   https://github.com/wildma
 * Date     2018/6/24
 * Desc     ${图片选择}
 */
public class PictureSelectActivity extends AppCompatActivity {

    private final int PERMISSION_CODE_FIRST = 0x14;//权限请求码
    private PictureSelectDialog mSelectPictureDialog;
    private boolean isToast = true;//是否弹吐司，为了保证for循环只弹一次
    private String picturePath = null;//最终图片路径
    public static final String CROP_WIDTH = "crop_width";
    public static final String CROP_HEIGHT = "crop_Height";
    public static final String RATIO_WIDTH = "ratio_Width";
    public static final String RATIO_HEIGHT = "ratio_Height";
    public static final String ENABLE_CROP = "enable_crop";
    private int mCropWidth;
    private int mCropHeight;
    private int mRatioWidth;
    private int mRatioHeight;
    private boolean mCropEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_select);

        mCropEnabled = getIntent().getBooleanExtra(ENABLE_CROP, true);
        mCropWidth = getIntent().getIntExtra(CROP_WIDTH, 200);
        mCropHeight = getIntent().getIntExtra(CROP_HEIGHT, 200);
        mRatioWidth = getIntent().getIntExtra(RATIO_WIDTH, 1);
        mRatioHeight = getIntent().getIntExtra(RATIO_HEIGHT, 1);

        //请求应用需要的所有权限
        boolean checkPermissionFirst = PermissionUtils.checkPermissionFirst(this, PERMISSION_CODE_FIRST,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA});
        if (checkPermissionFirst) {
            selectPicture();
        }
    }

    /**
     * 处理请求权限的响应
     *
     * @param requestCode  请求码
     * @param permissions  权限数组
     * @param grantResults 请求权限结果数组
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean isPermissions = true;
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                isPermissions = false;
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) { //用户选择了"不再询问"
                    if (isToast) {
                        Toast.makeText(this, "请手动打开该应用需要的权限", Toast.LENGTH_SHORT).show();
                        isToast = false;
                    }
                }
            }
        }
        isToast = true;
        if (isPermissions) {
            Log.d("onRequestPermission", "onRequestPermissionsResult: " + "允许所有权限");
            selectPicture();
        } else {
            Log.d("onRequestPermission", "onRequestPermissionsResult: " + "有权限不允许");
            finish();
        }
    }

    /**
     * 选择图片
     */
    public void selectPicture() {
        final ActivityResultLauncher<Intent> cropLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                dealCrop(PictureSelectActivity.this);
                if (cropPictureTempUri.getPath() != null) {
                    File file = new File(cropPictureTempUri.getPath());
                    picturePath = file.getAbsolutePath();
                }
            }
        });
        mSelectPictureDialog = new PictureSelectDialog(this, R.style.ActionSheetDialogStyle);
        mSelectPictureDialog.setOnItemClickListener(new PictureSelectDialog.OnItemClickListener() {
            @Override
            public void onItemClick(int type) {
                if (type == Constant.CAMERA) {
                    PictureSelectUtils.getByCamera(PictureSelectActivity.this, new ActivityResultCallback<Boolean>() {
                        @Override
                        public void onActivityResult(Boolean result) {
                            if (!result) finish();
                            getPicturePath(cropLauncher, GET_BY_CAMERA, takePictureUri, mCropEnabled, mCropWidth, mCropHeight, mRatioWidth, mRatioHeight);
                            handlePicturePath();
                        }
                    });
                } else if (type == Constant.ALBUM) {
                    PictureSelectUtils.getByAlbum(PictureSelectActivity.this, new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            if (result.getResultCode() == RESULT_CANCELED) finish();
                            getPicturePath(cropLauncher, GET_BY_ALBUM, result.getData().getData(), mCropEnabled, mCropWidth, mCropHeight, mRatioWidth, mRatioHeight);
                            handlePicturePath();
                        }
                    });
                } else if (type == Constant.CANCEL) {
                    finish();
                    PictureSelectActivity.this.overridePendingTransition(0, R.anim.activity_out);//activity延迟150毫秒退出，为了执行完Dialog退出的动画
                }
            }
        });
    }

    /**
     * 处理拍照或相册获取的图片
     *
     * @param requestCode 请求类型
     * @param uri        图片数据
     * @param cropEnabled 是否裁剪
     * @param w           输出宽
     * @param h           输出高
     * @param aspectX     宽比例
     * @param aspectY     高比例
     */
    public void getPicturePath(ActivityResultLauncher<Intent> launcher, int requestCode, Uri uri,
                                 boolean cropEnabled, int w, int h, int aspectX, int aspectY) {
        switch (requestCode) {
            case GET_BY_ALBUM:
                if (cropEnabled) {
                    launcher.launch(crop(this, uri, w, h, aspectX, aspectY), ActivityOptionsCompat.makeBasic());
                } else {
                    picturePath = ImageUtils.getImagePath(this, uri);
                }
                break;
            case GET_BY_CAMERA:
                if (cropEnabled) {
                    launcher.launch(crop(this, uri, w, h, aspectX, aspectY), ActivityOptionsCompat.makeBasic());
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        picturePath = ImageUtils.getImagePath(this, uri);
                    } else {
                        picturePath = takePictureFile.getAbsolutePath();
                    }
                }
                /*Android Q 以下发送广播通知图库更新，Android Q 以上使用 insert 的方式则会自动更新*/
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(takePictureFile)));
                }
                break;
        }
    }

    private void handlePicturePath() {
        if (!TextUtils.isEmpty(picturePath)) {
            PictureBean bean = new PictureBean();
            bean.setPath(picturePath);
            bean.setCut(mCropEnabled);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                bean.setUri(ImageUtils.getImageUri(this, picturePath));
            } else {
                bean.setUri(Uri.fromFile(new File(picturePath)));
            }

            Intent intent = new Intent();
            intent.putExtra(PictureSelector.PICTURE_RESULT, bean);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

}
