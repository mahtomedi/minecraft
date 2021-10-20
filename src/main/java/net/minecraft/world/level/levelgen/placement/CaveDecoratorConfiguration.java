package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class CaveDecoratorConfiguration implements DecoratorConfiguration {
    public static final Codec<CaveDecoratorConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    CaveSurface.CODEC.fieldOf("surface").forGetter(param0x -> param0x.surface),
                    Codec.INT.fieldOf("floor_to_ceiling_search_range").forGetter(param0x -> param0x.floorToCeilingSearchRange),
                    Codec.BOOL.fieldOf("allow_water").forGetter(param0x -> param0x.allowWater)
                )
                .apply(param0, CaveDecoratorConfiguration::new)
    );
    public final CaveSurface surface;
    public final int floorToCeilingSearchRange;
    public final boolean allowWater;

    public CaveDecoratorConfiguration(CaveSurface param0, int param1, boolean param2) {
        this.surface = param0;
        this.floorToCeilingSearchRange = param1;
        this.allowWater = param2;
    }
}
