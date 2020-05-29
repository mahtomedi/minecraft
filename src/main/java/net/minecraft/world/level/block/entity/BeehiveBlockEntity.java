package net.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;

public class BeehiveBlockEntity extends BlockEntity implements TickableBlockEntity {
    private final List<BeehiveBlockEntity.BeeData> stored = Lists.newArrayList();
    @Nullable
    private BlockPos savedFlowerPos = null;

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
                        if (!this.isSedated()) {
                            var2.makeAngry(param0);
                        } else {
                            var2.setStayOutOfHiveCountdown(400);
                        }
                    }
                }
            }
        }

    }

    private List<Entity> releaseAllOccupants(BlockState param0, BeehiveBlockEntity.BeeReleaseStatus param1) {
        List<Entity> var0 = Lists.newArrayList();
        this.stored.removeIf(param3 -> this.releaseOccupant(param0, param3, var0, param1));
        return var0;
    }

    public void addOccupant(Entity param0, boolean param1) {
        this.addOccupantWithPresetTicks(param0, param1, 0);
    }

    public int getOccupantCount() {
        return this.stored.size();
    }

    public static int getHoneyLevel(BlockState param0) {
        return param0.getValue(BeehiveBlock.HONEY_LEVEL);
    }

    public boolean isSedated() {
        return CampfireBlock.isSmokeyPos(this.level, this.getBlockPos());
    }

    protected void sendDebugPackets() {
        DebugPackets.sendHiveInfo(this);
    }

    public void addOccupantWithPresetTicks(Entity param0, boolean param1, int param2) {
        if (this.stored.size() < 3) {
            param0.stopRiding();
            param0.ejectPassengers();
            CompoundTag var0 = new CompoundTag();
            param0.save(var0);
            this.stored.add(new BeehiveBlockEntity.BeeData(var0, param2, param1 ? 2400 : 600));
            if (this.level != null) {
                if (param0 instanceof Bee) {
                    Bee var1 = (Bee)param0;
                    if (var1.hasSavedFlowerPos() && (!this.hasSavedFlowerPos() || this.level.random.nextBoolean())) {
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

    private boolean releaseOccupant(
        BlockState param0, BeehiveBlockEntity.BeeData param1, @Nullable List<Entity> param2, BeehiveBlockEntity.BeeReleaseStatus param3
    ) {
        if ((this.level.isNight() || this.level.isRaining()) && param3 != BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY) {
            return false;
        } else {
            BlockPos var0 = this.getBlockPos();
            CompoundTag var1 = param1.entityData;
            var1.remove("Passengers");
            var1.remove("Leash");
            var1.remove("UUID");
            Direction var2 = param0.getValue(BeehiveBlock.FACING);
            BlockPos var3 = var0.relative(var2);
            boolean var4 = !this.level.getBlockState(var3).getCollisionShape(this.level, var3).isEmpty();
            if (var4 && param3 != BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY) {
                return false;
            } else {
                Entity var5 = EntityType.loadEntityRecursive(var1, this.level, param0x -> param0x);
                if (var5 != null) {
                    if (!var5.getType().is(EntityTypeTags.BEEHIVE_INHABITORS)) {
                        return false;
                    } else {
                        if (var5 instanceof Bee) {
                            Bee var6 = (Bee)var5;
                            if (this.hasSavedFlowerPos() && !var6.hasSavedFlowerPos() && this.level.random.nextFloat() < 0.9F) {
                                var6.setSavedFlowerPos(this.savedFlowerPos);
                            }

                            if (param3 == BeehiveBlockEntity.BeeReleaseStatus.HONEY_DELIVERED) {
                                var6.dropOffNectar();
                                if (param0.getBlock().is(BlockTags.BEEHIVES)) {
                                    int var7 = getHoneyLevel(param0);
                                    if (var7 < 5) {
                                        int var8 = this.level.random.nextInt(100) == 0 ? 2 : 1;
                                        if (var7 + var8 > 5) {
                                            --var8;
                                        }

                                        this.level
                                            .setBlockAndUpdate(this.getBlockPos(), param0.setValue(BeehiveBlock.HONEY_LEVEL, Integer.valueOf(var7 + var8)));
                                    }
                                }
                            }

                            int var9 = param1.ticksInHive;
                            var6.ageUp(var9);
                            var6.setInLoveTime(Math.max(0, var6.getInLoveTime() - var9));
                            var6.resetTicksWithoutNectarSinceExitingHive();
                            if (param2 != null) {
                                param2.add(var6);
                            }

                            float var10 = var5.getBbWidth();
                            double var11 = var4 ? 0.0 : 0.55 + (double)(var10 / 2.0F);
                            double var12 = (double)var0.getX() + 0.5 + var11 * (double)var2.getStepX();
                            double var13 = (double)var0.getY() + 0.5 - (double)(var5.getBbHeight() / 2.0F);
                            double var14 = (double)var0.getZ() + 0.5 + var11 * (double)var2.getStepZ();
                            var5.moveTo(var12, var13, var14, var5.yRot, var5.xRot);
                        }

                        this.level.playSound(null, var0, SoundEvents.BEEHIVE_EXIT, SoundSource.BLOCKS, 1.0F, 1.0F);
                        return this.level.addFreshEntity(var5);
                    }
                } else {
                    return false;
                }
            }
        }
    }

    private boolean hasSavedFlowerPos() {
        return this.savedFlowerPos != null;
    }

    private void tickOccupants() {
        Iterator<BeehiveBlockEntity.BeeData> var0 = this.stored.iterator();
        BlockState var1 = this.getBlockState();

        while(var0.hasNext()) {
            BeehiveBlockEntity.BeeData var2 = var0.next();
            if (var2.ticksInHive > var2.minOccupationTicks) {
                BeehiveBlockEntity.BeeReleaseStatus var3 = var2.entityData.getBoolean("HasNectar")
                    ? BeehiveBlockEntity.BeeReleaseStatus.HONEY_DELIVERED
                    : BeehiveBlockEntity.BeeReleaseStatus.BEE_RELEASED;
                if (this.releaseOccupant(var1, var2, null, var3)) {
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

            this.sendDebugPackets();
        }
    }

    @Override
    public void load(BlockState param0, CompoundTag param1) {
        super.load(param0, param1);
        this.stored.clear();
        ListTag var0 = param1.getList("Bees", 10);

        for(int var1 = 0; var1 < var0.size(); ++var1) {
            CompoundTag var2 = var0.getCompound(var1);
            BeehiveBlockEntity.BeeData var3 = new BeehiveBlockEntity.BeeData(
                var2.getCompound("EntityData"), var2.getInt("TicksInHive"), var2.getInt("MinOccupationTicks")
            );
            this.stored.add(var3);
        }

        this.savedFlowerPos = null;
        if (param1.contains("FlowerPos")) {
            this.savedFlowerPos = NbtUtils.readBlockPos(param1.getCompound("FlowerPos"));
        }

    }

    @Override
    public CompoundTag save(CompoundTag param0) {
        super.save(param0);
        param0.put("Bees", this.writeBees());
        if (this.hasSavedFlowerPos()) {
            param0.put("FlowerPos", NbtUtils.writeBlockPos(this.savedFlowerPos));
        }

        return param0;
    }

    public ListTag writeBees() {
        ListTag var0 = new ListTag();

        for(BeehiveBlockEntity.BeeData var1 : this.stored) {
            var1.entityData.remove("UUID");
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
            param0.remove("UUID");
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
