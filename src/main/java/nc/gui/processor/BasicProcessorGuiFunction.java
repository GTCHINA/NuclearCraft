package nc.gui.processor;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;

@FunctionalInterface
public interface BasicProcessorGuiFunction<TILE extends TileEntity> {
	
	GuiContainer apply(Container inventory, EntityPlayer player, TILE tile, String textureLocation);
}
