package com.pictura.circlemenu;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class Editor extends Activity implements OnClickListener,
		OnSeekBarChangeListener {

	Stack<ViewState> undo, redo;

	TouchImageView imgView;
	Uri uri;
	LinearLayout linear;
	/** Selection content */
	ImageView imgView1;
	ImageView imgView2;
	ImageView imgView3;
	ImageView imgView4;
	ImageView imgView5;
	ImageView imgView6;
	Uri current_uri;
	SeekBar seek;
	int seekState;
	ViewState state;
	/** rotate */
	Bitmap bmp;
	Bitmap rotatedBMP;
	Bitmap bmmp;
	Matrix mtx = new Matrix();
	TextView too;

	int debug1;
	int debug2;
	int printDebug;
	private static final int CROP_FROM_CAMERA = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		debug2 = 0;
		debug1 = 0;
		printDebug = 0;
		undo = new Stack<ViewState>();
		redo = new Stack<ViewState>();

		setContentView(R.layout.activity_trial);
		Intent intent = getIntent();
		uri = intent.getParcelableExtra("path");
		imgView = (TouchImageView) findViewById(R.id.imgView);

		imgView.setImageURI(uri);

		imgView1 = (ImageView) findViewById(R.id.imgView1);
		imgView1.setOnClickListener(this);
		imgView2 = (ImageView) findViewById(R.id.imgView2);
		imgView2.setOnClickListener(this);
		imgView3 = (ImageView) findViewById(R.id.imgView3);
		imgView3.setOnClickListener(this);
		imgView4 = (ImageView) findViewById(R.id.imgView4);
		imgView4.setOnClickListener(this);
		imgView5 = (ImageView) findViewById(R.id.imgView5);
		imgView5.setOnClickListener(this);
		imgView6 = (ImageView) findViewById(R.id.imgView6);
		imgView6.setOnClickListener(this);

		too = (TextView) findViewById(R.id.textview1);
		seek = (SeekBar) findViewById(R.id.seekBar1);

		BitmapDrawable drawable = (BitmapDrawable) imgView.getDrawable();
		bmp = drawable.getBitmap();
		seek.setOnSeekBarChangeListener(this);
		state = new ViewState(bmp, -1);
		pushState();
		debug();
	}

	private void debug() {
		too.setText("Undo:" + undo.size() + "    " + "Redo:" + redo.size()
				+ "   Clicks:" + debug2 + "   Debug:" + printDebug);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {

		case R.id.imgView1: // Rotate
			if (seek.getVisibility() == View.VISIBLE) {
				seek.setVisibility(View.INVISIBLE);
			} else {
				seek.setVisibility(View.VISIBLE);
			}
			break;
		case R.id.imgView2: // Crop
			seek.setVisibility(View.INVISIBLE);
			doCrop();
			break;
		case R.id.imgView3: // Undo
			seek.setVisibility(View.INVISIBLE);
			undo();
			debug2++;
			break;
		case R.id.imgView4: // Redo
			seek.setVisibility(View.INVISIBLE);
			redo();
			break;
		case R.id.imgView5: // Reset
			seek.setVisibility(View.INVISIBLE);
			imgView.setImageURI(uri);
			// bmmp = imageViewToBitmap(imgView);
			pushState();
			break;
		case R.id.imgView6: // Save
			seek.setVisibility(View.INVISIBLE);

			BitmapDrawable drawable = (BitmapDrawable) imgView.getDrawable();
			Bitmap btmp = drawable.getBitmap();
			File file = new File(
					Environment
							.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
					"tmp_avatar_" + String.valueOf(System.currentTimeMillis())
							+ ".jpg");
			MediaStore.Images.Media.insertImage(getContentResolver(), btmp, "",
					"");
			try {
				file.createNewFile();
				FileOutputStream ostream = new FileOutputStream(file);
				btmp.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
				ostream.flush();
				ostream.close();
				sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
						Uri.fromFile(file)));

				Toast.makeText(getApplicationContext(), "Photo Saved",
						Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(), "Error",
						Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
			break;

		}

		debug();
	}

	// push photo state in undo stack
	private void pushState() {
		bmmp = imageViewToBitmap(imgView);
		undo.push(new ViewState(bmmp, debug1++));
		Log.i("PushState", "redo.clear()");
		redo.clear();
	}

	private void redo() {
		// TODO Auto-generated method stub
		if (!redo.isEmpty()) {
			state = redo.pop();
			if (!redo.isEmpty()) {
				imgView.setImageBitmap(Bitmap.createBitmap(redo.peek().bitmap));
				printDebug = redo.peek().seekState;
			}
			undo.push(state);
		} else {
			imgView.setImageBitmap(Bitmap.createBitmap(state.bitmap));
		}
	}

	private void undo() {
		// TODO Auto-generated method stub
		if (!undo.isEmpty()) {
			if (undo.size() > 1) {
				state = undo.pop();
				redo.push(state);
			}
			if (!undo.isEmpty()) {
				imgView.setImageBitmap(Bitmap.createBitmap(undo.peek().bitmap));
				printDebug = undo.peek().seekState;
			}
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// rotate code
		int w = bmp.getWidth();
		int h = bmp.getHeight();
		seekState = progress;
		if (seekState % 5 == 0)
			mtx.postRotate(seekState);
		rotatedBMP = Bitmap.createBitmap(bmp, 0, 0, w, h, mtx, true);
		imgView.setImageBitmap(rotatedBMP);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		pushState();
		debug();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK)
			return;

		switch (requestCode) {

		case CROP_FROM_CAMERA:
			Bundle extras = data.getExtras();
			/**
			 * After cropping the image, get the bitmap of the cropped image and
			 * display it on imageview.
			 */
			if (extras != null) {
				Bitmap photo = extras.getParcelable("data");

				imgView.setImageBitmap(photo);
				bmp = imageViewToBitmap(imgView);
				pushState();

			}

			File f = new File(uri.getPath());
			/**
			 * Delete the temporary image
			 */
			if (f.exists())
				f.delete();

			break;

		}
	}

	public class CropOptionAdapter extends ArrayAdapter<CropOption> {
		private ArrayList<CropOption> mOptions;
		private LayoutInflater mInflater;

		public CropOptionAdapter(Context context, ArrayList<CropOption> options) {
			super(context, R.layout.crop_selector, options);

			mOptions = options;

			mInflater = LayoutInflater.from(context);
		}

		@SuppressLint("InflateParams")
		@Override
		public View getView(int position, View convertView, ViewGroup group) {
			if (convertView == null)
				convertView = mInflater.inflate(R.layout.crop_selector, null);

			CropOption item = mOptions.get(position);

			if (item != null) {
				((ImageView) convertView.findViewById(R.id.iv_icon))
						.setImageDrawable(item.icon);

				return convertView;
			}

			return null;
		}
	}

	public class CropOption {
		public CharSequence title;
		public Drawable icon;
		public Intent appIntent;
	}

	private void doCrop() {
		final ArrayList<CropOption> cropOptions = new ArrayList<CropOption>();
		/**
		 * Open image crop app by starting an intent
		 * ‘com.android.camera.action.CROP‘.
		 */
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setType("image/*");

		/**
		 * Check if there is image cropper app installed.
		 */
		List<ResolveInfo> list = getPackageManager().queryIntentActivities(
				intent, 0);

		int size = list.size();

		/**
		 * If there is no image cropper app, display warning message
		 */
		if (size == 0) {

			Toast.makeText(this, "Can not find image crop app",
					Toast.LENGTH_SHORT).show();

			return;
		} else {
			BitmapDrawable drawable = (BitmapDrawable) imgView.getDrawable();
			Bitmap btmp = drawable.getBitmap();
			File file = new File(
					Environment
							.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
					"Pictura_crop" + ".tmp");
			MediaStore.Images.Media.insertImage(getContentResolver(), btmp, "",
					"");
			try {
				file.createNewFile();
				FileOutputStream ostream = new FileOutputStream(file);
				btmp.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
				ostream.flush();
				ostream.close();
				sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
						Uri.fromFile(file)));
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(), "error",
						Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
			/**
			 * Specify the image path, crop dimension and scale
			 */
			intent.setData(Uri.fromFile(file));

			intent.putExtra("outputX", 200);
			intent.putExtra("outputY", 200);
			intent.putExtra("aspectX", 1);
			intent.putExtra("aspectY", 1);
			intent.putExtra("scale", true);
			intent.putExtra("return-data", true);
			/**
			 * There is possibility when more than one image cropper app exist,
			 * so we have to check for it first. If there is only one app, open
			 * then app.
			 */

			if (size == 1) {
				Intent i = new Intent(intent);
				ResolveInfo res = list.get(0);

				i.setComponent(new ComponentName(res.activityInfo.packageName,
						res.activityInfo.name));

				startActivityForResult(i, CROP_FROM_CAMERA);
			} else {
				/**
				 * If there are several app exist, create a custom chooser to
				 * let user selects the app.
				 */
				for (ResolveInfo res : list) {
					final CropOption co = new CropOption();

					co.title = getPackageManager().getApplicationLabel(
							res.activityInfo.applicationInfo);
					co.icon = getPackageManager().getApplicationIcon(
							res.activityInfo.applicationInfo);
					co.appIntent = new Intent(intent);

					co.appIntent
							.setComponent(new ComponentName(
									res.activityInfo.packageName,
									res.activityInfo.name));

					cropOptions.add(co);
				}

				CropOptionAdapter adapter = new CropOptionAdapter(
						getApplicationContext(), cropOptions);

				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Choose Crop App");
				builder.setAdapter(adapter,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int item) {
								startActivityForResult(
										cropOptions.get(item).appIntent,
										CROP_FROM_CAMERA);
							}
						});

				builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {

						if (uri != null) {
							uri = null;
						}
					}
				});

				AlertDialog alert = builder.create();

				alert.show();
			}
		}
	}

	private Bitmap imageViewToBitmap(ImageView iv) {
		BitmapDrawable drawable = (BitmapDrawable) iv.getDrawable();
		return drawable.getBitmap();
	}

}
