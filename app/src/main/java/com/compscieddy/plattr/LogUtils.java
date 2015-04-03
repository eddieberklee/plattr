package com.compscieddy.plattr;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by elee on 4/2/15.
 */
public class LogUtils {

  public static void errorAndToast(Context context, String TAG, String errorMessage, Exception e) {
    Log.e(TAG, errorMessage, e);
    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
  }

}
