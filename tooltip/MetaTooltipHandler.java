package clashsoft.mods.cshud.tooltip;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.input.Keyboard;

import clashsoft.cslib.minecraft.lang.I18n;
import clashsoft.cslib.reflect.CSReflection;
import clashsoft.mods.cshud.CSHUD;
import clashsoft.mods.cshud.api.ITooltipHandler;
import clashsoft.mods.cshud.components.HUDCurrentObject;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import cpw.mods.fml.common.registry.GameData;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
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
			float f = CSReflection.getValue(Minecraft.getMinecraft().playerController, 6);
			
			int x = object.blockX;
			int y = object.blockY;
			int z = object.blockZ;
			
			if (f > 0F)
			{
				lines.add(String.format("%s: %.1f %%", I18n.getString("tooltip.breakprogress"), f * 100F));
			}
			
			Block block = world.getBlock(x, y, z);
			String name = Block.blockRegistry.getNameForObject(block);
			
			if (CSHUD.tooltipModName)
			{
				lines.add(getBlockOwner(name));
			}
			
			if (Keyboard.isKeyDown(Keyboard.KEY_LMENU))
			{
				int metadata = world.getBlockMetadata(x, y, z);
				String className = FMLDeobfuscatingRemapper.INSTANCE.map(block.getClass().getSimpleName());
				
				lines.add(I18n.getString("tooltip.block.position") + COLON + String.format("%d %d %d", x, y, z));
				lines.add(I18n.getString("tooltip.block.unlocalized_name") + COLON + stack.getUnlocalizedName());
				lines.add(I18n.getString("tooltip.block.id") + COLON + name + " (#" + Block.getIdFromBlock(block) + "/" + metadata + ")");
				lines.add(I18n.getString("tooltip.block.type") + COLON + className);
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
