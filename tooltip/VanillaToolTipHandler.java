package clashsoft.mods.cshud.tooltip;

import java.util.List;
import java.util.Set;

import org.lwjgl.input.Keyboard;

import clashsoft.cslib.minecraft.lang.I18n;
import clashsoft.cslib.reflect.CSReflection;
import clashsoft.mods.cshud.CSHUD;
import clashsoft.mods.cshud.api.ITooltipHandler;
import clashsoft.mods.cshud.components.HUDCurrentObject;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityMinecartFurnace;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.*;
import net.minecraft.util.Facing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.StringUtils;
import net.minecraft.world.World;

public class VanillaTooltipHandler implements ITooltipHandler
{
	public static final String	COLON = ": \u00a7f";
	public static String[]	NOTES		= new String[] { "F#", "G", "G#", "A", "B", "H", "C", "C#", "D", "D#", "E", "F" };
	public static String[]	NOTE_TYPES	= new String[] { "tooltip.music.harp", "tooltip.music.bassdrum", "tooltip.music.snare", "tooltip.music.hat", "tooltip.music.bassattack" };
	
	@Override
	public void addInformation(List<String> lines, HUDCurrentObject hud, ItemStack stack)
	{
		World world = hud.world;
		MovingObjectPosition object = hud.object;
		
		if (object.typeOfHit == MovingObjectType.ENTITY)
		{
			Entity entity = object.entityHit;
			
			if (entity instanceof EntityLiving)
			{
				if (((EntityLiving) entity).hasCustomNameTag())
				{
					lines.set(0, "\u00a7o" + lines.get(0));
				}
			}
			
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
				lines.add(I18n.getString("tooltip.fuse") + COLON + StringUtils.ticksToElapsedTime(fuse));
			}
			else if (entity instanceof EntityMinecartFurnace)
			{
				// Reflection is actually faster than getting the value with the
				// NBT
				int fuel = CSReflection.getValue((EntityMinecartFurnace) entity, 0);
				
				lines.add(I18n.getString("tooltip.fuel") + COLON + StringUtils.ticksToElapsedTime(fuel));
			}
			else if (entity instanceof EntityCreeper)
			{
				int fuse = (30 - ((Integer) CSReflection.getValue((EntityCreeper) entity, 1)).intValue()) * 60;
				lines.add(I18n.getString("tooltip.fuse") + COLON + StringUtils.ticksToElapsedTime(fuse));
			}
			else if (entity instanceof EntitySheep)
			{
				EntitySheep sheep = (EntitySheep) entity;
				int color = sheep.getFleeceColor();
				int color1 = ItemDye.field_150922_c[color];
				lines.add(String.format("%s: \u00a7f#%x", I18n.getString("tooltip.color"), color1));
			}
		}
		else if (stack != null)
		{
			int x = object.blockX;
			int y = object.blockY;
			int z = object.blockZ;
			
			Block block = world.getBlock(x, y, z);
			int metadata = world.getBlockMetadata(x, y, z);
			
			if (block instanceof BlockReed)
			{
				lines.add(I18n.getString("tooltip.state") + COLON + metadata);
			}
			else if (block instanceof BlockSapling)
			{
				lines.add(I18n.getString("tooltip.state") + COLON + (metadata >> 3));
			}
			else if (block instanceof BlockRedstoneWire)
			{
				lines.add(I18n.getString("tooltip.power") + COLON + (metadata == 0 ? I18n.getString("options.off") : metadata));
			}
			else if (block instanceof BlockRedstoneTorch)
			{
				boolean on = block == Blocks.redstone_torch;
				lines.add(I18n.getString("tooltip.state") + COLON + I18n.getString(on ? "options.on" : "options.off"));
			}
			else if (block instanceof BlockRedstoneLight)
			{
				boolean on = block == Blocks.redstone_lamp;
				lines.add(I18n.getString("tooltip.state") + COLON + I18n.getString(on ? "options.on" : "options.off"));
			}
			else if (block instanceof BlockLever || block instanceof BlockTripWireHook || block instanceof BlockButton || block instanceof BlockRailPowered)
			{
				boolean on = (metadata & 8) != 0;
				lines.add(I18n.getString("tooltip.state") + COLON + I18n.getString(on ? "options.on" : "options.off"));
			}
			else if (block instanceof BlockPistonBase)
			{
				boolean on = (metadata & 8) != 0;
				lines.add(I18n.getString("tooltip.state") + COLON + I18n.getString(on ? "options.on" : "options.off"));
				lines.add(I18n.getString("tooltip.facing") + COLON + Facing.facings[metadata % 6]);
			}
			else if (block instanceof BlockPistonExtension)
			{
				lines.add(I18n.getString("tooltip.state") + COLON + I18n.getString("options.on"));
				lines.add(I18n.getString("tooltip.facing") + COLON + Facing.facings[metadata % 6]);
			}
			else if (block instanceof BlockDispenser)
			{
				lines.add(I18n.getString("tooltip.facing") + COLON + Facing.facings[metadata % 6]);
			}
			else if (block instanceof BlockTripWire)
			{
				boolean on = (metadata & 1) != 0;
				lines.add(I18n.getString("tooltip.state") + COLON + I18n.getString(on ? "options.on" : "options.off"));
			}
			else if (block instanceof BlockRedstoneRepeater)
			{
				boolean on = block == Blocks.powered_repeater;
				int delay = (metadata >> 2) + 1;
				lines.add(I18n.getString("tooltip.state") + COLON + I18n.getString(on ? "options.on" : "options.off"));
				lines.add(I18n.getString("tooltip.delay") + COLON + delay);
			}
			else if (block instanceof BlockRedstoneComparator)
			{
				boolean on = (metadata & 8) != 0;
				boolean mode = (metadata & 4) != 0;
				lines.add(I18n.getString("tooltip.state") + COLON + I18n.getString(on ? "options.on" : "options.off"));
				lines.add(I18n.getString("tooltip.mode") + COLON + (mode ? "-" : "&"));
			}
			else if (block instanceof BlockDaylightDetector)
			{
				lines.add(I18n.getString("tooltip.lightvalue") + COLON + metadata);
			}
			else if (block instanceof BlockBasePressurePlate)
			{
				boolean on = metadata == 1;
				lines.add(I18n.getString("tooltip.state") + COLON + I18n.getString(on ? "options.on" : "options.off"));
			}
			else if (block instanceof BlockDoor || block instanceof BlockTrapDoor || block instanceof BlockFenceGate)
			{
				if ((metadata & 8) != 0)
				{
					metadata = world.getBlockMetadata(x, y - 1, z);
				}
				boolean open = (metadata & 4) != 0;
				
				lines.add(I18n.getString("tooltip.state") + COLON + I18n.getString(open ? "tooltip.open" : "tooltip.closed"));
			}
			
			if (CSHUD.tooltipTileEntityData)
			{
				TileEntity te = hud.tileEntity;
				if (te != null)
				{
					if (CSHUD.tooltipAdvancedTileEntityData && Keyboard.isKeyDown(Keyboard.KEY_LMENU))
					{
						this.addAdvancedTileEntityData(lines, te);
					}
					else
					{
						if (te instanceof IInventory)
						{
							this.addInventoryLines(lines, (IInventory) te);
						}
						
						if (te instanceof TileEntitySign)
						{
							this.addSignLines(lines, (TileEntitySign) te);
						}
						else if (te instanceof TileEntityNote)
						{
							this.addNoteLines(lines, (TileEntityNote) te);
						}
						else if (te instanceof TileEntityFurnace)
						{
							this.addFurnaceLines(lines, (TileEntityFurnace) te);
						}
						else if (te instanceof TileEntitySkull)
						{
							this.addSkullLines(lines, (TileEntitySkull) te);
						}
						else if (te instanceof TileEntityCommandBlock)
						{
							this.addCommandBlockLines(lines, (TileEntityCommandBlock) te);
						}
						else if (te instanceof TileEntityMobSpawner)
						{
							this.addSpawnerLines(lines, (TileEntityMobSpawner) te);
						}
					}
				}
			}
		}
	}
	
	public void addAdvancedTileEntityData(List<String> lines, TileEntity te)
	{
		NBTTagCompound nbt = new NBTTagCompound();
		te.writeToNBT(nbt);
		
		this.addNBTLines(lines, "", nbt, "");
	}
	
	public void addNBTLines(List<String> lines, String prefix, NBTBase tag, String name)
	{
		if (tag instanceof NBTTagCompound)
		{
			NBTTagCompound compound = (NBTTagCompound) tag;
			Set<String> keys = compound.func_150296_c();
			lines.add(prefix + (name.isEmpty() ? "COMPOUND" : name));
			lines.add(prefix + "{");
			
			for (String key : keys)
			{
				this.addNBTLines(lines, prefix + " ", compound.getTag(key), key);
			}
			
			lines.add(prefix + "}");
		}
		else if (tag instanceof NBTTagList)
		{
			NBTTagList list = (NBTTagList) tag.copy();
			lines.add(prefix + (name.isEmpty() ? "LIST" : name));
			lines.add(prefix + "[");
			
			for (int i = 0; i < list.tagCount(); i++)
			{
				this.addNBTLines(lines, prefix + " ", list.removeTag(i), Integer.toString(i));
			}
			
			lines.add(prefix + "]");
		}
		else if (!"xyz".contains(name) && !"id".equals(name))
		{
			lines.add(prefix + name + ": " + tag.toString());
		}
	}
	
	public void addSignLines(List<String> lines, TileEntitySign sign)
	{
		String[] text = sign.signText;
		for (String element : text)
		{
			String s = element;
			if (s != null && !s.isEmpty())
			{
				lines.add(s);
			}
		}
	}
	
	public void addNoteLines(List<String> lines, TileEntityNote te)
	{
		int note = te.note % 12;
		byte type = this.getNoteType(te.getWorldObj(), te.xCoord, te.yCoord, te.zCoord);
		
		if (type == -1)
		{
			lines.add(I18n.getString("tooltip.deactivated"));
		}
		else
		{
			lines.add(I18n.getString("tooltip.music.note") + COLON + I18n.getString(NOTES[note]));
			lines.add(I18n.getString("tooltip.music.type") + COLON + I18n.getString(NOTE_TYPES[type]));
		}
	}
	
	public byte getNoteType(World world, int x, int y, int z)
	{
		if (world.getBlock(x, y + 1, z) == Blocks.air)
		{
			Material material = world.getBlock(x, y - 1, z).getMaterial();
			
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
		if (inventory.hasCustomInventoryName())
		{
			lines.set(0, "\u00a7o" + inventory.getInventoryName());
		}
		
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
			lines.add(I18n.getString("tooltip.inventory.items") + COLON + count);
		}
	}
	
	public void addFurnaceLines(List<String> lines, TileEntityFurnace furnace)
	{
		int burn = furnace.furnaceBurnTime;
		int cook = furnace.furnaceCookTime;
		
		if (burn > 0)
		{
			lines.add(I18n.getString("tooltip.furnace.burntime") + COLON + StringUtils.ticksToElapsedTime(burn));
		}
		if (cook > 0)
		{
			lines.add(I18n.getString("tooltip.furnace.cooktime") + COLON + StringUtils.ticksToElapsedTime(cook));
		}
	}
	
	public void addSkullLines(List<String> lines, TileEntitySkull skull)
	{
		String username = skull.func_145907_c();
		if (username != null && !username.isEmpty())
		{
			lines.add(I18n.getString("tooltip.head.owner") + COLON + username);
		}
	}
	
	public void addCommandBlockLines(List<String> lines, TileEntityCommandBlock commandBlock)
	{
		CommandBlockLogic logic = commandBlock.func_145993_a();
		
		String command = logic.func_145753_i();
		String commandSender = logic.getCommandSenderName();
		String lastOutput = logic.func_145749_h().getFormattedText();
		
		if (command != null && !command.isEmpty())
		{
			if (command.length() >= CSHUD.tooltipCommandThreshold)
			{
				int j = command.lastIndexOf(' ', CSHUD.tooltipCommandThreshold);
				if (j != -1)
				{
					command = command.substring(0, j) + " [...]";
				}
			}
			
			lines.add(I18n.getString("tooltip.command") + COLON + command);
		}
		if (commandSender != null && !commandSender.isEmpty())
		{
			lines.add(I18n.getString("tooltip.command.sender") + COLON + commandSender);
		}
		if (lastOutput != null && !lastOutput.isEmpty())
		{
			lines.add(I18n.getString("tooltip.command.lastoutput") + COLON + lastOutput);
		}
	}
	
	public void addSpawnerLines(List<String> lines, TileEntityMobSpawner spawner)
	{
		String s = spawner.func_145881_a().getEntityNameToSpawn();
		lines.add(I18n.getString("tooltip.spawner.entity") + COLON + I18n.getString("entity." + s + ".name"));
	}
}
