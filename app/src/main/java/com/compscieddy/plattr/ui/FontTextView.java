package com.compscieddy.plattr.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.TextView;

import com.compscieddy.plattr.FontCache;
import com.compscieddy.plattr.R;

/**
 * Created by elee on 1/6/16.
 */
public class FontTextView extends TextView {

  public FontTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  private void init(Context context, AttributeSet attrs) {
    if (isInEditMode()) return;

    TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.FontTextView);
    int typefaceId = ta.getInt(R.styleable.FontTextView_fontface, FontCache.MONTSERRAT_REGULAR);
    setTypeface(FontCache.get(context, typefaceId));
    ta.recycle();

  }


}
