package com.chute.android.photopickerplus.ui.fragment;

import java.util.ArrayList;

import android.app.Activity;
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
import com.chute.android.photopickerplus.util.intent.PhotosIntentWrapper;
import com.chute.sdk.v2.api.accounts.GCAccounts;
import com.chute.sdk.v2.model.AccountMediaModel;
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
	private int filterType;

	private GridCursorSingleSelectListener gridCursorSelectItemListener;
	private GridSocialSingleSelectListener gridSocialSelectItemListener;
	private ButtonCancelListener cancelListener;
	private ButtonConfirmCursorAssetsListener confirmCursorAssetsListener;
	private ButtonConfirmSocialAssetsListener confirmSocialAssetsListener;

	public interface GridCursorSingleSelectListener {
		public void onSelectedCursorItem(AccountMediaModel accountMediaModel, String albumId);
	}

	public interface GridSocialSingleSelectListener {
		public void onSelectedSocialItem(AccountMediaModel accountMediaModel, String albumId);
	}

	public interface ButtonCancelListener {
		public void onCanceled();
	}

	public interface ButtonConfirmSocialAssetsListener {
		public void onOkClicked(ArrayList<AccountMediaModel> accountMediaModelList, String albumId);
	}

	public interface ButtonConfirmCursorAssetsListener {
		public void onOkClicked(ArrayList<String> assetPathList, String albumId);
	}

	public static AssetsFragment newInstance(int filterType, String accountId, String albumId, boolean isMultiPicker) {
		AssetsFragment frag = new AssetsFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_FILTER_TYPE, filterType);
		args.putString(ARG_ALBUM_ID, albumId);
		args.putString(ARG_ACCOUNT_ID, accountId);
		args.putBoolean(ARG_MULTIPICKER, isMultiPicker);
		frag.setArguments(args);
		return frag;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		gridCursorSelectItemListener = (GridCursorSingleSelectListener) activity;
		gridSocialSelectItemListener = (GridSocialSingleSelectListener) activity;
		cancelListener = (ButtonCancelListener) activity;
		confirmCursorAssetsListener = (ButtonConfirmCursorAssetsListener) activity;
		confirmSocialAssetsListener = (ButtonConfirmSocialAssetsListener) activity;

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_assets, container, false);

		filterType = getArguments().getInt(ARG_FILTER_TYPE);
		String accountId = getArguments().getString(ARG_ACCOUNT_ID);
		albumId = getArguments().getString(ARG_ALBUM_ID);
		isMultipicker = getArguments().getBoolean(ARG_MULTIPICKER);

		textViewSelectPhotos = (TextView) view.findViewById(R.id.textViewSelectPhotos);
		gridViewAssets = (GridView) view.findViewById(R.id.gridViewAssets);
		emptyView = view.findViewById(R.id.empty_view_layout);
		gridViewAssets.setEmptyView(emptyView);

		Button ok = (Button) view.findViewById(R.id.buttonOk);
		ok.setOnClickListener(new OkClickListener());
		Button cancel = (Button) view.findViewById(R.id.buttonCancel);
		cancel.setOnClickListener(new CancelClickListener());

		if ((filterType == PhotosIntentWrapper.TYPE_ALL_PHOTOS) || (filterType == PhotosIntentWrapper.TYPE_CAMERA_ROLL)) {
			getActivity().getSupportLoaderManager().initLoader(1, null, new AssetsLoaderCallback(filterType));
		} else if (filterType == PhotosIntentWrapper.TYPE_SOCIAL_PHOTOS) {
			GCAccounts.objectMedia(getActivity().getApplicationContext(), accountId, albumId, new PhotoListCallback())
					.executeAsync();
		}

		return view;
	}

	private final class AssetsLoaderCallback implements LoaderCallbacks<Cursor> {

		private int filterType;

		private AssetsLoaderCallback(int filterType) {
			this.filterType = filterType;
		}

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
			gridCursorSelectItemListener.onSelectedCursorItem(AppUtil.getMediaModel(cursorAdapter.getItem(position)),
					albumId);
		}
	}

	private final class CancelClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			cancelListener.onCanceled();
		}

	}

	private final class PhotoListCallback implements HttpCallback<ListResponseModel<AccountMediaModel>> {

		@Override
		public void onSuccess(ListResponseModel<AccountMediaModel> responseData) {
			Log.d("debug", "responsedata = " + responseData.getData().toString());
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
			NotificationUtil.showPhotosAdapterToast(getActivity().getApplicationContext(), socialAdapter.getCount());
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
			gridSocialSelectItemListener.onSelectedSocialItem(socialAdapter.getItem(position), albumId);
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
			if (filterType == PhotosIntentWrapper.TYPE_SOCIAL_PHOTOS) {
				confirmSocialAssetsListener.onOkClicked(socialAdapter.getPhotoCollection(), albumId);
			} else if ((filterType == PhotosIntentWrapper.TYPE_ALL_PHOTOS)
					|| (filterType == PhotosIntentWrapper.TYPE_CAMERA_ROLL)) {
				confirmCursorAssetsListener.onOkClicked(cursorAdapter.getSelectedFilePaths(), albumId);
			}
		}
	}

}
