package net.minecraft.world.level.material;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public final class FluidState extends StateHolder<Fluid, FluidState> {
    public static final Codec<FluidState> CODEC = codec(Registry.FLUID, Fluid::defaultFluidState).stable();

    public FluidState(Fluid param0, ImmutableMap<Property<?>, Comparable<?>> param1, MapCodec<FluidState> param2) {
        super(param0, param1, param2);
    }

    public Fluid getType() {
        return this.owner;
    }

    public boolean isSource() {
        return this.getType().isSource(this);
    }

    public boolean isSourceOfType(Fluid param0) {
        return this.owner == param0 && this.owner.isSource(this);
    }

    public boolean isEmpty() {
        return this.getType().isEmpty();
    }

    public float getHeight(BlockGetter param0, BlockPos param1) {
        return this.getType().getHeight(this, param0, param1);
    }

    public float getOwnHeight() {
        return this.getType().getOwnHeight(this);
    }

    public int getAmount() {
        return this.getType().getAmount(this);
    }

    @OnlyIn(Dist.CLIENT)
    public boolean shouldRenderBackwardUpFace(BlockGetter param0, BlockPos param1) {
        for(int var0 = -1; var0 <= 1; ++var0) {
            for(int var1 = -1; var1 <= 1; ++var1) {
                BlockPos var2 = param1.offset(var0, 0, var1);
                FluidState var3 = param0.getFluidState(var2);
                if (!var3.getType().isSame(this.getType()) && !param0.getBlockState(var2).isSolidRender(param0, var2)) {
                    return true;
                }
            }
        }

        return false;
    }

    public void tick(Level param0, BlockPos param1) {
        this.getType().tick(param0, param1, this);
    }

    @OnlyIn(Dist.CLIENT)
    public void animateTick(Level param0, BlockPos param1, Random param2) {
        this.getType().animateTick(param0, param1, this, param2);
    }

    public boolean isRandomlyTicking() {
        return this.getType().isRandomlyTicking();
    }

    public void randomTick(Level param0, BlockPos param1, Random param2) {
        this.getType().randomTick(param0, param1, this, param2);
    }

    public Vec3 getFlow(BlockGetter param0, BlockPos param1) {
        return this.getType().getFlow(param0, param1, this);
    }

    public BlockState createLegacyBlock() {
        return this.getType().createLegacyBlock(this);
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    public ParticleOptions getDripParticle() {
        return this.getType().getDripParticle();
    }

    public boolean is(Tag<Fluid> param0) {
        return this.getType().is(param0);
    }

    public float getExplosionResistance() {
        return this.getType().getExplosionResistance();
    }

    public boolean canBeReplacedWith(BlockGetter param0, BlockPos param1, Fluid param2, Direction param3) {
        return this.getType().canBeReplacedWith(this, param0, param1, param2, param3);
    }

    public VoxelShape getShape(BlockGetter param0, BlockPos param1) {
        return this.getType().getShape(this, param0, param1);
    }
}
