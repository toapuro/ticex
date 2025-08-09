package moffy.ticex.block.transmuter.pattern;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.tags.ITag;
import net.minecraftforge.registries.tags.ITagManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class FluidTransmuterPattern {
    private final Map<Fluid, Fluid> pairCache = new HashMap<>();
    private final String tagPrefix;

    public FluidTransmuterPattern(String tagPrefix) {
        this.tagPrefix = tagPrefix;
    }

    public ITagManager<Fluid> getTagManager() {
        IForgeRegistry<Fluid> registry = ForgeRegistries.FLUIDS;
        ITagManager<Fluid> manager = registry.tags();
        if (manager == null) {
            throw new IllegalStateException("Expected " + registry.getRegistryName() + " to have tags.");
        }
        return manager;
    }

    public boolean isValidTag(TagKey<Fluid> tag, Predicate<TagKey<Fluid>> tagValidator) {
        if (!tagValidator.test(tag)) {
            return false;
        }

        if (tag != null && getTagManager().isKnownTagName(tag)) {
            ResourceLocation location = tag.location();
            return location.toString().startsWith(tagPrefix);
        }

        return false;
    }

    public Fluid resolveOutput(Fluid input, Predicate<TagKey<Fluid>> tagValidator) {
        if (pairCache.containsKey(input)) {
            return pairCache.get(input);
        }

        Optional<Holder<Fluid>> holder = ForgeRegistries.FLUIDS.getHolder(input);
        if (holder.isEmpty()) {
            return Fluids.EMPTY;
        }

        Holder<Fluid> fluidHolder = holder.get();
        fluidHolder.tags().forEach(fluidTagKey -> {
            if (!isValidTag(fluidTagKey, tagValidator)) {
                return;
            }

            ITag<Fluid> tagContents = getTagManager().getTag(fluidTagKey);

            Optional<Fluid> matchFluid = tagContents.stream()
                    .filter(fluid -> !fluid.isSame(input))
                    .filter(Predicate.not(pairCache::containsValue)) // no duplicate
                    .findFirst();

            matchFluid.ifPresent(fluid -> {
                pairCache.put(input, fluid);
            });
        });

        if (!pairCache.containsKey(input)) {
            return Fluids.EMPTY;
        }
        return pairCache.get(input);
    }

    public String getTagPrefix() {
        return tagPrefix;
    }
}
