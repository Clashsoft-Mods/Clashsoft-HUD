package clashsoft.mods.cshud.common;

import clashsoft.mods.cshud.api.IHUDComponent;
import clashsoft.mods.cshud.api.IToolTipHandler;

import net.minecraft.tileentity.TileEntity;

public class CSHUDCommonProxy
{
	public void init()
	{
	}
	
	public void registerHUDComponent(IHUDComponent component)
	{
	}
	
	public void registerToolTipHandler(IToolTipHandler handler)
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
