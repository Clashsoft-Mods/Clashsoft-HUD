package clashsoft.mods.cshud.network;

import clashsoft.cslib.minecraft.network.CSPacket;
import clashsoft.mods.cshud.CSHUD;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class PacketTEData extends CSPacket
{
	public byte				action;
	
	public World			world;
	public int				x;
	public int				y;
	public int				z;
	
	public NBTTagCompound	data;
	
	public PacketTEData()
	{
	}
	
	public PacketTEData(World world, int x, int y, int z)
	{
		this(CSHUDNetHandler.REQUEST, world, x, y, z, null);
	}
	
	public PacketTEData(byte action, World world, int x, int y, int z)
	{
		this(action, world, x, y, z, null);
	}
	
	public PacketTEData(World world, int x, int y, int z, NBTTagCompound data)
	{
		this(CSHUDNetHandler.SEND, world, x, y, z, data);
	}
	
	public PacketTEData(byte action, World world, int x, int y, int z, NBTTagCompound data)
	{
		this.action = action;
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		this.data = data;
	}
	
	@Override
	public void write(PacketBuffer buf)
	{
		buf.writeByte(this.action);
		writeWorld(buf, this.world);
		buf.writeInt(this.x);
		buf.writeInt(this.y);
		buf.writeInt(this.z);
		
		if (this.action == CSHUDNetHandler.SEND)
		{
			try
			{
				buf.writeNBTTagCompoundToBuffer(this.data);
			}
			catch (Exception ioex)
			{
			}
		}
	}
	
	@Override
	public void read(PacketBuffer buf)
	{
		this.action = buf.readByte();
		this.world = readWorld(buf);
		this.x = buf.readInt();
		this.y = buf.readInt();
		this.z = buf.readInt();
		
		if (this.action == CSHUDNetHandler.SEND)
		{
			try
			{
				this.data = buf.readNBTTagCompoundFromBuffer();
			}
			catch (Exception ioex)
			{
			}
		}
	}
	
	@Override
	public void handleClient(EntityPlayer player)
	{
		TileEntity te = TileEntity.createAndLoadEntity(this.data);
		CSHUD.proxy.setTileEntity(te);
	}
	
	@Override
	public void handleServer(EntityPlayerMP player)
	{
		CSHUD.instance.netHandler.sendTEData(this.world, this.x, this.y, this.z, player);
	}
}