package net.minecraft.world.level.chunk;

import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.DebugGeneratorSettings;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.NetherGeneratorSettings;
import net.minecraft.world.level.levelgen.NetherLevelSource;
import net.minecraft.world.level.levelgen.OverworldGeneratorSettings;
import net.minecraft.world.level.levelgen.OverworldLevelSource;
import net.minecraft.world.level.levelgen.TheEndGeneratorSettings;
import net.minecraft.world.level.levelgen.TheEndLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ChunkGeneratorType<C extends ChunkGeneratorSettings, T extends ChunkGenerator<C>> implements ChunkGeneratorFactory<C, T> {
    public static final ChunkGeneratorType<OverworldGeneratorSettings, OverworldLevelSource> SURFACE = register(
        "surface", OverworldLevelSource::new, OverworldGeneratorSettings::new, true
    );
    public static final ChunkGeneratorType<NetherGeneratorSettings, NetherLevelSource> CAVES = register(
        "caves", NetherLevelSource::new, NetherGeneratorSettings::new, true
    );
    public static final ChunkGeneratorType<TheEndGeneratorSettings, TheEndLevelSource> FLOATING_ISLANDS = register(
        "floating_islands", TheEndLevelSource::new, TheEndGeneratorSettings::new, true
    );
    public static final ChunkGeneratorType<DebugGeneratorSettings, DebugLevelSource> DEBUG = register(
        "debug", DebugLevelSource::new, DebugGeneratorSettings::new, false
    );
    public static final ChunkGeneratorType<FlatLevelGeneratorSettings, FlatLevelSource> FLAT = register(
        "flat", FlatLevelSource::new, FlatLevelGeneratorSettings::new, false
    );
    private final ChunkGeneratorFactory<C, T> factory;
    private final boolean isPublic;
    private final Supplier<C> settingsFactory;

    private static <C extends ChunkGeneratorSettings, T extends ChunkGenerator<C>> ChunkGeneratorType<C, T> register(
        String param0, ChunkGeneratorFactory<C, T> param1, Supplier<C> param2, boolean param3
    ) {
        return Registry.register(Registry.CHUNK_GENERATOR_TYPE, param0, new ChunkGeneratorType<>(param1, param3, param2));
    }

    public ChunkGeneratorType(ChunkGeneratorFactory<C, T> param0, boolean param1, Supplier<C> param2) {
        this.factory = param0;
        this.isPublic = param1;
        this.settingsFactory = param2;
    }

    @Override
    public T create(Level param0, BiomeSource param1, C param2) {
        return this.factory.create(param0, param1, param2);
    }

    public C createSettings() {
        return this.settingsFactory.get();
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isPublic() {
        return this.isPublic;
    }
}
