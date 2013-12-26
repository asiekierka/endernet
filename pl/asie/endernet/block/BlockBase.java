package pl.asie.endernet.block;

import pl.asie.endernet.EnderNet;
import powercrystals.minefactoryreloaded.api.rednet.IConnectableRedNet;
import powercrystals.minefactoryreloaded.api.rednet.RedNetConnectionType;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public abstract class BlockBase extends BlockContainer implements IConnectableRedNet {
	protected BlockBase(int id) {
		super(id, Material.iron);
		this.setCreativeTab(CreativeTabs.tabMisc);
		this.setHardness(1.5F);
	}
	
	// DESTROY HANDLERS
	
	public void onBlockDestroyed(World world, int x, int y, int z, int meta) {
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
		if(tileEntity != null) {
			tileEntity.invalidate();
		}
	}

	@Override
	public void onBlockDestroyedByPlayer(World world, int x, int y, int z, int meta) {
		super.onBlockDestroyedByPlayer(world, x, y, z, meta);
		this.onBlockDestroyed(world, x, y, z, meta);
	}

	@Override
	public void onBlockDestroyedByExplosion(World world, int x, int y, int z, Explosion explosion) {
		super.onBlockDestroyedByExplosion(world, x, y, z, explosion);
		this.onBlockDestroyed(world, x, y, z, 0);
	}
	
    @Override
	public void breakBlock(World world, int x, int y, int z, int id, int meta) {
    	this.onBlockDestroyed(world, x, y, z, meta);
    	super.breakBlock(world, x, y, z, id, meta);
	}
	
    // REDSTONE CODE
    
    // Helpers
    private void setRedstone(World world, int x, int y, int z, int value) {
		TileEntity entity = world.getBlockTileEntity(x, y, z);
		if(!(entity instanceof TileEntityBase)) return;
		((TileEntityBase)entity).setRedstoneValue(value);
	}
	
	@Override
    public void onNeighborBlockChange(World world, int x, int y, int z, int side) {
		if(canInputRedstoneFromSide(world, x, y, z, side)) setRedstone(world, x, y, z, this.getWorldRedstoneValue(world, x, y, z));
	}
	
	public boolean canInputRedstoneFromSide(IBlockAccess world, int x, int y, int z, int side) {
		TileEntity entity = world.getBlockTileEntity(x, y, z);
		if(!(entity instanceof TileEntityBase)) return true;
		return ((TileEntityBase)entity).canInputRedstoneFromSide(side);
	}
	
	public boolean canOutputRedstoneToSide(IBlockAccess world, int x, int y, int z, int side) {
		TileEntity entity = world.getBlockTileEntity(x, y, z);
		if(!(entity instanceof TileEntityBase)) return true;
		return ((TileEntityBase)entity).canOutputRedstoneToSide(side);
	}
	
    // Vanilla redstone
	
	@Override
	public boolean canConnectRedstone(IBlockAccess world, int x, int y, int z, int side) {
		return (canInputRedstoneFromSide(world,x,y,z,side) || canOutputRedstoneToSide(world,x,y,z,side));
	}
	
    public int getWorldRedstoneValue(World world, int x, int y, int z) {
    	return (world.isBlockIndirectlyGettingPowered(x, y, z) ? 1 : 0);
    }

	@Override
	public boolean canProvidePower() { return true; }
	
    @Override
	public int isProvidingWeakPower(IBlockAccess access, int x, int y, int z, int side) {
    	if(!canOutputRedstoneToSide(access,x,y,z,side)) return 0;
    	TileEntityEnder entity = (TileEntityEnder)access.getBlockTileEntity(x, y, z);
        return entity.getRedstoneValue() > 0 ? 15 : 0;
    }

    // MineFactory Reloaded
    
	@Override
	/**
	 * Override this function if you receive or transmit in the TileEntity.
	 */
	public RedNetConnectionType getConnectionType(World world, int x, int y,
			int z, ForgeDirection side) {
		return canConnectRedstone(world, x, y, z, side.ordinal()) ? RedNetConnectionType.CableSingle : RedNetConnectionType.None;
	}

	@Override
	public int[] getOutputValues(World world, int x, int y, int z,
			ForgeDirection side) {
		return null;
	}

	@Override
	public int getOutputValue(World world, int x, int y, int z,
			ForgeDirection side, int subnet) {
		if(!canOutputRedstoneToSide(world, x, y, z, side.ordinal())) return 0;
    	TileEntityEnder entity = (TileEntityEnder)world.getBlockTileEntity(x, y, z);
        return entity.getRedstoneValue();
	}

	@Override
	public void onInputsChanged(World world, int x, int y, int z,
			ForgeDirection side, int[] inputValues) {
	}	
	
	@Override
	public void onInputChanged(World world, int x, int y, int z,
			ForgeDirection side, int inputValue) {
		if(canInputRedstoneFromSide(world, x, y, z, side.ordinal())) setRedstone(world, x, y, z, inputValue);
	}
}
