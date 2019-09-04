package net.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class BeehiveBlockEntity extends BlockEntity implements TickableBlockEntity {
    private final List<BeehiveBlockEntity.BeeData> stored = Lists.newArrayList();
    private BlockPos savedFlowerPos = BlockPos.ZERO;

    public BeehiveBlockEntity() {
        super(BlockEntityType.BEEHIVE);
    }

    public boolean isEmpty() {
        return this.stored.isEmpty();
    }

    public boolean isFull() {
        return this.stored.size() == 3;
    }

    public void emptyAllLivingFromHive(@Nullable Player param0, BeehiveBlockEntity.BeeReleaseStatus param1) {
        List<Entity> var0 = this.releaseAllOccupants(param1);
        if (param0 != null) {
            for(Entity var1 : var0) {
                if (var1 instanceof Bee) {
                    Bee var2 = (Bee)var1;
                    if (param0.position().distanceToSqr(var1.position()) <= 16.0) {
                        if (!this.isCampfireBelow(this.level, this.getBlockPos())) {
                            var2.makeAngry(param0);
                        } else {
                            var2.setCannotEnterHiveTicks(400);
                        }
                    }
                }
            }
        }

    }

    private boolean isCampfireBelow(Level param0, BlockPos param1) {
        for(int var0 = 1; var0 <= 5; ++var0) {
            BlockState var1 = param0.getBlockState(param1.below(var0));
            if (!var1.isAir()) {
                return var1.getBlock() == Blocks.CAMPFIRE;
            }
        }

        return false;
    }

    private List<Entity> releaseAllOccupants(BeehiveBlockEntity.BeeReleaseStatus param0) {
        List<Entity> var0 = Lists.newArrayList();
        this.stored.removeIf(param2 -> this.releaseOccupant(param2.entityData, var0, param0));
        return var0;
    }

    public void addOccupant(Entity param0, boolean param1) {
        this.addOccupantWithPresetTicks(param0, param1, 0);
    }

    public void addOccupantWithPresetTicks(Entity param0, boolean param1, int param2) {
        if (this.stored.size() < 3) {
            param0.ejectPassengers();
            CompoundTag var0 = new CompoundTag();
            param0.save(var0);
            this.stored.add(new BeehiveBlockEntity.BeeData(var0, param2, param1 ? 2400 : 600));
            if (this.level != null) {
                if (param0 instanceof Bee) {
                    Bee var1 = (Bee)param0;
                    if (!this.hasSavedFlowerPos() || var1.hasSavedFlowerPos() && this.level.random.nextBoolean()) {
                        this.savedFlowerPos = var1.getSavedFlowerPos();
                    }
                }

                BlockPos var2 = this.getBlockPos();
                this.level
                    .playSound(null, (double)var2.getX(), (double)var2.getY(), (double)var2.getZ(), SoundEvents.BEEHIVE_ENTER, SoundSource.BLOCKS, 1.0F, 1.0F);
            }

            param0.remove();
        }
    }

    private boolean releaseOccupant(CompoundTag param0, @Nullable List<Entity> param1, BeehiveBlockEntity.BeeReleaseStatus param2) {
        BlockPos var0 = this.getBlockPos();
        if (this.level.isDay() && !this.level.isRainingAt(var0)) {
            param0.remove("Passengers");
            param0.remove("Leash");
            param0.removeUUID("UUID");
            Optional<BlockPos> var1 = Optional.empty();
            BlockState var2 = this.getBlockState();
            Direction var3 = var2.getValue(BeehiveBlock.FACING);
            BlockPos var4 = var0.relative(var3, 2);
            if (this.level.getBlockState(var4).getCollisionShape(this.level, var4).isEmpty()) {
                var1 = Optional.of(var4);
            }

            if (!var1.isPresent()) {
                for(Direction var5 : Direction.Plane.HORIZONTAL) {
                    BlockPos var6 = var0.offset(var5.getStepX() * 2, var5.getStepY(), var5.getStepZ() * 2);
                    if (this.level.getBlockState(var6).getCollisionShape(this.level, var6).isEmpty()) {
                        var1 = Optional.of(var6);
                        break;
                    }
                }
            }

            if (!var1.isPresent()) {
                for(Direction var7 : Direction.Plane.VERTICAL) {
                    BlockPos var8 = var0.offset(var7.getStepX() * 2, var7.getStepY(), var7.getStepZ() * 2);
                    if (this.level.getBlockState(var8).getCollisionShape(this.level, var8).isEmpty()) {
                        var1 = Optional.of(var8);
                    }
                }
            }

            if (!var1.isPresent()) {
                return false;
            } else {
                BlockPos var9 = var1.get();
                Entity var10 = EntityType.loadEntityRecursive(param0, this.level, param1x -> {
                    param1x.moveTo((double)var9.getX(), (double)var9.getY(), (double)var9.getZ(), param1x.yRot, param1x.xRot);
                    return param1x;
                });
                if (var10 != null) {
                    if (!var10.getType().is(EntityTypeTags.BEEHIVE_INHABITORS)) {
                        return false;
                    } else {
                        if (var10 instanceof Bee) {
                            Bee var11 = (Bee)var10;
                            if (this.hasSavedFlowerPos() && !var11.hasSavedFlowerPos() && this.level.random.nextFloat() < 0.9F) {
                                var11.setSavedFlowerPos(this.savedFlowerPos);
                            }

                            if (param2 == BeehiveBlockEntity.BeeReleaseStatus.HONEY_DELIVERED) {
                                var11.dropOffNectar();
                                if (var2.getBlock().is(BlockTags.BEEHIVES)) {
                                    int var12 = var2.getValue(BeehiveBlock.HONEY_LEVEL);
                                    if (var12 < 5) {
                                        int var13 = this.level.random.nextInt(100) == 0 ? 2 : 1;
                                        if (var12 + var13 > 5) {
                                            --var13;
                                        }

                                        this.level
                                            .setBlockAndUpdate(this.getBlockPos(), var2.setValue(BeehiveBlock.HONEY_LEVEL, Integer.valueOf(var12 + var13)));
                                    }
                                }
                            }

                            if (param1 != null) {
                                var11.resetTicksSincePollination();
                                param1.add(var11);
                            }
                        }

                        BlockPos var14 = this.getBlockPos();
                        this.level
                            .playSound(
                                null,
                                (double)var14.getX(),
                                (double)var14.getY(),
                                (double)var14.getZ(),
                                SoundEvents.BEEHIVE_EXIT,
                                SoundSource.BLOCKS,
                                1.0F,
                                1.0F
                            );
                        return this.level.addFreshEntity(var10);
                    }
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    private boolean hasSavedFlowerPos() {
        return this.savedFlowerPos != BlockPos.ZERO;
    }

    private void tickOccupants() {
        Iterator<BeehiveBlockEntity.BeeData> var0 = this.stored.iterator();

        while(var0.hasNext()) {
            BeehiveBlockEntity.BeeData var1 = var0.next();
            if (var1.ticksInHive > var1.minOccupationTicks) {
                CompoundTag var2 = var1.entityData;
                BeehiveBlockEntity.BeeReleaseStatus var3 = var2.getBoolean("HasNectar")
                    ? BeehiveBlockEntity.BeeReleaseStatus.HONEY_DELIVERED
                    : BeehiveBlockEntity.BeeReleaseStatus.BEE_RELEASED;
                if (this.releaseOccupant(var2, null, var3)) {
                    var0.remove();
                }
            } else {
                var1.ticksInHive++;
            }
        }

    }

    @Override
    public void tick() {
        if (!this.level.isClientSide) {
            this.tickOccupants();
            BlockPos var0 = this.getBlockPos();
            if (this.stored.size() > 0 && this.level.getRandom().nextDouble() < 0.005) {
                double var1 = (double)var0.getX() + 0.5;
                double var2 = (double)var0.getY();
                double var3 = (double)var0.getZ() + 0.5;
                this.level.playSound(null, var1, var2, var3, SoundEvents.BEEHIVE_WORK, SoundSource.BLOCKS, 1.0F, 1.0F);
            }

        }
    }

    @Override
    public void load(CompoundTag param0) {
        super.load(param0);
        this.stored.clear();
        ListTag var0 = param0.getList("Bees", 10);

        for(int var1 = 0; var1 < var0.size(); ++var1) {
            CompoundTag var2 = var0.getCompound(var1);
            BeehiveBlockEntity.BeeData var3 = new BeehiveBlockEntity.BeeData(
                var2.getCompound("EntityData"), var2.getInt("TicksInHive"), var2.getInt("MinOccupationTicks")
            );
            this.stored.add(var3);
        }

        this.savedFlowerPos = NbtUtils.readBlockPos(param0.getCompound("FlowerPos"));
    }

    @Override
    public CompoundTag save(CompoundTag param0) {
        super.save(param0);
        param0.put("Bees", this.writeBees());
        param0.put("FlowerPos", NbtUtils.writeBlockPos(this.savedFlowerPos));
        return param0;
    }

    public ListTag writeBees() {
        ListTag var0 = new ListTag();

        for(BeehiveBlockEntity.BeeData var1 : this.stored) {
            var1.entityData.removeUUID("UUID");
            CompoundTag var2 = new CompoundTag();
            var2.put("EntityData", var1.entityData);
            var2.putInt("TicksInHive", var1.ticksInHive);
            var2.putInt("MinOccupationTicks", var1.minOccupationTicks);
            var0.add(var2);
        }

        return var0;
    }

    static class BeeData {
        private final CompoundTag entityData;
        private int ticksInHive;
        private final int minOccupationTicks;

        private BeeData(CompoundTag param0, int param1, int param2) {
            param0.removeUUID("UUID");
            this.entityData = param0;
            this.ticksInHive = param1;
            this.minOccupationTicks = param2;
        }
    }

    public static enum BeeReleaseStatus {
        HONEY_DELIVERED,
        BEE_RELEASED;
    }
}
