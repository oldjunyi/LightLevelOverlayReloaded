package com.mmyzd.llor.overlay;

import java.util.ArrayList;
import com.mmyzd.llor.config.Config;
import com.mmyzd.llor.config.ConfigManager;
import com.mmyzd.llor.event.WeakEventSubscriber;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class OverlayProvider {

  private final ConfigManager configManager;
  private final OverlayPoller overlayPoller = new OverlayPoller();
  private long tickIndex = 0;

  public OverlayProvider(ConfigManager configManager) {
    this.configManager = configManager;
    MinecraftForge.EVENT_BUS.register(new EventHandler(this));
  }

  public ArrayList<Overlay> getOverlays() {
    return overlayPoller.getOverlays();
  }

  private void tick() {
    Config config = configManager.getConfig();
    ++tickIndex;
    overlayPoller.updateOverlays(config, (chunkOffsetX, chunkOffsetZ) -> {
      int pollingInterval = config.getPollingInterval();
      if (pollingInterval > 0 && tickIndex % pollingInterval != 0) {
        return false;
      }
      int chunkDistanceX = Math.abs(chunkOffsetX);
      int chunkDistanceZ = Math.abs(chunkOffsetZ);
      int chunkDistance = Math.max(chunkDistanceX, chunkDistanceZ);
      if (chunkDistance <= 1) {
        return true;
      }
      int totalPhase = chunkDistance * 2;
      int chunkPhase = (chunkOffsetX + chunkOffsetZ + totalPhase) % totalPhase;
      int tickPhase = (int) (tickIndex / pollingInterval % totalPhase);
      return tickPhase == chunkPhase;
    });
  }

  private static class EventHandler extends WeakEventSubscriber<OverlayProvider> {

    private EventHandler(OverlayProvider overlayProvider) {
      super(overlayProvider);
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent event) {
      with(OverlayProvider::tick);
    }
  }
}
