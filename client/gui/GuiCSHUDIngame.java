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
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;

public class GuiCSHUDIngame extends GuiIngameForge
{
	public static final ResourceLocation	inventoryTexture	= new ResourceLocation("minecraft", "textures/gui/container/inventory.png");
	
	public final Minecraft					mc;
	
	public int								lastItemPickupTime	= 0;
	public List<ItemPickup>					itemPickups			= new ArrayList();
	
	public int								width				= 0;
	public int								height				= 0;
	
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
			GL11.glPushMatrix();
			
			GL11.glColor4f(1F, 1F, 1F, 1F);
			this.renderPickups();
			this.renderActivePotionEffects();
			this.renderCurrentObject();
			
			GL11.glPopMatrix();
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
			for (int i = 0, j = 0; i < potionEffects.size() && j < this.height; i++)
			{
				j += this.drawPotionEffect(0, j, potionEffects.get(i));
			}
		}
	}
	
	public int drawPotionEffect(int x, int y, PotionEffect potionEffect)
	{
		Potion potion = Potion.potionTypes[potionEffect.getPotionID()];
		
		int mode = potionEffectDisplayMode;
		boolean renderIcon = (mode & 1) != 0;
		boolean renderDuration = (mode & 2) != 0;
		boolean renderAmplifier = (mode & 4) != 0;
		int color = this.getPotionEffectColor(potion, potionEffect.getIsAmbient());
		int textColor = potionUseColorForText ? color : 0xFFFFFF;
		
		if (!renderIcon)
		{
			String s = getPotionEffectDisplayString(potionEffect, renderDuration, renderAmplifier);
			
			this.drawHoveringFrameAtPos(x, y, Math.max(80, this.mc.fontRenderer.getStringWidth(s) + 10), potionEffectBoxHeight, color);
			
			this.mc.fontRenderer.drawStringWithShadow(s, x + 5, y + 5, textColor);
			return potionEffectBoxHeight;
		}
		else
		{
			this.drawHoveringFrameAtPos(x, y, 28, 28, color);
			
			if (potion.hasStatusIcon())
			{
				GL11.glColor4f(1F, 1F, 1F, 1F);
				this.mc.renderEngine.bindTexture(inventoryTexture);
				int l = potion.getStatusIconIndex();
				this.drawTexturedModalRect(x + 5, y + 5, 0 + l % 8 * 18, 198 + l / 8 * 18, 18, 18);
			}
			
			if (renderAmplifier && potionEffect.getAmplifier() > 0)
			{
				this.mc.fontRenderer.drawStringWithShadow(CSString.convertToRoman(potionEffect.getAmplifier() + 1), x + 3, y + 3, textColor);
			}
			if (renderDuration)
			{
				String duration = Potion.getDurationString(potionEffect);
				int width = this.mc.fontRenderer.getStringWidth(duration);
				
				this.mc.fontRenderer.drawStringWithShadow(duration, x + 26 - width, y + 18, 0xFFFFFF);
			}
			
			return 28;
		}
	}
	
	protected String getPotionEffectDisplayString(PotionEffect potionEffect, boolean duration, boolean amplifier)
	{
		String effectName = I18n.getString(potionEffect.getEffectName());
		if (amplifier && duration)
		{
			return String.format("%s %s (%s)", effectName, CSString.convertToRoman(potionEffect.getAmplifier() + 1), Potion.getDurationString(potionEffect));
		}
		else if (amplifier)
		{
			return String.format("%s %s", effectName, CSString.convertToRoman(potionEffect.getAmplifier() + 1));
		}
		else
		{
			return String.format("%s (%s)", effectName, Potion.getDurationString(potionEffect));
		}
	}
	
	protected int getPotionEffectColor(Potion potion, boolean isAmbient)
	{
		if (isAmbient)
		{
			return potionAmbientEffectColor;
		}
		else if (potion != null)
		{
			return potion.getIsBadEffect() ? potionBadEffectColor : potionGoodEffectColor;
		}
		else
		{
			return potionNoEffectColor;
		}
	}
	
	public void renderPickups()
	{
		int l = (this.lastItemPickupTime < pickupBoxHeight ? pickupBoxHeight - this.lastItemPickupTime : 0);
		
		for (int i = 0, j = 0; i < this.itemPickups.size() && j < this.height; i++)
		{
			ItemPickup itemPickup = this.itemPickups.get(i);
			
			j += this.drawItemPickup(this.width, j - l, itemPickup);
		}
	}
	
	public int drawItemPickup(int x, int y, ItemPickup itemPickup)
	{
		ItemStack stack = itemPickup.stack;
		
		String s = stack.stackSize == 1 ? stack.getDisplayName() : String.format("%s (%d)", stack.getDisplayName(), stack.stackSize);
		int width = Math.max(80, this.mc.fontRenderer.getStringWidth(s) + 10);
		
		if (itemPickup.time > maxPickupTime)
		{
			float f = width / 20F;
			int i = itemPickup.time - maxPickupTime;
			x += i * f;
		}
		
		this.drawHoveringFrameAtPos(x - width, y, width, pickupBoxHeight, pickupBoxColor);
		this.mc.fontRenderer.drawString(s, x - width + 5, y + 5, pickupTextColor);
		
		return pickupBoxHeight;
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
					height = (int) (Math.max(aabb.maxY - aabb.minY, entity.getEyeHeight()) * 16);
					width1 = (int) (Math.max(aabb.maxX - aabb.minX, aabb.maxZ - aabb.minZ));
				}
				else
				{
					height = (int) (Math.max(entity.height, entity.getEyeHeight() + 0.5F) * 16);
					width1 = height + 16;
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
			
			x0 = ((this.width - width) / 2);
			y1 = ((height - y1)) / 2;
			
			GL11.glColor4f(1F, 1F, 1F, 1F);
			
			this.drawHoveringFrameAtPos(((this.width - width) / 2), 0, width, height, color);
			
			if (isEntity)
			{
				GL11.glPushMatrix();
				this.renderEntity(mop.entityHit, x0 + (x1 / 2), y0 + height - 4, 16);
				GL11.glPopMatrix();
			}
			else
			{
				RenderHelper.enableGUIStandardItemLighting();
				GL11.glEnable(GL11.GL_DEPTH_TEST);
				
				RenderItem itemRenderer = new RenderItem();
				int x2 = x0 + 4;
				int y2 = y0 + y1 - 4;
				
				itemRenderer.renderItemAndEffectIntoGUI(this.mc.fontRenderer, this.mc.getTextureManager(), stack, x2, y2);
				itemRenderer.renderItemOverlayIntoGUI(this.mc.fontRenderer, this.mc.getTextureManager(), stack, x2, x2, null);
				
				RenderHelper.disableStandardItemLighting();
			}
			
			this.drawString(this.mc.fontRenderer, renderName, x0 + x1, y0 + y1, currentObjUseColorForText ? color : 0xFFFFFF);
		}
	}
	
	public void renderEntity(Entity par5EntityLivingBase, int x, int y, float scale)
	{
		GL11.glPushMatrix();
		
		GL11.glColor4f(1F, 1F, 1F, 1F);
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		
		GL11.glTranslatef((float) x, (float) y, 50.0F);
		GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
		GL11.glScalef(scale, scale, scale);
		
		RenderHelper.enableGUIStandardItemLighting();
		
		GL11.glTranslatef(0.0F, par5EntityLivingBase.yOffset, 0.0F);
		
		RenderManager.instance.playerViewY = 180.0F;
		RenderManager.instance.renderEntityWithPosYaw(par5EntityLivingBase, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F);
		
		RenderHelper.disableStandardItemLighting();
		
		GL11.glPopMatrix();
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
