package net.minecraft.world.level.block.entity;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Clearable;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class ChiseledBookShelfBlockEntity extends BlockEntity implements Clearable {
    public static final int MAX_BOOKS_IN_STORAGE = 6;
    private final Deque<ItemStack> items = new ArrayDeque<>(6);

    public ChiseledBookShelfBlockEntity(BlockPos param0, BlockState param1) {
        super(BlockEntityType.CHISELED_BOOKSHELF, param0, param1);
    }

    public ItemStack removeBook() {
        return Objects.requireNonNullElse(this.items.poll(), ItemStack.EMPTY);
    }

    public void addBook(ItemStack param0) {
        if (param0.is(ItemTags.BOOKSHELF_BOOKS)) {
            this.items.addFirst(param0);
        }

    }

    @Override
    public void load(CompoundTag param0) {
        NonNullList<ItemStack> var0 = NonNullList.withSize(6, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(param0, var0);
        this.items.clear();

        for(ItemStack var1 : var0) {
            if (var1.is(ItemTags.BOOKSHELF_BOOKS)) {
                this.items.add(var1);
            }
        }

    }

    @Override
    protected void saveAdditional(CompoundTag param0) {
        ContainerHelper.saveAllItems(param0, asNonNullList(this.items), true);
    }

    @NotNull
    private static NonNullList<ItemStack> asNonNullList(Collection<ItemStack> param0) {
        NonNullList<ItemStack> var0 = NonNullList.createWithCapacity(param0.size());
        var0.addAll(param0);
        return var0;
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag var0 = new CompoundTag();
        ContainerHelper.saveAllItems(var0, asNonNullList(this.items), true);
        return var0;
    }

    @Override
    public void clearContent() {
        this.items.clear();
    }

    public int bookCount() {
        return this.items.size();
    }

    public boolean isFull() {
        return this.bookCount() == 6;
    }

    public boolean isEmpty() {
        return this.items.isEmpty();
    }
}
