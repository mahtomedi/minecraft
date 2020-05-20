package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class ReplaceSpheroidConfiguration implements FeatureConfiguration {
    public static final Codec<ReplaceSpheroidConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    BlockState.CODEC.fieldOf("target").forGetter(param0x -> param0x.targetState),
                    BlockState.CODEC.fieldOf("state").forGetter(param0x -> param0x.replaceState),
                    Vec3i.CODEC.fieldOf("minimum_reach").forGetter(param0x -> param0x.minimumReach),
                    Vec3i.CODEC.fieldOf("maximum_reach").forGetter(param0x -> param0x.maximumReach)
                )
                .apply(param0, ReplaceSpheroidConfiguration::new)
    );
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
