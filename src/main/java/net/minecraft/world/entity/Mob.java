package net.minecraft.world.entity;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensing;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.AABB;

public abstract class Mob extends LivingEntity implements Targeting {
    private static final EntityDataAccessor<Byte> DATA_MOB_FLAGS_ID = SynchedEntityData.defineId(Mob.class, EntityDataSerializers.BYTE);
    private static final int MOB_FLAG_NO_AI = 1;
    private static final int MOB_FLAG_LEFTHANDED = 2;
    private static final int MOB_FLAG_AGGRESSIVE = 4;
    protected static final int PICKUP_REACH = 1;
    private static final Vec3i ITEM_PICKUP_REACH = new Vec3i(1, 0, 1);
    public static final float MAX_WEARING_ARMOR_CHANCE = 0.15F;
    public static final float MAX_PICKUP_LOOT_CHANCE = 0.55F;
    public static final float MAX_ENCHANTED_ARMOR_CHANCE = 0.5F;
    public static final float MAX_ENCHANTED_WEAPON_CHANCE = 0.25F;
    public static final String LEASH_TAG = "Leash";
    public static final float DEFAULT_EQUIPMENT_DROP_CHANCE = 0.085F;
    public static final int PRESERVE_ITEM_DROP_CHANCE = 2;
    public static final int UPDATE_GOAL_SELECTOR_EVERY_N_TICKS = 2;
    private static final double DEFAULT_ATTACK_REACH = Math.sqrt(2.04F) - 0.6F;
    public int ambientSoundTime;
    protected int xpReward;
    protected LookControl lookControl;
    protected MoveControl moveControl;
    protected JumpControl jumpControl;
    private final BodyRotationControl bodyRotationControl;
    protected PathNavigation navigation;
    protected final GoalSelector goalSelector;
    protected final GoalSelector targetSelector;
    @Nullable
    private LivingEntity target;
    private final Sensing sensing;
    private final NonNullList<ItemStack> handItems = NonNullList.withSize(2, ItemStack.EMPTY);
    protected final float[] handDropChances = new float[2];
    private final NonNullList<ItemStack> armorItems = NonNullList.withSize(4, ItemStack.EMPTY);
    protected final float[] armorDropChances = new float[4];
    private boolean canPickUpLoot;
    private boolean persistenceRequired;
    private final Map<BlockPathTypes, Float> pathfindingMalus = Maps.newEnumMap(BlockPathTypes.class);
    @Nullable
    private ResourceLocation lootTable;
    private long lootTableSeed;
    @Nullable
    private Entity leashHolder;
    private int delayedLeashHolderId;
    @Nullable
    private CompoundTag leashInfoTag;
    private BlockPos restrictCenter = BlockPos.ZERO;
    private float restrictRadius = -1.0F;

    protected Mob(EntityType<? extends Mob> param0, Level param1) {
        super(param0, param1);
        this.goalSelector = new GoalSelector(param1.getProfilerSupplier());
        this.targetSelector = new GoalSelector(param1.getProfilerSupplier());
        this.lookControl = new LookControl(this);
        this.moveControl = new MoveControl(this);
        this.jumpControl = new JumpControl(this);
        this.bodyRotationControl = this.createBodyControl();
        this.navigation = this.createNavigation(param1);
        this.sensing = new Sensing(this);
        Arrays.fill(this.armorDropChances, 0.085F);
        Arrays.fill(this.handDropChances, 0.085F);
        if (param1 != null && !param1.isClientSide) {
            this.registerGoals();
        }

    }

    protected void registerGoals() {
    }

    public static AttributeSupplier.Builder createMobAttributes() {
        return LivingEntity.createLivingAttributes().add(Attributes.FOLLOW_RANGE, 16.0).add(Attributes.ATTACK_KNOCKBACK);
    }

    protected PathNavigation createNavigation(Level param0) {
        return new GroundPathNavigation(this, param0);
    }

    protected boolean shouldPassengersInheritMalus() {
        return false;
    }

    public float getPathfindingMalus(BlockPathTypes param0) {
        Mob var1;
        label17: {
            Entity var4 = this.getControlledVehicle();
            if (var4 instanceof Mob var0 && var0.shouldPassengersInheritMalus()) {
                var1 = var0;
                break label17;
            }

            var1 = this;
        }

        Float var3 = var1.pathfindingMalus.get(param0);
        return var3 == null ? param0.getMalus() : var3;
    }

    public void setPathfindingMalus(BlockPathTypes param0, float param1) {
        this.pathfindingMalus.put(param0, param1);
    }

    public void onPathfindingStart() {
    }

    public void onPathfindingDone() {
    }

    protected BodyRotationControl createBodyControl() {
        return new BodyRotationControl(this);
    }

    public LookControl getLookControl() {
        return this.lookControl;
    }

    public MoveControl getMoveControl() {
        Entity var2 = this.getControlledVehicle();
        return var2 instanceof Mob var0 ? var0.getMoveControl() : this.moveControl;
    }

    public JumpControl getJumpControl() {
        return this.jumpControl;
    }

    public PathNavigation getNavigation() {
        Entity var2 = this.getControlledVehicle();
        return var2 instanceof Mob var0 ? var0.getNavigation() : this.navigation;
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        Entity var0 = this.getFirstPassenger();
        if (!this.isNoAi() && var0 instanceof Mob var1 && var0.canControlVehicle()) {
            return var1;
        }

        return null;
    }

    public Sensing getSensing() {
        return this.sensing;
    }

    @Nullable
    @Override
    public LivingEntity getTarget() {
        return this.target;
    }

    public void setTarget(@Nullable LivingEntity param0) {
        this.target = param0;
    }

    @Override
    public boolean canAttackType(EntityType<?> param0) {
        return param0 != EntityType.GHAST;
    }

    public boolean canFireProjectileWeapon(ProjectileWeaponItem param0) {
        return false;
    }

    public void ate() {
        this.gameEvent(GameEvent.EAT);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_MOB_FLAGS_ID, (byte)0);
    }

    public int getAmbientSoundInterval() {
        return 80;
    }

    public void playAmbientSound() {
        SoundEvent var0 = this.getAmbientSound();
        if (var0 != null) {
            this.playSound(var0, this.getSoundVolume(), this.getVoicePitch());
        }

    }

    @Override
    public void baseTick() {
        super.baseTick();
        this.level().getProfiler().push("mobBaseTick");
        if (this.isAlive() && this.random.nextInt(1000) < this.ambientSoundTime++) {
            this.resetAmbientSoundTime();
            this.playAmbientSound();
        }

        this.level().getProfiler().pop();
    }

    @Override
    protected void playHurtSound(DamageSource param0) {
        this.resetAmbientSoundTime();
        super.playHurtSound(param0);
    }

    private void resetAmbientSoundTime() {
        this.ambientSoundTime = -this.getAmbientSoundInterval();
    }

    @Override
    public int getExperienceReward() {
        if (this.xpReward > 0) {
            int var0 = this.xpReward;

            for(int var1 = 0; var1 < this.armorItems.size(); ++var1) {
                if (!this.armorItems.get(var1).isEmpty() && this.armorDropChances[var1] <= 1.0F) {
                    var0 += 1 + this.random.nextInt(3);
                }
            }

            for(int var2 = 0; var2 < this.handItems.size(); ++var2) {
                if (!this.handItems.get(var2).isEmpty() && this.handDropChances[var2] <= 1.0F) {
                    var0 += 1 + this.random.nextInt(3);
                }
            }

            return var0;
        } else {
            return this.xpReward;
        }
    }

    public void spawnAnim() {
        if (this.level().isClientSide) {
            for(int var0 = 0; var0 < 20; ++var0) {
                double var1 = this.random.nextGaussian() * 0.02;
                double var2 = this.random.nextGaussian() * 0.02;
                double var3 = this.random.nextGaussian() * 0.02;
                double var4 = 10.0;
                this.level()
                    .addParticle(
                        ParticleTypes.POOF, this.getX(1.0) - var1 * 10.0, this.getRandomY() - var2 * 10.0, this.getRandomZ(1.0) - var3 * 10.0, var1, var2, var3
                    );
            }
        } else {
            this.level().broadcastEntityEvent(this, (byte)20);
        }

    }

    @Override
    public void handleEntityEvent(byte param0) {
        if (param0 == 20) {
            this.spawnAnim();
        } else {
            super.handleEntityEvent(param0);
        }

    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            this.tickLeash();
            if (this.tickCount % 5 == 0) {
                this.updateControlFlags();
            }
        }

    }

    protected void updateControlFlags() {
        boolean var0 = !(this.getControllingPassenger() instanceof Mob);
        boolean var1 = !(this.getVehicle() instanceof Boat);
        this.goalSelector.setControlFlag(Goal.Flag.MOVE, var0);
        this.goalSelector.setControlFlag(Goal.Flag.JUMP, var0 && var1);
        this.goalSelector.setControlFlag(Goal.Flag.LOOK, var0);
    }

    @Override
    protected float tickHeadTurn(float param0, float param1) {
        this.bodyRotationControl.clientTick();
        return param1;
    }

    @Nullable
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        param0.putBoolean("CanPickUpLoot", this.canPickUpLoot());
        param0.putBoolean("PersistenceRequired", this.persistenceRequired);
        ListTag var0 = new ListTag();

        for(ItemStack var1 : this.armorItems) {
            CompoundTag var2 = new CompoundTag();
            if (!var1.isEmpty()) {
                var1.save(var2);
            }

            var0.add(var2);
        }

        param0.put("ArmorItems", var0);
        ListTag var3 = new ListTag();

        for(ItemStack var4 : this.handItems) {
            CompoundTag var5 = new CompoundTag();
            if (!var4.isEmpty()) {
                var4.save(var5);
            }

            var3.add(var5);
        }

        param0.put("HandItems", var3);
        ListTag var6 = new ListTag();

        for(float var7 : this.armorDropChances) {
            var6.add(FloatTag.valueOf(var7));
        }

        param0.put("ArmorDropChances", var6);
        ListTag var8 = new ListTag();

        for(float var9 : this.handDropChances) {
            var8.add(FloatTag.valueOf(var9));
        }

        param0.put("HandDropChances", var8);
        if (this.leashHolder != null) {
            CompoundTag var10 = new CompoundTag();
            if (this.leashHolder instanceof LivingEntity) {
                UUID var11 = this.leashHolder.getUUID();
                var10.putUUID("UUID", var11);
            } else if (this.leashHolder instanceof HangingEntity) {
                BlockPos var12 = ((HangingEntity)this.leashHolder).getPos();
                var10.putInt("X", var12.getX());
                var10.putInt("Y", var12.getY());
                var10.putInt("Z", var12.getZ());
            }

            param0.put("Leash", var10);
        } else if (this.leashInfoTag != null) {
            param0.put("Leash", this.leashInfoTag.copy());
        }

        param0.putBoolean("LeftHanded", this.isLeftHanded());
        if (this.lootTable != null) {
            param0.putString("DeathLootTable", this.lootTable.toString());
            if (this.lootTableSeed != 0L) {
                param0.putLong("DeathLootTableSeed", this.lootTableSeed);
            }
        }

        if (this.isNoAi()) {
            param0.putBoolean("NoAI", this.isNoAi());
        }

    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        if (param0.contains("CanPickUpLoot", 1)) {
            this.setCanPickUpLoot(param0.getBoolean("CanPickUpLoot"));
        }

        this.persistenceRequired = param0.getBoolean("PersistenceRequired");
        if (param0.contains("ArmorItems", 9)) {
            ListTag var0 = param0.getList("ArmorItems", 10);

            for(int var1 = 0; var1 < this.armorItems.size(); ++var1) {
                this.armorItems.set(var1, ItemStack.of(var0.getCompound(var1)));
            }
        }

        if (param0.contains("HandItems", 9)) {
            ListTag var2 = param0.getList("HandItems", 10);

            for(int var3 = 0; var3 < this.handItems.size(); ++var3) {
                this.handItems.set(var3, ItemStack.of(var2.getCompound(var3)));
            }
        }

        if (param0.contains("ArmorDropChances", 9)) {
            ListTag var4 = param0.getList("ArmorDropChances", 5);

            for(int var5 = 0; var5 < var4.size(); ++var5) {
                this.armorDropChances[var5] = var4.getFloat(var5);
            }
        }

        if (param0.contains("HandDropChances", 9)) {
            ListTag var6 = param0.getList("HandDropChances", 5);

            for(int var7 = 0; var7 < var6.size(); ++var7) {
                this.handDropChances[var7] = var6.getFloat(var7);
            }
        }

        if (param0.contains("Leash", 10)) {
            this.leashInfoTag = param0.getCompound("Leash");
        }

        this.setLeftHanded(param0.getBoolean("LeftHanded"));
        if (param0.contains("DeathLootTable", 8)) {
            this.lootTable = new ResourceLocation(param0.getString("DeathLootTable"));
            this.lootTableSeed = param0.getLong("DeathLootTableSeed");
        }

        this.setNoAi(param0.getBoolean("NoAI"));
    }

    @Override
    protected void dropFromLootTable(DamageSource param0, boolean param1) {
        super.dropFromLootTable(param0, param1);
        this.lootTable = null;
    }

    @Override
    public final ResourceLocation getLootTable() {
        return this.lootTable == null ? this.getDefaultLootTable() : this.lootTable;
    }

    protected ResourceLocation getDefaultLootTable() {
        return super.getLootTable();
    }

    @Override
    public long getLootTableSeed() {
        return this.lootTableSeed;
    }

    public void setZza(float param0) {
        this.zza = param0;
    }

    public void setYya(float param0) {
        this.yya = param0;
    }

    public void setXxa(float param0) {
        this.xxa = param0;
    }

    @Override
    public void setSpeed(float param0) {
        super.setSpeed(param0);
        this.setZza(param0);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.level().getProfiler().push("looting");
        if (!this.level().isClientSide
            && this.canPickUpLoot()
            && this.isAlive()
            && !this.dead
            && this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            Vec3i var0 = this.getPickupReach();

            for(ItemEntity var2 : this.level()
                .getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate((double)var0.getX(), (double)var0.getY(), (double)var0.getZ()))) {
                if (!var2.isRemoved() && !var2.getItem().isEmpty() && !var2.hasPickUpDelay() && this.wantsToPickUp(var2.getItem())) {
                    this.pickUpItem(var2);
                }
            }
        }

        this.level().getProfiler().pop();
    }

    protected Vec3i getPickupReach() {
        return ITEM_PICKUP_REACH;
    }

    protected void pickUpItem(ItemEntity param0) {
        ItemStack var0 = param0.getItem();
        ItemStack var1 = this.equipItemIfPossible(var0.copy());
        if (!var1.isEmpty()) {
            this.onItemPickup(param0);
            this.take(param0, var1.getCount());
            var0.shrink(var1.getCount());
            if (var0.isEmpty()) {
                param0.discard();
            }
        }

    }

    public ItemStack equipItemIfPossible(ItemStack param0) {
        EquipmentSlot var0 = getEquipmentSlotForItem(param0);
        ItemStack var1 = this.getItemBySlot(var0);
        boolean var2 = this.canReplaceCurrentItem(param0, var1);
        if (var0.isArmor() && !var2) {
            var0 = EquipmentSlot.MAINHAND;
            var1 = this.getItemBySlot(var0);
            var2 = var1.isEmpty();
        }

        if (var2 && this.canHoldItem(param0)) {
            double var3 = (double)this.getEquipmentDropChance(var0);
            if (!var1.isEmpty() && (double)Math.max(this.random.nextFloat() - 0.1F, 0.0F) < var3) {
                this.spawnAtLocation(var1);
            }

            if (var0.isArmor() && param0.getCount() > 1) {
                ItemStack var4 = param0.copyWithCount(1);
                this.setItemSlotAndDropWhenKilled(var0, var4);
                return var4;
            } else {
                this.setItemSlotAndDropWhenKilled(var0, param0);
                return param0;
            }
        } else {
            return ItemStack.EMPTY;
        }
    }

    protected void setItemSlotAndDropWhenKilled(EquipmentSlot param0, ItemStack param1) {
        this.setItemSlot(param0, param1);
        this.setGuaranteedDrop(param0);
        this.persistenceRequired = true;
    }

    public void setGuaranteedDrop(EquipmentSlot param0) {
        switch(param0.getType()) {
            case HAND:
                this.handDropChances[param0.getIndex()] = 2.0F;
                break;
            case ARMOR:
                this.armorDropChances[param0.getIndex()] = 2.0F;
        }

    }

    protected boolean canReplaceCurrentItem(ItemStack param0, ItemStack param1) {
        if (param1.isEmpty()) {
            return true;
        } else if (param0.getItem() instanceof SwordItem) {
            if (!(param1.getItem() instanceof SwordItem)) {
                return true;
            } else {
                SwordItem var0 = (SwordItem)param0.getItem();
                SwordItem var1 = (SwordItem)param1.getItem();
                if (var0.getDamage() != var1.getDamage()) {
                    return var0.getDamage() > var1.getDamage();
                } else {
                    return this.canReplaceEqualItem(param0, param1);
                }
            }
        } else if (param0.getItem() instanceof BowItem && param1.getItem() instanceof BowItem) {
            return this.canReplaceEqualItem(param0, param1);
        } else if (param0.getItem() instanceof CrossbowItem && param1.getItem() instanceof CrossbowItem) {
            return this.canReplaceEqualItem(param0, param1);
        } else {
            Item var4 = param0.getItem();
            if (var4 instanceof ArmorItem var2) {
                if (EnchantmentHelper.hasBindingCurse(param1)) {
                    return false;
                } else if (!(param1.getItem() instanceof ArmorItem)) {
                    return true;
                } else {
                    ArmorItem var3 = (ArmorItem)param1.getItem();
                    if (var2.getDefense() != var3.getDefense()) {
                        return var2.getDefense() > var3.getDefense();
                    } else if (var2.getToughness() != var3.getToughness()) {
                        return var2.getToughness() > var3.getToughness();
                    } else {
                        return this.canReplaceEqualItem(param0, param1);
                    }
                }
            } else {
                if (param0.getItem() instanceof DiggerItem) {
                    if (param1.getItem() instanceof BlockItem) {
                        return true;
                    }

                    Item var5 = param1.getItem();
                    if (var5 instanceof DiggerItem var4) {
                        DiggerItem var5x = (DiggerItem)param0.getItem();
                        if (var5x.getAttackDamage() != var4.getAttackDamage()) {
                            return var5x.getAttackDamage() > var4.getAttackDamage();
                        }

                        return this.canReplaceEqualItem(param0, param1);
                    }
                }

                return false;
            }
        }
    }

    public boolean canReplaceEqualItem(ItemStack param0, ItemStack param1) {
        if (param0.getDamageValue() >= param1.getDamageValue() && (!param0.hasTag() || param1.hasTag())) {
            if (param0.hasTag() && param1.hasTag()) {
                return param0.getTag().getAllKeys().stream().anyMatch(param0x -> !param0x.equals("Damage"))
                    && !param1.getTag().getAllKeys().stream().anyMatch(param0x -> !param0x.equals("Damage"));
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public boolean canHoldItem(ItemStack param0) {
        return true;
    }

    public boolean wantsToPickUp(ItemStack param0) {
        return this.canHoldItem(param0);
    }

    public boolean removeWhenFarAway(double param0) {
        return true;
    }

    public boolean requiresCustomPersistence() {
        return this.isPassenger();
    }

    protected boolean shouldDespawnInPeaceful() {
        return false;
    }

    @Override
    public void checkDespawn() {
        if (this.level().getDifficulty() == Difficulty.PEACEFUL && this.shouldDespawnInPeaceful()) {
            this.discard();
        } else if (!this.isPersistenceRequired() && !this.requiresCustomPersistence()) {
            Entity var0 = this.level().getNearestPlayer(this, -1.0);
            if (var0 != null) {
                double var1 = var0.distanceToSqr(this);
                int var2 = this.getType().getCategory().getDespawnDistance();
                int var3 = var2 * var2;
                if (var1 > (double)var3 && this.removeWhenFarAway(var1)) {
                    this.discard();
                }

                int var4 = this.getType().getCategory().getNoDespawnDistance();
                int var5 = var4 * var4;
                if (this.noActionTime > 600 && this.random.nextInt(800) == 0 && var1 > (double)var5 && this.removeWhenFarAway(var1)) {
                    this.discard();
                } else if (var1 < (double)var5) {
                    this.noActionTime = 0;
                }
            }

        } else {
            this.noActionTime = 0;
        }
    }

    @Override
    protected final void serverAiStep() {
        ++this.noActionTime;
        this.level().getProfiler().push("sensing");
        this.sensing.tick();
        this.level().getProfiler().pop();
        int var0 = this.level().getServer().getTickCount() + this.getId();
        if (var0 % 2 != 0 && this.tickCount > 1) {
            this.level().getProfiler().push("targetSelector");
            this.targetSelector.tickRunningGoals(false);
            this.level().getProfiler().pop();
            this.level().getProfiler().push("goalSelector");
            this.goalSelector.tickRunningGoals(false);
            this.level().getProfiler().pop();
        } else {
            this.level().getProfiler().push("targetSelector");
            this.targetSelector.tick();
            this.level().getProfiler().pop();
            this.level().getProfiler().push("goalSelector");
            this.goalSelector.tick();
            this.level().getProfiler().pop();
        }

        this.level().getProfiler().push("navigation");
        this.navigation.tick();
        this.level().getProfiler().pop();
        this.level().getProfiler().push("mob tick");
        this.customServerAiStep();
        this.level().getProfiler().pop();
        this.level().getProfiler().push("controls");
        this.level().getProfiler().push("move");
        this.moveControl.tick();
        this.level().getProfiler().popPush("look");
        this.lookControl.tick();
        this.level().getProfiler().popPush("jump");
        this.jumpControl.tick();
        this.level().getProfiler().pop();
        this.level().getProfiler().pop();
        this.sendDebugPackets();
    }

    protected void sendDebugPackets() {
        DebugPackets.sendGoalSelector(this.level(), this, this.goalSelector);
    }

    protected void customServerAiStep() {
    }

    public int getMaxHeadXRot() {
        return 40;
    }

    public int getMaxHeadYRot() {
        return 75;
    }

    public int getHeadRotSpeed() {
        return 10;
    }

    public void lookAt(Entity param0, float param1, float param2) {
        double var0 = param0.getX() - this.getX();
        double var1 = param0.getZ() - this.getZ();
        double var3;
        if (param0 instanceof LivingEntity var2) {
            var3 = var2.getEyeY() - this.getEyeY();
        } else {
            var3 = (param0.getBoundingBox().minY + param0.getBoundingBox().maxY) / 2.0 - this.getEyeY();
        }

        double var5 = Math.sqrt(var0 * var0 + var1 * var1);
        float var6 = (float)(Mth.atan2(var1, var0) * 180.0F / (float)Math.PI) - 90.0F;
        float var7 = (float)(-(Mth.atan2(var3, var5) * 180.0F / (float)Math.PI));
        this.setXRot(this.rotlerp(this.getXRot(), var7, param2));
        this.setYRot(this.rotlerp(this.getYRot(), var6, param1));
    }

    private float rotlerp(float param0, float param1, float param2) {
        float var0 = Mth.wrapDegrees(param1 - param0);
        if (var0 > param2) {
            var0 = param2;
        }

        if (var0 < -param2) {
            var0 = -param2;
        }

        return param0 + var0;
    }

    public static boolean checkMobSpawnRules(EntityType<? extends Mob> param0, LevelAccessor param1, MobSpawnType param2, BlockPos param3, RandomSource param4) {
        BlockPos var0 = param3.below();
        return param2 == MobSpawnType.SPAWNER || param1.getBlockState(var0).isValidSpawn(param1, var0, param0);
    }

    public boolean checkSpawnRules(LevelAccessor param0, MobSpawnType param1) {
        return true;
    }

    public boolean checkSpawnObstruction(LevelReader param0) {
        return !param0.containsAnyLiquid(this.getBoundingBox()) && param0.isUnobstructed(this);
    }

    public int getMaxSpawnClusterSize() {
        return 4;
    }

    public boolean isMaxGroupSizeReached(int param0) {
        return false;
    }

    @Override
    public int getMaxFallDistance() {
        if (this.getTarget() == null) {
            return 3;
        } else {
            int var0 = (int)(this.getHealth() - this.getMaxHealth() * 0.33F);
            var0 -= (3 - this.level().getDifficulty().getId()) * 4;
            if (var0 < 0) {
                var0 = 0;
            }

            return var0 + 3;
        }
    }

    @Override
    public Iterable<ItemStack> getHandSlots() {
        return this.handItems;
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return this.armorItems;
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot param0) {
        switch(param0.getType()) {
            case HAND:
                return this.handItems.get(param0.getIndex());
            case ARMOR:
                return this.armorItems.get(param0.getIndex());
            default:
                return ItemStack.EMPTY;
        }
    }

    @Override
    public void setItemSlot(EquipmentSlot param0, ItemStack param1) {
        this.verifyEquippedItem(param1);
        switch(param0.getType()) {
            case HAND:
                this.onEquipItem(param0, this.handItems.set(param0.getIndex(), param1), param1);
                break;
            case ARMOR:
                this.onEquipItem(param0, this.armorItems.set(param0.getIndex(), param1), param1);
        }

    }

    @Override
    protected void dropCustomDeathLoot(DamageSource param0, int param1, boolean param2) {
        super.dropCustomDeathLoot(param0, param1, param2);

        for(EquipmentSlot var0 : EquipmentSlot.values()) {
            ItemStack var1 = this.getItemBySlot(var0);
            float var2 = this.getEquipmentDropChance(var0);
            boolean var3 = var2 > 1.0F;
            if (!var1.isEmpty()
                && !EnchantmentHelper.hasVanishingCurse(var1)
                && (param2 || var3)
                && Math.max(this.random.nextFloat() - (float)param1 * 0.01F, 0.0F) < var2) {
                if (!var3 && var1.isDamageableItem()) {
                    var1.setDamageValue(var1.getMaxDamage() - this.random.nextInt(1 + this.random.nextInt(Math.max(var1.getMaxDamage() - 3, 1))));
                }

                this.spawnAtLocation(var1);
                this.setItemSlot(var0, ItemStack.EMPTY);
            }
        }

    }

    protected float getEquipmentDropChance(EquipmentSlot param0) {
        return switch(param0.getType()) {
            case HAND -> this.handDropChances[param0.getIndex()];
            case ARMOR -> this.armorDropChances[param0.getIndex()];
            default -> 0.0F;
        };
    }

    protected void populateDefaultEquipmentSlots(RandomSource param0, DifficultyInstance param1) {
        if (param0.nextFloat() < 0.15F * param1.getSpecialMultiplier()) {
            int var0 = param0.nextInt(2);
            float var1 = this.level().getDifficulty() == Difficulty.HARD ? 0.1F : 0.25F;
            if (param0.nextFloat() < 0.095F) {
                ++var0;
            }

            if (param0.nextFloat() < 0.095F) {
                ++var0;
            }

            if (param0.nextFloat() < 0.095F) {
                ++var0;
            }

            boolean var2 = true;

            for(EquipmentSlot var3 : EquipmentSlot.values()) {
                if (var3.getType() == EquipmentSlot.Type.ARMOR) {
                    ItemStack var4 = this.getItemBySlot(var3);
                    if (!var2 && param0.nextFloat() < var1) {
                        break;
                    }

                    var2 = false;
                    if (var4.isEmpty()) {
                        Item var5 = getEquipmentForSlot(var3, var0);
                        if (var5 != null) {
                            this.setItemSlot(var3, new ItemStack(var5));
                        }
                    }
                }
            }
        }

    }

    @Nullable
    public static Item getEquipmentForSlot(EquipmentSlot param0, int param1) {
        switch(param0) {
            case HEAD:
                if (param1 == 0) {
                    return Items.LEATHER_HELMET;
                } else if (param1 == 1) {
                    return Items.GOLDEN_HELMET;
                } else if (param1 == 2) {
                    return Items.CHAINMAIL_HELMET;
                } else if (param1 == 3) {
                    return Items.IRON_HELMET;
                } else if (param1 == 4) {
                    return Items.DIAMOND_HELMET;
                }
            case CHEST:
                if (param1 == 0) {
                    return Items.LEATHER_CHESTPLATE;
                } else if (param1 == 1) {
                    return Items.GOLDEN_CHESTPLATE;
                } else if (param1 == 2) {
                    return Items.CHAINMAIL_CHESTPLATE;
                } else if (param1 == 3) {
                    return Items.IRON_CHESTPLATE;
                } else if (param1 == 4) {
                    return Items.DIAMOND_CHESTPLATE;
                }
            case LEGS:
                if (param1 == 0) {
                    return Items.LEATHER_LEGGINGS;
                } else if (param1 == 1) {
                    return Items.GOLDEN_LEGGINGS;
                } else if (param1 == 2) {
                    return Items.CHAINMAIL_LEGGINGS;
                } else if (param1 == 3) {
                    return Items.IRON_LEGGINGS;
                } else if (param1 == 4) {
                    return Items.DIAMOND_LEGGINGS;
                }
            case FEET:
                if (param1 == 0) {
                    return Items.LEATHER_BOOTS;
                } else if (param1 == 1) {
                    return Items.GOLDEN_BOOTS;
                } else if (param1 == 2) {
                    return Items.CHAINMAIL_BOOTS;
                } else if (param1 == 3) {
                    return Items.IRON_BOOTS;
                } else if (param1 == 4) {
                    return Items.DIAMOND_BOOTS;
                }
            default:
                return null;
        }
    }

    protected void populateDefaultEquipmentEnchantments(RandomSource param0, DifficultyInstance param1) {
        float var0 = param1.getSpecialMultiplier();
        this.enchantSpawnedWeapon(param0, var0);

        for(EquipmentSlot var1 : EquipmentSlot.values()) {
            if (var1.getType() == EquipmentSlot.Type.ARMOR) {
                this.enchantSpawnedArmor(param0, var0, var1);
            }
        }

    }

    protected void enchantSpawnedWeapon(RandomSource param0, float param1) {
        if (!this.getMainHandItem().isEmpty() && param0.nextFloat() < 0.25F * param1) {
            this.setItemSlot(
                EquipmentSlot.MAINHAND, EnchantmentHelper.enchantItem(param0, this.getMainHandItem(), (int)(5.0F + param1 * (float)param0.nextInt(18)), false)
            );
        }

    }

    protected void enchantSpawnedArmor(RandomSource param0, float param1, EquipmentSlot param2) {
        ItemStack var0 = this.getItemBySlot(param2);
        if (!var0.isEmpty() && param0.nextFloat() < 0.5F * param1) {
            this.setItemSlot(param2, EnchantmentHelper.enchantItem(param0, var0, (int)(5.0F + param1 * (float)param0.nextInt(18)), false));
        }

    }

    @Nullable
    public SpawnGroupData finalizeSpawn(
        ServerLevelAccessor param0, DifficultyInstance param1, MobSpawnType param2, @Nullable SpawnGroupData param3, @Nullable CompoundTag param4
    ) {
        RandomSource var0 = param0.getRandom();
        this.getAttribute(Attributes.FOLLOW_RANGE)
            .addPermanentModifier(
                new AttributeModifier("Random spawn bonus", var0.triangle(0.0, 0.11485000000000001), AttributeModifier.Operation.MULTIPLY_BASE)
            );
        if (var0.nextFloat() < 0.05F) {
            this.setLeftHanded(true);
        } else {
            this.setLeftHanded(false);
        }

        return param3;
    }

    public void setPersistenceRequired() {
        this.persistenceRequired = true;
    }

    public void setDropChance(EquipmentSlot param0, float param1) {
        switch(param0.getType()) {
            case HAND:
                this.handDropChances[param0.getIndex()] = param1;
                break;
            case ARMOR:
                this.armorDropChances[param0.getIndex()] = param1;
        }

    }

    public boolean canPickUpLoot() {
        return this.canPickUpLoot;
    }

    public void setCanPickUpLoot(boolean param0) {
        this.canPickUpLoot = param0;
    }

    @Override
    public boolean canTakeItem(ItemStack param0) {
        EquipmentSlot var0 = getEquipmentSlotForItem(param0);
        return this.getItemBySlot(var0).isEmpty() && this.canPickUpLoot();
    }

    public boolean isPersistenceRequired() {
        return this.persistenceRequired;
    }

    @Override
    public final InteractionResult interact(Player param0, InteractionHand param1) {
        if (!this.isAlive()) {
            return InteractionResult.PASS;
        } else if (this.getLeashHolder() == param0) {
            this.dropLeash(true, !param0.getAbilities().instabuild);
            this.gameEvent(GameEvent.ENTITY_INTERACT, param0);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        } else {
            InteractionResult var0 = this.checkAndHandleImportantInteractions(param0, param1);
            if (var0.consumesAction()) {
                this.gameEvent(GameEvent.ENTITY_INTERACT, param0);
                return var0;
            } else {
                var0 = this.mobInteract(param0, param1);
                if (var0.consumesAction()) {
                    this.gameEvent(GameEvent.ENTITY_INTERACT, param0);
                    return var0;
                } else {
                    return super.interact(param0, param1);
                }
            }
        }
    }

    private InteractionResult checkAndHandleImportantInteractions(Player param0, InteractionHand param1) {
        ItemStack var0 = param0.getItemInHand(param1);
        if (var0.is(Items.LEAD) && this.canBeLeashed(param0)) {
            this.setLeashedTo(param0, true);
            var0.shrink(1);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        } else {
            if (var0.is(Items.NAME_TAG)) {
                InteractionResult var1 = var0.interactLivingEntity(param0, this, param1);
                if (var1.consumesAction()) {
                    return var1;
                }
            }

            if (var0.getItem() instanceof SpawnEggItem) {
                if (this.level() instanceof ServerLevel) {
                    SpawnEggItem var2 = (SpawnEggItem)var0.getItem();
                    Optional<Mob> var3 = var2.spawnOffspringFromSpawnEgg(param0, this, this.getType(), (ServerLevel)this.level(), this.position(), var0);
                    var3.ifPresent(param1x -> this.onOffspringSpawnedFromEgg(param0, param1x));
                    return var3.isPresent() ? InteractionResult.SUCCESS : InteractionResult.PASS;
                } else {
                    return InteractionResult.CONSUME;
                }
            } else {
                return InteractionResult.PASS;
            }
        }
    }

    protected void onOffspringSpawnedFromEgg(Player param0, Mob param1) {
    }

    protected InteractionResult mobInteract(Player param0, InteractionHand param1) {
        return InteractionResult.PASS;
    }

    public boolean isWithinRestriction() {
        return this.isWithinRestriction(this.blockPosition());
    }

    public boolean isWithinRestriction(BlockPos param0) {
        if (this.restrictRadius == -1.0F) {
            return true;
        } else {
            return this.restrictCenter.distSqr(param0) < (double)(this.restrictRadius * this.restrictRadius);
        }
    }

    public void restrictTo(BlockPos param0, int param1) {
        this.restrictCenter = param0;
        this.restrictRadius = (float)param1;
    }

    public BlockPos getRestrictCenter() {
        return this.restrictCenter;
    }

    public float getRestrictRadius() {
        return this.restrictRadius;
    }

    public void clearRestriction() {
        this.restrictRadius = -1.0F;
    }

    public boolean hasRestriction() {
        return this.restrictRadius != -1.0F;
    }

    @Nullable
    public <T extends Mob> T convertTo(EntityType<T> param0, boolean param1) {
        if (this.isRemoved()) {
            return null;
        } else {
            T var0 = param0.create(this.level());
            if (var0 == null) {
                return null;
            } else {
                var0.copyPosition(this);
                var0.setBaby(this.isBaby());
                var0.setNoAi(this.isNoAi());
                if (this.hasCustomName()) {
                    var0.setCustomName(this.getCustomName());
                    var0.setCustomNameVisible(this.isCustomNameVisible());
                }

                if (this.isPersistenceRequired()) {
                    var0.setPersistenceRequired();
                }

                var0.setInvulnerable(this.isInvulnerable());
                if (param1) {
                    var0.setCanPickUpLoot(this.canPickUpLoot());

                    for(EquipmentSlot var1 : EquipmentSlot.values()) {
                        ItemStack var2 = this.getItemBySlot(var1);
                        if (!var2.isEmpty()) {
                            var0.setItemSlot(var1, var2.copyAndClear());
                            var0.setDropChance(var1, this.getEquipmentDropChance(var1));
                        }
                    }
                }

                this.level().addFreshEntity(var0);
                if (this.isPassenger()) {
                    Entity var3 = this.getVehicle();
                    this.stopRiding();
                    var0.startRiding(var3, true);
                }

                this.discard();
                return var0;
            }
        }
    }

    protected void tickLeash() {
        if (this.leashInfoTag != null) {
            this.restoreLeashFromSave();
        }

        if (this.leashHolder != null) {
            if (!this.isAlive() || !this.leashHolder.isAlive()) {
                this.dropLeash(true, true);
            }

        }
    }

    public void dropLeash(boolean param0, boolean param1) {
        if (this.leashHolder != null) {
            this.leashHolder = null;
            this.leashInfoTag = null;
            if (!this.level().isClientSide && param1) {
                this.spawnAtLocation(Items.LEAD);
            }

            if (!this.level().isClientSide && param0 && this.level() instanceof ServerLevel) {
                ((ServerLevel)this.level()).getChunkSource().broadcast(this, new ClientboundSetEntityLinkPacket(this, null));
            }
        }

    }

    public boolean canBeLeashed(Player param0) {
        return !this.isLeashed() && !(this instanceof Enemy);
    }

    public boolean isLeashed() {
        return this.leashHolder != null;
    }

    @Nullable
    public Entity getLeashHolder() {
        if (this.leashHolder == null && this.delayedLeashHolderId != 0 && this.level().isClientSide) {
            this.leashHolder = this.level().getEntity(this.delayedLeashHolderId);
        }

        return this.leashHolder;
    }

    public void setLeashedTo(Entity param0, boolean param1) {
        this.leashHolder = param0;
        this.leashInfoTag = null;
        if (!this.level().isClientSide && param1 && this.level() instanceof ServerLevel) {
            ((ServerLevel)this.level()).getChunkSource().broadcast(this, new ClientboundSetEntityLinkPacket(this, this.leashHolder));
        }

        if (this.isPassenger()) {
            this.stopRiding();
        }

    }

    public void setDelayedLeashHolderId(int param0) {
        this.delayedLeashHolderId = param0;
        this.dropLeash(false, false);
    }

    @Override
    public boolean startRiding(Entity param0, boolean param1) {
        boolean var0 = super.startRiding(param0, param1);
        if (var0 && this.isLeashed()) {
            this.dropLeash(true, true);
        }

        return var0;
    }

    private void restoreLeashFromSave() {
        if (this.leashInfoTag != null && this.level() instanceof ServerLevel) {
            if (this.leashInfoTag.hasUUID("UUID")) {
                UUID var0 = this.leashInfoTag.getUUID("UUID");
                Entity var1 = ((ServerLevel)this.level()).getEntity(var0);
                if (var1 != null) {
                    this.setLeashedTo(var1, true);
                    return;
                }
            } else if (this.leashInfoTag.contains("X", 99) && this.leashInfoTag.contains("Y", 99) && this.leashInfoTag.contains("Z", 99)) {
                BlockPos var2 = NbtUtils.readBlockPos(this.leashInfoTag);
                this.setLeashedTo(LeashFenceKnotEntity.getOrCreateKnot(this.level(), var2), true);
                return;
            }

            if (this.tickCount > 100) {
                this.spawnAtLocation(Items.LEAD);
                this.leashInfoTag = null;
            }
        }

    }

    @Override
    public boolean isEffectiveAi() {
        return super.isEffectiveAi() && !this.isNoAi();
    }

    public void setNoAi(boolean param0) {
        byte var0 = this.entityData.get(DATA_MOB_FLAGS_ID);
        this.entityData.set(DATA_MOB_FLAGS_ID, param0 ? (byte)(var0 | 1) : (byte)(var0 & -2));
    }

    public void setLeftHanded(boolean param0) {
        byte var0 = this.entityData.get(DATA_MOB_FLAGS_ID);
        this.entityData.set(DATA_MOB_FLAGS_ID, param0 ? (byte)(var0 | 2) : (byte)(var0 & -3));
    }

    public void setAggressive(boolean param0) {
        byte var0 = this.entityData.get(DATA_MOB_FLAGS_ID);
        this.entityData.set(DATA_MOB_FLAGS_ID, param0 ? (byte)(var0 | 4) : (byte)(var0 & -5));
    }

    public boolean isNoAi() {
        return (this.entityData.get(DATA_MOB_FLAGS_ID) & 1) != 0;
    }

    public boolean isLeftHanded() {
        return (this.entityData.get(DATA_MOB_FLAGS_ID) & 2) != 0;
    }

    public boolean isAggressive() {
        return (this.entityData.get(DATA_MOB_FLAGS_ID) & 4) != 0;
    }

    public void setBaby(boolean param0) {
    }

    @Override
    public HumanoidArm getMainArm() {
        return this.isLeftHanded() ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
    }

    public boolean isWithinMeleeAttackRange(LivingEntity param0) {
        return this.getAttackBoundingBox().intersects(param0.getHitbox());
    }

    protected AABB getAttackBoundingBox() {
        Entity var0 = this.getVehicle();
        AABB var3;
        if (var0 != null) {
            AABB var1 = var0.getBoundingBox();
            AABB var2 = this.getBoundingBox();
            var3 = new AABB(
                Math.min(var2.minX, var1.minX),
                var2.minY,
                Math.min(var2.minZ, var1.minZ),
                Math.max(var2.maxX, var1.maxX),
                var2.maxY,
                Math.max(var2.maxZ, var1.maxZ)
            );
        } else {
            var3 = this.getBoundingBox();
        }

        return var3.inflate(DEFAULT_ATTACK_REACH, 0.0, DEFAULT_ATTACK_REACH);
    }

    @Override
    public boolean doHurtTarget(Entity param0) {
        float var0 = (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float var1 = (float)this.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
        if (param0 instanceof LivingEntity) {
            var0 += EnchantmentHelper.getDamageBonus(this.getMainHandItem(), ((LivingEntity)param0).getMobType());
            var1 += (float)EnchantmentHelper.getKnockbackBonus(this);
        }

        int var2 = EnchantmentHelper.getFireAspect(this);
        if (var2 > 0) {
            param0.setSecondsOnFire(var2 * 4);
        }

        boolean var3 = param0.hurt(this.damageSources().mobAttack(this), var0);
        if (var3) {
            if (var1 > 0.0F && param0 instanceof LivingEntity) {
                ((LivingEntity)param0)
                    .knockback(
                        (double)(var1 * 0.5F),
                        (double)Mth.sin(this.getYRot() * (float) (Math.PI / 180.0)),
                        (double)(-Mth.cos(this.getYRot() * (float) (Math.PI / 180.0)))
                    );
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.6, 1.0, 0.6));
            }

            if (param0 instanceof Player var4) {
                this.maybeDisableShield(var4, this.getMainHandItem(), var4.isUsingItem() ? var4.getUseItem() : ItemStack.EMPTY);
            }

            this.doEnchantDamageEffects(this, param0);
            this.setLastHurtMob(param0);
        }

        return var3;
    }

    private void maybeDisableShield(Player param0, ItemStack param1, ItemStack param2) {
        if (!param1.isEmpty() && !param2.isEmpty() && param1.getItem() instanceof AxeItem && param2.is(Items.SHIELD)) {
            float var0 = 0.25F + (float)EnchantmentHelper.getBlockEfficiency(this) * 0.05F;
            if (this.random.nextFloat() < var0) {
                param0.getCooldowns().addCooldown(Items.SHIELD, 100);
                this.level().broadcastEntityEvent(param0, (byte)30);
            }
        }

    }

    protected boolean isSunBurnTick() {
        if (this.level().isDay() && !this.level().isClientSide) {
            float var0 = this.getLightLevelDependentMagicValue();
            BlockPos var1 = BlockPos.containing(this.getX(), this.getEyeY(), this.getZ());
            boolean var2 = this.isInWaterRainOrBubble() || this.isInPowderSnow || this.wasInPowderSnow;
            if (var0 > 0.5F && this.random.nextFloat() * 30.0F < (var0 - 0.4F) * 2.0F && !var2 && this.level().canSeeSky(var1)) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void jumpInLiquid(TagKey<Fluid> param0) {
        if (this.getNavigation().canFloat()) {
            super.jumpInLiquid(param0);
        } else {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.3, 0.0));
        }

    }

    @VisibleForTesting
    public void removeFreeWill() {
        this.removeAllGoals(param0 -> true);
        this.getBrain().removeAllBehaviors();
    }

    public void removeAllGoals(Predicate<Goal> param0) {
        this.goalSelector.removeAllGoals(param0);
    }

    @Override
    protected void removeAfterChangingDimensions() {
        super.removeAfterChangingDimensions();
        this.dropLeash(true, false);
        this.getAllSlots().forEach(param0 -> {
            if (!param0.isEmpty()) {
                param0.setCount(0);
            }

        });
    }

    @Nullable
    @Override
    public ItemStack getPickResult() {
        SpawnEggItem var0 = SpawnEggItem.byId(this.getType());
        return var0 == null ? null : new ItemStack(var0);
    }
}
