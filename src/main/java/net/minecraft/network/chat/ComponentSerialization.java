package net.minecraft.network.chat;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapDecoder;
import com.mojang.serialization.MapEncoder;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.network.chat.contents.KeybindContents;
import net.minecraft.network.chat.contents.NbtContents;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.ScoreContents;
import net.minecraft.network.chat.contents.SelectorContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;

public class ComponentSerialization {
    public static final Codec<Component> CODEC = ExtraCodecs.recursive(ComponentSerialization::createCodec);
    public static final Codec<Component> FLAT_CODEC = ExtraCodecs.FLAT_JSON
        .flatXmap(param0 -> CODEC.parse(JsonOps.INSTANCE, param0), param0 -> CODEC.encodeStart(JsonOps.INSTANCE, param0));

    private static MutableComponent createFromList(List<Component> param0) {
        MutableComponent var0 = param0.get(0).copy();

        for(int var1 = 1; var1 < param0.size(); ++var1) {
            var0.append(param0.get(var1));
        }

        return var0;
    }

    public static <T extends StringRepresentable, E> MapCodec<E> createLegacyComponentMatcher(
        T[] param0, Function<T, MapCodec<? extends E>> param1, Function<E, T> param2, String param3
    ) {
        MapCodec<E> var0 = new ComponentSerialization.FuzzyCodec<>(Stream.<T>of(param0).map(param1).toList(), param2x -> param1.apply(param2.apply(param2x)));
        Codec<T> var1 = StringRepresentable.fromValues(() -> param0);
        MapCodec<E> var2 = var1.dispatchMap(param3, param2, param1x -> param1.apply(param1x).codec());
        MapCodec<E> var3 = new ComponentSerialization.StrictEither<>(param3, var2, var0);
        return ExtraCodecs.orCompressed(var3, var2);
    }

    private static Codec<Component> createCodec(Codec<Component> param0) {
        ComponentContents.Type<?>[] var0 = new ComponentContents.Type[]{
            PlainTextContents.TYPE, TranslatableContents.TYPE, KeybindContents.TYPE, ScoreContents.TYPE, SelectorContents.TYPE, NbtContents.TYPE
        };
        MapCodec<ComponentContents> var1 = createLegacyComponentMatcher(var0, ComponentContents.Type::codec, ComponentContents::type, "type");
        Codec<Component> var2 = RecordCodecBuilder.create(
            param2 -> param2.group(
                        var1.forGetter(Component::getContents),
                        ExtraCodecs.strictOptionalField(ExtraCodecs.nonEmptyList(param0.listOf()), "extra", List.of()).forGetter(Component::getSiblings),
                        Style.Serializer.MAP_CODEC.forGetter(Component::getStyle)
                    )
                    .apply(param2, MutableComponent::new)
        );
        return Codec.either(Codec.either(Codec.STRING, ExtraCodecs.nonEmptyList(param0.listOf())), var2)
            .xmap(
                param0x -> param0x.map(param0xx -> param0xx.map(Component::literal, ComponentSerialization::createFromList), param0xx -> param0xx),
                param0x -> {
                    String var0x = param0x.tryCollapseToString();
                    return var0x != null ? Either.left(Either.left(var0x)) : Either.right(param0x);
                }
            );
    }

    static class FuzzyCodec<T> extends MapCodec<T> {
        private final List<MapCodec<? extends T>> codecs;
        private final Function<T, MapEncoder<? extends T>> encoderGetter;

        public FuzzyCodec(List<MapCodec<? extends T>> param0, Function<T, MapEncoder<? extends T>> param1) {
            this.codecs = param0;
            this.encoderGetter = param1;
        }

        @Override
        public <S> DataResult<T> decode(DynamicOps<S> param0, MapLike<S> param1) {
            for(MapDecoder<? extends T> var0 : this.codecs) {
                DataResult<? extends T> var1 = var0.decode(param0, param1);
                if (var1.result().isPresent()) {
                    return var1;
                }
            }

            return DataResult.error(() -> "No matching codec found");
        }

        @Override
        public <S> RecordBuilder<S> encode(T param0, DynamicOps<S> param1, RecordBuilder<S> param2) {
            MapEncoder<T> var0 = this.encoderGetter.apply(param0);
            return var0.encode(param0, param1, param2);
        }

        @Override
        public <S> Stream<S> keys(DynamicOps<S> param0) {
            return this.codecs.stream().flatMap(param1 -> param1.keys(param0)).distinct();
        }

        @Override
        public String toString() {
            return "FuzzyCodec[" + this.codecs + "]";
        }
    }

    static class StrictEither<T> extends MapCodec<T> {
        private final String typeFieldName;
        private final MapCodec<T> typed;
        private final MapCodec<T> fuzzy;

        public StrictEither(String param0, MapCodec<T> param1, MapCodec<T> param2) {
            this.typeFieldName = param0;
            this.typed = param1;
            this.fuzzy = param2;
        }

        @Override
        public <O> DataResult<T> decode(DynamicOps<O> param0, MapLike<O> param1) {
            return param1.get(this.typeFieldName) != null ? this.typed.decode(param0, param1) : this.fuzzy.decode(param0, param1);
        }

        @Override
        public <O> RecordBuilder<O> encode(T param0, DynamicOps<O> param1, RecordBuilder<O> param2) {
            return this.fuzzy.encode(param0, param1, param2);
        }

        @Override
        public <T1> Stream<T1> keys(DynamicOps<T1> param0) {
            return Stream.concat(this.typed.keys(param0), this.fuzzy.keys(param0)).distinct();
        }
    }
}
