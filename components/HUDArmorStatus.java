package clashsoft.mods.cshud.components;

import static clashsoft.mods.cshud.CSHUDMod.*;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import clashsoft.cslib.util.CSArrays;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class HUDArmorStatus extends HUDComponent
{
	@Override
	public void render(float partialTickTime)
	{
		renderArmorStatus();
	}
	
	public void renderArmorStatus()
	{
		if (!showArmorStatus)
		{
			return;
		}
		
		ItemStack[] armorStacks = getArmorStacks(this.mc.thePlayer);
		if (armorStacks.length == 0)
		{
			return;
		}
		
		Alignment align = armorStatusAlignment;
		int count = armorStacks.length;
		int width = 24;
		int height = count * 20 + 4;
		
		for (int i = 0; i < count; i++)
		{
			if (armorStacks[i].isItemDamaged())
			{
				width = 80;
			}
		}
		
		int x = align.getX(width, this.width);
		int y = align.getY(height, this.height);
		
		this.drawHoveringFrame(x, y, width, height, armorStatusBoxColor);
		
		x += 4;
		y += 4;
		
		for (int i = 0; i < count; i++)
		{
			ItemStack stack = armorStacks[i];
			this.drawItem(stack, x, y);
			
			if (stack.isItemDamaged())
			{
				int maxDamage = stack.getMaxDamage();
				int damage = maxDamage - stack.getItemDamageForDisplay();
				
				float f = (float) damage / (float) maxDamage;
				int f1 = (int) (f * 56F);
				int color = Color.HSBtoRGB(f / 3F, 1F, 1F);
				damage++;
				
				if (armorStatusUseColorForText)
				{
					int y1 = y + 6;
					int x1 = this.mc.fontRenderer.drawStringWithShadow("" + damage, x + 17, y1, color);
					this.mc.fontRenderer.drawStringWithShadow("/" + maxDamage, x1, y1,  0xA4A4A4);
				}
				else
				{
					this.mc.fontRenderer.drawStringWithShadow(damage + "/" + maxDamage, x + 17, y + 6, 0xA4A4A4);
				}
				this.drawHoveringFrame(x + 17, y, f1, 4, color);
			}
			
			y += 20;
		}
	}
	
	public ItemStack[] getArmorStacks(EntityPlayer player)
	{
		List<ItemStack> result = new ArrayList();
		ItemStack stack;
		
		for (int i = 0; i < 4; i++)
		{
			stack = player.getCurrentArmor(3 - i);
			if (stack != null)
			{
				result.add(stack);
			}
		}
		
		if (armorStatusRenderCurrentItem)
		{
			stack = player.getCurrentEquippedItem();
			if (stack != null)
			{
				result.add(stack);
			}
		}
		
		return CSArrays.fromList(ItemStack.class, result);
	}
}
