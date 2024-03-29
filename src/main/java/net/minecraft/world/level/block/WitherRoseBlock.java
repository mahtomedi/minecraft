package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WitherRoseBlock extends FlowerBlock {
    public static final MapCodec<WitherRoseBlock> CODEC = RecordCodecBuilder.mapCodec(
        param0 -> param0.group(EFFECTS_FIELD.forGetter(FlowerBlock::getSuspiciousEffects), propertiesCodec()).apply(param0, WitherRoseBlock::new)
    );

    @Override
    public MapCodec<WitherRoseBlock> codec() {
        return CODEC;
    }

    public WitherRoseBlock(MobEffect param0, int param1, BlockBehaviour.Properties param2) {
        this(makeEffectList(param0, param1), param2);
    }

    public WitherRoseBlock(List<SuspiciousEffectHolder.EffectEntry> param0, BlockBehaviour.Properties param1) {
        super(param0, param1);
    }

    @Override
    protected boolean mayPlaceOn(BlockState param0, BlockGetter param1, BlockPos param2) {
        return super.mayPlaceOn(param0, param1, param2) || param0.is(Blocks.NETHERRACK) || param0.is(Blocks.SOUL_SAND) || param0.is(Blocks.SOUL_SOIL);
    }

    @Override
    public void animateTick(BlockState param0, Level param1, BlockPos param2, RandomSource param3) {
        VoxelShape var0 = this.getShape(param0, param1, param2, CollisionContext.empty());
        Vec3 var1 = var0.bounds().getCenter();
        double var2 = (double)param2.getX() + var1.x;
        double var3 = (double)param2.getZ() + var1.z;

        for(int var4 = 0; var4 < 3; ++var4) {
            if (param3.nextBoolean()) {
                param1.addParticle(
                    ParticleTypes.SMOKE,
                    var2 + param3.nextDouble() / 5.0,
                    (double)param2.getY() + (0.5 - param3.nextDouble()),
                    var3 + param3.nextDouble() / 5.0,
                    0.0,
                    0.0,
                    0.0
                );
            }
        }

    }

    @Override
    public void entityInside(BlockState param0, Level param1, BlockPos param2, Entity param3) {
        if (!param1.isClientSide && param1.getDifficulty() != Difficulty.PEACEFUL) {
            if (param3 instanceof LivingEntity var0 && !var0.isInvulnerableTo(param1.damageSources().wither())) {
                var0.addEffect(new MobEffectInstance(MobEffects.WITHER, 40));
            }

        }
    }
}
