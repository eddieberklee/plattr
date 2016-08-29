package com.compscieddy.plattr;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;

/**
 * Created by elee on 8/24/16.
 */

public class TwiceApplication extends Application {
  @Override
  public void onCreate() {
    super.onCreate();
    Fresco.initialize(this);
  }
}
