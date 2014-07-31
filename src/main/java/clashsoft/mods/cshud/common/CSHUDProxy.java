package clashsoft.mods.cshud.common;

import clashsoft.cslib.minecraft.common.BaseProxy;
import clashsoft.mods.cshud.api.IHUDComponent;
import clashsoft.mods.cshud.api.ITooltipHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class CSHUDProxy extends BaseProxy
{
	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		return null;
	}
	
	@Override
	public void init(FMLInitializationEvent event)
	{
	}
	
	public void registerHUDComponent(IHUDComponent component)
	{
	}
	
	public void registerToolTipHandler(ITooltipHandler handler)
	{
	}
}
