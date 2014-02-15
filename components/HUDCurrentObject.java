package clashsoft.mods.cshud.components;

import static clashsoft.mods.cshud.CSHUD.*;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import clashsoft.mods.cshud.CSHUD;
import clashsoft.mods.cshud.api.IToolTipHandler;
import clashsoft.mods.cshud.client.gui.GuiCSHUDIngame;

import net.minecraft.block.Block;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
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
		boolean isEntity = mop.typeOfHit == MovingObjectType.ENTITY;
		boolean isHanging = false;
		boolean isLiving = false;
		List<String> lines = new ArrayList();
		int width = 0;
		int height = 0;
		int color = 0;
		int textX = 0;
		ItemStack stack = null;
		
		if (isEntity)
		{
			Entity entity = mop.entityHit;
			
			String name = entity.getCommandSenderName();
			int entityWidth;
			int entityHeight;
			
			if (entity instanceof EntityHanging)
			{
				isHanging = true;
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
			
			width = entityWidth;
			height = entityHeight + 8;
			textX = entityWidth - 4;
			color = this.getEntityColor(entity);
			
			lines.add(name);
			if (entity instanceof EntityLivingBase)
			{
				isLiving = true;
				lines.add(null);
			}
		}
		else
		{
			Block block = this.world.getBlock(mop.blockX, mop.blockY, mop.blockZ);
			int metadata = this.world.getBlockMetadata(mop.blockX, mop.blockY, mop.blockZ);
			
			if (block == Blocks.air)
			{
				return;
			}
			
			stack = block.getPickBlock(mop, this.mc.theWorld, mop.blockX, mop.blockY, mop.blockZ);
			if (stack == null)
			{
				stack = new ItemStack(block, 1, metadata);
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
			
			String name = this.getStackName(stack);
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
		
		width = Math.max(width, width + this.getMaxWidth(mop, lines, font));
		height = Math.max(height, textHeight + 16);
		
		int frameX = align.getX(width, this.width);
		int frameY = align.getY(height, this.height);
		
		int textY = (height - textHeight) / 2;
		
		// Do Actual Rendering

		this.drawHoveringFrame(frameX, frameY, width, height, color);
		
		if (isEntity)
		{
			int entityY = frameY + (isHanging ? height / 2 : height - 4);
			this.renderEntity(mop.entityHit, frameX + (textX / 2), frameY + entityY, 16, partialTickTime);
			
			if (isLiving)
			{
				EntityLivingBase living = ((EntityLivingBase) mop.entityHit);
				float health = living.getHealth() / 2F;
				float maxHealth = living.getMaxHealth() / 2F;
				this.mc.renderEngine.bindTexture(GuiCSHUDIngame.icons);
				
				int x1 = frameX + textX;
				int y1 = frameY + textY + 8;
				
				for (int i = 0; i < maxHealth; i++)
				{
					float f = health - i;
					int x = x1 + i * 9;
					int y = y1;
					
					this.drawTexturedModalRect(x, y1, 16, 0, 9, 9);
					if (f >= 0.5F)
					{
						int u = f < 1F ? 61 : 52;
						this.drawTexturedModalRect(x, y1, u, 0, 9, 9);
					}
				}
			}
		}
		else
		{
			int x2 = frameX + 4;
			int y2 = frameY + ((height - 16) / 2);
			
			this.drawItem(stack, x2, y2);
		}
		
		int x1 = frameX + textX;
		int y1 = frameY + textY;
		
		font.drawStringWithShadow(lines.get(0), x1, y1, currentObjUseColorForText ? color : 0xFFFFFF);
		textY++;
		textY++;
		
		for (int i = 1; i < lineCount; i++)
		{
			y1 += font.FONT_HEIGHT;
			String line = lines.get(i);
			if (line != null)
			{
				font.drawStringWithShadow(line, x1, y1, currentObjUseColorForText ? color : 0xA4A4A4);
			}
		}
	}
	
	public int getMaxWidth(MovingObjectPosition mop, List<String> lines, FontRenderer font)
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
		
		if (mop.entityHit instanceof EntityLivingBase)
		{
			int w = (int) (((EntityLivingBase) mop.entityHit).getMaxHealth() * 4.5F);
			if (w > width)
			{
				width = w;
			}
		}
		
		return width;
	}
	
	public void addInformation(List<String> lines, ItemStack block)
	{
		for (IToolTipHandler handler : this.handlers)
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
		
		GL11.glPopMatrix();
		
		RenderHelper.disableStandardItemLighting();
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
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
			return this.rayTrace(this.mc.renderViewEntity, reach, partialTickTime);
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
		return this.world.rayTraceBlocks(position, vec, true);
	}
	
	public String getStackName(ItemStack stack)
	{
		if (stack != null)
		{
			try
			{
				return stack.getDisplayName();
			}
			catch (Exception ex)
			{
				return "ERROR";
			}
		}
		return "NULL";
	}
	
	public void requestTileEntityData()
	{
		CSHUD.netHandler.requestTEData(this.world, this.object.blockX, this.object.blockY, this.object.blockZ);
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
