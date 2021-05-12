package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Clearable;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.LecternMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class LecternBlockEntity extends BlockEntity implements Clearable, MenuProvider {
    public static final int DATA_PAGE = 0;
    public static final int NUM_DATA = 1;
    public static final int SLOT_BOOK = 0;
    public static final int NUM_SLOTS = 1;
    private final Container bookAccess = new Container() {
        @Override
        public int getContainerSize() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return LecternBlockEntity.this.book.isEmpty();
        }

        @Override
        public ItemStack getItem(int param0) {
            return param0 == 0 ? LecternBlockEntity.this.book : ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItem(int param0, int param1) {
            if (param0 == 0) {
                ItemStack var0 = LecternBlockEntity.this.book.split(param1);
                if (LecternBlockEntity.this.book.isEmpty()) {
                    LecternBlockEntity.this.onBookItemRemove();
                }

                return var0;
            } else {
                return ItemStack.EMPTY;
            }
        }

        @Override
        public ItemStack removeItemNoUpdate(int param0) {
            if (param0 == 0) {
                ItemStack var0 = LecternBlockEntity.this.book;
                LecternBlockEntity.this.book = ItemStack.EMPTY;
                LecternBlockEntity.this.onBookItemRemove();
                return var0;
            } else {
                return ItemStack.EMPTY;
            }
        }

        @Override
        public void setItem(int param0, ItemStack param1) {
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public void setChanged() {
            LecternBlockEntity.this.setChanged();
        }

        @Override
        public boolean stillValid(Player param0) {
            if (LecternBlockEntity.this.level.getBlockEntity(LecternBlockEntity.this.worldPosition) != LecternBlockEntity.this) {
                return false;
            } else {
                return param0.distanceToSqr(
                            (double)LecternBlockEntity.this.worldPosition.getX() + 0.5,
                            (double)LecternBlockEntity.this.worldPosition.getY() + 0.5,
                            (double)LecternBlockEntity.this.worldPosition.getZ() + 0.5
                        )
                        > 64.0
                    ? false
                    : LecternBlockEntity.this.hasBook();
            }
        }

        @Override
        public boolean canPlaceItem(int param0, ItemStack param1) {
            return false;
        }

        @Override
        public void clearContent() {
        }
    };
    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int param0) {
            return param0 == 0 ? LecternBlockEntity.this.page : 0;
        }

        @Override
        public void set(int param0, int param1) {
            if (param0 == 0) {
                LecternBlockEntity.this.setPage(param1);
            }

        }

        @Override
        public int getCount() {
            return 1;
        }
    };
    ItemStack book = ItemStack.EMPTY;
    int page;
    private int pageCount;

    public LecternBlockEntity(BlockPos param0, BlockState param1) {
        super(BlockEntityType.LECTERN, param0, param1);
    }

    public ItemStack getBook() {
        return this.book;
    }

    public boolean hasBook() {
        return this.book.is(Items.WRITABLE_BOOK) || this.book.is(Items.WRITTEN_BOOK);
    }

    public void setBook(ItemStack param0) {
        this.setBook(param0, null);
    }

    void onBookItemRemove() {
        this.page = 0;
        this.pageCount = 0;
        LecternBlock.resetBookState(this.getLevel(), this.getBlockPos(), this.getBlockState(), false);
    }

    public void setBook(ItemStack param0, @Nullable Player param1) {
        this.book = this.resolveBook(param0, param1);
        this.page = 0;
        this.pageCount = WrittenBookItem.getPageCount(this.book);
        this.setChanged();
    }

    void setPage(int param0) {
        int var0 = Mth.clamp(param0, 0, this.pageCount - 1);
        if (var0 != this.page) {
            this.page = var0;
            this.setChanged();
            LecternBlock.signalPageChange(this.getLevel(), this.getBlockPos(), this.getBlockState());
        }

    }

    public int getPage() {
        return this.page;
    }

    public int getRedstoneSignal() {
        float var0 = this.pageCount > 1 ? (float)this.getPage() / ((float)this.pageCount - 1.0F) : 1.0F;
        return Mth.floor(var0 * 14.0F) + (this.hasBook() ? 1 : 0);
    }

    private ItemStack resolveBook(ItemStack param0, @Nullable Player param1) {
        if (this.level instanceof ServerLevel && param0.is(Items.WRITTEN_BOOK)) {
            WrittenBookItem.resolveBookComponents(param0, this.createCommandSourceStack(param1), param1);
        }

        return param0;
    }

    private CommandSourceStack createCommandSourceStack(@Nullable Player param0) {
        String var0;
        Component var1;
        if (param0 == null) {
            var0 = "Lectern";
            var1 = new TextComponent("Lectern");
        } else {
            var0 = param0.getName().getString();
            var1 = param0.getDisplayName();
        }

        Vec3 var4 = Vec3.atCenterOf(this.worldPosition);
        return new CommandSourceStack(CommandSource.NULL, var4, Vec2.ZERO, (ServerLevel)this.level, 2, var0, var1, this.level.getServer(), param0);
    }

    @Override
    public boolean onlyOpCanSetNbt() {
        return true;
    }

    @Override
    public void load(CompoundTag param0) {
        super.load(param0);
        if (param0.contains("Book", 10)) {
            this.book = this.resolveBook(ItemStack.of(param0.getCompound("Book")), null);
        } else {
            this.book = ItemStack.EMPTY;
        }

        this.pageCount = WrittenBookItem.getPageCount(this.book);
        this.page = Mth.clamp(param0.getInt("Page"), 0, this.pageCount - 1);
    }

    @Override
    public CompoundTag save(CompoundTag param0) {
        super.save(param0);
        if (!this.getBook().isEmpty()) {
            param0.put("Book", this.getBook().save(new CompoundTag()));
            param0.putInt("Page", this.page);
        }

        return param0;
    }

    @Override
    public void clearContent() {
        this.setBook(ItemStack.EMPTY);
    }

    @Override
    public AbstractContainerMenu createMenu(int param0, Inventory param1, Player param2) {
        return new LecternMenu(param0, this.bookAccess, this.dataAccess);
    }

    @Override
    public Component getDisplayName() {
        return new TranslatableComponent("container.lectern");
    }
}
