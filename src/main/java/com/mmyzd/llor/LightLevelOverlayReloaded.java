package com.mmyzd.llor;

import com.mmyzd.llor.config.ConfigManager;
import com.mmyzd.llor.message.MessagePresenter;
import com.mmyzd.llor.overlay.OverlayProvider;
import com.mmyzd.llor.overlay.OverlayRenderer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(ForgeMod.ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class LightLevelOverlayReloaded implements ForgeMod {

  private static MessagePresenter messagePresenter;
  private static ConfigManager configManager;
  private static OverlayProvider overlayProvider;
  private static OverlayRenderer overlayRenderer;

  public LightLevelOverlayReloaded() {
    messagePresenter = new MessagePresenter();
    configManager = new ConfigManager(messagePresenter);
    overlayProvider = new OverlayProvider(configManager);
    overlayRenderer = new OverlayRenderer(configManager, overlayProvider);
  }

  @SubscribeEvent
  public static void onModConfigLoading(ModConfig.Loading event) {
    configManager.loadConfigFromFile(event.getConfig());
  }
}
