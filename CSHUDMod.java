package clashsoft.mods.cshud;

import clashsoft.cslib.minecraft.update.CSUpdate;
import clashsoft.cslib.minecraft.util.CSConfig;
import clashsoft.mods.cshud.common.CSHUDCommonProxy;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.LanguageRegistry;

@Mod(modid = "CSHUD", name = "Clashsoft's HUD Mod", version = CSHUDMod.VERSION)
public class CSHUDMod
{
	@SidedProxy(clientSide = "clashsoft.mods.cshud.client.CSHUDClientProxy", serverSide = "clashsoft.mods.cshud.common.CSHUDCommonProxy")
	public static CSHUDCommonProxy	proxy;
	
	public static final int			REVISION						= 0;
	public static final String		VERSION							= CSUpdate.CURRENT_VERSION + "-" + REVISION;
	
	public static int				hoveringFrameDefaultColor		= 0x5000FF;
	public static int				hoveringFrameBackgroundColor	= -0xFEFFFF0;
	
	public static boolean			currentObjUseColorForText		= false;
	public static int				currentObjBlockColor			= 0xFFFFFF;
	public static int				currentObjAnimalEntityColor		= 0x00FF00;
	public static int				currentObjWaterEntityColor		= 0x00FFFF;
	public static int				currentObjMonsterEntityColor	= 0xFF0000;
	public static int				currentObjOtherEntityColor		= 0xFFFFFF;
	
	public static int				maxPickupTime					= 100;
	public static int				pickupBoxHeight					= 17;
	public static int				pickupBoxColor					= 0xA4A4A4;
	public static int				pickupTextColor					= 0xFFFFFF;
	
	public static boolean			potionUseColorForText			= true;
	public static int				potionEffectBoxHeight			= 17;
	public static int				potionGoodEffectColor			= 0x00FF00;
	public static int				potionBadEffectColor			= 0xFF0000;
	public static int				potionAmbientEffectColor		= 0x0081FF;
	public static int				potionNoEffectColor				= 0xFFFFFF;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		CSConfig.loadConfig(event.getSuggestedConfigurationFile());
		
		hoveringFrameDefaultColor = CSConfig.getInt("hoveringframe", "Default Color", hoveringFrameDefaultColor);
		hoveringFrameBackgroundColor = CSConfig.getInt("hoveringframe", "Background Color", hoveringFrameBackgroundColor);
		
		currentObjUseColorForText = CSConfig.getBool("currentobj", "Use Frame Color for Text", currentObjUseColorForText);
		currentObjBlockColor = CSConfig.getInt("currentobj", "Block Color", currentObjBlockColor);
		currentObjAnimalEntityColor = CSConfig.getInt("currentobj", "Animal Entity Color", currentObjAnimalEntityColor);
		currentObjMonsterEntityColor = CSConfig.getInt("currentobj", "Monster Entity Color", currentObjMonsterEntityColor);
		currentObjWaterEntityColor = CSConfig.getInt("currentobj", "Water Entity Color", currentObjWaterEntityColor);
		currentObjOtherEntityColor = CSConfig.getInt("currentobj", "Other Entity Color", currentObjOtherEntityColor);
		
		maxPickupTime = CSConfig.getInt("itempickup", "Max Pickup Display Time", maxPickupTime);
		pickupBoxHeight = CSConfig.getInt("itempickup", "Pickup Display Box Height", pickupBoxHeight);
		pickupBoxColor = CSConfig.getInt("itempickup", "Pickup Display Box Color", pickupBoxColor);
		pickupTextColor = CSConfig.getInt("itempickup", "Pickup Display Text Color", pickupTextColor);
		
		potionUseColorForText = CSConfig.getBool("potion", "Use Frame Color for Text", potionUseColorForText);
		potionEffectBoxHeight = CSConfig.getInt("potion", "Potion Display Box Height", potionEffectBoxHeight);
		potionGoodEffectColor = CSConfig.getInt("potion", "Good Effect Color", potionGoodEffectColor);
		potionBadEffectColor = CSConfig.getInt("potion", "Bad Effect Color", potionBadEffectColor);
		potionAmbientEffectColor = CSConfig.getInt("potion", "Ambient Effect Color", potionAmbientEffectColor);
		potionNoEffectColor = CSConfig.getInt("potion", "No Effect Color", potionNoEffectColor);
		
		CSConfig.saveConfig();
	}
	
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
