package net.minecraft.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.Codec.ResultFunction;
import com.mojang.serialization.DataResult.PartialResult;
import com.mojang.serialization.MapCodec.MapCodecCodec;
import com.mojang.serialization.codecs.BaseMapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Base64;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.HolderSet;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.mutable.MutableObject;
import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class ExtraCodecs {
    public static final Codec<JsonElement> JSON = Codec.PASSTHROUGH
        .xmap(param0 -> param0.convert(JsonOps.INSTANCE).getValue(), param0 -> new Dynamic<>(JsonOps.INSTANCE, param0));
    public static final Codec<Component> COMPONENT = adaptJsonSerializer(Component.Serializer::fromJson, Component.Serializer::toJsonTree);
    public static final Codec<Component> FLAT_COMPONENT = Codec.STRING.flatXmap(param0 -> {
        try {
            return DataResult.success(Component.Serializer.fromJson(param0));
        } catch (JsonParseException var2) {
            return DataResult.error(var2::getMessage);
        }
    }, param0 -> {
        try {
            return DataResult.success(Component.Serializer.toJson(param0));
        } catch (IllegalArgumentException var2) {
            return DataResult.error(var2::getMessage);
        }
    });
    public static final Codec<Vector3f> VECTOR3F = Codec.FLOAT
        .listOf()
        .comapFlatMap(
            param0 -> Util.fixedSize(param0, 3).map(param0x -> new Vector3f(param0x.get(0), param0x.get(1), param0x.get(2))),
            param0 -> List.of(param0.x(), param0.y(), param0.z())
        );
    public static final Codec<Quaternionf> QUATERNIONF_COMPONENTS = Codec.FLOAT
        .listOf()
        .comapFlatMap(
            param0 -> Util.fixedSize(param0, 4).map(param0x -> new Quaternionf(param0x.get(0), param0x.get(1), param0x.get(2), param0x.get(3))),
            param0 -> List.of(param0.x, param0.y, param0.z, param0.w)
        );
    public static final Codec<AxisAngle4f> AXISANGLE4F = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codec.FLOAT.fieldOf("angle").forGetter(param0x -> param0x.angle),
                    VECTOR3F.fieldOf("axis").forGetter(param0x -> new Vector3f(param0x.x, param0x.y, param0x.z))
                )
                .apply(param0, AxisAngle4f::new)
    );
    public static final Codec<Quaternionf> QUATERNIONF = withAlternative(QUATERNIONF_COMPONENTS, AXISANGLE4F.xmap(Quaternionf::new, AxisAngle4f::new));
    public static Codec<Matrix4f> MATRIX4F = Codec.FLOAT.listOf().comapFlatMap(param0 -> Util.fixedSize(param0, 16).map(param0x -> {
            Matrix4f var0x = new Matrix4f();

            for(int var1 = 0; var1 < param0x.size(); ++var1) {
                var0x.setRowColumn(var1 >> 2, var1 & 3, param0x.get(var1));
            }

            return var0x.determineProperties();
        }), param0 -> {
        FloatList var0 = new FloatArrayList(16);

        for(int var1 = 0; var1 < 16; ++var1) {
            var0.add(param0.getRowColumn(var1 >> 2, var1 & 3));
        }

        return var0;
    });
    public static final Codec<Integer> NON_NEGATIVE_INT = intRangeWithMessage(0, Integer.MAX_VALUE, param0 -> "Value must be non-negative: " + param0);
    public static final Codec<Integer> POSITIVE_INT = intRangeWithMessage(1, Integer.MAX_VALUE, param0 -> "Value must be positive: " + param0);
    public static final Codec<Float> POSITIVE_FLOAT = floatRangeMinExclusiveWithMessage(0.0F, Float.MAX_VALUE, param0 -> "Value must be positive: " + param0);
    public static final Codec<Pattern> PATTERN = Codec.STRING.comapFlatMap(param0 -> {
        try {
            return DataResult.success(Pattern.compile(param0));
        } catch (PatternSyntaxException var2) {
            return DataResult.error(() -> "Invalid regex pattern '" + param0 + "': " + var2.getMessage());
        }
    }, Pattern::pattern);
    public static final Codec<Instant> INSTANT_ISO8601 = temporalCodec(DateTimeFormatter.ISO_INSTANT).xmap(Instant::from, Function.identity());
    public static final Codec<byte[]> BASE64_STRING = Codec.STRING.comapFlatMap(param0 -> {
        try {
            return DataResult.success(Base64.getDecoder().decode(param0));
        } catch (IllegalArgumentException var2) {
            return DataResult.error(() -> "Malformed base64 string");
        }
    }, param0 -> Base64.getEncoder().encodeToString(param0));
    public static final Codec<String> ESCAPED_STRING = Codec.STRING
        .comapFlatMap(param0 -> DataResult.success(StringEscapeUtils.unescapeJava(param0)), StringEscapeUtils::escapeJava);
    public static final Codec<ExtraCodecs.TagOrElementLocation> TAG_OR_ELEMENT_ID = Codec.STRING
        .comapFlatMap(
            param0 -> param0.startsWith("#")
                    ? ResourceLocation.read(param0.substring(1)).map(param0x -> new ExtraCodecs.TagOrElementLocation(param0x, true))
                    : ResourceLocation.read(param0).map(param0x -> new ExtraCodecs.TagOrElementLocation(param0x, false)),
            ExtraCodecs.TagOrElementLocation::decoratedId
        );
    public static final Function<Optional<Long>, OptionalLong> toOptionalLong = param0 -> param0.map(OptionalLong::of).orElseGet(OptionalLong::empty);
    public static final Function<OptionalLong, Optional<Long>> fromOptionalLong = param0 -> param0.isPresent()
            ? Optional.of(param0.getAsLong())
            : Optional.empty();
    public static final Codec<BitSet> BIT_SET = Codec.LONG_STREAM
        .xmap(param0 -> BitSet.valueOf(param0.toArray()), param0 -> Arrays.stream(param0.toLongArray()));
    private static final Codec<Property> PROPERTY = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codec.STRING.fieldOf("name").forGetter(Property::name),
                    Codec.STRING.fieldOf("value").forGetter(Property::value),
                    Codec.STRING.optionalFieldOf("signature").forGetter(param0x -> Optional.ofNullable(param0x.signature()))
                )
                .apply(param0, (param0x, param1, param2) -> new Property(param0x, param1, param2.orElse(null)))
    );
    @VisibleForTesting
    public static final Codec<PropertyMap> PROPERTY_MAP = Codec.either(Codec.unboundedMap(Codec.STRING, Codec.STRING.listOf()), PROPERTY.listOf())
        .xmap(param0 -> {
            PropertyMap var0 = new PropertyMap();
            param0.ifLeft(param1 -> param1.forEach((param1x, param2) -> {
                    for(String var0x : param2) {
                        var0.put(param1x, new Property(param1x, var0x));
                    }
    
                })).ifRight(param1 -> {
                for(Property var0x : param1) {
                    var0.put(var0x.name(), var0x);
                }
    
            });
            return var0;
        }, param0 -> Either.right(param0.values().stream().toList()));
    private static final MapCodec<GameProfile> GAME_PROFILE_WITHOUT_PROPERTIES = RecordCodecBuilder.mapCodec(
        param0 -> param0.group(UUIDUtil.AUTHLIB_CODEC.fieldOf("id").forGetter(GameProfile::getId), Codec.STRING.fieldOf("name").forGetter(GameProfile::getName))
                .apply(param0, GameProfile::new)
    );
    public static final Codec<GameProfile> GAME_PROFILE = RecordCodecBuilder.create(
        param0 -> param0.group(
                    GAME_PROFILE_WITHOUT_PROPERTIES.forGetter(Function.identity()),
                    PROPERTY_MAP.optionalFieldOf("properties", new PropertyMap()).forGetter(GameProfile::getProperties)
                )
                .apply(param0, (param0x, param1) -> {
                    param1.forEach((param1x, param2) -> param0x.getProperties().put(param1x, param2));
                    return param0x;
                })
    );
    public static final Codec<String> NON_EMPTY_STRING = validate(
        Codec.STRING, param0 -> param0.isEmpty() ? DataResult.error(() -> "Expected non-empty string") : DataResult.success(param0)
    );
    public static final Codec<Integer> CODEPOINT = Codec.STRING.comapFlatMap(param0 -> {
        int[] var0 = param0.codePoints().toArray();
        return var0.length != 1 ? DataResult.error(() -> "Expected one codepoint, got: " + param0) : DataResult.success(var0[0]);
    }, Character::toString);
    public static Codec<String> RESOURCE_PATH_CODEC = validate(
        Codec.STRING,
        param0 -> !ResourceLocation.isValidPath(param0)
                ? DataResult.error(() -> "Invalid string to use as a resource path element: " + param0)
                : DataResult.success(param0)
    );

    @Deprecated
    public static <T> Codec<T> adaptJsonSerializer(Function<JsonElement, T> param0, Function<T, JsonElement> param1) {
        return JSON.flatXmap(param1x -> {
            try {
                return DataResult.success(param0.apply(param1x));
            } catch (JsonParseException var3) {
                return DataResult.error(var3::getMessage);
            }
        }, param1x -> {
            try {
                return DataResult.success(param1.apply(param1x));
            } catch (IllegalArgumentException var3) {
                return DataResult.error(var3::getMessage);
            }
        });
    }

    public static <F, S> Codec<Either<F, S>> xor(Codec<F> param0, Codec<S> param1) {
        return new ExtraCodecs.XorCodec<>(param0, param1);
    }

    public static <P, I> Codec<I> intervalCodec(
        Codec<P> param0, String param1, String param2, BiFunction<P, P, DataResult<I>> param3, Function<I, P> param4, Function<I, P> param5
    ) {
        Codec<I> var0 = Codec.list(param0).comapFlatMap(param1x -> Util.fixedSize(param1x, 2).flatMap(param1xx -> {
                P var0x = param1xx.get(0);
                P var1x = param1xx.get(1);
                return param3.apply(var0x, var1x);
            }), param2x -> ImmutableList.of(param4.apply(param2x), param5.apply(param2x)));
        Codec<I> var1 = RecordCodecBuilder.<Pair>create(
                param3x -> param3x.group(param0.fieldOf(param1).forGetter(Pair::getFirst), param0.fieldOf(param2).forGetter(Pair::getSecond))
                        .apply(param3x, Pair::of)
            )
            .comapFlatMap(
                param1x -> param3.apply((P)param1x.getFirst(), (P)param1x.getSecond()), param2x -> Pair.of(param4.apply(param2x), param5.apply(param2x))
            );
        Codec<I> var2 = withAlternative(var0, var1);
        return Codec.either(param0, var2).comapFlatMap(param1x -> param1x.map(param1xx -> param3.apply(param1xx, param1xx), DataResult::success), param2x -> {
            P var0x = param4.apply(param2x);
            P var1x = param5.apply(param2x);
            return Objects.equals(var0x, var1x) ? Either.left(var0x) : Either.right(param2x);
        });
    }

    public static <A> ResultFunction<A> orElsePartial(final A param0) {
        return new ResultFunction<A>() {
            @Override
            public <T> DataResult<Pair<A, T>> apply(DynamicOps<T> param0x, T param1, DataResult<Pair<A, T>> param2) {
                MutableObject<String> var0 = new MutableObject<>();
                Optional<Pair<A, T>> var1 = param2.resultOrPartial(var0::setValue);
                return var1.isPresent() ? param2 : DataResult.error(() -> "(" + (String)var0.getValue() + " -> using default)", Pair.of(param0, param1));
            }

            @Override
            public <T> DataResult<T> coApply(DynamicOps<T> param0x, A param1, DataResult<T> param2) {
                return param2;
            }

            @Override
            public String toString() {
                return "OrElsePartial[" + param0 + "]";
            }
        };
    }

    public static <E> Codec<E> idResolverCodec(ToIntFunction<E> param0, IntFunction<E> param1, int param2) {
        return Codec.INT
            .flatXmap(
                param1x -> Optional.ofNullable(param1.apply(param1x))
                        .map(DataResult::success)
                        .orElseGet(() -> DataResult.error(() -> "Unknown element id: " + param1x)),
                param2x -> {
                    int var0x = param0.applyAsInt(param2x);
                    return var0x == param2 ? DataResult.error(() -> "Element with unknown id: " + param2x) : DataResult.success(var0x);
                }
            );
    }

    public static <E> Codec<E> stringResolverCodec(Function<E, String> param0, Function<String, E> param1) {
        return Codec.STRING
            .flatXmap(
                param1x -> Optional.ofNullable(param1.apply(param1x))
                        .map(DataResult::success)
                        .orElseGet(() -> DataResult.error(() -> "Unknown element name:" + param1x)),
                param1x -> Optional.ofNullable(param0.apply(param1x))
                        .map(DataResult::success)
                        .orElseGet(() -> DataResult.error(() -> "Element with unknown name: " + param1x))
            );
    }

    public static <E> Codec<E> orCompressed(final Codec<E> param0, final Codec<E> param1) {
        return new Codec<E>() {
            @Override
            public <T> DataResult<T> encode(E param0x, DynamicOps<T> param1x, T param2) {
                return param1.compressMaps() ? param1.encode(param0, param1, param2) : param0.encode(param0, param1, param2);
            }

            @Override
            public <T> DataResult<Pair<E, T>> decode(DynamicOps<T> param0x, T param1x) {
                return param0.compressMaps() ? param1.decode(param0, param1) : param0.decode(param0, param1);
            }

            @Override
            public String toString() {
                return param0 + " orCompressed " + param1;
            }
        };
    }

    public static <E> Codec<E> overrideLifecycle(Codec<E> param0, final Function<E, Lifecycle> param1, final Function<E, Lifecycle> param2) {
        return param0.mapResult(new ResultFunction<E>() {
            @Override
            public <T> DataResult<Pair<E, T>> apply(DynamicOps<T> param0, T param1x, DataResult<Pair<E, T>> param2x) {
                return param2.result().map(param2xxx -> param2.setLifecycle(param1.apply(param2xxx.getFirst()))).orElse(param2);
            }

            @Override
            public <T> DataResult<T> coApply(DynamicOps<T> param0, E param1x, DataResult<T> param2x) {
                return param2.setLifecycle(param2.apply(param1));
            }

            @Override
            public String toString() {
                return "WithLifecycle[" + param1 + " " + param2 + "]";
            }
        });
    }

    public static <F, S> ExtraCodecs.EitherCodec<F, S> either(Codec<F> param0, Codec<S> param1) {
        return new ExtraCodecs.EitherCodec<>(param0, param1);
    }

    public static <K, V> ExtraCodecs.StrictUnboundedMapCodec<K, V> strictUnboundedMap(Codec<K> param0, Codec<V> param1) {
        return new ExtraCodecs.StrictUnboundedMapCodec<>(param0, param1);
    }

    public static <T> Codec<T> validate(Codec<T> param0, Function<T, DataResult<T>> param1) {
        return param0 instanceof MapCodecCodec var0 ? validate(var0.codec(), param1).codec() : param0.flatXmap(param1, param1);
    }

    public static <T> MapCodec<T> validate(MapCodec<T> param0, Function<T, DataResult<T>> param1) {
        return param0.flatXmap(param1, param1);
    }

    private static Codec<Integer> intRangeWithMessage(int param0, int param1, Function<Integer, String> param2) {
        return validate(
            Codec.INT,
            param3 -> param3.compareTo(param0) >= 0 && param3.compareTo(param1) <= 0
                    ? DataResult.success(param3)
                    : DataResult.error(() -> param2.apply(param3))
        );
    }

    public static Codec<Integer> intRange(int param0, int param1) {
        return intRangeWithMessage(param0, param1, param2 -> "Value must be within range [" + param0 + ";" + param1 + "]: " + param2);
    }

    private static Codec<Float> floatRangeMinExclusiveWithMessage(float param0, float param1, Function<Float, String> param2) {
        return validate(
            Codec.FLOAT,
            param3 -> param3.compareTo(param0) > 0 && param3.compareTo(param1) <= 0 ? DataResult.success(param3) : DataResult.error(() -> param2.apply(param3))
        );
    }

    public static <T> Codec<List<T>> nonEmptyList(Codec<List<T>> param0) {
        return validate(param0, param0x -> param0x.isEmpty() ? DataResult.error(() -> "List must have contents") : DataResult.success(param0x));
    }

    public static <T> Codec<HolderSet<T>> nonEmptyHolderSet(Codec<HolderSet<T>> param0) {
        return validate(
            param0,
            param0x -> param0x.unwrap().right().filter(List::isEmpty).isPresent()
                    ? DataResult.error(() -> "List must have contents")
                    : DataResult.success(param0x)
        );
    }

    public static <T> Codec<T> recursive(Function<Codec<T>, Codec<T>> param0) {
        return new ExtraCodecs.RecursiveCodec<>(param0);
    }

    public static <A> Codec<A> lazyInitializedCodec(Supplier<Codec<A>> param0) {
        return new ExtraCodecs.RecursiveCodec<>(param1 -> param0.get());
    }

    public static <A> MapCodec<Optional<A>> strictOptionalField(Codec<A> param0, String param1) {
        return new ExtraCodecs.StrictOptionalFieldCodec<>(param1, param0);
    }

    public static <A> MapCodec<A> strictOptionalField(Codec<A> param0, String param1, A param2) {
        return strictOptionalField(param0, param1)
            .xmap(param1x -> param1x.orElse(param2), param1x -> Objects.equals(param1x, param2) ? Optional.empty() : Optional.of(param1x));
    }

    public static <E> MapCodec<E> retrieveContext(final Function<DynamicOps<?>, DataResult<E>> param0) {
        class ContextRetrievalCodec extends MapCodec<E> {
            @Override
            public <T> RecordBuilder<T> encode(E param0x, DynamicOps<T> param1, RecordBuilder<T> param2) {
                return param2;
            }

            @Override
            public <T> DataResult<E> decode(DynamicOps<T> param0x, MapLike<T> param1) {
                return param0.apply(param0);
            }

            @Override
            public String toString() {
                return "ContextRetrievalCodec[" + param0 + "]";
            }

            @Override
            public <T> Stream<T> keys(DynamicOps<T> param0x) {
                return Stream.empty();
            }
        }

        return new ContextRetrievalCodec();
    }

    public static <E, L extends Collection<E>, T> Function<L, DataResult<L>> ensureHomogenous(Function<E, T> param0) {
        return param1 -> {
            Iterator<E> var0x = param1.iterator();
            if (var0x.hasNext()) {
                T var1 = param0.apply((E)var0x.next());

                while(var0x.hasNext()) {
                    E var2 = (E)var0x.next();
                    T var3 = param0.apply(var2);
                    if (var3 != var1) {
                        return DataResult.error(() -> "Mixed type list: element " + var2 + " had type " + var3 + ", but list is of type " + var1);
                    }
                }
            }

            return DataResult.success(param1, Lifecycle.stable());
        };
    }

    public static <A> Codec<A> catchDecoderException(final Codec<A> param0) {
        return Codec.of(param0, new Decoder<A>() {
            @Override
            public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> param0x, T param1) {
                try {
                    return param0.decode(param0, param1);
                } catch (Exception var4) {
                    return DataResult.error(() -> "Caught exception decoding " + param1 + ": " + var4.getMessage());
                }
            }
        });
    }

    public static Codec<TemporalAccessor> temporalCodec(DateTimeFormatter param0) {
        return Codec.STRING.comapFlatMap(param1 -> {
            try {
                return DataResult.success(param0.parse(param1));
            } catch (Exception var3) {
                return DataResult.error(var3::getMessage);
            }
        }, param0::format);
    }

    public static MapCodec<OptionalLong> asOptionalLong(MapCodec<Optional<Long>> param0) {
        return param0.xmap(toOptionalLong, fromOptionalLong);
    }

    public static Codec<String> sizeLimitedString(int param0, int param1) {
        return validate(
            Codec.STRING,
            param2 -> {
                int var0x = param2.length();
                if (var0x < param0) {
                    return DataResult.error(() -> "String \"" + param2 + "\" is too short: " + var0x + ", expected range [" + param0 + "-" + param1 + "]");
                } else {
                    return var0x > param1
                        ? DataResult.error(() -> "String \"" + param2 + "\" is too long: " + var0x + ", expected range [" + param0 + "-" + param1 + "]")
                        : DataResult.success(param2);
                }
            }
        );
    }

    public static <T> Codec<T> withAlternative(Codec<T> param0, Codec<? extends T> param1) {
        return Codec.either(param0, param1).xmap(param0x -> param0x.map(param0xx -> param0xx, param0xx -> param0xx), Either::left);
    }

    public static <T, U> Codec<T> withAlternative(Codec<T> param0, Codec<U> param1, Function<U, T> param2) {
        return Codec.either(param0, param1).xmap(param1x -> param1x.map(param0x -> param0x, param2), Either::left);
    }

    public static <T> Codec<Object2BooleanMap<T>> object2BooleanMap(Codec<T> param0) {
        return Codec.unboundedMap(param0, Codec.BOOL).xmap(Object2BooleanOpenHashMap::new, Object2ObjectOpenHashMap::new);
    }

    public static final class EitherCodec<F, S> implements Codec<Either<F, S>> {
        private final Codec<F> first;
        private final Codec<S> second;

        public EitherCodec(Codec<F> param0, Codec<S> param1) {
            this.first = param0;
            this.second = param1;
        }

        @Override
        public <T> DataResult<Pair<Either<F, S>, T>> decode(DynamicOps<T> param0, T param1) {
            DataResult<Pair<Either<F, S>, T>> var0 = this.first.decode(param0, param1).map(param0x -> param0x.mapFirst(Either::left));
            if (var0.error().isEmpty()) {
                return var0;
            } else {
                DataResult<Pair<Either<F, S>, T>> var1 = this.second.decode(param0, param1).map(param0x -> param0x.mapFirst(Either::right));
                return var1.error().isEmpty() ? var1 : var0.apply2((param0x, param1x) -> param1x, var1);
            }
        }

        public <T> DataResult<T> encode(Either<F, S> param0, DynamicOps<T> param1, T param2) {
            return param0.map(param2x -> this.first.encode(param2x, param1, param2), param2x -> this.second.encode(param2x, param1, param2));
        }

        @Override
        public boolean equals(Object param0) {
            if (this == param0) {
                return true;
            } else if (param0 != null && this.getClass() == param0.getClass()) {
                ExtraCodecs.EitherCodec<?, ?> var0 = (ExtraCodecs.EitherCodec)param0;
                return Objects.equals(this.first, var0.first) && Objects.equals(this.second, var0.second);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.first, this.second);
        }

        @Override
        public String toString() {
            return "EitherCodec[" + this.first + ", " + this.second + "]";
        }
    }

    static class RecursiveCodec<T> implements Codec<T> {
        private final Supplier<Codec<T>> wrapped;

        RecursiveCodec(Function<Codec<T>, Codec<T>> param0) {
            this.wrapped = Suppliers.memoize(() -> param0.apply(this));
        }

        @Override
        public <S> DataResult<Pair<T, S>> decode(DynamicOps<S> param0, S param1) {
            return this.wrapped.get().decode(param0, param1);
        }

        @Override
        public <S> DataResult<S> encode(T param0, DynamicOps<S> param1, S param2) {
            return this.wrapped.get().encode(param0, param1, param2);
        }

        @Override
        public String toString() {
            return "RecursiveCodec[" + this.wrapped + "]";
        }
    }

    static final class StrictOptionalFieldCodec<A> extends MapCodec<Optional<A>> {
        private final String name;
        private final Codec<A> elementCodec;

        public StrictOptionalFieldCodec(String param0, Codec<A> param1) {
            this.name = param0;
            this.elementCodec = param1;
        }

        @Override
        public <T> DataResult<Optional<A>> decode(DynamicOps<T> param0, MapLike<T> param1) {
            T var0 = param1.get(this.name);
            return var0 == null ? DataResult.success(Optional.empty()) : this.elementCodec.parse(param0, var0).map(Optional::of);
        }

        public <T> RecordBuilder<T> encode(Optional<A> param0, DynamicOps<T> param1, RecordBuilder<T> param2) {
            return param0.isPresent() ? param2.add(this.name, this.elementCodec.encodeStart(param1, param0.get())) : param2;
        }

        @Override
        public <T> Stream<T> keys(DynamicOps<T> param0) {
            return Stream.of(param0.createString(this.name));
        }

        @Override
        public boolean equals(Object param0) {
            if (this == param0) {
                return true;
            } else if (!(param0 instanceof ExtraCodecs.StrictOptionalFieldCodec)) {
                return false;
            } else {
                ExtraCodecs.StrictOptionalFieldCodec<?> var0 = (ExtraCodecs.StrictOptionalFieldCodec)param0;
                return Objects.equals(this.name, var0.name) && Objects.equals(this.elementCodec, var0.elementCodec);
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.name, this.elementCodec);
        }

        @Override
        public String toString() {
            return "StrictOptionalFieldCodec[" + this.name + ": " + this.elementCodec + "]";
        }
    }

    public static record StrictUnboundedMapCodec<K, V>(Codec<K> keyCodec, Codec<V> elementCodec) implements Codec<Map<K, V>>, BaseMapCodec<K, V> {
        @Override
        public <T> DataResult<Map<K, V>> decode(DynamicOps<T> param0, MapLike<T> param1) {
            Builder<K, V> var0 = ImmutableMap.builder();

            for(Pair<T, T> var1 : param1.entries().toList()) {
                DataResult<K> var2 = this.keyCodec().parse(param0, var1.getFirst());
                DataResult<V> var3 = this.elementCodec().parse(param0, var1.getSecond());
                DataResult<Pair<K, V>> var4 = var2.apply2stable(Pair::of, var3);
                if (var4.error().isPresent()) {
                    return DataResult.error(() -> {
                        PartialResult<Pair<K, V>> var0x = var4.error().get();
                        String var1x;
                        if (var2.result().isPresent()) {
                            var1x = "Map entry '" + var2.result().get() + "' : " + var0x.message();
                        } else {
                            var1x = var0x.message();
                        }

                        return var1x;
                    });
                }

                if (!var4.result().isPresent()) {
                    return DataResult.error(() -> "Empty or invalid map contents are not allowed");
                }

                Pair<K, V> var5 = var4.result().get();
                var0.put(var5.getFirst(), var5.getSecond());
            }

            Map<K, V> var6 = var0.build();
            return DataResult.success(var6);
        }

        @Override
        public <T> DataResult<Pair<Map<K, V>, T>> decode(DynamicOps<T> param0, T param1) {
            return param0.getMap(param1)
                .setLifecycle(Lifecycle.stable())
                .flatMap(param1x -> this.decode(param0, param1x))
                .map(param1x -> Pair.of(param1x, param1));
        }

        public <T> DataResult<T> encode(Map<K, V> param0, DynamicOps<T> param1, T param2) {
            return this.encode(param0, param1, param1.mapBuilder()).build(param2);
        }

        @Override
        public String toString() {
            return "StrictUnboundedMapCodec[" + this.keyCodec + " -> " + this.elementCodec + "]";
        }
    }

    public static record TagOrElementLocation(ResourceLocation id, boolean tag) {
        @Override
        public String toString() {
            return this.decoratedId();
        }

        private String decoratedId() {
            return this.tag ? "#" + this.id : this.id.toString();
        }
    }

    static record XorCodec<F, S>(Codec<F> first, Codec<S> second) implements Codec<Either<F, S>> {
        @Override
        public <T> DataResult<Pair<Either<F, S>, T>> decode(DynamicOps<T> param0, T param1) {
            DataResult<Pair<Either<F, S>, T>> var0 = this.first.decode(param0, param1).map(param0x -> param0x.mapFirst(Either::left));
            DataResult<Pair<Either<F, S>, T>> var1 = this.second.decode(param0, param1).map(param0x -> param0x.mapFirst(Either::right));
            Optional<Pair<Either<F, S>, T>> var2 = var0.result();
            Optional<Pair<Either<F, S>, T>> var3 = var1.result();
            if (var2.isPresent() && var3.isPresent()) {
                return DataResult.error(
                    () -> "Both alternatives read successfully, can not pick the correct one; first: " + var2.get() + " second: " + var3.get(), var2.get()
                );
            } else if (var2.isPresent()) {
                return var0;
            } else {
                return var3.isPresent() ? var1 : var0.apply2((param0x, param1x) -> param1x, var1);
            }
        }

        public <T> DataResult<T> encode(Either<F, S> param0, DynamicOps<T> param1, T param2) {
            return param0.map(param2x -> this.first.encode(param2x, param1, param2), param2x -> this.second.encode(param2x, param1, param2));
        }

        @Override
        public String toString() {
            return "XorCodec[" + this.first + ", " + this.second + "]";
        }
    }
}
