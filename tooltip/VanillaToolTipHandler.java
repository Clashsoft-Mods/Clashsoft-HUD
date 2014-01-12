package clashsoft.mods.cshud.tooltip;

import java.util.List;

import clashsoft.cslib.reflect.CSReflection;
import clashsoft.mods.cshud.CSHUDMod;
import clashsoft.mods.cshud.api.IToolTipHandler;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityMinecartFurnace;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.*;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StringUtils;
import net.minecraft.world.World;

public class VanillaToolTipHandler implements IToolTipHandler
{
	public static String[]	NOTES		= new String[] { "F#", "G", "G#", "A", "B", "H", "C", "C#", "D", "D#", "E", "F" };
	public static String[]	NOTE_TYPES	= new String[] { "tooltip.music.harp", "tooltip.music.bassdrum", "tooltip.music.snare", "tooltip.music.hat", "tooltip.music.bassattack" };
	
	@Override
	public void addInformation(List<String> lines, World world, MovingObjectPosition object, ItemStack stack)
	{
		boolean isEntity = object.typeOfHit == EnumMovingObjectType.ENTITY;
		
		if (isEntity)
		{
			Entity entity = object.entityHit;
			
			if (entity instanceof EntityLivingBase)
			{
				if (((EntityLivingBase) entity).isChild())
				{
					lines.add(I18n.getString("tooltip.child"));
				}
			}
			
			if (entity instanceof EntityTNTPrimed)
			{
				int fuse = ((EntityTNTPrimed) entity).fuse * 60;
				lines.add(I18n.getString("tooltip.fuse") + ": " + StringUtils.ticksToElapsedTime(fuse));
			}
			else if (entity instanceof EntityMinecartFurnace)
			{
				// Reflection is actually faster than getting the value with the NBT
				int fuel = CSReflection.getValue((EntityMinecartFurnace) entity, 0);
				
				lines.add(I18n.getString("tooltip.fuel") + ": " + StringUtils.ticksToElapsedTime(fuel));
			}
			else if (entity instanceof EntityCreeper)
			{
				int fuse = (30 - ((Integer) CSReflection.getValue((EntityCreeper) entity, 1)).intValue()) * 60;
				lines.add(I18n.getString("tooltip.fuse") + ": " + StringUtils.ticksToElapsedTime(fuse));
			}
			else if (entity instanceof EntityZombie)
			{
				EntityZombie zombie = (EntityZombie) entity;
				
				if (zombie.isConverting())
				{
					int time = CSReflection.getValue(zombie, 3);
					lines.add(I18n.getString("tooltip.converting") + ": " + StringUtils.ticksToElapsedTime(time));
				}
			}
		}
		else
		{
			int x = object.blockX;
			int y = object.blockY;
			int z = object.blockZ;
			
			Block block = Block.blocksList[world.getBlockId(x, y, z)];
			int metadata = world.getBlockMetadata(x, y, z);
			
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
				lines.add(I18n.getString("tooltip.power") + ": " + (metadata == 0 ? I18n.getString("options.off") : metadata));
			}
			else if (block instanceof BlockRedstoneTorch)
			{
				boolean flag = block == Block.torchRedstoneActive;
				lines.add(I18n.getString("tooltip.state") + ": " + I18n.getString(flag ? "options.on" : "options.off"));
			}
			else if (block instanceof BlockRedstoneLight)
			{
				boolean flag = block == Block.redstoneLampActive;
				lines.add(I18n.getString("tooltip.state") + ": " + I18n.getString(flag ? "options.on" : "options.off"));
			}
			else if (block instanceof BlockLever || block instanceof BlockTripWireSource || block instanceof BlockButton || block instanceof BlockPistonBase || block instanceof BlockRailPowered)
			{
				boolean flag = (metadata & 8) != 0;
				lines.add(I18n.getString("tooltip.state") + ": " + I18n.getString(flag ? "options.on" : "options.off"));
			}
			else if (block instanceof BlockTripWire)
			{
				boolean flag = (metadata & 1) != 0;
				lines.add(I18n.getString("tooltip.state") + ": " + I18n.getString(flag ? "options.on" : "options.off"));
			}
			else if (block instanceof BlockRedstoneRepeater)
			{
				boolean active = block == Block.redstoneRepeaterActive;
				int delay = ((metadata >> 2) + 1);
				lines.add(I18n.getString("tooltip.state") + ": " + I18n.getString(active ? "options.on" : "options.off"));
				lines.add(I18n.getString("tooltip.delay") + ": " + delay);
			}
			else if (block instanceof BlockComparator)
			{
				boolean active = (metadata & 8) != 0;
				boolean mode = (metadata & 4) != 0;
				lines.add(I18n.getString("tooltip.state") + ": " + I18n.getString(active ? "options.on" : "options.off"));
				lines.add(I18n.getString("tooltip.mode") + ": " + (mode ? "-" : "&"));
			}
			else if (block instanceof BlockDaylightDetector)
			{
				lines.add(I18n.getString("tooltip.lightvalue") + ": " + metadata);
			}
			else if (block instanceof BlockBasePressurePlate)
			{
				boolean flag = metadata == 1;
				lines.add(I18n.getString("tooltip.state") + ": " + I18n.getString(flag ? "options.on" : "options.off"));
			}
			else if (block instanceof BlockDoor || block instanceof BlockTrapDoor || block instanceof BlockFenceGate)
			{
				if ((metadata & 8) != 0)
				{
					metadata = world.getBlockMetadata(x, y, z);
				}
				boolean flag = (metadata & 4) != 0;
				
				lines.add(I18n.getString("tooltip.open") + ": " + I18n.getString(flag ? "gui.yes" : "gui.no"));
			}
			
			if (CSHUDMod.currentObjTileEntityData)
			{
				TileEntity te = world.getBlockTileEntity(x, y, z);
				
				if (te instanceof IInventory)
				{
					addInventoryLines(lines, (IInventory) te);
				}
				
				if (te instanceof TileEntitySign)
				{
					addSignLines(lines, (TileEntitySign) te);
				}
				else if (te instanceof TileEntityNote)
				{
					addNoteLines(lines, (TileEntityNote) te);
				}
				else if (te instanceof TileEntityFurnace)
				{
					addFurnaceLines(lines, (TileEntityFurnace) te);
				}
				else if (te instanceof TileEntitySkull)
				{
					addSkullLines(lines, (TileEntitySkull) te);
				}
				else if (te instanceof TileEntityCommandBlock)
				{
					addCommandBlockLines(lines, (TileEntityCommandBlock) te);
				}
			}
		}
	}
	
	public void addSignLines(List<String> lines, TileEntitySign sign)
	{
		String[] text = sign.signText;
		for (int i = 0; i < text.length; i++)
		{
			String s = text[i];
			if (s != null && !s.isEmpty())
			{
				lines.add(s);
			}
		}
	}
	
	public void addNoteLines(List<String> lines, TileEntityNote te)
	{
		int note = ((TileEntityNote) te).note % 12;
		byte type = getNoteType(te.worldObj, te.xCoord, te.yCoord, te.zCoord);
		
		if (type == -1)
		{
			lines.add(I18n.getString("tooltip.deactivated"));
		}
		else
		{
			lines.add(I18n.getString("tooltip.music.note") + ": " + I18n.getString(NOTES[note]));
			lines.add(I18n.getString("tooltip.music.type") + ": " + I18n.getString(NOTE_TYPES[type]));
		}
	}
	
	public byte getNoteType(World world, int x, int y, int z)
	{
		if (world.getBlockMaterial(x, y + 1, z) == Material.air)
		{
			Material material = world.getBlockMaterial(x, y - 1, z);
			
			if (material == Material.rock)
			{
				return 1;
			}
			else if (material == Material.sand)
			{
				return 2;
			}
			else if (material == Material.glass)
			{
				return 3;
			}
			else if (material == Material.wood)
			{
				return 4;
			}
			return 0;
		}
		return -1;
	}
	
	public void addInventoryLines(List<String> lines, IInventory inventory)
	{
		int count = 0;
		
		for (int i = 0; i < inventory.getSizeInventory(); i++)
		{
			ItemStack is = inventory.getStackInSlot(i);
			if (is != null)
			{
				count += is.stackSize;
			}
		}
		
		if (count > 0)
		{
			lines.add(I18n.getString("tooltip.inventory.items") + ": " + count);
		}
	}
	
	public void addFurnaceLines(List<String> lines, TileEntityFurnace furnace)
	{
		int burn = furnace.furnaceBurnTime * 60;
		int cook = furnace.furnaceCookTime * 60;
		
		if (burn > 0)
		{
			lines.add(I18n.getString("tooltip.furnace.burntime") + ": " + StringUtils.ticksToElapsedTime(burn));
		}
		if (cook > 0)
		{
			lines.add(I18n.getString("tooltip.furnace.cooktime") + ": " + StringUtils.ticksToElapsedTime(cook));
		}
	}
	
	public void addSkullLines(List<String> lines, TileEntitySkull skull)
	{
		String username = skull.getExtraType();
		if (username != null && !username.isEmpty())
		{
			lines.add(I18n.getString("tooltip.head.owner") + ": " + username);
		}
	}
	
	public void addCommandBlockLines(List<String> lines, TileEntityCommandBlock command)
	{
		String c = command.getCommand();
		String s = command.getCommandSenderName();
		int i = command.getSignalStrength();
		
		if (c != null && !c.isEmpty())
		{
			lines.add(I18n.getString("tooltip.command") + ": " + c);
		}
		if (s != null && !s.isEmpty())
		{
			lines.add(I18n.getString("tooltip.command.sender") + ": " + s);
		}
		if (i > 0)
		{
			lines.add(I18n.getString("tooltip.command.successcount") + ": " + i);
		}
	}
}
