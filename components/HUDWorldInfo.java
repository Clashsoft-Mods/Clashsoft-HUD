package clashsoft.mods.cshud.components;

import static clashsoft.mods.cshud.CSHUDMod.*;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class HUDWorldInfo extends HUDComponent
{
	public static final ResourceLocation	sunTexture			= new ResourceLocation("minecraft", "textures/environment/sun.png");
	public static final ResourceLocation	moonTexture			= new ResourceLocation("minecraft", "textures/environment/moon_phases.png");
	public static final ResourceLocation	rainTexture			= new ResourceLocation("minecraft", "textures/environment/rain.png");
	public static final ResourceLocation	snowTexture			= new ResourceLocation("minecraft", "textures/environment/snow.png");
	
	public Random rand = new Random();
	
	@Override
	public void render(float partialTickTime)
	{
		renderWorldInfo();
	}
	
	public void renderWorldInfo()
	{
		if (!showWorldInfo)
		{
			return;
		}
		
		Alignment align = Alignment.TOP_RIGHT;
		World world = this.mc.theWorld;
		int time = (int) world.getWorldTime() % 24000;
		boolean isDay = time < 12500;
		boolean isRaining = world.isRaining();
		boolean isThundering = world.isThundering();
		int color = isDay ? weatherDayColor : weatherNightColor;
		
		int frameX = align.getX(80, this.width);
		int frameY = align.getY(32, this.height);
		
		this.drawHoveringFrameAtPos(frameX, frameY, 80, 32, color);
		this.mc.fontRenderer.drawStringWithShadow(world.getWorldInfo().getWorldName(), frameX + 29, frameY + 4, 0xFFFFFF);
		
		this.mc.fontRenderer.drawStringWithShadow(StringUtils.ticksToElapsedTime(time), frameX + 29, frameY + 16, weatherUseColorForText ? color : 0xFFFFFF);
		
		GL11.glColor4f(1F, 1F, 1F, 1F);
		
		GL11.glPushMatrix();
		
		GL11.glTranslatef(frameX + 4F, frameY + 4F, 0F);
		if (isDay)
		{
			this.mc.renderEngine.bindTexture(sunTexture);
			
			GL11.glScalef(0.125F, 0.125F, 1F);
			this.drawTexturedModalRect(0, 0, 32, 32, 192, 192);
			GL11.glScalef(8F, 8F, 1F);
		}
		else
		{
			int moonPhase = world.getMoonPhase();
			int x1 = (moonPhase & 3) * 64;
			int y1 = (moonPhase >> 2) * 128;
			
			this.mc.renderEngine.bindTexture(moonTexture);
			
			GL11.glScalef(0.5F, 0.25F, 1F);
			this.drawTexturedModalRect(0, 0, 8 + x1, 16 + y1, 48, 96);
			GL11.glScalef(2F, 4F, 1F);
		}
		if (isRaining)
		{
			boolean snow = false;
			
			if (!weatherShowSnowAsRain)
			{
				BiomeGenBase biome = world.getBiomeGenForCoords((int) this.mc.thePlayer.posX, (int) this.mc.thePlayer.posZ);
				snow = biome.getFloatTemperature() <= 0.15F;
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
			
			GL11.glScalef(0.125F, 0.5F, 1F);
			this.drawTexturedModalRect(0, 0, 0, off, 192, 48);
			GL11.glScalef(8F, 2F, 1F);
		}
		GL11.glPopMatrix();
	}
}
