package com.wildma.pictureselector;

import android.content.Intent;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

/**
 * Author       wildma
 * Github       https://github.com/wildma
 * Date         2018/6/24
 * Desc	        ${图片选择器}
 */
public class PictureSelector {

    public static final String PICTURE_RESULT = "picture_result";//选择的图片结果
    private final ActivityResultLauncher<Intent> launcher;
    private final Intent intent;
    private final ActivityOptionsCompat animation;

    private PictureSelector(final ComponentActivity activity, final ActivityResultCallback<ActivityResult> callback) {
        if (activity.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
            throw new RuntimeException("Initialization too late, must call before activity's onStart().");
        launcher = activity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), callback);
        intent = new Intent(activity, PictureSelectActivity.class);
        animation = ActivityOptionsCompat.makeCustomAnimation(activity, R.anim.activity_out, R.anim.activity_out);
    }

    private PictureSelector(final Fragment fragment, final ActivityResultCallback<ActivityResult> callback) {
        if (fragment.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
            throw new RuntimeException("Initialization too late, must call before fragment's onStart().");
        launcher = fragment.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), callback);
        intent = new Intent(fragment.requireActivity(), PictureSelectActivity.class);
        animation = ActivityOptionsCompat.makeCustomAnimation(fragment.requireContext(), R.anim.activity_out, R.anim.activity_out);
    }

    private PictureSelector(final ComponentActivity activity, final ActivityResultLauncher<Intent> launcher) {
        this.launcher = launcher;
        intent = new Intent(activity, PictureSelectActivity.class);
        animation = ActivityOptionsCompat.makeCustomAnimation(activity, R.anim.activity_out, R.anim.activity_out);
    }

    private PictureSelector(final Fragment fragment, final ActivityResultLauncher<Intent> launcher) {
        this.launcher = launcher;
        intent = new Intent(fragment.requireActivity(), PictureSelectActivity.class);
        animation = ActivityOptionsCompat.makeCustomAnimation(fragment.requireContext(), R.anim.activity_out, R.anim.activity_out);
    }

    /**
     * 创建 PictureSelector（用于 Activity），使用{@link ActivityResultCallback}
     * 必须于{@link LifecycleOwner}的生命周期STARTED之前调用，不然将抛出{@link RuntimeException}
     *
     * @param activity Activity
     * @param callback 结果回调
     * @return PictureSelector
     * @throws RuntimeException 当在{@link LifecycleOwner}的生命周期STARTED之后调用
     */
    public static PictureSelector create(ComponentActivity activity, ActivityResultCallback<ActivityResult> callback) {
        return new PictureSelector(activity, callback);
    }

    /**
     * 创建 PictureSelector（用于 Fragment），使用{@link ActivityResultLauncher}（方便依赖注入）
     *
     * @param fragment    Fragment
     * @param launcher 含有回调的启动器
     * @return PictureSelector
     */
    public static PictureSelector create(Fragment fragment, ActivityResultLauncher<Intent> launcher) {
        return new PictureSelector(fragment, launcher);
    }

    /**
     * 创建 PictureSelector（用于 Activity），使用{@link ActivityResultLauncher}（方便依赖注入）
     *
     * @param activity Activity
     * @param launcher 含有回调的启动器
     * @return PictureSelector
     */
    public static PictureSelector create(ComponentActivity activity, ActivityResultLauncher<Intent> launcher) {
        return new PictureSelector(activity, launcher);
    }

    /**
     * 创建 PictureSelector（用于 Fragment），使用{@link ActivityResultCallback}
     * 必须于{@link LifecycleOwner}的生命周期STARTED之前调用，不然将抛出{@link RuntimeException}
     *
     * @param fragment Fragment
     * @param callback 结果回调
     * @return PictureSelector
     * @throws RuntimeException 当在{@link LifecycleOwner}的生命周期STARTED之后调用
     */
    public static PictureSelector create(Fragment fragment, ActivityResultCallback<ActivityResult> callback) {
        return new PictureSelector(fragment, callback);
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
        launcher.launch(intent, animation);
    }
}

