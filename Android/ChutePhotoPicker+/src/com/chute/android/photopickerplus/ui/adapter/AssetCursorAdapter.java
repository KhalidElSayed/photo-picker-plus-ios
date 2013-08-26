package com.chute.android.photopickerplus.ui.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.chute.android.photopickerplus.R;
import com.chute.android.photopickerplus.ui.activity.AssetActivity;
import com.chute.android.photopickerplus.ui.activity.ServicesActivity;
import com.chute.android.photopickerplus.util.AppUtil;

import darko.imagedownloader.ImageLoader;

public class AssetCursorAdapter extends CursorAdapter implements
    OnScrollListener, AssetSelectListener {

  public static final String TAG = AssetCursorAdapter.class.getSimpleName();

  private static LayoutInflater inflater = null;
  public ImageLoader loader;
  private final int dataIndex;
  public HashMap<Integer, String> tick;
  private boolean shouldLoadImages = true;

  @SuppressWarnings("deprecation")
  public AssetCursorAdapter(FragmentActivity context, Cursor c) {
    super(context, c);
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    loader = ImageLoader.getLoader(context);
    dataIndex = c.getColumnIndex(MediaStore.Images.Media.DATA);
    tick = new HashMap<Integer, String>();
    if (context.getResources().getBoolean(R.bool.has_two_panes)) {
      ((ServicesActivity) context).setAssetSelectListener(this);
    } else {
      ((AssetActivity) context).setAssetSelectListener(this);
    }

  }

  public static class ViewHolder {

    public ImageView imageViewThumb;
    public ImageView imageViewTick;
  }

  @Override
  public void bindView(View view, Context context, Cursor cursor) {
    ViewHolder holder = (ViewHolder) view.getTag();
    String path = cursor.getString(dataIndex);
    holder.imageViewThumb.setTag(path);
    holder.imageViewTick.setTag(cursor.getPosition());
    if (shouldLoadImages) {
      loader.displayImage(Uri.fromFile(new File(path)).toString(), holder.imageViewThumb,
          null);
    }
    AppUtil.configureAssetImageViewDimensions(context, holder.imageViewThumb);
    if (tick.containsKey(cursor.getPosition())) {
      holder.imageViewTick.setVisibility(View.VISIBLE);
      view.setBackgroundColor(context.getResources().getColor(R.color.sky_blue));
    } else {
      holder.imageViewTick.setVisibility(View.GONE);
      view.setBackgroundColor(context.getResources().getColor(R.color.gray_light));
    }
  }

  @Override
  public View newView(Context context, Cursor cursor, ViewGroup parent) {
    ViewHolder holder;
    View vi = inflater.inflate(R.layout.adapter_assets, null);
    holder = new ViewHolder();
    holder.imageViewThumb = (ImageView) vi.findViewById(R.id.imageViewThumb);
    holder.imageViewTick = (ImageView) vi.findViewById(R.id.imageViewTick);
    vi.setTag(holder);
    return vi;
  }

  @Override
  public String getItem(int position) {
    final Cursor cursor = getCursor();
    cursor.moveToPosition(position);
    return cursor.getString(dataIndex);
  }

  @Override
  public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
      int totalItemCount) {
    // Do nothing

  }

  @Override
  public void onScrollStateChanged(AbsListView view, int scrollState) {
    switch (scrollState) {
    case OnScrollListener.SCROLL_STATE_FLING:
    case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
      shouldLoadImages = false;
      break;
    case OnScrollListener.SCROLL_STATE_IDLE:
      shouldLoadImages = true;
      notifyDataSetChanged();
      break;
    }
  }

  public ArrayList<String> getSelectedFilePaths() {
    final ArrayList<String> photos = new ArrayList<String>();
    final Iterator<String> iterator = tick.values().iterator();
    while (iterator.hasNext()) {
      photos.add(iterator.next());
    }
    return photos;
  }

  public boolean hasSelectedItems() {
    return tick.size() > 0;
  }

  public int getSelectedItemsCount() {
    return tick.size();
  }

  public void toggleTick(int position) {
    if (getCount() >= position) {
    if (tick.containsKey(position)) {
      tick.remove(position);
    } else {
      tick.put(position, getItem(position));
    }
    }
    notifyDataSetChanged();
  }

  @Override
  public ArrayList<Integer> getSelectedItemPositions() {
    final ArrayList<Integer> positions = new ArrayList<Integer>();
    final Iterator<Integer> iterator = tick.keySet().iterator();
    while (iterator.hasNext()) {
      positions.add(iterator.next());
    }
    return positions;
  }

}
