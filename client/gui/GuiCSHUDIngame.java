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
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.EventPriority;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;

public class GuiCSHUDIngame extends GuiIngameForge
{
	public static final ResourceLocation	inventoryTexture	= new ResourceLocation("minecraft", "textures/gui/container/inventory.png");
	public static final ResourceLocation	sunTexture			= new ResourceLocation("minecraft", "textures/environment/sun.png");
	public static final ResourceLocation	moonTexture			= new ResourceLocation("minecraft", "textures/environment/moon_phases.png");
	
	public final Minecraft					mc;
	
	public RenderItem						itemRenderer		= new RenderItem();
	
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
	
	@ForgeSubscribe(priority = EventPriority.HIGH)
	public void onItemPickup(EntityItemPickupEvent event)
	{
		ItemStack stack = event.item.getEntityItem();
		if (stack != null && stack.stackSize > 0)
		{
			stack = stack.copy();
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
		
		if (alwaysShow || this.mc.inGameHasFocus)
		{
			GL11.glPushMatrix();
			
			GL11.glColor4f(1F, 1F, 1F, 1F);
			
			if (showPickupDisplay)
			{
				this.renderPickups(event.partialTicks);
			}
			if (showPotionEffectDisplay)
			{
				this.renderActivePotionEffects();
			}
			if (showWorldInfo)
			{
				this.renderWorldInfo();
			}
			if (showCurrentObject)
			{
				this.renderCurrentObject(event.partialTicks);
			}
			
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
			return potion.isBadEffect() ? potionBadEffectColor : potionGoodEffectColor;
		}
		else
		{
			return potionNoEffectColor;
		}
	}
	
	public void renderWorldInfo()
	{
		World world = this.mc.theWorld;
		int time = (int) world.getWorldTime() % 24000;
		boolean isDay = time < 12500;
		int color = isDay ? weatherDayColor : weatherNightColor;
		
		GL11.glPushMatrix();
		
		this.drawHoveringFrameAtPos(0, height - 32, 80, 32, color);
		this.mc.fontRenderer.drawStringWithShadow(world.getWorldInfo().getWorldName(), 28, height - 26, 0xFFFFFF);
		this.mc.fontRenderer.drawStringWithShadow(StringUtils.ticksToElapsedTime(time), 28, height - 14, weatherUseColorForText ? color : 0xFFFFFF);
		
		if (isDay)
		{
			this.mc.renderEngine.bindTexture(sunTexture);
			
			GL11.glTranslatef(4F, height - 28F, 1F);
			GL11.glScalef(0.125F, 0.125F, 1F);
			this.drawTexturedModalRect(0, 0, 32, 32, 192, 192);
		}
		else
		{
			int moonPhase = world.getMoonPhase();
			int x1 = (moonPhase & 3) * 64;
			int y1 = (moonPhase >> 2) * 128;
			
			this.mc.renderEngine.bindTexture(moonTexture);
			
			GL11.glTranslatef(4F, height - 28F, 1F);
			GL11.glScalef(0.5F, 0.25F, 1F);
			this.drawTexturedModalRect(0, 0, 8 + x1, 16 + y1, 48, 96);
		}
		
		GL11.glPopMatrix();
	}
	
	public void renderPickups(float partialTickTime)
	{
		int l = (this.lastItemPickupTime < pickupBoxHeight ? pickupBoxHeight - this.lastItemPickupTime : 0);
		
		for (int i = 0, j = 0; i < this.itemPickups.size() && j < this.height; i++)
		{
			ItemPickup itemPickup = this.itemPickups.get(i);
			
			j += this.drawItemPickup(this.width, j - l, partialTickTime, itemPickup);
		}
	}
	
	public int drawItemPickup(int x, int y, float partialTickTime, ItemPickup itemPickup)
	{
		ItemStack stack = itemPickup.stack;
		
		String s = stack.stackSize == 1 ? stack.getDisplayName() : String.format("%s (%d)", stack.getDisplayName(), stack.stackSize);
		int width = Math.max(80, this.mc.fontRenderer.getStringWidth(s) + 10);
		
		if (itemPickup.time > maxPickupTime)
		{
			float f = width / 20F;
			float f1 = (itemPickup.time - maxPickupTime) + partialTickTime;
			x += f * f1;
		}
		
		this.drawHoveringFrameAtPos(x - width, y, width, pickupBoxHeight, pickupBoxColor);
		this.mc.fontRenderer.drawString(s, x - width + 5, y + 5, pickupTextColor);
		
		return pickupBoxHeight;
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
			
			x0 = ((this.width - width) / 2);
			y1 = ((height - y1)) / 2;
			
			GL11.glColor4f(1F, 1F, 1F, 1F);
			
			this.drawHoveringFrameAtPos(((this.width - width) / 2), 0, width, height, color);
			
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
			
			this.drawString(this.mc.fontRenderer, renderName, x0 + x1, y0 + y1, currentObjUseColorForText ? color : 0xFFFFFF);
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
