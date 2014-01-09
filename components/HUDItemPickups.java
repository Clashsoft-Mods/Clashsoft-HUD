package clashsoft.mods.cshud.components;

import static clashsoft.mods.cshud.CSHUDMod.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.event.EventPriority;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;

public class HUDItemPickups extends HUDComponent
{
	public static class ItemPickup
	{
		public ItemStack stack;
		public int time = 0;
		
		public ItemPickup(ItemStack stack)
		{
			this.stack = stack;
		}
	}
	
	public static final HUDItemPickups instance = new HUDItemPickups();
	
	public int								lastItemPickupTime	= 0;
	public List<ItemPickup>					itemPickups			= new ArrayList();
	
	@Override
	public void render(float partialTickTime)
	{
		renderPickups(partialTickTime);
	}
	
	@ForgeSubscribe(priority = EventPriority.HIGH)
	public void onItemPickup(EntityItemPickupEvent event)
	{
		if (!showItemPickups)
		{
			return;
		}
		
		ItemStack stack = event.item.getEntityItem();
		if (stack != null && stack.stackSize > 0)
		{
			stack = stack.copy();
			for (ItemPickup itemPickup : this.itemPickups)
			{
				if (itemPickup.stack.isItemEqual(stack))
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
		
		if (lastItemPickupTime < maxPickupTime + 20)
		{
			lastItemPickupTime++;
		}
		
		Iterator<ItemPickup> iterator = itemPickups.iterator();
		while (iterator.hasNext())
		{
			ItemPickup itemPickup = iterator.next();
			itemPickup.time++;
			
			if (itemPickup.time > maxPickupTime + 20)
			{
				iterator.remove();
			}
		}
	}
	
	public void renderPickups(float partialTickTime)
	{
		int l = (this.lastItemPickupTime < pickupBoxHeight ? pickupBoxHeight - this.lastItemPickupTime : 0);
		
		for (int i = 0, j = 0; i < this.itemPickups.size() && j < this.height; i++)
		{
			ItemPickup itemPickup = this.itemPickups.get(i);
			
			j += this.drawItemPickup(this.width, j - l, partialTickTime, itemPickup);
		}
	}
	
	public int drawItemPickup(int x, int y, float partialTickTime, ItemPickup itemPickup)
	{
		ItemStack stack = itemPickup.stack;
		
		String s = stack.stackSize == 1 ? stack.getDisplayName() : String.format("%s (%d)", stack.getDisplayName(), stack.stackSize);
		int width = Math.max(80, this.mc.fontRenderer.getStringWidth(s) + 10);
		
		if (itemPickup.time > maxPickupTime)
		{
			float f = width / 20F;
			float f1 = (itemPickup.time - maxPickupTime) + partialTickTime;
			x += f * f1;
		}
		
		this.drawHoveringFrame(x - width, y, width, pickupBoxHeight, pickupBoxColor);
		this.mc.fontRenderer.drawString(s, x - width + 5, y + 5, pickupTextColor);
		
		return pickupBoxHeight;
	}
}
