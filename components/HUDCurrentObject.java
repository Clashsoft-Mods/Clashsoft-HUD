package clashsoft.mods.cshud.components;

import static clashsoft.mods.cshud.CSHUDMod.*;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MovingObjectPosition;

public class HUDCurrentObject extends HUDComponent
{
	@Override
	public void render(float partialTickTime)
	{
		renderCurrentObject(partialTickTime);
	}
	
	public void renderCurrentObject(float partialTickTime)
	{
		
		if (!showCurrentObject)
		{
			return;
		}
		
		MovingObjectPosition mop = Minecraft.getMinecraft().objectMouseOver;
		if (mop == null)
		{
			return;
		}
		
		boolean isEntity = mop.typeOfHit == EnumMovingObjectType.ENTITY;
		
		Alignment align = currentObjAlignment;
		String renderName = "";
		int width = 0;
		int height = 0;
		int color = 0;
		int frameX = 0;
		int frameY = 0;
		int textX = 0;
		int textY = 0;
		ItemStack stack = null;
		
		if (isEntity)
		{
			Entity entity = mop.entityHit;
			
			renderName = entity.getEntityName();
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
			
			width = entityWidth + this.mc.fontRenderer.getStringWidth(renderName);
			height = entityHeight + 8;
			textX = entityWidth - 4;
			textY = 8;
			color = getEntityColor(entity);
		}
		else
		{
			int blockID = this.mc.theWorld.getBlockId(mop.blockX, mop.blockY, mop.blockZ);
			int metadata = this.mc.theWorld.getBlockMetadata(mop.blockX, mop.blockY, mop.blockZ);
			
			stack = Block.blocksList[blockID].getPickBlock(mop, this.mc.theWorld, mop.blockX, mop.blockY, mop.blockZ);
			if (stack == null)
			{
				stack = new ItemStack(blockID, 1, metadata);
			}
			
			renderName = stack.getDisplayName();
			width = 32 + this.mc.fontRenderer.getStringWidth(renderName);
			height = 24;
			textX = 24;
			textY = 8;
			
			color = currentObjBlockColor;
		}
		
		frameX = align.getX(width, this.width);
		frameY = align.getY(height, this.height);
		
		textY = (height - textY) / 2;
		
		this.drawHoveringFrameAtPos(frameX, frameY, width, height, color);
		
		if (isEntity)
		{
			int entityY = frameY + (mop.entityHit instanceof EntityHanging ? height / 2 : height - 4);
			
			this.renderEntity(mop.entityHit, frameX + (textX / 2), frameY + entityY, 16, partialTickTime);
		}
		else
		{
			int x2 = frameX + 4;
			int y2 = frameY + textY - 4;
			
			RenderHelper.enableGUIStandardItemLighting();
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			itemRenderer.renderItemAndEffectIntoGUI(this.mc.fontRenderer, this.mc.getTextureManager(), stack, x2, y2);
			RenderHelper.disableStandardItemLighting();
		}
		
		this.mc.fontRenderer.drawString(renderName, frameX + textX, frameY + textY, currentObjUseColorForText ? color : 0xFFFFFF);
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
		
		GL11.glTranslatef((float) x, (float) y, 50.0F);
		GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
		GL11.glScalef(scale, scale, scale);
		
		RenderHelper.enableGUIStandardItemLighting();
		
		GL11.glTranslatef(0.0F, entity.yOffset, 0.0F);
		
		RenderManager.instance.playerViewY = 180.0F;
		RenderManager.instance.renderEntityWithPosYaw(entity, 0.0D, 0.0D, 0.0D, 0.0F, partialTickTime);
		
		RenderHelper.disableStandardItemLighting();
		
		GL11.glDisable(GL11.GL_COLOR_MATERIAL);
		
		GL11.glPopMatrix();
	}
	
}
