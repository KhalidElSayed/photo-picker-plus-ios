package com.chute.android.photopickerplus.config;

import java.util.Arrays;
import java.util.List;

import android.content.Context;

import com.chute.android.photopickerplus.models.enums.LocalMediaType;
import com.chute.sdk.v2.model.enums.AccountType;

public final class ServiceConfiguration {

  final Context context;
  final List<AccountType> accountList;
  final List<LocalMediaType> localMediaList;
  final String configUrl;
  final boolean isMultiPicker;

  private ServiceConfiguration(final Builder builder) {
    context = builder.context;
    isMultiPicker = builder.isMultiPicker;
    accountList = builder.accountList;
    localMediaList = builder.localMediaList;
    configUrl = builder.configUrl;
  }

  public static class Builder {

    private Context context;
    private boolean isMultiPicker = false;
    private List<AccountType> accountList = null;
    private List<LocalMediaType> localMediaList = null;
    private String configUrl = null;

    public Builder(Context context) {
      this.context = context.getApplicationContext();
    }

    public ServiceConfiguration build() {
      return new ServiceConfiguration(this);
    }

    public Builder accountList(AccountType... accountList) {
      this.accountList = Arrays.asList(accountList);
      return this;
    }

    public Builder localMediaList(LocalMediaType... localMediaList) {
      this.localMediaList = Arrays.asList(localMediaList);
      return this;
    }

    public Builder configUrl(String configUrl) {
      this.configUrl = configUrl;
      return this;
    }

    public Builder isMultiPicker(boolean isMultiPicker) {
      this.isMultiPicker = isMultiPicker;
      return this;
    }

  }
}
