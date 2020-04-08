package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class ReplaceSpheroidConfiguration implements FeatureConfiguration {
    public final BlockState targetState;
    public final BlockState replaceState;
    public final Vec3i minimumReach;
    public final Vec3i maximumReach;

    public ReplaceSpheroidConfiguration(BlockState param0, BlockState param1, Vec3i param2, Vec3i param3) {
        this.targetState = param0;
        this.replaceState = param1;
        this.minimumReach = param2;
        this.maximumReach = param3;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        ImmutableMap.Builder<T, T> var0 = ImmutableMap.builder();
        var0.put(param0.createString("target"), BlockState.serialize(param0, this.targetState).getValue());
        var0.put(param0.createString("state"), BlockState.serialize(param0, this.replaceState).getValue());
        var0.put(param0.createString("minimum_reach_x"), param0.createInt(this.minimumReach.getX()));
        var0.put(param0.createString("minimum_reach_y"), param0.createInt(this.minimumReach.getY()));
        var0.put(param0.createString("minimum_reach_z"), param0.createInt(this.minimumReach.getZ()));
        var0.put(param0.createString("maximum_reach_x"), param0.createInt(this.maximumReach.getX()));
        var0.put(param0.createString("maximum_reach_y"), param0.createInt(this.maximumReach.getY()));
        var0.put(param0.createString("maximum_reach_z"), param0.createInt(this.maximumReach.getZ()));
        return new Dynamic<>(param0, param0.createMap(var0.build()));
    }

    public static <T> ReplaceSpheroidConfiguration deserialize(Dynamic<T> param0) {
        BlockState var0 = param0.get("target").map(BlockState::deserialize).orElse(Blocks.AIR.defaultBlockState());
        BlockState var1 = param0.get("state").map(BlockState::deserialize).orElse(Blocks.AIR.defaultBlockState());
        int var2 = param0.get("minimum_reach_x").asInt(0);
        int var3 = param0.get("minimum_reach_y").asInt(0);
        int var4 = param0.get("minimum_reach_z").asInt(0);
        int var5 = param0.get("maximum_reach_x").asInt(0);
        int var6 = param0.get("maximum_reach_y").asInt(0);
        int var7 = param0.get("maximum_reach_z").asInt(0);
        return new ReplaceSpheroidConfiguration(var0, var1, new Vec3i(var2, var3, var4), new Vec3i(var5, var6, var7));
    }

    public static class Builder {
        private BlockState target = Blocks.AIR.defaultBlockState();
        private BlockState state = Blocks.AIR.defaultBlockState();
        private Vec3i minimumReach = Vec3i.ZERO;
        private Vec3i maximumReach = Vec3i.ZERO;

        public ReplaceSpheroidConfiguration.Builder targetBlockState(BlockState param0) {
            this.target = param0;
            return this;
        }

        public ReplaceSpheroidConfiguration.Builder replaceWithBlockState(BlockState param0) {
            this.state = param0;
            return this;
        }

        public ReplaceSpheroidConfiguration.Builder minimumReach(Vec3i param0) {
            this.minimumReach = param0;
            return this;
        }

        public ReplaceSpheroidConfiguration.Builder maximumReach(Vec3i param0) {
            this.maximumReach = param0;
            return this;
        }

        public ReplaceSpheroidConfiguration build() {
            if (this.minimumReach.getX() >= 0 && this.minimumReach.getY() >= 0 && this.minimumReach.getZ() >= 0) {
                if (this.minimumReach.getX() <= this.maximumReach.getX()
                    && this.minimumReach.getY() <= this.maximumReach.getY()
                    && this.minimumReach.getZ() <= this.maximumReach.getZ()) {
                    return new ReplaceSpheroidConfiguration(this.target, this.state, this.minimumReach, this.maximumReach);
                } else {
                    throw new IllegalArgumentException("Maximum reach must be greater than minimum reach for each axis");
                }
            } else {
                throw new IllegalArgumentException("Minimum reach cannot be less than zero");
            }
        }
    }
}
