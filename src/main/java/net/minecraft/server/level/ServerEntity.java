package net.minecraft.server.level;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddMobPacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerEntity {
    private static final Logger LOGGER = LogManager.getLogger();
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
        this.yRotp = Mth.floor(param1.yRot * 256.0F / 360.0F);
        this.xRotp = Mth.floor(param1.xRot * 256.0F / 360.0F);
        this.yHeadRotp = Mth.floor(param1.getYHeadRot() * 256.0F / 360.0F);
        this.wasOnGround = param1.isOnGround();
    }

    public void sendChanges() {
        List<Entity> var0 = this.entity.getPassengers();
        if (!var0.equals(this.lastPassengers)) {
            this.lastPassengers = var0;
            this.broadcast.accept(new ClientboundSetPassengersPacket(this.entity));
        }

        if (this.entity instanceof ItemFrame && this.tickCount % 10 == 0) {
            ItemFrame var1 = (ItemFrame)this.entity;
            ItemStack var2 = var1.getItem();
            if (var2.getItem() instanceof MapItem) {
                MapItemSavedData var3 = MapItem.getOrCreateSavedData(var2, this.level);

                for(ServerPlayer var4 : this.level.players()) {
                    var3.tickCarriedBy(var4, var2);
                    Packet<?> var5 = ((MapItem)var2.getItem()).getUpdatePacket(var2, this.level, var4);
                    if (var5 != null) {
                        var4.connection.send(var5);
                    }
                }
            }

            this.sendDirtyEntityData();
        }

        if (this.tickCount % this.updateInterval == 0 || this.entity.hasImpulse || this.entity.getEntityData().isDirty()) {
            if (this.entity.isPassenger()) {
                int var6 = Mth.floor(this.entity.yRot * 256.0F / 360.0F);
                int var7 = Mth.floor(this.entity.xRot * 256.0F / 360.0F);
                boolean var8 = Math.abs(var6 - this.yRotp) >= 1 || Math.abs(var7 - this.xRotp) >= 1;
                if (var8) {
                    this.broadcast.accept(new ClientboundMoveEntityPacket.Rot(this.entity.getId(), (byte)var6, (byte)var7, this.entity.isOnGround()));
                    this.yRotp = var6;
                    this.xRotp = var7;
                }

                this.updateSentPos();
                this.sendDirtyEntityData();
                this.wasRiding = true;
            } else {
                ++this.teleportDelay;
                int var9 = Mth.floor(this.entity.yRot * 256.0F / 360.0F);
                int var10 = Mth.floor(this.entity.xRot * 256.0F / 360.0F);
                Vec3 var11 = this.entity.position().subtract(ClientboundMoveEntityPacket.packetToEntity(this.xp, this.yp, this.zp));
                boolean var12 = var11.lengthSqr() >= 7.6293945E-6F;
                Packet<?> var13 = null;
                boolean var14 = var12 || this.tickCount % 60 == 0;
                boolean var15 = Math.abs(var9 - this.yRotp) >= 1 || Math.abs(var10 - this.xRotp) >= 1;
                if (this.tickCount > 0 || this.entity instanceof AbstractArrow) {
                    long var16 = ClientboundMoveEntityPacket.entityToPacket(var11.x);
                    long var17 = ClientboundMoveEntityPacket.entityToPacket(var11.y);
                    long var18 = ClientboundMoveEntityPacket.entityToPacket(var11.z);
                    boolean var19 = var16 < -32768L || var16 > 32767L || var17 < -32768L || var17 > 32767L || var18 < -32768L || var18 > 32767L;
                    if (var19 || this.teleportDelay > 400 || this.wasRiding || this.wasOnGround != this.entity.isOnGround()) {
                        this.wasOnGround = this.entity.isOnGround();
                        this.teleportDelay = 0;
                        var13 = new ClientboundTeleportEntityPacket(this.entity);
                    } else if ((!var14 || !var15) && !(this.entity instanceof AbstractArrow)) {
                        if (var14) {
                            var13 = new ClientboundMoveEntityPacket.Pos(
                                this.entity.getId(), (short)((int)var16), (short)((int)var17), (short)((int)var18), this.entity.isOnGround()
                            );
                        } else if (var15) {
                            var13 = new ClientboundMoveEntityPacket.Rot(this.entity.getId(), (byte)var9, (byte)var10, this.entity.isOnGround());
                        }
                    } else {
                        var13 = new ClientboundMoveEntityPacket.PosRot(
                            this.entity.getId(),
                            (short)((int)var16),
                            (short)((int)var17),
                            (short)((int)var18),
                            (byte)var9,
                            (byte)var10,
                            this.entity.isOnGround()
                        );
                    }
                }

                if ((this.trackDelta || this.entity.hasImpulse || this.entity instanceof LivingEntity && ((LivingEntity)this.entity).isFallFlying())
                    && this.tickCount > 0) {
                    Vec3 var20 = this.entity.getDeltaMovement();
                    double var21 = var20.distanceToSqr(this.ap);
                    if (var21 > 1.0E-7 || var21 > 0.0 && var20.lengthSqr() == 0.0) {
                        this.ap = var20;
                        this.broadcast.accept(new ClientboundSetEntityMotionPacket(this.entity.getId(), this.ap));
                    }
                }

                if (var13 != null) {
                    this.broadcast.accept(var13);
                }

                this.sendDirtyEntityData();
                if (var14) {
                    this.updateSentPos();
                }

                if (var15) {
                    this.yRotp = var9;
                    this.xRotp = var10;
                }

                this.wasRiding = false;
            }

            int var22 = Mth.floor(this.entity.getYHeadRot() * 256.0F / 360.0F);
            if (Math.abs(var22 - this.yHeadRotp) >= 1) {
                this.broadcast.accept(new ClientboundRotateHeadPacket(this.entity, (byte)var22));
                this.yHeadRotp = var22;
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
        param0.sendRemoveEntity(this.entity);
    }

    public void addPairing(ServerPlayer param0) {
        this.sendPairingData(param0.connection::send);
        this.entity.startSeenByPlayer(param0);
        param0.cancelRemoveEntity(this.entity);
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

        if (this.entity instanceof LivingEntity) {
            LivingEntity var6 = (LivingEntity)this.entity;

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

        if (this.entity instanceof Mob) {
            Mob var8 = (Mob)this.entity;
            if (var8.isLeashed()) {
                param0.accept(new ClientboundSetEntityLinkPacket(var8, var8.getLeashHolder()));
            }
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
