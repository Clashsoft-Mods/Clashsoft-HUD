package clashsoft.mods.cshud.client;

import clashsoft.mods.cshud.api.IHUDComponent;
import clashsoft.mods.cshud.api.IToolTipHandler;
import clashsoft.mods.cshud.client.gui.GuiCSHUDIngame;
import clashsoft.mods.cshud.common.CSHUDCommonProxy;
import clashsoft.mods.cshud.components.*;
import clashsoft.mods.cshud.tooltip.VanillaToolTipHandler;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;

public class CSHUDClientProxy extends CSHUDCommonProxy
{	
	@Override
	public void init()
	{
		MinecraftForge.EVENT_BUS.register(CSHUDClientTickHandler.instance);
		MinecraftForge.EVENT_BUS.register(GuiCSHUDIngame.instance);
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
		
		this.registerToolTipHandler(new VanillaToolTipHandler());
	}
	
	@Override
	public void registerHUDComponent(IHUDComponent component)
	{
		GuiCSHUDIngame.instance.registerHUDComponent(component);
	}
	
	@Override
	public void registerToolTipHandler(IToolTipHandler handler)
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
