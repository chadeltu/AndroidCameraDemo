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

	// Ӧ�����ƣ����ڴ��ͼƬ�ļ�·��
	private final String m_appName = "AndroidCameraDemo";

	private ImageView ivDemo;
	private Button btnDemo;

	// ����ͼƬURI
	private Uri imageUri;

	// �ü�ͼƬ��߱�Ϊ4:3
	private static final int aspectX = 4;
	private static final int aspectY = 3;
	// ����ͼƬ��͸�
	private static final int outputX = 480;
	private static final int outputY = 360;

	private static final int TAKE_BIG_PICTURE = 1;		// ����
	private static final int CHOOSE_BIG_PICTURE = 2;	// ���
	private static final int CROP_BIG_PICTURE = 3;		// �ü�
	
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

		// ��ʱͼƬ�ļ�
		String strImgPath = getImageCachePath() + File.separator
				+ "img_temp.jpg";
		imageUri = Uri.fromFile(new File(strImgPath));
	}

	/**
	 * ��ʾ�Ի��� - ���/���
	 */
	private void showPickDialog() {
		new AlertDialog.Builder(this)
				.setTitle(
						this.getResources().getString(R.string.dlg_title_photo))
				// ����ᡱ
				.setNegativeButton(
						this.getResources().getString(R.string.dlg_msg_album),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								
								// Intent.ACTION_GET_CONTENT��"image/*"��������ʾ��ȡ��������ͼƬ��Դ
								Intent intent = new Intent(
										Intent.ACTION_GET_CONTENT, null);
								intent.setType("image/*");
								intent.putExtra("crop", "true");	// ��ʾҪ�ü���ԭʼ�ߴ�һ��ܴ�
								// ���п�߱�
								intent.putExtra("aspectX", aspectX);
								intent.putExtra("aspectY", aspectY);
								// Ҫ����ͼƬ�ߴ�
								intent.putExtra("outputX", outputX);
								intent.putExtra("outputY", outputY);
								intent.putExtra("scale", true);
								// �Ƿ񷵻����ݣ�false��ʾ����Ҫ�������ӱ��浽���أ�imageUri��
								intent.putExtra("return-data", false);
								intent.putExtra(MediaStore.EXTRA_OUTPUT,
										imageUri);
								intent.putExtra("outputFormat",
										Bitmap.CompressFormat.JPEG.toString());
								// ���������
								intent.putExtra("noFaceDetection", false);
								startActivityForResult(intent,
										CHOOSE_BIG_PICTURE);
							}
						})
				// �������
				.setPositiveButton(
						this.getResources().getString(
								R.string.dlg_msg_take_photo),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {

								// ���������������
								Intent intent = new Intent(
										MediaStore.ACTION_IMAGE_CAPTURE);
								
								// �������ָ������������պ����Ƭ�洢��·��
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
		case CHOOSE_BIG_PICTURE: // �����ֱ�Ӵ�����ȡ
			setPicToView();
			break;
		case TAKE_BIG_PICTURE: // ����ǵ����������ʱ
			cropImageUri(imageUri, outputX, outputY, CROP_BIG_PICTURE);
			break;
		case CROP_BIG_PICTURE: // ȡ�òü����ͼƬ
			setPicToView();
			break;
		default:
			break;

		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	/**
	 * �ü�ͼƬ����ʵ��
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
	 * ����ü�֮���ͼƬ
	 * 
	 * @param picdata
	 */
	private void setPicToView() {
		Bitmap bitmap = decodeUriAsBitmap(imageUri);
		if (bitmap != null) {
			// ����ImageView
			ivDemo.setImageBitmap(bitmap);
		}
	}

	/**
	 * ��ȡUri��ӦBitmap
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
	 * �ж�SD��
	 * 
	 * @return
	 */
	private boolean isSDCardAvailable() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
	}

	/**
	 * ��ȡͼƬ���·�������������ļ��У�
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
