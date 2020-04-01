package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;

public class ShapeConfiguration implements FeatureConfiguration {
    public final BlockStateProvider material;
    public final ShapeConfiguration.Metric metric;
    public final float radiusMin;
    public final float radiusMax;

    public ShapeConfiguration(BlockStateProvider param0, ShapeConfiguration.Metric param1, float param2, float param3) {
        this.material = param0;
        this.metric = param1;
        this.radiusMin = param2;
        this.radiusMax = param3;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(
            param0,
            param0.createMap(
                ImmutableMap.of(
                    param0.createString("metric"),
                    param0.createString(this.metric.name),
                    param0.createString("material"),
                    this.material.serialize(param0),
                    param0.createString("radiusMin"),
                    param0.createFloat(this.radiusMin),
                    param0.createString("radiusMax"),
                    param0.createFloat(this.radiusMax)
                )
            )
        );
    }

    public static <T> ShapeConfiguration deserialize(Dynamic<T> param0) {
        ShapeConfiguration.Metric var0 = param0.get("metric").asString().map(ShapeConfiguration.Metric::fromId).get();
        BlockStateProvider var1 = param0.get("material")
            .map(
                param0x -> {
                    ResourceLocation var0x = param0x.get("type").asString().map(ResourceLocation::new).get();
                    BlockStateProviderType<?> var1x = Registry.BLOCKSTATE_PROVIDER_TYPES
                        .getOptional(var0x)
                        .orElseThrow(() -> new IllegalStateException(var0x.toString()));
                    return var1x.deserialize(param0x);
                }
            )
            .orElseThrow(IllegalStateException::new);
        float var2 = param0.get("radiusMin").asFloat(0.0F);
        float var3 = param0.get("radiusMax").asFloat(0.0F);
        return new ShapeConfiguration(var1, var0, var2, var3);
    }

    public static ShapeConfiguration random(Random param0) {
        ShapeConfiguration.Metric var0 = Util.randomObject(param0, ShapeConfiguration.Metric.values());
        BlockStateProvider var1 = BlockStateProvider.random(param0);
        float var2 = 1.0F + param0.nextFloat() * 5.0F;
        float var3 = Math.min(var2 + param0.nextFloat() * 10.0F, 15.0F);
        return new ShapeConfiguration(var1, var0, var2, var3);
    }

    public static enum Metric {
        EUCLIDIAN("euclidian") {
            @Override
            public float distance(BlockPos param0, BlockPos param1) {
                return Mth.sqrt(param0.distSqr(param1));
            }
        },
        TAXICAB("taxicab") {
            @Override
            public float distance(BlockPos param0, BlockPos param1) {
                return (float)param0.distManhattan(param1);
            }
        },
        CHESSBOARD("chessboard") {
            @Override
            public float distance(BlockPos param0, BlockPos param1) {
                float var0 = (float)Math.abs(param1.getX() - param0.getX());
                float var1 = (float)Math.abs(param1.getY() - param0.getY());
                float var2 = (float)Math.abs(param1.getZ() - param0.getZ());
                return Math.max(var0, Math.max(var1, var2));
            }
        };

        private final String name;

        private Metric(String param0) {
            this.name = param0;
        }

        public abstract float distance(BlockPos var1, BlockPos var2);

        public static ShapeConfiguration.Metric fromId(String param0) {
            return Stream.of(values()).filter(param1 -> param1.name.equals(param0)).findFirst().orElseThrow(() -> new IllegalStateException(param0));
        }
    }
}
