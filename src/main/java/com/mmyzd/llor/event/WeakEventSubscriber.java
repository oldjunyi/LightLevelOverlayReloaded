package com.mmyzd.llor.event;

import net.minecraftforge.common.MinecraftForge;

public class WeakEventSubscriber<T> extends WeakDataAccessor<T> {

  public WeakEventSubscriber(T instance) {
    super(instance);
  }

  @Override
  public void clear() {
    super.clear();
    MinecraftForge.EVENT_BUS.unregister(this);
  }
}
