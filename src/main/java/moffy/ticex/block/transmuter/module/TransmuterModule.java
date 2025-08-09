package moffy.ticex.block.transmuter.module;

import moffy.ticex.block.transmuter.pattern.FluidTransmuterExcludePattern;
import moffy.ticex.block.transmuter.pattern.FluidTransmuterPair;
import moffy.ticex.block.transmuter.pattern.FluidTransmuterPattern;
import moffy.ticex.block.transmuter.tank.ITransmuterTank;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import slimeknights.mantle.block.entity.MantleBlockEntity;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TransmuterModule {
    private final MantleBlockEntity parent;
    private final Map<Fluid, FluidTransmuterPair> pairCache;
    private final ITransmuterTank transmuterTank;
    private final List<FluidTransmuterPattern> patterns;
    private final List<FluidTransmuterExcludePattern> excludePatterns;
    private final int maxRate;

    public TransmuterModule(MantleBlockEntity parent, ITransmuterTank transmuterTank, int maxRate, List<FluidTransmuterPattern> patterns, List<FluidTransmuterExcludePattern> excludePatterns) {
        this.pairCache = new HashMap<>();

        this.parent = parent;
        this.transmuterTank = transmuterTank;
        this.maxRate = maxRate;
        this.patterns = patterns;
        this.excludePatterns = excludePatterns;
    }

    private boolean validateTag(TagKey<Fluid> tagKey) {
        return this.excludePatterns.stream().noneMatch(pattern -> pattern.isInvalidTag(tagKey));
    }

    @Nullable
    private FluidTransmuterPair findMatchPair() {
        for (int tank = 0; tank < this.transmuterTank.getInputTanks(); tank++) {
            IFluidHandler fluidHandler = transmuterTank.getFluidHandler(tank);
            if (fluidHandler == null) continue;

            for (int i = 0; i < fluidHandler.getTanks(); i++) {
                FluidStack fluidInTank = fluidHandler.getFluidInTank(i);
                Fluid inputFluid = fluidInTank.getFluid();
                if (fluidInTank.isEmpty()) {
                    continue;
                }

                if (this.pairCache.containsKey(inputFluid)) {
                    return this.pairCache.get(inputFluid);
                }

                Optional<Fluid> matchPattern = patterns.stream()
                        .map(pattern -> pattern.resolveOutput(inputFluid, this::validateTag))
                        .filter(output -> output != Fluids.EMPTY)
                        .findAny();

                if (matchPattern.isPresent()) {
                    Fluid outputFluid = matchPattern.get();
                    FluidTransmuterPair pair = new FluidTransmuterPair(inputFluid, outputFluid);
                    this.pairCache.put(inputFluid, pair);
                    return pair;
                }
            }
        }

        return null;
    }

    public boolean canPerform() {
        return this.findMatchPair() != null;
    }

    public void processPattern() {
        FluidTransmuterPair pair = this.findMatchPair();
        if (pair != null) {
            performPattern(pair);
        }
    }

    private void performPattern(FluidTransmuterPair pair) {
        for (int tank = 0; tank < this.transmuterTank.getInputTanks(); tank++) {
            IFluidHandler fluidHandler = transmuterTank.getFluidHandler(tank);
            if (fluidHandler == null) continue;

            for (int i = 0; i < fluidHandler.getTanks(); i++) {
                FluidStack fluidInTank = fluidHandler.getFluidInTank(i);
                if (fluidInTank.isEmpty()) {
                    continue;
                }

                Fluid inputFluid = fluidInTank.getFluid();
                if (!pair.inputFluid().isSame(inputFluid)) {
                    continue;
                }

                Fluid outputFluid = pair.outputFluid();


                FluidStack willDrain = fluidHandler.drain(new FluidStack(inputFluid, maxRate), IFluidHandler.FluidAction.SIMULATE);
                if (!willDrain.isEmpty()) {
                    FluidStack outputStack = new FluidStack(outputFluid, willDrain.getAmount());
                    if (!transmuterTank.canFit(outputStack.copy())) continue;

                    fluidHandler.drain(willDrain, IFluidHandler.FluidAction.EXECUTE);
                    this.transmuterTank.fill(outputStack.copy());
                    return;
                }
            }
        }
    }

    public int getMaxRate() {
        return maxRate;
    }
}
