package clashsoft.mods.cshud.network;

import clashsoft.cslib.minecraft.network.CSCodec;
import clashsoft.cslib.minecraft.network.CSMessageHandler;
import clashsoft.cslib.minecraft.network.CSPacket;
import clashsoft.cslib.minecraft.network.CSPacketHandler;
import clashsoft.mods.cshud.components.HUDCurrentObject;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class CSHUDPacketHandler extends CSPacketHandler
{
	public static final byte			REQUEST		= 0x1;
	public static final byte			SEND		= 0x2;
	
	private static final CSHUDPacketHandler	instance	= new CSHUDPacketHandler();
	
	public static CSHUDPacketHandler getInstance()
	{
		return instance;
	}
	
	public CSHUDPacketHandler()
	{
		super("CSHUD|TEData");
	}
	
	public static class TEData extends CSPacket
	{
		public byte				action;
		
		public World			world;
		public int				x;
		public int				y;
		public int				z;
		
		public NBTTagCompound	data;
		
		public TEData(World world, int x, int y, int z)
		{
			this(REQUEST, world, x, y, z, null);
		}
		
		public TEData(byte action, World world, int x, int y, int z)
		{
			this(action, world, x, y, z, null);
		}
		
		public TEData(World world, int x, int y, int z, NBTTagCompound data)
		{
			this(SEND, world, x, y, z, data);
		}
		
		public TEData(byte action, World world, int x, int y, int z, NBTTagCompound data)
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
			this.writeWorld(buf, this.world);
			buf.writeInt(this.x);
			buf.writeInt(this.y);
			buf.writeInt(this.z);
			
			if (this.action == SEND)
			{
				buf.writeNBTTagCompoundToBuffer(this.data);
			}
		}
		
		@Override
		public void read(PacketBuffer buf)
		{
			this.action = buf.readByte();
			this.world = this.readWorld(buf);
			this.x = buf.readInt();
			this.y = buf.readInt();
			this.z = buf.readInt();
			
			if (this.action == SEND)
			{
				this.data = buf.readNBTTagCompoundFromBuffer();
			}
		}
	}
	
	@Override
	public CSCodec createCodec()
	{
		return new CSCodec<TEData>()
		{
			@Override
			public void addDiscriminators()
			{
				this.addDiscriminator(0, TEData.class);
			}
		};
	}
	
	@Override
	public CSMessageHandler createMessageHandler()
	{
		return new CSMessageHandler<TEData>()
		{
			@Override
			public void process(TEData msg)
			{
				if (msg.action == SEND)
				{
					HUDCurrentObject.instance.setTileEntityData(TileEntity.createAndLoadEntity(msg.data));
				}
				else if (msg.action == REQUEST)
				{
					CSHUDPacketHandler.this.sendTEData(msg.world, msg.x, msg.y, msg.z);
				}
			}
		};
	}
	
	public void requestTEData(World world, int x, int y, int z)
	{
		this.sendPacketToServer(new TEData(world, x, y, z));
	}
	
	public void sendTEData(World world, int x, int y, int z)
	{
		NBTTagCompound data = new NBTTagCompound();
		TileEntity te = world.getTileEntity(x, y, z);
		
		if (te != null)
		{
			te.writeToNBT(data);
		}
		this.sendPacketToClient(new TEData(world, x, y, z, data));
	}
}
