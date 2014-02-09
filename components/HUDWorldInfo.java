package clashsoft.mods.cshud.components;

import static clashsoft.mods.cshud.CSHUDMod.*;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class HUDWorldInfo extends HUDComponent
{
	public static final ResourceLocation	sunTexture	= new ResourceLocation("minecraft", "textures/environment/sun.png");
	public static final ResourceLocation	moonTexture	= new ResourceLocation("minecraft", "textures/environment/moon_phases.png");
	public static final ResourceLocation	rainTexture	= new ResourceLocation("minecraft", "textures/environment/rain.png");
	public static final ResourceLocation	snowTexture	= new ResourceLocation("minecraft", "textures/environment/snow.png");
	
	public Random							rand		= new Random();
	
	@Override
	public void render(float partialTickTime)
	{
		this.renderWorldInfo();
	}
	
	public void renderWorldInfo()
	{
		if (!showWorldInfo)
		{
			return;
		}
		
		Alignment align = weatherAlignment;
		World world = this.mc.theWorld;
		int time = (int) world.getWorldTime() % 24000;
		boolean isDay = time < 12500;
		boolean isRaining = world.isRaining();
		boolean isThundering = world.isThundering();
		int color = isDay ? weatherDayColor : weatherNightColor;
		
		int frameX = align.getX(80, this.width);
		int frameY = align.getY(32, this.height);
		
		this.drawHoveringFrame(frameX, frameY, 80, 32, color);
		this.mc.fontRenderer.drawStringWithShadow(this.getWorldName(), frameX + 29, frameY + 6, 0xFFFFFF);
		
		this.mc.fontRenderer.drawStringWithShadow(StringUtils.ticksToElapsedTime(time), frameX + 29, frameY + 18, weatherUseColorForText ? color : 0xFFFFFF);
		
		GL11.glColor4f(1F, 1F, 1F, 1F);
		
		GL11.glPushMatrix();
		
		GL11.glTranslatef(frameX, frameY, 0F);
		
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_COLOR);
		
		if (isDay)
		{
			this.mc.renderEngine.bindTexture(sunTexture);
			
			GL11.glScalef(0.125F, 0.125F, 1F);
			this.drawTexturedModalRect(0, 0, 0, 0, 256, 256);
			GL11.glScalef(8F, 8F, 1F);
		}
		else
		{
			int moonPhase = world.getMoonPhase();
			int x1 = (moonPhase & 3) * 64;
			int y1 = (moonPhase >> 2) * 128;
			
			this.mc.renderEngine.bindTexture(moonTexture);
			
			GL11.glScalef(0.5F, 0.25F, 1F);
			this.drawTexturedModalRect(0, 0, x1, y1, 64, 128);
			GL11.glScalef(2F, 4F, 1F);
		}
		
		GL11.glDisable(GL11.GL_BLEND);
		
		if (isRaining)
		{
			boolean snow = false;
			
			if (!weatherShowSnowAsRain)
			{
				EntityPlayer player = this.mc.thePlayer;
				int x = (int) player.posX;
				int y = (int) player.posY;
				int z = (int) player.posZ;
				BiomeGenBase biome = world.getBiomeGenForCoords(x, z);
				snow = biome.getFloatTemperature(x, y, z) <= 0.15F;
			}
			
			this.mc.renderEngine.bindTexture(snow ? snowTexture : rainTexture);
			
			int off = 0;
			if (weatherRandomizeDownfall)
			{
				off = this.rand.nextInt(256) | (snow ? 0x7 : 0x3);
			}
			else
			{
				off = 256 - (this.updateCounter & 255);
				if (!snow)
				{
					off = (off * 8 + (this.updateCounter & 7)) & 255;
				}
			}
			
			GL11.glTranslatef(4F, 4F, 0F);
			GL11.glScalef(0.125F, 0.5F, 1F);
			this.drawTexturedModalRect(0, 0, 0, off, 192, 48);
			GL11.glScalef(8F, 2F, 1F);
		}
		GL11.glPopMatrix();
	}
	
	public String getWorldName()
	{
		IntegratedServer server = this.mc.getIntegratedServer();
		if (server != null)
		{
			return server.getWorldName();
		}
		else
		{
			return EnumChatFormatting.ITALIC + "Server World";
		}
	}
}
