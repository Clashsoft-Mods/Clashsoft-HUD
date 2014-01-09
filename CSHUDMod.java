package clashsoft.mods.cshud;

import clashsoft.cslib.minecraft.update.CSUpdate;
import clashsoft.cslib.minecraft.util.CSConfig;
import clashsoft.mods.cshud.api.IHUDComponent;
import clashsoft.mods.cshud.common.CSHUDCommonProxy;
import clashsoft.mods.cshud.components.Alignment;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.LanguageRegistry;

@Mod(modid = "CSHUD", name = "Clashsoft's HUD Mod", version = CSHUDMod.VERSION)
public class CSHUDMod
{
	@Instance("CSHUD")
	public static CSHUDMod			instance;
	
	@SidedProxy(clientSide = "clashsoft.mods.cshud.client.CSHUDClientProxy", serverSide = "clashsoft.mods.cshud.common.CSHUDCommonProxy")
	public static CSHUDCommonProxy	proxy;
	
	public static final int			REVISION						= 0;
	public static final String		VERSION							= CSUpdate.CURRENT_VERSION + "-" + REVISION;
	
	public static boolean			hasLoaded						= false;
	
	public static boolean			alwaysShow						= false;
	public static boolean			showCurrentObject				= true;
	public static boolean			showPotionEffectDisplay			= true;
	public static boolean			showItemPickups					= true;
	public static boolean			showWorldInfo					= true;
	
	public static int				hoveringFrameDefaultColor		= 0x5000FF;
	public static int				hoveringFrameBackgroundColor	= 0x10000F;
	public static int				hoveringFrameAlpha				= 0x0000B0;
	
	public static Alignment			currentObjAlignment				= Alignment.TOP_CENTER;
	public static boolean			currentObjUseColorForText		= false;
	public static int				currentObjBlockColor			= 0xFFFFFF;
	public static int				currentObjAnimalEntityColor		= 0x00FF00;
	public static int				currentObjWaterEntityColor		= 0x00FFFF;
	public static int				currentObjMonsterEntityColor	= 0xFF0000;
	public static int				currentObjOtherEntityColor		= 0xFFFFFF;
	
	public static Alignment			pickupAlignment					= Alignment.TOP_RIGHT;
	public static int				maxPickupTime					= 100;
	public static int				pickupBoxHeight					= 17;
	public static int				pickupBoxColor					= 0xA4A4A4;
	public static int				pickupTextColor					= 0xFFFFFF;
	
	public static Alignment			potionAlignment					= Alignment.TOP_LEFT;
	public static boolean			potionUseColorForText			= true;
	public static int				potionEffectDisplayMode			= 7;
	public static int				potionEffectBoxHeight			= 17;
	public static int				potionGoodEffectColor			= 0x00FF00;
	public static int				potionBadEffectColor			= 0xFF0000;
	public static int				potionAmbientEffectColor		= 0x0081FF;
	public static int				potionNoEffectColor				= 0xFFFFFF;
	
	public static Alignment			weatherAlignment				= Alignment.BOTTOM_LEFT;
	public static boolean			weatherUseColorForText			= false;
	public static int				weatherDayColor					= 0xFFFF00;
	public static int				weatherNightColor				= 0x0000FF;
	public static boolean			weatherRandomizeDownfall		= false;
	public static boolean			weatherShowSnowAsRain			= false;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		CSConfig.loadConfig(event.getSuggestedConfigurationFile(), "CSHUD");
		
		alwaysShow = CSConfig.getBool("general", "Always Show HUD", alwaysShow);
		showCurrentObject = CSConfig.getBool("general", "Show Current Object Display", showCurrentObject);
		showPotionEffectDisplay = CSConfig.getBool("general", "Show Potion Effect Display", showPotionEffectDisplay);
		showItemPickups = CSConfig.getBool("general", "Show Pickup Display", showItemPickups);
		showWorldInfo = CSConfig.getBool("general", "Show World Info", showWorldInfo);
		
		hoveringFrameDefaultColor = CSConfig.getInt("hoveringframe", "Default Color", hoveringFrameDefaultColor);
		hoveringFrameBackgroundColor = CSConfig.getInt("hoveringframe", "Background Color", hoveringFrameBackgroundColor);
		hoveringFrameAlpha = CSConfig.getInt("hoveringframe", "Alpha", hoveringFrameAlpha);
		
		currentObjAlignment = Alignment.parseAlignment(CSConfig.getString("currentobj", "Alignment", currentObjAlignment));
		currentObjUseColorForText = CSConfig.getBool("currentobj", "Use Frame Color for Text", currentObjUseColorForText);
		currentObjBlockColor = CSConfig.getInt("currentobj", "Block Color", currentObjBlockColor);
		currentObjAnimalEntityColor = CSConfig.getInt("currentobj", "Animal Entity Color", currentObjAnimalEntityColor);
		currentObjMonsterEntityColor = CSConfig.getInt("currentobj", "Monster Entity Color", currentObjMonsterEntityColor);
		currentObjWaterEntityColor = CSConfig.getInt("currentobj", "Water Entity Color", currentObjWaterEntityColor);
		currentObjOtherEntityColor = CSConfig.getInt("currentobj", "Other Entity Color", currentObjOtherEntityColor);
		
		pickupAlignment = Alignment.parseAlignment(CSConfig.getString("itempickup", "Alignment", pickupAlignment));
		maxPickupTime = CSConfig.getInt("itempickup", "Max Pickup Display Time", maxPickupTime);
		pickupBoxHeight = CSConfig.getInt("itempickup", "Pickup Display Box Height", pickupBoxHeight);
		pickupBoxColor = CSConfig.getInt("itempickup", "Pickup Display Box Color", pickupBoxColor);
		pickupTextColor = CSConfig.getInt("itempickup", "Pickup Display Text Color", pickupTextColor);
		
		potionAlignment = Alignment.parseAlignment(CSConfig.getString("buff", "Alignment", potionAlignment));
		potionUseColorForText = CSConfig.getBool("buff", "Use Frame Color for Text", potionUseColorForText);
		potionEffectBoxHeight = CSConfig.getInt("buff", "Potion Display Box Height", potionEffectBoxHeight);
		potionGoodEffectColor = CSConfig.getInt("buff", "Good Effect Color", potionGoodEffectColor);
		potionBadEffectColor = CSConfig.getInt("buff", "Bad Effect Color", potionBadEffectColor);
		potionAmbientEffectColor = CSConfig.getInt("buff", "Ambient Effect Color", potionAmbientEffectColor);
		potionNoEffectColor = CSConfig.getInt("buff", "No Effect Color", potionNoEffectColor);
		
		weatherAlignment = Alignment.parseAlignment(CSConfig.getString("weather", "Alignment", weatherAlignment));
		weatherUseColorForText = CSConfig.getBool("weather", "Use Frame Color for Text", weatherUseColorForText);
		weatherDayColor = CSConfig.getInt("weather", "Day Color", weatherDayColor);
		weatherNightColor = CSConfig.getInt("weather", "Night Color", weatherNightColor);
		weatherShowSnowAsRain = CSConfig.getBool("weather", "Show Snow As Rain", weatherShowSnowAsRain);
		weatherRandomizeDownfall = CSConfig.getBool("weather", "Randomize Downfall Animation", weatherRandomizeDownfall);
		
		CSConfig.saveConfig();
	}
	
	public static void registerHUDComponent(IHUDComponent component)
	{
		load();
		proxy.registerHUDComponent(component);
	}
	
	@EventHandler
	public void load(FMLInitializationEvent event)
	{
		load();
	}
	
	private static void load()
	{
		if (!hasLoaded)
		{
			proxy.init();
			
			LanguageRegistry lr = LanguageRegistry.instance();
			
			lr.addStringLocalization("entity.MinecartRideable.name", "Minecart");
			lr.addStringLocalization("entity.MinecartFurnace.name", "Minecart with Furnace");
			lr.addStringLocalization("entity.MinecartChest.name", "Minecart with Chest");
			lr.addStringLocalization("entity.MinecartTNT.name", "Minecart with TNT");
			lr.addStringLocalization("entity.MinecartHopper.name", "Minecart with Hopper");
			lr.addStringLocalization("entity.ItemFrame.name", "Item Frame");
			lr.addStringLocalization("entity.LeashKnot.name", "Leash Knot");
			lr.addStringLocalization("entity.EnderCrystal.name", "Ender Crystal");
			
			hasLoaded = true;
		}
	}
}
