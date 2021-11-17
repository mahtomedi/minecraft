package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.CrashReportCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class BlockEntity {
    private static final Logger LOGGER = LogManager.getLogger();
    private final BlockEntityType<?> type;
    @Nullable
    protected Level level;
    protected final BlockPos worldPosition;
    protected boolean remove;
    private BlockState blockState;

    public BlockEntity(BlockEntityType<?> param0, BlockPos param1, BlockState param2) {
        this.type = param0;
        this.worldPosition = param1.immutable();
        this.blockState = param2;
    }

    public static BlockPos getPosFromTag(CompoundTag param0) {
        return new BlockPos(param0.getInt("x"), param0.getInt("y"), param0.getInt("z"));
    }

    @Nullable
    public Level getLevel() {
        return this.level;
    }

    public void setLevel(Level param0) {
        this.level = param0;
    }

    public boolean hasLevel() {
        return this.level != null;
    }

    public void load(CompoundTag param0) {
    }

    protected void saveAdditional(CompoundTag param0) {
    }

    public final CompoundTag saveWithFullMetadata() {
        CompoundTag var0 = this.saveWithoutMetadata();
        this.saveMetadata(var0);
        return var0;
    }

    public final CompoundTag saveWithId() {
        CompoundTag var0 = this.saveWithoutMetadata();
        this.saveId(var0);
        return var0;
    }

    public final CompoundTag saveWithoutMetadata() {
        CompoundTag var0 = new CompoundTag();
        this.saveAdditional(var0);
        return var0;
    }

    private void saveId(CompoundTag param0) {
        ResourceLocation var0 = BlockEntityType.getKey(this.getType());
        if (var0 == null) {
            throw new RuntimeException(this.getClass() + " is missing a mapping! This is a bug!");
        } else {
            param0.putString("id", var0.toString());
        }
    }

    public static void addEntityType(CompoundTag param0, BlockEntityType<?> param1) {
        param0.putString("id", BlockEntityType.getKey(param1).toString());
    }

    public void saveToItem(ItemStack param0) {
        BlockItem.setBlockEntityData(param0, this.getType(), this.saveWithoutMetadata());
    }

    private void saveMetadata(CompoundTag param0) {
        this.saveId(param0);
        param0.putInt("x", this.worldPosition.getX());
        param0.putInt("y", this.worldPosition.getY());
        param0.putInt("z", this.worldPosition.getZ());
    }

    @Nullable
    public static BlockEntity loadStatic(BlockPos param0, BlockState param1, CompoundTag param2) {
        String var0 = param2.getString("id");
        ResourceLocation var1 = ResourceLocation.tryParse(var0);
        if (var1 == null) {
            LOGGER.error("Block entity has invalid type: {}", var0);
            return null;
        } else {
            return Registry.BLOCK_ENTITY_TYPE.getOptional(var1).map(param3 -> {
                try {
                    return param3.create(param0, param1);
                } catch (Throwable var5) {
                    LOGGER.error("Failed to create block entity {}", var0, var5);
                    return null;
                }
            }).map(param2x -> {
                try {
                    param2x.load(param2);
                    return param2x;
                } catch (Throwable var4x) {
                    LOGGER.error("Failed to load data for block entity {}", var0, var4x);
                    return null;
                }
            }).orElseGet(() -> {
                LOGGER.warn("Skipping BlockEntity with id {}", var0);
                return null;
            });
        }
    }

    public void setChanged() {
        if (this.level != null) {
            setChanged(this.level, this.worldPosition, this.blockState);
        }

    }

    protected static void setChanged(Level param0, BlockPos param1, BlockState param2) {
        param0.blockEntityChanged(param1);
        if (!param2.isAir()) {
            param0.updateNeighbourForOutputSignal(param1, param2.getBlock());
        }

    }

    public BlockPos getBlockPos() {
        return this.worldPosition;
    }

    public BlockState getBlockState() {
        return this.blockState;
    }

    @Nullable
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return null;
    }

    public CompoundTag getUpdateTag() {
        return new CompoundTag();
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

    public void fillCrashReportCategory(CrashReportCategory param0) {
        param0.setDetail("Name", () -> Registry.BLOCK_ENTITY_TYPE.getKey(this.getType()) + " // " + this.getClass().getCanonicalName());
        if (this.level != null) {
            CrashReportCategory.populateBlockDetails(param0, this.level, this.worldPosition, this.getBlockState());
            CrashReportCategory.populateBlockDetails(param0, this.level, this.worldPosition, this.level.getBlockState(this.worldPosition));
        }
    }

    public boolean onlyOpCanSetNbt() {
        return false;
    }

    public BlockEntityType<?> getType() {
        return this.type;
    }

    @Deprecated
    public void setBlockState(BlockState param0) {
        this.blockState = param0;
    }
}
