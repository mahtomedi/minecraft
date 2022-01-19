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
import net.minecraft.network.protocol.game.ClientboundAddMobPacket;
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
    private long xp;
    private long yp;
    private long zp;
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
        this.updateSentPos();
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

        if (this.entity instanceof ItemFrame var1 && this.tickCount % 10 == 0) {
            ItemStack var2 = var1.getItem();
            if (var2.getItem() instanceof MapItem) {
                Integer var3 = MapItem.getMapId(var2);
                MapItemSavedData var4 = MapItem.getSavedData(var3, this.level);
                if (var4 != null) {
                    for(ServerPlayer var5 : this.level.players()) {
                        var4.tickCarriedBy(var5, var2);
                        Packet<?> var6 = var4.getUpdatePacket(var3, var5);
                        if (var6 != null) {
                            var5.connection.send(var6);
                        }
                    }
                }
            }

            this.sendDirtyEntityData();
        }

        if (this.tickCount % this.updateInterval == 0 || this.entity.hasImpulse || this.entity.getEntityData().isDirty()) {
            if (this.entity.isPassenger()) {
                int var7 = Mth.floor(this.entity.getYRot() * 256.0F / 360.0F);
                int var8 = Mth.floor(this.entity.getXRot() * 256.0F / 360.0F);
                boolean var9 = Math.abs(var7 - this.yRotp) >= 1 || Math.abs(var8 - this.xRotp) >= 1;
                if (var9) {
                    this.broadcast.accept(new ClientboundMoveEntityPacket.Rot(this.entity.getId(), (byte)var7, (byte)var8, this.entity.isOnGround()));
                    this.yRotp = var7;
                    this.xRotp = var8;
                }

                this.updateSentPos();
                this.sendDirtyEntityData();
                this.wasRiding = true;
            } else {
                ++this.teleportDelay;
                int var10 = Mth.floor(this.entity.getYRot() * 256.0F / 360.0F);
                int var11 = Mth.floor(this.entity.getXRot() * 256.0F / 360.0F);
                Vec3 var12 = this.entity.position().subtract(ClientboundMoveEntityPacket.packetToEntity(this.xp, this.yp, this.zp));
                boolean var13 = var12.lengthSqr() >= 7.6293945E-6F;
                Packet<?> var14 = null;
                boolean var15 = var13 || this.tickCount % 60 == 0;
                boolean var16 = Math.abs(var10 - this.yRotp) >= 1 || Math.abs(var11 - this.xRotp) >= 1;
                if (this.tickCount > 0 || this.entity instanceof AbstractArrow) {
                    long var17 = ClientboundMoveEntityPacket.entityToPacket(var12.x);
                    long var18 = ClientboundMoveEntityPacket.entityToPacket(var12.y);
                    long var19 = ClientboundMoveEntityPacket.entityToPacket(var12.z);
                    boolean var20 = var17 < -32768L || var17 > 32767L || var18 < -32768L || var18 > 32767L || var19 < -32768L || var19 > 32767L;
                    if (var20 || this.teleportDelay > 400 || this.wasRiding || this.wasOnGround != this.entity.isOnGround()) {
                        this.wasOnGround = this.entity.isOnGround();
                        this.teleportDelay = 0;
                        var14 = new ClientboundTeleportEntityPacket(this.entity);
                    } else if ((!var15 || !var16) && !(this.entity instanceof AbstractArrow)) {
                        if (var15) {
                            var14 = new ClientboundMoveEntityPacket.Pos(
                                this.entity.getId(), (short)((int)var17), (short)((int)var18), (short)((int)var19), this.entity.isOnGround()
                            );
                        } else if (var16) {
                            var14 = new ClientboundMoveEntityPacket.Rot(this.entity.getId(), (byte)var10, (byte)var11, this.entity.isOnGround());
                        }
                    } else {
                        var14 = new ClientboundMoveEntityPacket.PosRot(
                            this.entity.getId(),
                            (short)((int)var17),
                            (short)((int)var18),
                            (short)((int)var19),
                            (byte)var10,
                            (byte)var11,
                            this.entity.isOnGround()
                        );
                    }
                }

                if ((this.trackDelta || this.entity.hasImpulse || this.entity instanceof LivingEntity && ((LivingEntity)this.entity).isFallFlying())
                    && this.tickCount > 0) {
                    Vec3 var21 = this.entity.getDeltaMovement();
                    double var22 = var21.distanceToSqr(this.ap);
                    if (var22 > 1.0E-7 || var22 > 0.0 && var21.lengthSqr() == 0.0) {
                        this.ap = var21;
                        this.broadcast.accept(new ClientboundSetEntityMotionPacket(this.entity.getId(), this.ap));
                    }
                }

                if (var14 != null) {
                    this.broadcast.accept(var14);
                }

                this.sendDirtyEntityData();
                if (var15) {
                    this.updateSentPos();
                }

                if (var16) {
                    this.yRotp = var10;
                    this.xRotp = var11;
                }

                this.wasRiding = false;
            }

            int var23 = Mth.floor(this.entity.getYHeadRot() * 256.0F / 360.0F);
            if (Math.abs(var23 - this.yHeadRotp) >= 1) {
                this.broadcast.accept(new ClientboundRotateHeadPacket(this.entity, (byte)var23));
                this.yHeadRotp = var23;
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
        if (var1 && !(var0 instanceof ClientboundAddMobPacket)) {
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

    private void updateSentPos() {
        this.xp = ClientboundMoveEntityPacket.entityToPacket(this.entity.getX());
        this.yp = ClientboundMoveEntityPacket.entityToPacket(this.entity.getY());
        this.zp = ClientboundMoveEntityPacket.entityToPacket(this.entity.getZ());
    }

    public Vec3 sentPos() {
        return ClientboundMoveEntityPacket.packetToEntity(this.xp, this.yp, this.zp);
    }

    private void broadcastAndSend(Packet<?> param0) {
        this.broadcast.accept(param0);
        if (this.entity instanceof ServerPlayer) {
            ((ServerPlayer)this.entity).connection.send(param0);
        }

    }
}
