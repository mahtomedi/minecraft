package net.minecraft.world.level.block;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class TorchBlock extends BaseTorchBlock {
    protected static final MapCodec<SimpleParticleType> PARTICLE_OPTIONS_FIELD = BuiltInRegistries.PARTICLE_TYPE
        .byNameCodec()
        .comapFlatMap(
            param0 -> param0 instanceof SimpleParticleType var0 ? DataResult.success(var0) : DataResult.error(() -> "Not a SimpleParticleType: " + param0),
            (Function<? super SimpleParticleType, ? extends SimpleParticleType>)(param0 -> param0)
        )
        .fieldOf("particle_options");
    public static final MapCodec<TorchBlock> CODEC = RecordCodecBuilder.mapCodec(
        param0 -> param0.group(PARTICLE_OPTIONS_FIELD.forGetter(param0x -> param0x.flameParticle), propertiesCodec()).apply(param0, TorchBlock::new)
    );
    protected final SimpleParticleType flameParticle;

    @Override
    public MapCodec<? extends TorchBlock> codec() {
        return CODEC;
    }

    protected TorchBlock(SimpleParticleType param0, BlockBehaviour.Properties param1) {
        super(param1);
        this.flameParticle = param0;
    }

    @Override
    public void animateTick(BlockState param0, Level param1, BlockPos param2, RandomSource param3) {
        double var0 = (double)param2.getX() + 0.5;
        double var1 = (double)param2.getY() + 0.7;
        double var2 = (double)param2.getZ() + 0.5;
        param1.addParticle(ParticleTypes.SMOKE, var0, var1, var2, 0.0, 0.0, 0.0);
        param1.addParticle(this.flameParticle, var0, var1, var2, 0.0, 0.0, 0.0);
    }
}
