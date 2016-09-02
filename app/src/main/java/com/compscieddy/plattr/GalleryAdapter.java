package com.compscieddy.plattr;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import butterknife.ButterKnife;

/**
 * Created by elee on 9/2/16.
 */

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {

  private List<GalleryImageModel> mGalleryImages;
  private final Context mContext;

  public GalleryAdapter(Context context, List<GalleryImageModel> galleryImages) {
    mContext = context;
    mGalleryImages = galleryImages;
  }

  @Override
  public int getItemCount() {
    return mGalleryImages.size();
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    Context context = parent.getContext();
    LayoutInflater inflater = LayoutInflater.from(context);
    View galleryImageView = inflater.inflate(R.layout.item_gallery, parent, false);
    ViewHolder viewHolder = new ViewHolder(galleryImageView);
    return viewHolder;
  }

  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    GalleryImageModel galleryImageModel = mGalleryImages.get(position);
    ImageView galleryImageView = holder.imageView;
    Glide.with(mContext).load(galleryImageModel.getUrl())
        .thumbnail(0.5f)
        .crossFade()
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(galleryImageView);
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    public ImageView imageView;
    public ViewHolder(View itemView) {
      super(itemView);
      imageView = ButterKnife.findById(itemView, R.id.gallery_image);
    }
  }

}
