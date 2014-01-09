package clashsoft.mods.cshud.components;

import static clashsoft.mods.cshud.CSHUDMod.hoveringFrameAlpha;
import static clashsoft.mods.cshud.CSHUDMod.hoveringFrameBackgroundColor;
import static clashsoft.mods.cshud.CSHUDMod.hoveringFrameDefaultColor;

import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import clashsoft.mods.cshud.api.IHUDComponent;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;

public abstract class HUDComponent extends Gui implements IHUDComponent
{
	public static RenderItem	itemRenderer	= new RenderItem();
	
	public Minecraft			mc;
	public int					width;
	public int					height;
	
	public int					updateCounter	= 0;
	
	@Override
	public void setMinecraft(Minecraft mc)
	{
		this.mc = mc;
	}
	
	@Override
	public void update()
	{
		this.updateCounter++;
	}
	
	@Override
	public void render(int width, int height, float partialTickTime)
	{
		this.width = width;
		this.height = height;
		this.render(partialTickTime);
	}
	
	public abstract void render(float partialTickTime);
	
	public void drawHoveringText(List<String> list, int x, int y, FontRenderer fontrenderer)
	{
		if (!list.isEmpty())
		{
			GL11.glDisable(GL12.GL_RESCALE_NORMAL);
			RenderHelper.disableStandardItemLighting();
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			this.zLevel = 300.0F;
			
			int width = 0;
			for (String line : list)
			{
				int lineWidth = fontrenderer.getStringWidth(line);
				
				if (lineWidth > width)
				{
					width = lineWidth;
				}
			}
			
			int x1 = x;
			int y1 = y;
			int height = 8;
			
			if (list.size() > 1)
			{
				height = list.size() * 10;
			}
			
			if (x1 + width > this.width)
			{
				x1 -= 28 + width;
			}
			
			if (y1 + height + 6 > this.height)
			{
				y1 = this.height - height - 6;
			}
			
			this.drawHoveringFrame(x1, y1, width, height, hoveringFrameDefaultColor);
			
			for (int i = 0; i < list.size(); ++i)
			{
				String s1 = (String) list.get(i);
				fontrenderer.drawStringWithShadow(s1, x1, y1, -1);
				
				if (i == 0)
				{
					y1 += 2;
				}
				
				y1 += 10;
			}
			
			this.zLevel = 0.0F;
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			RenderHelper.enableStandardItemLighting();
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		}
	}
	
	public void drawHoveringFrameAtPos(int x, int y, int width, int height, int color)
	{
		this.drawHoveringFrame(x + 4, y + 4, width - 8, height - 8, color);
	}
	
	public void drawHoveringFrame(int x, int y, int width, int height, int color)
	{
		int alpha = hoveringFrameAlpha << 24;
		int bgRGB = hoveringFrameBackgroundColor & 0xFFFFFF;
		int bgRGBA = bgRGB | alpha;
		int colorRGBA = color | alpha;
		int colorGradient = (colorRGBA & 0xFEFEFE) >> 1 | colorRGBA & -0xFFFFFF;
		
		drawRect(x - 3, y - 4, x + width + 3, y - 3, bgRGBA);
		drawRect(x - 3, y + height + 3, x + width + 3, y + height + 4, bgRGBA);
		drawRect(x - 3, y - 3, x + width + 3, y + height + 3, bgRGBA);
		drawRect(x - 4, y - 3, x - 3, y + height + 3, bgRGBA);
		drawRect(x + width + 3, y - 3, x + width + 4, y + height + 3, bgRGBA);
		this.drawGradientRect(x - 3, y - 3 + 1, x - 3 + 1, y + height + 3 - 1, colorRGBA, colorGradient);
		this.drawGradientRect(x + width + 2, y - 3 + 1, x + width + 3, y + height + 3 - 1, colorRGBA, colorGradient);
		drawRect(x - 3, y - 3, x + width + 3, y - 3 + 1, colorRGBA);
		drawRect(x - 3, y + height + 2, x + width + 3, y + height + 3, colorGradient);
	}
}
