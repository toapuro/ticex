package moffy.ticex.block.transmuter.container;

import moffy.ticex.block.transmuter.entity.FluidTransmuterBlockEntity;
import moffy.ticex.block.transmuter.tank.TransmuterFluidTank;
import moffy.ticex.modules.general.TicEXRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.fluid.FluidTransferHelper;
import slimeknights.mantle.fluid.transfer.IFluidContainerTransfer;
import slimeknights.mantle.inventory.SmartItemHandlerSlot;
import slimeknights.mantle.util.sync.ValidZeroDataSlot;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.shared.inventory.TriggeringBaseContainerMenu;

public class FluidTransmuterContainerMenu extends TriggeringBaseContainerMenu<FluidTransmuterBlockEntity> {
    public static final ResourceLocation TOOLTIP_FORMAT = TConstruct.getResource("fluid_transmuter");
    private boolean hasFuelSlot;

    public FluidTransmuterContainerMenu(int id, @Nullable Inventory inv, @Nullable FluidTransmuterBlockEntity transmuter) {
        super(TicEXRegistry.FLUID_TRANSMUTER_MENU_TYPE.get(), id, inv, transmuter);

        this.hasFuelSlot = false;
        if (transmuter != null) {
            Level world = transmuter.getLevel();
            if (world != null && world.isClientSide) {
                TransmuterFluidTank transmuterTank = transmuter.getTransmuterTank();

                for (Direction direction : Direction.values()) {
                    if (direction != Direction.DOWN) {
                        transmuterTank.refreshDirection(direction);
                    }
                }
            }

            BlockPos down = transmuter.getBlockPos().below();
            if (world != null && world.getBlockState(down).is(TinkerTags.Blocks.FUEL_TANKS)) {
                BlockEntity te = world.getBlockEntity(down);
                if (te != null) {
                    this.hasFuelSlot = te.getCapability(ForgeCapabilities.ITEM_HANDLER).filter((handler) -> {
                        this.addSlot(new SmartItemHandlerSlot(handler, 0, 151, 32));
                        return true;
                    }).isPresent();
                }
            }

            this.addInventorySlots();
            ValidZeroDataSlot.trackIntArray(this::addDataSlot, transmuter.getFuelModule());
        }
    }

    public FluidTransmuterContainerMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, getTileEntityFromBuf(buf, FluidTransmuterBlockEntity.class));
    }

    @Override
    public boolean clickMenuButton(@NotNull Player player, int id) {
        ItemStack held = this.getCarried();
        if (id >= 0 && !held.isEmpty() && !player.isSpectator()) {
            if (!player.level().isClientSide && this.tile != null) {
                int index = id / 2;
                IFluidHandler handler;
                if (index == 0) {
                    handler = this.tile.getTank();
                } else if (index == 1) {
                    handler = this.tile.getFuelModule().getTank();
                } else {
                    handler = this.tile.getTransmuterTank().getFluidHandler(index - 2);
                }

                if (handler != EmptyFluidHandler.INSTANCE && handler != null) {
                    IFluidContainerTransfer.TransferResult result = FluidTransferHelper.interactWithStack(handler, held, (id & 1) == 0 ? IFluidContainerTransfer.TransferDirection.FILL_ITEM : IFluidContainerTransfer.TransferDirection.EMPTY_ITEM);
                    this.setCarried(FluidTransferHelper.handleUIResult(player, held, result));
                }
            }

            return true;
        } else {
            return false;
        }
    }

    public boolean isHasFuelSlot() {
        return this.hasFuelSlot;
    }
}
