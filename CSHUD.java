package clashsoft.mods.cshud;

import clashsoft.cslib.minecraft.update.CSUpdate;
import clashsoft.cslib.minecraft.util.CSConfig;
import clashsoft.mods.cshud.api.IHUDComponent;
import clashsoft.mods.cshud.api.IToolTipHandler;
import clashsoft.mods.cshud.common.CSHProxy;
import clashsoft.mods.cshud.components.Alignment;
import clashsoft.mods.cshud.network.CSHNetHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = CSHUD.MODID, name = CSHUD.NAME, version = CSHUD.VERSION)
public class CSHUD
{
	public static final String	MODID							= "cshud";
	public static final String	NAME							= "Clashsoft's HUD Mod";
	public static final int		REVISION						= 0;
	public static final String	VERSION							= CSUpdate.CURRENT_VERSION + "-" + REVISION;
	
	public static final String	CHANNEL							= "CSHUD";
	
	@Instance(MODID)
	public static CSHUD			instance;
	
	@SidedProxy(clientSide = "clashsoft.mods.cshud.client.CSHClientProxy", serverSide = "clashsoft.mods.cshud.common.CSHProxy")
	public static CSHProxy		proxy;
	
	public static CSHNetHandler	netHandler						= new CSHNetHandler();
	
	public static boolean		hasLoaded						= false;
	
	public static boolean		alwaysShow						= false;
	public static boolean		showCurrentObject				= true;
	public static boolean		showPotionEffectDisplay			= true;
	public static boolean		showItemPickups					= true;
	public static boolean		showWorldInfo					= true;
	public static boolean		showArmorStatus					= true;
	
	public static int			hoveringFrameDefaultColor		= 0x5000FF;
	public static int			hoveringFrameBackgroundColor	= 0x10000F;
	public static int			hoveringFrameAlpha				= 0x0000B0;
	
	public static Alignment		currentObjAlignment				= Alignment.TOP_CENTER;
	public static boolean		currentObjLiquids				= false;
	public static boolean		currentObjUseColorForText		= false;
	public static double		currentObjCustomReach			= 0D;
	public static int			currentObjBlockColor			= 0xFFFFFF;
	public static int			currentObjAnimalEntityColor		= 0x00FF00;
	public static int			currentObjWaterEntityColor		= 0x00FFFF;
	public static int			currentObjMonsterEntityColor	= 0xFF0000;
	public static int			currentObjOtherEntityColor		= 0xFFFFFF;
	
	public static boolean		tooltipTileEntityData			= true;
	public static boolean		tooltipAdvancedTileEntityData	= false;
	public static int			tooltipCommandThreshold			= 32;
	
	public static Alignment		pickupAlignment					= Alignment.TOP_RIGHT;
	public static int			maxPickupTime					= 100;
	public static int			pickupBoxHeight					= 17;
	public static int			pickupBoxColor					= 0xA4A4A4;
	public static int			pickupTextColor					= 0xFFFFFF;
	
	public static Alignment		potionAlignment					= Alignment.TOP_LEFT;
	public static boolean		potionUseColorForText			= true;
	public static int			potionEffectDisplayMode			= 7;
	public static int			potionEffectBoxHeight			= 17;
	public static int			potionGoodEffectColor			= 0x00FF00;
	public static int			potionBadEffectColor			= 0xFF0000;
	public static int			potionAmbientEffectColor		= 0x0081FF;
	public static int			potionNoEffectColor				= 0xFFFFFF;
	
	public static Alignment		weatherAlignment				= Alignment.BOTTOM_LEFT;
	public static boolean		weatherUseColorForText			= false;
	public static boolean		weatherRandomizeDownfall		= false;
	public static boolean		weatherShowSnowAsRain			= false;
	public static int			weatherDayColor					= 0xFFFF00;
	public static int			weatherNightColor				= 0x0000FF;
	
	public static Alignment		armorStatusAlignment			= Alignment.BOTTOM_RIGHT;
	public static boolean		armorStatusRenderCurrentItem	= true;
	public static boolean		armorStatusUseColorForText		= false;
	public static int			armorStatusBoxColor				= 0xFFFFFF;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		CSConfig.loadConfig(event.getSuggestedConfigurationFile(), NAME);
		
		alwaysShow = CSConfig.getBool("general", "Always Show HUD", alwaysShow);
		showCurrentObject = CSConfig.getBool("general", "Show Current Object Display", showCurrentObject);
		showPotionEffectDisplay = CSConfig.getBool("general", "Show Potion Effect Display", showPotionEffectDisplay);
		showItemPickups = CSConfig.getBool("general", "Show Pickup Display", showItemPickups);
		showWorldInfo = CSConfig.getBool("general", "Show World Info", showWorldInfo);
		showArmorStatus = CSConfig.getBool("general", "Show Armor Status", showArmorStatus);
		
		hoveringFrameDefaultColor = CSConfig.getInt("hoveringframe", "Default Color", hoveringFrameDefaultColor);
		hoveringFrameBackgroundColor = CSConfig.getInt("hoveringframe", "Background Color", hoveringFrameBackgroundColor);
		hoveringFrameAlpha = CSConfig.getInt("hoveringframe", "Alpha", hoveringFrameAlpha);
		
		currentObjAlignment = Alignment.parseAlignment(CSConfig.getString("currentobj", "Alignment", currentObjAlignment));
		currentObjLiquids = CSConfig.getBool("currentobj", "Liquids", currentObjLiquids);
		currentObjCustomReach = CSConfig.getDouble("currentobj", "Custom Reach Distance", currentObjCustomReach);
		currentObjUseColorForText = CSConfig.getBool("currentobj", "Use Frame Color for Text", currentObjUseColorForText);
		currentObjBlockColor = CSConfig.getInt("currentobj", "Block Color", currentObjBlockColor);
		currentObjAnimalEntityColor = CSConfig.getInt("currentobj", "Animal Entity Color", currentObjAnimalEntityColor);
		currentObjMonsterEntityColor = CSConfig.getInt("currentobj", "Monster Entity Color", currentObjMonsterEntityColor);
		currentObjWaterEntityColor = CSConfig.getInt("currentobj", "Water Entity Color", currentObjWaterEntityColor);
		currentObjOtherEntityColor = CSConfig.getInt("currentobj", "Other Entity Color", currentObjOtherEntityColor);
		
		tooltipTileEntityData = CSConfig.getBool("tooltip", "Tile Entity Info", tooltipTileEntityData);
		tooltipAdvancedTileEntityData = CSConfig.getBool("tooltip", "Advanced Tile Entity Data", tooltipAdvancedTileEntityData);
		tooltipCommandThreshold = CSConfig.getInt("tooltip", "Command Block Treshold", tooltipCommandThreshold);
		
		pickupAlignment = Alignment.parseAlignment(CSConfig.getString("itempickup", "Alignment", pickupAlignment));
		maxPickupTime = CSConfig.getInt("itempickup", "Max Display Time", maxPickupTime);
		pickupBoxHeight = CSConfig.getInt("itempickup", "Box Height", pickupBoxHeight);
		pickupBoxColor = CSConfig.getInt("itempickup", "Box Color", pickupBoxColor);
		pickupTextColor = CSConfig.getInt("itempickup", "Text Color", pickupTextColor);
		
		potionAlignment = Alignment.parseAlignment(CSConfig.getString("buff", "Alignment", potionAlignment));
		potionUseColorForText = CSConfig.getBool("buff", "Use Frame Color for Text", potionUseColorForText);
		potionEffectBoxHeight = CSConfig.getInt("buff", "Potion Display Box Height", potionEffectBoxHeight);
		potionGoodEffectColor = CSConfig.getInt("buff", "Good Effect Color", potionGoodEffectColor);
		potionBadEffectColor = CSConfig.getInt("buff", "Bad Effect Color", potionBadEffectColor);
		potionAmbientEffectColor = CSConfig.getInt("buff", "Ambient Effect Color", potionAmbientEffectColor);
		potionNoEffectColor = CSConfig.getInt("buff", "No Effect Color", potionNoEffectColor);
		
		weatherAlignment = Alignment.parseAlignment(CSConfig.getString("weather", "Alignment", weatherAlignment));
		weatherUseColorForText = CSConfig.getBool("weather", "Use Frame Color for Text", weatherUseColorForText);
		weatherShowSnowAsRain = CSConfig.getBool("weather", "Show Snow As Rain", weatherShowSnowAsRain);
		weatherRandomizeDownfall = CSConfig.getBool("weather", "Randomize Downfall Animation", weatherRandomizeDownfall);
		weatherDayColor = CSConfig.getInt("weather", "Day Color", weatherDayColor);
		weatherNightColor = CSConfig.getInt("weather", "Night Color", weatherNightColor);
		
		armorStatusAlignment = Alignment.parseAlignment(CSConfig.getString("armorstatus", "Alignment", armorStatusAlignment));
		armorStatusRenderCurrentItem = CSConfig.getBool("armorstatus", "Render Current Item", armorStatusRenderCurrentItem);
		armorStatusUseColorForText = CSConfig.getBool("armorstatus", "Use Damage Color for Text", armorStatusUseColorForText);
		armorStatusBoxColor = CSConfig.getInt("armorstatus", "Box Color", armorStatusBoxColor);
		
		CSConfig.saveConfig();
	}
	
	public static void registerHUDComponent(IHUDComponent component)
	{
		load();
		proxy.registerHUDComponent(component);
	}
	
	public static void registerToolTipHandler(IToolTipHandler handler)
	{
		load();
		proxy.registerToolTipHandler(handler);
	}
	
	@EventHandler
	public void load(FMLInitializationEvent event)
	{
		load();
		netHandler.init();
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		netHandler.postInit();
	}
	
	private static void load()
	{
		if (!hasLoaded && proxy.isClient())
		{
			proxy.init();
			hasLoaded = true;
		}
	}
}
