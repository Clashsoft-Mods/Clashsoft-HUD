package clashsoft.mods.cshud.client;

import java.util.EnumSet;

import clashsoft.mods.cshud.client.gui.GuiCSHUDIngame;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class CSHUDClientTickHandler implements ITickHandler
{
	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData)
	{
		GuiCSHUDIngame.instance.updateTick();
	}
	
	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData)
	{
	}
	
	@Override
	public EnumSet<TickType> ticks()
	{
		return EnumSet.of(TickType.CLIENT);
	}
	
	@Override
	public String getLabel()
	{
		return "CSHUD Client Tick Handler";
	}
}
