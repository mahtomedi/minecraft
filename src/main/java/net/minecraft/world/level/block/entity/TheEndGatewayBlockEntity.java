package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.worldgen.features.EndFeatures;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.EndGatewayConfiguration;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class TheEndGatewayBlockEntity extends TheEndPortalBlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int SPAWN_TIME = 200;
    private static final int COOLDOWN_TIME = 40;
    private static final int ATTENTION_INTERVAL = 2400;
    private static final int EVENT_COOLDOWN = 1;
    private static final int GATEWAY_HEIGHT_ABOVE_SURFACE = 10;
    private long age;
    private int teleportCooldown;
    @Nullable
    private BlockPos exitPortal;
    private boolean exactTeleport;

    public TheEndGatewayBlockEntity(BlockPos param0, BlockState param1) {
        super(BlockEntityType.END_GATEWAY, param0, param1);
    }

    @Override
    protected void saveAdditional(CompoundTag param0) {
        super.saveAdditional(param0);
        param0.putLong("Age", this.age);
        if (this.exitPortal != null) {
            param0.put("ExitPortal", NbtUtils.writeBlockPos(this.exitPortal));
        }

        if (this.exactTeleport) {
            param0.putBoolean("ExactTeleport", true);
        }

    }

    @Override
    public void load(CompoundTag param0) {
        super.load(param0);
        this.age = param0.getLong("Age");
        if (param0.contains("ExitPortal", 10)) {
            BlockPos var0 = NbtUtils.readBlockPos(param0.getCompound("ExitPortal"));
            if (Level.isInSpawnableBounds(var0)) {
                this.exitPortal = var0;
            }
        }

        this.exactTeleport = param0.getBoolean("ExactTeleport");
    }

    public static void beamAnimationTick(Level param0, BlockPos param1, BlockState param2, TheEndGatewayBlockEntity param3) {
        ++param3.age;
        if (param3.isCoolingDown()) {
            --param3.teleportCooldown;
        }

    }

    public static void teleportTick(Level param0, BlockPos param1, BlockState param2, TheEndGatewayBlockEntity param3) {
        boolean var0 = param3.isSpawning();
        boolean var1 = param3.isCoolingDown();
        ++param3.age;
        if (var1) {
            --param3.teleportCooldown;
        } else {
            List<Entity> var2 = param0.getEntitiesOfClass(Entity.class, new AABB(param1), TheEndGatewayBlockEntity::canEntityTeleport);
            if (!var2.isEmpty()) {
                teleportEntity(param0, param1, param2, var2.get(param0.random.nextInt(var2.size())), param3);
            }

            if (param3.age % 2400L == 0L) {
                triggerCooldown(param0, param1, param2, param3);
            }
        }

        if (var0 != param3.isSpawning() || var1 != param3.isCoolingDown()) {
            setChanged(param0, param1, param2);
        }

    }

    public static boolean canEntityTeleport(Entity param0x) {
        return EntitySelector.NO_SPECTATORS.test(param0x) && !param0x.getRootVehicle().isOnPortalCooldown();
    }

    public boolean isSpawning() {
        return this.age < 200L;
    }

    public boolean isCoolingDown() {
        return this.teleportCooldown > 0;
    }

    public float getSpawnPercent(float param0) {
        return Mth.clamp(((float)this.age + param0) / 200.0F, 0.0F, 1.0F);
    }

    public float getCooldownPercent(float param0) {
        return 1.0F - Mth.clamp(((float)this.teleportCooldown - param0) / 40.0F, 0.0F, 1.0F);
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    private static void triggerCooldown(Level param0, BlockPos param1, BlockState param2, TheEndGatewayBlockEntity param3) {
        if (!param0.isClientSide) {
            param3.teleportCooldown = 40;
            param0.blockEvent(param1, param2.getBlock(), 1, 0);
            setChanged(param0, param1, param2);
        }

    }

    @Override
    public boolean triggerEvent(int param0, int param1) {
        if (param0 == 1) {
            this.teleportCooldown = 40;
            return true;
        } else {
            return super.triggerEvent(param0, param1);
        }
    }

    public static void teleportEntity(Level param0, BlockPos param1, BlockState param2, Entity param3, TheEndGatewayBlockEntity param4) {
        if (param0 instanceof ServerLevel var0 && !param4.isCoolingDown()) {
            param4.teleportCooldown = 100;
            if (param4.exitPortal == null && param0.dimension() == Level.END) {
                BlockPos var1 = findOrCreateValidTeleportPos(var0, param1);
                var1 = var1.above(10);
                LOGGER.debug("Creating portal at {}", var1);
                spawnGatewayPortal(var0, var1, EndGatewayConfiguration.knownExit(param1, false));
                param4.exitPortal = var1;
            }

            if (param4.exitPortal != null) {
                BlockPos var2 = param4.exactTeleport ? param4.exitPortal : findExitPosition(param0, param4.exitPortal);
                Entity var4;
                if (param3 instanceof ThrownEnderpearl) {
                    Entity var3 = ((ThrownEnderpearl)param3).getOwner();
                    if (var3 instanceof ServerPlayer) {
                        CriteriaTriggers.ENTER_BLOCK.trigger((ServerPlayer)var3, param2);
                    }

                    if (var3 != null) {
                        var4 = var3;
                        param3.discard();
                    } else {
                        var4 = param3;
                    }
                } else {
                    var4 = param3.getRootVehicle();
                }

                var4.setPortalCooldown();
                var4.teleportToWithTicket((double)var2.getX() + 0.5, (double)var2.getY(), (double)var2.getZ() + 0.5);
            }

            triggerCooldown(param0, param1, param2, param4);
        }
    }

    private static BlockPos findExitPosition(Level param0, BlockPos param1) {
        BlockPos var0 = findTallestBlock(param0, param1.offset(0, 2, 0), 5, false);
        LOGGER.debug("Best exit position for portal at {} is {}", param1, var0);
        return var0.above();
    }

    private static BlockPos findOrCreateValidTeleportPos(ServerLevel param0, BlockPos param1) {
        Vec3 var0 = findExitPortalXZPosTentative(param0, param1);
        LevelChunk var1 = getChunk(param0, var0);
        BlockPos var2 = findValidSpawnInChunk(var1);
        if (var2 == null) {
            var2 = new BlockPos(var0.x + 0.5, 75.0, var0.z + 0.5);
            LOGGER.debug("Failed to find a suitable block to teleport to, spawning an island on {}", var2);
            EndFeatures.END_ISLAND.place(param0, param0.getChunkSource().getGenerator(), new Random(var2.asLong()), var2);
        } else {
            LOGGER.debug("Found suitable block to teleport to: {}", var2);
        }

        return findTallestBlock(param0, var2, 16, true);
    }

    private static Vec3 findExitPortalXZPosTentative(ServerLevel param0, BlockPos param1) {
        Vec3 var0 = new Vec3((double)param1.getX(), 0.0, (double)param1.getZ()).normalize();
        int var1 = 1024;
        Vec3 var2 = var0.scale(1024.0);

        for(int var3 = 16; !isChunkEmpty(param0, var2) && var3-- > 0; var2 = var2.add(var0.scale(-16.0))) {
            LOGGER.debug("Skipping backwards past nonempty chunk at {}", var2);
        }

        for(int var6 = 16; isChunkEmpty(param0, var2) && var6-- > 0; var2 = var2.add(var0.scale(16.0))) {
            LOGGER.debug("Skipping forward past empty chunk at {}", var2);
        }

        LOGGER.debug("Found chunk at {}", var2);
        return var2;
    }

    private static boolean isChunkEmpty(ServerLevel param0, Vec3 param1) {
        return getChunk(param0, param1).getHighestSectionPosition() <= param0.getMinBuildHeight();
    }

    private static BlockPos findTallestBlock(BlockGetter param0, BlockPos param1, int param2, boolean param3) {
        BlockPos var0 = null;

        for(int var1 = -param2; var1 <= param2; ++var1) {
            for(int var2 = -param2; var2 <= param2; ++var2) {
                if (var1 != 0 || var2 != 0 || param3) {
                    for(int var3 = param0.getMaxBuildHeight() - 1; var3 > (var0 == null ? param0.getMinBuildHeight() : var0.getY()); --var3) {
                        BlockPos var4 = new BlockPos(param1.getX() + var1, var3, param1.getZ() + var2);
                        BlockState var5 = param0.getBlockState(var4);
                        if (var5.isCollisionShapeFullBlock(param0, var4) && (param3 || !var5.is(Blocks.BEDROCK))) {
                            var0 = var4;
                            break;
                        }
                    }
                }
            }
        }

        return var0 == null ? param1 : var0;
    }

    private static LevelChunk getChunk(Level param0, Vec3 param1) {
        return param0.getChunk(Mth.floor(param1.x / 16.0), Mth.floor(param1.z / 16.0));
    }

    @Nullable
    private static BlockPos findValidSpawnInChunk(LevelChunk param0) {
        ChunkPos var0 = param0.getPos();
        BlockPos var1 = new BlockPos(var0.getMinBlockX(), 30, var0.getMinBlockZ());
        int var2 = param0.getHighestSectionPosition() + 16 - 1;
        BlockPos var3 = new BlockPos(var0.getMaxBlockX(), var2, var0.getMaxBlockZ());
        BlockPos var4 = null;
        double var5 = 0.0;

        for(BlockPos var6 : BlockPos.betweenClosed(var1, var3)) {
            BlockState var7 = param0.getBlockState(var6);
            BlockPos var8 = var6.above();
            BlockPos var9 = var6.above(2);
            if (var7.is(Blocks.END_STONE)
                && !param0.getBlockState(var8).isCollisionShapeFullBlock(param0, var8)
                && !param0.getBlockState(var9).isCollisionShapeFullBlock(param0, var9)) {
                double var10 = var6.distToCenterSqr(0.0, 0.0, 0.0);
                if (var4 == null || var10 < var5) {
                    var4 = var6;
                    var5 = var10;
                }
            }
        }

        return var4;
    }

    private static void spawnGatewayPortal(ServerLevel param0, BlockPos param1, EndGatewayConfiguration param2) {
        Feature.END_GATEWAY.configured(param2).place(param0, param0.getChunkSource().getGenerator(), new Random(), param1);
    }

    @Override
    public boolean shouldRenderFace(Direction param0) {
        return Block.shouldRenderFace(this.getBlockState(), this.level, this.getBlockPos(), param0, this.getBlockPos().relative(param0));
    }

    public int getParticleAmount() {
        int var0 = 0;

        for(Direction var1 : Direction.values()) {
            var0 += this.shouldRenderFace(var1) ? 1 : 0;
        }

        return var0;
    }

    public void setExitPosition(BlockPos param0, boolean param1) {
        this.exactTeleport = param1;
        this.exitPortal = param0;
    }
}
