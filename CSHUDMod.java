package clashsoft.mods.cshud;

import clashsoft.cslib.minecraft.update.CSUpdate;
import clashsoft.mods.cshud.common.CSHUDCommonProxy;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.registry.LanguageRegistry;

@Mod(modid = "CSHUD", name = "Clashsoft's HUD Mod", version = CSHUDMod.VERSION)
public class CSHUDMod
{
	public static final int REVISION = 0;
	public static final String VERSION = CSUpdate.CURRENT_VERSION + "-" + REVISION;
	
	@SidedProxy(clientSide = "clashsoft.mods.cshud.client.CSHUDClientProxy", serverSide = "clashsoft.mods.cshud.common.CSHUDCommonProxy")
	public static CSHUDCommonProxy proxy;
	
	@EventHandler
	public void load(FMLInitializationEvent event)
	{
		proxy.init();
		
		LanguageRegistry lr = LanguageRegistry.instance();
		
		lr.addStringLocalization("entity.MinecartRideable.name", "Minecart");
		lr.addStringLocalization("entity.MinecartFurnace.name", "Minecart with Furnace");
		lr.addStringLocalization("entity.MinecartChest.name", "Minecart with Chest");
		lr.addStringLocalization("entity.MinecartTNT.name", "Minecart with TNT");
		lr.addStringLocalization("entity.MinecartHopper.name", "Minecart with Hopper");
		lr.addStringLocalization("entity.ItemFrame.name", "Item Frame");
	}
}
