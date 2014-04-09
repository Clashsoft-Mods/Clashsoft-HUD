package clashsoft.mods.cshud.components;

import static clashsoft.mods.cshud.CSHUD.*;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import clashsoft.cslib.minecraft.lang.I18n;
import clashsoft.mods.cshud.CSHUD;
import clashsoft.mods.cshud.api.ITooltipHandler;

import net.minecraft.block.Block;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
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
	
	private List<ITooltipHandler>			handlers	= new ArrayList();
	
	public static void registerToolTipHandler(ITooltipHandler handler)
	{
		instance.handlers.add(handler);
	}
	
	@Override
	public void render(float partialTickTime)
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
		
		if (mop.typeOfHit == MovingObjectType.ENTITY)
		{
			renderEntity(CSHUD.currentObjAlignment, partialTickTime, mop);
		}
		else if (mop.typeOfHit == MovingObjectType.BLOCK)
		{
			renderBlock(CSHUD.currentObjAlignment, partialTickTime, mop, requestTileEntityData);
		}
	}
	
	public void renderEntity(Alignment align, float partialTickTime, MovingObjectPosition mop)
	{
		boolean isHanging = false;
		float health = -1F;
		float maxHealth = -1F;
		List<String> lines = new ArrayList();
		Entity entity = mop.entityHit;
		String name = entity.getCommandSenderName();
		int entityWidth;
		int entityHeight;
		
		// Compute dimensions for entity
		
		if (entity instanceof EntityHanging)
		{
			isHanging = true;
			EntityHanging entityhanging = (EntityHanging) mop.entityHit;
			entityWidth = entityhanging.getWidthPixels() + 12;
			entityHeight = entityhanging.getHeightPixels() + 8;
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
				entityWidth = entityHeight + 8;
			}
		}
		
		// Compute textual information
		
		lines.add(name);
		if (entity instanceof EntityLivingBase)
		{
			EntityLivingBase living = (EntityLivingBase) entity;
			health = living.getHealth() / 2F;
			maxHealth = living.getMaxHealth() / 2F;
			if (maxHealth <= 20F)
			{
				lines.add("[HEALTH]");
			}
			else
			{
				lines.add(String.format("%s: %.2f / %.2f", I18n.getString("tooltip.health"), health, maxHealth));
				health = -1F; // Do not render hearts
			}
		}
		
		this.addInformation(lines, null);
		
		// Calculate Positions and Dimensions
		
		FontRenderer font = this.mc.fontRenderer;
		int lineCount = lines.size();
		int textHeight = lineCount == 1 ? font.FONT_HEIGHT : (lineCount * font.FONT_HEIGHT + 2);
		int textWidth = this.getMaxWidth(mop, lines, font);
		if (health != -1F)
		{
			int w = (int) (maxHealth * 9F);
			if (w > textWidth)
			{
				textWidth = w;
			}
		}
		
		int color = this.getEntityColor(entity);
		int textColor = currentObjUseColorForText ? color : 0xA4A4A4;
		int width = entityWidth + textWidth + 4;
		int height = Math.max(entityHeight, textHeight) + 8;
		int frameX = align.getX(width, this.width);
		int frameY = align.getY(height, this.height);
		int textX = entityWidth;
		int textY = (height - textHeight) / 2 + 2;
		int x1 = frameX + textX;
		int y1 = frameY + textY;
		int entityX = frameX + (textX / 2);
		int entityY = frameY + (isHanging ? height / 2 : height - 4);
		
		// Do Actual Rendering
		
		this.drawHoveringFrame(frameX, frameY, width, height, color);
		
		this.renderEntity(mop.entityHit, entityX, entityY, 16, partialTickTime);
		
		font.drawStringWithShadow(lines.get(0), x1, y1, currentObjUseColorForText ? color : 0xFFFFFF);
		textY += 2;
		
		for (int i = 1; i < lineCount; i++)
		{
			y1 += font.FONT_HEIGHT;
			String line = lines.get(i);
			if (line != null)
			{
				if (health != -1F && "[HEALTH]".equals(line))
				{
					this.renderHealth(x1, y1, health, maxHealth);
				}
				else
				{
					font.drawStringWithShadow(line, x1, y1, textColor);
				}
			}
		}
	}
	
	public void renderBlock(Alignment align, float partialTickTime, MovingObjectPosition mop, boolean requestTileEntityData)
	{
		List<String> lines = new ArrayList();
		ItemStack stack = null;
		
		Block block = this.world.getBlock(mop.blockX, mop.blockY, mop.blockZ);
		int metadata = this.world.getBlockMetadata(mop.blockX, mop.blockY, mop.blockZ);
		
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
		
		this.addInformation(lines, stack);
		
		// Compute font
		
		FontRenderer font = this.mc.fontRenderer;
		int lineCount = lines.size();
		int textWidth = this.getMaxWidth(mop, lines, font);
		int textHeight = lineCount == 1 ? font.FONT_HEIGHT : (lineCount * font.FONT_HEIGHT + 2);
		
		// Compute dimensions
		
		int color = CSHUD.currentObjBlockColor;
		int textColor = currentObjUseColorForText ? color : 0xA4A4A4;
		int width = textWidth + 28;
		int height = textHeight + 16;
		int frameX = align.getX(width, this.width);
		int frameY = align.getY(height, this.height);
		int textX = frameX + 24;
		int textY = frameY + (height - textHeight) / 2;
		int stackX = frameX + 4;
		int stackY = frameY + (height / 2) - 8;
		
		// Do Actual Rendering
		
		this.drawHoveringFrame(frameX, frameY, width, height, color);
		this.drawItem(stack, stackX, stackY);
		font.drawStringWithShadow(lines.get(0), textX, textY, currentObjUseColorForText ? color : 0xFFFFFF);
		textY += 2;
		
		for (int i = 1; i < lineCount; i++)
		{
			textY += font.FONT_HEIGHT;
			String line = lines.get(i);
			if (line != null)
			{
				font.drawStringWithShadow(line, textX, textY, textColor);
			}
		}
	}
	
	public void renderHealth(int x, int y, float health, float maxHealth)
	{
		this.mc.renderEngine.bindTexture(Gui.icons);
		
		for (int i = 0; i < maxHealth; i++)
		{
			float f = health - i;
			int x1 = x + i * 9;
			
			this.drawTexturedModalRect(x1, y, 16, 0, 9, 9);
			if (f >= 0.5F)
			{
				int u = f < 1F ? 61 : 52;
				this.drawTexturedModalRect(x1, y, u, 0, 9, 9);
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
		
		return width;
	}
	
	public void addInformation(List<String> lines, ItemStack block)
	{
		for (ITooltipHandler handler : this.handlers)
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
		GL11.glRotatef(180F, 0F, 0F, 1F);
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
		CSHUD.instance.netHandler.requestTEData(this.world, this.object.blockX, this.object.blockY, this.object.blockZ);
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
