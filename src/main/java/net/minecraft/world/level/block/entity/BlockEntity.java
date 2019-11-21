package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.CrashReportCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class BlockEntity {
    private static final Logger LOGGER = LogManager.getLogger();
    private final BlockEntityType<?> type;
    @Nullable
    protected Level level;
    protected BlockPos worldPosition = BlockPos.ZERO;
    protected boolean remove;
    @Nullable
    private BlockState blockState;
    private boolean hasLoggedInvalidStateBefore;

    public BlockEntity(BlockEntityType<?> param0) {
        this.type = param0;
    }

    @Nullable
    public Level getLevel() {
        return this.level;
    }

    public void setLevelAndPosition(Level param0, BlockPos param1) {
        this.level = param0;
        this.worldPosition = param1.immutable();
    }

    public boolean hasLevel() {
        return this.level != null;
    }

    public void load(CompoundTag param0) {
        this.worldPosition = new BlockPos(param0.getInt("x"), param0.getInt("y"), param0.getInt("z"));
    }

    public CompoundTag save(CompoundTag param0) {
        return this.saveMetadata(param0);
    }

    private CompoundTag saveMetadata(CompoundTag param0) {
        ResourceLocation var0 = BlockEntityType.getKey(this.getType());
        if (var0 == null) {
            throw new RuntimeException(this.getClass() + " is missing a mapping! This is a bug!");
        } else {
            param0.putString("id", var0.toString());
            param0.putInt("x", this.worldPosition.getX());
            param0.putInt("y", this.worldPosition.getY());
            param0.putInt("z", this.worldPosition.getZ());
            return param0;
        }
    }

    @Nullable
    public static BlockEntity loadStatic(CompoundTag param0) {
        String var0 = param0.getString("id");
        return Registry.BLOCK_ENTITY_TYPE.getOptional(new ResourceLocation(var0)).map(param1 -> {
            try {
                return param1.create();
            } catch (Throwable var3) {
                LOGGER.error("Failed to create block entity {}", var0, var3);
                return null;
            }
        }).map(param2 -> {
            try {
                param2.load(param0);
                return param2;
            } catch (Throwable var4) {
                LOGGER.error("Failed to load data for block entity {}", var0, var4);
                return null;
            }
        }).orElseGet(() -> {
            LOGGER.warn("Skipping BlockEntity with id {}", var0);
            return null;
        });
    }

    public void setChanged() {
        if (this.level != null) {
            this.blockState = this.level.getBlockState(this.worldPosition);
            this.level.blockEntityChanged(this.worldPosition, this);
            if (!this.blockState.isAir()) {
                this.level.updateNeighbourForOutputSignal(this.worldPosition, this.blockState.getBlock());
            }
        }

    }

    @OnlyIn(Dist.CLIENT)
    public double distanceToSqr(double param0, double param1, double param2) {
        double var0 = (double)this.worldPosition.getX() + 0.5 - param0;
        double var1 = (double)this.worldPosition.getY() + 0.5 - param1;
        double var2 = (double)this.worldPosition.getZ() + 0.5 - param2;
        return var0 * var0 + var1 * var1 + var2 * var2;
    }

    @OnlyIn(Dist.CLIENT)
    public double getViewDistance() {
        return 4096.0;
    }

    public BlockPos getBlockPos() {
        return this.worldPosition;
    }

    public BlockState getBlockState() {
        if (this.blockState == null) {
            this.blockState = this.level.getBlockState(this.worldPosition);
        }

        return this.blockState;
    }

    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return null;
    }

    public CompoundTag getUpdateTag() {
        return this.saveMetadata(new CompoundTag());
    }

    public boolean isRemoved() {
        return this.remove;
    }

    public void setRemoved() {
        this.remove = true;
    }

    public void clearRemoved() {
        this.remove = false;
    }

    public boolean triggerEvent(int param0, int param1) {
        return false;
    }

    public void clearCache() {
        this.blockState = null;
    }

    public void fillCrashReportCategory(CrashReportCategory param0) {
        param0.setDetail("Name", () -> Registry.BLOCK_ENTITY_TYPE.getKey(this.getType()) + " // " + this.getClass().getCanonicalName());
        if (this.level != null) {
            CrashReportCategory.populateBlockDetails(param0, this.worldPosition, this.getBlockState());
            CrashReportCategory.populateBlockDetails(param0, this.worldPosition, this.level.getBlockState(this.worldPosition));
        }
    }

    public void setPosition(BlockPos param0) {
        this.worldPosition = param0.immutable();
    }

    public boolean onlyOpCanSetNbt() {
        return false;
    }

    public void rotate(Rotation param0) {
    }

    public void mirror(Mirror param0) {
    }

    public BlockEntityType<?> getType() {
        return this.type;
    }

    public void logInvalidState() {
        if (!this.hasLoggedInvalidStateBefore) {
            this.hasLoggedInvalidStateBefore = true;
            LOGGER.warn("Block entity invalid: {} @ {}", () -> Registry.BLOCK_ENTITY_TYPE.getKey(this.getType()), this::getBlockPos);
        }
    }
}
