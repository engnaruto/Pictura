package com.pictura.circlemenu;

import java.io.File;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.Toast;

import com.pictura.circlemenu.R;
import com.pictura.circlemenu.view.CircleLayout;
import com.pictura.circlemenu.view.CircleLayout.OnCenterClickListener;
import com.pictura.circlemenu.view.CircleLayout.OnItemClickListener;
import com.pictura.circlemenu.view.CircleLayout.OnItemSelectedListener;
import com.pictura.circlemenu.view.CircleLayout.OnRotationFinishedListener;

public class MainActivity extends Activity implements OnItemSelectedListener,
		OnItemClickListener, OnRotationFinishedListener, OnCenterClickListener {
	// TextView selectedTextView;
	private Uri mImageCaptureUri;
	private ImageView mImageView;

	private static final int PICK_FROM_CAMERA = 1;
	private static final int CROP_FROM_CAMERA = 2;
	private static final int PICK_FROM_FILE = 3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		CircleLayout circleMenu = (CircleLayout) findViewById(R.id.main_circle_layout);
		circleMenu.setOnItemSelectedListener(this);
		circleMenu.setOnItemClickListener(this);
		circleMenu.setOnRotationFinishedListener(this);
		circleMenu.setOnCenterClickListener(this);
	}

	@Override
	public void onItemSelected(View view, String name) {
		// selectedTextView.setText(name);

		switch (view.getId()) {

		case R.id.main_linkedin_image:
			// Handle linkedin selection
			break;
		case R.id.main_myspace_image:
			// Handle myspace selection
			break;

		case R.id.main_wordpress_image:
			// Handle wordpress selection
			break;
		}
	}

	@Override
	public void onItemClick(View view, String name) {
		switch (view.getId()) {

		case R.id.main_linkedin_image:
			// Handle linkedin selection
			/**
			 * To select an image from existing files, use Intent.createChooser
			 * to open image chooser. Android will automatically display a list
			 * of supported applications, such as image gallery or file manager.
			 */
			Intent intent = new Intent();

			intent.setType("image/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);

			startActivityForResult(
					Intent.createChooser(intent, "Complete action using"),
					PICK_FROM_FILE);
			break;
		case R.id.main_myspace_image:
			// Handle myspace selection
			/**
			 * To take a photo from camera, pass intent action
			 * ‘MediaStore.ACTION_IMAGE_CAPTURE‘ to open the camera app.
			 */
			Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

			/**
			 * Also specify the Uri to save the image on specified path and file
			 * name. Note that this Uri variable also used by gallery app to
			 * hold the selected image path.
			 */
			mImageCaptureUri = Uri
					.fromFile(new File(
							Environment
									.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
							"tmp_avatar_"
									+ String.valueOf(System.currentTimeMillis())
									+ ".jpg"));

			intent2.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
					mImageCaptureUri);

			try {
				intent2.putExtra("return-data", true);

				startActivityForResult(intent2, PICK_FROM_CAMERA);

			} catch (ActivityNotFoundException e) {
				e.printStackTrace();
			}
			break;

		case R.id.main_wordpress_image:
			// Handle wordpress click
			finish();
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK)
			return;
		Intent in = new Intent(this, Editor.class);
		switch (requestCode) {
		case PICK_FROM_CAMERA:
			/**
			 * After taking a picture, do the crop
			 */
			in.putExtra("path", mImageCaptureUri);
			sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
					mImageCaptureUri));

			startActivity(in);
			break;

		case PICK_FROM_FILE:
			/**
			 * After selecting image from files, save the selected path
			 */
			mImageCaptureUri = data.getData();

			in.putExtra("path", mImageCaptureUri);
			startActivity(in);
			break;

		case CROP_FROM_CAMERA:
			Bundle extras = data.getExtras();
			/**
			 * After cropping the image, get the bitmap of the cropped image and
			 * display it on imageview.
			 */
			if (extras != null) {
				Bitmap photo = extras.getParcelable("data");

				mImageView.setImageBitmap(photo);
			}

			File f = new File(mImageCaptureUri.getPath());
			/**
			 * Delete the temporary image
			 */
			if (f.exists())
				f.delete();

			break;

		}
	}

	@Override
	public void onCenterClick() {
		Toast.makeText(getApplicationContext(), R.string.center_click,
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onRotationFinished(View view, String name) {
		Animation animation = new RotateAnimation(0, 360, view.getWidth() / 2,
				view.getHeight() / 2);
		animation.setDuration(250);
		view.startAnimation(animation);
	}

}
