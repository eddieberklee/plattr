package com.compscieddy.plattr;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;

public class GalleryActivity extends Activity {

  private static final Lawg L = Lawg.newInstance(GalleryActivity.class.getSimpleName());

  private int count;
  private Bitmap[] thumbnailBitmaps;
  private boolean[] thumbnailsselection;
  private String[] imagePaths;
  private ImageAdapter imageAdapter;

  /**
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_gallery);

    final String[] columns = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID };
    final String orderBy = MediaStore.Images.Media._ID;
    // todo: read up on and use LoaderManager instead
    Cursor imagesCursor = managedQuery(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null,
        null, orderBy);
    int image_column_index = imagesCursor.getColumnIndex(MediaStore.Images.Media._ID);
    this.count = imagesCursor.getCount();
    this.thumbnailBitmaps = new Bitmap[this.count];
    this.imagePaths = new String[this.count];
    this.thumbnailsselection = new boolean[this.count];
    for (int i = 0; i < this.count; i++) {
      imagesCursor.moveToPosition(i);
      int id = imagesCursor.getInt(image_column_index);
      int dataColumnIndex = imagesCursor.getColumnIndex(MediaStore.Images.Media.DATA);
      thumbnailBitmaps[i] = MediaStore.Images.Thumbnails.getThumbnail(
          getApplicationContext().getContentResolver(), id,
          MediaStore.Images.Thumbnails.MICRO_KIND, null);
      imagePaths[i] = imagesCursor.getString(dataColumnIndex);
    }
    GridView imagegrid = (GridView) findViewById(R.id.PhoneImageGrid);
    imageAdapter = new ImageAdapter();
    imagegrid.setAdapter(imageAdapter);
    imagesCursor.close();

    final Button selectBtn = (Button) findViewById(R.id.selectBtn);
    selectBtn.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        // TODO Auto-generated method stub
        final int len = thumbnailsselection.length;
        int cnt = 0;
        String selectImages = "";
        for (int i = 0; i < len; i++) {
          if (thumbnailsselection[i]) {
            cnt++;
            selectImages = selectImages + imagePaths[i] + "|";
          }
        }
        if (cnt == 0) {
          Toast.makeText(getApplicationContext(),
              "Please select at least one imageView",
              Toast.LENGTH_LONG).show();
        } else {
          Toast.makeText(getApplicationContext(),
              "You've selected Total " + cnt + " imageView(s).",
              Toast.LENGTH_LONG).show();
          Log.d("SelectedImages", selectImages);
        }
      }
    });
  }

  public class ImageAdapter extends BaseAdapter {
    private LayoutInflater mInflater;

    public ImageAdapter() {
      mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
      return count;
    }

    public Object getItem(int position) {
      return position;
    }

    public long getItemId(int position) {
      return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
      ViewHolder holder;
      if (convertView == null) {
        holder = new ViewHolder();
        convertView = mInflater.inflate(R.layout.item_gallery_image, null);
        holder.image = (SimpleDraweeView) convertView.findViewById(R.id.thumbnail_image);
        holder.checkbox = (CheckBox) convertView.findViewById(R.id.item_checkbox);

        convertView.setTag(holder);
      } else {
        holder = (ViewHolder) convertView.getTag();
      }
      holder.checkbox.setId(position);
      holder.image.setId(position);
      holder.checkbox.setOnClickListener(new View.OnClickListener() {

        public void onClick(View v) {
          // TODO Auto-generated method stub
          CheckBox cb = (CheckBox) v;
          int id = cb.getId();
          if (thumbnailsselection[id]) {
            cb.setChecked(false);
            thumbnailsselection[id] = false;
          } else {
            cb.setChecked(true);
            thumbnailsselection[id] = true;
          }
        }
      });
      holder.image.setOnClickListener(new View.OnClickListener() {

        public void onClick(View v) {
          // TODO Auto-generated method stub
          int id = v.getId();
          Intent intent = new Intent();
          intent.setAction(Intent.ACTION_VIEW);
          intent.setDataAndType(Uri.parse("file://" + imagePaths[id]), "imageView/*");
          startActivity(intent);
        }
      });
      holder.image.setImageURI(imagePaths[position]);
      holder.checkbox.setChecked(thumbnailsselection[position]);
      holder.id = position;
      return convertView;
    }
  }

  class ViewHolder {
    SimpleDraweeView image;
    CheckBox checkbox;
    int id;
  }
}