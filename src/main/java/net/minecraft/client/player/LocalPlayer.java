package net.minecraft.client.player;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.client.gui.screens.inventory.CommandBlockEditScreen;
import net.minecraft.client.gui.screens.inventory.JigsawBlockEditScreen;
import net.minecraft.client.gui.screens.inventory.MinecartCommandBlockEditScreen;
import net.minecraft.client.gui.screens.inventory.SignEditScreen;
import net.minecraft.client.gui.screens.inventory.StructureBlockEditScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.resources.sounds.AmbientSoundHandler;
import net.minecraft.client.resources.sounds.BiomeAmbientSoundsHandler;
import net.minecraft.client.resources.sounds.BubbleColumnAmbientSoundHandler;
import net.minecraft.client.resources.sounds.ElytraOnPlayerSoundInstance;
import net.minecraft.client.resources.sounds.RidingMinecartSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.UnderwaterAmbientSoundHandler;
import net.minecraft.client.resources.sounds.UnderwaterAmbientSoundInstances;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundRecipeBookSeenRecipePacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.StatsCounter;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LocalPlayer extends AbstractClientPlayer {
    private static final int POSITION_REMINDER_INTERVAL = 20;
    private static final int WATER_VISION_MAX_TIME = 600;
    private static final int WATER_VISION_QUICK_TIME = 100;
    private static final float WATER_VISION_QUICK_PERCENT = 0.6F;
    private static final double SUFFOCATING_COLLISION_CHECK_SCALE = 0.35;
    public final ClientPacketListener connection;
    private final StatsCounter stats;
    private final ClientRecipeBook recipeBook;
    private final List<AmbientSoundHandler> ambientSoundHandlers = Lists.newArrayList();
    private int permissionLevel = 0;
    private double xLast;
    private double yLast1;
    private double zLast;
    private float yRotLast;
    private float xRotLast;
    private boolean lastOnGround;
    private boolean crouching;
    private boolean wasShiftKeyDown;
    private boolean wasSprinting;
    private int positionReminder;
    private boolean flashOnSetHealth;
    private String serverBrand;
    public Input input;
    protected final Minecraft minecraft;
    protected int sprintTriggerTime;
    public int sprintTime;
    public float yBob;
    public float xBob;
    public float yBobO;
    public float xBobO;
    private int jumpRidingTicks;
    private float jumpRidingScale;
    public float portalTime;
    public float oPortalTime;
    private boolean startedUsingItem;
    private InteractionHand usingItemHand;
    private boolean handsBusy;
    private boolean autoJumpEnabled = true;
    private int autoJumpTime;
    private boolean wasFallFlying;
    private int waterVisionTime;
    private boolean showDeathScreen = true;

    public LocalPlayer(
        Minecraft param0, ClientLevel param1, ClientPacketListener param2, StatsCounter param3, ClientRecipeBook param4, boolean param5, boolean param6
    ) {
        super(param1, param2.getLocalGameProfile());
        this.minecraft = param0;
        this.connection = param2;
        this.stats = param3;
        this.recipeBook = param4;
        this.wasShiftKeyDown = param5;
        this.wasSprinting = param6;
        this.ambientSoundHandlers.add(new UnderwaterAmbientSoundHandler(this, param0.getSoundManager()));
        this.ambientSoundHandlers.add(new BubbleColumnAmbientSoundHandler(this));
        this.ambientSoundHandlers.add(new BiomeAmbientSoundsHandler(this, param0.getSoundManager(), param1.getBiomeManager()));
    }

    @Override
    public boolean hurt(DamageSource param0, float param1) {
        return false;
    }

    @Override
    public void heal(float param0) {
    }

    @Override
    public boolean startRiding(Entity param0, boolean param1) {
        if (!super.startRiding(param0, param1)) {
            return false;
        } else {
            if (param0 instanceof AbstractMinecart) {
                this.minecraft.getSoundManager().play(new RidingMinecartSoundInstance(this, (AbstractMinecart)param0, true));
                this.minecraft.getSoundManager().play(new RidingMinecartSoundInstance(this, (AbstractMinecart)param0, false));
            }

            if (param0 instanceof Boat) {
                this.yRotO = param0.getYRot();
                this.setYRot(param0.getYRot());
                this.setYHeadRot(param0.getYRot());
            }

            return true;
        }
    }

    @Override
    public void removeVehicle() {
        super.removeVehicle();
        this.handsBusy = false;
    }

    @Override
    public float getViewXRot(float param0) {
        return this.getXRot();
    }

    @Override
    public float getViewYRot(float param0) {
        return this.isPassenger() ? super.getViewYRot(param0) : this.getYRot();
    }

    @Override
    public void tick() {
        if (this.level.hasChunkAt(this.getBlockX(), this.getBlockZ())) {
            super.tick();
            if (this.isPassenger()) {
                this.connection.send(new ServerboundMovePlayerPacket.Rot(this.getYRot(), this.getXRot(), this.onGround));
                this.connection.send(new ServerboundPlayerInputPacket(this.xxa, this.zza, this.input.jumping, this.input.shiftKeyDown));
                Entity var0 = this.getRootVehicle();
                if (var0 != this && var0.isControlledByLocalInstance()) {
                    this.connection.send(new ServerboundMoveVehiclePacket(var0));
                }
            } else {
                this.sendPosition();
            }

            for(AmbientSoundHandler var1 : this.ambientSoundHandlers) {
                var1.tick();
            }

        }
    }

    public float getCurrentMood() {
        for(AmbientSoundHandler var0 : this.ambientSoundHandlers) {
            if (var0 instanceof BiomeAmbientSoundsHandler) {
                return ((BiomeAmbientSoundsHandler)var0).getMoodiness();
            }
        }

        return 0.0F;
    }

    private void sendPosition() {
        boolean var0 = this.isSprinting();
        if (var0 != this.wasSprinting) {
            ServerboundPlayerCommandPacket.Action var1 = var0
                ? ServerboundPlayerCommandPacket.Action.START_SPRINTING
                : ServerboundPlayerCommandPacket.Action.STOP_SPRINTING;
            this.connection.send(new ServerboundPlayerCommandPacket(this, var1));
            this.wasSprinting = var0;
        }

        boolean var2 = this.isShiftKeyDown();
        if (var2 != this.wasShiftKeyDown) {
            ServerboundPlayerCommandPacket.Action var3 = var2
                ? ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY
                : ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY;
            this.connection.send(new ServerboundPlayerCommandPacket(this, var3));
            this.wasShiftKeyDown = var2;
        }

        if (this.isControlledCamera()) {
            double var4 = this.getX() - this.xLast;
            double var5 = this.getY() - this.yLast1;
            double var6 = this.getZ() - this.zLast;
            double var7 = (double)(this.getYRot() - this.yRotLast);
            double var8 = (double)(this.getXRot() - this.xRotLast);
            ++this.positionReminder;
            boolean var9 = var4 * var4 + var5 * var5 + var6 * var6 > 9.0E-4 || this.positionReminder >= 20;
            boolean var10 = var7 != 0.0 || var8 != 0.0;
            if (this.isPassenger()) {
                Vec3 var11 = this.getDeltaMovement();
                this.connection.send(new ServerboundMovePlayerPacket.PosRot(var11.x, -999.0, var11.z, this.getYRot(), this.getXRot(), this.onGround));
                var9 = false;
            } else if (var9 && var10) {
                this.connection
                    .send(new ServerboundMovePlayerPacket.PosRot(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot(), this.onGround));
            } else if (var9) {
                this.connection.send(new ServerboundMovePlayerPacket.Pos(this.getX(), this.getY(), this.getZ(), this.onGround));
            } else if (var10) {
                this.connection.send(new ServerboundMovePlayerPacket.Rot(this.getYRot(), this.getXRot(), this.onGround));
            } else if (this.lastOnGround != this.onGround) {
                this.connection.send(new ServerboundMovePlayerPacket.StatusOnly(this.onGround));
            }

            if (var9) {
                this.xLast = this.getX();
                this.yLast1 = this.getY();
                this.zLast = this.getZ();
                this.positionReminder = 0;
            }

            if (var10) {
                this.yRotLast = this.getYRot();
                this.xRotLast = this.getXRot();
            }

            this.lastOnGround = this.onGround;
            this.autoJumpEnabled = this.minecraft.options.autoJump;
        }

    }

    public boolean drop(boolean param0) {
        ServerboundPlayerActionPacket.Action var0 = param0
            ? ServerboundPlayerActionPacket.Action.DROP_ALL_ITEMS
            : ServerboundPlayerActionPacket.Action.DROP_ITEM;
        ItemStack var1 = this.getInventory().removeFromSelected(param0);
        this.connection.send(new ServerboundPlayerActionPacket(var0, BlockPos.ZERO, Direction.DOWN));
        return !var1.isEmpty();
    }

    public void chat(String param0) {
        this.connection.send(new ServerboundChatPacket(param0));
    }

    @Override
    public void swing(InteractionHand param0) {
        super.swing(param0);
        this.connection.send(new ServerboundSwingPacket(param0));
    }

    @Override
    public void respawn() {
        this.connection.send(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.PERFORM_RESPAWN));
    }

    @Override
    protected void actuallyHurt(DamageSource param0, float param1) {
        if (!this.isInvulnerableTo(param0)) {
            this.setHealth(this.getHealth() - param1);
        }
    }

    @Override
    public void closeContainer() {
        this.connection.send(new ServerboundContainerClosePacket(this.containerMenu.containerId));
        this.clientSideCloseContainer();
    }

    public void clientSideCloseContainer() {
        super.closeContainer();
        this.minecraft.setScreen(null);
    }

    public void hurtTo(float param0) {
        if (this.flashOnSetHealth) {
            float var0 = this.getHealth() - param0;
            if (var0 <= 0.0F) {
                this.setHealth(param0);
                if (var0 < 0.0F) {
                    this.invulnerableTime = 10;
                }
            } else {
                this.lastHurt = var0;
                this.invulnerableTime = 20;
                this.setHealth(param0);
                this.hurtDuration = 10;
                this.hurtTime = this.hurtDuration;
            }
        } else {
            this.setHealth(param0);
            this.flashOnSetHealth = true;
        }

    }

    @Override
    public void onUpdateAbilities() {
        this.connection.send(new ServerboundPlayerAbilitiesPacket(this.getAbilities()));
    }

    @Override
    public boolean isLocalPlayer() {
        return true;
    }

    @Override
    public boolean isSuppressingSlidingDownLadder() {
        return !this.getAbilities().flying && super.isSuppressingSlidingDownLadder();
    }

    @Override
    public boolean canSpawnSprintParticle() {
        return !this.getAbilities().flying && super.canSpawnSprintParticle();
    }

    @Override
    public boolean canSpawnSoulSpeedParticle() {
        return !this.getAbilities().flying && super.canSpawnSoulSpeedParticle();
    }

    protected void sendRidingJump() {
        this.connection
            .send(
                new ServerboundPlayerCommandPacket(this, ServerboundPlayerCommandPacket.Action.START_RIDING_JUMP, Mth.floor(this.getJumpRidingScale() * 100.0F))
            );
    }

    public void sendOpenInventory() {
        this.connection.send(new ServerboundPlayerCommandPacket(this, ServerboundPlayerCommandPacket.Action.OPEN_INVENTORY));
    }

    public void setServerBrand(String param0) {
        this.serverBrand = param0;
    }

    public String getServerBrand() {
        return this.serverBrand;
    }

    public StatsCounter getStats() {
        return this.stats;
    }

    public ClientRecipeBook getRecipeBook() {
        return this.recipeBook;
    }

    public void removeRecipeHighlight(Recipe<?> param0) {
        if (this.recipeBook.willHighlight(param0)) {
            this.recipeBook.removeHighlight(param0);
            this.connection.send(new ServerboundRecipeBookSeenRecipePacket(param0));
        }

    }

    @Override
    protected int getPermissionLevel() {
        return this.permissionLevel;
    }

    public void setPermissionLevel(int param0) {
        this.permissionLevel = param0;
    }

    @Override
    public void displayClientMessage(Component param0, boolean param1) {
        if (param1) {
            this.minecraft.gui.setOverlayMessage(param0, false);
        } else {
            this.minecraft.gui.getChat().addMessage(param0);
        }

    }

    private void moveTowardsClosestSpace(double param0, double param1) {
        BlockPos var0 = new BlockPos(param0, this.getY(), param1);
        if (this.suffocatesAt(var0)) {
            double var1 = param0 - (double)var0.getX();
            double var2 = param1 - (double)var0.getZ();
            Direction var3 = null;
            double var4 = Double.MAX_VALUE;
            Direction[] var5 = new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH};

            for(Direction var6 : var5) {
                double var7 = var6.getAxis().choose(var1, 0.0, var2);
                double var8 = var6.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1.0 - var7 : var7;
                if (var8 < var4 && !this.suffocatesAt(var0.relative(var6))) {
                    var4 = var8;
                    var3 = var6;
                }
            }

            if (var3 != null) {
                Vec3 var9 = this.getDeltaMovement();
                if (var3.getAxis() == Direction.Axis.X) {
                    this.setDeltaMovement(0.1 * (double)var3.getStepX(), var9.y, var9.z);
                } else {
                    this.setDeltaMovement(var9.x, var9.y, 0.1 * (double)var3.getStepZ());
                }
            }

        }
    }

    private boolean suffocatesAt(BlockPos param0) {
        AABB var0 = this.getBoundingBox();
        AABB var1 = new AABB((double)param0.getX(), var0.minY, (double)param0.getZ(), (double)param0.getX() + 1.0, var0.maxY, (double)param0.getZ() + 1.0)
            .deflate(1.0E-7);
        return this.level.hasBlockCollision(this, var1, (param0x, param1) -> param0x.isSuffocating(this.level, param1));
    }

    @Override
    public void setSprinting(boolean param0) {
        super.setSprinting(param0);
        this.sprintTime = 0;
    }

    public void setExperienceValues(float param0, int param1, int param2) {
        this.experienceProgress = param0;
        this.totalExperience = param1;
        this.experienceLevel = param2;
    }

    @Override
    public void sendMessage(Component param0, UUID param1) {
        this.minecraft.gui.getChat().addMessage(param0);
    }

    @Override
    public void handleEntityEvent(byte param0) {
        if (param0 >= 24 && param0 <= 28) {
            this.setPermissionLevel(param0 - 24);
        } else {
            super.handleEntityEvent(param0);
        }

    }

    public void setShowDeathScreen(boolean param0) {
        this.showDeathScreen = param0;
    }

    public boolean shouldShowDeathScreen() {
        return this.showDeathScreen;
    }

    @Override
    public void playSound(SoundEvent param0, float param1, float param2) {
        this.level.playLocalSound(this.getX(), this.getY(), this.getZ(), param0, this.getSoundSource(), param1, param2, false);
    }

    @Override
    public void playNotifySound(SoundEvent param0, SoundSource param1, float param2, float param3) {
        this.level.playLocalSound(this.getX(), this.getY(), this.getZ(), param0, param1, param2, param3, false);
    }

    @Override
    public boolean isEffectiveAi() {
        return true;
    }

    @Override
    public void startUsingItem(InteractionHand param0) {
        ItemStack var0 = this.getItemInHand(param0);
        if (!var0.isEmpty() && !this.isUsingItem()) {
            super.startUsingItem(param0);
            this.startedUsingItem = true;
            this.usingItemHand = param0;
        }
    }

    @Override
    public boolean isUsingItem() {
        return this.startedUsingItem;
    }

    @Override
    public void stopUsingItem() {
        super.stopUsingItem();
        this.startedUsingItem = false;
    }

    @Override
    public InteractionHand getUsedItemHand() {
        return this.usingItemHand;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> param0) {
        super.onSyncedDataUpdated(param0);
        if (DATA_LIVING_ENTITY_FLAGS.equals(param0)) {
            boolean var0 = (this.entityData.get(DATA_LIVING_ENTITY_FLAGS) & 1) > 0;
            InteractionHand var1 = (this.entityData.get(DATA_LIVING_ENTITY_FLAGS) & 2) > 0 ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            if (var0 && !this.startedUsingItem) {
                this.startUsingItem(var1);
            } else if (!var0 && this.startedUsingItem) {
                this.stopUsingItem();
            }
        }

        if (DATA_SHARED_FLAGS_ID.equals(param0) && this.isFallFlying() && !this.wasFallFlying) {
            this.minecraft.getSoundManager().play(new ElytraOnPlayerSoundInstance(this));
        }

    }

    public boolean isRidingJumpable() {
        Entity var0 = this.getVehicle();
        return this.isPassenger() && var0 instanceof PlayerRideableJumping && ((PlayerRideableJumping)var0).canJump();
    }

    public float getJumpRidingScale() {
        return this.jumpRidingScale;
    }

    @Override
    public void openTextEdit(SignBlockEntity param0) {
        this.minecraft.setScreen(new SignEditScreen(param0, this.minecraft.isTextFilteringEnabled()));
    }

    @Override
    public void openMinecartCommandBlock(BaseCommandBlock param0) {
        this.minecraft.setScreen(new MinecartCommandBlockEditScreen(param0));
    }

    @Override
    public void openCommandBlock(CommandBlockEntity param0) {
        this.minecraft.setScreen(new CommandBlockEditScreen(param0));
    }

    @Override
    public void openStructureBlock(StructureBlockEntity param0) {
        this.minecraft.setScreen(new StructureBlockEditScreen(param0));
    }

    @Override
    public void openJigsawBlock(JigsawBlockEntity param0) {
        this.minecraft.setScreen(new JigsawBlockEditScreen(param0));
    }

    @Override
    public void openItemGui(ItemStack param0, InteractionHand param1) {
        if (param0.is(Items.WRITABLE_BOOK)) {
            this.minecraft.setScreen(new BookEditScreen(this, param0, param1));
        }

    }

    @Override
    public void crit(Entity param0) {
        this.minecraft.particleEngine.createTrackingEmitter(param0, ParticleTypes.CRIT);
    }

    @Override
    public void magicCrit(Entity param0) {
        this.minecraft.particleEngine.createTrackingEmitter(param0, ParticleTypes.ENCHANTED_HIT);
    }

    @Override
    public boolean isShiftKeyDown() {
        return this.input != null && this.input.shiftKeyDown;
    }

    @Override
    public boolean isCrouching() {
        return this.crouching;
    }

    public boolean isMovingSlowly() {
        return this.isCrouching() || this.isVisuallyCrawling();
    }

    @Override
    public void serverAiStep() {
        super.serverAiStep();
        if (this.isControlledCamera()) {
            this.xxa = this.input.leftImpulse;
            this.zza = this.input.forwardImpulse;
            this.jumping = this.input.jumping;
            this.yBobO = this.yBob;
            this.xBobO = this.xBob;
            this.xBob = (float)((double)this.xBob + (double)(this.getXRot() - this.xBob) * 0.5);
            this.yBob = (float)((double)this.yBob + (double)(this.getYRot() - this.yBob) * 0.5);
        }

    }

    protected boolean isControlledCamera() {
        return this.minecraft.getCameraEntity() == this;
    }

    public void resetPos() {
        this.setPose(Pose.STANDING);
        if (this.level != null) {
            for(double var0 = this.getY(); var0 > (double)this.level.getMinBuildHeight() && var0 < (double)this.level.getMaxBuildHeight(); ++var0) {
                this.setPos(this.getX(), var0, this.getZ());
                if (this.level.noCollision(this)) {
                    break;
                }
            }

            this.setDeltaMovement(Vec3.ZERO);
            this.setXRot(0.0F);
        }

        this.setHealth(this.getMaxHealth());
        this.deathTime = 0;
    }

    @Override
    public void aiStep() {
        ++this.sprintTime;
        if (this.sprintTriggerTime > 0) {
            --this.sprintTriggerTime;
        }

        this.handleNetherPortalClient();
        boolean var0 = this.input.jumping;
        boolean var1 = this.input.shiftKeyDown;
        boolean var2 = this.hasEnoughImpulseToStartSprinting();
        this.crouching = !this.getAbilities().flying
            && !this.isSwimming()
            && this.canEnterPose(Pose.CROUCHING)
            && (this.isShiftKeyDown() || !this.isSleeping() && !this.canEnterPose(Pose.STANDING));
        this.input.tick(this.isMovingSlowly());
        this.minecraft.getTutorial().onInput(this.input);
        if (this.isUsingItem() && !this.isPassenger()) {
            this.input.leftImpulse *= 0.2F;
            this.input.forwardImpulse *= 0.2F;
            this.sprintTriggerTime = 0;
        }

        boolean var3 = false;
        if (this.autoJumpTime > 0) {
            --this.autoJumpTime;
            var3 = true;
            this.input.jumping = true;
        }

        if (!this.noPhysics) {
            this.moveTowardsClosestSpace(this.getX() - (double)this.getBbWidth() * 0.35, this.getZ() + (double)this.getBbWidth() * 0.35);
            this.moveTowardsClosestSpace(this.getX() - (double)this.getBbWidth() * 0.35, this.getZ() - (double)this.getBbWidth() * 0.35);
            this.moveTowardsClosestSpace(this.getX() + (double)this.getBbWidth() * 0.35, this.getZ() - (double)this.getBbWidth() * 0.35);
            this.moveTowardsClosestSpace(this.getX() + (double)this.getBbWidth() * 0.35, this.getZ() + (double)this.getBbWidth() * 0.35);
        }

        if (var1) {
            this.sprintTriggerTime = 0;
        }

        boolean var4 = (float)this.getFoodData().getFoodLevel() > 6.0F || this.getAbilities().mayfly;
        if ((this.onGround || this.isUnderWater())
            && !var1
            && !var2
            && this.hasEnoughImpulseToStartSprinting()
            && !this.isSprinting()
            && var4
            && !this.isUsingItem()
            && !this.hasEffect(MobEffects.BLINDNESS)) {
            if (this.sprintTriggerTime <= 0 && !this.minecraft.options.keySprint.isDown()) {
                this.sprintTriggerTime = 7;
            } else {
                this.setSprinting(true);
            }
        }

        if (!this.isSprinting()
            && (!this.isInWater() || this.isUnderWater())
            && this.hasEnoughImpulseToStartSprinting()
            && var4
            && !this.isUsingItem()
            && !this.hasEffect(MobEffects.BLINDNESS)
            && this.minecraft.options.keySprint.isDown()) {
            this.setSprinting(true);
        }

        if (this.isSprinting()) {
            boolean var5 = !this.input.hasForwardImpulse() || !var4;
            boolean var6 = var5 || this.horizontalCollision && !this.minorHorizontalCollision || this.isInWater() && !this.isUnderWater();
            if (this.isSwimming()) {
                if (!this.onGround && !this.input.shiftKeyDown && var5 || !this.isInWater()) {
                    this.setSprinting(false);
                }
            } else if (var6) {
                this.setSprinting(false);
            }
        }

        boolean var7 = false;
        if (this.getAbilities().mayfly) {
            if (this.minecraft.gameMode.isAlwaysFlying()) {
                if (!this.getAbilities().flying) {
                    this.getAbilities().flying = true;
                    var7 = true;
                    this.onUpdateAbilities();
                }
            } else if (!var0 && this.input.jumping && !var3) {
                if (this.jumpTriggerTime == 0) {
                    this.jumpTriggerTime = 7;
                } else if (!this.isSwimming()) {
                    this.getAbilities().flying = !this.getAbilities().flying;
                    var7 = true;
                    this.onUpdateAbilities();
                    this.jumpTriggerTime = 0;
                }
            }
        }

        if (this.input.jumping && !var7 && !var0 && !this.getAbilities().flying && !this.isPassenger() && !this.onClimbable()) {
            ItemStack var8 = this.getItemBySlot(EquipmentSlot.CHEST);
            if (var8.is(Items.ELYTRA) && ElytraItem.isFlyEnabled(var8) && this.tryToStartFallFlying()) {
                this.connection.send(new ServerboundPlayerCommandPacket(this, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
            }
        }

        this.wasFallFlying = this.isFallFlying();
        if (this.isInWater() && this.input.shiftKeyDown && this.isAffectedByFluids()) {
            this.goDownInWater();
        }

        if (this.isEyeInFluid(FluidTags.WATER)) {
            int var9 = this.isSpectator() ? 10 : 1;
            this.waterVisionTime = Mth.clamp(this.waterVisionTime + var9, 0, 600);
        } else if (this.waterVisionTime > 0) {
            this.isEyeInFluid(FluidTags.WATER);
            this.waterVisionTime = Mth.clamp(this.waterVisionTime - 10, 0, 600);
        }

        if (this.getAbilities().flying && this.isControlledCamera()) {
            int var10 = 0;
            if (this.input.shiftKeyDown) {
                --var10;
            }

            if (this.input.jumping) {
                ++var10;
            }

            if (var10 != 0) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0, (double)((float)var10 * this.getAbilities().getFlyingSpeed() * 3.0F), 0.0));
            }
        }

        if (this.isRidingJumpable()) {
            PlayerRideableJumping var11 = (PlayerRideableJumping)this.getVehicle();
            if (this.jumpRidingTicks < 0) {
                ++this.jumpRidingTicks;
                if (this.jumpRidingTicks == 0) {
                    this.jumpRidingScale = 0.0F;
                }
            }

            if (var0 && !this.input.jumping) {
                this.jumpRidingTicks = -10;
                var11.onPlayerJump(Mth.floor(this.getJumpRidingScale() * 100.0F));
                this.sendRidingJump();
            } else if (!var0 && this.input.jumping) {
                this.jumpRidingTicks = 0;
                this.jumpRidingScale = 0.0F;
            } else if (var0) {
                ++this.jumpRidingTicks;
                if (this.jumpRidingTicks < 10) {
                    this.jumpRidingScale = (float)this.jumpRidingTicks * 0.1F;
                } else {
                    this.jumpRidingScale = 0.8F + 2.0F / (float)(this.jumpRidingTicks - 9) * 0.1F;
                }
            }
        } else {
            this.jumpRidingScale = 0.0F;
        }

        super.aiStep();
        if (this.onGround && this.getAbilities().flying && !this.minecraft.gameMode.isAlwaysFlying()) {
            this.getAbilities().flying = false;
            this.onUpdateAbilities();
        }

    }

    @Override
    protected void tickDeath() {
        ++this.deathTime;
        if (this.deathTime == 20) {
            this.remove(Entity.RemovalReason.KILLED);
        }

    }

    private void handleNetherPortalClient() {
        this.oPortalTime = this.portalTime;
        if (this.isInsidePortal) {
            if (this.minecraft.screen != null && !this.minecraft.screen.isPauseScreen() && !(this.minecraft.screen instanceof DeathScreen)) {
                if (this.minecraft.screen instanceof AbstractContainerScreen) {
                    this.closeContainer();
                }

                this.minecraft.setScreen(null);
            }

            if (this.portalTime == 0.0F) {
                this.minecraft
                    .getSoundManager()
                    .play(SimpleSoundInstance.forLocalAmbience(SoundEvents.PORTAL_TRIGGER, this.random.nextFloat() * 0.4F + 0.8F, 0.25F));
            }

            this.portalTime += 0.0125F;
            if (this.portalTime >= 1.0F) {
                this.portalTime = 1.0F;
            }

            this.isInsidePortal = false;
        } else if (this.hasEffect(MobEffects.CONFUSION) && this.getEffect(MobEffects.CONFUSION).getDuration() > 60) {
            this.portalTime += 0.006666667F;
            if (this.portalTime > 1.0F) {
                this.portalTime = 1.0F;
            }
        } else {
            if (this.portalTime > 0.0F) {
                this.portalTime -= 0.05F;
            }

            if (this.portalTime < 0.0F) {
                this.portalTime = 0.0F;
            }
        }

        this.processPortalCooldown();
    }

    @Override
    public void rideTick() {
        super.rideTick();
        this.handsBusy = false;
        if (this.getVehicle() instanceof Boat var0) {
            var0.setInput(this.input.left, this.input.right, this.input.up, this.input.down);
            this.handsBusy |= this.input.left || this.input.right || this.input.up || this.input.down;
        }

    }

    public boolean isHandsBusy() {
        return this.handsBusy;
    }

    @Nullable
    @Override
    public MobEffectInstance removeEffectNoUpdate(@Nullable MobEffect param0) {
        if (param0 == MobEffects.CONFUSION) {
            this.oPortalTime = 0.0F;
            this.portalTime = 0.0F;
        }

        return super.removeEffectNoUpdate(param0);
    }

    @Override
    public void move(MoverType param0, Vec3 param1) {
        double var0 = this.getX();
        double var1 = this.getZ();
        super.move(param0, param1);
        this.updateAutoJump((float)(this.getX() - var0), (float)(this.getZ() - var1));
    }

    public boolean isAutoJumpEnabled() {
        return this.autoJumpEnabled;
    }

    protected void updateAutoJump(float param0, float param1) {
        if (this.canAutoJump()) {
            Vec3 var0 = this.position();
            Vec3 var1 = var0.add((double)param0, 0.0, (double)param1);
            Vec3 var2 = new Vec3((double)param0, 0.0, (double)param1);
            float var3 = this.getSpeed();
            float var4 = (float)var2.lengthSqr();
            if (var4 <= 0.001F) {
                Vec2 var5 = this.input.getMoveVector();
                float var6 = var3 * var5.x;
                float var7 = var3 * var5.y;
                float var8 = Mth.sin(this.getYRot() * (float) (Math.PI / 180.0));
                float var9 = Mth.cos(this.getYRot() * (float) (Math.PI / 180.0));
                var2 = new Vec3((double)(var6 * var9 - var7 * var8), var2.y, (double)(var7 * var9 + var6 * var8));
                var4 = (float)var2.lengthSqr();
                if (var4 <= 0.001F) {
                    return;
                }
            }

            float var10 = Mth.fastInvSqrt(var4);
            Vec3 var11 = var2.scale((double)var10);
            Vec3 var12 = this.getForward();
            float var13 = (float)(var12.x * var11.x + var12.z * var11.z);
            if (!(var13 < -0.15F)) {
                CollisionContext var14 = CollisionContext.of(this);
                BlockPos var15 = new BlockPos(this.getX(), this.getBoundingBox().maxY, this.getZ());
                BlockState var16 = this.level.getBlockState(var15);
                if (var16.getCollisionShape(this.level, var15, var14).isEmpty()) {
                    var15 = var15.above();
                    BlockState var17 = this.level.getBlockState(var15);
                    if (var17.getCollisionShape(this.level, var15, var14).isEmpty()) {
                        float var18 = 7.0F;
                        float var19 = 1.2F;
                        if (this.hasEffect(MobEffects.JUMP)) {
                            var19 += (float)(this.getEffect(MobEffects.JUMP).getAmplifier() + 1) * 0.75F;
                        }

                        float var20 = Math.max(var3 * 7.0F, 1.0F / var10);
                        Vec3 var22 = var1.add(var11.scale((double)var20));
                        float var23 = this.getBbWidth();
                        float var24 = this.getBbHeight();
                        AABB var25 = new AABB(var0, var22.add(0.0, (double)var24, 0.0)).inflate((double)var23, 0.0, (double)var23);
                        Vec3 var21 = var0.add(0.0, 0.51F, 0.0);
                        var22 = var22.add(0.0, 0.51F, 0.0);
                        Vec3 var26 = var11.cross(new Vec3(0.0, 1.0, 0.0));
                        Vec3 var27 = var26.scale((double)(var23 * 0.5F));
                        Vec3 var28 = var21.subtract(var27);
                        Vec3 var29 = var22.subtract(var27);
                        Vec3 var30 = var21.add(var27);
                        Vec3 var31 = var22.add(var27);
                        Iterator<AABB> var32 = this.level.getCollisions(this, var25, param0x -> true).flatMap(param0x -> param0x.toAabbs().stream()).iterator();
                        float var33 = Float.MIN_VALUE;

                        while(var32.hasNext()) {
                            AABB var34 = var32.next();
                            if (var34.intersects(var28, var29) || var34.intersects(var30, var31)) {
                                var33 = (float)var34.maxY;
                                Vec3 var35 = var34.getCenter();
                                BlockPos var36 = new BlockPos(var35);

                                for(int var37 = 1; (float)var37 < var19; ++var37) {
                                    BlockPos var38 = var36.above(var37);
                                    BlockState var39 = this.level.getBlockState(var38);
                                    VoxelShape var40;
                                    if (!(var40 = var39.getCollisionShape(this.level, var38, var14)).isEmpty()) {
                                        var33 = (float)var40.max(Direction.Axis.Y) + (float)var38.getY();
                                        if ((double)var33 - this.getY() > (double)var19) {
                                            return;
                                        }
                                    }

                                    if (var37 > 1) {
                                        var15 = var15.above();
                                        BlockState var41 = this.level.getBlockState(var15);
                                        if (!var41.getCollisionShape(this.level, var15, var14).isEmpty()) {
                                            return;
                                        }
                                    }
                                }
                                break;
                            }
                        }

                        if (var33 != Float.MIN_VALUE) {
                            float var42 = (float)((double)var33 - this.getY());
                            if (!(var42 <= 0.5F) && !(var42 > var19)) {
                                this.autoJumpTime = 1;
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean canAutoJump() {
        return this.isAutoJumpEnabled()
            && this.autoJumpTime <= 0
            && this.onGround
            && !this.isStayingOnGroundSurface()
            && !this.isPassenger()
            && this.isMoving()
            && (double)this.getBlockJumpFactor() >= 1.0;
    }

    private boolean isMoving() {
        Vec2 var0 = this.input.getMoveVector();
        return var0.x != 0.0F || var0.y != 0.0F;
    }

    private boolean hasEnoughImpulseToStartSprinting() {
        double var0 = 0.8;
        return this.isUnderWater() ? this.input.hasForwardImpulse() : (double)this.input.forwardImpulse >= 0.8;
    }

    public float getWaterVision() {
        if (!this.isEyeInFluid(FluidTags.WATER)) {
            return 0.0F;
        } else {
            float var0 = 600.0F;
            float var1 = 100.0F;
            if ((float)this.waterVisionTime >= 600.0F) {
                return 1.0F;
            } else {
                float var2 = Mth.clamp((float)this.waterVisionTime / 100.0F, 0.0F, 1.0F);
                float var3 = (float)this.waterVisionTime < 100.0F ? 0.0F : Mth.clamp(((float)this.waterVisionTime - 100.0F) / 500.0F, 0.0F, 1.0F);
                return var2 * 0.6F + var3 * 0.39999998F;
            }
        }
    }

    @Override
    public boolean isUnderWater() {
        return this.wasUnderwater;
    }

    @Override
    protected boolean updateIsUnderwater() {
        boolean var0 = this.wasUnderwater;
        boolean var1 = super.updateIsUnderwater();
        if (this.isSpectator()) {
            return this.wasUnderwater;
        } else {
            if (!var0 && var1) {
                this.level.playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.AMBIENT_UNDERWATER_ENTER, SoundSource.AMBIENT, 1.0F, 1.0F, false);
                this.minecraft.getSoundManager().play(new UnderwaterAmbientSoundInstances.UnderwaterAmbientSoundInstance(this));
            }

            if (var0 && !var1) {
                this.level.playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.AMBIENT_UNDERWATER_EXIT, SoundSource.AMBIENT, 1.0F, 1.0F, false);
            }

            return this.wasUnderwater;
        }
    }

    @Override
    public Vec3 getRopeHoldPosition(float param0) {
        if (this.minecraft.options.getCameraType().isFirstPerson()) {
            float var0 = Mth.lerp(param0 * 0.5F, this.getYRot(), this.yRotO) * (float) (Math.PI / 180.0);
            float var1 = Mth.lerp(param0 * 0.5F, this.getXRot(), this.xRotO) * (float) (Math.PI / 180.0);
            double var2 = this.getMainArm() == HumanoidArm.RIGHT ? -1.0 : 1.0;
            Vec3 var3 = new Vec3(0.39 * var2, -0.6, 0.3);
            return var3.xRot(-var1).yRot(-var0).add(this.getEyePosition(param0));
        } else {
            return super.getRopeHoldPosition(param0);
        }
    }

    @Override
    public void updateTutorialInventoryAction(ItemStack param0, ItemStack param1, ClickAction param2) {
        this.minecraft.getTutorial().onInventoryAction(param0, param1, param2);
    }
}
