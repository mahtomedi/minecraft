package net.minecraft.client.resources.metadata.gui;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.OptionalInt;
import java.util.function.Function;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface GuiSpriteScaling {
    Codec<GuiSpriteScaling> CODEC = GuiSpriteScaling.Type.CODEC.dispatch(GuiSpriteScaling::type, GuiSpriteScaling.Type::codec);
    GuiSpriteScaling DEFAULT = new GuiSpriteScaling.Stretch();

    GuiSpriteScaling.Type type();

    @OnlyIn(Dist.CLIENT)
    public static record NineSlice(int width, int height, GuiSpriteScaling.NineSlice.Border border) implements GuiSpriteScaling {
        public static final Codec<GuiSpriteScaling.NineSlice> CODEC = ExtraCodecs.validate(
            RecordCodecBuilder.create(
                param0 -> param0.group(
                            ExtraCodecs.POSITIVE_INT.fieldOf("width").forGetter(GuiSpriteScaling.NineSlice::width),
                            ExtraCodecs.POSITIVE_INT.fieldOf("height").forGetter(GuiSpriteScaling.NineSlice::height),
                            GuiSpriteScaling.NineSlice.Border.CODEC.fieldOf("border").forGetter(GuiSpriteScaling.NineSlice::border)
                        )
                        .apply(param0, GuiSpriteScaling.NineSlice::new)
            ),
            GuiSpriteScaling.NineSlice::validate
        );

        private static DataResult<GuiSpriteScaling.NineSlice> validate(GuiSpriteScaling.NineSlice param0) {
            GuiSpriteScaling.NineSlice.Border var0 = param0.border();
            if (var0.left() + var0.right() >= param0.width()) {
                return DataResult.error(
                    () -> "Nine-sliced texture has no horizontal center slice: " + var0.left() + " + " + var0.right() + " >= " + param0.width()
                );
            } else {
                return var0.top() + var0.bottom() >= param0.height()
                    ? DataResult.error(
                        () -> "Nine-sliced texture has no vertical center slice: " + var0.top() + " + " + var0.bottom() + " >= " + param0.height()
                    )
                    : DataResult.success(param0);
            }
        }

        @Override
        public GuiSpriteScaling.Type type() {
            return GuiSpriteScaling.Type.NINE_SLICE;
        }

        @OnlyIn(Dist.CLIENT)
        public static record Border(int left, int top, int right, int bottom) {
            private static final Codec<GuiSpriteScaling.NineSlice.Border> VALUE_CODEC = ExtraCodecs.POSITIVE_INT
                .flatComapMap(param0 -> new GuiSpriteScaling.NineSlice.Border(param0, param0, param0, param0), param0 -> {
                    OptionalInt var0 = param0.unpackValue();
                    return var0.isPresent() ? DataResult.success(var0.getAsInt()) : DataResult.error(() -> "Border has different side sizes");
                });
            private static final Codec<GuiSpriteScaling.NineSlice.Border> RECORD_CODEC = RecordCodecBuilder.create(
                param0 -> param0.group(
                            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("left").forGetter(GuiSpriteScaling.NineSlice.Border::left),
                            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("top").forGetter(GuiSpriteScaling.NineSlice.Border::top),
                            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("right").forGetter(GuiSpriteScaling.NineSlice.Border::right),
                            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("bottom").forGetter(GuiSpriteScaling.NineSlice.Border::bottom)
                        )
                        .apply(param0, GuiSpriteScaling.NineSlice.Border::new)
            );
            static final Codec<GuiSpriteScaling.NineSlice.Border> CODEC = Codec.either(VALUE_CODEC, RECORD_CODEC)
                .xmap(
                    param0 -> param0.map(Function.identity(), Function.identity()),
                    param0 -> param0.unpackValue().isPresent() ? Either.left(param0) : Either.right(param0)
                );

            private OptionalInt unpackValue() {
                return this.left() == this.top() && this.top() == this.right() && this.right() == this.bottom()
                    ? OptionalInt.of(this.left())
                    : OptionalInt.empty();
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static record Stretch() implements GuiSpriteScaling {
        public static final Codec<GuiSpriteScaling.Stretch> CODEC = Codec.unit(GuiSpriteScaling.Stretch::new);

        @Override
        public GuiSpriteScaling.Type type() {
            return GuiSpriteScaling.Type.STRETCH;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static record Tile(int width, int height) implements GuiSpriteScaling {
        public static final Codec<GuiSpriteScaling.Tile> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        ExtraCodecs.POSITIVE_INT.fieldOf("width").forGetter(GuiSpriteScaling.Tile::width),
                        ExtraCodecs.POSITIVE_INT.fieldOf("height").forGetter(GuiSpriteScaling.Tile::height)
                    )
                    .apply(param0, GuiSpriteScaling.Tile::new)
        );

        @Override
        public GuiSpriteScaling.Type type() {
            return GuiSpriteScaling.Type.TILE;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static enum Type implements StringRepresentable {
        STRETCH("stretch", GuiSpriteScaling.Stretch.CODEC),
        TILE("tile", GuiSpriteScaling.Tile.CODEC),
        NINE_SLICE("nine_slice", GuiSpriteScaling.NineSlice.CODEC);

        public static final Codec<GuiSpriteScaling.Type> CODEC = StringRepresentable.fromEnum(GuiSpriteScaling.Type::values);
        private final String key;
        private final Codec<? extends GuiSpriteScaling> codec;

        private Type(String param0, Codec<? extends GuiSpriteScaling> param1) {
            this.key = param0;
            this.codec = param1;
        }

        @Override
        public String getSerializedName() {
            return this.key;
        }

        public Codec<? extends GuiSpriteScaling> codec() {
            return this.codec;
        }
    }
}
