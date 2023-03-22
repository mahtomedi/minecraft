package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.dimension.DimensionType;

public record NoiseSettings(int minY, int height, int noiseSizeHorizontal, int noiseSizeVertical) {
    public static final Codec<NoiseSettings> CODEC = RecordCodecBuilder.<NoiseSettings>create(
            param0 -> param0.group(
                        Codec.intRange(DimensionType.MIN_Y, DimensionType.MAX_Y).fieldOf("min_y").forGetter(NoiseSettings::minY),
                        Codec.intRange(0, DimensionType.Y_SIZE).fieldOf("height").forGetter(NoiseSettings::height),
                        Codec.intRange(1, 4).fieldOf("size_horizontal").forGetter(NoiseSettings::noiseSizeHorizontal),
                        Codec.intRange(1, 4).fieldOf("size_vertical").forGetter(NoiseSettings::noiseSizeVertical)
                    )
                    .apply(param0, NoiseSettings::new)
        )
        .comapFlatMap(NoiseSettings::guardY, Function.identity());
    protected static final NoiseSettings OVERWORLD_NOISE_SETTINGS = create(-64, 384, 1, 2);
    protected static final NoiseSettings NETHER_NOISE_SETTINGS = create(0, 128, 1, 2);
    protected static final NoiseSettings END_NOISE_SETTINGS = create(0, 128, 2, 1);
    protected static final NoiseSettings CAVES_NOISE_SETTINGS = create(-64, 192, 1, 2);
    protected static final NoiseSettings FLOATING_ISLANDS_NOISE_SETTINGS = create(0, 256, 2, 1);

    private static DataResult<NoiseSettings> guardY(NoiseSettings param0) {
        if (param0.minY() + param0.height() > DimensionType.MAX_Y + 1) {
            return DataResult.error(() -> "min_y + height cannot be higher than: " + (DimensionType.MAX_Y + 1));
        } else if (param0.height() % 16 != 0) {
            return DataResult.error(() -> "height has to be a multiple of 16");
        } else {
            return param0.minY() % 16 != 0 ? DataResult.error(() -> "min_y has to be a multiple of 16") : DataResult.success(param0);
        }
    }

    public static NoiseSettings create(int param0, int param1, int param2, int param3) {
        NoiseSettings var0 = new NoiseSettings(param0, param1, param2, param3);
        guardY(var0).error().ifPresent(param0x -> {
            throw new IllegalStateException(param0x.message());
        });
        return var0;
    }

    public int getCellHeight() {
        return QuartPos.toBlock(this.noiseSizeVertical());
    }

    public int getCellWidth() {
        return QuartPos.toBlock(this.noiseSizeHorizontal());
    }

    public NoiseSettings clampToHeightAccessor(LevelHeightAccessor param0) {
        int var0 = Math.max(this.minY, param0.getMinBuildHeight());
        int var1 = Math.min(this.minY + this.height, param0.getMaxBuildHeight()) - var0;
        return new NoiseSettings(var0, var1, this.noiseSizeHorizontal, this.noiseSizeVertical);
    }
}
