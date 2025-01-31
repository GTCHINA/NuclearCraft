package nc.gui.processor;

import java.io.IOException;
import java.util.List;

import nc.gui.NCGui;
import nc.gui.element.*;
import nc.network.PacketHandler;
import nc.network.gui.*;
import nc.tile.internal.fluid.Tank;
import nc.tile.processor.IProcessor;
import nc.tile.processor.info.*;
import nc.util.*;
import nc.util.Lazy.LazyInt;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.energy.*;
import net.minecraftforge.fml.common.FMLCommonHandler;

public abstract class GuiProcessor<TILE extends TileEntity & IProcessor<TILE, INFO>, INFO extends ProcessorContainerInfo<TILE, INFO>> extends NCGui {
	
	protected final EntityPlayer player;
	
	protected final TILE tile;
	protected final INFO info;
	
	protected final ResourceLocation guiTextures;
	
	protected final Lazy<String> guiName;
	protected final LazyInt nameWidth;
	
	public GuiProcessor(Container inventory, EntityPlayer player, TILE tile, String textureLocation) {
		super(inventory);
		this.player = player;
		
		this.tile = tile;
		info = tile.getContainerInfo();
		
		guiTextures = new ResourceLocation(textureLocation);
		
		guiName = new Lazy<>(() -> tile.getDisplayName().getUnformattedText());
		nameWidth = new LazyInt(() -> fontRenderer.getStringWidth(guiName.get()));
		
		xSize = info.guiWidth;
		ySize = info.guiHeight;
	}
	
	@Override
	public void initGui() {
		super.initGui();
		initButtons();
	}
	
	protected void initButtons() {
		for (int i = 0; i < info.fluidInputSize; ++i) {
			int[] tankXYWH = info.fluidInputGuiXYWH.get(i);
			buttonList.add(new NCButton.EmptyTank(i, guiLeft + tankXYWH[0], guiTop + tankXYWH[1], tankXYWH[2], tankXYWH[3]));
		}
		
		for (int i = 0; i < info.fluidOutputSize; ++i) {
			int[] tankXYWH = info.fluidOutputGuiXYWH.get(i);
			buttonList.add(new NCButton.EmptyTank(i + info.fluidInputSize, guiLeft + tankXYWH[0], guiTop + tankXYWH[1], tankXYWH[2], tankXYWH[3]));
		}
		
		buttonList.add(new NCButton.MachineConfig(info.getMachineConfigButtonID(), guiLeft + info.machineConfigGuiX, guiTop + info.machineConfigGuiY));
		buttonList.add(new NCToggleButton.RedstoneControl(info.getRedstoneControlButtonID(), guiLeft + info.redstoneControlGuiX, guiTop + info.redstoneControlGuiY, tile));
	}
	
	protected void initSorptionButtons() {
		for (int i = 0; i < info.itemInputSize; ++i) {
			addSorptionButton(NCButton.SorptionConfig.ItemInput::new, info.itemInputSorptionButtonID[i], info.itemInputGuiXYWH.get(i));
		}
		
		for (int i = 0; i < info.fluidInputSize; ++i) {
			addSorptionButton(NCButton.SorptionConfig.FluidInput::new, info.fluidInputSorptionButtonID[i], info.fluidInputGuiXYWH.get(i));
		}
		
		for (int i = 0; i < info.itemOutputSize; ++i) {
			addSorptionButton(NCButton.SorptionConfig.ItemOutput::new, info.itemOutputSorptionButtonID[i], info.itemOutputGuiXYWH.get(i));
		}
		
		for (int i = 0; i < info.fluidOutputSize; ++i) {
			addSorptionButton(NCButton.SorptionConfig.FluidOutput::new, info.fluidOutputSorptionButtonID[i], info.fluidOutputGuiXYWH.get(i));
		}
	}
	
	protected void addSorptionButton(SorptionButtonFunction function, int id, int[] slotXYWH) {
		buttonList.add(function.apply(id, guiLeft + slotXYWH[0] - 1, guiTop + slotXYWH[1] - 1, slotXYWH[2] + 2, slotXYWH[3] + 2));
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		fontRenderer.drawString(guiName.get(), xSize / 2 - nameWidth.get() / 2, 6, 4210752);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1F, 1F, 1F, 1F);
		mc.getTextureManager().bindTexture(guiTextures);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		
		List<Tank> tanks = tile.getTanks();
		for (int i = 0; i < info.fluidInputSize; ++i) {
			int[] tankXYWH = info.fluidInputGuiXYWH.get(i);
			GuiFluidRenderer.renderGuiTank(tanks.get(i), guiLeft + tankXYWH[0], guiTop + tankXYWH[1], zLevel, tankXYWH[2], tankXYWH[3]);
		}
		
		for (int i = 0; i < info.fluidOutputSize; ++i) {
			int[] tankXYWH = info.fluidOutputGuiXYWH.get(i);
			GuiFluidRenderer.renderGuiTank(tanks.get(i + info.fluidInputSize), guiLeft + tankXYWH[0], guiTop + tankXYWH[1], zLevel, tankXYWH[2], tankXYWH[3]);
		}
		
		drawProgressBar();
		
		if (tile.hasCapability(CapabilityEnergy.ENERGY, null)) {
			drawEnergyBar();
		}
	}
	
	protected void drawProgressBar() {
		drawTexturedModalRect(guiLeft + info.progressBarGuiX, guiTop + info.progressBarGuiY, info.progressBarGuiU, info.progressBarGuiV, getProgressBarWidth(), info.progressBarGuiH);
	}
	
	protected int getProgressBarWidth() {
		double baseProcessTime = tile.getBaseProcessTime();
		if (baseProcessTime / tile.getSpeedMultiplier() < 4D) {
			return tile.getIsProcessing() ? info.progressBarGuiW : 0;
		}
		else {
			return baseProcessTime == 0D ? 0 : (int) Math.round(tile.getCurrentTime() * info.progressBarGuiW / baseProcessTime);
		}
	}
	
	protected void drawEnergyBar() {
		int energyX = guiLeft + info.energyBarGuiX, energyY = guiTop + info.energyBarGuiY;
		if (info.defaultProcessPower != 0) {
			int e = getEnergyBarHeight(), d = info.energyBarGuiH - e;
			drawTexturedModalRect(energyX, energyY + d, info.energyBarGuiU, info.energyBarGuiV + d, info.energyBarGuiW, e);
		}
		else {
			drawGradientRect(energyX, energyY, energyX + info.energyBarGuiW, energyY + info.energyBarGuiH, 0xFFC6C6C6, 0xFF8B8B8B);
		}
	}
	
	protected int getEnergyBarHeight() {
		IEnergyStorage energyStorage = tile.getCapability(CapabilityEnergy.ENERGY, null);
		return (int) Math.round((double) info.energyBarGuiH * energyStorage.getEnergyStored() / energyStorage.getMaxEnergyStored());
	}
	
	@Override
	protected void actionPerformed(GuiButton button) {
		if (tile.getWorld().isRemote) {
			if (NCUtil.isModifierKeyDown()) {
				for (int i = 0; i < info.fluidInputSize + info.fluidOutputSize; ++i) {
					if (button.id == i) {
						PacketHandler.instance.sendToServer(new EmptyTankPacket(tile, i));
						return;
					}
				}
			}
			else if (button.id == info.getMachineConfigButtonID()) {
				PacketHandler.instance.sendToServer(new OpenSideConfigGuiPacket(tile));
			}
			else if (button.id == info.getRedstoneControlButtonID()) {
				tile.setRedstoneControl(!tile.getRedstoneControl());
				PacketHandler.instance.sendToServer(new ToggleRedstoneControlPacket(tile));
			}
		}
	}
	
	protected void sorptionButtonActionPerformed(GuiButton button) {
		if (tile.getWorld().isRemote) {
			for (int i = 0; i < info.itemInputSize; ++i) {
				if (button.id == info.itemInputSorptionButtonID[i]) {
					FMLCommonHandler.instance().showGuiScreen(new GuiItemSorptions.Input<>(this, tile, info.itemInputSlots[i]));
					return;
				}
			}
			
			for (int i = 0; i < info.fluidInputSize; ++i) {
				if (button.id == info.fluidInputSorptionButtonID[i]) {
					FMLCommonHandler.instance().showGuiScreen(new GuiFluidSorptions.Input<>(this, tile, info.fluidInputTanks[i]));
					return;
				}
			}
			
			for (int i = 0; i < info.itemOutputSize; ++i) {
				if (button.id == info.itemOutputSorptionButtonID[i]) {
					FMLCommonHandler.instance().showGuiScreen(new GuiItemSorptions.Output<>(this, tile, info.itemOutputSlots[i]));
					return;
				}
			}
			
			for (int i = 0; i < info.fluidOutputSize; ++i) {
				if (button.id == info.fluidOutputSorptionButtonID[i]) {
					FMLCommonHandler.instance().showGuiScreen(new GuiFluidSorptions.Output<>(this, tile, info.fluidOutputTanks[i]));
					return;
				}
			}
		}
	}
	
	@Override
	protected void renderTooltips(int mouseX, int mouseY) {
		renderButtonTooltips(mouseX, mouseY);
	}
	
	protected void renderButtonTooltips(int mouseX, int mouseY) {
		List<Tank> tanks = tile.getTanks();
		for (int i = 0; i < info.fluidInputSize; ++i) {
			int[] tankXYWH = info.fluidInputGuiXYWH.get(i);
			drawFluidTooltip(tanks.get(i), mouseX, mouseY, tankXYWH[0], tankXYWH[1], tankXYWH[2], tankXYWH[3]);
		}
		
		for (int i = 0; i < info.fluidOutputSize; ++i) {
			int[] tankXYWH = info.fluidOutputGuiXYWH.get(i);
			drawFluidTooltip(tanks.get(i + info.fluidInputSize), mouseX, mouseY, tankXYWH[0], tankXYWH[1], tankXYWH[2], tankXYWH[3]);
		}
		
		IEnergyStorage energyStorage = tile.getCapability(CapabilityEnergy.ENERGY, null);
		if (energyStorage != null) {
			drawEnergyTooltip(energyStorage, mouseX, mouseY, info.energyBarGuiX, info.energyBarGuiY, info.energyBarGuiW, info.energyBarGuiH);
		}
		
		drawTooltip(Lang.localize("gui.nc.container.machine_side_config"), mouseX, mouseY, info.machineConfigGuiX, info.machineConfigGuiY, 18, 18);
		drawTooltip(Lang.localize("gui.nc.container.redstone_control"), mouseX, mouseY, info.redstoneControlGuiX, info.redstoneControlGuiY, 18, 18);
	}
	
	protected void renderSorptionButtonTooltips(int mouseX, int mouseY) {
		for (int i = 0; i < info.itemInputSize; ++i) {
			drawSorptionButtonTooltip(mouseX, mouseY, TextFormatting.BLUE, "gui.nc.container.input_item_config", info.itemInputGuiXYWH.get(i));
		}
		
		for (int i = 0; i < info.fluidInputSize; ++i) {
			drawSorptionButtonTooltip(mouseX, mouseY, TextFormatting.DARK_AQUA, "gui.nc.container.input_tank_config", info.fluidInputGuiXYWH.get(i));
		}
		
		for (int i = 0; i < info.itemOutputSize; ++i) {
			drawSorptionButtonTooltip(mouseX, mouseY, TextFormatting.GOLD, "gui.nc.container.output_item_config", info.itemOutputGuiXYWH.get(i));
		}
		
		for (int i = 0; i < info.fluidOutputSize; ++i) {
			drawSorptionButtonTooltip(mouseX, mouseY, TextFormatting.RED, "gui.nc.container.output_tank_config", info.fluidOutputGuiXYWH.get(i));
		}
	}
	
	protected void drawSorptionButtonTooltip(int mouseX, int mouseY, TextFormatting formatting, String unlocalized, int[] slotXYWH) {
		drawTooltip(formatting + Lang.localize(unlocalized), mouseX, mouseY, slotXYWH[0] - 1, slotXYWH[1] - 1, slotXYWH[2] + 2, slotXYWH[3] + 2);
	}
	
	@Override
	protected void drawEnergyTooltip(IEnergyStorage energyStorage, int mouseX, int mouseY, int x, int y, int drawWidth, int drawHeight) {
		if (info.defaultProcessPower != 0) {
			super.drawEnergyTooltip(energyStorage, mouseX, mouseY, x, y, drawWidth, drawHeight);
		}
		else {
			drawNoEnergyTooltip(mouseX, mouseY, x, y, drawWidth, drawHeight);
		}
	}
	
	@Override
	protected List<String> energyInfo(IEnergyStorage energyStorage) {
		List<String> info = super.energyInfo(energyStorage);
		info.add(TextFormatting.LIGHT_PURPLE + Lang.localize("gui.nc.container.process_power") + TextFormatting.WHITE + " " + UnitHelper.prefix(tile.getProcessPower(), 5, "RF/t"));
		return info;
	}
	
	public static class SideConfig<TILE extends TileEntity & IProcessor<TILE, INFO>, INFO extends ProcessorContainerInfo<TILE, INFO>> extends GuiProcessor<TILE, INFO> {
		
		public SideConfig(Container inventory, EntityPlayer player, TILE tile, String textureLocation) {
			super(inventory, player, tile, textureLocation);
		}
		
		@Override
		public void initButtons() {
			initSorptionButtons();
		}
		
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
