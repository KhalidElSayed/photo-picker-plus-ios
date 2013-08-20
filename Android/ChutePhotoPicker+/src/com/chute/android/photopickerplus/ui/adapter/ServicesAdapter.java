/*
 *  Copyright (c) 2012 Chute Corporation

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.chute.android.photopickerplus.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chute.android.photopickerplus.R;
import com.chute.android.photopickerplus.dao.MediaDAO;
import com.chute.sdk.v2.model.enums.Service;

import darko.imagedownloader.ImageLoader;

public class ServicesAdapter extends BaseAdapter {

  @SuppressWarnings("unused")
  private static final String TAG = ServicesAdapter.class.getSimpleName();

  private static LayoutInflater inflater;
  public ImageLoader loader;
  private final DisplayMetrics displayMetrics;
  private final Activity context;

  private Service[] services;

  public ServicesAdapter(final Activity context, final Service[] services) {
    this.services = services;
    this.context = context;
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    loader = ImageLoader.getLoader(context);
    displayMetrics = context.getResources().getDisplayMetrics();
  }

  @Override
  public int getCount() {
    return services.length;
  }

  @Override
  public Object getItem(final int position) {
    return position;
  }

  @Override
  public long getItemId(final int position) {
    return position;
  }

  public static class ViewHolder {

    public ImageView imageView;
    public TextView textViewServiceTitle;
  }

  @Override
  public View getView(final int position, final View convertView, final ViewGroup parent) {
    View vi = convertView;
    ViewHolder holder;
    if (convertView == null) {
      vi = inflater.inflate(R.layout.adapter_services, null);
      holder = new ViewHolder();
      holder.imageView = (ImageView) vi.findViewById(R.id.imageViewService);
      holder.textViewServiceTitle = (TextView) vi.findViewById(R.id.textViewServiceTitle);
      configureImageViewDimensions(holder.imageView, holder.textViewServiceTitle);
      vi.setTag(holder);
    } else {
      holder = (ViewHolder) vi.getTag();
    }
    setImageViewBackground(holder.imageView, holder.textViewServiceTitle, position);
    return vi;
  }

  private void configureImageViewDimensions(ImageView imageViewThumb,
      TextView textViewServiceTitle) {
    int gridColumns = context.getResources().getInteger(R.integer.grid_columns_services);
    int imageDimension = context.getResources().getInteger(
        R.integer.image_dimensions_services);
    int imageViewDimension = displayMetrics.widthPixels - imageDimension;
    imageViewThumb.setLayoutParams(new RelativeLayout.LayoutParams(imageViewDimension
        / gridColumns,
        imageViewDimension / gridColumns));
  }

  @SuppressWarnings("deprecation")
  private void setImageViewBackground(ImageView imageView, TextView serviceTitle,
      int position) {
    Uri uriAllPhotos = MediaDAO
        .getLastPhotoFromAllPhotos(context.getApplicationContext());
    Uri uriLastPhotoFromCameraPhotos = MediaDAO.getLastPhotoFromCameraPhotos(context
        .getApplicationContext());
    Service service = services[position];
    imageView.setTag(position);
    switch (service) {
    case FACEBOOK:
      imageView.setBackgroundDrawable(context.getResources().getDrawable(
          R.drawable.facebook));
      serviceTitle.setVisibility(View.GONE);
      break;
    case FLICKR:
      imageView.setBackgroundDrawable(context.getResources().getDrawable(
          R.drawable.flickr));
      serviceTitle.setVisibility(View.GONE);
      break;
    case PICASA:
      imageView.setBackgroundDrawable(context.getResources().getDrawable(
          R.drawable.picassa));
      serviceTitle.setVisibility(View.GONE);
      break;
    case INSTAGRAM:
      imageView.setBackgroundDrawable(context.getResources().getDrawable(
          R.drawable.instagram));
      serviceTitle.setVisibility(View.GONE);
      break;
    case TAKE_PHOTO:
      imageView.setBackgroundDrawable(context.getResources().getDrawable(
          R.drawable.take_photo));
      serviceTitle.setText(context.getResources().getString(R.string.take_photos));
      break;
    case CAMERA_SHOTS:
      if (uriLastPhotoFromCameraPhotos != null) {
        loader.displayImage(uriLastPhotoFromCameraPhotos.toString(), imageView, null);
      } else {
        imageView.setBackgroundDrawable(context.getResources().getDrawable(
            R.drawable.photo_placeholder));
      }
      serviceTitle.setText(context.getResources().getString(R.string.camera_shots));
      break;
    case LAST_PHOTO_TAKEN:
      if (uriLastPhotoFromCameraPhotos != null) {
        loader.displayImage(uriLastPhotoFromCameraPhotos.toString(), imageView, null);
      } else {
        imageView.setBackgroundDrawable(context.getResources().getDrawable(
            R.drawable.photo_placeholder));
      }
      serviceTitle.setText(context.getResources().getString(R.string.last_photo));
      break;
    case ALL_PHOTOS:
      if (uriAllPhotos != null) {
        loader.displayImage(uriAllPhotos.toString(), imageView, null);
      } else {
        imageView.setBackgroundDrawable(context.getResources().getDrawable(
            R.drawable.photo_placeholder));
      }
      serviceTitle.setText(context.getResources().getString(R.string.all_photos));
      break;
    default:
      break;
    }
  }

}
