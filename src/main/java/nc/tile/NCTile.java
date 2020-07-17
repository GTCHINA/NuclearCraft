package nc.tile;

import javax.annotation.Nullable;

import nc.block.tile.IActivatable;
import nc.capability.radiation.source.*;
import nc.util.NCMath;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.*;

public abstract class NCTile extends TileEntity implements ITickable, ITile {
	
	public boolean isAdded = false, isMarkedDirty = false;
	private boolean isRedstonePowered = false, alternateComparator = false, redstoneControl = false;
	
	private final IRadiationSource radiation;
	
	public NCTile() {
		super();
		radiation = new RadiationSource(0D);
	}
	
	@Override
	public void update() {
		if (!isAdded) {
			onAdded();
			isAdded = true;
		}
		if (isMarkedDirty) {
			markDirty();
			isMarkedDirty = false;
		}
	}
	
	public void onAdded() {
		if (world.isRemote) {
			world.markBlockRangeForRenderUpdate(pos, pos);
			world.getChunk(pos).markDirty();
			refreshIsRedstonePowered(world, pos);
			markDirty();
		}
	}
	
	@Override
	public TileEntity thisTile() {
		return this;
	}
	
	@Override
	public World getTileWorld() {
		return world;
	}
	
	@Override
	public BlockPos getTilePos() {
		return pos;
	}
	
	@Override
	public Block getTileBlockType() {
		return getBlockType();
	}
	
	@Override
	public int getTileBlockMeta() {
		return getBlockMetadata();
	}
	
	@Override
	public IRadiationSource getRadiationSource() {
		return radiation;
	}
	
	@Override
	public ITextComponent getDisplayName() {
		return getBlockType() == null ? null : new TextComponentTranslation(getBlockType().getLocalizedName());
	}
	
	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
		return oldState.getBlock() != newState.getBlock();
	}
	
	/** Override this if your block does not implement IActivatable! */
	@Override
	public void setState(boolean isActive, TileEntity tile) {
		if (getBlockType() instanceof IActivatable) {
			((IActivatable) getBlockType()).setState(isActive, tile);
		}
	}
	
	@Override
	public final void markTileDirty() {
		markDirty();
	}
	
	@Override
	public void markDirtyAndNotify() {
		markDirty();
		world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
		world.notifyNeighborsOfStateChange(pos, getBlockType(), true);
	}
	
	// Redstone
	
	@Override
	public boolean getIsRedstonePowered() {
		return isRedstonePowered;
	}
	
	@Override
	public void setIsRedstonePowered(boolean isRedstonePowered) {
		this.isRedstonePowered = isRedstonePowered;
	}
	
	@Override
	public boolean getAlternateComparator() {
		return alternateComparator;
	}
	
	@Override
	public void setAlternateComparator(boolean alternate) {
		alternateComparator = alternate;
	}
	
	@Override
	public boolean getRedstoneControl() {
		return redstoneControl;
	}
	
	@Override
	public void setRedstoneControl(boolean redstoneControl) {
		this.redstoneControl = redstoneControl;
	}
	
	// NBT
	
	public NBTTagCompound writeRadiation(NBTTagCompound nbt) {
		nbt.setDouble("radiationLevel", getRadiationSource().getRadiationLevel());
		return nbt;
	}
	
	public void readRadiation(NBTTagCompound nbt) {
		if (nbt.hasKey("radiationLevel")) {
			getRadiationSource().setRadiationLevel(nbt.getDouble("radiationLevel"));
		}
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		writeAll(nbt);
		return nbt;
	}
	
	public NBTTagCompound writeAll(NBTTagCompound nbt) {
		nbt.setBoolean("isRedstonePowered", isRedstonePowered);
		nbt.setBoolean("alternateComparator", alternateComparator);
		nbt.setBoolean("redstoneControl", redstoneControl);
		if (shouldSaveRadiation()) {
			writeRadiation(nbt);
		}
		return nbt;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		readAll(nbt);
	}
	
	public void readAll(NBTTagCompound nbt) {
		isRedstonePowered = nbt.getBoolean("isRedstonePowered");
		alternateComparator = nbt.getBoolean("alternateComparator");
		redstoneControl = nbt.getBoolean("redstoneControl");
		if (shouldSaveRadiation()) {
			readRadiation(nbt);
		}
	}
	
	/* public NBTTagCompound getTileData() { NBTTagCompound nbt = new NBTTagCompound(); writeToNBT(nbt); return nbt; } */
	
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		NBTTagCompound nbt = new NBTTagCompound();
		writeToNBT(nbt);
		int metadata = getBlockMetadata();
		return new SPacketUpdateTileEntity(pos, metadata, nbt);
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
		readFromNBT(packet.getNbtCompound());
	}
	
	@Override
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound nbt = new NBTTagCompound();
		writeToNBT(nbt);
		return nbt;
	}
	
	@Override
	public void handleUpdateTag(NBTTagCompound tag) {
		super.handleUpdateTag(tag);
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing side) {
		if (capability == IRadiationSource.CAPABILITY_RADIATION_SOURCE) {
			return radiation != null;
		}
		return super.hasCapability(capability, side);
	}
	
	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing side) {
		if (capability == IRadiationSource.CAPABILITY_RADIATION_SOURCE) {
			return (T) radiation;
		}
		return super.getCapability(capability, side);
	}
	
	// For when the raw TE capabilities need to be reached:
	
	protected boolean hasCapabilityDefault(Capability<?> capability, @Nullable EnumFacing side) {
		return super.hasCapability(capability, side);
	}
	
	protected <T> T getCapabilityDefault(Capability<T> capability, @Nullable EnumFacing side) {
		return super.getCapability(capability, side);
	}
	
	// TESR
	
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return NCMath.sq(16D * FMLClientHandler.instance().getClient().gameSettings.renderDistanceChunks);
	}
}
