package com.mmyzd.llor.overlay;

import java.util.ArrayList;
import com.mmyzd.llor.config.ConfigManager;
import com.mmyzd.llor.config.ConfigUpdateEvent;
import com.mmyzd.llor.util.EventBusWeakSubscriber;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class OverlayProvider {

  private final ConfigManager configManager;
  private OverlayPoller overlayPoller;

  public OverlayProvider(ConfigManager configManager) {
    this.configManager = configManager;
    overlayPoller = new OverlayPoller(configManager.getConfig());
    refreshPoller();
    MinecraftForge.EVENT_BUS.register(new EventHandler(this));
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

  private static class EventHandler extends EventBusWeakSubscriber<OverlayProvider> {

    private EventHandler(OverlayProvider overlayProvider) {
      super(overlayProvider);
    }

    @SubscribeEvent
    public void onConfigUpdate(ConfigUpdateEvent event) {
      with(overlayProvider -> {
        if (event.getConfigManager() == overlayProvider.configManager) {
          overlayProvider.overlayPoller.setConfig(event.getConfig());
          overlayProvider.refreshPoller();
        }
      });
    }
  }
}
