package net.minecraft.world.level.material;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class Fluids {
    public static final Fluid EMPTY = register("empty", new EmptyFluid());
    public static final FlowingFluid FLOWING_WATER = register("flowing_water", new WaterFluid.Flowing());
    public static final FlowingFluid WATER = register("water", new WaterFluid.Source());
    public static final FlowingFluid FLOWING_LAVA = register("flowing_lava", new LavaFluid.Flowing());
    public static final FlowingFluid LAVA = register("lava", new LavaFluid.Source());

    private static <T extends Fluid> T register(String param0, T param1) {
        return Registry.register(BuiltInRegistries.FLUID, param0, param1);
    }

    static {
        for(Fluid var0 : BuiltInRegistries.FLUID) {
            for(FluidState var1 : var0.getStateDefinition().getPossibleStates()) {
                Fluid.FLUID_STATE_REGISTRY.add(var1);
            }
        }

    }
}
