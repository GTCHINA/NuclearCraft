package nc.network.multiblock;

import java.util.List;

import io.netty.buffer.ByteBuf;
import nc.multiblock.fission.FissionCluster;
import nc.network.tile.TileUpdatePacket;
import nc.tile.ITileGui;
import nc.tile.internal.fluid.Tank;
import nc.tile.internal.fluid.Tank.TankInfo;
import net.minecraft.util.math.BlockPos;

public class SaltFissionHeaterUpdatePacket extends TileUpdatePacket {
	
	public BlockPos masterPortPos;
	public List<TankInfo> tanksInfo;
	public List<TankInfo> filterTanksInfo;
	public long clusterHeatStored, clusterHeatCapacity;
	public boolean isProcessing;
	public double time;
	
	public SaltFissionHeaterUpdatePacket() {
		super();
	}
	
	public SaltFissionHeaterUpdatePacket(BlockPos pos, BlockPos masterPortPos, List<Tank> tanks, List<Tank> filterTanks, FissionCluster cluster, boolean isProcessing, double time) {
		super(pos);
		this.masterPortPos = masterPortPos;
		tanksInfo = TankInfo.infoList(tanks);
		filterTanksInfo = TankInfo.infoList(filterTanks);
		clusterHeatStored = cluster == null ? -1L : cluster.heatBuffer.getHeatStored();
		clusterHeatCapacity = cluster == null ? -1L : cluster.heatBuffer.getHeatCapacity();
		this.isProcessing = isProcessing;
		this.time = time;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);
		masterPortPos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
		byte numberOfTanks = buf.readByte();
		tanksInfo = TankInfo.readBuf(buf, numberOfTanks);
		byte numberOfFilterTanks = buf.readByte();
		filterTanksInfo = TankInfo.readBuf(buf, numberOfFilterTanks);
		clusterHeatStored = buf.readLong();
		clusterHeatCapacity = buf.readLong();
		isProcessing = buf.readBoolean();
		time = buf.readDouble();
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		buf.writeInt(masterPortPos.getX());
		buf.writeInt(masterPortPos.getY());
		buf.writeInt(masterPortPos.getZ());
		buf.writeByte(tanksInfo.size());
		for (TankInfo info : tanksInfo) {
			info.writeBuf(buf);
		}
		buf.writeByte(filterTanksInfo.size());
		for (TankInfo info : filterTanksInfo) {
			info.writeBuf(buf);
		}
		buf.writeLong(clusterHeatStored);
		buf.writeLong(clusterHeatCapacity);
		buf.writeBoolean(isProcessing);
		buf.writeDouble(time);
	}
	
	public static class Handler extends TileUpdatePacket.Handler<SaltFissionHeaterUpdatePacket, ITileGui<SaltFissionHeaterUpdatePacket>> {
		
		@Override
		protected void onTileUpdatePacket(SaltFissionHeaterUpdatePacket message, ITileGui<SaltFissionHeaterUpdatePacket> processor) {
			processor.onTileUpdatePacket(message);
		}
	}
}
