package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;

public class InteractWithDoor extends Behavior<LivingEntity> {
    private static final int COOLDOWN_BEFORE_RERUNNING_IN_SAME_NODE = 20;
    private static final double SKIP_CLOSING_DOOR_IF_FURTHER_AWAY_THAN = 2.0;
    private static final double MAX_DISTANCE_TO_HOLD_DOOR_OPEN_FOR_OTHER_MOBS = 2.0;
    @Nullable
    private Node lastCheckedNode;
    private int remainingCooldown;

    public InteractWithDoor() {
        super(ImmutableMap.of(MemoryModuleType.PATH, MemoryStatus.VALUE_PRESENT, MemoryModuleType.DOORS_TO_CLOSE, MemoryStatus.REGISTERED));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel param0, LivingEntity param1) {
        Path var0 = param1.getBrain().getMemory(MemoryModuleType.PATH).get();
        if (!var0.notStarted() && !var0.isDone()) {
            if (!Objects.equals(this.lastCheckedNode, var0.getNextNode())) {
                this.remainingCooldown = 20;
                return true;
            } else {
                if (this.remainingCooldown > 0) {
                    --this.remainingCooldown;
                }

                return this.remainingCooldown == 0;
            }
        } else {
            return false;
        }
    }

    @Override
    protected void start(ServerLevel param0, LivingEntity param1, long param2) {
        Path var0 = param1.getBrain().getMemory(MemoryModuleType.PATH).get();
        this.lastCheckedNode = var0.getNextNode();
        Node var1 = var0.getPreviousNode();
        Node var2 = var0.getNextNode();
        BlockPos var3 = var1.asBlockPos();
        BlockState var4 = param0.getBlockState(var3);
        if (var4.is(BlockTags.WOODEN_DOORS)) {
            DoorBlock var5 = (DoorBlock)var4.getBlock();
            if (!var5.isOpen(var4)) {
                var5.setOpen(param1, param0, var4, var3, true);
            }

            this.rememberDoorToClose(param0, param1, var3);
        }

        BlockPos var6 = var2.asBlockPos();
        BlockState var7 = param0.getBlockState(var6);
        if (var7.is(BlockTags.WOODEN_DOORS)) {
            DoorBlock var8 = (DoorBlock)var7.getBlock();
            if (!var8.isOpen(var7)) {
                var8.setOpen(param1, param0, var7, var6, true);
                this.rememberDoorToClose(param0, param1, var6);
            }
        }

        closeDoorsThatIHaveOpenedOrPassedThrough(param0, param1, var1, var2);
    }

    public static void closeDoorsThatIHaveOpenedOrPassedThrough(ServerLevel param0, LivingEntity param1, @Nullable Node param2, @Nullable Node param3) {
        Brain<?> var0 = param1.getBrain();
        if (var0.hasMemoryValue(MemoryModuleType.DOORS_TO_CLOSE)) {
            Iterator<GlobalPos> var1 = var0.getMemory(MemoryModuleType.DOORS_TO_CLOSE).get().iterator();

            while(var1.hasNext()) {
                GlobalPos var2 = var1.next();
                BlockPos var3 = var2.pos();
                if ((param2 == null || !param2.asBlockPos().equals(var3)) && (param3 == null || !param3.asBlockPos().equals(var3))) {
                    if (isDoorTooFarAway(param0, param1, var2)) {
                        var1.remove();
                    } else {
                        BlockState var4 = param0.getBlockState(var3);
                        if (!var4.is(BlockTags.WOODEN_DOORS)) {
                            var1.remove();
                        } else {
                            DoorBlock var5 = (DoorBlock)var4.getBlock();
                            if (!var5.isOpen(var4)) {
                                var1.remove();
                            } else if (areOtherMobsComingThroughDoor(param0, param1, var3)) {
                                var1.remove();
                            } else {
                                var5.setOpen(param1, param0, var4, var3, false);
                                var1.remove();
                            }
                        }
                    }
                }
            }
        }

    }

    private static boolean areOtherMobsComingThroughDoor(ServerLevel param0, LivingEntity param1, BlockPos param2) {
        Brain<?> var0 = param1.getBrain();
        return !var0.hasMemoryValue(MemoryModuleType.NEAREST_LIVING_ENTITIES)
            ? false
            : var0.getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES)
                .get()
                .stream()
                .filter(param1x -> param1x.getType() == param1.getType())
                .filter(param1x -> param2.closerThan(param1x.position(), 2.0))
                .anyMatch(param2x -> isMobComingThroughDoor(param0, param2x, param2));
    }

    private static boolean isMobComingThroughDoor(ServerLevel param0, LivingEntity param1, BlockPos param2) {
        if (!param1.getBrain().hasMemoryValue(MemoryModuleType.PATH)) {
            return false;
        } else {
            Path var0 = param1.getBrain().getMemory(MemoryModuleType.PATH).get();
            if (var0.isDone()) {
                return false;
            } else {
                Node var1 = var0.getPreviousNode();
                if (var1 == null) {
                    return false;
                } else {
                    Node var2 = var0.getNextNode();
                    return param2.equals(var1.asBlockPos()) || param2.equals(var2.asBlockPos());
                }
            }
        }
    }

    private static boolean isDoorTooFarAway(ServerLevel param0, LivingEntity param1, GlobalPos param2) {
        return param2.dimension() != param0.dimension() || !param2.pos().closerThan(param1.position(), 2.0);
    }

    private void rememberDoorToClose(ServerLevel param0, LivingEntity param1, BlockPos param2) {
        Brain<?> var0 = param1.getBrain();
        GlobalPos var1 = GlobalPos.of(param0.dimension(), param2);
        if (var0.getMemory(MemoryModuleType.DOORS_TO_CLOSE).isPresent()) {
            var0.getMemory(MemoryModuleType.DOORS_TO_CLOSE).get().add(var1);
        } else {
            var0.setMemory(MemoryModuleType.DOORS_TO_CLOSE, Sets.newHashSet(var1));
        }

    }
}
