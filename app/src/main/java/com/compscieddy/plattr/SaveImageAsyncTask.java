package com.compscieddy.plattr;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.ContentValues.TAG;

/**
 * Created by elee on 8/23/16.
 */

public class SaveImageAsyncTask extends AsyncTask<Integer, Void, Void> {
  private View mImagesLayout;
  private View mRootView;
  private final Context mContext;

  public SaveImageAsyncTask(Context context, View imagesLayout, View rootView) {
    super();
    mContext = context;
    this.mImagesLayout = imagesLayout;
    this.mRootView = rootView;
  }

  @Override
  protected void onPreExecute() {
    super.onPreExecute();
    Snackbar snackbar = Snackbar.make(mRootView, "Image is saving to your Camera Roll", Snackbar.LENGTH_SHORT);
    snackbar.show();
  }

  @Override
  protected Void doInBackground(Integer... params) {
    int imagesLayoutWidth = params[0];
    int imagesLayoutHeight = params[1];

    File folder = new File(Environment.getExternalStorageDirectory().toString());
    if (!folder.exists()) { folder.mkdirs(); }
    String uniqueName = "" + System.currentTimeMillis();
    File file = new File(Environment.getExternalStorageDirectory().toString(), uniqueName + ".png");
    if (!file.exists()) {
      try {
        file.createNewFile();
      } catch (IOException e) {
        Log.e(TAG, "Error creating file", e);
      }
    }
    FileOutputStream outputStream = null;
    try {
      outputStream = new FileOutputStream(file);
      Bitmap compositeBitmap = Bitmap.createBitmap(imagesLayoutWidth, imagesLayoutHeight, Bitmap.Config.ARGB_8888);
      this.mImagesLayout.draw(new Canvas(compositeBitmap));
      compositeBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

      SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HHmmss"); // ("yyyy_MM_dd_HH_mm_ss");
      Date now = new Date();
      String fileName = formatter.format(now);

      Utils.insertImage(mContext.getContentResolver(), compositeBitmap, fileName, "Plattr Image");

    } catch (FileNotFoundException e) {
      Log.e(TAG, "File not found", e);
    } catch (Exception e) {
      Log.e(TAG, "General exception outputting composite imageView", e);
    }

    return null;
  }

  @Override
  protected void onPostExecute(Void aVoid) {
    super.onPostExecute(aVoid);
  }
}
