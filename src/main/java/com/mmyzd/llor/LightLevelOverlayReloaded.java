package com.mmyzd.llor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;

import org.lwjgl.input.Keyboard;

@Mod(modid = LightLevelOverlayReloaded.MODID, useMetadata = true, clientSideOnly = true, guiFactory = "com.mmyzd.llor.GuiFactory")
public class LightLevelOverlayReloaded {

	public static final String MODID = "llor";
	private static final int TICKS_FOR_MESSAGE = 40;

	@Instance(MODID)
	public static LightLevelOverlayReloaded instance;

	public OverlayRenderer renderer;
	public OverlayPoller poller;
	public ConfigManager config;
	public boolean active;
	public KeyBinding hotkey;
	public String message;
	public double messageRemainingTicks;

	@EventHandler
	public void preInit(FMLPreInitializationEvent evt) {
		config = new ConfigManager(evt.getModConfigurationDirectory());
	}

	@EventHandler
	public void initialize(FMLInitializationEvent evt) {
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(config);
		renderer = new OverlayRenderer();
		poller = new OverlayPoller();
		active = false;
		hotkey = new KeyBinding("key.llor.hotkey", Keyboard.KEY_F4, "key.categories.llor");
		ClientRegistry.registerKeyBinding(hotkey);
		launchPoller();
	}

	private void launchPoller() {
		for (int i = 0; i < 3; i++) {
			if (poller.isAlive())
				return;
			try {
				poller.start();
			} catch (Exception e) {
				e.printStackTrace();
				poller = new OverlayPoller();
			}
		}
	}

	@SubscribeEvent
	public void onKeyInputEvent(KeyInputEvent event) {
		boolean withShift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
		boolean withCtrl = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
		boolean withAlt = Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU);
		if (hotkey.isPressed()) {
			if (active && withShift && !withCtrl) {
				boolean useSkyLight = !config.useSkyLight.getBoolean();
				config.useSkyLight.set(useSkyLight);
				message = "Light Level Overlay: " + (useSkyLight ? "Block Light + Sky Light" : "Block Light Only");
				messageRemainingTicks = TICKS_FOR_MESSAGE;
			} else if (active && withCtrl && !withShift) {
				int mode = (config.displayMode.getInt() + 1) % 3;
				config.displayMode.set(mode);
				message = "Light Level Overlay: " + config.displayModeName.get(mode) + " Mode";
				messageRemainingTicks = TICKS_FOR_MESSAGE;
			} else if (active && withCtrl && withShift) {
				if(config.focusCoordinate.getString() == null || config.focusCoordinate.getString().isEmpty()) {
					Minecraft mc = Minecraft.getMinecraft();
					int playerPosY = (int)Math.floor(mc.player.posY);
					int playerPosX = (int)Math.floor(mc.player.posX);
					int playerPosZ = (int)Math.floor(mc.player.posZ); 
					String focusCoordinate = playerPosX + "," + playerPosY + "," + playerPosZ;
					config.focusCoordinate.set(focusCoordinate);
					config.showClosest.set(true);
					message = "Light Level Overlay: Focus on: " + focusCoordinate;
					messageRemainingTicks = TICKS_FOR_MESSAGE;
				} else {
					config.focusCoordinate.set("");
					config.showClosest.set(false);
					message = "Light Level Overlay: Focus on: player";
					messageRemainingTicks = TICKS_FOR_MESSAGE;
				}
			} else if (!withShift && !withCtrl && !withAlt) {
				active = !active;
				launchPoller();
			}
		}
	}

	@SubscribeEvent
	public void onRenderWorldLastEvent(RenderWorldLastEvent event) {
		if (active) {
			EntityPlayerSP player = Minecraft.getMinecraft().player;
			if (player == null)
				return;
			double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.getPartialTicks();
			double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.getPartialTicks();
			double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.getPartialTicks();
			renderer.render(x, y, z, poller.overlays);
		}
	}

	@SubscribeEvent
	public void onRenderGameOverlayEventText(RenderGameOverlayEvent.Text event) {
		if (messageRemainingTicks > 0) {
			messageRemainingTicks -= event.getPartialTicks();
			event.getLeft().add(message);
		}
		
		if(LightLevelOverlayReloaded.instance.config.showClosest.getBoolean()) {
			StringBuilder messageB = new StringBuilder("");
			for(int ii = 0; ii < poller.counts.length; ii++) {
				messageB.append(ii).append(": ").append(poller.counts[ii]).append(" -- ");
			}
			event.getRight().add(messageB.toString());
			if(poller.closestPosX > Integer.MIN_VALUE) {
				messageB = new StringBuilder();
				messageB.append("x: ").append(poller.closestPosX).append(" y: ").append(poller.closestPosY).append(" z: ").append(poller.closestPosZ);
				event.getRight().add(messageB.toString());
				messageB = new StringBuilder();
				Minecraft mc = Minecraft.getMinecraft();
				messageB.append("x: ").append((int)mc.player.posX - poller.closestPosX)
								.append(" y: ").append((int)mc.player.posY - poller.closestPosY)
								.append(" z: ").append((int)mc.player.posZ - poller.closestPosZ);
				event.getRight().add(messageB.toString());
				event.getRight().add("minY : " + poller.minimumY);
			}
		}
	}
}
