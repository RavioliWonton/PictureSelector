[![](https://jitpack.io/v/RavioliWonton/PictureSelector.svg)](https://jitpack.io/#RavioliWonton/PictureSelector)

# PictureSelector
Android 图片选择器（仿 IOS 图片选择控件）· 改

## 效果图（与原版完全一致）

![效果图.jpg](https://upload-images.jianshu.io/upload_images/5382223-9d82fb9c0f22bfb2.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

## 功能特点
- 支持通过拍照获取图片
- 支持通过相册获取图片
- 支持图片是否裁剪两i种场景R
- 支持仿 iOS 底部弹出选择菜单 ActionSheet 效果
- 适配 Android R 版本
- 适配 AndroidX 中的 [ActivityResult API](https://developer.android.com/jetpack/androidx/releases/activity#1.2.0-alpha02)

## 使用
### Step 1. 添加 JitPack 仓库
在项目的 build.gradle 添加 JitPack 仓库
```java
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```
### Step 2. 添加依赖
在需要使用的 module 中添加依赖  
注意：从 2.0.0 版本开始，项目迁移到 AndroidX。如果你的项目还未迁移到 AndroidX，可以使用 1.2.0 版本，同时由于原支持库停止更新，故不支持 ActivityResult API。
```java
dependencies {
	implementation 'com.github.RavioliWonton:PictureSelector:3.0.0-beta01'
}
```
### Step 3. 创建 PictureSelector 对象
```java
private final ActivityResultCallback<ActivityResult> callback = new ActivityResultCallback<ActivityResult>() {
    @Override
    public void onActivityResult(ActivityResult result) {
        if (result.getData() != null) {
           PictureBean pictureBean = result.getData().getParcelableExtra(PictureSelector.PICTURE_RESULT);
           if (pictureBean.isCut()) {
               mIvImage.setImageBitmap(BitmapFactory.decodeFile(pictureBean.getPath()));
           } else {
               mIvImage.setImageURI(pictureBean.getUri());
           }

           //使用 Glide 或者你喜欢的图片加载框架加载图片
           /*Glide.with(this)
               .load(pictureBean.isCut() ? pictureBean.getPath() : pictureBean.getUri())
               .apply(RequestOptions.centerCropTransform()).into(mIvImage);*/
       }
   }
};

// 在 Activity / Fragment 的 onStart() 方法之前（推荐onCreate() (Activity) / onViewCreated() (Fragment)）创建 PictureSelector 对象。
private PictureSelector selector;
@Override
protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ...
        selector = PictureSelector.create(MainActivity.this, callback);
}
```
或
```java
// 直接在 Activity / Fragment 的 onStart() 方法之前（推荐onCreate() (Activity) / onViewCreated() (Fragment)）创建 ActivityResultLauncher 对象。
private ActivityResultLauncher<Intent> launcher;
@Override
protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ...
        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
    		@Override
    		public void onActivityResult(ActivityResult result) {
        		if (result.getData() != null) {
           		PictureBean pictureBean = result.getData().getParcelableExtra(PictureSelector.PICTURE_RESULT);
           		if (pictureBean.isCut()) {
               			mIvImage.setImageBitmap(BitmapFactory.decodeFile(pictureBean.getPath()));
           		} else {
               			mIvImage.setImageURI(pictureBean.getUri());
           		}

           		//使用 Glide 或者你喜欢的图片加载框架加载图片
           		/*Glide.with(this)
               		.load(pictureBean.isCut() ? pictureBean.getPath() : pictureBean.getUri())
               		.apply(RequestOptions.centerCropTransform()).into(mIvImage);*/
       		}
   	    }
	});
}
```
请注意，ActivityResult API 不再以请求码分辨 Activity 结果回调，你可以为不同的场景或调用设置不同的回调，同时建议在onCreate及之前的方法中设置回调。详情请参考[官方文档](https://developer.android.com/training/basics/intents/result)。
### Step 4. 拍照或者从相册选择图片
**使用场景：**

***创建 PictureSelector 的情况下***
- 不裁剪
```java
selector.selectPicture(false);
```

- 自由裁剪
```java
selector.selectPicture(true);
```

- 指定宽高及宽高比例裁剪
```java
selector.selectPicture(true, 200, 200, 1, 1);
```

***创建 ActivityResultLauncher 的情况下***
- 不裁剪
```java
PictureSelector
        .create(MainActivity.this, launcher)
        .selectPicture(false);
```

- 自由裁剪
```java
PictureSelector
        .create(MainActivity.this, launcher)
        .selectPicture(true);
```

- 指定宽高及宽高比例裁剪
```java
PictureSelector
        .create(MainActivity.this, launcher)
        .selectPicture(true, 200, 200, 1, 1);
```

**参数解释：**
- create()：参数一是实现了 ActivityResultCaller 与 LifecycleOwner 接口的上下文。参数二 callback 是一个 ActivityResultCallBack 或 ActivityResultLauncher 对象（即步骤三设置的），负责处理选择图片结果回调。
- selectPicture()：参数分别为是否裁剪、裁剪后图片的宽(单位 px)、裁剪后图片的高、宽比例、高比例。

### 清理缓存
实际开发中将图片上传到服务器成功后需要删除全部缓存图片（即裁剪后的无用图片），调用如下方法即可：
```java
FileUtils.deleteAllCacheImage(this);
```

## 注意
1.你的 Activity 和 Fragment 必须继承于 androidx.core.app.ComponentActivity 和 androidx.fragment.app.Fragment 或实现它们的子类， 平台 SDK 的 Activity 和 Fragment 没有实现 [ActivityResultCaller](https://developer.android.com/reference/androidx/activity/result/ActivityResultCaller) 接口，会导致运行异常。目前使用 Android Studio 创建的新项目均会自动导入 AndroidX Activity 包和 AndroidX Fragment 包并默认继承以上两类，不需要做特别的处理。

**同时请注意，该 API 仍然在 Beta 阶段，正式项目开发请慎用！**

2.本项目仅仅适配了 ActivityResult API 并对原有接口实现做出必要更改，并无任何新功能添加，与原版库完全（在使用 AppCompat 组件的情况下）接口兼容，如对 **ActivityResult API 相关以外**的功能有任何Issue，请提交至主项目。

3.如果你没有使用依赖的方式，而是直接拷贝源码到你的项目中使用。那么需要自己适配 Android 7.0 导致的 FileUriExposedException 异常，具体方式如下：

将 PictureSelectUtils 中的 authority 与你项目中 AndroidManifest.xml 下的 authority 保持一致。
例如 AndroidManifest.xml 下的 authority 为：
```java
android:authorities="myAuthority"
```
则需要修改 PictureSelectUtils 中的 authority（ [这一行](https://github.com/wildma/PictureSelector/blob/master/pictureselector/src/main/java/com/wildma/pictureselector/PictureSelectUtils.java#L85)） 为：
```java
String authority = "myAuthority";
```


详细介绍请看原作者文章：[一个非常好用的 Android 图片选择框架](https://www.jianshu.com/p/6ac6b681c413)
