package net.minecraft.client.gui.screens.worldselection;

import com.mojang.serialization.Lifecycle;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record WorldCreationContext(
    WorldGenSettings worldGenSettings, Lifecycle worldSettingsStability, RegistryAccess.Frozen registryAccess, ReloadableServerResources dataPackResources
) {
    public WorldCreationContext withSettings(WorldGenSettings param0) {
        return new WorldCreationContext(param0, this.worldSettingsStability, this.registryAccess, this.dataPackResources);
    }

    public WorldCreationContext withSettings(WorldCreationContext.SimpleUpdater param0) {
        WorldGenSettings var0 = param0.apply(this.worldGenSettings);
        return this.withSettings(var0);
    }

    public WorldCreationContext withSettings(WorldCreationContext.Updater param0) {
        WorldGenSettings var0 = param0.apply(this.registryAccess, this.worldGenSettings);
        return this.withSettings(var0);
    }

    @FunctionalInterface
    @OnlyIn(Dist.CLIENT)
    public interface SimpleUpdater extends UnaryOperator<WorldGenSettings> {
    }

    @FunctionalInterface
    @OnlyIn(Dist.CLIENT)
    public interface Updater extends BiFunction<RegistryAccess.Frozen, WorldGenSettings, WorldGenSettings> {
    }
}
