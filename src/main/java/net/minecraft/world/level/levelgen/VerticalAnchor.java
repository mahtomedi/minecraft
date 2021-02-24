package net.minecraft.world.level.levelgen;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import java.util.function.Function;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.dimension.DimensionType;

public abstract class VerticalAnchor {
    public static final Codec<VerticalAnchor> CODEC = ExtraCodecs.xor(
            VerticalAnchor.Absolute.CODEC, ExtraCodecs.xor(VerticalAnchor.AboveBottom.CODEC, VerticalAnchor.BelowTop.CODEC)
        )
        .xmap(VerticalAnchor::merge, VerticalAnchor::split);
    private static final VerticalAnchor BOTTOM = aboveBottom(0);
    private static final VerticalAnchor TOP = belowTop(0);
    private final int value;

    protected VerticalAnchor(int param0) {
        this.value = param0;
    }

    public static VerticalAnchor absolute(int param0) {
        return new VerticalAnchor.Absolute(param0);
    }

    public static VerticalAnchor aboveBottom(int param0) {
        return new VerticalAnchor.AboveBottom(param0);
    }

    public static VerticalAnchor belowTop(int param0) {
        return new VerticalAnchor.BelowTop(param0);
    }

    public static VerticalAnchor bottom() {
        return BOTTOM;
    }

    public static VerticalAnchor top() {
        return TOP;
    }

    private static VerticalAnchor merge(Either<VerticalAnchor.Absolute, Either<VerticalAnchor.AboveBottom, VerticalAnchor.BelowTop>> param0) {
        return param0.map(Function.identity(), param0x -> param0x.map(Function.identity(), Function.identity()));
    }

    private static Either<VerticalAnchor.Absolute, Either<VerticalAnchor.AboveBottom, VerticalAnchor.BelowTop>> split(VerticalAnchor param0) {
        return param0 instanceof VerticalAnchor.Absolute
            ? Either.left((VerticalAnchor.Absolute)param0)
            : Either.right(
                param0 instanceof VerticalAnchor.AboveBottom ? Either.left((VerticalAnchor.AboveBottom)param0) : Either.right((VerticalAnchor.BelowTop)param0)
            );
    }

    protected int value() {
        return this.value;
    }

    public abstract int resolveY(WorldGenerationContext var1);

    static final class AboveBottom extends VerticalAnchor {
        public static final Codec<VerticalAnchor.AboveBottom> CODEC = Codec.intRange(DimensionType.MIN_Y, DimensionType.MAX_Y)
            .fieldOf("above_bottom")
            .xmap(VerticalAnchor.AboveBottom::new, VerticalAnchor::value)
            .codec();

        protected AboveBottom(int param0) {
            super(param0);
        }

        @Override
        public int resolveY(WorldGenerationContext param0) {
            return param0.getMinGenY() + this.value();
        }
    }

    static final class Absolute extends VerticalAnchor {
        public static final Codec<VerticalAnchor.Absolute> CODEC = Codec.intRange(DimensionType.MIN_Y, DimensionType.MAX_Y)
            .fieldOf("absolute")
            .xmap(VerticalAnchor.Absolute::new, VerticalAnchor::value)
            .codec();

        protected Absolute(int param0) {
            super(param0);
        }

        @Override
        public int resolveY(WorldGenerationContext param0) {
            return this.value();
        }
    }

    static final class BelowTop extends VerticalAnchor {
        public static final Codec<VerticalAnchor.BelowTop> CODEC = Codec.intRange(DimensionType.MIN_Y, DimensionType.MAX_Y)
            .fieldOf("below_top")
            .xmap(VerticalAnchor.BelowTop::new, VerticalAnchor::value)
            .codec();

        protected BelowTop(int param0) {
            super(param0);
        }

        @Override
        public int resolveY(WorldGenerationContext param0) {
            return param0.getGenDepth() - 1 + param0.getMinGenY() - this.value();
        }
    }
}
