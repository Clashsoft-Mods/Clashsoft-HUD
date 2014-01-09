package clashsoft.mods.cshud.client.gui;

import static clashsoft.mods.cshud.CSHUDMod.alwaysShow;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import clashsoft.mods.cshud.api.IHUDComponent;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.event.ForgeSubscribe;

public class GuiCSHUDIngame extends GuiIngameForge
{
	public static final GuiCSHUDIngame instance = new GuiCSHUDIngame(Minecraft.getMinecraft());
	
	public List<IHUDComponent>	components	= new ArrayList();
	
	public final Minecraft		mc;
	
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
			component.update();
		}
	}
	
	@ForgeSubscribe
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
					component.render(width, height, partialTicks);
				}
			}
			
			GL11.glColor4f(1F, 1F, 1F, 1F);
			this.mc.renderEngine.bindTexture(GuiContainer.icons);
		}
	}
}
