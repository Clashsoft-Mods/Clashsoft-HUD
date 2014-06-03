package clashsoft.mods.cshud;

import clashsoft.cslib.config.CSConfig;
import clashsoft.cslib.minecraft.init.CSLib;
import clashsoft.cslib.minecraft.init.ClashsoftMod;
import clashsoft.cslib.minecraft.update.CSUpdate;
import clashsoft.mods.cshud.api.IHUDComponent;
import clashsoft.mods.cshud.api.ITooltipHandler;
import clashsoft.mods.cshud.common.CSHUDProxy;
import clashsoft.mods.cshud.components.Alignment;
import clashsoft.mods.cshud.network.CSHUDNetHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = CSHUD.MODID, name = CSHUD.NAME, version = CSHUD.VERSION, dependencies = CSHUD.DEPENDENCIES)
public class CSHUD extends ClashsoftMod<CSHUDNetHandler>
{
	public static final String	MODID							= "cshud";
	public static final String	ACRONYM							= "cshud";
	public static final String	NAME							= "Clashsoft's HUD Mod";
	public static final String	VERSION							= CSUpdate.CURRENT_VERSION + "-1.0.1";
	public static final String	DEPENDENCIES					= CSLib.DEPENDENCY;
	
	@Instance(MODID)
	public static CSHUD			instance;
	
	public static CSHUDProxy	proxy							= createProxy("clashsoft.mods.cshud.client.CSHUDClientProxy", "clashsoft.mods.cshud.common.CSHUDProxy");
	
	public static boolean		alwaysShow						= false;
	public static boolean		showCurrentObject				= true;
	public static boolean		showPotionEffects				= true;
	public static boolean		showItemPickups					= true;
	public static boolean		showWorldInfo					= true;
	public static boolean		showArmorStatus					= true;
	
	public static int			hoveringFrameDefaultColor		= 0x5000FF;
	public static int			hoveringFrameBackgroundColor	= 0x10000F;
	public static int			hoveringFrameAlpha				= 0x0000D0;
	
	public static Alignment		currentObjAlignment				= Alignment.TOP_CENTER;
	public static boolean		currentObjLiquids				= false;
	public static boolean		currentObjUseColorForText		= false;
	public static double		currentObjCustomReach			= 0D;
	public static int			currentObjBlockColor			= 0xFFFFFF;
	public static int			currentObjAnimalEntityColor		= 0x00FF00;
	public static int			currentObjWaterEntityColor		= 0x00FFFF;
	public static int			currentObjMonsterEntityColor	= 0xFF0000;
	public static int			currentObjOtherEntityColor		= 0xFFFFFF;
	public static int			currentObjBoxOffsetX			= 4;
	public static int			currentObjBoxOffsetY			= 4;
	
	public static boolean		tooltipModName					= false;
	public static boolean		tooltipBreakProgress			= true;
	public static boolean		tooltipDrops					= true;
	public static boolean		tooltipTEData					= true;
	public static boolean		tooltipTEDataTick				= true;
	public static boolean		tooltipTEDataAdvanced			= false;
	public static int			tooltipCommandThreshold			= 32;
	
	public static Alignment		pickupAlignment					= Alignment.TOP_RIGHT;
	public static int			pickupDisplayTime					= 100;
	public static int			pickupBoxHeight					= 17;
	public static int			pickupBoxColor					= 0xA4A4A4;
	public static int			pickupTextColor					= 0xFFFFFF;
	public static int			pickupBoxOffsetX				= 4;
	public static int			pickupBoxOffsetY				= 4;
	
	public static Alignment		potionAlignment					= Alignment.TOP_LEFT;
	public static boolean		potionUseColorForText			= true;
	public static int			potionEffectDisplayMode			= 7;
	public static int			potionEffectBoxHeight			= 17;
	public static int			potionGoodEffectColor			= 0x00FF00;
	public static int			potionBadEffectColor			= 0xFF0000;
	public static int			potionAmbientEffectColor		= 0x0081FF;
	public static int			potionNoEffectColor				= 0xFFFFFF;
	public static int			potionBoxOffsetX				= 4;
	public static int			potionBoxOffsetY				= 4;
	
	public static Alignment		weatherAlignment				= Alignment.BOTTOM_LEFT;
	public static boolean		weatherUseColorForText			= false;
	public static boolean		weatherRandomizeDownfall		= false;
	public static boolean		weatherRenderSnowAsRain			= false;
	public static int			weatherDayColor					= 0xFFFF00;
	public static int			weatherNightColor				= 0x0000FF;
	public static int			weatherBoxOffsetX				= 4;
	public static int			weatherBoxOffsetY				= 4;
	
	public static Alignment		armorStatusAlignment			= Alignment.BOTTOM_RIGHT;
	public static boolean		armorStatusRenderCurrentItem	= true;
	public static boolean		armorStatusUseColorForText		= false;
	public static int			armorStatusBoxColor				= 0xFFFFFF;
	public static int			armorStatusBoxOffsetX			= 4;
	public static int			armorStatusBoxOffsetY			= 4;
	
	public CSHUD()
	{
		super(proxy, MODID, NAME, ACRONYM, VERSION);
		this.hasConfig = true;
		this.netHandlerClass = CSHUDNetHandler.class;
		this.url = "https://github.com/Clashsoft/Clashsoft-HUD/wiki/";
	}
	
	@Override
	public void readConfig()
	{
		alwaysShow = CSConfig.getBool("general", "Always Show HUD", alwaysShow);
		showCurrentObject = CSConfig.getBool("general", "Show Current Object Display", showCurrentObject);
		showPotionEffects = CSConfig.getBool("general", "Show Potion Effect Display", showPotionEffects);
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
		currentObjBoxOffsetX = CSConfig.getInt("currentobj", "Box Offset X", currentObjBoxOffsetX);
		currentObjBoxOffsetY = CSConfig.getInt("currentobj", "Box Offset Y", currentObjBoxOffsetY);
		
		tooltipModName = CSConfig.getBool("tooltip", "Mod Name", tooltipModName);
		
		tooltipBreakProgress = CSConfig.getBool("tooltip", "Break Progress", tooltipBreakProgress);
		tooltipDrops = CSConfig.getBool("tooltip", "Drops", tooltipDrops);
		tooltipTEData = CSConfig.getBool("tooltip", "Tile Entity Info", tooltipTEData);
		tooltipTEDataTick = CSConfig.getBool("tooltip", "Update Tile Entities each Tick", tooltipTEDataTick);
		tooltipTEDataAdvanced = CSConfig.getBool("tooltip", "Advanced Tile Entity Data", tooltipTEDataAdvanced);
		tooltipCommandThreshold = CSConfig.getInt("tooltip", "Command Block Treshold", tooltipCommandThreshold);
		
		pickupAlignment = Alignment.parseAlignment(CSConfig.getString("itempickup", "Alignment", pickupAlignment));
		pickupDisplayTime = CSConfig.getInt("itempickup", "Max Display Time", pickupDisplayTime);
		pickupBoxHeight = CSConfig.getInt("itempickup", "Box Height", pickupBoxHeight);
		pickupBoxColor = CSConfig.getInt("itempickup", "Box Color", pickupBoxColor);
		pickupTextColor = CSConfig.getInt("itempickup", "Text Color", pickupTextColor);
		pickupBoxOffsetX = CSConfig.getInt("itempickup", "Box Offset X", pickupBoxOffsetX);
		pickupBoxOffsetY = CSConfig.getInt("itempickup", "Box Offset Y", pickupBoxOffsetY);
		
		potionAlignment = Alignment.parseAlignment(CSConfig.getString("buff", "Alignment", potionAlignment));
		potionUseColorForText = CSConfig.getBool("buff", "Use Frame Color for Text", potionUseColorForText);
		potionEffectBoxHeight = CSConfig.getInt("buff", "Potion Display Box Height", potionEffectBoxHeight);
		potionGoodEffectColor = CSConfig.getInt("buff", "Good Effect Color", potionGoodEffectColor);
		potionBadEffectColor = CSConfig.getInt("buff", "Bad Effect Color", potionBadEffectColor);
		potionAmbientEffectColor = CSConfig.getInt("buff", "Ambient Effect Color", potionAmbientEffectColor);
		potionNoEffectColor = CSConfig.getInt("buff", "No Effect Color", potionNoEffectColor);
		potionBoxOffsetX = CSConfig.getInt("buff", "Box Offset X", potionBoxOffsetX);
		potionBoxOffsetY = CSConfig.getInt("buff", "Box Offset Y", potionBoxOffsetY);
		
		weatherAlignment = Alignment.parseAlignment(CSConfig.getString("weather", "Alignment", weatherAlignment));
		weatherUseColorForText = CSConfig.getBool("weather", "Use Frame Color for Text", weatherUseColorForText);
		weatherRenderSnowAsRain = CSConfig.getBool("weather", "Render Snow As Rain", weatherRenderSnowAsRain);
		weatherRandomizeDownfall = CSConfig.getBool("weather", "Randomize Downfall Animation", weatherRandomizeDownfall);
		weatherDayColor = CSConfig.getInt("weather", "Day Color", weatherDayColor);
		weatherNightColor = CSConfig.getInt("weather", "Night Color", weatherNightColor);
		weatherBoxOffsetX = CSConfig.getInt("weather", "Box Offset X", weatherBoxOffsetX);
		weatherBoxOffsetY = CSConfig.getInt("weather", "Box Offset Y", weatherBoxOffsetY);
		
		armorStatusAlignment = Alignment.parseAlignment(CSConfig.getString("armorstatus", "Alignment", armorStatusAlignment));
		armorStatusRenderCurrentItem = CSConfig.getBool("armorstatus", "Render Current Item", armorStatusRenderCurrentItem);
		armorStatusUseColorForText = CSConfig.getBool("armorstatus", "Use Damage Color for Text", armorStatusUseColorForText);
		armorStatusBoxColor = CSConfig.getInt("armorstatus", "Box Color", armorStatusBoxColor);
		armorStatusBoxOffsetX = CSConfig.getInt("armorstatus", "Box Offset X", pickupBoxOffsetX);
		armorStatusBoxOffsetY = CSConfig.getInt("armorstatus", "Box Offset Y", pickupBoxOffsetY);
	}
	
	@Override
	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		super.preInit(event);
	}
	
	@Override
	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		super.init(event);
	}
	
	@Override
	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		super.postInit(event);
	}
	
	public static void registerHUDComponent(IHUDComponent component)
	{
		proxy.registerHUDComponent(component);
	}
	
	public static void registerToolTipHandler(ITooltipHandler handler)
	{
		proxy.registerToolTipHandler(handler);
	}
}
