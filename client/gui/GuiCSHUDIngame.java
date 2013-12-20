package clashsoft.mods.cshud.client.gui;

import static clashsoft.mods.cshud.CSHUDMod.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import clashsoft.cslib.util.CSString;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;

public class GuiCSHUDIngame extends GuiIngameForge
{
	public final Minecraft			mc;
	
	public int						lastItemPickupTime				= 0;
	public List<ItemPickup>			itemPickups						= new ArrayList();
	
	public int						width							= 0;
	public int						height							= 0;
	
	public GuiCSHUDIngame(Minecraft mc)
	{
		super(mc);
		this.mc = mc;
	}
	
	@Override
	public void updateTick()
	{
		Iterator<ItemPickup> iterator = itemPickups.iterator();
		
		if (lastItemPickupTime < maxPickupTime + 20)
		{
			lastItemPickupTime++;
		}
		
		while (iterator.hasNext())
		{
			ItemPickup itemPickup = iterator.next();
			itemPickup.time++;
			
			if (itemPickup.time > maxPickupTime + 20)
			{
				iterator.remove();
			}
		}
	}
	
	@ForgeSubscribe
	public void onItemPickup(EntityItemPickupEvent event)
	{
		ItemStack stack = event.item.getEntityItem().copy();
		if (stack != null && stack.stackSize > 0)
		{
			for (ItemPickup itemPickup : this.itemPickups)
			{
				if (itemPickup.stack.isItemEqual(stack))
				{
					itemPickup.time = 0;
					itemPickup.stack.stackSize += stack.stackSize;
					return;
				}
			}
			
			this.lastItemPickupTime = 0;
			this.itemPickups.add(new ItemPickup(stack));
		}
	}
	
	@ForgeSubscribe
	public void onRenderGameOverlay(RenderGameOverlayEvent event)
	{
		this.width = event.resolution.getScaledWidth();
		this.height = event.resolution.getScaledHeight();
		
		if (this.mc.inGameHasFocus)
		{
			this.renderCurrentObject();
			this.renderPickups();
			this.renderActivePotionEffects();
		}
		
		GL11.glColor4f(1F, 1F, 1F, 1F);
		this.mc.renderEngine.bindTexture(GuiContainer.icons);
	}
	
	public void renderActivePotionEffects()
	{
		Collection<PotionEffect> activeEffects = this.mc.thePlayer.getActivePotionEffects();
		
		if (!activeEffects.isEmpty())
		{
			List<PotionEffect> potionEffects = new ArrayList(activeEffects);
			for (int i = 0; i < potionEffects.size(); i++)
			{
				this.drawPotionEffect(0, i * pickupBoxHeight, potionEffects.get(i));
			}
		}
	}
	
	public void drawPotionEffect(int x, int y, PotionEffect potionEffect)
	{
		String s = String.format("%s %s (%s)", I18n.getString(potionEffect.getEffectName()), CSString.convertToRoman(potionEffect.getAmplifier() + 1), Potion.getDurationString(potionEffect));
		int color = this.getPotionEffectColor(potionEffect);
		this.drawHoveringFrameAtPos(x, y, this.mc.fontRenderer.getStringWidth(s) + 10, potionEffectBoxHeight, color);
		
		this.mc.fontRenderer.drawStringWithShadow(s, x + 5, y + 5, potionUseColorForText ? color : 0xFFFFFF);
	}
	
	protected int getPotionEffectColor(PotionEffect potionEffect)
	{
		if (potionEffect != null)
		{
			if (potionEffect.getIsAmbient())
			{
				return potionAmbientEffectColor;
			}
			else
			{
				Potion potion = Potion.potionTypes[potionEffect.getPotionID()];
				if (potion != null)
				{
					return potion.getIsBadEffect() ? potionBadEffectColor : potionGoodEffectColor;
				}
			}
		}
		return potionNoEffectColor;
	}
	
	public void renderPickups()
	{
		int l = (this.lastItemPickupTime < pickupBoxHeight ? this.lastItemPickupTime : pickupBoxHeight);
		
		for (int i = 0;; i++)
		{
			// Avoid ConcurrentModificationException
			if (i < this.itemPickups.size())
			{
				ItemPickup itemPickup = this.itemPickups.get(i);
				int k = (itemPickup.time > maxPickupTime ? itemPickup.time - maxPickupTime : 0) * 8;
				
				this.drawItemPickup(this.width + k, ((i - 1) * pickupBoxHeight) + l, itemPickup);
			}
			else
			{
				break;
			}
		}
	}
	
	public void drawItemPickup(int x, int y, ItemPickup itemPickup)
	{
		ItemStack stack = itemPickup.stack;
		String s = stack.stackSize == 1 ? stack.getDisplayName() : String.format("%s (%d)", stack.getDisplayName(), stack.stackSize);
		int width = this.mc.fontRenderer.getStringWidth(s) + 10;
		
		this.drawHoveringFrameAtPos(x - width, y, width, pickupBoxHeight, pickupBoxColor);
		this.mc.fontRenderer.drawString(s, x - width + 5, y + 5, pickupTextColor);
	}
	
	public void renderCurrentObject()
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
				AxisAlignedBB aabb = entity.getBoundingBox();
				int width1;
				if (aabb != null)
				{
					width1 = (int) Math.max(aabb.maxX - aabb.minX, aabb.maxZ - aabb.minZ);
					height = (int) (aabb.maxY - aabb.minY);
				}
				else
				{
					width1 = 40;
					height = (int) (entity.height * 16);
				}
				
				width = width1 + this.mc.fontRenderer.getStringWidth(renderName);
				height += 8;
				x1 = width1 - 4;
				y1 = 10;
				
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
			
			x0 = ((this.width - width) / 2);
			y1 = ((height - y1)) / 2;
			
			GL11.glColor4f(1F, 1F, 1F, 1F);
			
			this.drawHoveringFrameAtPos(((this.width - width) / 2), 0, width, height, color);
			
			if (isEntity)
			{
				this.renderEntity(mop.entityHit, x0 + 16, y0 + height - 4, 16);
			}
			else
			{
				GL11.glEnable(GL11.GL_DEPTH_TEST);
				RenderHelper.enableGUIStandardItemLighting();
				new RenderItem().renderItemIntoGUI(this.mc.fontRenderer, this.mc.renderEngine, stack, x0 + 4, y0 + ((height - 16) / 2), true);
				RenderHelper.disableStandardItemLighting();
			}
			
			this.drawString(this.mc.fontRenderer, renderName, x0 + x1, y0 + y1, currentObjUseColorForText ? color : 0xFFFFFF);
		}
	}
	
	public void renderEntity(Entity par5EntityLivingBase, int x, int y, float scale)
	{
		GL11.glColor4f(1F, 1F, 1F, 1F);
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		
		GL11.glPushMatrix();
		
		GL11.glTranslatef((float) x, (float) y, 50.0F);
		GL11.glScalef(-scale, scale, scale);
		GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
		
		GL11.glRotatef(135.0F, 0.0F, 1.0F, 0.0F);
		RenderHelper.enableStandardItemLighting();
		GL11.glRotatef(-135.0F, 0.0F, 1.0F, 0.0F);
		
		GL11.glTranslatef(0.0F, par5EntityLivingBase.yOffset, 0.0F);
		
		RenderManager.instance.playerViewY = 180.0F;
		RenderManager.instance.renderEntityWithPosYaw(par5EntityLivingBase, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F);
		
		GL11.glPopMatrix();
		
		RenderHelper.disableStandardItemLighting();
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
	}
	
	public void drawHoveringText(List<String> list, int x, int y, FontRenderer fontrenderer)
	{
		if (!list.isEmpty())
		{
			GL11.glDisable(GL12.GL_RESCALE_NORMAL);
			RenderHelper.disableStandardItemLighting();
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			this.zLevel = 300.0F;
			
			int width = 0;
			for (String line : list)
			{
				int lineWidth = fontrenderer.getStringWidth(line);
				
				if (lineWidth > width)
				{
					width = lineWidth;
				}
			}
			
			int x1 = x + 0;
			int y1 = y - 0;
			int height = 8;
			
			if (list.size() > 1)
			{
				height += 2 + (list.size() - 1) * 10;
			}
			
			if (x1 + width > this.width)
			{
				x1 -= 28 + width;
			}
			
			if (y1 + height + 6 > this.height)
			{
				y1 = this.height - height - 6;
			}
			
			this.drawHoveringFrame(x1, y1, width, height, hoveringFrameDefaultColor);
			
			for (int i = 0; i < list.size(); ++i)
			{
				String s1 = (String) list.get(i);
				fontrenderer.drawStringWithShadow(s1, x1, y1, -1);
				
				if (i == 0)
				{
					y1 += 2;
				}
				
				y1 += 10;
			}
			
			this.zLevel = 0.0F;
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			RenderHelper.enableStandardItemLighting();
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		}
	}
	
	public void drawHoveringFrameAtPos(int x, int y, int width, int height, int color)
	{
		this.drawHoveringFrame(x + 4, y + 4, width - 8, height - 8, color);
	}
	
	public void drawHoveringFrame(int x, int y, int width, int height, int color)
	{
		int l1 = hoveringFrameBackgroundColor;
		this.drawGradientRect(x - 3, y - 4, x + width + 3, y - 3, l1, l1);
		this.drawGradientRect(x - 3, y + height + 3, x + width + 3, y + height + 4, l1, l1);
		this.drawGradientRect(x - 3, y - 3, x + width + 3, y + height + 3, l1, l1);
		this.drawGradientRect(x - 4, y - 3, x - 3, y + height + 3, l1, l1);
		this.drawGradientRect(x + width + 3, y - 3, x + width + 4, y + height + 3, l1, l1);
		int i2 = 0xF0000000 | color;
		int j2 = (i2 & 0xFEFEFE) >> 1 | i2 & -0xFFFFFF;
		this.drawGradientRect(x - 3, y - 3 + 1, x - 3 + 1, y + height + 3 - 1, i2, j2);
		this.drawGradientRect(x + width + 2, y - 3 + 1, x + width + 3, y + height + 3 - 1, i2, j2);
		this.drawGradientRect(x - 3, y - 3, x + width + 3, y - 3 + 1, i2, i2);
		this.drawGradientRect(x - 3, y + height + 2, x + width + 3, y + height + 3, j2, j2);
	}
}
