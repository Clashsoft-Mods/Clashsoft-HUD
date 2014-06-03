package clashsoft.mods.cshud.components;

import static clashsoft.mods.cshud.CSHUD.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import clashsoft.mods.cshud.CSHUD;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;

public class HUDItemPickups extends HUDComponent
{
	public static class ItemPickup
	{
		public ItemStack	stack;
		public int			time	= 0;
		
		public ItemPickup(ItemStack stack)
		{
			this.stack = stack;
		}
	}
	
	public static final HUDItemPickups	instance			= new HUDItemPickups();
	
	public int							lastItemPickupTime	= 0;
	public List<ItemPickup>				itemPickups			= new ArrayList();
	
	@Override
	public boolean enable()
	{
		return CSHUD.showItemPickups;
	}
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onItemPickup(EntityItemPickupEvent event)
	{
		if (!CSHUD.showItemPickups)
			return;
		
		ItemStack stack = event.item.getEntityItem();
		if (stack != null && stack.stackSize > 0)
		{
			stack = stack.copy();
			for (ItemPickup itemPickup : this.itemPickups)
			{
				if (itemPickup.stack.isItemEqual(stack) && Objects.equals(itemPickup.stack.stackTagCompound, stack.stackTagCompound))
				{
					itemPickup.time = 0;
					itemPickup.stack.stackSize += stack.stackSize;
					return;
				}
			}
			
			this.lastItemPickupTime = 0;
			this.itemPickups.add(new ItemPickup(stack));
		}
	}
	
	@Override
	public void update()
	{
		super.update();
		
		if (this.lastItemPickupTime < pickupDisplayTime + 20)
		{
			this.lastItemPickupTime++;
		}
		
		Iterator<ItemPickup> iterator = this.itemPickups.iterator();
		while (iterator.hasNext())
		{
			ItemPickup itemPickup = iterator.next();
			itemPickup.time++;
			
			if (itemPickup.time > pickupDisplayTime + 20)
			{
				iterator.remove();
			}
		}
	}

	@Override
	public void render(float partialTickTime)
	{
		Alignment align = pickupAlignment;
		int x = 0;
		int y = 0;
		int count = this.itemPickups.size();
		int y1 = pickupBoxHeight;
		y = align.getY(count * y1, this.height, CSHUD.armorStatusBoxOffsetY);
		
		for (int i = 0;; i++)
		{
			if (i < this.itemPickups.size())
			{
				ItemPickup itemPickup = this.itemPickups.get(i);
				
				x = align.getX(this.drawItemPickup(0, 0, 0F, itemPickup, true), this.width, CSHUD.pickupBoxOffsetX);
				this.drawItemPickup(x, y, partialTickTime, itemPickup, false);
				y += y1;
			}
			else
			{
				break;
			}
		}
	}
	
	public int drawItemPickup(int x, int y, float partialTickTime, ItemPickup itemPickup, boolean flag)
	{
		ItemStack stack = itemPickup.stack;
		
		String s = stack.stackSize == 1 ? stack.getDisplayName() : String.format("%s (%d)", stack.getDisplayName(), stack.stackSize);
		int textWidth = this.mc.fontRenderer.getStringWidth(s);
		int width = Math.max(80, textWidth + 10);
		
		if (flag)
		{
			return width;
		}
		
		int color = pickupTextColor;
		int alpha = hoveringFrameAlpha;
		
		if (itemPickup.time > pickupDisplayTime)
		{
			float f1 = itemPickup.time - pickupDisplayTime;
			
			float f = 1F - f1 / 20F;
			alpha = Math.min((int) (f * 255F), alpha);
			color |= alpha << 24;
		}
		
		this.drawHoveringFrame(x, y, width, pickupBoxHeight, pickupBoxColor, hoveringFrameBackgroundColor, alpha);
		this.mc.fontRenderer.drawStringWithShadow(s, x + (width - textWidth) / 2, y + 5, color);
		
		return width;
	}
}
