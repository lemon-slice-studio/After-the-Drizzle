package roito.afterthedrizzle.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import roito.afterthedrizzle.AfterTheDrizzle;
import roito.afterthedrizzle.common.tileentity.StoveTileEntity;
import roito.afterthedrizzle.common.tileentity.TileEntityTypeRegistry;

import java.util.Random;

public abstract class StoveBlock extends NormalHorizontalBlock implements IStoveBlock
{
    protected int efficiency;
    protected static boolean keepInventory = false;

    public StoveBlock(Properties properties, String name, int efficiency)
    {
        super(properties, name);
        this.setDefaultState(this.getStateContainer().getBaseState().with(HORIZONTAL_FACING, Direction.NORTH));
        this.efficiency = efficiency;
    }

    @Override
    public BlockRenderLayer getRenderLayer()
    {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean isBurning()
    {
        return this.lightValue != 0;
    }

    @Override
    public int getFuelPower()
    {
        return efficiency;
    }

    @Override
    public boolean hasTileEntity(BlockState state)
    {
        return true;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(HORIZONTAL_FACING);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean causesSuffocation(BlockState state, IBlockReader worldIn, BlockPos pos)
    {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isNormalCube(BlockState p_220081_1_, IBlockReader p_220081_2_, BlockPos p_220081_3_)
    {
        return false;
    }

    @Override
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand)
    {
        if (isBurning())
        {
            double d0 = pos.getX() + 0.5D;
            double d1 = pos.getY() + rand.nextDouble() * 6.0D / 16.0D;
            double d2 = pos.getZ() + 0.5D;

            TileEntity te = worldIn.getTileEntity(pos);

            te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.DOWN).ifPresent(inv ->
            {
                int ash = inv.getStackInSlot(0).getCount();
                if (ash < 32)
                {
                    for (int i = 0; i < ash / 4 + 1; i++)
                    {
                        double d4 = rand.nextDouble() * 0.6D - 0.3D;
                        worldIn.addParticle(ParticleTypes.SMOKE, false, d0 + d4, d1 + 1.0D, d2 + d4, 0.0D, 0.1D, 0.0D);
                    }
                }
                else
                {
                    for (int i = 0; i < ash / 5; i++)
                    {
                        double d4 = rand.nextDouble() * 0.6D - 0.3D;
                        worldIn.addParticle(ParticleTypes.LARGE_SMOKE, false, d0 + d4, d1 + 1.0D, d2 + d4, 0.0D, 0.1D, 0.0D);
                    }
                }
                double d4 = rand.nextDouble() * 0.6D - 0.3D;
                worldIn.addParticle(ParticleTypes.FLAME, false, d0 + d4, d1, d2 + d4, 0.0D, 0.06D, 0.0D);
            });
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit)
    {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof StoveTileEntity)
        {
            if (player.isSneaking())
            {
                if (!worldIn.isRemote)
                    NetworkHooks.openGui((ServerPlayerEntity) player, (INamedContainerProvider) te, te.getPos());
                return true;
            }
            else
            {
                if (player.getHeldItem(handIn).getItem().equals(Items.FLINT_AND_STEEL))
                {
                    ((StoveTileEntity) te).setToLit();
                    player.getHeldItem(handIn).damageItem(1, player, onBroken -> onBroken.sendBreakAnimation(handIn));
                    worldIn.playSound(player, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, worldIn.getRandom().nextFloat() * 0.4F + 0.8F);
                    return true;
                }
                else if (ForgeHooks.getBurnTime(player.getHeldItem(handIn)) > 0)
                {
                    te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.UP).ifPresent(fuel ->
                    {
                        player.setHeldItem(handIn, fuel.insertItem(0, player.getHeldItem(handIn), false));
                        te.markDirty();
                    });
                    return true;
                }
                else if (((StoveTileEntity) te).isDoubleClick())
                {
                    dropFuel(worldIn, pos);
                    return true;
                }
                else
                {
                    dropAsh(worldIn, pos);
                    if (!worldIn.isRemote)
                        ((StoveTileEntity) te).singleClickStart();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world)
    {
        return TileEntityTypeRegistry.STOVE_TILE_ENTITY_TYPE.create();
    }

    private void dropAsh(World worldIn, BlockPos pos)
    {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te != null)
        {
            te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.DOWN).ifPresent(ash ->
            {
                for (int i = ash.getSlots() - 1; i >= 0; --i)
                {
                    if (ash.getStackInSlot(i) != ItemStack.EMPTY)
                    {
                        Block.spawnAsEntity(worldIn, pos, ash.getStackInSlot(i));
                        ((IItemHandlerModifiable) ash).setStackInSlot(i, ItemStack.EMPTY);
                    }
                }
            });
        }
    }

    private void dropFuel(World worldIn, BlockPos pos)
    {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te != null)
        {
            te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.UP).ifPresent(fuel ->
            {
                for (int i = fuel.getSlots() - 1; i >= 0; --i)
                {
                    if (fuel.getStackInSlot(i) != ItemStack.EMPTY)
                    {
                        Block.spawnAsEntity(worldIn, pos, fuel.getStackInSlot(i));
                        ((IItemHandlerModifiable) fuel).setStackInSlot(i, ItemStack.EMPTY);
                    }
                }
            });
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving)
    {
        if (state.hasTileEntity() && !(newState.getBlock() == this.getLit() || newState.getBlock() == this.getUnlit()))
        {
            dropFuel(worldIn, pos);
            dropAsh(worldIn, pos);
            worldIn.removeTileEntity(pos);
        }
    }

    public static void setState(boolean active, World worldIn, BlockPos pos, IStoveBlock stove)
    {
        BlockState iblockstate = worldIn.getBlockState(pos);
        TileEntity tileentity = worldIn.getTileEntity(pos);

        if (active)
        {
            worldIn.setBlockState(pos, stove.getLit().getDefaultState().with(HORIZONTAL_FACING, iblockstate.get(HORIZONTAL_FACING)));
        }
        else
        {
            worldIn.setBlockState(pos, stove.getUnlit().getDefaultState().with(HORIZONTAL_FACING, iblockstate.get(HORIZONTAL_FACING)));
        }

        if (tileentity != null)
        {
            tileentity.validate();
            worldIn.setTileEntity(pos, tileentity);
        }
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player)
    {
        return new ItemStack(getUnlit());
    }

    public static Item.Properties getItemProperties()
    {
        return new Item.Properties().group(AfterTheDrizzle.GROUP_CRAFT);
    }
}
