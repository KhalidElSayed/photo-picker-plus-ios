package com.chute.android.photopickerplus.config;

import java.util.ArrayList;
import java.util.List;

import com.araneaapps.android.libs.logger.ALog;
import com.chute.android.photopickerplus.models.enums.LocalMediaType;
import com.chute.sdk.v2.model.enums.AccountType;
import com.dg.libs.rest.callbacks.HttpCallback;
import com.dg.libs.rest.domain.ResponseStatus;

public class ServiceLoader {

  private static final String LOG_INIT_CONFIG =
      "Initialize ServiceLoader with configuration";
  private static final String LOG_DESTROY = "Destroy ServiceLoader";

  private static final String WARNING_RE_INIT_CONFIG =
      "Try to initialize ServiceLoader which had already been initialized before. "
          + "To re-init ServiceLoader with new configuration call ServiceLoader.destroy() at first.";
  private static final String ERROR_NOT_INIT =
      "ServiceLoader must be initialized with configuration before using";
  private static final String ERROR_INIT_CONFIG_WITH_NULL =
      "ServiceLoader configuration can not be initialized with null";
  private static final String WARNING_INIT_SERVICES =
      "Local and remote services need to be initialized when starting the application for the first time, otherwise the list of services will be empty";
  private static final String ERROR_HTTP =
      "Error when trying to get services from server: ";

  private List<AccountType> remoteServices;
  private List<LocalMediaType> localServices;

  private ServiceConfiguration configuration;

  private volatile static ServiceLoader instance;

  public static ServiceLoader getInstance() {
    if (instance == null) {
      synchronized (ServiceLoader.class) {
        if (instance == null) {
          instance = new ServiceLoader();
        }
      }
    }
    return instance;
  }

  protected ServiceLoader() {
  }

  public synchronized void init(ServiceConfiguration configuration) {
    if (configuration == null) {
      throw new IllegalArgumentException(ERROR_INIT_CONFIG_WITH_NULL);
    }
    if (this.configuration == null) {
      ALog.d(LOG_INIT_CONFIG);
      this.configuration = configuration;
    } else {
      ALog.w(WARNING_RE_INIT_CONFIG);
    }
    fetchConfigFromServer();
  }

  public void fetchConfigFromServer() {
    checkConfiguration();
    if (configuration.configUrl != null) {
      checkAvailableLocalOrRemoteServices(configuration.configUrl);
    }
  }

  private void checkAvailableLocalOrRemoteServices(String url) {
    if (getLocalServices() == null && getRemoteServices() == null) {
      ALog.w(WARNING_INIT_SERVICES);
    } else {
      fetchConfigFromServer(configuration.configUrl);
    }
  }

  public List<LocalMediaType> getLocalServices() {
    if (localServices == null) {
      if (configuration.localMediaList != null) {
        return configuration.localMediaList;
      } else {
        return new ArrayList<LocalMediaType>();
      }
    } else {
      return localServices;
    }
  }

  public List<AccountType> getRemoteServices() {
    if (remoteServices == null) {
      if (configuration.accountList != null) {
        return configuration.accountList;
      } else {
        return new ArrayList<AccountType>();
      }
    } else {
      return remoteServices;
    }
  }

  public void setAvailableRemoteServices(List<AccountType> remoteServices) {
    this.remoteServices = remoteServices;
  }

  public void setAvailableLocalServices(List<LocalMediaType> localServices) {
    this.localServices = localServices;

  }

  public void fetchConfigFromServer(String url) {
    new ServiceRequest(configuration.context, url, new ConfigServicesCallback())
        .executeAsync();
  }

  private final class ConfigServicesCallback implements
      HttpCallback<ServiceResponseModel> {

    @Override
    public void onHttpError(ResponseStatus status) {
      ALog.d(ERROR_HTTP + status.getStatusMessage() + " " + status.getStatusCode());
    }

    @Override
    public void onSuccess(ServiceResponseModel data) {
      remoteServices = new ArrayList<AccountType>();
      localServices = new ArrayList<LocalMediaType>();
      if (data.getServices() != null) {
        for (String service : data.getServices()) {
          AccountType accountType = AccountType.valueOf(service.toUpperCase());
          remoteServices.add(accountType);
        }
        setAvailableRemoteServices(remoteServices);
      }
      if (data.getLocalFeatures() != null) {
        for (String localFeature : data.getLocalFeatures()) {
          LocalMediaType localMediaType = LocalMediaType.valueOf(localFeature
              .toUpperCase());
          localServices.add(localMediaType);
        }
        setAvailableLocalServices(localServices);
      }
    }

  }

  public boolean isMultiPicker() {
    return configuration.isMultiPicker;

  }

  private void checkConfiguration() {
    if (configuration == null) {
      throw new IllegalStateException(ERROR_NOT_INIT);
    }
  }

  public void destroy() {
    if (configuration != null)
      ALog.d(LOG_DESTROY);
    configuration = null;
  }

}
