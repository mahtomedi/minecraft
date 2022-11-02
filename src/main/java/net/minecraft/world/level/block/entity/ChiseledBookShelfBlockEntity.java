package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.StackInventory;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.slf4j.Logger;

public class ChiseledBookShelfBlockEntity extends BlockEntity implements Container {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int MAX_BOOKS_IN_STORAGE = 6;
    private final StackInventory books = new StackInventory(6);

    public ChiseledBookShelfBlockEntity(BlockPos param0, BlockState param1) {
        super(BlockEntityType.CHISELED_BOOKSHELF, param0, param1);
    }

    private void updateBlockState() {
        int var0 = this.getBlockState().getValue(BlockStateProperties.BOOKS_STORED);
        if (var0 != this.books.size()) {
            Objects.requireNonNull(this.level)
                .setBlock(
                    this.worldPosition,
                    this.getBlockState()
                        .setValue(BlockStateProperties.BOOKS_STORED, Integer.valueOf(this.books.size()))
                        .setValue(
                            BlockStateProperties.LAST_INTERACTION_BOOK_SLOT,
                            Integer.valueOf(var0 > this.books.size() ? this.books.size() + 1 : this.books.size())
                        ),
                    3
                );
        }
    }

    public ItemStack removeBook() {
        ItemStack var0 = this.books.pop();
        if (!var0.isEmpty()) {
            this.updateBlockState();
        }

        return var0;
    }

    public List<ItemStack> removeAllBooksWithoutBlockStateUpdate() {
        return this.books.clear();
    }

    public boolean addBook(ItemStack param0) {
        if (this.isFull()) {
            return false;
        } else if (param0.getCount() > 1) {
            LOGGER.warn("tried to add a stack with more than one items {} at {}", param0, this.worldPosition);
            return false;
        } else if (!param0.is(ItemTags.BOOKSHELF_BOOKS)) {
            LOGGER.warn("tried to add a non book: {} at {}", param0, this.worldPosition);
            return false;
        } else if (!this.books.push(param0)) {
            LOGGER.warn("failed to add {} at {}", param0, this.worldPosition);
            return false;
        } else {
            this.updateBlockState();
            return true;
        }
    }

    @Override
    public void load(CompoundTag param0) {
        NonNullList<ItemStack> var0 = NonNullList.withSize(6, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(param0, var0);
        this.books.clear();

        for(int var1 = 0; var1 < var0.size(); ++var1) {
            ItemStack var2 = var0.get(var1);
            if (!var2.isEmpty()) {
                this.books.pushWithSlot(var2, var1);
            }
        }

    }

    @Override
    protected void saveAdditional(CompoundTag param0) {
        ContainerHelper.saveAllItems(param0, asNonNullList(this.books), true);
    }

    private static NonNullList<ItemStack> asNonNullList(StackInventory param0) {
        NonNullList<ItemStack> var0 = NonNullList.withSize(6, ItemStack.EMPTY);

        for(int var1 = 0; var1 < 6; ++var1) {
            var0.set(var1, param0.get(var1));
        }

        return var0;
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag var0 = new CompoundTag();
        ContainerHelper.saveAllItems(var0, asNonNullList(this.books), true);
        return var0;
    }

    @Override
    public void clearContent() {
        this.books.clear();
    }

    public int bookCount() {
        return this.books.size();
    }

    public boolean isFull() {
        return this.books.isFull();
    }

    @Override
    public int getContainerSize() {
        return 6;
    }

    @Override
    public void setChanged() {
        this.books.flatten();
        this.updateBlockState();
    }

    @Override
    public boolean isEmpty() {
        return this.books.isEmpty();
    }

    @Override
    public ItemStack getItem(int param0) {
        return this.books.get(param0);
    }

    @Override
    public ItemStack removeItem(int param0, int param1) {
        ItemStack var0 = this.removeItemNoUpdate(param0);
        this.updateBlockState();
        return var0;
    }

    @Override
    public ItemStack removeItemNoUpdate(int param0) {
        return this.books.remove(param0);
    }

    @Override
    public void setItem(int param0, ItemStack param1) {
        if (this.books.set(param1, param0)) {
            this.updateBlockState();
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
            return param0.distanceToSqr(
                    (double)this.worldPosition.getX() + 0.5, (double)this.worldPosition.getY() + 0.5, (double)this.worldPosition.getZ() + 0.5
                )
                <= 64.0;
        }
    }

    @Override
    public boolean canPlaceItem(int param0, ItemStack param1) {
        return !this.isFull() && param1.is(ItemTags.BOOKSHELF_BOOKS) && this.books.canSet(param0);
    }

    @Override
    public int countItem(Item param0) {
        return (int)this.books.view().stream().filter(param1 -> param1.is(param0)).count();
    }

    @Override
    public boolean hasAnyOf(Set<Item> param0) {
        return this.books.view().stream().anyMatch(param1 -> param0.contains(param1.getItem()));
    }

    @Override
    public boolean hasAnyMatching(Predicate<ItemStack> param0) {
        return this.books.view().stream().anyMatch(param0);
    }
}
