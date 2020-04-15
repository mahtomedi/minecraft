package net.minecraft.world.level.block.entity;

import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.end.TheEndDimension;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.EndGatewayConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TheEndGatewayBlockEntity extends TheEndPortalBlockEntity implements TickableBlockEntity {
    private static final Logger LOGGER = LogManager.getLogger();
    private long age;
    private int teleportCooldown;
    @Nullable
    private BlockPos exitPortal;
    private boolean exactTeleport;

    public TheEndGatewayBlockEntity() {
        super(BlockEntityType.END_GATEWAY);
    }

    @Override
    public CompoundTag save(CompoundTag param0) {
        super.save(param0);
        param0.putLong("Age", this.age);
        if (this.exitPortal != null) {
            param0.put("ExitPortal", NbtUtils.writeBlockPos(this.exitPortal));
        }

        if (this.exactTeleport) {
            param0.putBoolean("ExactTeleport", this.exactTeleport);
        }

        return param0;
    }

    @Override
    public void load(BlockState param0, CompoundTag param1) {
        super.load(param0, param1);
        this.age = param1.getLong("Age");
        if (param1.contains("ExitPortal", 10)) {
            this.exitPortal = NbtUtils.readBlockPos(param1.getCompound("ExitPortal"));
        }

        this.exactTeleport = param1.getBoolean("ExactTeleport");
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public double getViewDistance() {
        return 256.0;
    }

    @Override
    public void tick() {
        boolean var0 = this.isSpawning();
        boolean var1 = this.isCoolingDown();
        ++this.age;
        if (var1) {
            --this.teleportCooldown;
        } else if (!this.level.isClientSide) {
            List<Entity> var2 = this.level.getEntitiesOfClass(Entity.class, new AABB(this.getBlockPos()));
            if (!var2.isEmpty()) {
                this.teleportEntity(var2.get(0).getRootVehicle());
            }

            if (this.age % 2400L == 0L) {
                this.triggerCooldown();
            }
        }

        if (var0 != this.isSpawning() || var1 != this.isCoolingDown()) {
            this.setChanged();
        }

    }

    public boolean isSpawning() {
        return this.age < 200L;
    }

    public boolean isCoolingDown() {
        return this.teleportCooldown > 0;
    }

    @OnlyIn(Dist.CLIENT)
    public float getSpawnPercent(float param0) {
        return Mth.clamp(((float)this.age + param0) / 200.0F, 0.0F, 1.0F);
    }

    @OnlyIn(Dist.CLIENT)
    public float getCooldownPercent(float param0) {
        return 1.0F - Mth.clamp(((float)this.teleportCooldown - param0) / 40.0F, 0.0F, 1.0F);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 8, this.getUpdateTag());
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.save(new CompoundTag());
    }

    public void triggerCooldown() {
        if (!this.level.isClientSide) {
            this.teleportCooldown = 40;
            this.level.blockEvent(this.getBlockPos(), this.getBlockState().getBlock(), 1, 0);
            this.setChanged();
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

    public void teleportEntity(Entity param0) {
        if (this.level instanceof ServerLevel && !this.isCoolingDown()) {
            this.teleportCooldown = 100;
            if (this.exitPortal == null && this.level.dimension instanceof TheEndDimension) {
                this.findExitPortal((ServerLevel)this.level);
            }

            if (this.exitPortal != null) {
                BlockPos var0 = this.exactTeleport ? this.exitPortal : this.findExitPosition();
                param0.teleportToWithTicket((double)var0.getX() + 0.5, (double)var0.getY() + 0.5, (double)var0.getZ() + 0.5);
            }

            this.triggerCooldown();
        }
    }

    private BlockPos findExitPosition() {
        BlockPos var0 = findTallestBlock(this.level, this.exitPortal, 5, false);
        LOGGER.debug("Best exit position for portal at {} is {}", this.exitPortal, var0);
        return var0.above();
    }

    private void findExitPortal(ServerLevel param0) {
        Vec3 var0 = new Vec3((double)this.getBlockPos().getX(), 0.0, (double)this.getBlockPos().getZ()).normalize();
        Vec3 var1 = var0.scale(1024.0);

        for(int var2 = 16; getChunk(param0, var1).getHighestSectionPosition() > 0 && var2-- > 0; var1 = var1.add(var0.scale(-16.0))) {
            LOGGER.debug("Skipping backwards past nonempty chunk at {}", var1);
        }

        for(int var6 = 16; getChunk(param0, var1).getHighestSectionPosition() == 0 && var6-- > 0; var1 = var1.add(var0.scale(16.0))) {
            LOGGER.debug("Skipping forward past empty chunk at {}", var1);
        }

        LOGGER.debug("Found chunk at {}", var1);
        LevelChunk var3 = getChunk(param0, var1);
        this.exitPortal = findValidSpawnInChunk(var3);
        if (this.exitPortal == null) {
            this.exitPortal = new BlockPos(var1.x + 0.5, 75.0, var1.z + 0.5);
            LOGGER.debug("Failed to find suitable block, settling on {}", this.exitPortal);
            Feature.END_ISLAND
                .configured(FeatureConfiguration.NONE)
                .place(param0, param0.structureFeatureManager(), param0.getChunkSource().getGenerator(), new Random(this.exitPortal.asLong()), this.exitPortal);
        } else {
            LOGGER.debug("Found block at {}", this.exitPortal);
        }

        this.exitPortal = findTallestBlock(param0, this.exitPortal, 16, true);
        LOGGER.debug("Creating portal at {}", this.exitPortal);
        this.exitPortal = this.exitPortal.above(10);
        this.createExitPortal(param0, this.exitPortal);
        this.setChanged();
    }

    private static BlockPos findTallestBlock(BlockGetter param0, BlockPos param1, int param2, boolean param3) {
        BlockPos var0 = null;

        for(int var1 = -param2; var1 <= param2; ++var1) {
            for(int var2 = -param2; var2 <= param2; ++var2) {
                if (var1 != 0 || var2 != 0 || param3) {
                    for(int var3 = 255; var3 > (var0 == null ? 0 : var0.getY()); --var3) {
                        BlockPos var4 = new BlockPos(param1.getX() + var1, var3, param1.getZ() + var2);
                        BlockState var5 = param0.getBlockState(var4);
                        if (var5.isCollisionShapeFullBlock(param0, var4) && (param3 || var5.getBlock() != Blocks.BEDROCK)) {
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
            if (var7.getBlock() == Blocks.END_STONE
                && !param0.getBlockState(var8).isCollisionShapeFullBlock(param0, var8)
                && !param0.getBlockState(var9).isCollisionShapeFullBlock(param0, var9)) {
                double var10 = var6.distSqr(0.0, 0.0, 0.0, true);
                if (var4 == null || var10 < var5) {
                    var4 = var6;
                    var5 = var10;
                }
            }
        }

        return var4;
    }

    private void createExitPortal(ServerLevel param0, BlockPos param1) {
        Feature.END_GATEWAY
            .configured(EndGatewayConfiguration.knownExit(this.getBlockPos(), false))
            .place(param0, param0.structureFeatureManager(), param0.getChunkSource().getGenerator(), new Random(), param1);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean shouldRenderFace(Direction param0) {
        return Block.shouldRenderFace(this.getBlockState(), this.level, this.getBlockPos(), param0);
    }

    @OnlyIn(Dist.CLIENT)
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
