package clashsoft.mods.cshud.api;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public interface IToolTipHandler
{
	public void addInformation(List<String> lines, World world, MovingObjectPosition object, ItemStack block);
}
