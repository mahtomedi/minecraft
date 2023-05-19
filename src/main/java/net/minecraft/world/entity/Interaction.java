package net.minecraft.world.entity;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;

public class Interaction extends Entity implements Attackable, Targeting {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final EntityDataAccessor<Float> DATA_WIDTH_ID = SynchedEntityData.defineId(Interaction.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_HEIGHT_ID = SynchedEntityData.defineId(Interaction.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_RESPONSE_ID = SynchedEntityData.defineId(Interaction.class, EntityDataSerializers.BOOLEAN);
    private static final String TAG_WIDTH = "width";
    private static final String TAG_HEIGHT = "height";
    private static final String TAG_ATTACK = "attack";
    private static final String TAG_INTERACTION = "interaction";
    private static final String TAG_RESPONSE = "response";
    @Nullable
    private Interaction.PlayerAction attack;
    @Nullable
    private Interaction.PlayerAction interaction;

    public Interaction(EntityType<?> param0, Level param1) {
        super(param0, param1);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_WIDTH_ID, 1.0F);
        this.entityData.define(DATA_HEIGHT_ID, 1.0F);
        this.entityData.define(DATA_RESPONSE_ID, false);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag param0) {
        if (param0.contains("width", 99)) {
            this.setWidth(param0.getFloat("width"));
        }

        if (param0.contains("height", 99)) {
            this.setHeight(param0.getFloat("height"));
        }

        if (param0.contains("attack")) {
            Interaction.PlayerAction.CODEC
                .decode(NbtOps.INSTANCE, param0.get("attack"))
                .resultOrPartial(Util.prefix("Interaction entity", LOGGER::error))
                .ifPresent(param0x -> this.attack = param0x.getFirst());
        } else {
            this.attack = null;
        }

        if (param0.contains("interaction")) {
            Interaction.PlayerAction.CODEC
                .decode(NbtOps.INSTANCE, param0.get("interaction"))
                .resultOrPartial(Util.prefix("Interaction entity", LOGGER::error))
                .ifPresent(param0x -> this.interaction = param0x.getFirst());
        } else {
            this.interaction = null;
        }

        this.setResponse(param0.getBoolean("response"));
        this.setBoundingBox(this.makeBoundingBox());
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag param0) {
        param0.putFloat("width", this.getWidth());
        param0.putFloat("height", this.getHeight());
        if (this.attack != null) {
            Interaction.PlayerAction.CODEC.encodeStart(NbtOps.INSTANCE, this.attack).result().ifPresent(param1 -> param0.put("attack", param1));
        }

        if (this.interaction != null) {
            Interaction.PlayerAction.CODEC.encodeStart(NbtOps.INSTANCE, this.interaction).result().ifPresent(param1 -> param0.put("interaction", param1));
        }

        param0.putBoolean("response", this.getResponse());
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> param0) {
        super.onSyncedDataUpdated(param0);
        if (DATA_HEIGHT_ID.equals(param0) || DATA_WIDTH_ID.equals(param0)) {
            this.setBoundingBox(this.makeBoundingBox());
        }

    }

    @Override
    public boolean canBeHitByProjectile() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    @Override
    public boolean skipAttackInteraction(Entity param0) {
        if (param0 instanceof Player var0) {
            this.attack = new Interaction.PlayerAction(var0.getUUID(), this.level().getGameTime());
            if (var0 instanceof ServerPlayer var1) {
                CriteriaTriggers.PLAYER_HURT_ENTITY.trigger(var1, this, var0.damageSources().generic(), 1.0F, 1.0F, false);
            }

            return !this.getResponse();
        } else {
            return false;
        }
    }

    @Override
    public InteractionResult interact(Player param0, InteractionHand param1) {
        if (this.level().isClientSide) {
            return this.getResponse() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
        } else {
            this.interaction = new Interaction.PlayerAction(param0.getUUID(), this.level().getGameTime());
            return InteractionResult.CONSUME;
        }
    }

    @Override
    public void tick() {
    }

    @Nullable
    @Override
    public LivingEntity getLastAttacker() {
        return this.attack != null ? this.level().getPlayerByUUID(this.attack.player()) : null;
    }

    @Nullable
    @Override
    public LivingEntity getTarget() {
        return this.interaction != null ? this.level().getPlayerByUUID(this.interaction.player()) : null;
    }

    private void setWidth(float param0) {
        this.entityData.set(DATA_WIDTH_ID, param0);
    }

    private float getWidth() {
        return this.entityData.get(DATA_WIDTH_ID);
    }

    private void setHeight(float param0) {
        this.entityData.set(DATA_HEIGHT_ID, param0);
    }

    private float getHeight() {
        return this.entityData.get(DATA_HEIGHT_ID);
    }

    private void setResponse(boolean param0) {
        this.entityData.set(DATA_RESPONSE_ID, param0);
    }

    private boolean getResponse() {
        return this.entityData.get(DATA_RESPONSE_ID);
    }

    private EntityDimensions getDimensions() {
        return EntityDimensions.scalable(this.getWidth(), this.getHeight());
    }

    @Override
    public EntityDimensions getDimensions(Pose param0) {
        return this.getDimensions();
    }

    @Override
    protected AABB makeBoundingBox() {
        return this.getDimensions().makeBoundingBox(this.position());
    }

    static record PlayerAction(UUID player, long timestamp) {
        public static final Codec<Interaction.PlayerAction> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        UUIDUtil.CODEC.fieldOf("player").forGetter(Interaction.PlayerAction::player),
                        Codec.LONG.fieldOf("timestamp").forGetter(Interaction.PlayerAction::timestamp)
                    )
                    .apply(param0, Interaction.PlayerAction::new)
        );
    }
}
