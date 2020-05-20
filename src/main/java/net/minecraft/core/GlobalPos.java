package net.minecraft.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.DimensionType;

public final class GlobalPos {
    public static final Codec<GlobalPos> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    DimensionType.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(GlobalPos::dimension),
                    BlockPos.CODEC.fieldOf("pos").forGetter(GlobalPos::pos)
                )
                .apply(param0, GlobalPos::of)
    );
    private final ResourceKey<DimensionType> dimension;
    private final BlockPos pos;

    private GlobalPos(ResourceKey<DimensionType> param0, BlockPos param1) {
        this.dimension = param0;
        this.pos = param1;
    }

    public static GlobalPos of(ResourceKey<DimensionType> param0, BlockPos param1) {
        return new GlobalPos(param0, param1);
    }

    public ResourceKey<DimensionType> dimension() {
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
    public String toString() {
        return this.dimension.toString() + " " + this.pos;
    }
}
