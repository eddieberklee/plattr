package com.compscieddy.plattr.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.EditText;

import com.compscieddy.plattr.FontCache;
import com.compscieddy.plattr.R;

/**
 * Created by elee on 1/7/16.
 */
public class FontEditText extends EditText {

  public FontEditText(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  private void init(Context context, AttributeSet attrs) {
    if (isInEditMode()) return;

    TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.FontEditText);
    int typefaceId = ta.getInt(R.styleable.FontEditText_fontface, FontCache.MONTSERRAT_REGULAR);
    setTypeface(FontCache.get(context, typefaceId));
    ta.recycle();
  }

}
