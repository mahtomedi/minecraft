package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

public class UseBonemeal extends Behavior<Villager> {
    private static final int BONEMEALING_DURATION = 80;
    private long nextWorkCycleTime;
    private long lastBonemealingSession;
    private int timeWorkedSoFar;
    private Optional<BlockPos> cropPos = Optional.empty();

    public UseBonemeal() {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT));
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, Villager param1) {
        if (param1.tickCount % 10 == 0 && (this.lastBonemealingSession == 0L || this.lastBonemealingSession + 160L <= (long)param1.tickCount)) {
            if (param1.getInventory().countItem(Items.BONE_MEAL) <= 0) {
                return false;
            } else {
                this.cropPos = this.pickNextTarget(param0, param1);
                return this.cropPos.isPresent();
            }
        } else {
            return false;
        }
    }

    protected boolean canStillUse(ServerLevel param0, Villager param1, long param2) {
        return this.timeWorkedSoFar < 80 && this.cropPos.isPresent();
    }

    private Optional<BlockPos> pickNextTarget(ServerLevel param0, Villager param1) {
        BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos();
        Optional<BlockPos> var1 = Optional.empty();
        int var2 = 0;

        for(int var3 = -1; var3 <= 1; ++var3) {
            for(int var4 = -1; var4 <= 1; ++var4) {
                for(int var5 = -1; var5 <= 1; ++var5) {
                    var0.setWithOffset(param1.blockPosition(), var3, var4, var5);
                    if (this.validPos(var0, param0)) {
                        if (param0.random.nextInt(++var2) == 0) {
                            var1 = Optional.of(var0.immutable());
                        }
                    }
                }
            }
        }

        return var1;
    }

    private boolean validPos(BlockPos param0, ServerLevel param1) {
        BlockState var0 = param1.getBlockState(param0);
        Block var1 = var0.getBlock();
        return var1 instanceof CropBlock && !((CropBlock)var1).isMaxAge(var0);
    }

    protected void start(ServerLevel param0, Villager param1, long param2) {
        this.setCurrentCropAsTarget(param1);
        param1.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BONE_MEAL));
        this.nextWorkCycleTime = param2;
        this.timeWorkedSoFar = 0;
    }

    private void setCurrentCropAsTarget(Villager param0) {
        this.cropPos.ifPresent(param1 -> {
            BlockPosTracker var0 = new BlockPosTracker(param1);
            param0.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, var0);
            param0.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(var0, 0.5F, 1));
        });
    }

    protected void stop(ServerLevel param0, Villager param1, long param2) {
        param1.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        this.lastBonemealingSession = (long)param1.tickCount;
    }

    protected void tick(ServerLevel param0, Villager param1, long param2) {
        BlockPos var0 = this.cropPos.get();
        if (param2 >= this.nextWorkCycleTime && var0.closerToCenterThan(param1.position(), 1.0)) {
            ItemStack var1 = ItemStack.EMPTY;
            SimpleContainer var2 = param1.getInventory();
            int var3 = var2.getContainerSize();

            for(int var4 = 0; var4 < var3; ++var4) {
                ItemStack var5 = var2.getItem(var4);
                if (var5.is(Items.BONE_MEAL)) {
                    var1 = var5;
                    break;
                }
            }

            if (!var1.isEmpty() && BoneMealItem.growCrop(var1, param0, var0)) {
                param0.levelEvent(1505, var0, 0);
                this.cropPos = this.pickNextTarget(param0, param1);
                this.setCurrentCropAsTarget(param1);
                this.nextWorkCycleTime = param2 + 40L;
            }

            ++this.timeWorkedSoFar;
        }
    }
}
