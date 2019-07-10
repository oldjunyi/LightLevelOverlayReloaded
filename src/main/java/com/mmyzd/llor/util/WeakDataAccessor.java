package com.mmyzd.llor.util;

import java.lang.ref.WeakReference;
import java.util.function.Consumer;

public class WeakDataAccessor<T> {

  private WeakReference<T> reference;

  public WeakDataAccessor(T instance) {
    this.reference = new WeakReference<>(instance);
  }

  public void clear() {
    reference = null;
  }

  public final void with(Consumer<T> consumer) {
    if (reference == null) {
      return;
    }

    T instance = reference.get();
    if (instance == null) {
      clear();
      return;
    }

    consumer.accept(instance);
  }
}
