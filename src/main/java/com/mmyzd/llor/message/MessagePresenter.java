package com.mmyzd.llor.message;

import java.util.ArrayList;
import com.mmyzd.llor.event.WeakEventSubscriber;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class MessagePresenter {

  public final ArrayList<FloatingMessage> floatingMessages = new ArrayList<FloatingMessage>();

  public MessagePresenter() {
    MinecraftForge.EVENT_BUS.register(new EventHandler(this));
  }

  public void present(Message message) {
    if (message instanceof FloatingMessage) {
      present((FloatingMessage) message);
    }
  }

  public void present(FloatingMessage message) {
    floatingMessages.removeIf(
        existingMessage -> message.getIdentifier().equals(existingMessage.getIdentifier()));
    floatingMessages.add(message);
  }

  private static class EventHandler extends WeakEventSubscriber<MessagePresenter> {

    private EventHandler(MessagePresenter messagePresenter) {
      super(messagePresenter);
    }

    @SubscribeEvent
    public void onRenderTextOverlay(RenderGameOverlayEvent.Text event) {
      with(messagePresenter -> messagePresenter.floatingMessages
          .forEach(message -> event.getLeft().add(message.getContent())));
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent event) {
      with(messagePresenter -> {
        if (Minecraft.getInstance().world != null) {
          messagePresenter.floatingMessages.removeIf(message -> !message.elapse());
        }
      });
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerLoggedOutEvent event) {
      with(messagePresenter -> messagePresenter.floatingMessages.clear());
    }
  }
}
