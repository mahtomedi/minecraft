package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.Sets;
import com.mojang.datafixers.kinds.OptionalBox.Mu;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;

public class InteractWithDoor {
    private static final int COOLDOWN_BEFORE_RERUNNING_IN_SAME_NODE = 20;
    private static final double SKIP_CLOSING_DOOR_IF_FURTHER_AWAY_THAN = 3.0;
    private static final double MAX_DISTANCE_TO_HOLD_DOOR_OPEN_FOR_OTHER_MOBS = 2.0;

    public static BehaviorControl<LivingEntity> create() {
        MutableObject<Node> var0 = new MutableObject<>(null);
        MutableInt var1 = new MutableInt(0);
        return BehaviorBuilder.create(
            param2 -> param2.<MemoryAccessor, MemoryAccessor, MemoryAccessor>group(
                        param2.present(MemoryModuleType.PATH),
                        param2.registered(MemoryModuleType.DOORS_TO_CLOSE),
                        param2.registered(MemoryModuleType.NEAREST_LIVING_ENTITIES)
                    )
                    .apply(param2, (param3, param4, param5) -> (param6, param7, param8) -> {
                            Path var0x = param2.get(param3);
                            Optional<Set<GlobalPos>> var1x = param2.tryGet(param4);
                            if (!var0x.notStarted() && !var0x.isDone()) {
                                if (Objects.equals(var0.getValue(), var0x.getNextNode())) {
                                    var1.setValue(20);
                                } else if (var1.decrementAndGet() > 0) {
                                    return false;
                                }
        
                                var0.setValue(var0x.getNextNode());
                                Node var2x = var0x.getPreviousNode();
                                Node var3x = var0x.getNextNode();
                                BlockPos var4x = var2x.asBlockPos();
                                BlockState var5x = param6.getBlockState(var4x);
                                if (var5x.is(BlockTags.WOODEN_DOORS, param0x -> param0x.getBlock() instanceof DoorBlock)) {
                                    DoorBlock var6 = (DoorBlock)var5x.getBlock();
                                    if (!var6.isOpen(var5x)) {
                                        var6.setOpen(param7, param6, var5x, var4x, true);
                                    }
        
                                    var1x = rememberDoorToClose(param4, var1x, param6, var4x);
                                }
        
                                BlockPos var7 = var3x.asBlockPos();
                                BlockState var8 = param6.getBlockState(var7);
                                if (var8.is(BlockTags.WOODEN_DOORS, param0x -> param0x.getBlock() instanceof DoorBlock)) {
                                    DoorBlock var9 = (DoorBlock)var8.getBlock();
                                    if (!var9.isOpen(var8)) {
                                        var9.setOpen(param7, param6, var8, var7, true);
                                        var1x = rememberDoorToClose(param4, var1x, param6, var7);
                                    }
                                }
        
                                var1x.ifPresent(
                                    param6x -> closeDoorsThatIHaveOpenedOrPassedThrough(param6, param7, var2x, var3x, param6x, param2.tryGet(param5))
                                );
                                return true;
                            } else {
                                return false;
                            }
                        })
        );
    }

    public static void closeDoorsThatIHaveOpenedOrPassedThrough(
        ServerLevel param0, LivingEntity param1, @Nullable Node param2, @Nullable Node param3, Set<GlobalPos> param4, Optional<List<LivingEntity>> param5
    ) {
        Iterator<GlobalPos> var0 = param4.iterator();

        while(var0.hasNext()) {
            GlobalPos var1 = var0.next();
            BlockPos var2 = var1.pos();
            if ((param2 == null || !param2.asBlockPos().equals(var2)) && (param3 == null || !param3.asBlockPos().equals(var2))) {
                if (isDoorTooFarAway(param0, param1, var1)) {
                    var0.remove();
                } else {
                    BlockState var3 = param0.getBlockState(var2);
                    if (!var3.is(BlockTags.WOODEN_DOORS, param0x -> param0x.getBlock() instanceof DoorBlock)) {
                        var0.remove();
                    } else {
                        DoorBlock var4 = (DoorBlock)var3.getBlock();
                        if (!var4.isOpen(var3)) {
                            var0.remove();
                        } else if (areOtherMobsComingThroughDoor(param1, var2, param5)) {
                            var0.remove();
                        } else {
                            var4.setOpen(param1, param0, var3, var2, false);
                            var0.remove();
                        }
                    }
                }
            }
        }

    }

    private static boolean areOtherMobsComingThroughDoor(LivingEntity param0, BlockPos param1, Optional<List<LivingEntity>> param2) {
        return param2.isEmpty()
            ? false
            : param2.get()
                .stream()
                .filter(param1x -> param1x.getType() == param0.getType())
                .filter(param1x -> param1.closerToCenterThan(param1x.position(), 2.0))
                .anyMatch(param1x -> isMobComingThroughDoor(param1x.getBrain(), param1));
    }

    private static boolean isMobComingThroughDoor(Brain<?> param0, BlockPos param1) {
        if (!param0.hasMemoryValue(MemoryModuleType.PATH)) {
            return false;
        } else {
            Path var0 = param0.getMemory(MemoryModuleType.PATH).get();
            if (var0.isDone()) {
                return false;
            } else {
                Node var1 = var0.getPreviousNode();
                if (var1 == null) {
                    return false;
                } else {
                    Node var2 = var0.getNextNode();
                    return param1.equals(var1.asBlockPos()) || param1.equals(var2.asBlockPos());
                }
            }
        }
    }

    private static boolean isDoorTooFarAway(ServerLevel param0, LivingEntity param1, GlobalPos param2) {
        return param2.dimension() != param0.dimension() || !param2.pos().closerToCenterThan(param1.position(), 3.0);
    }

    private static Optional<Set<GlobalPos>> rememberDoorToClose(
        MemoryAccessor<Mu, Set<GlobalPos>> param0, Optional<Set<GlobalPos>> param1, ServerLevel param2, BlockPos param3
    ) {
        GlobalPos var0 = GlobalPos.of(param2.dimension(), param3);
        return Optional.of(param1.map((Function<? super Set<GlobalPos>, ? extends Set<GlobalPos>>)(param1x -> {
            param1x.add(var0);
            return param1x;
        })).orElseGet(() -> {
            Set<GlobalPos> var0x = Sets.newHashSet(var0);
            param0.set(var0x);
            return var0x;
        }));
    }
}
