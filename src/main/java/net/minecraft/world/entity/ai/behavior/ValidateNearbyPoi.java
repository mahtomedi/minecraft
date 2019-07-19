package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;

public class ValidateNearbyPoi extends Behavior<LivingEntity> {
    private final MemoryModuleType<GlobalPos> memoryType;
    private final Predicate<PoiType> poiPredicate;

    public ValidateNearbyPoi(PoiType param0, MemoryModuleType<GlobalPos> param1) {
        super(ImmutableMap.of(param1, MemoryStatus.VALUE_PRESENT));
        this.poiPredicate = param0.getPredicate();
        this.memoryType = param1;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel param0, LivingEntity param1) {
        GlobalPos var0 = param1.getBrain().getMemory(this.memoryType).get();
        return Objects.equals(param0.getDimension().getType(), var0.dimension()) && var0.pos().closerThan(param1.position(), 5.0);
    }

    @Override
    protected void start(ServerLevel param0, LivingEntity param1, long param2) {
        Brain<?> var0 = param1.getBrain();
        GlobalPos var1 = var0.getMemory(this.memoryType).get();
        ServerLevel var2 = param0.getServer().getLevel(var1.dimension());
        if (this.poiDoesntExist(var2, var1.pos()) || this.bedIsOccupied(var2, var1.pos(), param1)) {
            var0.eraseMemory(this.memoryType);
        }

    }

    private boolean bedIsOccupied(ServerLevel param0, BlockPos param1, LivingEntity param2) {
        BlockState var0 = param0.getBlockState(param1);
        return var0.getBlock().is(BlockTags.BEDS) && var0.getValue(BedBlock.OCCUPIED) && !param2.isSleeping();
    }

    private boolean poiDoesntExist(ServerLevel param0, BlockPos param1) {
        return !param0.getPoiManager().exists(param1, this.poiPredicate);
    }
}
