package mekanism.client.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mekanism.api.Coord4D;
import mekanism.api.util.ListUtils;
import mekanism.client.gui.element.GuiElement.IInfoHandler;
import mekanism.client.gui.element.*;
import mekanism.client.gui.element.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.ContainerMetallurgicInfuser;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.TileEntityMetallurgicInfuser;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.entity.player.InventoryPlayer;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiMetallurgicInfuser extends GuiMekanism
{
	public TileEntityMetallurgicInfuser tileEntity;

	public GuiMetallurgicInfuser(InventoryPlayer inventory, TileEntityMetallurgicInfuser tentity)
	{
		super(tentity, new ContainerMetallurgicInfuser(inventory, tentity));
		tileEntity = tentity;

		guiElements.add(new GuiRedstoneControl(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiMetallurgicInfuser.png")));
		guiElements.add(new GuiUpgradeTab(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiMetallurgicInfuser.png")));
		guiElements.add(new GuiSecurityTab(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiMetallurgicInfuser.png")));
		guiElements.add(new GuiSideConfigurationTab(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiMetallurgicInfuser.png")));
		guiElements.add(new GuiTransporterConfigTab(this, 34, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiMetallurgicInfuser.png")));
		guiElements.add(new GuiPowerBar(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiMetallurgicInfuser.png"), 164, 15));
		guiElements.add(new GuiEnergyInfo(new IInfoHandler() {
			@Override
			public List<String> getInfo()
			{
				String multiplier = MekanismUtils.getEnergyDisplay(tileEntity.energyPerTick);
				return ListUtils.asList(LangUtils.localize("gui.using") + ": " + multiplier + "/t", LangUtils.localize("gui.needed") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.getMaxEnergy()-tileEntity.getEnergy()));
			}
		}, this, MekanismUtils.getResource(ResourceType.GUI, "GuiMetallurgicInfuser.png")));

		guiElements.add(new GuiSlot(SlotType.EXTRA, this, MekanismUtils.getResource(ResourceType.GUI, "GuiMetallurgicInfuser.png"), 16, 34));
		guiElements.add(new GuiSlot(SlotType.INPUT, this, MekanismUtils.getResource(ResourceType.GUI, "GuiMetallurgicInfuser.png"), 50, 42));
		guiElements.add(new GuiSlot(SlotType.POWER, this, MekanismUtils.getResource(ResourceType.GUI, "GuiMetallurgicInfuser.png"), 142, 34).with(SlotOverlay.POWER));
		guiElements.add(new GuiSlot(SlotType.OUTPUT, this, MekanismUtils.getResource(ResourceType.GUI, "GuiMetallurgicInfuser.png"), 108, 42));

		guiElements.add(new GuiProgress(new IProgressInfoHandler()
		{
			@Override
			public double getProgress()
			{
				return tileEntity.getScaledProgress();
			}
		}, ProgressBar.MEDIUM, this, MekanismUtils.getResource(ResourceType.GUI, "GuiMetallurgicInfuser.png"), 70, 46));
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		fontRendererObj.drawString(tileEntity.getInventoryName(), 45, 6, 0x404040);
		fontRendererObj.drawString(LangUtils.localize("container.inventory"), 8, (ySize - 96) + 2, 0x404040);

		if(xAxis >= 7 && xAxis <= 11 && yAxis >= 17 && yAxis <= 69)
		{
			drawCreativeTabHoveringText(tileEntity.infuseStored.type != null ? tileEntity.infuseStored.type.getLocalizedName() + ": " + tileEntity.infuseStored.amount : LangUtils.localize("gui.empty"), xAxis, yAxis);
		}

		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiMetallurgicInfuser.png"));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;
		drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);

		if(tileEntity.infuseStored.type != null)
		{
			mc.renderEngine.bindTexture(MekanismRenderer.getBlocksTexture());
			int displayInt = tileEntity.getScaledInfuseLevel(52);
			drawTexturedRectFromIcon(guiWidth + 7, guiHeight + 17 + 52 - displayInt, tileEntity.infuseStored.type.icon, 4, displayInt);
		}

		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
	}

	@Override
	protected void mouseClicked(int x, int y, int button)
	{
		super.mouseClicked(x, y, button);

		if(button == 0)
		{
			int xAxis = (x - (width - xSize) / 2);
			int yAxis = (y - (height - ySize) / 2);

			if(xAxis > 144 && xAxis < 168 && yAxis > 72 && yAxis < 83)
			{
				ArrayList data = new ArrayList();
				data.add(0);

				Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));
				SoundHandler.playSound("gui.button.press");
			}
		}
	}
}
