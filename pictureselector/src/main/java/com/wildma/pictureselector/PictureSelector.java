package com.wildma.pictureselector;

import android.content.Intent;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;

/**
 * Author       wildma
 * Github       https://github.com/wildma
 * Date         2018/6/24
 * Desc	        ${图片选择器}
 */
public class PictureSelector {

    public static final String PICTURE_RESULT  = "picture_result";//选择的图片结果
    private final ActivityResultLauncher<Intent> launcher;
    private final Intent intent;

    /**
     * 创建 PictureSelector（用于 Activity）
     *
     * @param activity    Activity
     * @param callback 结果回调
     * @return PictureSelector
     */
    public static PictureSelector create(ComponentActivity activity, ActivityResultCallback<ActivityResult> callback) {
        return new PictureSelector(activity, callback);
    }

    /**
     * 创建 PictureSelector（用于 Fragment）
     *
     * @param fragment    Fragment
     * @param callback 结果回调
     * @return PictureSelector
     */
    public static PictureSelector create(Fragment fragment, ActivityResultCallback<ActivityResult> callback) {
        return new PictureSelector(fragment, callback);
    }

    private PictureSelector(ComponentActivity activity, ActivityResultCallback<ActivityResult> callback) {
        launcher = activity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), callback);
        intent = new Intent(activity, PictureSelectActivity.class);
    }

    private PictureSelector(Fragment fragment, ActivityResultCallback<ActivityResult> callback) {
        launcher = fragment.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), callback);
        intent = new Intent(fragment.requireActivity(), PictureSelectActivity.class);
    }

    /**
     * 选择图片（默认不裁剪）
     */
    public void selectPicture() {
        selectPicture(false, 0, 0, 0, 0);
    }

    /**
     * 选择图片（根据参数决定是否裁剪）
     *
     * @param cropEnabled 是否裁剪
     */
    public void selectPicture(boolean cropEnabled) {
        selectPicture(cropEnabled, 0, 0, 0, 0);
    }

    /**
     * 选择图片（指定宽高及宽高比例裁剪）
     *
     * @param cropEnabled 是否裁剪
     * @param cropWidth   裁剪宽
     * @param cropHeight  裁剪高
     * @param ratioWidth  宽比例
     * @param ratioHeight 高比例
     */
    public void selectPicture(boolean cropEnabled, int cropWidth, int cropHeight, int ratioWidth, int ratioHeight) {
        intent.putExtra(PictureSelectActivity.ENABLE_CROP, cropEnabled);
        intent.putExtra(PictureSelectActivity.CROP_WIDTH, cropWidth);
        intent.putExtra(PictureSelectActivity.CROP_HEIGHT, cropHeight);
        intent.putExtra(PictureSelectActivity.RATIO_WIDTH, ratioWidth);
        intent.putExtra(PictureSelectActivity.RATIO_HEIGHT, ratioHeight);
        launcher.launch(intent, ActivityOptionsCompat.makeBasic());
    }
}

