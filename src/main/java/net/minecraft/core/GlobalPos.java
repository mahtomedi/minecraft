package net.minecraft.core;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Objects;
import net.minecraft.util.Serializable;
import net.minecraft.world.level.dimension.DimensionType;

public final class GlobalPos implements Serializable {
    private final DimensionType dimension;
    private final BlockPos pos;

    private GlobalPos(DimensionType param0, BlockPos param1) {
        this.dimension = param0;
        this.pos = param1;
    }

    public static GlobalPos of(DimensionType param0, BlockPos param1) {
        return new GlobalPos(param0, param1);
    }

    public static GlobalPos of(Dynamic<?> param0) {
        return param0.get("dimension")
            .map(DimensionType::of)
            .flatMap(param1 -> param0.get("pos").map(BlockPos::deserialize).map(param1x -> new GlobalPos(param1, param1x)))
            .orElseThrow(() -> new IllegalArgumentException("Could not parse GlobalPos"));
    }

    public DimensionType dimension() {
        return this.dimension;
    }

    public BlockPos pos() {
        return this.pos;
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (param0 != null && this.getClass() == param0.getClass()) {
            GlobalPos var0 = (GlobalPos)param0;
            return Objects.equals(this.dimension, var0.dimension) && Objects.equals(this.pos, var0.pos);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.dimension, this.pos);
    }

    @Override
    public <T> T serialize(DynamicOps<T> param0) {
        return param0.createMap(
            ImmutableMap.of(param0.createString("dimension"), this.dimension.serialize(param0), param0.createString("pos"), this.pos.serialize(param0))
        );
    }

    @Override
    public String toString() {
        return this.dimension.toString() + " " + this.pos;
    }
}
