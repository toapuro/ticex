package moffy.ticex.lib.registry;

import moffy.ticex.TicEX;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import slimeknights.mantle.registration.deferred.DeferredRegisterWrapper;
import slimeknights.tconstruct.TConstruct;

public class TicEXRecipeTypeDeferredRegister extends DeferredRegisterWrapper<RecipeType<?>> {

    public TicEXRecipeTypeDeferredRegister(ResourceKey<Registry<RecipeType<?>>> reg) {
        super(reg, TicEX.MODID);
    }

    public TicEXRecipeTypeDeferredRegister(DeferredRegister<RecipeType<?>> register) {
        super(register, TicEX.MODID);
    }

    public static TicEXRecipeTypeDeferredRegister create(ResourceKey<Registry<RecipeType<?>>> reg) {
        return new TicEXRecipeTypeDeferredRegister(reg);
    }

    public static TicEXRecipeTypeDeferredRegister create(DeferredRegister<RecipeType<?>> register) {
        return new TicEXRecipeTypeDeferredRegister(register);
    }

    public <T extends Recipe<?>> RegistryObject<RecipeType<T>> register(String name) {
        return register.register(name, () -> new RecipeType<>() {
            @Override
            public String toString() {
                return TConstruct.MOD_ID + ":" + name;
            }
        });
    }
}
