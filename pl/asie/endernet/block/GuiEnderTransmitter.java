package pl.asie.endernet.block;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class GuiEnderTransmitter extends GuiContainer {
    private final ResourceLocation texture = new ResourceLocation("endernet", "textures/gui/ender_transmitter.png");
    private final TileEntityEnderTransmitter transmitter;
    private boolean canReceive = true;
    private int progress = 0;
    private int maxProgress = 100;
    
	public GuiEnderTransmitter(Container par1Container, TileEntityEnderTransmitter transmitter) {
		super(par1Container);
		this.transmitter = transmitter;
		this.xSize = 176;
		this.ySize = 166;
	}

	@Override
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
    {
		progress = transmitter.getProgress();
		maxProgress = transmitter.getMaxProgress();
		
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(texture);
        int xo = (this.width - this.xSize) / 2;
        int yo = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(xo, yo, 0, 0, this.xSize, this.ySize);
        if(!canReceive) this.drawTexturedModalRect(xo+66, yo+33, 0, 172, 18, 18);
        int progressWidth = Math.min(44, progress * 44 / transmitter.getMaxProgress());
        this.drawTexturedModalRect(xo+66, yo+53, 44-progressWidth, 167, progressWidth, 4);
    }

	public void syncNBTFromClient(NBTTagCompound data) {
		canReceive = data.getBoolean("r");
		//progress = data.getShort("p");
		//maxProgress = data.getShort("m");
	}

}
