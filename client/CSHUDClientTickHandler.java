package clashsoft.mods.cshud.client;

import clashsoft.mods.cshud.client.gui.GuiCSHUDIngame;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;

public class CSHUDClientTickHandler
{
	public static final CSHUDClientTickHandler	instance	= new CSHUDClientTickHandler();
	
	@SubscribeEvent
	public void tickStart(ClientTickEvent event)
	{
		if (event.phase == Phase.START)
		{
			GuiCSHUDIngame.instance.updateTick();
		}
	}
}
