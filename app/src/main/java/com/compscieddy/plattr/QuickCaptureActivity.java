package com.compscieddy.plattr;

import android.app.Activity;
import android.app.NotificationManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.facebook.drawee.view.DraweeView;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by elee on 9/2/16.
 */

public class QuickCaptureActivity extends Activity implements ViewTreeObserver.OnGlobalLayoutListener {

  @Bind(R.id.background_image) DraweeView mBackgroundImage;
  @Bind(R.id.foreground_image) DraweeView mForegroundImage;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_quick_capture);
    ButterKnife.bind(this);
    mBackgroundImage.getViewTreeObserver().addOnGlobalLayoutListener(this);
    mForegroundImage.setOnTouchListener(mForegroundImageOnTouchListener);

    NotificationCompat.Builder mBuilder =
        new NotificationCompat.Builder(this)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("My notification")
            .setContentText("Hello World!");
    int mNotificationId = 001;
    NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    mNotificationManager.notify(mNotificationId, mBuilder.build());
  }

  private Point[] CORNER_COORDINATES = new Point[4];
  private int mActivePointerIndex;

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
