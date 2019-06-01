package com.mmyzd.llor.config;

import net.minecraftforge.eventbus.api.Event;

public class ConfigUpdateEvent extends Event {

  private final ConfigManager configManager;
  private final Config config;

  public ConfigUpdateEvent(ConfigManager configManager, Config config) {
    this.configManager = configManager;
    this.config = config;
  }

  public ConfigManager getConfigManager() {
    return configManager;
  }

  public Config getConfig() {
    return config;
  }
}
