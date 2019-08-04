package com.mmyzd.llor.config;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.config.ModConfig;
import java.util.ArrayList;
import org.lwjgl.glfw.GLFW;
import com.mmyzd.llor.ForgeMod;
import com.mmyzd.llor.displaymode.DisplayMode;
import com.mmyzd.llor.displaymode.DisplayModeManager;
import com.mmyzd.llor.event.WeakEventSubscriber;
import com.mmyzd.llor.message.FloatingMessage;
import com.mmyzd.llor.message.MessagePresenter;

public class ConfigManager {

  private static final String TOGGLE_OVERLAY_ON_TRANSLATION_KEY = "llor.message.toggle_on";
  private static final String TOGGLE_OVERLAY_OFF_TRANSLATION_KEY = "llor.message.toggle_off";
  private static final String SWITCH_DISPLAY_MODE_TRANSLATION_KEY = "llor.message.switch_mode";
  private static final String MESSAGE_IDENTIFIER = "message_triggered_by_key_input";
  private static final int MESSAGE_DURATION_TICKS = 50;

  private final Config.Builder configBuilder = new Config.Builder();
  private Config config = configBuilder.build();

  private final KeyBinding hotkey;

  private ModConfig modConfig;
  private final ForgeConfigSpec configSpec;
  private final ForgeConfigSpec.IntValue renderingRadius;
  private final ForgeConfigSpec.IntValue pollingInterval;
  private final ForgeConfigSpec.BooleanValue announcingWhenToggleOverlay;
  private final ForgeConfigSpec.BooleanValue announcingWhenSwitchDisplayMode;
  private final ForgeConfigSpec.ConfigValue<String> displayModeName;

  private final MessagePresenter messagePresenter;
  private final DisplayModeManager displayModeManager;
  private final ArrayList<Runnable> updateHandlers = new ArrayList<>();

  public ConfigManager(MessagePresenter messagePresenter) {
    this.messagePresenter = messagePresenter;
    this.displayModeManager = new DisplayModeManager(messagePresenter);

    MinecraftForge.EVENT_BUS.register(new EventHandler(this, displayModeManager));

    hotkey = new KeyBinding("key.llor.hotkey", KeyConflictContext.IN_GAME,
        InputMappings.Type.KEYSYM.getOrMakeInput(GLFW.GLFW_KEY_F4), "key.categories.llor");
    ClientRegistry.registerKeyBinding(hotkey);

    ForgeConfigSpec.Builder configSpecBuilder = new ForgeConfigSpec.Builder();

    configSpecBuilder
        .comment(" The rendering radius. (default: " + config.getRenderingRadius() + ")");
    configSpecBuilder.push("renderingRadius");
    renderingRadius = configSpecBuilder.defineInRange("chunks", config.getRenderingRadius(), 0, 15);
    configSpecBuilder.pop();

    configSpecBuilder.comment(
        " The light level polling interval. Distant chunks will be updated less frequently. (default: "
            + config.getPollingInterval() + ")");
    configSpecBuilder.push("pollingInterval");
    pollingInterval =
        configSpecBuilder.defineInRange("milliseconds", config.getPollingInterval(), 10, 2000);
    configSpecBuilder.pop();

    configSpecBuilder.comment(" The current display mode. The default avaliable modes are:\n"
        + " - Standard mode. Displays green and red numbers representing safe and spawnable areas in night.\n"
        + " - Minimal mode. Only displays red numbers on spawnable blocks.\n"
        + " - Advanced mode. With extra orange numbers for blocks which are safe in day time but spawnable in night.\n"
        + " - X mode. Displays X on blocks instead of numbers.\n"
        + " Custom display mode can be added or overridden using resource pack.");
    configSpecBuilder.push("displayMode");
    displayModeName = configSpecBuilder.define("name", "standard");
    configSpecBuilder.pop();

    configSpecBuilder.comment(" User interaction announcement.");
    configSpecBuilder.push("announceWhen");
    announcingWhenToggleOverlay = configSpecBuilder.define("toggleOverlay", true);
    announcingWhenSwitchDisplayMode = configSpecBuilder.define("switchDisplayMode", true);
    configSpecBuilder.pop();

    configSpec = configSpecBuilder.build();
    ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, configSpec,
        ForgeMod.NAME + ".toml");
  }

  public Config getConfig() {
    return config;
  }

  public void onUpdate(Runnable updateHandler) {
    updateHandlers.add(updateHandler);
  }

  private void update() {
    for (Runnable updateHandler : updateHandlers) {
      updateHandler.run();
    }
  }

  private void handleKeyInput() {
    if (!hotkey.isPressed()) {
      return;
    }
    if (KeyModifier.getActiveModifier() == KeyModifier.NONE) {
      configBuilder.setOverlayEnabled(!config.isOverlayEnabled());
      config = configBuilder.build();
      update();
      if (announcingWhenToggleOverlay.get()) {
        messagePresenter
            .present(new FloatingMessage(
                I18n.format(config.isOverlayEnabled() ? TOGGLE_OVERLAY_ON_TRANSLATION_KEY
                    : TOGGLE_OVERLAY_OFF_TRANSLATION_KEY),
                MESSAGE_IDENTIFIER, MESSAGE_DURATION_TICKS));
      }
    } else if (KeyModifier.SHIFT.isActive(null) && config.isOverlayEnabled()) {
      DisplayMode displayMode =
          displayModeManager.getNextDisplayMode(config.getDisplayMode().getName());
      configBuilder.setDisplayMode(displayMode);
      config = configBuilder.build();
      modConfig.getConfigData().set(displayModeName.getPath(), displayMode.getName());
      modConfig.save();
      if (announcingWhenSwitchDisplayMode.get()) {
        messagePresenter.present(new FloatingMessage(
            I18n.format(SWITCH_DISPLAY_MODE_TRANSLATION_KEY, displayMode.getDisplayName()),
            MESSAGE_IDENTIFIER, MESSAGE_DURATION_TICKS));
      }
    }
  }

  public void loadConfigFromFile(ModConfig modConfig) {
    this.modConfig = modConfig;
    configBuilder.setRenderingRadius(renderingRadius.get());
    configBuilder.setPollingInterval(pollingInterval.get());
    configBuilder.setDisplayMode(displayModeManager.getDisplayMode(displayModeName.get()));
    config = configBuilder.build();
    update();
  }

  private void loadDisplayMode() {
    DisplayMode displayMode = displayModeManager.getDisplayMode(config.getDisplayMode().getName());
    configBuilder.setDisplayMode(displayMode);
    config = configBuilder.build();
    update();
  }

  private static class EventHandler extends WeakEventSubscriber<ConfigManager> {

    private EventHandler(ConfigManager configManager, DisplayModeManager displayModeManager) {
      super(configManager);
      displayModeManager.onUpdate(() -> with(ConfigManager::loadDisplayMode));
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent event) {
      with(ConfigManager::handleKeyInput);
    }
  }
}
