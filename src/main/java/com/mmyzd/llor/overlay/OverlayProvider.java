package com.mmyzd.llor.overlay;

import java.util.ArrayList;
import com.mmyzd.llor.config.ConfigManager;

public class OverlayProvider {

  private OverlayPoller overlayPoller;

  public OverlayProvider(ConfigManager configManager) {
    overlayPoller = new OverlayPoller(configManager.getConfig());
    refreshPoller();

    configManager.onUpdate(() -> {
      overlayPoller.setConfig(configManager.getConfig());
      refreshPoller();
    });
  }

  public ArrayList<Overlay> getOverlays() {
    return overlayPoller.getOverlays();
  }

  private void refreshPoller() {
    if (!overlayPoller.getConfig().isOverlayEnabled()) {
      return;
    }
    for (int retryCount = 0; retryCount < 3; retryCount++) {
      if (overlayPoller.isAlive()) {
        return;
      }
      try {
        overlayPoller.start();
      } catch (Exception exception) {
        exception.printStackTrace();
        overlayPoller = new OverlayPoller(overlayPoller.getConfig());
      }
    }
  }
}
