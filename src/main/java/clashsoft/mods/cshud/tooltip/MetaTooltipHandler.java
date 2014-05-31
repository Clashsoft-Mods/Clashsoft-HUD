package clashsoft.mods.cshud.tooltip;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.input.Keyboard;

import clashsoft.cslib.minecraft.lang.I18n;
import clashsoft.cslib.random.MaxRandom;
import clashsoft.cslib.random.MinRandom;
import clashsoft.cslib.reflect.CSReflection;
import clashsoft.mods.cshud.CSHUD;
import clashsoft.mods.cshud.api.ITooltipHandler;
import clashsoft.mods.cshud.components.HUDCurrentObject;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import cpw.mods.fml.common.registry.GameData;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;

public class MetaTooltipHandler implements ITooltipHandler
{
	protected static Map<String, String>	modOwners	= new HashMap();
	
	@Override
	public void addInformation(List<String> lines, HUDCurrentObject hud, ItemStack stack)
	{
		MovingObjectPosition object = hud.object;
		World world = hud.world;
		EntityPlayer player = hud.mc.thePlayer;
		
		if (object.typeOfHit == MovingObjectType.ENTITY)
		{
			if (Keyboard.isKeyDown(Keyboard.KEY_LMENU))
			{
				Entity entity = object.entityHit;
				String className = FMLDeobfuscatingRemapper.INSTANCE.map(entity.getClass().getSimpleName());
				
				lines.add(I18n.getString("tooltip.entity.position") + COLON + String.format("%.2f %.2f %.2f", entity.posX, entity.posY, entity.posZ));
				lines.add(I18n.getString("tooltip.entity.id") + COLON + EntityList.getEntityString(entity) + " (#" + EntityList.getEntityID(entity) + ")");
				lines.add(I18n.getString("tooltip.entity.type") + COLON + className);
			}
		}
		else if (stack != null)
		{
			int x = object.blockX;
			int y = object.blockY;
			int z = object.blockZ;
			
			Block block = world.getBlock(x, y, z);
			int metadata = world.getBlockMetadata(x, y, z);
			String name = Block.blockRegistry.getNameForObject(block);
			
			if (CSHUD.tooltipBreakProgress)
			{
				float breakProgress = CSReflection.getValue(Minecraft.getMinecraft().playerController, 6);
				
				if (breakProgress > 0F)
				{
					lines.add(String.format("%s: %.1f %%", I18n.getString("tooltip.breakprogress"), breakProgress * 100F));
				}
			}
			
			if (CSHUD.tooltipDrops)
			{
				Item item = block.getItemDropped(metadata, MaxRandom.instance, -1);
				if (item != null && item != stack.getItem())
				{
					if (block.canSilkHarvest(world, player, x, y, z, metadata) && EnchantmentHelper.getSilkTouchModifier(player))
					{
						lines.add(I18n.getString("tooltip.block.drops.silk_touch"));
					}
					else
					{
						int fortune = EnchantmentHelper.getFortuneModifier(hud.mc.thePlayer);
						ItemStack drop = new ItemStack(item, 1, block.damageDropped(metadata));
						int minDrops = block.quantityDropped(metadata, fortune, MinRandom.instance);
						int maxDrops = block.quantityDropped(metadata, fortune, MaxRandom.instance);
						
						if (minDrops > maxDrops)
						{
							int i = minDrops;
							minDrops = maxDrops;
							maxDrops = i;
						}
						
						StringBuilder builder = new StringBuilder(I18n.getString("tooltip.block.drops"));
						builder.append(COLON);
						
						if (minDrops != maxDrops)
							builder.append(minDrops).append("-").append(maxDrops).append(" x ");
						else if (minDrops != 1)
							builder.append(minDrops).append(" x ");
						
						builder.append(drop.getDisplayName());
						
						lines.add(builder.toString());
					}
				}
			}
			
			if (Keyboard.isKeyDown(Keyboard.KEY_LMENU))
			{
				String className = FMLDeobfuscatingRemapper.INSTANCE.map(block.getClass().getSimpleName());
				
				lines.add(I18n.getString("tooltip.block.position") + COLON + String.format("%d %d %d", x, y, z));
				lines.add(I18n.getString("tooltip.block.unlocalized_name") + COLON + stack.getUnlocalizedName());
				lines.add(I18n.getString("tooltip.block.id") + COLON + name + " (#" + Block.getIdFromBlock(block) + "/" + metadata + ")");
				lines.add(I18n.getString("tooltip.block.type") + COLON + className);
			}
			
			if (CSHUD.tooltipModName)
			{
				lines.add(getBlockOwner(name));
			}
		}
	}
	
	public static String getBlockOwner(String name)
	{
		String owner = modOwners.get(name);
		if (owner != null)
		{
			return owner;
		}
		@SuppressWarnings("deprecation")
		ModContainer mc = GameData.findModOwner(name);
		owner = mc == null ? "Minecraft" : mc.getName();
		modOwners.put(name, owner);
		return "\u00a79\u00a7o" + owner;
	}
}
