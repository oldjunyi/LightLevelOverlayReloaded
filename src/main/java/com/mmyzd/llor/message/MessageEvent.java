package com.mmyzd.llor.message;

import net.minecraftforge.eventbus.api.Event;

public class MessageEvent extends Event {
  
  private final Message message;

  public MessageEvent(Message message) {
    this.message = message;
  }

  public Message getMessage() {
    return message;
  }
}
