package com.mmyzd.llor;

import java.io.File;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mod(modid = LightLevelOverlayReloaded.MODID, useMetadata = true, guiFactory = "com.mmyzd.llor.GuiFactory")
public class LightLevelOverlayReloaded {
	
	public static final String MODID = "llor";
	
	@Instance(MODID)
	public static LightLevelOverlayReloaded instance;
    
	@SideOnly(Side.CLIENT)
    public OverlayRenderer renderer;
	
	@SideOnly(Side.CLIENT)
    public OverlayPoller poller;
	
	@SideOnly(Side.CLIENT)
    public ConfigManager config;
	
	@SideOnly(Side.CLIENT)
    public boolean active;
	
	@SideOnly(Side.CLIENT)
    public KeyBinding hotkey;
    
    @EventHandler
    @SideOnly(Side.CLIENT)
    public void preInit(FMLPreInitializationEvent evt) {
    	config = new ConfigManager(evt.getModConfigurationDirectory());
    }
    
	@EventHandler
	@SideOnly(Side.CLIENT)
    public void initialize(FMLInitializationEvent evt) {
		MinecraftForge.EVENT_BUS.register(this);
		FMLCommonHandler.instance().bus().register(this);
		FMLCommonHandler.instance().bus().register(config);
		renderer = new OverlayRenderer();
		poller = new OverlayPoller();
		active = false;
		hotkey = new KeyBinding("key.llor.hotkey", Keyboard.KEY_F4, "key.categories.llor");
		ClientRegistry.registerKeyBinding(hotkey);
		launchPoller();
    }
	
	@SideOnly(Side.CLIENT)
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
	@SideOnly(Side.CLIENT)
	public void onKeyInputEvent(KeyInputEvent evt) {
		EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
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
	@SideOnly(Side.CLIENT)
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
