package net.minecraft.client.gui.screens.worldselection;

import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record WorldCreationContext(
    WorldOptions options,
    Registry<LevelStem> datapackDimensions,
    WorldDimensions selectedDimensions,
    LayeredRegistryAccess<RegistryLayer> worldgenRegistries,
    ReloadableServerResources dataPackResources,
    WorldDataConfiguration dataConfiguration
) {
    public WorldCreationContext(
        WorldGenSettings param0, LayeredRegistryAccess<RegistryLayer> param1, ReloadableServerResources param2, WorldDataConfiguration param3
    ) {
        this(param0.options(), param0.dimensions(), param1, param2, param3);
    }

    public WorldCreationContext(
        WorldOptions param0,
        WorldDimensions param1,
        LayeredRegistryAccess<RegistryLayer> param2,
        ReloadableServerResources param3,
        WorldDataConfiguration param4
    ) {
        this(
            param0,
            param2.getLayer(RegistryLayer.DIMENSIONS).registryOrThrow(Registry.LEVEL_STEM_REGISTRY),
            param1,
            param2.replaceFrom(RegistryLayer.DIMENSIONS),
            param3,
            param4
        );
    }

    public WorldCreationContext withSettings(WorldOptions param0, WorldDimensions param1) {
        return new WorldCreationContext(param0, this.datapackDimensions, param1, this.worldgenRegistries, this.dataPackResources, this.dataConfiguration);
    }

    public WorldCreationContext withOptions(WorldCreationContext.OptionsModifier param0) {
        return new WorldCreationContext(
            param0.apply(this.options),
            this.datapackDimensions,
            this.selectedDimensions,
            this.worldgenRegistries,
            this.dataPackResources,
            this.dataConfiguration
        );
    }

    public WorldCreationContext withDimensions(WorldCreationContext.DimensionsUpdater param0) {
        return new WorldCreationContext(
            this.options,
            this.datapackDimensions,
            param0.apply(this.worldgenLoadContext(), this.selectedDimensions),
            this.worldgenRegistries,
            this.dataPackResources,
            this.dataConfiguration
        );
    }

    public RegistryAccess.Frozen worldgenLoadContext() {
        return this.worldgenRegistries.compositeAccess();
    }

    @FunctionalInterface
    @OnlyIn(Dist.CLIENT)
    public interface DimensionsUpdater extends BiFunction<RegistryAccess.Frozen, WorldDimensions, WorldDimensions> {
    }

    @OnlyIn(Dist.CLIENT)
    public interface OptionsModifier extends UnaryOperator<WorldOptions> {
    }
}
