package com.mmyzd.llor.displaymode;

import net.minecraftforge.eventbus.api.Event;

public class DisplayModeUpdateEvent extends Event {

  private final DisplayModeManager displayModeManager;

  public DisplayModeUpdateEvent(DisplayModeManager displayModeManager) {
    this.displayModeManager = displayModeManager;
  }

  public DisplayModeManager getDisplayModeManager() {
    return displayModeManager;
  }
}
