package com.mmyzd.llor.util;

import net.minecraftforge.common.MinecraftForge;

public class EventBusWeakSubscriber<T> extends WeakDataAccessor<T> {

  public EventBusWeakSubscriber(T instance) {
    super(instance);
  }

  @Override
  public void clear() {
    super.clear();
    MinecraftForge.EVENT_BUS.unregister(this);
  }
}
