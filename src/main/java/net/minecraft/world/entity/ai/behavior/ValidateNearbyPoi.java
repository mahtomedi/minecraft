package net.minecraft.world.entity.ai.behavior;

import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;

public class ValidateNearbyPoi {
    private static final int MAX_DISTANCE = 16;

    public static BehaviorControl<LivingEntity> create(Predicate<Holder<PoiType>> param0, MemoryModuleType<GlobalPos> param1) {
        return BehaviorBuilder.create(param2 -> param2.<MemoryAccessor>group(param2.present(param1)).apply(param2, param2x -> (param3, param4, param5) -> {
                    GlobalPos var0x = param2.get(param2x);
                    BlockPos var1x = var0x.pos();
                    if (param3.dimension() == var0x.dimension() && var1x.closerToCenterThan(param4.position(), 16.0)) {
                        ServerLevel var2x = param3.getServer().getLevel(var0x.dimension());
                        if (var2x == null || !var2x.getPoiManager().exists(var1x, param0)) {
                            param2x.erase();
                        } else if (bedIsOccupied(var2x, var1x, param4)) {
                            param2x.erase();
                            param3.getPoiManager().release(var1x);
                            DebugPackets.sendPoiTicketCountPacket(param3, var1x);
                        }

                        return true;
                    } else {
                        return false;
                    }
                }));
    }

    private static boolean bedIsOccupied(ServerLevel param0, BlockPos param1, LivingEntity param2) {
        BlockState var0 = param0.getBlockState(param1);
        return var0.is(BlockTags.BEDS) && var0.getValue(BedBlock.OCCUPIED) && !param2.isSleeping();
    }
}
