package com.example.androidcamerademo;

import java.io.File;
import java.io.FileNotFoundException;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends Activity {

	// 应用名称，用于存放图片文件路径
	private final String m_appName = "AndroidCameraDemo";

	private ImageView ivDemo;
	private Button btnDemo;

	// 本地图片URI
	private Uri imageUri;

	// 裁剪图片宽高比为4:3
	private static final int aspectX = 4;
	private static final int aspectY = 3;
	// 保存图片宽和高
	private static final int outputX = 480;
	private static final int outputY = 360;

	private static final int TAKE_BIG_PICTURE = 1;		// 拍照
	private static final int CHOOSE_BIG_PICTURE = 2;	// 相册
	private static final int CROP_BIG_PICTURE = 3;		// 裁剪
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ivDemo = (ImageView) this.findViewById(R.id.iv_demo);
		btnDemo = (Button) this.findViewById(R.id.btn_demo);
		btnDemo.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				showPickDialog();
			}
		});

		// 临时图片文件
		String strImgPath = getImageCachePath() + File.separator
				+ "img_temp.jpg";
		imageUri = Uri.fromFile(new File(strImgPath));
	}

	/**
	 * 显示对话框 - 相机/相册
	 */
	private void showPickDialog() {
		new AlertDialog.Builder(this)
				.setTitle(
						this.getResources().getString(R.string.dlg_title_photo))
				// “相册”
				.setNegativeButton(
						this.getResources().getString(R.string.dlg_msg_album),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								
								// Intent.ACTION_GET_CONTENT和"image/*"合起来表示获取本机已有图片资源
								Intent intent = new Intent(
										Intent.ACTION_GET_CONTENT, null);
								intent.setType("image/*");
								intent.putExtra("crop", "true");	// 表示要裁剪（原始尺寸一般很大）
								// 剪切宽高比
								intent.putExtra("aspectX", aspectX);
								intent.putExtra("aspectY", aspectY);
								// 要保存图片尺寸
								intent.putExtra("outputX", outputX);
								intent.putExtra("outputY", outputY);
								intent.putExtra("scale", true);
								// 是否返回数据，false表示不需要，本例子保存到本地（imageUri）
								intent.putExtra("return-data", false);
								intent.putExtra(MediaStore.EXTRA_OUTPUT,
										imageUri);
								intent.putExtra("outputFormat",
										Bitmap.CompressFormat.JPEG.toString());
								// 无人脸检测
								intent.putExtra("noFaceDetection", false);
								startActivityForResult(intent,
										CHOOSE_BIG_PICTURE);
							}
						})
				// “相机”
				.setPositiveButton(
						this.getResources().getString(
								R.string.dlg_msg_take_photo),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {

								// 调用相机进行拍照
								Intent intent = new Intent(
										MediaStore.ACTION_IMAGE_CAPTURE);
								
								// 下面这句指定调用相机拍照后的照片存储的路径
								intent.putExtra(MediaStore.EXTRA_OUTPUT,
										imageUri);
								startActivityForResult(intent, TAKE_BIG_PICTURE);
							}
						}).show();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK) {
			return;
		}

		switch (requestCode) {
		case CHOOSE_BIG_PICTURE: // 如果是直接从相册获取
			setPicToView();
			break;
		case TAKE_BIG_PICTURE: // 如果是调用相机拍照时
			cropImageUri(imageUri, outputX, outputY, CROP_BIG_PICTURE);
			break;
		case CROP_BIG_PICTURE: // 取得裁剪后的图片
			setPicToView();
			break;
		default:
			break;

		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	/**
	 * 裁剪图片方法实现
	 * 
	 * @param uri
	 */
	private void cropImageUri(Uri uri, int outputX, int outputY, int requestCode) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", aspectX);
		intent.putExtra("aspectY", aspectY);
		intent.putExtra("outputX", outputX);
		intent.putExtra("outputY", outputY);
		intent.putExtra("scale", true);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		intent.putExtra("return-data", false);
		intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		intent.putExtra("noFaceDetection", true); // no face detection
		startActivityForResult(intent, requestCode);
	}
	
	/**
	 * 处理裁剪之后的图片
	 * 
	 * @param picdata
	 */
	private void setPicToView() {
		Bitmap bitmap = decodeUriAsBitmap(imageUri);
		if (bitmap != null) {
			// 设置ImageView
			ivDemo.setImageBitmap(bitmap);
		}
	}

	/**
	 * 获取Uri对应Bitmap
	 * @param uri
	 * @return
	 */
	private Bitmap decodeUriAsBitmap(Uri uri) {
		Bitmap bitmap = null;
		try {
			bitmap = BitmapFactory.decodeStream(getContentResolver()
					.openInputStream(uri));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		return bitmap;
	}
	
	/**
	 * 判断SD卡
	 * 
	 * @return
	 */
	private boolean isSDCardAvailable() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
	}

	/**
	 * 获取图片存放路径（包括创建文件夹）
	 * 
	 * @param ctx
	 * @return
	 */
	private String getImageCachePath() {
		String strRootPath = null;
		if (isSDCardAvailable()) {
			strRootPath = Environment.getExternalStorageDirectory().getPath()
					+ File.separator + m_appName;
		} else {
			strRootPath = this.getCacheDir().getAbsolutePath() + File.separator
					+ m_appName;
		}
		strRootPath += File.separator + "img";
		File fileDir = new File(strRootPath);
		if (!fileDir.exists()) {
			fileDir.mkdirs();
		}
		return strRootPath;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}
