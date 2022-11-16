package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.slf4j.Logger;

public class ChiseledBookShelfBlockEntity extends BlockEntity implements Container {
    public static final int MAX_BOOKS_IN_STORAGE = 6;
    private static final Logger LOGGER = LogUtils.getLogger();
    private final NonNullList<ItemStack> items = NonNullList.withSize(6, ItemStack.EMPTY);

    public ChiseledBookShelfBlockEntity(BlockPos param0, BlockState param1) {
        super(BlockEntityType.CHISELED_BOOKSHELF, param0, param1);
    }

    private void updateState(int param0) {
        if (param0 >= 0 && param0 < 6) {
            BlockState var0 = this.getBlockState().setValue(BlockStateProperties.CHISELED_BOOKSHELF_LAST_INTERACTION_BOOK_SLOT, Integer.valueOf(param0 + 1));

            for(int var1 = 0; var1 < ChiseledBookShelfBlock.SLOT_OCCUPIED_PROPERTIES.size(); ++var1) {
                boolean var2 = !this.getItem(var1).isEmpty();
                BooleanProperty var3 = ChiseledBookShelfBlock.SLOT_OCCUPIED_PROPERTIES.get(var1);
                var0 = var0.setValue(var3, Boolean.valueOf(var2));
            }

            Objects.requireNonNull(this.level).setBlock(this.worldPosition, var0, 3);
        } else {
            LOGGER.error("Expected slot 0-5, got {}", param0);
        }
    }

    @Override
    public void load(CompoundTag param0) {
        ContainerHelper.loadAllItems(param0, this.items);
    }

    @Override
    protected void saveAdditional(CompoundTag param0) {
        ContainerHelper.saveAllItems(param0, this.items, true);
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag var0 = new CompoundTag();
        ContainerHelper.saveAllItems(var0, this.items, true);
        return var0;
    }

    public int count() {
        return (int)this.items.stream().filter(Predicate.not(ItemStack::isEmpty)).count();
    }

    @Override
    public void clearContent() {
        this.items.clear();
    }

    @Override
    public int getContainerSize() {
        return 6;
    }

    @Override
    public boolean isEmpty() {
        return this.items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int param0) {
        return this.items.get(param0);
    }

    @Override
    public ItemStack removeItem(int param0, int param1) {
        ItemStack var0 = Objects.requireNonNullElse(this.items.get(param0), ItemStack.EMPTY);
        this.items.set(param0, ItemStack.EMPTY);
        if (!var0.isEmpty()) {
            this.updateState(param0);
        }

        return var0;
    }

    @Override
    public ItemStack removeItemNoUpdate(int param0) {
        return this.removeItem(param0, 1);
    }

    @Override
    public void setItem(int param0, ItemStack param1) {
        if (param1.is(ItemTags.BOOKSHELF_BOOKS)) {
            this.items.set(param0, param1);
            this.updateState(param0);
        }

    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean stillValid(Player param0) {
        if (this.level == null) {
            return false;
        } else if (this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        } else {
            return !(
                param0.distanceToSqr((double)this.worldPosition.getX() + 0.5, (double)this.worldPosition.getY() + 0.5, (double)this.worldPosition.getZ() + 0.5)
                    > 64.0
            );
        }
    }

    @Override
    public boolean canPlaceItem(int param0, ItemStack param1) {
        return param1.is(ItemTags.BOOKSHELF_BOOKS) && this.getItem(param0).isEmpty();
    }
}
