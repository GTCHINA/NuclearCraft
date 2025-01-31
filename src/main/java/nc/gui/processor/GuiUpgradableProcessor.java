package nc.gui.processor;

import java.io.IOException;
import java.util.List;

import nc.gui.element.*;
import nc.init.NCItems;
import nc.network.PacketHandler;
import nc.network.gui.OpenTileGuiPacket;
import nc.tile.processor.IProcessor;
import nc.tile.processor.info.UpgradableProcessorContainerInfo;
import nc.util.*;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.common.FMLCommonHandler;

public abstract class GuiUpgradableProcessor<TILE extends TileEntity & IProcessor<TILE, INFO>, INFO extends UpgradableProcessorContainerInfo<TILE, INFO>> extends GuiProcessor<TILE, INFO> {
	
	protected GuiItemRenderer speedUpgradeRenderer = null, energyUpgradeRenderer = null;
	
	public GuiUpgradableProcessor(Container inventory, EntityPlayer player, TILE tile, String textureLocation) {
		super(inventory, player, tile, textureLocation);
	}
	
	@Override
	protected void initSorptionButtons() {
		super.initSorptionButtons();
		addSorptionButton(NCButton.SorptionConfig.SpeedUpgrade::new, info.speedUpgradeSorptionButtonID, info.speedUpgradeGuiXYWH);
		addSorptionButton(NCButton.SorptionConfig.EnergyUpgrade::new, info.energyUpgradeSorptionButtonID, info.energyUpgradeGuiXYWH);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
		drawUpgradeRenderers();
	}
	
	protected void drawUpgradeRenderers() {
		if (speedUpgradeRenderer == null) {
			int[] stackXY = info.speedUpgradeStackXY;
			speedUpgradeRenderer = new GuiItemRenderer(NCItems.upgrade, 0, guiLeft + stackXY[0], guiTop + stackXY[1], 0.5F);
		}
		speedUpgradeRenderer.draw();
		
		if (energyUpgradeRenderer == null) {
			int[] stackXY = info.energyUpgradeStackXY;
			energyUpgradeRenderer = new GuiItemRenderer(NCItems.upgrade, 1, guiLeft + stackXY[0], guiTop + stackXY[1], 0.5F);
		}
		energyUpgradeRenderer.draw();
	}
	
	@Override
	protected void sorptionButtonActionPerformed(GuiButton button) {
		super.sorptionButtonActionPerformed(button);
		
		if (button.id == info.speedUpgradeSorptionButtonID) {
			FMLCommonHandler.instance().showGuiScreen(new GuiItemSorptions.SpeedUpgrade<>(this, tile, info.speedUpgradeSlot));
		}
		else if (button.id == info.energyUpgradeSorptionButtonID) {
			FMLCommonHandler.instance().showGuiScreen(new GuiItemSorptions.EnergyUpgrade<>(this, tile, info.energyUpgradeSlot));
		}
	}
	
	@Override
	protected void renderSorptionButtonTooltips(int mouseX, int mouseY) {
		super.renderSorptionButtonTooltips(mouseX, mouseY);
		
		drawSorptionButtonTooltip(mouseX, mouseY, TextFormatting.DARK_BLUE, "gui.nc.container.upgrade_config", info.speedUpgradeGuiXYWH);
		drawSorptionButtonTooltip(mouseX, mouseY, TextFormatting.YELLOW, "gui.nc.container.upgrade_config", info.energyUpgradeGuiXYWH);
	}
	
	@Override
	protected List<String> energyInfo(IEnergyStorage energyStorage) {
		List<String> info = super.energyInfo(energyStorage);
		info.add(multiplierInfo("gui.nc.container.speed_multiplier", tile.getSpeedMultiplier()));
		info.add(multiplierInfo("gui.nc.container.power_multiplier", tile.getPowerMultiplier()));
		return info;
	}
	
	protected String multiplierInfo(String unloc, double mult) {
		return TextFormatting.AQUA + Lang.localize(unloc) + TextFormatting.WHITE + " x" + NCMath.decimalPlaces(mult, 2);
	}
	
	public static class SideConfig<TILE extends TileEntity & IProcessor<TILE, INFO>, INFO extends UpgradableProcessorContainerInfo<TILE, INFO>> extends GuiUpgradableProcessor<TILE, INFO> {
		
		public SideConfig(Container inventory, EntityPlayer player, TILE tile, String textureLocation) {
			super(inventory, player, tile, textureLocation);
		}
		
		@Override
		public void initButtons() {
			initSorptionButtons();
		}
		
		@Override
		protected void drawUpgradeRenderers() {}
		
		@Override
		protected void actionPerformed(GuiButton button) {
			sorptionButtonActionPerformed(button);
		}
		
		@Override
		public void renderButtonTooltips(int mouseX, int mouseY) {
			renderSorptionButtonTooltips(mouseX, mouseY);
		}
		
		@Override
		protected void keyTyped(char typedChar, int keyCode) throws IOException {
			if (isEscapeKeyDown(keyCode)) {
				PacketHandler.instance.sendToServer(new OpenTileGuiPacket(tile));
			}
			else {
				super.keyTyped(typedChar, keyCode);
			}
		}
	}
}
