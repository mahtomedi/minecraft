package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FlowerBlock extends BushBlock implements SuspiciousEffectHolder {
    protected static final MapCodec<List<SuspiciousEffectHolder.EffectEntry>> EFFECTS_FIELD = SuspiciousEffectHolder.EffectEntry.LIST_CODEC
        .fieldOf("suspicious_stew_effects");
    public static final MapCodec<FlowerBlock> CODEC = RecordCodecBuilder.mapCodec(
        param0 -> param0.group(EFFECTS_FIELD.forGetter(FlowerBlock::getSuspiciousEffects), propertiesCodec()).apply(param0, FlowerBlock::new)
    );
    protected static final float AABB_OFFSET = 3.0F;
    protected static final VoxelShape SHAPE = Block.box(5.0, 0.0, 5.0, 11.0, 10.0, 11.0);
    private final List<SuspiciousEffectHolder.EffectEntry> suspiciousStewEffects;

    @Override
    public MapCodec<? extends FlowerBlock> codec() {
        return CODEC;
    }

    public FlowerBlock(MobEffect param0, int param1, BlockBehaviour.Properties param2) {
        this(makeEffectList(param0, param1), param2);
    }

    public FlowerBlock(List<SuspiciousEffectHolder.EffectEntry> param0, BlockBehaviour.Properties param1) {
        super(param1);
        this.suspiciousStewEffects = param0;
    }

    protected static List<SuspiciousEffectHolder.EffectEntry> makeEffectList(MobEffect param0, int param1) {
        int var0;
        if (param0.isInstantenous()) {
            var0 = param1;
        } else {
            var0 = param1 * 20;
        }

        return List.of(new SuspiciousEffectHolder.EffectEntry(param0, var0));
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        Vec3 var0 = param0.getOffset(param1, param2);
        return SHAPE.move(var0.x, var0.y, var0.z);
    }

    @Override
    public List<SuspiciousEffectHolder.EffectEntry> getSuspiciousEffects() {
        return this.suspiciousStewEffects;
    }
}
