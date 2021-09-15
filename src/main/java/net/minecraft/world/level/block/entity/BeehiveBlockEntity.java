package net.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import java.util.Arrays;
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
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;

public class BeehiveBlockEntity extends BlockEntity {
    public static final String TAG_FLOWER_POS = "FlowerPos";
    public static final String MIN_OCCUPATION_TICKS = "MinOccupationTicks";
    public static final String ENTITY_DATA = "EntityData";
    public static final String TICKS_IN_HIVE = "TicksInHive";
    public static final String HAS_NECTAR = "HasNectar";
    public static final String BEES = "Bees";
    private static final List<String> IGNORED_BEE_TAGS = Arrays.asList(
        "Air",
        "ArmorDropChances",
        "ArmorItems",
        "Brain",
        "CanPickUpLoot",
        "DeathTime",
        "FallDistance",
        "FallFlying",
        "Fire",
        "HandDropChances",
        "HandItems",
        "HurtByTimestamp",
        "HurtTime",
        "LeftHanded",
        "Motion",
        "NoGravity",
        "OnGround",
        "PortalCooldown",
        "Pos",
        "Rotation",
        "CannotEnterHiveTicks",
        "TicksSincePollination",
        "CropsGrownSincePollination",
        "HivePos",
        "Passengers",
        "Leash",
        "UUID"
    );
    public static final int MAX_OCCUPANTS = 3;
    private static final int MIN_TICKS_BEFORE_REENTERING_HIVE = 400;
    private static final int MIN_OCCUPATION_TICKS_NECTAR = 2400;
    public static final int MIN_OCCUPATION_TICKS_NECTARLESS = 600;
    private final List<BeehiveBlockEntity.BeeData> stored = Lists.newArrayList();
    @Nullable
    private BlockPos savedFlowerPos;

    public BeehiveBlockEntity(BlockPos param0, BlockState param1) {
        super(BlockEntityType.BEEHIVE, param0, param1);
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
                if (var1 instanceof Bee var2 && param0.position().distanceToSqr(var1.position()) <= 16.0) {
                    if (!this.isSedated()) {
                        var2.setTarget(param0);
                    } else {
                        var2.setStayOutOfHiveCountdown(400);
                    }
                }
            }
        }

    }

    private List<Entity> releaseAllOccupants(BlockState param0, BeehiveBlockEntity.BeeReleaseStatus param1) {
        List<Entity> var0 = Lists.newArrayList();
        this.stored.removeIf(param3 -> releaseOccupant(this.level, this.worldPosition, param0, param3, var0, param1, this.savedFlowerPos));
        return var0;
    }

    public void addOccupant(Entity param0, boolean param1) {
        this.addOccupantWithPresetTicks(param0, param1, 0);
    }

    @VisibleForDebug
    public int getOccupantCount() {
        return this.stored.size();
    }

    public static int getHoneyLevel(BlockState param0) {
        return param0.getValue(BeehiveBlock.HONEY_LEVEL);
    }

    @VisibleForDebug
    public boolean isSedated() {
        return CampfireBlock.isSmokeyPos(this.level, this.getBlockPos());
    }

    public void addOccupantWithPresetTicks(Entity param0, boolean param1, int param2) {
        if (this.stored.size() < 3) {
            param0.stopRiding();
            param0.ejectPassengers();
            CompoundTag var0 = new CompoundTag();
            param0.save(var0);
            this.storeBee(var0, param2, param1);
            if (this.level != null) {
                if (param0 instanceof Bee var1 && var1.hasSavedFlowerPos() && (!this.hasSavedFlowerPos() || this.level.random.nextBoolean())) {
                    this.savedFlowerPos = var1.getSavedFlowerPos();
                }

                BlockPos var2 = this.getBlockPos();
                this.level
                    .playSound(null, (double)var2.getX(), (double)var2.getY(), (double)var2.getZ(), SoundEvents.BEEHIVE_ENTER, SoundSource.BLOCKS, 1.0F, 1.0F);
            }

            param0.discard();
        }
    }

    public void storeBee(CompoundTag param0, int param1, boolean param2) {
        this.stored.add(new BeehiveBlockEntity.BeeData(param0, param1, param2 ? 2400 : 600));
    }

    private static boolean releaseOccupant(
        Level param0,
        BlockPos param1,
        BlockState param2,
        BeehiveBlockEntity.BeeData param3,
        @Nullable List<Entity> param4,
        BeehiveBlockEntity.BeeReleaseStatus param5,
        @Nullable BlockPos param6
    ) {
        if ((param0.isNight() || param0.isRaining()) && param5 != BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY) {
            return false;
        } else {
            CompoundTag var0 = param3.entityData;
            removeIgnoredBeeTags(var0);
            var0.put("HivePos", NbtUtils.writeBlockPos(param1));
            var0.putBoolean("NoGravity", true);
            Direction var1 = param2.getValue(BeehiveBlock.FACING);
            BlockPos var2 = param1.relative(var1);
            boolean var3 = !param0.getBlockState(var2).getCollisionShape(param0, var2).isEmpty();
            if (var3 && param5 != BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY) {
                return false;
            } else {
                Entity var4 = EntityType.loadEntityRecursive(var0, param0, param0x -> param0x);
                if (var4 != null) {
                    if (!var4.getType().is(EntityTypeTags.BEEHIVE_INHABITORS)) {
                        return false;
                    } else {
                        if (var4 instanceof Bee var5) {
                            if (param6 != null && !var5.hasSavedFlowerPos() && param0.random.nextFloat() < 0.9F) {
                                var5.setSavedFlowerPos(param6);
                            }

                            if (param5 == BeehiveBlockEntity.BeeReleaseStatus.HONEY_DELIVERED) {
                                var5.dropOffNectar();
                                if (param2.is(BlockTags.BEEHIVES)) {
                                    int var6 = getHoneyLevel(param2);
                                    if (var6 < 5) {
                                        int var7 = param0.random.nextInt(100) == 0 ? 2 : 1;
                                        if (var6 + var7 > 5) {
                                            --var7;
                                        }

                                        param0.setBlockAndUpdate(param1, param2.setValue(BeehiveBlock.HONEY_LEVEL, Integer.valueOf(var6 + var7)));
                                    }
                                }
                            }

                            setBeeReleaseData(param3.ticksInHive, var5);
                            if (param4 != null) {
                                param4.add(var5);
                            }

                            float var8 = var4.getBbWidth();
                            double var9 = var3 ? 0.0 : 0.55 + (double)(var8 / 2.0F);
                            double var10 = (double)param1.getX() + 0.5 + var9 * (double)var1.getStepX();
                            double var11 = (double)param1.getY() + 0.5 - (double)(var4.getBbHeight() / 2.0F);
                            double var12 = (double)param1.getZ() + 0.5 + var9 * (double)var1.getStepZ();
                            var4.moveTo(var10, var11, var12, var4.getYRot(), var4.getXRot());
                        }

                        param0.playSound(null, param1, SoundEvents.BEEHIVE_EXIT, SoundSource.BLOCKS, 1.0F, 1.0F);
                        return param0.addFreshEntity(var4);
                    }
                } else {
                    return false;
                }
            }
        }
    }

    static void removeIgnoredBeeTags(CompoundTag param0) {
        for(String var0 : IGNORED_BEE_TAGS) {
            param0.remove(var0);
        }

    }

    private static void setBeeReleaseData(int param0, Bee param1) {
        int var0 = param1.getAge();
        if (var0 < 0) {
            param1.setAge(Math.min(0, var0 + param0));
        } else if (var0 > 0) {
            param1.setAge(Math.max(0, var0 - param0));
        }

        param1.setInLoveTime(Math.max(0, param1.getInLoveTime() - param0));
    }

    private boolean hasSavedFlowerPos() {
        return this.savedFlowerPos != null;
    }

    private static void tickOccupants(Level param0, BlockPos param1, BlockState param2, List<BeehiveBlockEntity.BeeData> param3, @Nullable BlockPos param4) {
        BeehiveBlockEntity.BeeData var1;
        for(Iterator<BeehiveBlockEntity.BeeData> var0 = param3.iterator(); var0.hasNext(); ++var1.ticksInHive) {
            var1 = var0.next();
            if (var1.ticksInHive > var1.minOccupationTicks) {
                BeehiveBlockEntity.BeeReleaseStatus var2 = var1.entityData.getBoolean("HasNectar")
                    ? BeehiveBlockEntity.BeeReleaseStatus.HONEY_DELIVERED
                    : BeehiveBlockEntity.BeeReleaseStatus.BEE_RELEASED;
                if (releaseOccupant(param0, param1, param2, var1, null, var2, param4)) {
                    var0.remove();
                }
            }
        }

    }

    public static void serverTick(Level param0, BlockPos param1, BlockState param2, BeehiveBlockEntity param3) {
        tickOccupants(param0, param1, param2, param3.stored, param3.savedFlowerPos);
        if (!param3.stored.isEmpty() && param0.getRandom().nextDouble() < 0.005) {
            double var0 = (double)param1.getX() + 0.5;
            double var1 = (double)param1.getY();
            double var2 = (double)param1.getZ() + 0.5;
            param0.playSound(null, var0, var1, var2, SoundEvents.BEEHIVE_WORK, SoundSource.BLOCKS, 1.0F, 1.0F);
        }

        DebugPackets.sendHiveInfo(param0, param1, param2, param3);
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

        this.savedFlowerPos = null;
        if (param0.contains("FlowerPos")) {
            this.savedFlowerPos = NbtUtils.readBlockPos(param0.getCompound("FlowerPos"));
        }

    }

    @Override
    protected void saveAdditional(CompoundTag param0) {
        super.saveAdditional(param0);
        param0.put("Bees", this.writeBees());
        if (this.hasSavedFlowerPos()) {
            param0.put("FlowerPos", NbtUtils.writeBlockPos(this.savedFlowerPos));
        }

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
        final CompoundTag entityData;
        int ticksInHive;
        final int minOccupationTicks;

        BeeData(CompoundTag param0, int param1, int param2) {
            BeehiveBlockEntity.removeIgnoredBeeTags(param0);
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
