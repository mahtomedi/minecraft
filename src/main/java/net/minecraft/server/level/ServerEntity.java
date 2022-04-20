package net.minecraft.server.level;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.protocol.game.VecDeltaCodec;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class ServerEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int TOLERANCE_LEVEL_ROTATION = 1;
    private final ServerLevel level;
    private final Entity entity;
    private final int updateInterval;
    private final boolean trackDelta;
    private final Consumer<Packet<?>> broadcast;
    private int yRotp;
    private int xRotp;
    private int yHeadRotp;
    private Vec3 ap = Vec3.ZERO;
    private int tickCount;
    private int teleportDelay;
    private List<Entity> lastPassengers = Collections.emptyList();
    private boolean wasRiding;
    private boolean wasOnGround;

    public ServerEntity(ServerLevel param0, Entity param1, int param2, boolean param3, Consumer<Packet<?>> param4) {
        this.level = param0;
        this.broadcast = param4;
        this.entity = param1;
        this.updateInterval = param2;
        this.trackDelta = param3;
        param1.getPositionCodec().setBase(param1.trackingPosition());
        this.yRotp = Mth.floor(param1.getYRot() * 256.0F / 360.0F);
        this.xRotp = Mth.floor(param1.getXRot() * 256.0F / 360.0F);
        this.yHeadRotp = Mth.floor(param1.getYHeadRot() * 256.0F / 360.0F);
        this.wasOnGround = param1.isOnGround();
    }

    public void sendChanges() {
        List<Entity> var0 = this.entity.getPassengers();
        if (!var0.equals(this.lastPassengers)) {
            this.lastPassengers = var0;
            this.broadcast.accept(new ClientboundSetPassengersPacket(this.entity));
        }

        Entity var24 = this.entity;
        if (var24 instanceof ItemFrame var1 && this.tickCount % 10 == 0) {
            ItemStack var2 = var1.getItem();
            if (var2.getItem() instanceof MapItem) {
                Integer var3x = MapItem.getMapId(var2);
                MapItemSavedData var4 = MapItem.getSavedData(var3x, this.level);
                if (var4 != null) {
                    for(ServerPlayer var5 : this.level.players()) {
                        var4.tickCarriedBy(var5, var2);
                        Packet<?> var6 = var4.getUpdatePacket(var3x, var5);
                        if (var6 != null) {
                            var5.connection.send(var6);
                        }
                    }
                }
            }

            this.sendDirtyEntityData();
        }

        if (this.tickCount % this.updateInterval == 0 || this.entity.hasImpulse || this.entity.getEntityData().isDirty()) {
            VecDeltaCodec var7 = this.entity.getPositionCodec();
            if (this.entity.isPassenger()) {
                int var8 = Mth.floor(this.entity.getYRot() * 256.0F / 360.0F);
                int var9 = Mth.floor(this.entity.getXRot() * 256.0F / 360.0F);
                boolean var10 = Math.abs(var8 - this.yRotp) >= 1 || Math.abs(var9 - this.xRotp) >= 1;
                if (var10) {
                    this.broadcast.accept(new ClientboundMoveEntityPacket.Rot(this.entity.getId(), (byte)var8, (byte)var9, this.entity.isOnGround()));
                    this.yRotp = var8;
                    this.xRotp = var9;
                }

                var7.setBase(this.entity.trackingPosition());
                this.sendDirtyEntityData();
                this.wasRiding = true;
            } else {
                ++this.teleportDelay;
                int var11 = Mth.floor(this.entity.getYRot() * 256.0F / 360.0F);
                int var12 = Mth.floor(this.entity.getXRot() * 256.0F / 360.0F);
                Vec3 var13 = this.entity.trackingPosition();
                boolean var14 = var7.delta(var13).lengthSqr() >= 7.6293945E-6F;
                Packet<?> var15 = null;
                boolean var16 = var14 || this.tickCount % 60 == 0;
                boolean var17 = Math.abs(var11 - this.yRotp) >= 1 || Math.abs(var12 - this.xRotp) >= 1;
                if (this.tickCount > 0 || this.entity instanceof AbstractArrow) {
                    long var18 = var7.encodeX(var13);
                    long var19 = var7.encodeY(var13);
                    long var20 = var7.encodeZ(var13);
                    boolean var21 = var18 < -32768L || var18 > 32767L || var19 < -32768L || var19 > 32767L || var20 < -32768L || var20 > 32767L;
                    if (var21 || this.teleportDelay > 400 || this.wasRiding || this.wasOnGround != this.entity.isOnGround()) {
                        this.wasOnGround = this.entity.isOnGround();
                        this.teleportDelay = 0;
                        var15 = new ClientboundTeleportEntityPacket(this.entity);
                    } else if ((!var16 || !var17) && !(this.entity instanceof AbstractArrow)) {
                        if (var16) {
                            var15 = new ClientboundMoveEntityPacket.Pos(
                                this.entity.getId(), (short)((int)var18), (short)((int)var19), (short)((int)var20), this.entity.isOnGround()
                            );
                        } else if (var17) {
                            var15 = new ClientboundMoveEntityPacket.Rot(this.entity.getId(), (byte)var11, (byte)var12, this.entity.isOnGround());
                        }
                    } else {
                        var15 = new ClientboundMoveEntityPacket.PosRot(
                            this.entity.getId(),
                            (short)((int)var18),
                            (short)((int)var19),
                            (short)((int)var20),
                            (byte)var11,
                            (byte)var12,
                            this.entity.isOnGround()
                        );
                    }
                }

                if ((this.trackDelta || this.entity.hasImpulse || this.entity instanceof LivingEntity && ((LivingEntity)this.entity).isFallFlying())
                    && this.tickCount > 0) {
                    Vec3 var22 = this.entity.getDeltaMovement();
                    double var23 = var22.distanceToSqr(this.ap);
                    if (var23 > 1.0E-7 || var23 > 0.0 && var22.lengthSqr() == 0.0) {
                        this.ap = var22;
                        this.broadcast.accept(new ClientboundSetEntityMotionPacket(this.entity.getId(), this.ap));
                    }
                }

                if (var15 != null) {
                    this.broadcast.accept(var15);
                }

                this.sendDirtyEntityData();
                if (var16) {
                    var7.setBase(var13);
                }

                if (var17) {
                    this.yRotp = var11;
                    this.xRotp = var12;
                }

                this.wasRiding = false;
            }

            int var24 = Mth.floor(this.entity.getYHeadRot() * 256.0F / 360.0F);
            if (Math.abs(var24 - this.yHeadRotp) >= 1) {
                this.broadcast.accept(new ClientboundRotateHeadPacket(this.entity, (byte)var24));
                this.yHeadRotp = var24;
            }

            this.entity.hasImpulse = false;
        }

        ++this.tickCount;
        if (this.entity.hurtMarked) {
            this.broadcastAndSend(new ClientboundSetEntityMotionPacket(this.entity));
            this.entity.hurtMarked = false;
        }

    }

    public void removePairing(ServerPlayer param0) {
        this.entity.stopSeenByPlayer(param0);
        param0.connection.send(new ClientboundRemoveEntitiesPacket(this.entity.getId()));
    }

    public void addPairing(ServerPlayer param0) {
        this.sendPairingData(param0.connection::send);
        this.entity.startSeenByPlayer(param0);
    }

    public void sendPairingData(Consumer<Packet<?>> param0) {
        if (this.entity.isRemoved()) {
            LOGGER.warn("Fetching packet for removed entity {}", this.entity);
        }

        Packet<?> var0 = this.entity.getAddEntityPacket();
        this.yHeadRotp = Mth.floor(this.entity.getYHeadRot() * 256.0F / 360.0F);
        param0.accept(var0);
        if (!this.entity.getEntityData().isEmpty()) {
            param0.accept(new ClientboundSetEntityDataPacket(this.entity.getId(), this.entity.getEntityData(), true));
        }

        boolean var1 = this.trackDelta;
        if (this.entity instanceof LivingEntity) {
            Collection<AttributeInstance> var2 = ((LivingEntity)this.entity).getAttributes().getSyncableAttributes();
            if (!var2.isEmpty()) {
                param0.accept(new ClientboundUpdateAttributesPacket(this.entity.getId(), var2));
            }

            if (((LivingEntity)this.entity).isFallFlying()) {
                var1 = true;
            }
        }

        this.ap = this.entity.getDeltaMovement();
        if (var1 && !(this.entity instanceof LivingEntity)) {
            param0.accept(new ClientboundSetEntityMotionPacket(this.entity.getId(), this.ap));
        }

        if (this.entity instanceof LivingEntity) {
            List<Pair<EquipmentSlot, ItemStack>> var3 = Lists.newArrayList();

            for(EquipmentSlot var4 : EquipmentSlot.values()) {
                ItemStack var5 = ((LivingEntity)this.entity).getItemBySlot(var4);
                if (!var5.isEmpty()) {
                    var3.add(Pair.of(var4, var5.copy()));
                }
            }

            if (!var3.isEmpty()) {
                param0.accept(new ClientboundSetEquipmentPacket(this.entity.getId(), var3));
            }
        }

        if (this.entity instanceof LivingEntity var6) {
            for(MobEffectInstance var7 : var6.getActiveEffects()) {
                param0.accept(new ClientboundUpdateMobEffectPacket(this.entity.getId(), var7));
            }
        }

        if (!this.entity.getPassengers().isEmpty()) {
            param0.accept(new ClientboundSetPassengersPacket(this.entity));
        }

        if (this.entity.isPassenger()) {
            param0.accept(new ClientboundSetPassengersPacket(this.entity.getVehicle()));
        }

        if (this.entity instanceof Mob var8 && var8.isLeashed()) {
            param0.accept(new ClientboundSetEntityLinkPacket(var8, var8.getLeashHolder()));
        }

    }

    private void sendDirtyEntityData() {
        SynchedEntityData var0 = this.entity.getEntityData();
        if (var0.isDirty()) {
            this.broadcastAndSend(new ClientboundSetEntityDataPacket(this.entity.getId(), var0, false));
        }

        if (this.entity instanceof LivingEntity) {
            Set<AttributeInstance> var1 = ((LivingEntity)this.entity).getAttributes().getDirtyAttributes();
            if (!var1.isEmpty()) {
                this.broadcastAndSend(new ClientboundUpdateAttributesPacket(this.entity.getId(), var1));
            }

            var1.clear();
        }

    }

    private void broadcastAndSend(Packet<?> param0) {
        this.broadcast.accept(param0);
        if (this.entity instanceof ServerPlayer) {
            ((ServerPlayer)this.entity).connection.send(param0);
        }

    }
}
