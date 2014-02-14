package clashsoft.mods.cshud.network;

import clashsoft.cslib.minecraft.network.CSNetHandler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class CSHNetHandler extends CSNetHandler
{
	public static final byte			REQUEST		= 0x1;
	public static final byte			SEND		= 0x2;
	
	public CSHNetHandler()
	{
		super("CSHUD");
		
		this.registerPacket(PacketTEData.class);
	}
	
	public void requestTEData(World world, int x, int y, int z)
	{
		this.sendToServer(new PacketTEData(world, x, y, z));
	}
	
	public void sendTEData(World world, int x, int y, int z, EntityPlayer player)
	{
		NBTTagCompound data = new NBTTagCompound();
		TileEntity te = world.getTileEntity(x, y, z);
		
		if (te != null)
		{
			te.writeToNBT(data);
		}
		this.sendTo(new PacketTEData(world, x, y, z, data), (EntityPlayerMP) player);
	}
}
