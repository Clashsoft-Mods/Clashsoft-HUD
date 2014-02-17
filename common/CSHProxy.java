package clashsoft.mods.cshud.common;

import clashsoft.mods.cshud.api.IHUDComponent;
import clashsoft.mods.cshud.api.ITooltipHandler;

import net.minecraft.tileentity.TileEntity;

public class CSHProxy
{
	public void init()
	{
	}
	
	public void registerHUDComponent(IHUDComponent component)
	{
	}
	
	public void registerToolTipHandler(ITooltipHandler handler)
	{
	}
	
	public boolean isClient()
	{
		return false;
	}
	
	public void setTileEntity(TileEntity tileEntity)
	{
		
	}
}
