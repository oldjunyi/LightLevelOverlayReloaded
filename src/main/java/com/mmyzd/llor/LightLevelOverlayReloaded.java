package com.mmyzd.llor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
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
	
	@Instance(MODID)
	public static LightLevelOverlayReloaded instance;
    
    public OverlayRenderer renderer;
    public OverlayPoller poller;
    public ConfigManager config;
    public boolean active;
    public KeyBinding hotkey;
    
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
			if (poller.isAlive()) return;
			try {
				poller.start();
			} catch (Exception e) {
				e.printStackTrace();
				poller = new OverlayPoller();
			}
		}
	}
    
	@SubscribeEvent
	public void onKeyInputEvent(KeyInputEvent evt) {
		if (hotkey.isPressed()) {
			if (active && (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))) {
				config.useSkyLight.set(!config.useSkyLight.getBoolean());
			} else if (active && (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))){
				config.overlayType.set((config.overlayType.getInt() + 1) % 3);
			} else {
				active = !active;
				launchPoller();
			}
		}
	}
	
	@SubscribeEvent
	public void onRenderWorldLastEvent(RenderWorldLastEvent evt) {
		if (active) {
			EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
			if (player == null) return;
			double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * evt.partialTicks;
	        double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * evt.partialTicks;
	        double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * evt.partialTicks;
	        renderer.render(x, y, z, poller.overlays);
		}
	}
	
}
