package moffy.ticex.block.transmuter.tank;

import net.minecraftforge.fluids.FluidStack;

public interface ITankMutable {
    FluidStack drain(int tank, FluidStack fluidStack);

    int fill(FluidStack fluidStack);
}
