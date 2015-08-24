package com.pictura.circlemenu;

import android.graphics.Bitmap;

public class ViewState {
	Bitmap bitmap;
	int seekState;

	public ViewState(Bitmap bitmap, int seekState) {
		this.bitmap = bitmap;
		this.seekState = seekState;
	}
	
}
