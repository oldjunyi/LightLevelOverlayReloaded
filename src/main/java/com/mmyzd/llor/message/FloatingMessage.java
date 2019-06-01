package com.mmyzd.llor.message;

public class FloatingMessage implements Message {
  
  private final String content;
  private final String identifier;
  private int remainingTicks;

  public FloatingMessage(String content, String identifier, int durationTicks) {
    this.content = content;
    this.identifier = identifier;
    remainingTicks = durationTicks;
  }

  public String getContent() {
    return content;
  }

  public String getIdentifier() {
    return identifier;
  }

  public boolean elapse() {
    if (remainingTicks == 0) {
      return false;
    } else {
      --remainingTicks;
      return true;
    }
  }
}
