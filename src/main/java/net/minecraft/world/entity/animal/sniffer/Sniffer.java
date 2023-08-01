package net.minecraft.world.entity.animal.sniffer;

import com.mojang.serialization.Dynamic;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class Sniffer extends Animal {
    private static final int DIGGING_PARTICLES_DELAY_TICKS = 1700;
    private static final int DIGGING_PARTICLES_DURATION_TICKS = 6000;
    private static final int DIGGING_PARTICLES_AMOUNT = 30;
    private static final int DIGGING_DROP_SEED_OFFSET_TICKS = 120;
    private static final int SNIFFER_BABY_AGE_TICKS = 48000;
    private static final float DIGGING_BB_HEIGHT_OFFSET = 0.4F;
    private static final EntityDimensions DIGGING_DIMENSIONS = EntityDimensions.scalable(EntityType.SNIFFER.getWidth(), EntityType.SNIFFER.getHeight() - 0.4F);
    private static final EntityDataAccessor<Sniffer.State> DATA_STATE = SynchedEntityData.defineId(Sniffer.class, EntityDataSerializers.SNIFFER_STATE);
    private static final EntityDataAccessor<Integer> DATA_DROP_SEED_AT_TICK = SynchedEntityData.defineId(Sniffer.class, EntityDataSerializers.INT);
    public final AnimationState feelingHappyAnimationState = new AnimationState();
    public final AnimationState scentingAnimationState = new AnimationState();
    public final AnimationState sniffingAnimationState = new AnimationState();
    public final AnimationState diggingAnimationState = new AnimationState();
    public final AnimationState risingAnimationState = new AnimationState();

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.1F).add(Attributes.MAX_HEALTH, 14.0);
    }

    public Sniffer(EntityType<? extends Animal> param0, Level param1) {
        super(param0, param1);
        this.entityData.define(DATA_STATE, Sniffer.State.IDLING);
        this.entityData.define(DATA_DROP_SEED_AT_TICK, 0);
        this.getNavigation().setCanFloat(true);
        this.setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.DANGER_POWDER_SNOW, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.DAMAGE_CAUTIOUS, -1.0F);
    }

    @Override
    protected float getStandingEyeHeight(Pose param0, EntityDimensions param1) {
        return this.getDimensions(param0).height * 0.6F;
    }

    @Override
    public void onPathfindingStart() {
        super.onPathfindingStart();
        if (this.isOnFire() || this.isInWater()) {
            this.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
        }

    }

    @Override
    public void onPathfindingDone() {
        this.setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
    }

    @Override
    public EntityDimensions getDimensions(Pose param0) {
        return this.entityData.hasItem(DATA_STATE) && this.getState() == Sniffer.State.DIGGING
            ? DIGGING_DIMENSIONS.scale(this.getScale())
            : super.getDimensions(param0);
    }

    public boolean isSearching() {
        return this.getState() == Sniffer.State.SEARCHING;
    }

    public boolean isTempted() {
        return this.brain.getMemory(MemoryModuleType.IS_TEMPTED).orElse(false);
    }

    public boolean canSniff() {
        return !this.isTempted() && !this.isPanicking() && !this.isInWater() && !this.isInLove() && this.onGround() && !this.isPassenger() && !this.isLeashed();
    }

    public boolean canPlayDiggingSound() {
        return this.getState() == Sniffer.State.DIGGING || this.getState() == Sniffer.State.SEARCHING;
    }

    private BlockPos getHeadBlock() {
        Vec3 var0 = this.getHeadPosition();
        return BlockPos.containing(var0.x(), this.getY() + 0.2F, var0.z());
    }

    private Vec3 getHeadPosition() {
        return this.position().add(this.getForward().scale(2.25));
    }

    private Sniffer.State getState() {
        return this.entityData.get(DATA_STATE);
    }

    private Sniffer setState(Sniffer.State param0) {
        this.entityData.set(DATA_STATE, param0);
        return this;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> param0) {
        if (DATA_STATE.equals(param0)) {
            Sniffer.State var0 = this.getState();
            this.resetAnimations();
            switch(var0) {
                case SCENTING:
                    this.scentingAnimationState.startIfStopped(this.tickCount);
                    break;
                case SNIFFING:
                    this.sniffingAnimationState.startIfStopped(this.tickCount);
                    break;
                case DIGGING:
                    this.diggingAnimationState.startIfStopped(this.tickCount);
                    break;
                case RISING:
                    this.risingAnimationState.startIfStopped(this.tickCount);
                    break;
                case FEELING_HAPPY:
                    this.feelingHappyAnimationState.startIfStopped(this.tickCount);
            }

            this.refreshDimensions();
        }

        super.onSyncedDataUpdated(param0);
    }

    private void resetAnimations() {
        this.diggingAnimationState.stop();
        this.sniffingAnimationState.stop();
        this.risingAnimationState.stop();
        this.feelingHappyAnimationState.stop();
        this.scentingAnimationState.stop();
    }

    public Sniffer transitionTo(Sniffer.State param0) {
        switch(param0) {
            case SCENTING:
                this.setState(Sniffer.State.SCENTING).onScentingStart();
                break;
            case SNIFFING:
                this.playSound(SoundEvents.SNIFFER_SNIFFING, 1.0F, 1.0F);
                this.setState(Sniffer.State.SNIFFING);
                break;
            case DIGGING:
                this.setState(Sniffer.State.DIGGING).onDiggingStart();
                break;
            case RISING:
                this.playSound(SoundEvents.SNIFFER_DIGGING_STOP, 1.0F, 1.0F);
                this.setState(Sniffer.State.RISING);
                break;
            case FEELING_HAPPY:
                this.playSound(SoundEvents.SNIFFER_HAPPY, 1.0F, 1.0F);
                this.setState(Sniffer.State.FEELING_HAPPY);
                break;
            case IDLING:
                this.setState(Sniffer.State.IDLING);
                break;
            case SEARCHING:
                this.setState(Sniffer.State.SEARCHING);
        }

        return this;
    }

    private Sniffer onScentingStart() {
        this.playSound(SoundEvents.SNIFFER_SCENTING, 1.0F, this.isBaby() ? 1.3F : 1.0F);
        return this;
    }

    private Sniffer onDiggingStart() {
        this.entityData.set(DATA_DROP_SEED_AT_TICK, this.tickCount + 120);
        this.level().broadcastEntityEvent(this, (byte)63);
        return this;
    }

    public Sniffer onDiggingComplete(boolean param0) {
        if (param0) {
            this.storeExploredPosition(this.getOnPos());
        }

        return this;
    }

    Optional<BlockPos> calculateDigPosition() {
        return IntStream.range(0, 5)
            .mapToObj(param0 -> LandRandomPos.getPos(this, 10 + 2 * param0, 3))
            .filter(Objects::nonNull)
            .map(BlockPos::containing)
            .filter(param0 -> this.level().getWorldBorder().isWithinBounds(param0))
            .map(BlockPos::below)
            .filter(this::canDig)
            .findFirst();
    }

    boolean canDig() {
        return !this.isPanicking()
            && !this.isTempted()
            && !this.isBaby()
            && !this.isInWater()
            && this.onGround()
            && !this.isPassenger()
            && this.canDig(this.getHeadBlock().below());
    }

    private boolean canDig(BlockPos param0) {
        return this.level().getBlockState(param0).is(BlockTags.SNIFFER_DIGGABLE_BLOCK)
            && this.getExploredPositions().noneMatch(param1 -> GlobalPos.of(this.level().dimension(), param0).equals(param1))
            && Optional.ofNullable(this.getNavigation().createPath(param0, 1)).map(Path::canReach).orElse(false);
    }

    private void dropSeed() {
        if (!this.level().isClientSide() && this.entityData.get(DATA_DROP_SEED_AT_TICK) == this.tickCount) {
            ServerLevel var0 = (ServerLevel)this.level();
            LootTable var1 = var0.getServer().getLootData().getLootTable(BuiltInLootTables.SNIFFER_DIGGING);
            LootParams var2 = new LootParams.Builder(var0)
                .withParameter(LootContextParams.ORIGIN, this.getHeadPosition())
                .withParameter(LootContextParams.THIS_ENTITY, this)
                .create(LootContextParamSets.GIFT);
            List<ItemStack> var3 = var1.getRandomItems(var2);
            BlockPos var4 = this.getHeadBlock();

            for(ItemStack var5 : var3) {
                ItemEntity var6 = new ItemEntity(var0, (double)var4.getX(), (double)var4.getY(), (double)var4.getZ(), var5);
                var6.setDefaultPickUpDelay();
                var0.addFreshEntity(var6);
            }

            this.playSound(SoundEvents.SNIFFER_DROP_SEED, 1.0F, 1.0F);
        }
    }

    private Sniffer emitDiggingParticles(AnimationState param0) {
        boolean var0 = param0.getAccumulatedTime() > 1700L && param0.getAccumulatedTime() < 6000L;
        if (var0) {
            BlockPos var1 = this.getHeadBlock();
            BlockState var2 = this.level().getBlockState(var1.below());
            if (var2.getRenderShape() != RenderShape.INVISIBLE) {
                for(int var3 = 0; var3 < 30; ++var3) {
                    Vec3 var4 = Vec3.atCenterOf(var1).add(0.0, -0.65F, 0.0);
                    this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, var2), var4.x, var4.y, var4.z, 0.0, 0.0, 0.0);
                }

                if (this.tickCount % 10 == 0) {
                    this.level()
                        .playLocalSound(this.getX(), this.getY(), this.getZ(), var2.getSoundType().getHitSound(), this.getSoundSource(), 0.5F, 0.5F, false);
                }
            }
        }

        if (this.tickCount % 10 == 0) {
            this.level().gameEvent(GameEvent.ENTITY_ACTION, this.getHeadBlock(), GameEvent.Context.of(this));
        }

        return this;
    }

    private Sniffer storeExploredPosition(BlockPos param0) {
        List<GlobalPos> var0 = this.getExploredPositions().limit(20L).collect(Collectors.toList());
        var0.add(0, GlobalPos.of(this.level().dimension(), param0));
        this.getBrain().setMemory(MemoryModuleType.SNIFFER_EXPLORED_POSITIONS, var0);
        return this;
    }

    private Stream<GlobalPos> getExploredPositions() {
        return this.getBrain().getMemory(MemoryModuleType.SNIFFER_EXPLORED_POSITIONS).stream().flatMap(Collection::stream);
    }

    @Override
    protected void jumpFromGround() {
        super.jumpFromGround();
        double var0 = this.moveControl.getSpeedModifier();
        if (var0 > 0.0) {
            double var1 = this.getDeltaMovement().horizontalDistanceSqr();
            if (var1 < 0.01) {
                this.moveRelative(0.1F, new Vec3(0.0, 0.0, 1.0));
            }
        }

    }

    @Override
    public void spawnChildFromBreeding(ServerLevel param0, Animal param1) {
        ItemStack var0 = new ItemStack(Items.SNIFFER_EGG);
        ItemEntity var1 = new ItemEntity(param0, this.position().x(), this.position().y(), this.position().z(), var0);
        var1.setDefaultPickUpDelay();
        this.finalizeSpawnChildFromBreeding(param0, param1, null);
        this.playSound(SoundEvents.SNIFFER_EGG_PLOP, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 0.5F);
        param0.addFreshEntity(var1);
    }

    @Override
    public void die(DamageSource param0) {
        this.transitionTo(Sniffer.State.IDLING);
        super.die(param0);
    }

    @Override
    public void tick() {
        switch(this.getState()) {
            case DIGGING:
                this.emitDiggingParticles(this.diggingAnimationState).dropSeed();
                break;
            case SEARCHING:
                this.playSearchingSound();
        }

        super.tick();
    }

    @Override
    public InteractionResult mobInteract(Player param0, InteractionHand param1) {
        ItemStack var0 = param0.getItemInHand(param1);
        boolean var1 = this.isFood(var0);
        InteractionResult var2 = super.mobInteract(param0, param1);
        if (var2.consumesAction() && var1) {
            this.level().playSound(null, this, this.getEatingSound(var0), SoundSource.NEUTRAL, 1.0F, Mth.randomBetween(this.level().random, 0.8F, 1.2F));
        }

        return var2;
    }

    @Override
    protected Vector3f getPassengerAttachmentPoint(Entity param0, EntityDimensions param1, float param2) {
        return new Vector3f(0.0F, param1.height + 0.34375F * param2, 0.0F);
    }

    @Override
    public float getNameTagOffsetY() {
        return super.getNameTagOffsetY() + 0.3F;
    }

    private void playSearchingSound() {
        if (this.level().isClientSide() && this.tickCount % 20 == 0) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.SNIFFER_SEARCHING, this.getSoundSource(), 1.0F, 1.0F, false);
        }

    }

    @Override
    protected void playStepSound(BlockPos param0, BlockState param1) {
        this.playSound(SoundEvents.SNIFFER_STEP, 0.15F, 1.0F);
    }

    @Override
    public SoundEvent getEatingSound(ItemStack param0) {
        return SoundEvents.SNIFFER_EAT;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return Set.of(Sniffer.State.DIGGING, Sniffer.State.SEARCHING).contains(this.getState()) ? null : SoundEvents.SNIFFER_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return SoundEvents.SNIFFER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SNIFFER_DEATH;
    }

    @Override
    public int getMaxHeadYRot() {
        return 50;
    }

    @Override
    public void setBaby(boolean param0) {
        this.setAge(param0 ? -48000 : 0);
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel param0, AgeableMob param1) {
        return EntityType.SNIFFER.create(param0);
    }

    @Override
    public boolean canMate(Animal param0) {
        if (!(param0 instanceof Sniffer)) {
            return false;
        } else {
            Sniffer var0 = (Sniffer)param0;
            Set<Sniffer.State> var1 = Set.of(Sniffer.State.IDLING, Sniffer.State.SCENTING, Sniffer.State.FEELING_HAPPY);
            return var1.contains(this.getState()) && var1.contains(var0.getState()) && super.canMate(param0);
        }
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return super.getBoundingBoxForCulling().inflate(0.6F);
    }

    @Override
    public boolean isFood(ItemStack param0) {
        return param0.is(ItemTags.SNIFFER_FOOD);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> param0) {
        return SnifferAi.makeBrain(this.brainProvider().makeBrain(param0));
    }

    @Override
    public Brain<Sniffer> getBrain() {
        return super.getBrain();
    }

    @Override
    protected Brain.Provider<Sniffer> brainProvider() {
        return Brain.provider(SnifferAi.MEMORY_TYPES, SnifferAi.SENSOR_TYPES);
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("snifferBrain");
        this.getBrain().tick((ServerLevel)this.level(), this);
        this.level().getProfiler().popPush("snifferActivityUpdate");
        SnifferAi.updateActivity(this);
        this.level().getProfiler().pop();
        super.customServerAiStep();
    }

    @Override
    protected void sendDebugPackets() {
        super.sendDebugPackets();
        DebugPackets.sendEntityBrain(this);
    }

    public static enum State {
        IDLING,
        FEELING_HAPPY,
        SCENTING,
        SNIFFING,
        SEARCHING,
        DIGGING,
        RISING;
    }
}
