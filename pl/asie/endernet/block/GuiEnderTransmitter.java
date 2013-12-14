package pl.asie.endernet.block;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.GuiTextField;
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
    private GuiTextField address;
    
	public GuiEnderTransmitter(Container par1Container, TileEntityEnderTransmitter transmitter) {
		super(par1Container);
		this.transmitter = transmitter;
		this.xSize = 176;
		this.ySize = 166;
	}
	
	@Override
	public void initGui() {
        int xo = (this.width - this.xSize) / 2;
        int yo = (this.height - this.ySize) / 2;
		this.address = new GuiTextField(this.fontRenderer, xo+46, yo+13, 84, 16); 
	}
	
	@Override
    public void keyTyped(char par1, int par2) {
		super.keyTyped(par1, par2);
		address.textboxKeyTyped(par1, par2);
	}

	@Override
    public void mouseClicked(int par1, int par2, int par3) {
		super.mouseClicked(par1, par2, par3);
		address.mouseClicked(par1, par2, par3);
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
    }
	
	@Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2)
    {
        int xo = (this.width - this.xSize) / 2;
        int yo = (this.height - this.ySize) / 2;
        int progressWidth = Math.min(44, progress * 44 / transmitter.getMaxProgress());
        this.drawTexturedModalRect(xo+66, yo+53, 44-progressWidth, 167, progressWidth, 4);
        this.drawString(this.fontRenderer, "ID " + transmitter.enderNetID, xo + 6, yo + 6, 16777215);
        this.address.drawTextBox();
    }

	public void syncNBTFromClient(NBTTagCompound data) {
		canReceive = data.getBoolean("r");
		this.address.setText(data.getString("a"));
		//progress = data.getShort("p");
		//maxProgress = data.getShort("m");
	}

}
