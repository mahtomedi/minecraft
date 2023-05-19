package net.minecraft.world.damagesource;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public record FallLocation(String id) {
    public static final FallLocation GENERIC = new FallLocation("generic");
    public static final FallLocation LADDER = new FallLocation("ladder");
    public static final FallLocation VINES = new FallLocation("vines");
    public static final FallLocation WEEPING_VINES = new FallLocation("weeping_vines");
    public static final FallLocation TWISTING_VINES = new FallLocation("twisting_vines");
    public static final FallLocation SCAFFOLDING = new FallLocation("scaffolding");
    public static final FallLocation OTHER_CLIMBABLE = new FallLocation("other_climbable");
    public static final FallLocation WATER = new FallLocation("water");

    public static FallLocation blockToFallLocation(BlockState param0) {
        if (param0.is(Blocks.LADDER) || param0.is(BlockTags.TRAPDOORS)) {
            return LADDER;
        } else if (param0.is(Blocks.VINE)) {
            return VINES;
        } else if (param0.is(Blocks.WEEPING_VINES) || param0.is(Blocks.WEEPING_VINES_PLANT)) {
            return WEEPING_VINES;
        } else if (param0.is(Blocks.TWISTING_VINES) || param0.is(Blocks.TWISTING_VINES_PLANT)) {
            return TWISTING_VINES;
        } else {
            return param0.is(Blocks.SCAFFOLDING) ? SCAFFOLDING : OTHER_CLIMBABLE;
        }
    }

    @Nullable
    public static FallLocation getCurrentFallLocation(LivingEntity param0) {
        Optional<BlockPos> var0 = param0.getLastClimbablePos();
        if (var0.isPresent()) {
            BlockState var1 = param0.level().getBlockState(var0.get());
            return blockToFallLocation(var1);
        } else {
            return param0.isInWater() ? WATER : null;
        }
    }

    public String languageKey() {
        return "death.fell.accident." + this.id;
    }
}
