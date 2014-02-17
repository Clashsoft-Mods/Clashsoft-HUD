package clashsoft.mods.cshud.client;

import cpw.mods.fml.common.FMLCommonHandler;
import clashsoft.mods.cshud.api.IHUDComponent;
import clashsoft.mods.cshud.api.ITooltipHandler;
import clashsoft.mods.cshud.client.gui.GuiCSHUDIngame;
import clashsoft.mods.cshud.common.CSHProxy;
import clashsoft.mods.cshud.components.*;
import clashsoft.mods.cshud.tooltip.MetaTooltipHandler;
import clashsoft.mods.cshud.tooltip.VanillaTooltipHandler;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;

public class CSHClientProxy extends CSHProxy
{
	@Override
	public void init()
	{
		MinecraftForge.EVENT_BUS.register(GuiCSHUDIngame.instance);
		FMLCommonHandler.instance().bus().register(GuiCSHUDIngame.instance);
		
		MinecraftForge.EVENT_BUS.register(HUDItemPickups.instance);
		
		this.registerHUDComponents();
	}
	
	public void registerHUDComponents()
	{
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
		GuiCSHUDIngame.instance.registerHUDComponent(component);
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
	
	@Override
	public void setTileEntity(TileEntity tileEntity)
	{
		HUDCurrentObject.instance.setTileEntityData(tileEntity);
	}
}
