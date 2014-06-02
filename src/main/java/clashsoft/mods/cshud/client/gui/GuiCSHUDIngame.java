package clashsoft.mods.cshud.client.gui;

import static clashsoft.mods.cshud.CSHUD.alwaysShow;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import clashsoft.mods.cshud.api.IHUDComponent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;

public class GuiCSHUDIngame extends GuiIngameForge
{
	public static final GuiCSHUDIngame	instance	= new GuiCSHUDIngame(Minecraft.getMinecraft());
	
	private final List<IHUDComponent>	components	= new ArrayList();
	
	private final Minecraft				mc;
	
	public GuiCSHUDIngame(Minecraft mc)
	{
		super(mc);
		this.mc = mc;
	}
	
	public void registerHUDComponent(IHUDComponent component)
	{
		component.setMinecraft(this.mc);
		this.components.add(component);
	}
	
	@Override
	public void updateTick()
	{
		for (IHUDComponent component : this.components)
		{
			if (component.enable())
				component.update();
		}
	}
	
	@SubscribeEvent
	public void onTick(ClientTickEvent event)
	{
		if (event.phase == Phase.START)
		{
			this.updateTick();
		}
	}
	
	@SubscribeEvent
	public void onRenderGameOverlay(RenderGameOverlayEvent.Post event)
	{
		if (event.type == ElementType.HOTBAR)
		{
			if (alwaysShow || this.mc.inGameHasFocus)
			{
				int width = event.resolution.getScaledWidth();
				int height = event.resolution.getScaledHeight();
				float partialTicks = event.partialTicks;
				
				for (IHUDComponent component : this.components)
				{
					if (component.enable())
						component.render(width, height, partialTicks);
				}
			}
			
			GL11.glColor4f(1F, 1F, 1F, 1F);
			this.mc.renderEngine.bindTexture(Gui.icons);
		}
	}
}
