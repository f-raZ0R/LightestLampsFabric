package tk.dczippl.lightestlamp.blocks;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import tk.dczippl.lightestlamp.blocks.entity.ChunkCleanerBlockEntity;

public class ChunkCleanerBlock extends BlockWithEntity
{
    public ChunkCleanerBlock()
    {
        super(AbstractBlock.Settings.copy(Blocks.STRUCTURE_BLOCK));
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ChunkCleanerBlockEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }
}