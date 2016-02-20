package com.mmyzd.llor;

import cpw.mods.fml.client.config.GuiConfig;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;

public class GuiModConfig extends GuiConfig {
	public GuiModConfig(GuiScreen parent) {
		super(
			parent,
			new ConfigElement(LightLevelOverlayReloaded.instance.config.file.getCategory("general")).getChildElements(),
			LightLevelOverlayReloaded.MODID,
			false,
			false,
			GuiConfig.getAbridgedConfigPath(LightLevelOverlayReloaded.instance.config.file.toString())
		);
	}
}