package clashsoft.mods.cshud.components;

import static clashsoft.mods.cshud.CSHUDMod.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.resources.I18n;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;

import clashsoft.cslib.util.CSString;

public class HUDPotionEffects extends HUDComponent
{
	public static final ResourceLocation	inventoryTexture	= new ResourceLocation("minecraft", "textures/gui/container/inventory.png");
	
	@Override
	public void render(float partialTickTime)
	{
		renderActivePotionEffects();
	}
	
	public void renderActivePotionEffects()
	{
		Collection<PotionEffect> activeEffects = this.mc.thePlayer.getActivePotionEffects();
		
		if (!activeEffects.isEmpty())
		{
			List<PotionEffect> potionEffects = new ArrayList(activeEffects);
			for (int i = 0, j = 0; i < potionEffects.size() && j < this.height; i++)
			{
				j += this.drawPotionEffect(0, j, potionEffects.get(i));
			}
		}
	}
	
	public int drawPotionEffect(int x, int y, PotionEffect potionEffect)
	{
		Potion potion = Potion.potionTypes[potionEffect.getPotionID()];
		
		int mode = potionEffectDisplayMode;
		boolean renderIcon = (mode & 1) != 0;
		boolean renderDuration = (mode & 2) != 0;
		boolean renderAmplifier = (mode & 4) != 0;
		int color = this.getPotionEffectColor(potion, potionEffect.getIsAmbient());
		int textColor = potionUseColorForText ? color : 0xFFFFFF;
		
		if (!renderIcon)
		{
			String s = getPotionEffectDisplayString(potionEffect, renderDuration, renderAmplifier);
			
			this.drawHoveringFrameAtPos(x, y, Math.max(80, this.mc.fontRenderer.getStringWidth(s) + 10), potionEffectBoxHeight, color);
			
			this.mc.fontRenderer.drawStringWithShadow(s, x + 5, y + 5, textColor);
			return potionEffectBoxHeight;
		}
		else
		{
			this.drawHoveringFrameAtPos(x, y, 28, 28, color);
			
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
