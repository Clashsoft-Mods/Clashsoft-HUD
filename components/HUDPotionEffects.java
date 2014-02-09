package clashsoft.mods.cshud.components;

import static clashsoft.mods.cshud.CSHUDMod.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.lwjgl.opengl.GL11;

import clashsoft.cslib.minecraft.lang.I18n;
import clashsoft.cslib.util.CSString;

import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;

public class HUDPotionEffects extends HUDComponent
{
	public static final ResourceLocation	inventoryTexture	= new ResourceLocation("minecraft", "textures/gui/container/inventory.png");
	
	public static boolean					renderIcon			= (potionEffectDisplayMode & 1) != 0;
	public static boolean					renderDuration		= (potionEffectDisplayMode & 2) != 0;
	public static boolean					renderAmplifier		= (potionEffectDisplayMode & 4) != 0;
	
	@Override
	public void render(float partialTickTime)
	{
		this.renderActivePotionEffects();
	}
	
	public void renderActivePotionEffects()
	{
		if (!showPotionEffectDisplay)
		{
			return;
		}
		
		Collection<PotionEffect> activeEffects = this.mc.thePlayer.getActivePotionEffects();
		if (!activeEffects.isEmpty())
		{
			List<PotionEffect> potionEffects = new ArrayList(activeEffects);
			this.renderPotionEffects(potionEffects);
		}
	}
	
	public void renderPotionEffects(List<PotionEffect> potionEffects)
	{
		Alignment align = potionAlignment;
		
		int count = potionEffects.size();
		int x = 0;
		int y = 0;
		
		renderIcon = true;
		
		if (align.isHorizontallyCentered() && renderIcon)
		{
			x = align.getX(count * 28, this.width);
			y = align.getY(28, this.height);
			
			for (PotionEffect potionEffect : potionEffects)
			{
				x += this.drawPotionEffect(x, y, potionEffect, false);
			}
		}
		else
		{
			int y1 = renderIcon ? 28 : potionEffectBoxHeight;
			y = align.getY(count * y1, this.height);
			
			for (PotionEffect potionEffect : potionEffects)
			{
				x = align.getX(this.drawPotionEffect(0, 0, potionEffect, true), this.width);
				this.drawPotionEffect(x, y, potionEffect, false);
				y += y1;
			}
		}
	}
	
	public int drawPotionEffect(int x, int y, PotionEffect potionEffect, boolean flag)
	{
		Potion potion = Potion.potionTypes[potionEffect.getPotionID()];
		
		int color = this.getPotionEffectColor(potion, potionEffect.getIsAmbient());
		int textColor = potionUseColorForText ? color : 0xFFFFFF;
		
		if (!renderIcon)
		{
			String text = this.getPotionEffectDisplayString(potionEffect, renderDuration, renderAmplifier);
			int width = Math.max(80, this.mc.fontRenderer.getStringWidth(text) + 10);
			
			if (flag)
			{
				return width;
			}
			
			this.drawHoveringFrame(x, y, width, potionEffectBoxHeight, color);
			
			this.mc.fontRenderer.drawStringWithShadow(text, x + 5, y + 5, textColor);
			return width;
		}
		else
		{
			if (flag)
			{
				return 28;
			}
			
			this.drawHoveringFrame(x, y, 28, 28, color);
			
			if (potion.hasStatusIcon())
			{
				GL11.glColor4f(1F, 1F, 1F, 1F);
				this.mc.renderEngine.bindTexture(inventoryTexture);
				int l = potion.getStatusIconIndex();
				this.drawTexturedModalRect(x + 5, y + 5, 0 + l % 8 * 18, 198 + l / 8 * 18, 18, 18);
			}
			
			if (renderAmplifier && potionEffect.getAmplifier() > 0)
			{
				this.mc.fontRenderer.drawStringWithShadow(CSString.convertToRoman(potionEffect.getAmplifier() + 1), x + 3, y + 3, textColor);
			}
			if (renderDuration)
			{
				String duration = Potion.getDurationString(potionEffect);
				int width = this.mc.fontRenderer.getStringWidth(duration);
				
				this.mc.fontRenderer.drawStringWithShadow(duration, x + 26 - width, y + 18, 0xFFFFFF);
			}
			
			return 28;
		}
	}
	
	protected String getPotionEffectDisplayString(PotionEffect potionEffect, boolean duration, boolean amplifier)
	{
		String effectName = I18n.getString(potionEffect.getEffectName());
		if (amplifier && duration)
		{
			return String.format("%s %s (%s)", effectName, CSString.convertToRoman(potionEffect.getAmplifier() + 1), Potion.getDurationString(potionEffect));
		}
		else if (amplifier)
		{
			return String.format("%s %s", effectName, CSString.convertToRoman(potionEffect.getAmplifier() + 1));
		}
		else
		{
			return String.format("%s (%s)", effectName, Potion.getDurationString(potionEffect));
		}
	}
	
	protected int getPotionEffectColor(Potion potion, boolean isAmbient)
	{
		if (isAmbient)
		{
			return potionAmbientEffectColor;
		}
		else if (potion != null)
		{
			return potion.isBadEffect() ? potionBadEffectColor : potionGoodEffectColor;
		}
		else
		{
			return potionNoEffectColor;
		}
	}
}
