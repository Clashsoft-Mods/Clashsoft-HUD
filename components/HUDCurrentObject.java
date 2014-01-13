package clashsoft.mods.cshud.components;

import static clashsoft.mods.cshud.CSHUDMod.*;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import clashsoft.mods.cshud.api.IToolTipHandler;
import clashsoft.mods.cshud.network.TileEntityData;

import net.minecraft.block.Block;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class HUDCurrentObject extends HUDComponent
{
	public static final HUDCurrentObject	instance	= new HUDCurrentObject();
	
	public World							world		= null;
	public MovingObjectPosition				object		= null;
	public TileEntity						tileEntity	= null;
	
	private List<IToolTipHandler>			handlers	= new ArrayList();
	
	public static void registerToolTipHandler(IToolTipHandler handler)
	{
		instance.handlers.add(handler);
	}
	
	@Override
	public void render(float partialTickTime)
	{
		this.renderCurrentObject(partialTickTime);
	}
	
	public void renderCurrentObject(float partialTickTime)
	{
		if (!showCurrentObject)
		{
			return;
		}
		
		this.world = this.mc.theWorld;
		MovingObjectPosition mop = this.rayTrace(partialTickTime);
		boolean requestTileEntityData = false;
		if (mop == null)
		{
			if (this.object != null)
			{
				this.object = null;
			}
			return;
		}
		else
		{
			if (this.object == null || mop.blockX != this.object.blockX || mop.blockY != this.object.blockY || mop.blockZ != this.object.blockZ || mop.entityHit != this.object.entityHit)
			{
				this.object = mop;
				requestTileEntityData = tooltipTileEntityData;
			}
		}
		
		Alignment align = currentObjAlignment;
		boolean isEntity = mop.typeOfHit == EnumMovingObjectType.ENTITY;
		List<String> lines = new ArrayList();
		int width = 0;
		int height = 0;
		int color = 0;
		int textX = 0;
		ItemStack stack = null;
		
		if (isEntity)
		{
			Entity entity = mop.entityHit;
			
			String name = entity.getEntityName();
			int entityWidth;
			int entityHeight;
			
			if (entity instanceof EntityHanging)
			{
				EntityHanging entityhanging = (EntityHanging) mop.entityHit;
				entityWidth = entityhanging.getWidthPixels() + 12;
				entityHeight = entityhanging.getHeightPixels();
			}
			else
			{
				AxisAlignedBB aabb = entity.getBoundingBox();
				if (aabb != null)
				{
					entityWidth = (int) (Math.max(aabb.maxX - aabb.minX, aabb.maxZ - aabb.minZ));
					entityHeight = (int) (Math.max(aabb.maxY - aabb.minY, entity.getEyeHeight()) * 16);
				}
				else
				{
					entityHeight = (int) (Math.max(entity.height, entity.getEyeHeight() + 0.5F) * 16);
					entityWidth = entityHeight + 16;
				}
			}
			
			lines.add(name);
			width = entityWidth;
			height = entityHeight + 8;
			textX = entityWidth - 4;
			color = this.getEntityColor(entity);
		}
		else
		{
			int blockID = world.getBlockId(mop.blockX, mop.blockY, mop.blockZ);
			int metadata = world.getBlockMetadata(mop.blockX, mop.blockY, mop.blockZ);
			Block block = Block.blocksList[blockID];
			
			stack = block.getPickBlock(mop, this.mc.theWorld, mop.blockX, mop.blockY, mop.blockZ);
			if (stack == null)
			{
				stack = new ItemStack(blockID, 1, metadata);
			}
			
			if (requestTileEntityData)
			{
				if (block.hasTileEntity(metadata))
				{
					this.requestTileEntityData();
				}
				else
				{
					this.setTileEntityData(null);
				}
			}
			
			String name = stack.getDisplayName();
			lines.add(name);
			width = 32;
			height = 24;
			textX = 24;
			
			color = currentObjBlockColor;
		}
		
		this.addInformation(lines, stack);
		
		// Calculate Positions and Dimensions
		
		FontRenderer font = this.mc.fontRenderer;
		int lineCount = lines.size();
		int textHeight = lineCount * font.FONT_HEIGHT;
		
		if (lineCount > 1)
		{
			textHeight += 2;
		}
		
		width = Math.max(width, width + getMaxWidth(lines, font));
		height = Math.max(height, textHeight + 16);
		
		int frameX = align.getX(width, this.width);
		int frameY = align.getY(height, this.height);
		
		int textY = (height - textHeight) / 2;
		
		// Do Actual Rendering
		
		this.drawHoveringFrame(frameX, frameY, width, height, color);
		if (isEntity)
		{
			int entityY = frameY + (mop.entityHit instanceof EntityHanging ? height / 2 : height - 4);
			
			this.renderEntity(mop.entityHit, frameX + (textX / 2), frameY + entityY, 16, partialTickTime);
		}
		else
		{
			int x2 = frameX + 4;
			int y2 = frameY + ((height - 16) / 2);
			
			this.drawItem(stack, x2, y2);
		}
		
		font.drawStringWithShadow(lines.get(0), frameX + textX, frameY + textY, currentObjUseColorForText ? color : 0xFFFFFF);
		textY += 2;
		
		for (int i = 1; i < lineCount; i++)
		{
			textY += font.FONT_HEIGHT;
			font.drawStringWithShadow(lines.get(i), frameX + textX, frameY + textY, currentObjUseColorForText ? color : 0xA4A4A4);
		}
	}
	
	public int getMaxWidth(List<String> lines, FontRenderer font)
	{
		int width = 0;
		
		for (String line : lines)
		{
			int w = font.getStringWidth(line);
			
			if (w > width)
			{
				width = w;
			}
		}
		
		return width;
	}
	
	public void addInformation(List<String> lines, ItemStack block)
	{
		for (IToolTipHandler handler : handlers)
		{
			handler.addInformation(lines, this, block);
		}
	}
	
	public int getEntityColor(Entity entity)
	{
		if (entity.isCreatureType(EnumCreatureType.monster, false))
		{
			return currentObjMonsterEntityColor;
		}
		else if (entity.isCreatureType(EnumCreatureType.waterCreature, false))
		{
			return currentObjWaterEntityColor;
		}
		else if (entity.isCreatureType(EnumCreatureType.creature, false))
		{
			return currentObjAnimalEntityColor;
		}
		else
		{
			return currentObjOtherEntityColor;
		}
	}
	
	public void renderEntity(Entity entity, int x, int y, float scale, float partialTickTime)
	{
		GL11.glPushMatrix();
		
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		GL11.glColor4f(1F, 1F, 1F, 1F);
		
		GL11.glTranslatef(x, y, 50.0F);
		GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
		GL11.glScalef(scale, scale, scale);
		
		RenderHelper.enableGUIStandardItemLighting();
		
		GL11.glTranslatef(0.0F, entity.yOffset, 0.0F);
		
		RenderManager.instance.playerViewY = 180.0F;
		RenderManager.instance.renderEntityWithPosYaw(entity, 0.0D, 0.0D, 0.0D, 0.0F, partialTickTime);
		
		RenderHelper.disableStandardItemLighting();
		
		GL11.glPopMatrix();
	}
	
	public MovingObjectPosition rayTrace(float partialTickTime)
	{
		if (currentObjLiquids)
		{
			double reach = currentObjCustomReach;
			if (reach <= 1D)
			{
				reach = this.mc.playerController.getBlockReachDistance();
			}
			return rayTrace(this.mc.renderViewEntity, reach, partialTickTime);
		}
		else
		{
			return this.mc.objectMouseOver;
		}
	}
	
	public MovingObjectPosition rayTrace(EntityLivingBase living, double reach, float partialTickTime)
	{
		Vec3 position = living.getPosition(partialTickTime);
        Vec3 look = living.getLook(partialTickTime);
        Vec3 vec = position.addVector(look.xCoord * reach, look.yCoord * reach, look.zCoord * reach);
        return this.world.clip(position, vec, true);
	}
	
	public void requestTileEntityData()
	{
		TileEntityData.getInstance().requestTEData(this.world, this.object.blockX, this.object.blockY, this.object.blockZ);
	}
	
	public void setTileEntityData(TileEntity tileEntity)
	{
		this.tileEntity = tileEntity;
		if (tileEntity != null)
		{
			tileEntity.setWorldObj(this.world);
		}
	}
}
