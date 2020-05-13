package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;

public class InteractWithDoor extends Behavior<LivingEntity> {
    public InteractWithDoor() {
        super(
            ImmutableMap.of(
                MemoryModuleType.PATH,
                MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.INTERACTABLE_DOORS,
                MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.OPENED_DOORS,
                MemoryStatus.REGISTERED
            )
        );
    }

    @Override
    protected void start(ServerLevel param0, LivingEntity param1, long param2) {
        Brain<?> var0 = param1.getBrain();
        Path var1 = var0.getMemory(MemoryModuleType.PATH).get();
        List<GlobalPos> var2 = var0.getMemory(MemoryModuleType.INTERACTABLE_DOORS).get();
        List<BlockPos> var3 = var1.getNodes().stream().map(param0x -> new BlockPos(param0x.x, param0x.y, param0x.z)).collect(Collectors.toList());
        Set<BlockPos> var4 = this.getDoorsThatAreOnMyPath(param0, var2, var3);
        int var5 = var1.getIndex() - 1;
        this.openOrCloseDoors(param0, var3, var4, var5, param1, var0);
    }

    private Set<BlockPos> getDoorsThatAreOnMyPath(ServerLevel param0, List<GlobalPos> param1, List<BlockPos> param2) {
        return param1.stream()
            .filter(param1x -> param1x.dimension() == param0.dimensionType())
            .map(GlobalPos::pos)
            .filter(param2::contains)
            .collect(Collectors.toSet());
    }

    private void openOrCloseDoors(ServerLevel param0, List<BlockPos> param1, Set<BlockPos> param2, int param3, LivingEntity param4, Brain<?> param5) {
        param2.forEach(param4x -> {
            int var0 = param1.indexOf(param4x);
            BlockState var1x = param0.getBlockState(param4x);
            Block var2x = var1x.getBlock();
            if (BlockTags.WOODEN_DOORS.contains(var2x) && var2x instanceof DoorBlock) {
                boolean var3x = var0 >= param3;
                ((DoorBlock)var2x).setOpen(param0, param4x, var3x);
                GlobalPos var4x = GlobalPos.of(param0.dimensionType(), param4x);
                if (!param5.getMemory(MemoryModuleType.OPENED_DOORS).isPresent() && var3x) {
                    param5.setMemory(MemoryModuleType.OPENED_DOORS, Sets.newHashSet(var4x));
                } else {
                    param5.getMemory(MemoryModuleType.OPENED_DOORS).ifPresent(param2x -> {
                        if (var3x) {
                            param2x.add(var4x);
                        } else {
                            param2x.remove(var4x);
                        }

                    });
                }
            }

        });
        closeAllOpenedDoors(param0, param1, param3, param4, param5);
    }

    public static void closeAllOpenedDoors(ServerLevel param0, List<BlockPos> param1, int param2, LivingEntity param3, Brain<?> param4) {
        param4.getMemory(MemoryModuleType.OPENED_DOORS).ifPresent(param4x -> {
            Iterator<GlobalPos> var0x = param4x.iterator();

            while(var0x.hasNext()) {
                GlobalPos var1x = (GlobalPos)var0x.next();
                BlockPos var2x = var1x.pos();
                int var3x = param1.indexOf(var2x);
                if (param0.dimensionType() != var1x.dimension()) {
                    var0x.remove();
                } else {
                    BlockState var4x = param0.getBlockState(var2x);
                    Block var5 = var4x.getBlock();
                    if (BlockTags.WOODEN_DOORS.contains(var5) && var5 instanceof DoorBlock && var3x < param2 && var2x.closerThan(param3.position(), 4.0)) {
                        ((DoorBlock)var5).setOpen(param0, var2x, false);
                        var0x.remove();
                    }
                }
            }

        });
    }
}
