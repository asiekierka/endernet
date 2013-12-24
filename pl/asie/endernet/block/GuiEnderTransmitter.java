package pl.asie.endernet.block;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import org.lwjgl.opengl.GL11;

import pl.asie.endernet.EnderNet;
import cpw.mods.fml.common.network.PacketDispatcher;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.util.ResourceLocation;

public class GuiEnderTransmitter extends GuiContainer {
    private final ResourceLocation texture = new ResourceLocation("endernet", "textures/gui/ender_transmitter.png");
    private final TileEntityEnderTransmitter transmitter;
    private boolean canReceive = true;
    private GuiTextField address;
    private String oldAddress = "";

	public GuiEnderTransmitter(Container par1Container, TileEntityEnderTransmitter transmitter) {
		super(par1Container);
		this.transmitter = transmitter;
		this.xSize = 176;
		this.ySize = 166;
	}
	
	@Override
	public void initGui() {
		super.initGui();
        int xo = (this.width - this.xSize) / 2;
        int yo = (this.height - this.ySize) / 2;
		this.address = new GuiTextField(this.fontRenderer, xo+46, yo+13, 84, 16); 
	}
	
	@Override
    public void keyTyped(char par1, int par2) {
		if(!address.textboxKeyTyped(par1, par2))
			super.keyTyped(par1, par2);
		updateText();
	}

	@Override
    public void mouseClicked(int par1, int par2, int par3) {
		super.mouseClicked(par1, par2, par3);
		address.mouseClicked(par1, par2, par3);
		updateText();
    }
	
	@Override
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int xo = (this.width - this.xSize) / 2;
        int yo = (this.height - this.ySize) / 2;
        this.mc.getTextureManager().bindTexture(texture);
        this.drawTexturedModalRect(xo, yo, 0, 0, this.xSize, this.ySize);
        if(!canReceive) this.drawTexturedModalRect(xo+66, yo+33, 0, 172, 18, 18);
        if(!EnderNet.enableEnergy) {
        	this.drawTexturedModalRect(xo+92, yo+33, 14, 14, 26, 18);
        }
        int progressWidth = Math.min(44, transmitter.getProgress() * 44 / transmitter.getMaxProgress());
        this.drawTexturedModalRect(xo+66, yo+53, 44-progressWidth, 167, progressWidth, 4);
        this.address.drawTextBox();
    }
	
	@Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2) {
        this.drawString(this.fontRenderer, "ID " + transmitter.enderNetID, 6, 6, 16777215);
    }

	public void syncNBTFromClient(NBTTagCompound data) {
		canReceive = data.getBoolean("r");
		this.address.setText(data.getString("a"));
	}
	
	private void updateText() {
		if(oldAddress.equals(address.getText())) return;
		oldAddress = address.getText();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream data = new DataOutputStream(baos);
		try {
			data.writeByte((byte)1);
			data.writeInt(transmitter.enderNetID);
			data.writeUTF(this.address.getText());
			PacketDispatcher.sendPacketToServer(new Packet250CustomPayload("EnderNet", baos.toByteArray()));
		} catch(Exception e) { e.printStackTrace(); }
	}

	@Override
    public void onGuiClosed() {
		super.onGuiClosed();
		this.updateText();
	}
}
