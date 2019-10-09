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
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;

public class BeehiveBlockEntity extends BlockEntity implements TickableBlockEntity {
    private final List<BeehiveBlockEntity.BeeData> stored = Lists.newArrayList();
    private BlockPos savedFlowerPos = BlockPos.ZERO;

    public BeehiveBlockEntity() {
        super(BlockEntityType.BEEHIVE);
    }

    @Override
    public void setChanged() {
        if (this.isFireNearby()) {
            this.emptyAllLivingFromHive(null, this.level.getBlockState(this.getBlockPos()), BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY);
        }

        super.setChanged();
    }

    public boolean isFireNearby() {
        if (this.level == null) {
            return false;
        } else {
            for(BlockPos var0 : BlockPos.betweenClosed(this.worldPosition.offset(-1, -1, -1), this.worldPosition.offset(1, 1, 1))) {
                if (this.level.getBlockState(var0).getBlock() instanceof FireBlock) {
                    return true;
                }
            }

            return false;
        }
    }

    public boolean isEmpty() {
        return this.stored.isEmpty();
    }

    public boolean isFull() {
        return this.stored.size() == 3;
    }

    public void emptyAllLivingFromHive(@Nullable Player param0, BlockState param1, BeehiveBlockEntity.BeeReleaseStatus param2) {
        List<Entity> var0 = this.releaseAllOccupants(param1, param2);
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

    private List<Entity> releaseAllOccupants(BlockState param0, BeehiveBlockEntity.BeeReleaseStatus param1) {
        List<Entity> var0 = Lists.newArrayList();
        this.stored.removeIf(param3 -> this.releaseOccupant(param0, param3.entityData, var0, param1));
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

    private boolean releaseOccupant(BlockState param0, CompoundTag param1, @Nullable List<Entity> param2, BeehiveBlockEntity.BeeReleaseStatus param3) {
        BlockPos var0 = this.getBlockPos();
        if ((!this.level.isDay() || this.level.isRaining()) && param3 != BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY) {
            return false;
        } else {
            param1.remove("Passengers");
            param1.remove("Leash");
            param1.removeUUID("UUID");
            Optional<BlockPos> var1 = Optional.empty();
            Direction var2 = param0.getValue(BeehiveBlock.FACING);
            BlockPos var3 = var0.relative(var2, 2);
            if (this.level.getBlockState(var3).getCollisionShape(this.level, var3).isEmpty()) {
                var1 = Optional.of(var3);
            }

            if (!var1.isPresent()) {
                for(Direction var4 : Direction.Plane.HORIZONTAL) {
                    BlockPos var5 = var0.offset(var4.getStepX() * 2, var4.getStepY(), var4.getStepZ() * 2);
                    if (this.level.getBlockState(var5).getCollisionShape(this.level, var5).isEmpty()) {
                        var1 = Optional.of(var5);
                        break;
                    }
                }
            }

            if (!var1.isPresent()) {
                for(Direction var6 : Direction.Plane.VERTICAL) {
                    BlockPos var7 = var0.offset(var6.getStepX() * 2, var6.getStepY(), var6.getStepZ() * 2);
                    if (this.level.getBlockState(var7).getCollisionShape(this.level, var7).isEmpty()) {
                        var1 = Optional.of(var7);
                    }
                }
            }

            if (!var1.isPresent()) {
                return false;
            } else {
                BlockPos var8 = var1.get();
                Entity var9 = EntityType.loadEntityRecursive(param1, this.level, param1x -> {
                    param1x.moveTo((double)var8.getX(), (double)var8.getY(), (double)var8.getZ(), param1x.yRot, param1x.xRot);
                    return param1x;
                });
                if (var9 != null) {
                    if (!var9.getType().is(EntityTypeTags.BEEHIVE_INHABITORS)) {
                        return false;
                    } else {
                        if (var9 instanceof Bee) {
                            Bee var10 = (Bee)var9;
                            if (this.hasSavedFlowerPos() && !var10.hasSavedFlowerPos() && this.level.random.nextFloat() < 0.9F) {
                                var10.setSavedFlowerPos(this.savedFlowerPos);
                            }

                            if (param3 == BeehiveBlockEntity.BeeReleaseStatus.HONEY_DELIVERED) {
                                var10.dropOffNectar();
                                if (param0.getBlock().is(BlockTags.BEEHIVES)) {
                                    int var11 = param0.getValue(BeehiveBlock.HONEY_LEVEL);
                                    if (var11 < 5) {
                                        int var12 = this.level.random.nextInt(100) == 0 ? 2 : 1;
                                        if (var11 + var12 > 5) {
                                            --var12;
                                        }

                                        this.level
                                            .setBlockAndUpdate(this.getBlockPos(), param0.setValue(BeehiveBlock.HONEY_LEVEL, Integer.valueOf(var11 + var12)));
                                    }
                                }
                            }

                            if (param2 != null) {
                                var10.resetTicksSincePollination();
                                param2.add(var10);
                            }
                        }

                        BlockPos var13 = this.getBlockPos();
                        this.level
                            .playSound(
                                null,
                                (double)var13.getX(),
                                (double)var13.getY(),
                                (double)var13.getZ(),
                                SoundEvents.BEEHIVE_EXIT,
                                SoundSource.BLOCKS,
                                1.0F,
                                1.0F
                            );
                        return this.level.addFreshEntity(var9);
                    }
                } else {
                    return false;
                }
            }
        }
    }

    private boolean hasSavedFlowerPos() {
        return this.savedFlowerPos != BlockPos.ZERO;
    }

    private void tickOccupants() {
        Iterator<BeehiveBlockEntity.BeeData> var0 = this.stored.iterator();
        BlockState var1 = this.getBlockState();

        while(var0.hasNext()) {
            BeehiveBlockEntity.BeeData var2 = var0.next();
            if (var2.ticksInHive > var2.minOccupationTicks) {
                CompoundTag var3 = var2.entityData;
                BeehiveBlockEntity.BeeReleaseStatus var4 = var3.getBoolean("HasNectar")
                    ? BeehiveBlockEntity.BeeReleaseStatus.HONEY_DELIVERED
                    : BeehiveBlockEntity.BeeReleaseStatus.BEE_RELEASED;
                if (this.releaseOccupant(var1, var3, null, var4)) {
                    var0.remove();
                }
            } else {
                var2.ticksInHive++;
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
        BEE_RELEASED,
        EMERGENCY;
    }
}
