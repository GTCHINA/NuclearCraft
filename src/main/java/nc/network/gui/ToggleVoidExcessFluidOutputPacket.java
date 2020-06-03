package nc.network.gui;

import io.netty.buffer.ByteBuf;
import nc.tile.fluid.ITileFluid;
import nc.tile.internal.fluid.TankOutputSetting;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.*;
import net.minecraftforge.fml.relauncher.Side;

public class ToggleVoidExcessFluidOutputPacket implements IMessage {
	
	boolean messageValid;
	
	BlockPos pos;
	int voidExcessFluidOutput;
	int tankNumber;
	
	public ToggleVoidExcessFluidOutputPacket() {
		messageValid = false;
	}
	
	public ToggleVoidExcessFluidOutputPacket(ITileFluid machine, int tankNumber) {
		pos = machine.getTilePos();
		voidExcessFluidOutput = machine.getTankOutputSetting(tankNumber).ordinal();
		this.tankNumber = tankNumber;
		messageValid = true;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		try {
			pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
			voidExcessFluidOutput = buf.readInt();
			tankNumber = buf.readInt();
		}
		catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
			return;
		}
		messageValid = true;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		if (!messageValid) {
			return;
		}
		buf.writeInt(pos.getX());
		buf.writeInt(pos.getY());
		buf.writeInt(pos.getZ());
		buf.writeInt(voidExcessFluidOutput);
		buf.writeInt(tankNumber);
	}
	
	public static class Handler implements IMessageHandler<ToggleVoidExcessFluidOutputPacket, IMessage> {
		
		@Override
		public IMessage onMessage(ToggleVoidExcessFluidOutputPacket message, MessageContext ctx) {
			if (!message.messageValid && ctx.side != Side.SERVER) {
				return null;
			}
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> processMessage(message, ctx));
			return null;
		}
		
		void processMessage(ToggleVoidExcessFluidOutputPacket message, MessageContext ctx) {
			TileEntity tile = ctx.getServerHandler().player.getServerWorld().getTileEntity(message.pos);
			if (tile instanceof ITileFluid) {
				ITileFluid machine = (ITileFluid) tile;
				machine.setTankOutputSetting(message.tankNumber, TankOutputSetting.values()[message.voidExcessFluidOutput]);
				tile.markDirty();
			}
		}
	}
}
