package moffy.ticex.datagen.fluid;

import moffy.ticex.TicEX;
import moffy.ticex.block.transmuter.container.FluidTransmuterContainerMenu;
import net.minecraft.data.PackOutput;
import org.jetbrains.annotations.NotNull;
import slimeknights.mantle.fluid.tooltip.AbstractFluidTooltipProvider;

public class TicEXFluidToolTipProvider extends AbstractFluidTooltipProvider {
    public TicEXFluidToolTipProvider(PackOutput output) {
        super(output, TicEX.MODID);
    }

    @Override
    protected void addFluids() {
        addRedirect(FluidTransmuterContainerMenu.TOOLTIP_FORMAT, id("ingots"));
    }

    @Override
    public @NotNull String getName() {
        return "TicEX Fluid Tooltip Provider";
    }
}
