package clashsoft.mods.cshud.network;

import java.io.*;

import clashsoft.cslib.util.CSLog;
import clashsoft.mods.cshud.CSHUDMod;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public class TileEntityData implements IPacketHandler
{
	public static final byte		REQUEST		= 0x1;
	public static final byte		SEND		= 0x2;
	
	private static TileEntityData	instance	= new TileEntityData();
	
	public static TileEntityData getInstance()
	{
		return instance;
	}
	
	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player)
	{
		if (CSHUDMod.CHANNEL.equals(packet.channel))
		{
			ByteArrayInputStream bis = new ByteArrayInputStream(packet.data);
			DataInputStream dis = new DataInputStream(bis);
			
			try
			{
				byte action = dis.readByte();
				int worldID = dis.readInt();
				int x = dis.readInt();
				int y = dis.readInt();
				int z = dis.readInt();
				
				World world = DimensionManager.getWorld(worldID);
				
				if (action == REQUEST)
				{
					sendTEData(world, x, y, z);
				}
				else if (action == SEND)
				{
					NBTTagCompound nbt = readNBTTagCompound(dis);
					TileEntity tileEntity = nbt == null ? null : TileEntity.createAndLoadEntity(nbt);
					CSHUDMod.proxy.setTileEntity(tileEntity);
				}
			}
			catch (Exception ex)
			{
				CSLog.error(ex);
			}
		}
	}
	
	public void requestTEData(World world, int x, int y, int z)
	{
		if (world.isRemote)
		{
			//CSLog.info("[CSHUD] Requesting Tile Entity Data (%d,%d,%d)", x, y, z);
			PacketDispatcher.sendPacketToServer(createPacketRequestTEData(world, x, y, z));
		}
	}
	
	public void sendTEData(World world, int x, int y, int z)
	{
		if (!world.isRemote)
		{
			PacketDispatcher.sendPacketToAllPlayers(createPacketTEData(world, x, y, z));
		}
	}
	
	protected Packet250CustomPayload createPacketRequestTEData(World world, int x, int y, int z)
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream(9);
		DataOutputStream dos = new DataOutputStream(bos);
		
		try
		{
			dos.writeByte(REQUEST);
			dos.writeInt(world.provider.dimensionId);
			dos.writeInt(x);
			dos.writeInt(y);
			dos.writeInt(z);
		}
		catch (IOException ex)
		{
			CSLog.error(ex);
		}
		
		return new Packet250CustomPayload(CSHUDMod.CHANNEL, bos.toByteArray());
	}
	
	protected Packet250CustomPayload createPacketTEData(World world, int x, int y, int z)
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream(9);
		DataOutputStream dos = new DataOutputStream(bos);
		
		try
		{
			dos.writeByte(SEND);
			dos.writeInt(world.provider.dimensionId);
			dos.writeInt(x);
			dos.writeInt(y);
			dos.writeInt(z);
			
			TileEntity te = world.getBlockTileEntity(x, y, z);
			if (te != null)
			{
				NBTTagCompound nbt = new NBTTagCompound();
				te.writeToNBT(nbt);
				writeNBTTagCompound(nbt, dos);
			}
			else
			{
				writeNBTTagCompound(null, dos);
			}
		}
		catch (IOException ex)
		{
			CSLog.error(ex);
		}
		
		return new Packet250CustomPayload(CSHUDMod.CHANNEL, bos.toByteArray());
	}
	
	public static void writeNBTTagCompound(NBTTagCompound nbt, DataOutput out) throws IOException
	{
		if (nbt == null)
		{
			out.writeShort(-1);
		}
		else
		{
			byte[] abyte = CompressedStreamTools.compress(nbt);
			out.writeShort((short) abyte.length);
			out.write(abyte);
		}
	}
	
	public static NBTTagCompound readNBTTagCompound(DataInput in) throws IOException
	{
		short short1 = in.readShort();
		
		if (short1 < 0)
		{
			return null;
		}
		else
		{
			byte[] abyte = new byte[short1];
			in.readFully(abyte);
			return CompressedStreamTools.decompress(abyte);
		}
	}
}
