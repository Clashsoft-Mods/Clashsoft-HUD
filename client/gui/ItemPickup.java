package clashsoft.mods.cshud.client.gui;

import net.minecraft.item.ItemStack;

public class ItemPickup
{
	public ItemStack stack;
	public int time = 0;
	
	public ItemPickup(ItemStack stack)
	{
		this.stack = stack;
	}
}
