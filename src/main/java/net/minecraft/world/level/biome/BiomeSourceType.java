package net.minecraft.world.level.biome;

import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.world.level.storage.LevelData;

public class BiomeSourceType<C extends BiomeSourceSettings, T extends BiomeSource> {
    public static final BiomeSourceType<CheckerboardBiomeSourceSettings, CheckerboardColumnBiomeSource> CHECKERBOARD = register(
        "checkerboard", CheckerboardColumnBiomeSource::new, CheckerboardBiomeSourceSettings::new
    );
    public static final BiomeSourceType<FixedBiomeSourceSettings, FixedBiomeSource> FIXED = register(
        "fixed", FixedBiomeSource::new, FixedBiomeSourceSettings::new
    );
    public static final BiomeSourceType<OverworldBiomeSourceSettings, OverworldBiomeSource> VANILLA_LAYERED = register(
        "vanilla_layered", OverworldBiomeSource::new, OverworldBiomeSourceSettings::new
    );
    public static final BiomeSourceType<TheEndBiomeSourceSettings, TheEndBiomeSource> THE_END = register(
        "the_end", TheEndBiomeSource::new, TheEndBiomeSourceSettings::new
    );
    private final Function<C, T> factory;
    private final Function<LevelData, C> settingsFactory;

    private static <C extends BiomeSourceSettings, T extends BiomeSource> BiomeSourceType<C, T> register(
        String param0, Function<C, T> param1, Function<LevelData, C> param2
    ) {
        return Registry.register(Registry.BIOME_SOURCE_TYPE, param0, new BiomeSourceType<>(param1, param2));
    }

    public BiomeSourceType(Function<C, T> param0, Function<LevelData, C> param1) {
        this.factory = param0;
        this.settingsFactory = param1;
    }

    public T create(C param0) {
        return this.factory.apply(param0);
    }

    public C createSettings(LevelData param0) {
        return this.settingsFactory.apply(param0);
    }
}
