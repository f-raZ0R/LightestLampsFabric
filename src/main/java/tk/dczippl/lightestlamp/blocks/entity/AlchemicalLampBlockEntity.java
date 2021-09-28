package tk.dczippl.lightestlamp.blocks.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import tk.dczippl.lightestlamp.init.ModBlockEntities;
import tk.dczippl.lightestlamp.util.BlockUtil;

public class AlchemicalLampBlockEntity extends BlockEntity implements BlockEntityTicker
{
    public AlchemicalLampBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.ALCHEMICALLAMP_TE,pos,state);
    }

    @Override
    public void tick(World world, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        BlockUtil.repelEntitiesInBoxFromPoint(world, new Box(pos.add(-8, -8, -8), pos.add(8, 8, 8)), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, false);
    }
}