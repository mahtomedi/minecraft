package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(
    value = Dist.CLIENT,
    _interface = LidBlockEntity.class
)
public class ChestBlockEntity extends RandomizableContainerBlockEntity implements LidBlockEntity {
    private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
    private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
        @Override
        protected void onOpen(Level param0, BlockPos param1, BlockState param2) {
            ChestBlockEntity.playSound(param0, param1, param2, SoundEvents.CHEST_OPEN);
        }

        @Override
        protected void onClose(Level param0, BlockPos param1, BlockState param2) {
            ChestBlockEntity.playSound(param0, param1, param2, SoundEvents.CHEST_CLOSE);
        }

        @Override
        protected void openerCountChanged(Level param0, BlockPos param1, BlockState param2, int param3, int param4) {
            ChestBlockEntity.this.signalOpenCount(param0, param1, param2, param3, param4);
        }

        @Override
        protected boolean isOwnContainer(Player param0) {
            if (!(param0.containerMenu instanceof ChestMenu)) {
                return false;
            } else {
                Container var0 = ((ChestMenu)param0.containerMenu).getContainer();
                return var0 == ChestBlockEntity.this || var0 instanceof CompoundContainer && ((CompoundContainer)var0).contains(ChestBlockEntity.this);
            }
        }
    };
    private final ChestLidController chestLidController = new ChestLidController();

    protected ChestBlockEntity(BlockEntityType<?> param0, BlockPos param1, BlockState param2) {
        super(param0, param1, param2);
    }

    public ChestBlockEntity(BlockPos param0, BlockState param1) {
        this(BlockEntityType.CHEST, param0, param1);
    }

    @Override
    public int getContainerSize() {
        return 27;
    }

    @Override
    protected Component getDefaultName() {
        return new TranslatableComponent("container.chest");
    }

    @Override
    public void load(CompoundTag param0) {
        super.load(param0);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(param0)) {
            ContainerHelper.loadAllItems(param0, this.items);
        }

    }

    @Override
    public CompoundTag save(CompoundTag param0) {
        super.save(param0);
        if (!this.trySaveLootTable(param0)) {
            ContainerHelper.saveAllItems(param0, this.items);
        }

        return param0;
    }

    public static void lidAnimateTick(Level param0, BlockPos param1, BlockState param2, ChestBlockEntity param3) {
        param3.chestLidController.tickLid();
    }

    private static void playSound(Level param0, BlockPos param1, BlockState param2, SoundEvent param3) {
        ChestType var0 = param2.getValue(ChestBlock.TYPE);
        if (var0 != ChestType.LEFT) {
            double var1 = (double)param1.getX() + 0.5;
            double var2 = (double)param1.getY() + 0.5;
            double var3 = (double)param1.getZ() + 0.5;
            if (var0 == ChestType.RIGHT) {
                Direction var4 = ChestBlock.getConnectedDirection(param2);
                var1 += (double)var4.getStepX() * 0.5;
                var3 += (double)var4.getStepZ() * 0.5;
            }

            param0.playSound(null, var1, var2, var3, param3, SoundSource.BLOCKS, 0.5F, param0.random.nextFloat() * 0.1F + 0.9F);
        }
    }

    @Override
    public boolean triggerEvent(int param0, int param1) {
        if (param0 == 1) {
            this.chestLidController.shouldBeOpen(param1 > 0);
            return true;
        } else {
            return super.triggerEvent(param0, param1);
        }
    }

    @Override
    public void startOpen(Player param0) {
        if (!param0.isSpectator()) {
            this.openersCounter.incrementOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
        }

    }

    @Override
    public void stopOpen(Player param0) {
        if (!param0.isSpectator()) {
            this.openersCounter.decrementOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
        }

    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> param0) {
        this.items = param0;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public float getOpenNess(float param0) {
        return this.chestLidController.getOpenness(param0);
    }

    public static int getOpenCount(BlockGetter param0, BlockPos param1) {
        BlockState var0 = param0.getBlockState(param1);
        if (var0.hasBlockEntity()) {
            BlockEntity var1 = param0.getBlockEntity(param1);
            if (var1 instanceof ChestBlockEntity) {
                return ((ChestBlockEntity)var1).openersCounter.getOpenerCount();
            }
        }

        return 0;
    }

    public static void swapContents(ChestBlockEntity param0, ChestBlockEntity param1) {
        NonNullList<ItemStack> var0 = param0.getItems();
        param0.setItems(param1.getItems());
        param1.setItems(var0);
    }

    @Override
    protected AbstractContainerMenu createMenu(int param0, Inventory param1) {
        return ChestMenu.threeRows(param0, param1, this);
    }

    public void recheckOpen() {
        this.openersCounter.recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
    }

    protected void signalOpenCount(Level param0, BlockPos param1, BlockState param2, int param3, int param4) {
        Block var0 = param2.getBlock();
        param0.blockEvent(param1, var0, 1, param4);
    }
}
