package com.mmyzd.llor;

import java.io.File;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public class ConfigManager {
	
	public Configuration file;
	
	public Property useSkyLight;
	public Property overlayType;
	public Property chunkRadius;
	public Property pollingInterval;
	
	public ConfigManager(File configDir) {
		file = new Configuration(new File(configDir, "LLOverlayReloaded.cfg"));
		reload();
	}
	
	@SubscribeEvent
	public void onConfigurationChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
		if (event.modID.equals(LightLevelOverlayReloaded.MODID)) update();
	}
	
	void reload() {
		file.load();
		useSkyLight = file.get("general", "useSkyLight", false, "If set to true, the sunlight/moonlight will be counted in light level. (default: false)");
		overlayType = file.get("general", "overlayType", 0, "0 - normal, 1 - only shows dangerous (red) area, 2 - only shows safe (green) area. (default: 0)");
		chunkRadius = file.get("general", "chunkRadius", 3, "The distance (in chunks) of rendering radius. (default: 3)");
		pollingInterval = file.get("general", "pollingInterval", 200, "The update interval (in milliseconds) of light level. Farther chunks update less frequently. (default: 200)");
		update();
	}
	
	public void update() {
		useSkyLight.set(useSkyLight.getBoolean(false));
		overlayType.set(Math.min(Math.max(overlayType.getInt(0), 0), 2));
		chunkRadius.set(Math.min(Math.max(chunkRadius.getInt(3), 1), 9));
		pollingInterval.set(Math.min(Math.max(pollingInterval.getInt(200), 10), 60000));
		file.save();
	}
	
}
