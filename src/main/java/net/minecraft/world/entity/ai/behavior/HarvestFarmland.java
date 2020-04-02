package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;

public class HarvestFarmland extends Behavior<Villager> {
    @Nullable
    private BlockPos aboveFarmlandPos;
    private boolean canPlantStuff;
    private boolean wantsToReapStuff;
    private long nextOkStartTime;
    private int timeWorkedSoFar;
    private final List<BlockPos> validFarmlandAroundVillager = Lists.newArrayList();

    public HarvestFarmland() {
        super(
            ImmutableMap.of(
                MemoryModuleType.LOOK_TARGET,
                MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.WALK_TARGET,
                MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.SECONDARY_JOB_SITE,
                MemoryStatus.VALUE_PRESENT
            )
        );
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, Villager param1) {
        if (!param0.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            return false;
        } else if (param1.getVillagerData().getProfession() != VillagerProfession.FARMER) {
            return false;
        } else {
            this.canPlantStuff = param1.hasFarmSeeds();
            this.wantsToReapStuff = false;
            SimpleContainer var0 = param1.getInventory();
            int var1 = var0.getContainerSize();

            for(int var2 = 0; var2 < var1; ++var2) {
                ItemStack var3 = var0.getItem(var2);
                if (var3.isEmpty()) {
                    this.wantsToReapStuff = true;
                    break;
                }
            }

            BlockPos.MutableBlockPos var4 = param1.blockPosition().mutable();
            this.validFarmlandAroundVillager.clear();

            for(int var5 = -1; var5 <= 1; ++var5) {
                for(int var6 = -1; var6 <= 1; ++var6) {
                    for(int var7 = -1; var7 <= 1; ++var7) {
                        var4.set(param1.getX() + (double)var5, param1.getY() + (double)var6, param1.getZ() + (double)var7);
                        if (this.validPos(var4, param0)) {
                            this.validFarmlandAroundVillager.add(new BlockPos(var4));
                        }
                    }
                }
            }

            this.aboveFarmlandPos = this.getValidFarmland(param0);
            return (this.canPlantStuff || this.wantsToReapStuff) && this.aboveFarmlandPos != null;
        }
    }

    @Nullable
    private BlockPos getValidFarmland(ServerLevel param0) {
        return this.validFarmlandAroundVillager.isEmpty()
            ? null
            : this.validFarmlandAroundVillager.get(param0.getRandom().nextInt(this.validFarmlandAroundVillager.size()));
    }

    private boolean validPos(BlockPos param0, ServerLevel param1) {
        BlockState var0 = param1.getBlockState(param0);
        Block var1 = var0.getBlock();
        Block var2 = param1.getBlockState(param0.below()).getBlock();
        return var1 instanceof CropBlock && ((CropBlock)var1).isMaxAge(var0) && this.wantsToReapStuff
            || var0.isAir() && var2 instanceof FarmBlock && this.canPlantStuff;
    }

    protected void start(ServerLevel param0, Villager param1, long param2) {
        if (param2 > this.nextOkStartTime && this.aboveFarmlandPos != null) {
            param1.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(this.aboveFarmlandPos));
            param1.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new BlockPosTracker(this.aboveFarmlandPos), 0.5F, 1));
        }

    }

    protected void stop(ServerLevel param0, Villager param1, long param2) {
        param1.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
        param1.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        this.timeWorkedSoFar = 0;
        this.nextOkStartTime = param2 + 40L;
    }

    protected void tick(ServerLevel param0, Villager param1, long param2) {
        if (this.aboveFarmlandPos == null || this.aboveFarmlandPos.closerThan(param1.position(), 1.0)) {
            if (this.aboveFarmlandPos != null && param2 > this.nextOkStartTime) {
                BlockState var0 = param0.getBlockState(this.aboveFarmlandPos);
                Block var1 = var0.getBlock();
                Block var2 = param0.getBlockState(this.aboveFarmlandPos.below()).getBlock();
                if (var1 instanceof CropBlock && ((CropBlock)var1).isMaxAge(var0) && this.wantsToReapStuff) {
                    param0.destroyBlock(this.aboveFarmlandPos, true, param1);
                }

                if (var0.isAir() && var2 instanceof FarmBlock && this.canPlantStuff) {
                    SimpleContainer var3 = param1.getInventory();

                    for(int var4 = 0; var4 < var3.getContainerSize(); ++var4) {
                        ItemStack var5 = var3.getItem(var4);
                        boolean var6 = false;
                        if (!var5.isEmpty()) {
                            if (var5.getItem() == Items.WHEAT_SEEDS) {
                                param0.setBlock(this.aboveFarmlandPos, Blocks.WHEAT.defaultBlockState(), 3);
                                var6 = true;
                            } else if (var5.getItem() == Items.POTATO) {
                                param0.setBlock(this.aboveFarmlandPos, Blocks.POTATOES.defaultBlockState(), 3);
                                var6 = true;
                            } else if (var5.getItem() == Items.CARROT) {
                                param0.setBlock(this.aboveFarmlandPos, Blocks.CARROTS.defaultBlockState(), 3);
                                var6 = true;
                            } else if (var5.getItem() == Items.BEETROOT_SEEDS) {
                                param0.setBlock(this.aboveFarmlandPos, Blocks.BEETROOTS.defaultBlockState(), 3);
                                var6 = true;
                            }
                        }

                        if (var6) {
                            param0.playSound(
                                null,
                                (double)this.aboveFarmlandPos.getX(),
                                (double)this.aboveFarmlandPos.getY(),
                                (double)this.aboveFarmlandPos.getZ(),
                                SoundEvents.CROP_PLANTED,
                                SoundSource.BLOCKS,
                                1.0F,
                                1.0F
                            );
                            var5.shrink(1);
                            if (var5.isEmpty()) {
                                var3.setItem(var4, ItemStack.EMPTY);
                            }
                            break;
                        }
                    }
                }

                if (var1 instanceof CropBlock && !((CropBlock)var1).isMaxAge(var0)) {
                    this.validFarmlandAroundVillager.remove(this.aboveFarmlandPos);
                    this.aboveFarmlandPos = this.getValidFarmland(param0);
                    if (this.aboveFarmlandPos != null) {
                        this.nextOkStartTime = param2 + 20L;
                        param1.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new BlockPosTracker(this.aboveFarmlandPos), 0.5F, 1));
                        param1.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(this.aboveFarmlandPos));
                    }
                }
            }

            ++this.timeWorkedSoFar;
        }
    }

    protected boolean canStillUse(ServerLevel param0, Villager param1, long param2) {
        return this.timeWorkedSoFar < 200;
    }
}
