package clashsoft.mods.cshud.tooltip;

import java.util.List;

import clashsoft.mods.cshud.api.IToolTipHandler;

import net.minecraft.block.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StringUtils;
import net.minecraft.world.World;

public class VanillaToolTipHandler implements IToolTipHandler
{
	@Override
	public void addInformation(List<String> lines, World world, MovingObjectPosition object, ItemStack stack)
	{
		boolean isEntity = object.typeOfHit == EnumMovingObjectType.ENTITY;
		
		if (isEntity)
		{
			Entity entity = object.entityHit;
			
			if (entity instanceof EntityTNTPrimed)
			{
				int fuse = ((EntityTNTPrimed)entity).fuse * 60;
				lines.add(I18n.getString("tooltip.fuse") + ": " + StringUtils.ticksToElapsedTime(fuse));
			}
		}
		else
		{
			String on = "options.on";
			String off = "options.off";
			String yes = "gui.yes";
			String no = "gui.no";
			
			Block block = Block.blocksList[world.getBlockId(object.blockX, object.blockY, object.blockZ)];
			int metadata = world.getBlockMetadata(object.blockX, object.blockY, object.blockZ);
			
			if (block instanceof BlockReed)
			{
				lines.add(I18n.getString("tooltip.state") + ": " + metadata);
			}
			else if (block instanceof BlockSapling)
			{
				lines.add(I18n.getString("tooltip.state") + ": " + (metadata >> 3));
			}
			else if (block instanceof BlockRedstoneWire)
			{
				lines.add(I18n.getString("tooltip.power") + ": " + (metadata == 0 ? I18n.getString(off) : metadata));
			}
			else if (block instanceof BlockRedstoneTorch)
			{
				boolean flag = block == Block.torchRedstoneActive;
				lines.add(I18n.getString("tooltip.state") + ": " + I18n.getString(flag ? on : off));
			}
			else if (block instanceof BlockRedstoneLight)
			{
				boolean flag = block == Block.redstoneLampActive;
				lines.add(I18n.getString("tooltip.state") + ": " + I18n.getString(flag ? on : off));
			}
			else if (block instanceof BlockLever || block instanceof BlockTripWireSource || block instanceof BlockButton || block instanceof BlockPistonBase)
			{
				boolean flag = (metadata & 8) != 0;
				lines.add(I18n.getString("tooltip.state") + ": " + I18n.getString(flag ? on : off));
			}
			else if (block instanceof BlockRedstoneRepeater)
			{
				boolean active = block == Block.redstoneRepeaterActive;
				int delay = ((metadata >> 2) + 1);
				lines.add(I18n.getString("tooltip.state") + ": " + (active ? on : off));
				lines.add(I18n.getString("tooltip.delay") + ": " + delay);
			}
			else if (block instanceof BlockComparator)
			{
				boolean active = (metadata & 8) != 0;
				boolean mode = (metadata & 4) != 0;
				lines.add(I18n.getString("tooltip.state") + ": " + (active ? on : off));
				lines.add(I18n.getString("tooltip.mode") + ": " + (mode ? "-" : "&"));
			}
			else if (block instanceof BlockDaylightDetector)
			{
				lines.add(I18n.getString("tooltip.lightvalue") + ": " + metadata);
			}
			else if (block instanceof BlockBasePressurePlate)
			{
				boolean flag = metadata == 1;
				lines.add(I18n.getString("tooltip.state") + ": " + I18n.getString(flag ? on : off));
			}
			else if (block instanceof BlockDoor || block instanceof BlockTrapDoor || block instanceof BlockFenceGate)
			{
				if ((metadata & 8) != 0)
				{
					metadata = world.getBlockMetadata(object.blockX, object.blockY - 1, object.blockZ);
				}
				boolean flag = (metadata & 4) != 0;
				
				lines.add(I18n.getString("tooltip.open") + ": " + I18n.getString(flag ? yes : no));
			}
		}
	}
}
