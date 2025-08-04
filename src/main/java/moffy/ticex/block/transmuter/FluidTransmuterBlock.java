package moffy.ticex.block.transmuter;

import moffy.ticex.block.transmuter.entity.FluidTransmuterBlockEntity;
import moffy.ticex.modules.general.TicEXRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.util.BlockEntityHelper;
import slimeknights.tconstruct.library.utils.Util;
import slimeknights.tconstruct.smeltery.block.controller.TinyMultiblockControllerBlock;

public class FluidTransmuterBlock extends TinyMultiblockControllerBlock {
    public FluidTransmuterBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return new FluidTransmuterBlockEntity(TicEXRegistry.FLUID_TRANSMUTER_ENTITY.get(), blockPos, blockState);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return pLevel.isClientSide ? null :
                BlockEntityHelper.castTicker(pBlockEntityType, TicEXRegistry.FLUID_TRANSMUTER_ENTITY.get(), FluidTransmuterBlockEntity.SERVER_TICKER);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull Block pNeighborBlock, @NotNull BlockPos pNeighborPos, boolean pMovedByPiston) {
        Direction direction = Util.directionFromOffset(pPos, pNeighborPos);
        if (direction != Direction.DOWN) {
            BlockEntityHelper.get(FluidTransmuterBlockEntity.class, pLevel, pPos).ifPresent((te) -> te.neighborChanged(direction));
        }
    }
}
