package net.minecraft.util.datafix.fixes;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.List.ListType;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.apache.commons.lang3.mutable.MutableInt;

public class ChunkProtoTickListFix extends DataFix {
    private static final int SECTION_WIDTH = 16;
    private static final ImmutableSet<String> ALWAYS_WATERLOGGED = ImmutableSet.of(
        "minecraft:bubble_column", "minecraft:kelp", "minecraft:kelp_plant", "minecraft:seagrass", "minecraft:tall_seagrass"
    );

    public ChunkProtoTickListFix(Schema param0) {
        super(param0, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> var0 = this.getInputSchema().getType(References.CHUNK);
        OpticFinder<?> var1 = var0.findField("Level");
        OpticFinder<?> var2 = var1.type().findField("Sections");
        OpticFinder<?> var3 = ((ListType)var2.type()).getElement().finder();
        OpticFinder<?> var4 = var3.type().findField("block_states");
        OpticFinder<?> var5 = var3.type().findField("biomes");
        OpticFinder<?> var6 = var4.type().findField("palette");
        OpticFinder<?> var7 = var1.type().findField("TileTicks");
        return this.fixTypeEverywhereTyped(
            "ChunkProtoTickListFix",
            var0,
            param7 -> param7.updateTyped(
                    var1,
                    param6x -> {
                        param6x = param6x.update(
                            DSL.remainderFinder(),
                            param0x -> DataFixUtils.orElse(
                                    param0x.get("LiquidTicks").result().map(param1x -> param0x.set("fluid_ticks", param1x).remove("LiquidTicks")), param0x
                                )
                        );
                        Dynamic<?> var0x = param6x.get(DSL.remainderFinder());
                        MutableInt var1x = new MutableInt();
                        Int2ObjectMap<Supplier<ChunkProtoTickListFix.PoorMansPalettedContainer>> var2x = new Int2ObjectArrayMap<>();
                        param6x.getOptionalTyped(var2)
                            .ifPresent(
                                param6xx -> param6xx.getAllTyped(var3)
                                        .forEach(
                                            param5x -> {
                                                Dynamic<?> var0xx = param5x.get(DSL.remainderFinder());
                                                int var1xx = var0xx.get("Y").asInt(Integer.MAX_VALUE);
                                                if (var1xx != Integer.MAX_VALUE) {
                                                    if (param5x.getOptionalTyped(var5).isPresent()) {
                                                        var1x.setValue(Math.min(var1xx, var1x.getValue()));
                                                    }
                        
                                                    param5x.getOptionalTyped(var4)
                                                        .ifPresent(
                                                            param3x -> var2x.put(
                                                                    var1xx,
                                                                    Suppliers.memoize(
                                                                        () -> {
                                                                            List<? extends Dynamic<?>> var0xxx = param3x.getOptionalTyped(var6)
                                                                                .map(
                                                                                    param0x -> (List)param0x.write()
                                                                                            .result()
                                                                                            .map(param0xx -> param0xx.asList(Function.identity()))
                                                                                            .orElse(Collections.emptyList())
                                                                                )
                                                                                .orElse(Collections.emptyList());
                                                                            long[] var1xxx = param3x.get(DSL.remainderFinder())
                                                                                .get("data")
                                                                                .asLongStream()
                                                                                .toArray();
                                                                            return new ChunkProtoTickListFix.PoorMansPalettedContainer(var0xxx, var1xxx);
                                                                        }
                                                                    )
                                                                )
                                                        );
                                                }
                                            }
                                        )
                            );
                        byte var3x = var1x.getValue().byteValue();
                        param6x = param6x.update(DSL.remainderFinder(), param1x -> param1x.update("yPos", param1xx -> param1xx.createByte(var3x)));
                        if (!param6x.getOptionalTyped(var7).isPresent() && !var0x.get("fluid_ticks").result().isPresent()) {
                            int var4x = var0x.get("xPos").asInt(0);
                            int var5x = var0x.get("zPos").asInt(0);
                            Dynamic<?> var6x = this.makeTickList(var0x, var2x, var3x, var4x, var5x, "LiquidsToBeTicked", ChunkProtoTickListFix::getLiquid);
                            Dynamic<?> var7x = this.makeTickList(var0x, var2x, var3x, var4x, var5x, "ToBeTicked", ChunkProtoTickListFix::getBlock);
                            Optional<? extends Pair<? extends Typed<?>, ?>> var8x = var7.type().readTyped(var7x).result();
                            if (var8x.isPresent()) {
                                param6x = param6x.set(var7, (Typed)((Pair)var8x.get()).getFirst());
                            }
        
                            return param6x.update(
                                DSL.remainderFinder(), param1x -> param1x.remove("ToBeTicked").remove("LiquidsToBeTicked").set("fluid_ticks", var6x)
                            );
                        } else {
                            return param6x;
                        }
                    }
                )
        );
    }

    private Dynamic<?> makeTickList(
        Dynamic<?> param0,
        Int2ObjectMap<Supplier<ChunkProtoTickListFix.PoorMansPalettedContainer>> param1,
        byte param2,
        int param3,
        int param4,
        String param5,
        Function<Dynamic<?>, String> param6
    ) {
        Stream<Dynamic<?>> var0 = Stream.empty();
        List<? extends Dynamic<?>> var1 = param0.get(param5).asList(Function.identity());

        for(int var2 = 0; var2 < var1.size(); ++var2) {
            int var3 = var2 + param2;
            Supplier<ChunkProtoTickListFix.PoorMansPalettedContainer> var4 = param1.get(var3);
            Stream<? extends Dynamic<?>> var5 = var1.get(var2)
                .asStream()
                .mapToInt(param0x -> param0x.asShort((short)-1))
                .filter(param0x -> param0x > 0)
                .mapToObj(param6x -> this.createTick(param0, var4, param3, var3, param4, param6x, param6));
            var0 = Stream.concat(var0, var5);
        }

        return param0.createList(var0);
    }

    private static String getBlock(@Nullable Dynamic<?> param0) {
        return param0 != null ? param0.get("Name").asString("minecraft:air") : "minecraft:air";
    }

    private static String getLiquid(@Nullable Dynamic<?> param0) {
        if (param0 == null) {
            return "minecraft:empty";
        } else {
            String var0 = param0.get("Name").asString("");
            if ("minecraft:water".equals(var0)) {
                return param0.get("Properties").get("level").asInt(0) == 0 ? "minecraft:water" : "minecraft:flowing_water";
            } else if ("minecraft:lava".equals(var0)) {
                return param0.get("Properties").get("level").asInt(0) == 0 ? "minecraft:lava" : "minecraft:flowing_lava";
            } else {
                return !ALWAYS_WATERLOGGED.contains(var0) && !param0.get("Properties").get("waterlogged").asBoolean(false)
                    ? "minecraft:empty"
                    : "minecraft:water";
            }
        }
    }

    private Dynamic<?> createTick(
        Dynamic<?> param0,
        @Nullable Supplier<ChunkProtoTickListFix.PoorMansPalettedContainer> param1,
        int param2,
        int param3,
        int param4,
        int param5,
        Function<Dynamic<?>, String> param6
    ) {
        int var0 = param5 & 15;
        int var1 = param5 >>> 4 & 15;
        int var2 = param5 >>> 8 & 15;
        String var3 = param6.apply(param1 != null ? param1.get().get(var0, var1, var2) : null);
        return param0.createMap(
            ImmutableMap.builder()
                .put(param0.createString("i"), param0.createString(var3))
                .put(param0.createString("x"), param0.createInt(param2 * 16 + var0))
                .put(param0.createString("y"), param0.createInt(param3 * 16 + var1))
                .put(param0.createString("z"), param0.createInt(param4 * 16 + var2))
                .put(param0.createString("t"), param0.createInt(0))
                .put(param0.createString("p"), param0.createInt(0))
                .build()
        );
    }

    public static final class PoorMansPalettedContainer {
        private static final long SIZE_BITS = 4L;
        private final List<? extends Dynamic<?>> palette;
        private final long[] data;
        private final int bits;
        private final long mask;
        private final int valuesPerLong;

        public PoorMansPalettedContainer(List<? extends Dynamic<?>> param0, long[] param1) {
            this.palette = param0;
            this.data = param1;
            this.bits = Math.max(4, ChunkHeightAndBiomeFix.ceillog2(param0.size()));
            this.mask = (1L << this.bits) - 1L;
            this.valuesPerLong = (char)(64 / this.bits);
        }

        @Nullable
        public Dynamic<?> get(int param0, int param1, int param2) {
            int var0 = this.palette.size();
            if (var0 < 1) {
                return null;
            } else if (var0 == 1) {
                return this.palette.get(0);
            } else {
                int var1 = this.getIndex(param0, param1, param2);
                int var2 = var1 / this.valuesPerLong;
                if (var2 >= 0 && var2 < this.data.length) {
                    long var3 = this.data[var2];
                    int var4 = (var1 - var2 * this.valuesPerLong) * this.bits;
                    int var5 = (int)(var3 >> var4 & this.mask);
                    return var5 >= 0 && var5 < var0 ? this.palette.get(var5) : null;
                } else {
                    return null;
                }
            }
        }

        private int getIndex(int param0, int param1, int param2) {
            return (param1 << 4 | param2) << 4 | param0;
        }

        public List<? extends Dynamic<?>> palette() {
            return this.palette;
        }

        public long[] data() {
            return this.data;
        }
    }
}
