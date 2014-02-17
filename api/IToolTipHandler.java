package clashsoft.mods.cshud.api;

import java.util.List;

import clashsoft.mods.cshud.components.HUDCurrentObject;

import net.minecraft.item.ItemStack;

public interface ITooltipHandler
{
	public void addInformation(List<String> lines, HUDCurrentObject hud, ItemStack stack);
}
