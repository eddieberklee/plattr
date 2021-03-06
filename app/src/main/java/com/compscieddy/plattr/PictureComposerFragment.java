package com.compscieddy.plattr;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import com.compscieddy.plattr.ui.RoundImage;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.view.DraweeView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A picture composer fragment for selecting the 2 images that will make up the final platter composition.
 */
public class PictureComposerFragment extends Fragment implements ViewTreeObserver.OnGlobalLayoutListener {

  private final String TAG = PictureComposerFragment.class.getSimpleName();
  private static final Lawg L = Lawg.newInstance(PictureComposerFragment.class.getSimpleName());

  private final int ACTION_REQUEST_GALLERY_BACKGROUND = 1;
  private final int ACTION_REQUEST_GALLERY_FOREGROUND = 2;
  private final int ACTION_PIC_CROP_BACKGROUND = 3;
  private final int ACTION_PIC_CROP_FOREGROUND = 4;

  private Uri mImageCaptureUri;
  private int mActivePointerIndex;
  private Point[] CORNER_COORDINATES = new Point[4];

  private View mRootView;
  @Bind(R.id.background_image) SimpleDraweeView mBackgroundImage;
  @Bind(R.id.foreground_image) DraweeView mForegroundImage;
  @Bind(R.id.images_layout) RelativeLayout mImagesLayout;
  @Bind(R.id.save_button) View mSaveButton;
  @Bind(R.id.pick_foreground_button) View mPickForegroundButton;
  @Bind(R.id.pick_background_button) View mPickBackgroundButton;
  @Bind(R.id.gallery_recyclerview) RecyclerView mGalleryRecyclerView;

  List<GalleryImageModel> mGalleryImages = new ArrayList<>();

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
          float distanceToCorners[] = new float[4];
          int minDistanceIndex = 0;
          for (int i=0; i<CORNER_COORDINATES.length; i++) {
            float cornerX = CORNER_COORDINATES[i].x;
            float cornerY = CORNER_COORDINATES[i].y;
            float distanceToCorner = (float) Math.sqrt(
                Math.pow( Math.abs(cornerX - currentX), 2)
                    + Math.pow( Math.abs(cornerY - currentY), 2)
            );
            if (distanceToCorner < distanceToCorners[minDistanceIndex]) {
              minDistanceIndex = i;
            }
            distanceToCorners[i] = distanceToCorner;
          }
          v.animate()
              .x(CORNER_COORDINATES[minDistanceIndex].x)
              .y(CORNER_COORDINATES[minDistanceIndex].y)
              .setInterpolator(new FastOutSlowInInterpolator())
              .setDuration(200);
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
      Log.d(TAG, "Save button started at: " + startTime);
      AsyncTask<Integer, Void, Void> saveImageTask = new SaveImageAsyncTask(mActivity, mImagesLayout, mRootView);
      saveImageTask.execute(mImagesLayout.getWidth(), mImagesLayout.getHeight());
    }
  };
  private Activity mActivity;

  public PictureComposerFragment() {
  }

  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    mActivity = getActivity();
    mRootView = inflater.inflate(R.layout.fragment_picture_composer, container, false);
    ButterKnife.bind(this, mRootView);

    init();

    mBackgroundImage.getViewTreeObserver().addOnGlobalLayoutListener(this);

    mPickForegroundButton.setOnClickListener(getChoosePictureOnClick(ACTION_REQUEST_GALLERY_FOREGROUND));
    mPickBackgroundButton.setOnClickListener(getChoosePictureOnClick(ACTION_REQUEST_GALLERY_BACKGROUND));

    mBackgroundImage.setOnLongClickListener(getChoosePictureOnLongClickListener(ACTION_REQUEST_GALLERY_BACKGROUND));
    mForegroundImage.setOnTouchListener(mForegroundImageOnTouchListener);
    mSaveButton.setOnClickListener(mSaveButtonOnClickListener);

    return mRootView;
  }

  private void init() {
    loadGalleryImages();
    RecyclerView.Adapter galleryAdapter = new GalleryAdapter(mActivity, mGalleryImages);
    mGalleryRecyclerView.setAdapter(galleryAdapter);
    final int NUM_COLUMNS = 2;
    mGalleryRecyclerView.setLayoutManager(new GridLayoutManager(mActivity, NUM_COLUMNS));
  }

  /** Populates mGalleryImages by using a SQLite cursor to get the gallery images info */
  private void loadGalleryImages() {
    final String[] columns = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.TITLE };
    final String orderBy = MediaStore.Images.Media.DATE_ADDED;
    // todo: read up on and use LoaderManager instead
    Cursor imagesCursor = mActivity.managedQuery(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null,
        null, orderBy);
    try {
      int image_column_index = imagesCursor.getColumnIndex(MediaStore.Images.Media._ID);
      for (int i = 0; i < imagesCursor.getCount(); i++) {
        imagesCursor.moveToPosition(i);
        int dataColumnIndex = imagesCursor.getColumnIndex(MediaStore.Images.Media.DATA);
        int displayNameColumnIndex = imagesCursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
        int titleColumnIndex = imagesCursor.getColumnIndex(MediaStore.Images.Media.TITLE);
        /*
        int id = imagesCursor.getInt(image_column_index);
        thumbnailBitmaps[i] = MediaStore.Images.Thumbnails.getThumbnail(
            getApplicationContext().getContentResolver(), id,
            MediaStore.Images.Thumbnails.MICRO_KIND, null);
        */
        String url = imagesCursor.getString(dataColumnIndex);
        String title = imagesCursor.getString(titleColumnIndex);
        String displayName = imagesCursor.getString(displayNameColumnIndex);
        L.d("Image info " + " url: " + url + " title: " + title + " displayName: " + displayName);
        mGalleryImages.add(new GalleryImageModel(url));
      }
    } finally {
      if (imagesCursor != null) imagesCursor.close();
    }
  }

  private void sendChoosePictureIntent(int action_request) {
    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
    intent.setType("imageView/*");
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
      mImageCaptureUri = data.getData();

//      String[] filePath = { MediaStore.Images.Media.DATA };
//      Cursor cursor = getActivity().getContentResolver().query(mImageCaptureUri, filePath, null, null, null);
//      String imagePath = null;
//      if (cursor != null && cursor.moveToFirst()) {
//        cursor.moveToFirst();
//        imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));
//      } else {
//        LogUtils.errorAndToast(getActivity(), TAG, getString(R.string.error_bad_cursor), new Exception());
//      }

//          Intent cropIntent = new Intent("com.android.camera.action.CROP");
//          cropIntent.setDataAndType(mImageCaptureUri, "imageView/*");
//          cropIntent.putExtra("crop", "true");
//          cropIntent.putExtra("aspectX", 1);
//          cropIntent.putExtra("aspectY", 1);
//          cropIntent.putExtra("return-data", true);
//          if (requestCode == ACTION_REQUEST_GALLERY_BACKGROUND) {
//            startActivityForResult(cropIntent, ACTION_PIC_CROP_BACKGROUND);
//          } else {
//            startActivityForResult(cropIntent, ACTION_PIC_CROP_FOREGROUND);
//          }

      switch (requestCode) {
        case ACTION_REQUEST_GALLERY_BACKGROUND:
          mBackgroundImage.setImageURI(mImageCaptureUri);
          break;
        case ACTION_REQUEST_GALLERY_FOREGROUND:
//          mForegroundImagePath = imagePath;
          boolean shouldResize = true;
          if (shouldResize) {
            int width, height;
            width = height = getResources().getDimensionPixelOffset(R.dimen.foreground_image_size);
            ImageRequest request = ImageRequestBuilder.newBuilderWithSource(mImageCaptureUri)
                .setResizeOptions(new ResizeOptions(width, height))
                .build();
            PipelineDraweeController controller = (PipelineDraweeController) Fresco.newDraweeControllerBuilder()
                .setOldController(mForegroundImage.getController())
                .setImageRequest(request)
                .build();
            mForegroundImage.setController(controller);
//            GenericDraweeHierarchyBuilder builder = new GenericDraweeHierarchyBuilder(getResources());
//            GenericDraweeHierarchy hierarchy = builder
//                .setActualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP)
//                .build();
//            RoundingParams roundingParams = new RoundingParams();
//            roundingParams.setRoundAsCircle(true);
//            hierarchy.setRoundingParams(roundingParams);
//            mForegroundImage.setHierarchy(hierarchy);

//            controller.setHierarchy(hierarchy);
//            mForegroundImage.setController(controller);
          }
          mForegroundImage.setImageURI(mImageCaptureUri);
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

    // Coming back to this, I think this was necessary to get the coordinates of the corners
    // TODO: Just get the screen width (which is also the height) then use the padding dimension to calculate

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

}
