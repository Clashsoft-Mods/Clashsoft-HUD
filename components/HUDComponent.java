package clashsoft.mods.cshud.components;

import static clashsoft.mods.cshud.CSHUD.hoveringFrameAlpha;
import static clashsoft.mods.cshud.CSHUD.hoveringFrameBackgroundColor;
import static clashsoft.mods.cshud.CSHUD.hoveringFrameDefaultColor;

import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import clashsoft.mods.cshud.api.IHUDComponent;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;

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
	
	public void drawItem(ItemStack stack, int x, int y)
	{
		if (stack != null)
		{
			RenderHelper.enableGUIStandardItemLighting();
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
			itemRenderer.renderItemAndEffectIntoGUI(this.mc.fontRenderer, this.mc.getTextureManager(), stack, x, y);
			RenderHelper.disableStandardItemLighting();
		}
	}
	
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
			
			this.drawHoveringFrame(x1 - 4, y1 - 4, width + 8, height + 8, hoveringFrameDefaultColor);
			
			for (int i = 0; i < list.size(); ++i)
			{
				String s1 = list.get(i);
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
	
	public void drawHoveringFrame(int x, int y, int width, int height)
	{
		this.drawHoveringFrame(x, y, width, height, hoveringFrameDefaultColor);
	}
	
	public void drawHoveringFrame(int x, int y, int width, int height, int color)
	{
		this.drawHoveringFrame(x, y, width, height, color, hoveringFrameBackgroundColor, hoveringFrameAlpha);
	}
	
	public void drawHoveringFrame(int x, int y, int width, int height, int color, int bg, int alpha)
	{
		if (width < 2 || height < 2)
		{
			return;
		}
		
		color &= 0xFFFFFF;
		bg &= 0xFFFFFF;
		alpha = (alpha & 0xFF) << 24;
		
		int bgAlpha = bg | alpha;
		int colorAlpha = color | alpha;
		int colorGradient = (color & 0xFEFEFE) >> 1 | alpha;
		
		// Render gray rects
		this.drawGradientRect(x + 1, y, x + width - 1, y + 1, bgAlpha, bgAlpha);
		this.drawGradientRect(x + 1, y + height - 1, x + width - 1, y + height, bgAlpha, bgAlpha);
		this.drawGradientRect(x + 2, y + 2, x + width - 2, y + height - 2, bgAlpha, bgAlpha);
		this.drawGradientRect(x, y + 1, x + 1, y + height - 1, bgAlpha, bgAlpha);
		this.drawGradientRect(x + width - 1, y + 1, x + width, y + height - 1, bgAlpha, bgAlpha);
		
		// Render colored rects
		this.drawGradientRect(x + 1, y + 2, x + 2, y + height - 2, colorAlpha, colorGradient);
		this.drawGradientRect(x + width - 2, y + 2, x + width - 1, y + height - 2, colorAlpha, colorGradient);
		this.drawGradientRect(x + 1, y + 1, x + width - 1, y + 2, colorAlpha, colorAlpha);
		this.drawGradientRect(x + 1, y + height - 2, x + width - 1, y + height - 1, colorGradient, colorGradient);
	}
}
