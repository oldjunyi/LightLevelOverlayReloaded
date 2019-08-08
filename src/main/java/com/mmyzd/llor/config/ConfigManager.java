package com.mmyzd.llor.config;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.client.settings.IKeyConflictContext;
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

  private static final String DEFAULT_DISPLAY_MODE_NAME = "standard";
  private static final int DEFAULT_RENDERING_RADIUS = 3;
  private static final int DEFAULT_POLLING_INTERVAL = 3;

  private static final String TOGGLE_OFF_TRANSLATION_ID = "llor.message.toggle_off";
  private static final String SWITCH_MODE_TRANSLATION_ID = "llor.message.switch_mode";
  private static final String MESSAGE_IDENTIFIER = "message_triggered_by_key_input";
  private static final int MESSAGE_DURATION_TICKS = 50;

  private static final String KEY_CATEGORY = "key.categories.llor";
  private static final String TOGGLE_KEY_ID = "key.llor.toggle";
  private static final String SWITCH_MODE_KEY_ID = "key.llor.switch_mode";
  private static final InputMappings.Input DEFAULT_HOTKEY =
      InputMappings.Type.KEYSYM.getOrMakeInput(GLFW.GLFW_KEY_F4);

  private final Config.Builder configBuilder = new Config.Builder();
  private Config config = configBuilder.build();

  private final KeyBinding toggleKey;
  private final KeyBinding switchModeKey;
  private final IKeyConflictContext keyConflictContext = new IKeyConflictContext() {

    @Override
    public boolean isActive() {
      return KeyConflictContext.IN_GAME.isActive();
    }

    @Override
    public boolean conflicts(IKeyConflictContext other) {
      return this == other;
    }
  };

  private final ForgeConfigSpec configSpec;
  private final ForgeConfigSpec.IntValue renderingRadius;
  private final ForgeConfigSpec.IntValue pollingInterval;
  private final ForgeConfigSpec.BooleanValue announceWhenTogglingOverlay;
  private final ForgeConfigSpec.BooleanValue announceWhenChangingDisplayMode;
  private final ForgeConfigSpec.ConfigValue<String> displayModeName;

  private final MessagePresenter messagePresenter;
  private final DisplayModeManager displayModeManager;
  private final ArrayList<Runnable> updateHandlers = new ArrayList<>();

  public ConfigManager(MessagePresenter messagePresenter) {
    this.messagePresenter = messagePresenter;
    this.displayModeManager = new DisplayModeManager(messagePresenter);

    MinecraftForge.EVENT_BUS.register(new EventHandler(this, displayModeManager));

    toggleKey = new KeyBinding(TOGGLE_KEY_ID, keyConflictContext, DEFAULT_HOTKEY, KEY_CATEGORY);
    switchModeKey = new KeyBinding(SWITCH_MODE_KEY_ID, keyConflictContext, KeyModifier.SHIFT,
        DEFAULT_HOTKEY, KEY_CATEGORY);
    ClientRegistry.registerKeyBinding(toggleKey);
    ClientRegistry.registerKeyBinding(switchModeKey);

    ForgeConfigSpec.Builder configSpecBuilder = new ForgeConfigSpec.Builder();

    configSpecBuilder.comment(" The rendering radius.\n"
        + " It determines how far will chunks be search for and rendered with overlays.");
    configSpecBuilder.push("renderingRadius");
    configSpecBuilder.comment("Default: " + DEFAULT_RENDERING_RADIUS);
    renderingRadius = configSpecBuilder.defineInRange("chunks", DEFAULT_RENDERING_RADIUS, 0, 15);
    configSpecBuilder.pop();

    configSpecBuilder.comment(" The polling interval.\n"
        + " Light level overlays will be updated for every N client ticks.\n"
        + " Distant chunks will be updated less frequently.");
    configSpecBuilder.push("pollingInterval");
    configSpecBuilder.comment("Default: " + DEFAULT_POLLING_INTERVAL);
    pollingInterval =
        configSpecBuilder.defineInRange("clientTicksPerUpdate", DEFAULT_POLLING_INTERVAL, 1, 100);
    configSpecBuilder.pop();

    configSpecBuilder.comment(" The display mode. The default avaliable modes are:\n"
        + " - \"standard\". Displays green and red numbers representing safe and spawnable areas in night.\n"
        + " - \"minimal\". Only displays red numbers on spawnable blocks.\n"
        + " - \"advanced\". With extra yellow numbers for blocks which are safe in day time but spawnable in night.\n"
        + " - \"x\". Displays X on blocks instead of numbers.\n"
        + " Display modes can be changed or added using resource packs.");
    configSpecBuilder.push("displayMode");
    configSpecBuilder.comment("Default: \"" + DEFAULT_DISPLAY_MODE_NAME + "\"");
    displayModeName = configSpecBuilder.define("name", DEFAULT_DISPLAY_MODE_NAME);
    configSpecBuilder.pop();

    configSpecBuilder.comment(" The announcements.\n"
        + " If enabled, text messages will be displayed on the top-left corner of the screen.");
    configSpecBuilder.push("announceWhen");
    announceWhenTogglingOverlay = configSpecBuilder.define("toggleOverlay", true);
    announceWhenChangingDisplayMode = configSpecBuilder.define("switchDisplayMode", true);
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

  private void updateConfig() {
    config = configBuilder.build();
    for (Runnable updateHandler : updateHandlers) {
      updateHandler.run();
    }
  }

  private boolean isKeyPressed(KeyBinding binding, KeyInputEvent event) {
    if (event.getAction() == GLFW.GLFW_RELEASE || binding.isInvalid()
        || event.getKey() != binding.getKey().getKeyCode()) {
      return false;
    }
    switch (binding.getKeyModifier()) {
      case NONE:
        return true;
      case SHIFT:
        return (event.getModifiers() & GLFW.GLFW_MOD_SHIFT) != 0;
      case ALT:
        return (event.getModifiers() & GLFW.GLFW_MOD_ALT) != 0;
      case CONTROL:
        return (event.getModifiers() & GLFW.GLFW_MOD_CONTROL) != 0;
    }
    return false;
  }

  private void announceDisplayModeUpdate() {
    String content;
    if (config.isOverlayEnabled()) {
      String displayModeName = config.getDisplayMode().getDisplayName();
      content = I18n.format(SWITCH_MODE_TRANSLATION_ID, displayModeName);
    } else {
      content = I18n.format(TOGGLE_OFF_TRANSLATION_ID);
    }
    messagePresenter
        .present(new FloatingMessage(content, MESSAGE_IDENTIFIER, MESSAGE_DURATION_TICKS));
  }

  private void handleKeyInput(KeyInputEvent event) {
    boolean changingDisplayMode = isKeyPressed(switchModeKey, event);
    boolean togglingVisibility = isKeyPressed(toggleKey, event);
    if (changingDisplayMode && config.isOverlayEnabled()) {
      DisplayMode displayMode = displayModeManager.getNextDisplayMode(displayModeName.get());
      if (displayMode == DisplayMode.NULL) {
        displayModeName.set(DEFAULT_DISPLAY_MODE_NAME);
      } else {
        displayModeName.set(displayMode.getName());
      }
      configBuilder.setDisplayMode(displayMode);
      updateConfig();
      displayModeName.save();
      if (announceWhenChangingDisplayMode.get()) {
        announceDisplayModeUpdate();
      }
    } else if (togglingVisibility || (!config.isOverlayEnabled() && changingDisplayMode)) {
      configBuilder.setOverlayEnabled(!config.isOverlayEnabled());
      updateConfig();
      if (announceWhenTogglingOverlay.get()) {
        announceDisplayModeUpdate();
      }
    }
  }

  public void loadConfig() {
    configBuilder.setRenderingRadius(renderingRadius.get());
    configBuilder.setPollingInterval(pollingInterval.get());
    loadDisplayMode();
  }

  private void loadDisplayMode() {
    DisplayMode displayMode = displayModeManager.getDisplayMode(displayModeName.get());
    configBuilder.setDisplayMode(displayMode);
    updateConfig();
  }

  private static class EventHandler extends WeakEventSubscriber<ConfigManager> {

    private EventHandler(ConfigManager configManager, DisplayModeManager displayModeManager) {
      super(configManager);
      displayModeManager.onUpdate(() -> with(ConfigManager::loadDisplayMode));
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent event) {
      with(configManager -> configManager.handleKeyInput(event));
    }
  }
}
