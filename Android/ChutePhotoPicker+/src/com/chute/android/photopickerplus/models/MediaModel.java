package com.chute.android.photopickerplus.models;

import java.util.List;

import com.araneaapps.android.libs.logger.ALog;
import com.chute.sdk.v2.utils.JsonUtil;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

@JsonFilter("imageDataModelFilter")
public class MediaModel {
  
  @JsonProperty("options")
  private OptionsModel options;

  @JsonProperty("media")
  private List<MediaDataModel> media;


  public OptionsModel getOptions() {
    return options;
  }

  public void setOptions(OptionsModel options) {
    this.options = options;
  }

  public List<MediaDataModel> getMedia() {
    return media;
  }

  public void setMedia(List<MediaDataModel> media) {
    this.media = media;
  }

  public String serializeImageDataModel() {
    FilterProvider filters = new SimpleFilterProvider().addFilter("imageDataModelFilter",
        SimpleBeanPropertyFilter.filterOutAllExcept("options", "media"));
    String result = null;
    try {
      result = JsonUtil.getMapper().writer(filters).writeValueAsString(this);
    } catch (JsonProcessingException e) {
      ALog.d("", e);
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("ImageDataModel [options=");
    builder.append(options);
    builder.append(", media=");
    builder.append(media);
    builder.append("]");
    return builder.toString();
  }

  
}
