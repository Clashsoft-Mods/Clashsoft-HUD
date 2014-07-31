package clashsoft.mods.cshud.client;

import clashsoft.mods.cshud.api.IHUDComponent;
import clashsoft.mods.cshud.api.ITooltipHandler;
import clashsoft.mods.cshud.client.gui.GuiIngameOverlay;
import clashsoft.mods.cshud.common.CSHUDProxy;
import clashsoft.mods.cshud.components.*;
import clashsoft.mods.cshud.tooltip.MetaTooltipHandler;
import clashsoft.mods.cshud.tooltip.VanillaTooltipHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;

import net.minecraftforge.common.MinecraftForge;

public class CSHUDClientProxy extends CSHUDProxy
{
	@Override
	public void init(FMLInitializationEvent event)
	{
		MinecraftForge.EVENT_BUS.register(HUDItemPickups.instance);
		MinecraftForge.EVENT_BUS.register(GuiIngameOverlay.instance);
		FMLCommonHandler.instance().bus().register(GuiIngameOverlay.instance);
		
		this.registerHUDComponent(HUDCurrentObject.instance);
		this.registerHUDComponent(new HUDPotionEffects());
		this.registerHUDComponent(new HUDWorldInfo());
		this.registerHUDComponent(HUDItemPickups.instance);
		this.registerHUDComponent(new HUDArmorStatus());
		
		this.registerToolTipHandler(new VanillaTooltipHandler());
		this.registerToolTipHandler(new MetaTooltipHandler());
	}
	
	@Override
	public void registerHUDComponent(IHUDComponent component)
	{
		GuiIngameOverlay.instance.registerHUDComponent(component);
	}
	
	@Override
	public void registerToolTipHandler(ITooltipHandler handler)
	{
		HUDCurrentObject.registerToolTipHandler(handler);
	}
	
	@Override
	public boolean isClient()
	{
		return true;
	}
}
