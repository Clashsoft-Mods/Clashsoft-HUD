package clashsoft.mods.cshud.client;

import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;

import clashsoft.mods.cshud.client.gui.GuiCSHUDIngame;
import clashsoft.mods.cshud.common.CSHUDCommonProxy;

public class CSHUDClientProxy extends CSHUDCommonProxy
{
	public static GuiCSHUDIngame theGUIOverlay;
	
	@Override
	public void init()
	{
		theGUIOverlay = new GuiCSHUDIngame(Minecraft.getMinecraft());
		MinecraftForge.EVENT_BUS.register(theGUIOverlay);
		
		TickRegistry.registerTickHandler(new CSHUDClientTickHandler(), Side.CLIENT);
	}
}
