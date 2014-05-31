package clashsoft.mods.cshud.api;

import net.minecraft.client.Minecraft;

public interface IHUDComponent
{
	public void setMinecraft(Minecraft mc);
	
	public void update();
	
	public void render(int width, int height, float partialTickTime);
}
