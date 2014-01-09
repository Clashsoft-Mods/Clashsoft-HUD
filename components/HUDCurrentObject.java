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
		MovingObjectPosition mop = Minecraft.getMinecraft().objectMouseOver;
		if (mop != null)
		{
			boolean isEntity = mop.typeOfHit == EnumMovingObjectType.ENTITY;
			
			String renderName = "";
			int width = 0;
			int height = 0;
			int color = 0;
			int x0 = 0;
			int y0 = 0;
			int x1 = 0;
			int y1 = 0;
			ItemStack stack = null;
			
			if (isEntity)
			{
				Entity entity = mop.entityHit;
				
				renderName = entity.getEntityName();
				int width1;
				
				if (entity instanceof EntityHanging)
				{
					EntityHanging entityhanging = (EntityHanging) mop.entityHit;
					width1 = entityhanging.getWidthPixels() + 12;
					height = entityhanging.getHeightPixels();
				}
				else
				{
					AxisAlignedBB aabb = entity.getBoundingBox();
					if (aabb != null)
					{
						height = (int) (Math.max(aabb.maxY - aabb.minY, entity.getEyeHeight()) * 16);
						width1 = (int) (Math.max(aabb.maxX - aabb.minX, aabb.maxZ - aabb.minZ));
					}
					else
					{
						height = (int) (Math.max(entity.height, entity.getEyeHeight() + 0.5F) * 16);
						width1 = height + 16;
					}
				}
				
				width = width1 + this.mc.fontRenderer.getStringWidth(renderName);
				height += 8;
				x1 = width1 - 4;
				y1 = 8;
				
				if (entity.isCreatureType(EnumCreatureType.monster, false))
				{
					color = currentObjMonsterEntityColor;
				}
				else if (entity.isCreatureType(EnumCreatureType.waterCreature, false))
				{
					color = currentObjWaterEntityColor;
				}
				else if (entity.isCreatureType(EnumCreatureType.creature, false))
				{
					color = currentObjAnimalEntityColor;
				}
				else
				{
					color = currentObjOtherEntityColor;
				}
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
				x1 = 24;
				y1 = 8;
				
				color = currentObjBlockColor;
			}
			
			x0 = (this.width - width) / 2;
			y1 = (height - y1) / 2;
			
			GL11.glColor4f(1F, 1F, 1F, 1F);
			
			this.drawHoveringFrameAtPos(x0, 0, width, height, color);
			
			if (isEntity)
			{
				int y2 = y0 + (mop.entityHit instanceof EntityHanging ? height / 2 : height - 4);
				
				this.renderEntity(mop.entityHit, x0 + (x1 / 2), y2, 16, partialTickTime);
			}
			else
			{
				
				int x2 = x0 + 4;
				int y2 = y0 + y1 - 4;
				
				RenderHelper.enableGUIStandardItemLighting();
				GL11.glEnable(GL12.GL_RESCALE_NORMAL);
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
				GL11.glEnable(GL11.GL_DEPTH_TEST);
				itemRenderer.renderItemAndEffectIntoGUI(this.mc.fontRenderer, this.mc.getTextureManager(), stack, x2, y2);
				RenderHelper.disableStandardItemLighting();
			}
			
			this.mc.fontRenderer.drawString(renderName, x0 + x1, y0 + y1, currentObjUseColorForText ? color : 0xFFFFFF);
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
