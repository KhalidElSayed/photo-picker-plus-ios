/*
 *  Copyright (c) 2012 Chute Corporation

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.chute.android.photopickerplus.ui.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Window;
import android.widget.Toast;

import com.araneaapps.android.libs.logger.ALog;
import com.chute.android.photopickerplus.R;
import com.chute.android.photopickerplus.ui.adapter.AssetSelectListener;
import com.chute.android.photopickerplus.ui.fragment.AccountFilesListener;
import com.chute.android.photopickerplus.ui.fragment.CursorFilesListener;
import com.chute.android.photopickerplus.ui.fragment.FragmentRoot;
import com.chute.android.photopickerplus.ui.fragment.FragmentServices;
import com.chute.android.photopickerplus.ui.fragment.FragmentSingle;
import com.chute.android.photopickerplus.util.AppUtil;
import com.chute.android.photopickerplus.util.Constants;
import com.chute.android.photopickerplus.util.PhotoFilterType;
import com.chute.android.photopickerplus.util.PhotoPickerPreferenceUtil;
import com.chute.android.photopickerplus.util.intent.IntentUtil;
import com.chute.android.photopickerplus.util.intent.PhotosIntentWrapper;
import com.chute.sdk.v2.api.accounts.GCAccounts;
import com.chute.sdk.v2.api.authentication.AuthenticationFactory;
import com.chute.sdk.v2.model.AccountMediaModel;
import com.chute.sdk.v2.model.AccountModel;
import com.chute.sdk.v2.model.enums.AccountType;
import com.chute.sdk.v2.model.response.ListResponseModel;
import com.chute.sdk.v2.utils.PreferenceUtil;
import com.dg.libs.rest.callbacks.HttpCallback;
import com.dg.libs.rest.domain.ResponseStatus;

public class AssetActivity extends FragmentActivity implements CursorFilesListener,
    AccountFilesListener {

  public static final String TAG = AssetActivity.class.getSimpleName();
  private String accountID;
  private PhotoFilterType filterType;
  private PhotosIntentWrapper wrapper;
  private FragmentRoot fragmentRoot;
  private FragmentSingle fragmentSingle;
  private String accountName;
  private String accountShortcut;
  private ArrayList<Integer> selectedItemsPositions;
  private AssetSelectListener assetSelectListener;
  private String folderId;
  private static FragmentManager fragmentManager;
  private FragmentServices fragmentServices;
  private AccountType accountType;

  public AssetSelectListener getAdapterListener() {
    return assetSelectListener;
  }

  public void setAssetSelectListener(AssetSelectListener adapterListener) {
    this.assetSelectListener = adapterListener;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    requestWindowFeature(Window.FEATURE_NO_TITLE);

    setContentView(R.layout.activity_assets);

    selectedItemsPositions = savedInstanceState != null ? savedInstanceState
        .getIntegerArrayList(Constants.KEY_SELECTED_ITEMS)
        : null;

    folderId = savedInstanceState != null ? savedInstanceState
        .getString(Constants.KEY_FOLDER_ID)
        : null;

    wrapper = new PhotosIntentWrapper(getIntent());
    accountID = wrapper.getAccountId();
    filterType = wrapper.getFilterType();
    accountName = wrapper.getAccountName();
    accountShortcut = wrapper.getAccountShortcut();

    fragmentRoot = (FragmentRoot) getSupportFragmentManager().findFragmentById(
        R.id.fragmentAssets);
    fragmentRoot.setRetainInstance(true);
    fragmentRoot.updateFragment(accountID, filterType,
        selectedItemsPositions,
        accountName,
        accountShortcut);
  }

  @Override
  public void onAccountFilesSelect(AccountMediaModel accountMediaModel) {
    IntentUtil.deliverDataToInitialActivity(AssetActivity.this, accountMediaModel);
    setResult(RESULT_OK);
    finish();

  }

  @Override
  public void onCursorAssetsSelect(AccountMediaModel accountMediaModel) {
    IntentUtil.deliverDataToInitialActivity(AssetActivity.this, accountMediaModel);
    setResult(RESULT_OK);
    finish();

  }

  @Override
  public void onDeliverCursorAssets(ArrayList<String> assetPathList) {
    IntentUtil.deliverDataToInitialActivity(AssetActivity.this,
        AppUtil.getPhotoCollection(assetPathList));
    setResult(RESULT_OK);
    finish();

  }

  @Override
  public void onDeliverAccountFiles(ArrayList<AccountMediaModel> accountMediaModelList) {
    IntentUtil.deliverDataToInitialActivity(AssetActivity.this, accountMediaModelList);
    setResult(RESULT_OK);
    finish();

  }

  @Override
  public void onAccountFolderSelect(String accountName, String accountShortcut,
      String folderId) {
    this.folderId = folderId;
    FragmentTransaction fragmentTransaction = getSupportFragmentManager()
        .beginTransaction();
    fragmentTransaction
        .replace(R.id.fragments, FragmentSingle.newInstance(accountName, accountShortcut,
            folderId, selectedItemsPositions),
            Constants.TAG_FRAGMENT_FILES);
    fragmentTransaction.addToBackStack(null);
    fragmentTransaction.commit();

  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString(Constants.KEY_FOLDER_ID, folderId);
    if (assetSelectListener != null
        && assetSelectListener.getSelectedItemPositions() !=
        null) {
      outState.putIntegerArrayList(Constants.KEY_SELECTED_ITEMS,
          assetSelectListener
              .getSelectedItemPositions());
    }

  }

  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    fragmentSingle = (FragmentSingle) getSupportFragmentManager().findFragmentByTag(
        Constants.TAG_FRAGMENT_FILES);
    if (fragmentSingle != null) {
      fragmentSingle.setRetainInstance(true);
      fragmentSingle.updateFragment(accountName, accountShortcut, folderId,
          selectedItemsPositions);
    }
  }

  @Override
  public void onSessionExpired(AccountType accountType) {
    PhotoPickerPreferenceUtil.get().setAccountType(accountType);
    AuthenticationFactory.getInstance().startAuthenticationActivity(
        AssetActivity.this, accountType);

  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == Activity.RESULT_OK) {
      GCAccounts.allUserAccounts(getApplicationContext(), new AccountsCallback())
          .executeAsync();
    }
  }

  private final class AccountsCallback implements
      HttpCallback<ListResponseModel<AccountModel>> {

    @Override
    public void onSuccess(ListResponseModel<AccountModel> responseData) {
      if (accountType == null) {
        accountType = PhotoPickerPreferenceUtil.get().getAccountType();
      }
      if (responseData.getData().size() == 0) {
        Toast.makeText(getApplicationContext(),
            getResources().getString(R.string.no_albums_found),
            Toast.LENGTH_SHORT).show();
        return;
      }
      for (AccountModel accountModel : responseData.getData()) {
        if (accountModel.getType().equals(accountType.getLoginMethod())) {
          PreferenceUtil.get().saveAccount(accountModel);
          accountClicked(accountModel.getId(), accountType.name().toLowerCase(),
              accountModel.getShortcut());
        }
      }
    }

    @Override
    public void onHttpError(ResponseStatus responseStatus) {
      ALog.d("Http Error: " + responseStatus.getStatusCode() + " "
          + responseStatus.getStatusMessage());
    }

  }

  public void accountClicked(String accountId, String accountName, String accountShortcut) {
    selectedItemsPositions = null;
    if (fragmentRoot != null) {
      fragmentRoot.setRetainInstance(true);
      fragmentRoot.updateFragment(accountId, filterType,
          selectedItemsPositions,
          accountName,
          accountShortcut);
    }
  }

}