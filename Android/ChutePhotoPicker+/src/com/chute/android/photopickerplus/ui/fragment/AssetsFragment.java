package com.chute.android.photopickerplus.ui.fragment;

import java.util.ArrayList;

import android.app.Activity;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.chute.android.photopickerplus.R;
import com.chute.android.photopickerplus.loaders.AssetsAsyncTaskLoader;
import com.chute.android.photopickerplus.ui.adapter.PhotoSelectCursorAdapter;
import com.chute.android.photopickerplus.ui.adapter.PhotosAdapter;
import com.chute.android.photopickerplus.util.AppUtil;
import com.chute.android.photopickerplus.util.NotificationUtil;
import com.chute.android.photopickerplus.util.PhotoFilterType;
import com.chute.sdk.v2.api.accounts.GCAccounts;
import com.chute.sdk.v2.model.AccountBaseModel;
import com.chute.sdk.v2.model.AccountMediaModel;
import com.chute.sdk.v2.model.enums.AccountType;
import com.chute.sdk.v2.model.response.ListResponseModel;
import com.dg.libs.rest.callbacks.HttpCallback;
import com.dg.libs.rest.domain.ResponseStatus;

public class AssetsFragment extends Fragment {

	private static final String ARG_FILTER_TYPE = "argFilterType";
	private static final String ARG_ACCOUNT_ID = "argAccountId";
	private static final String ARG_ALBUM_ID = "argAlbumId";
	private static final String ARG_MULTIPICKER = "argMultiPicker";

	private GridView gridViewAssets;
	private PhotoSelectCursorAdapter cursorAdapter;
	private PhotosAdapter socialAdapter;
	private TextView textViewSelectPhotos;
	private View emptyView;

	private boolean isMultipicker;
	private String albumId;
	private PhotoFilterType filterType;
	private AssetFragmentListener assetFragmentListener;

	public interface AssetFragmentListener {
		public void onSelectedCursorItem(AccountMediaModel accountMediaModel, String albumId);

		public void onSelectedSocialItem(AccountMediaModel accountMediaModel, String albumId);

		public void onConfirmedSocialAssets(ArrayList<AccountMediaModel> accountMediaModelList, String albumId);

		public void onConfirmedCursorAssets(ArrayList<String> assetPathList, String albumId);

	}

	public static AssetsFragment newInstance(PhotoFilterType filterType, String accountId, String albumId,
			boolean isMultiPicker) {
		AssetsFragment frag = new AssetsFragment();
		Bundle args = new Bundle();
		args.putSerializable(ARG_FILTER_TYPE, filterType);
		args.putString(ARG_ALBUM_ID, albumId);
		args.putString(ARG_ACCOUNT_ID, accountId);
		args.putBoolean(ARG_MULTIPICKER, isMultiPicker);
		frag.setArguments(args);
		return frag;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		assetFragmentListener = (AssetFragmentListener) activity;

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_assets, container, false);

		textViewSelectPhotos = (TextView) view.findViewById(R.id.textViewSelectPhotos);
		gridViewAssets = (GridView) view.findViewById(R.id.gridViewAssets);
		emptyView = view.findViewById(R.id.empty_view_layout);
		gridViewAssets.setEmptyView(emptyView);

		Button ok = (Button) view.findViewById(R.id.buttonOk);
		Button cancel = (Button) view.findViewById(R.id.buttonCancel);

		ok.setOnClickListener(new OkClickListener());
		cancel.setOnClickListener(new CancelClickListener());

		if (getArguments() != null) {
			updateFragment(getArguments().getString(ARG_ALBUM_ID), getArguments().getString(ARG_ACCOUNT_ID),
					(PhotoFilterType) getArguments().get(ARG_FILTER_TYPE), getArguments().getBoolean(ARG_MULTIPICKER));
		}

		int orientation = getResources().getConfiguration().orientation;
		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			gridViewAssets.setNumColumns(5);
		}

		return view;
	}

	public void updateFragment(String albumId, String accountId, PhotoFilterType filterType, boolean isMultipicker) {
		this.albumId = albumId;
		this.filterType = filterType;
		this.isMultipicker = isMultipicker;

		if ((filterType == PhotoFilterType.ALL_PHOTOS) || (filterType == PhotoFilterType.CAMERA_ROLL)) {
			getActivity().getSupportLoaderManager().initLoader(1, null, new AssetsLoaderCallback());
		} else if (filterType == PhotoFilterType.SOCIAL_PHOTOS) {
			GCAccounts.albumMedia(getActivity().getApplicationContext(), accountId, albumId, new PhotoListCallback())
					.executeAsync();
		}
	}

	private final class AssetsLoaderCallback implements LoaderCallbacks<Cursor> {

		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
			return new AssetsAsyncTaskLoader(getActivity().getApplicationContext(), filterType);
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
			if (cursor == null) {
				return;
			}
			cursorAdapter = new PhotoSelectCursorAdapter(getActivity(), cursor);
			gridViewAssets.setAdapter(cursorAdapter);

			if (cursorAdapter.getCount() == 0) {
				emptyView.setVisibility(View.GONE);
			}

			if (isMultipicker == true) {
				textViewSelectPhotos.setText(getActivity().getApplicationContext().getResources()
						.getString(R.string.select_photos));
				gridViewAssets.setOnItemClickListener(new OnMultiSelectGridItemClickListener());
			} else {
				textViewSelectPhotos.setText(getActivity().getApplicationContext().getResources()
						.getString(R.string.select_a_photo));
				gridViewAssets.setOnItemClickListener(new OnSingleSelectGridItemClickListener());
			}
			NotificationUtil.showPhotosAdapterToast(getActivity().getApplicationContext(), cursorAdapter.getCount());

		}

		@Override
		public void onLoaderReset(Loader<Cursor> loader) {
			// TODO Auto-generated method stub

		}

	}

	private final class OnMultiSelectGridItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
			cursorAdapter.toggleTick(position);
		}
	}

	private final class OnSingleSelectGridItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
			assetFragmentListener.onSelectedCursorItem(AppUtil.getMediaModel(cursorAdapter.getItem(position)), albumId);
		}
	}

	private final class CancelClickListener implements OnClickListener {
		//
		@Override
		public void onClick(View v) {
			// if (!getResources().getBoolean(R.bool.has_two_panes)) {
			getActivity().finish();
			// } else {
			// cancelListener.onCanceled();
			// }
		}

	}

	private final class PhotoListCallback implements HttpCallback<ListResponseModel<AccountMediaModel>> {

		@Override
		public void onSuccess(ListResponseModel<AccountMediaModel> responseData) {
			if (responseData != null && getActivity() != null) {
				socialAdapter = new PhotosAdapter(getActivity(), (ArrayList<AccountMediaModel>) responseData.getData());
				gridViewAssets.setAdapter(socialAdapter);

				if (socialAdapter.getCount() == 0) {
					emptyView.setVisibility(View.GONE);
				}

				if (isMultipicker == true) {
					textViewSelectPhotos.setText(getActivity().getApplicationContext().getResources()
							.getString(R.string.select_photos));
					gridViewAssets.setOnItemClickListener(new OnMultiGridItemClickListener());
				} else {
					textViewSelectPhotos.setText(getActivity().getApplicationContext().getResources()
							.getString(R.string.select_a_photo));
					gridViewAssets.setOnItemClickListener(new OnSingleGridItemClickListener());
				}
				NotificationUtil
						.showPhotosAdapterToast(getActivity().getApplicationContext(), socialAdapter.getCount());
			}
		}

		public void toggleEmptyViewErrorMessage() {
			emptyView.setVisibility(View.GONE);
		}

		@Override
		public void onHttpError(ResponseStatus responseStatus) {
			NotificationUtil.makeConnectionProblemToast(getActivity().getApplicationContext());
			toggleEmptyViewErrorMessage();

		}
	}

	private final class OnSingleGridItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
			assetFragmentListener.onSelectedSocialItem(socialAdapter.getItem(position), albumId);
		}
	}

	private final class OnMultiGridItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
			socialAdapter.toggleTick(position);
		}
	}

	private final class OkClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			if (filterType == PhotoFilterType.SOCIAL_PHOTOS) {
				assetFragmentListener.onConfirmedSocialAssets(socialAdapter.getPhotoCollection(), albumId);
			} else if ((filterType == PhotoFilterType.ALL_PHOTOS) || (filterType == PhotoFilterType.CAMERA_ROLL)) {
				assetFragmentListener.onConfirmedCursorAssets(cursorAdapter.getSelectedFilePaths(), albumId);
			}
		}
	}

}
