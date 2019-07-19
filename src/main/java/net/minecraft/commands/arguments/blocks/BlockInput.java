package net.minecraft.commands.arguments.blocks;

import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockInput implements Predicate<BlockInWorld> {
    private final BlockState state;
    private final Set<Property<?>> properties;
    @Nullable
    private final CompoundTag tag;

    public BlockInput(BlockState param0, Set<Property<?>> param1, @Nullable CompoundTag param2) {
        this.state = param0;
        this.properties = param1;
        this.tag = param2;
    }

    public BlockState getState() {
        return this.state;
    }

    public boolean test(BlockInWorld param0) {
        BlockState var0 = param0.getState();
        if (var0.getBlock() != this.state.getBlock()) {
            return false;
        } else {
            for(Property<?> var1 : this.properties) {
                if (var0.getValue(var1) != this.state.getValue(var1)) {
                    return false;
                }
            }

            if (this.tag == null) {
                return true;
            } else {
                BlockEntity var2 = param0.getEntity();
                return var2 != null && NbtUtils.compareNbt(this.tag, var2.save(new CompoundTag()), true);
            }
        }
    }

    public boolean place(ServerLevel param0, BlockPos param1, int param2) {
        if (!param0.setBlock(param1, this.state, param2)) {
            return false;
        } else {
            if (this.tag != null) {
                BlockEntity var0 = param0.getBlockEntity(param1);
                if (var0 != null) {
                    CompoundTag var1 = this.tag.copy();
                    var1.putInt("x", param1.getX());
                    var1.putInt("y", param1.getY());
                    var1.putInt("z", param1.getZ());
                    var0.load(var1);
                }
            }

            return true;
        }
    }
}
