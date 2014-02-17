package clashsoft.mods.cshud.tooltip;

import java.util.List;

import org.lwjgl.input.Keyboard;

import clashsoft.cslib.minecraft.lang.I18n;
import clashsoft.mods.cshud.api.ITooltipHandler;
import clashsoft.mods.cshud.components.HUDCurrentObject;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;

public class MetaTooltipHandler implements ITooltipHandler
{
	@Override
	public void addInformation(List<String> lines, HUDCurrentObject hud, ItemStack stack)
	{
		if (Keyboard.isKeyDown(Keyboard.KEY_LMENU))
		{
			MovingObjectPosition object = hud.object;
			World world = hud.world;
			
			if (object.typeOfHit == MovingObjectType.ENTITY)
			{
				Entity entity = object.entityHit;
				
				if (Keyboard.isKeyDown(Keyboard.KEY_LMENU))
				{
					lines.add(I18n.getString("tooltip.entity.type") + ": " + EntityList.getEntityString(entity));
					lines.add(I18n.getString("tooltip.entity.id") + ": " + EntityList.getEntityID(entity));
				}
			}
			else
			{
				int x = object.blockX;
				int y = object.blockY;
				int z = object.blockZ;
				
				Block block = world.getBlock(x, y, z);
				int metadata = world.getBlockMetadata(x, y, z);
				TileEntity te = hud.tileEntity;
				
				lines.add(I18n.getString("tooltip.block.unlocalized_name") + ": " + stack.getUnlocalizedName());
				lines.add(I18n.getString("tooltip.block.type") + ": " + Block.blockRegistry.getNameForObject(block));
				lines.add(I18n.getString("tooltip.block.id") + ": " + Block.getIdFromBlock(block));
				lines.add(I18n.getString("tooltip.metadata") + ": " + metadata);
			}
		}
	}
}
