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
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class HUDCurrentObject extends HUDComponent
{
	public static final HUDCurrentObject		instance	= new HUDCurrentObject();
	
	private static final List<ITooltipHandler>	handlers	= new ArrayList();
	
	public World								world;
	public MovingObjectPosition					object;
	public boolean								objectChanged;
	
	public TileEntity							tileEntity;
	
	private boolean								isHanging;
	private int									entityWidth;
	private int									entityHeight;
	private float								health;
	private float								maxHealth;
	
	public ItemStack							stack;
	public List<String>							lines		= new ArrayList();
	
	public static void registerToolTipHandler(ITooltipHandler handler)
	{
		handlers.add(handler);
	}
	
	@Override
	public boolean enable()
	{
		return CSHUD.showCurrentObject;
	}
	
	@Override
	public void update()
	{
		this.world = this.mc.theWorld;
		
		if (this.world == null)
			return;
		
		MovingObjectPosition mop = this.rayTrace(0F);
		boolean requestTileEntityData = false;
		
		if (mop == null)
		{
			if (this.object != null)
			{
				this.object = null;
				this.objectChanged = true;
			}
			else
			{
				this.objectChanged = false;
			}
			return;
		}
		else
		{
			if (tooltipTEDataTick)
				requestTileEntityData = tooltipTEData;
			
			if (this.object == null || mop.blockX != this.object.blockX || mop.blockY != this.object.blockY || mop.blockZ != this.object.blockZ || mop.entityHit != this.object.entityHit)
			{
				this.object = mop;
				this.objectChanged = true;
				requestTileEntityData = tooltipTEData;
			}
			else
			{
				this.objectChanged = false;
			}
		}
		
		this.lines.clear();
		
		if (mop.typeOfHit == MovingObjectType.BLOCK)
		{
			String name = null;
			
			Block block = this.world.getBlock(mop.blockX, mop.blockY, mop.blockZ);
			int metadata = this.world.getBlockMetadata(mop.blockX, mop.blockY, mop.blockZ);
			
			ItemStack stack = block.getPickBlock(mop, this.mc.theWorld, mop.blockX, mop.blockY, mop.blockZ);
			if (stack == null)
			{
				if (block == Blocks.lit_redstone_ore)
				{
					stack = new ItemStack(Blocks.redstone_ore, 1, metadata);
				}
				else if (block == Blocks.end_portal)
				{
					stack = new ItemStack(Items.ender_eye, 1, metadata);
					name = I18n.getString("tile.endPortal.name");
				}
				else
				{
					stack = new ItemStack(block, 1, metadata);
				}
			}
			
			if (name == null)
			{
				name = getStackName(stack);
			}
			this.lines.add(name);
			
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
			
			this.addInformation(this.lines, stack);
			
			this.stack = stack;
		}
		else if (mop.typeOfHit == MovingObjectType.ENTITY)
		{
			Entity entity = mop.entityHit;
			
			String name = entity.getCommandSenderName();
			int entityWidth;
			int entityHeight;
			float health = 0F;
			float maxHealth = 0F;
			
			// Compute dimensions for entity
			
			if (entity instanceof EntityHanging)
			{
				this.isHanging = true;
				EntityHanging entityhanging = (EntityHanging) mop.entityHit;
				entityWidth = entityhanging.getWidthPixels() + 12;
				entityHeight = entityhanging.getHeightPixels() + 8;
			}
			else
			{
				AxisAlignedBB aabb = entity.getBoundingBox();
				if (aabb != null)
				{
					entityWidth = (int) Math.max(aabb.maxX - aabb.minX, aabb.maxZ - aabb.minZ);
					entityHeight = (int) (Math.max(aabb.maxY - aabb.minY, entity.getEyeHeight()) * 16);
				}
				else
				{
					entityHeight = (int) (Math.max(entity.height, entity.getEyeHeight() + 0.5F) * 16);
					entityWidth = entityHeight + 8;
				}
			}
			
			// Compute textual information
			
			this.lines.add(name);
			if (entity instanceof EntityLivingBase)
			{
				EntityLivingBase living = (EntityLivingBase) entity;
				health = living.getHealth() / 2F;
				maxHealth = living.getMaxHealth() / 2F;
				
				if (maxHealth <= 10F)
				{
					this.lines.add("[HEALTH]");
				}
				else
				{
					this.lines.add(String.format("%s: %.2f / %.2f", I18n.getString("tooltip.health"), health, maxHealth));
					health = -1F;
				}
			}
			
			this.addInformation(this.lines, null);
			
			this.entityWidth = entityWidth;
			this.entityHeight = entityHeight;
			this.health = health;
			this.maxHealth = maxHealth;
		}
	}
	
	@Override
	public void render(float partialTickTime)
	{
		MovingObjectPosition mop = this.object;
		
		if (mop != null && !this.lines.isEmpty())
		{
			if (mop.typeOfHit == MovingObjectType.ENTITY)
			{
				this.renderEntity(CSHUD.currentObjAlignment, partialTickTime, mop);
			}
			else if (mop.typeOfHit == MovingObjectType.BLOCK)
			{
				this.renderBlock(CSHUD.currentObjAlignment, partialTickTime, mop);
			}
		}
	}
	
	public void renderEntity(Alignment align, float partialTickTime, MovingObjectPosition mop)
	{
		Entity entity = mop.entityHit;
		
		// Calculate Positions and Dimensions
		
		FontRenderer font = this.mc.fontRenderer;
		int lineCount = this.lines.size();
		int textHeight = lineCount == 1 ? font.FONT_HEIGHT : lineCount * font.FONT_HEIGHT + 2;
		int textWidth = this.getMaxWidth(mop, this.lines, font);
		if (this.health != -1F)
		{
			int w = (int) (this.maxHealth * 9F);
			if (w > textWidth)
			{
				textWidth = w;
			}
		}
		
		int color = this.getEntityColor(entity);
		int textColor = currentObjUseColorForText ? color : 0xA4A4A4;
		int width = this.entityWidth + textWidth + 4;
		int height = Math.max(this.entityHeight, textHeight) + 8;
		int frameX = align.getX(width, this.width, CSHUD.currentObjBoxOffsetX);
		int frameY = align.getY(height, this.height, CSHUD.currentObjBoxOffsetY);
		int textX = this.entityWidth;
		int textY = (height - textHeight) / 2 + 2;
		int x1 = frameX + textX;
		int y1 = frameY + textY;
		int entityX = frameX + textX / 2;
		int entityY = frameY + (this.isHanging ? height / 2 : height - 4);
		
		// Do Actual Rendering
		
		this.drawHoveringFrame(frameX, frameY, width, height, color);
		
		this.renderEntity(mop.entityHit, entityX, entityY, 16, partialTickTime);
		
		font.drawStringWithShadow(this.lines.get(0), x1, y1, currentObjUseColorForText ? color : 0xFFFFFF);
		textY += 2;
		
		for (int i = 1; i < lineCount; i++)
		{
			y1 += font.FONT_HEIGHT;
			String line = this.lines.get(i);
			if (line != null)
			{
				if ("[HEALTH]".equals(line))
				{
					this.renderHealth(x1, y1, this.health, this.maxHealth);
				}
				else
				{
					font.drawStringWithShadow(line, x1, y1, textColor);
				}
			}
		}
	}
	
	public void renderBlock(Alignment align, float partialTickTime, MovingObjectPosition mop)
	{
		// Compute font
		
		FontRenderer font = this.mc.fontRenderer;
		int lineCount = this.lines.size();
		int textWidth = this.getMaxWidth(mop, this.lines, font);
		int textHeight = lineCount == 1 ? font.FONT_HEIGHT : lineCount * font.FONT_HEIGHT + 2;
		
		// Compute dimensions
		
		int color = CSHUD.currentObjBlockColor;
		int textColor = currentObjUseColorForText ? color : 0xA4A4A4;
		int width = textWidth + 28;
		int height = textHeight + 16;
		int frameX = align.getX(width, this.width, CSHUD.currentObjBoxOffsetX);
		int frameY = align.getY(height, this.height, CSHUD.currentObjBoxOffsetY);
		int textX = frameX + 24;
		int textY = frameY + (height - textHeight) / 2;
		int stackX = frameX + 4;
		int stackY = frameY + height / 2 - 8;
		
		// Do Actual Rendering
		
		this.drawHoveringFrame(frameX, frameY, width, height, color);
		this.drawItem(this.stack, stackX, stackY);
		font.drawStringWithShadow(this.lines.get(0), textX, textY, currentObjUseColorForText ? color : 0xFFFFFF);
		textY += 2;
		
		for (int i = 1; i < lineCount; i++)
		{
			textY += font.FONT_HEIGHT;
			String line = this.lines.get(i);
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
		for (ITooltipHandler handler : handlers)
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
	
	public static String getStackName(ItemStack stack)
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
