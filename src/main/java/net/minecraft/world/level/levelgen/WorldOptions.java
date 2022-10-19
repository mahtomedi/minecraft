package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.OptionalLong;
import net.minecraft.util.RandomSource;
import org.apache.commons.lang3.StringUtils;

public class WorldOptions {
    public static final MapCodec<WorldOptions> CODEC = RecordCodecBuilder.mapCodec(
        param0 -> param0.group(
                    Codec.LONG.fieldOf("seed").stable().forGetter(WorldOptions::seed),
                    Codec.BOOL.fieldOf("generate_features").orElse(true).stable().forGetter(WorldOptions::generateStructures),
                    Codec.BOOL.fieldOf("bonus_chest").orElse(false).stable().forGetter(WorldOptions::generateBonusChest),
                    Codec.STRING.optionalFieldOf("legacy_custom_options").stable().forGetter(param0x -> param0x.legacyCustomOptions)
                )
                .apply(param0, param0.stable(WorldOptions::new))
    );
    public static final WorldOptions DEMO_OPTIONS = new WorldOptions((long)"North Carolina".hashCode(), true, true);
    private final long seed;
    private final boolean generateStructures;
    private final boolean generateBonusChest;
    private final Optional<String> legacyCustomOptions;

    public WorldOptions(long param0, boolean param1, boolean param2) {
        this(param0, param1, param2, Optional.empty());
    }

    public static WorldOptions defaultWithRandomSeed() {
        return new WorldOptions(RandomSource.create().nextLong(), true, false);
    }

    private WorldOptions(long param0, boolean param1, boolean param2, Optional<String> param3) {
        this.seed = param0;
        this.generateStructures = param1;
        this.generateBonusChest = param2;
        this.legacyCustomOptions = param3;
    }

    public long seed() {
        return this.seed;
    }

    public boolean generateStructures() {
        return this.generateStructures;
    }

    public boolean generateBonusChest() {
        return this.generateBonusChest;
    }

    public boolean isOldCustomizedWorld() {
        return this.legacyCustomOptions.isPresent();
    }

    public WorldOptions withBonusChest(boolean param0) {
        return new WorldOptions(this.seed, this.generateStructures, param0, this.legacyCustomOptions);
    }

    public WorldOptions withStructures(boolean param0) {
        return new WorldOptions(this.seed, param0, this.generateBonusChest, this.legacyCustomOptions);
    }

    public WorldOptions withSeed(OptionalLong param0) {
        return new WorldOptions(param0.orElse(this.seed), this.generateStructures, this.generateBonusChest, this.legacyCustomOptions);
    }

    public static OptionalLong parseSeed(String param0) {
        param0 = param0.trim();
        if (StringUtils.isEmpty(param0)) {
            return OptionalLong.empty();
        } else {
            try {
                return OptionalLong.of(Long.parseLong(param0));
            } catch (NumberFormatException var2) {
                return OptionalLong.of((long)param0.hashCode());
            }
        }
    }
}
