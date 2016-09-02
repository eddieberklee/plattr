package com.compscieddy.plattr;

/**
 * Created by elee on 9/2/16.
 */

public class GalleryImageModel {
  String name, url;
  public GalleryImageModel(String url) {
    this.url = url;
  }

  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public String getUrl() {
    return url;
  }
  public void setUrl(String url) {
    this.url = url;
  }
}
