package com.compscieddy.plattr;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.compscieddy.plattr.ui.RoundImage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A picture composer fragment for selecting the 2 images that will make up the final platter composition.
 */
public class PictureComposerFragment extends Fragment implements ViewTreeObserver.OnGlobalLayoutListener {

  private final String TAG = PictureComposerFragment.class.getSimpleName();

  /* Intent Actions */
  private final int ACTION_REQUEST_GALLERY_BACKGROUND = 1;
  private final int ACTION_REQUEST_GALLERY_FOREGROUND = 2;
  private final int ACTION_PIC_CROP_BACKGROUND = 3;
  private final int ACTION_PIC_CROP_FOREGROUND = 4;

  /* Member Variables */
  private Uri mImageCaptureUri;
  private int mActivePointerIndex;
  private Point[] CORNER_COORDINATES = new Point[4];

  /* Member Variables - Views */
  private View mRootLayout;
  private ImageView mBackgroundImage;
  private ImageView mForegroundImage;
  private Button mSaveButton;
  private RelativeLayout mImagesLayout;
  private Button mSetForegroundImageButton;
  private Button mSetBackgroundImageButton;

  private View.OnTouchListener mForegroundImageOnTouchListener = new View.OnTouchListener() {
    @Override
    public boolean onTouch(View v, MotionEvent event) {
      switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN: {
          mActivePointerIndex = event.getPointerId(0);
          break;
        }
        case MotionEvent.ACTION_MOVE: {
          final float x = event.getX(mActivePointerIndex);
          final float y = event.getY(mActivePointerIndex);
          v.setX(x + v.getX() - v.getWidth() *1/2 );
          v.setY(y + v.getY() - v.getHeight() *1/2 );
          break;
        }
        case MotionEvent.ACTION_UP: {
          // Snap to one of the corners
          float currentX = v.getX();
          float currentY = v.getY();
          Log.d(TAG, "Action Up X:"+currentX+" Y:"+currentY);
          float distances[] = new float[4];
          int minDistanceIndex = 0;
          for (int i=0; i<CORNER_COORDINATES.length; i++) {
            float cornerX = CORNER_COORDINATES[i].x;
            float cornerY = CORNER_COORDINATES[i].y;
            float distance = (float) Math.sqrt(
                Math.pow( Math.abs(cornerX - currentX), 2)
                    + Math.pow( Math.abs(cornerY - currentY), 2)
            );
            if (distance < distances[minDistanceIndex]) {
              minDistanceIndex = i;
            }
            distances[i] = distance;
          }
          Log.d(TAG, "Corner Distances " + "TL:"+distances[0] + " TR:"+distances[1]
              + " BL:"+distances[2] + " BR:"+distances[3]);
          v.setX(CORNER_COORDINATES[minDistanceIndex].x);
          v.setY(CORNER_COORDINATES[minDistanceIndex].y);
          break;
        }
      }
      return true;
    }
  };

  View.OnClickListener mSaveButtonOnClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      final float startTime = System.currentTimeMillis();
      Log.d(TAG, "Save button started " + startTime);
      AsyncTask<Void, Void, Void> saveImageTask = new AsyncTask<Void, Void, Void>() {
        @Override
        protected void onPreExecute() {
          super.onPreExecute();
        }
        @Override
        protected Void doInBackground(Void... params) {
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
            Bitmap compositeBitmap = Bitmap.createBitmap(mImagesLayout.getWidth(), mImagesLayout.getHeight(), Bitmap.Config.ARGB_8888);
            mImagesLayout.draw(new Canvas(compositeBitmap));
            compositeBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HHmmss"); // ("yyyy_MM_dd_HH_mm_ss");
            Date now = new Date();
            String fileName = formatter.format(now);
            Utils.insertImage(getActivity().getContentResolver(), compositeBitmap, fileName, "Plattr Image");

          } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found", e);
          } catch (Exception e) {
            Log.e(TAG, "General Exception outputting composite image", e);
          }
          return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
          super.onPostExecute(aVoid);
          Toast.makeText(getActivity(), "Image saved to Camera Roll", Toast.LENGTH_SHORT).show();
        }
      };
      saveImageTask.execute();
    }
  };

  public PictureComposerFragment() {
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    mRootLayout = inflater.inflate(R.layout.fragment_picture_composer, container, false);

    mBackgroundImage = (ImageView) mRootLayout.findViewById(R.id.background_image);
    mForegroundImage = (ImageView) mRootLayout.findViewById(R.id.foreground_image);
    mSaveButton = (Button) mRootLayout.findViewById(R.id.save_button);
    mImagesLayout = (RelativeLayout) mRootLayout.findViewById(R.id.images_layout);
    mSetForegroundImageButton = (Button) mRootLayout.findViewById(R.id.set_foreground_button);
    mSetBackgroundImageButton = (Button) mRootLayout.findViewById(R.id.set_background_button);

    mBackgroundImage.getViewTreeObserver().addOnGlobalLayoutListener(this);

    mSetForegroundImageButton.setOnClickListener(getChoosePictureOnClick(ACTION_REQUEST_GALLERY_FOREGROUND));
    mSetBackgroundImageButton.setOnClickListener(getChoosePictureOnClick(ACTION_REQUEST_GALLERY_BACKGROUND));
    mBackgroundImage.setOnLongClickListener(getChoosePictureOnLongClickListener(ACTION_REQUEST_GALLERY_BACKGROUND));
    mForegroundImage.setOnTouchListener(mForegroundImageOnTouchListener);
    mSaveButton.setOnClickListener(mSaveButtonOnClickListener);

    return mRootLayout;
  }

  private void sendChoosePictureIntent(int action_request) {
    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
    intent.setType("image/*");
//    Intent chooser = Intent.createChooser(intent, getString(R.string.choose_picture_dialog_title));
    Intent chooser = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    startActivityForResult(chooser, action_request);
  }

  private View.OnLongClickListener getChoosePictureOnLongClickListener(final int action_request) {
    return new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        sendChoosePictureIntent(action_request);
        return true;
      }
    };
  }

  private View.OnClickListener getChoosePictureOnClick(final int action_request) {
    return new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        sendChoosePictureIntent(action_request);
      }
    };
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (resultCode == Activity.RESULT_OK) {
      switch (requestCode) {
        case ACTION_REQUEST_GALLERY_BACKGROUND:
        case ACTION_REQUEST_GALLERY_FOREGROUND:
          mImageCaptureUri = data.getData();

          Intent cropIntent = new Intent("com.android.camera.action.CROP");
          cropIntent.setDataAndType(mImageCaptureUri, "image/*");
          cropIntent.putExtra("crop", "true");
          cropIntent.putExtra("aspectX", 1);
          cropIntent.putExtra("aspectY", 1);
          cropIntent.putExtra("outputX", 256);
          cropIntent.putExtra("outputY", 256);
          cropIntent.putExtra("return-data", true);
          if (requestCode == ACTION_REQUEST_GALLERY_BACKGROUND) {
            startActivityForResult(cropIntent, ACTION_PIC_CROP_BACKGROUND);
          } else {
            startActivityForResult(cropIntent, ACTION_PIC_CROP_FOREGROUND);
          }

          String[] filePath = { MediaStore.Images.Media.DATA };
          Cursor cursor = getActivity().getContentResolver().query(mImageCaptureUri, filePath, null, null, null);
          if (cursor != null && cursor.moveToFirst()) {
            cursor.moveToFirst();
            String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));
          } else {
            Log.e(TAG, getString(R.string.error_bad_cursor));
          }

          break;
        case ACTION_PIC_CROP_BACKGROUND:
        case ACTION_PIC_CROP_FOREGROUND:
          Bundle extras = data.getExtras();
          Bitmap bitmap = extras.getParcelable("data");
          if (requestCode == ACTION_PIC_CROP_BACKGROUND) {
            mBackgroundImage.setImageBitmap(bitmap);
          } else {
            Drawable roundedImage = new RoundImage(bitmap);
            mForegroundImage.setImageDrawable(roundedImage);
//            mForegroundImage.setImageBitmap(bitmap);
          }
          break;
      }
    }

    super.onActivityResult(requestCode, resultCode, data);
  }

  /* Interface Implementations */

  @Override
  public void onGlobalLayout() {
    int width = mBackgroundImage.getWidth();
    ViewGroup.LayoutParams layoutParams = mBackgroundImage.getLayoutParams();
    layoutParams.height = width;
    mBackgroundImage.setLayoutParams(layoutParams);

    int padding = getResources().getDimensionPixelOffset(R.dimen.foreground_margin);
    Point point;

    point = new Point();
    point.x = mBackgroundImage.getX() + padding;
    point.y = mBackgroundImage.getY() + padding;
    CORNER_COORDINATES[0] = point; // top left

    point = new Point();
    point.x = mBackgroundImage.getX() + mBackgroundImage.getWidth() - padding - mForegroundImage.getWidth();
    point.y = mBackgroundImage.getY() + padding;
    CORNER_COORDINATES[1] = point; // top right

    point = new Point();
    point.x = mBackgroundImage.getX() + padding;
    point.y = mBackgroundImage.getY() + mBackgroundImage.getHeight() - padding - mForegroundImage.getHeight();
    CORNER_COORDINATES[2] = point; // bottom left

    point = new Point();
    point.x = mBackgroundImage.getX() + mBackgroundImage.getWidth() - padding - mForegroundImage.getWidth();
    point.y = mBackgroundImage.getY() + mBackgroundImage.getHeight() - padding - mForegroundImage.getHeight();
    CORNER_COORDINATES[3] = point; // bottom right
  }

  public class Point {
    public float x = 0;
    public float y = 0;
  }
}
