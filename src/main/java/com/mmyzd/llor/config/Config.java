package com.mmyzd.llor.config;

import com.mmyzd.llor.displaymode.DisplayMode;

public class Config {

  private final boolean overlayEnabled;
  private final int renderingRadius;
  private final int pollingInterval;
  private final DisplayMode displayMode;

  public boolean isOverlayEnabled() {
    return overlayEnabled;
  }

  public int getRenderingRadius() {
    return renderingRadius;
  }

  public int getPollingInterval() {
    return pollingInterval;
  }

  public DisplayMode getDisplayMode() {
    return displayMode;
  }

  public Config(Builder builder) {
    overlayEnabled = builder.overlayEnabled;
    renderingRadius = builder.renderingRadius;
    pollingInterval = builder.pollingInterval;
    displayMode = builder.displayMode;
  }

  public static class Builder {

    private boolean overlayEnabled;
    private int renderingRadius;
    private int pollingInterval;
    private DisplayMode displayMode;

    public Builder() {
      overlayEnabled = false;
      renderingRadius = 0;
      pollingInterval = 0;
      displayMode = DisplayMode.NULL;
    }

    public void setOverlayEnabled(boolean overlayEnabled) {
      this.overlayEnabled = overlayEnabled;
    }

    public void setRenderingRadius(int renderingRadius) {
      this.renderingRadius = renderingRadius;
    }

    public void setPollingInterval(int pollingInterval) {
      this.pollingInterval = pollingInterval;
    }

    public void setDisplayMode(DisplayMode displayMode) {
      this.displayMode = displayMode;
    }

    public Config build() {
      return new Config(this);
    }
  }
}
