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
import net.minecraft.util.Mth;
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
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(
    value = Dist.CLIENT,
    _interface = LidBlockEntity.class
)
public class ChestBlockEntity extends RandomizableContainerBlockEntity implements LidBlockEntity, TickableBlockEntity {
    private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
    protected float openness;
    protected float oOpenness;
    protected int openCount;
    private int tickInterval;

    protected ChestBlockEntity(BlockEntityType<?> param0) {
        super(param0);
    }

    public ChestBlockEntity() {
        this(BlockEntityType.CHEST);
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
    public void load(BlockState param0, CompoundTag param1) {
        super.load(param0, param1);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(param1)) {
            ContainerHelper.loadAllItems(param1, this.items);
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

    @Override
    public void tick() {
        int var0 = this.worldPosition.getX();
        int var1 = this.worldPosition.getY();
        int var2 = this.worldPosition.getZ();
        ++this.tickInterval;
        this.openCount = getOpenCount(this.level, this, this.tickInterval, var0, var1, var2, this.openCount);
        this.oOpenness = this.openness;
        float var3 = 0.1F;
        if (this.openCount > 0 && this.openness == 0.0F) {
            this.playSound(SoundEvents.CHEST_OPEN);
        }

        if (this.openCount == 0 && this.openness > 0.0F || this.openCount > 0 && this.openness < 1.0F) {
            float var4 = this.openness;
            if (this.openCount > 0) {
                this.openness += 0.1F;
            } else {
                this.openness -= 0.1F;
            }

            if (this.openness > 1.0F) {
                this.openness = 1.0F;
            }

            float var5 = 0.5F;
            if (this.openness < 0.5F && var4 >= 0.5F) {
                this.playSound(SoundEvents.CHEST_CLOSE);
            }

            if (this.openness < 0.0F) {
                this.openness = 0.0F;
            }
        }

    }

    public static int getOpenCount(Level param0, BaseContainerBlockEntity param1, int param2, int param3, int param4, int param5, int param6) {
        if (!param0.isClientSide && param6 != 0 && (param2 + param3 + param4 + param5) % 200 == 0) {
            param6 = getOpenCount(param0, param1, param3, param4, param5);
        }

        return param6;
    }

    public static int getOpenCount(Level param0, BaseContainerBlockEntity param1, int param2, int param3, int param4) {
        int var0 = 0;
        float var1 = 5.0F;

        for(Player var3 : param0.getEntitiesOfClass(
            Player.class,
            new AABB(
                (double)((float)param2 - 5.0F),
                (double)((float)param3 - 5.0F),
                (double)((float)param4 - 5.0F),
                (double)((float)(param2 + 1) + 5.0F),
                (double)((float)(param3 + 1) + 5.0F),
                (double)((float)(param4 + 1) + 5.0F)
            )
        )) {
            if (var3.containerMenu instanceof ChestMenu) {
                Container var4 = ((ChestMenu)var3.containerMenu).getContainer();
                if (var4 == param1 || var4 instanceof CompoundContainer && ((CompoundContainer)var4).contains(param1)) {
                    ++var0;
                }
            }
        }

        return var0;
    }

    private void playSound(SoundEvent param0) {
        ChestType var0 = this.getBlockState().getValue(ChestBlock.TYPE);
        if (var0 != ChestType.LEFT) {
            double var1 = (double)this.worldPosition.getX() + 0.5;
            double var2 = (double)this.worldPosition.getY() + 0.5;
            double var3 = (double)this.worldPosition.getZ() + 0.5;
            if (var0 == ChestType.RIGHT) {
                Direction var4 = ChestBlock.getConnectedDirection(this.getBlockState());
                var1 += (double)var4.getStepX() * 0.5;
                var3 += (double)var4.getStepZ() * 0.5;
            }

            this.level.playSound(null, var1, var2, var3, param0, SoundSource.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.9F);
        }
    }

    @Override
    public boolean triggerEvent(int param0, int param1) {
        if (param0 == 1) {
            this.openCount = param1;
            return true;
        } else {
            return super.triggerEvent(param0, param1);
        }
    }

    @Override
    public void startOpen(Player param0) {
        if (!param0.isSpectator()) {
            if (this.openCount < 0) {
                this.openCount = 0;
            }

            ++this.openCount;
            this.signalOpenCount();
        }

    }

    @Override
    public void stopOpen(Player param0) {
        if (!param0.isSpectator()) {
            --this.openCount;
            this.signalOpenCount();
        }

    }

    protected void signalOpenCount() {
        Block var0 = this.getBlockState().getBlock();
        if (var0 instanceof ChestBlock) {
            this.level.blockEvent(this.worldPosition, var0, 1, this.openCount);
            this.level.updateNeighborsAt(this.worldPosition, var0);
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
        return Mth.lerp(param0, this.oOpenness, this.openness);
    }

    public static int getOpenCount(BlockGetter param0, BlockPos param1) {
        BlockState var0 = param0.getBlockState(param1);
        if (var0.getBlock().isEntityBlock()) {
            BlockEntity var1 = param0.getBlockEntity(param1);
            if (var1 instanceof ChestBlockEntity) {
                return ((ChestBlockEntity)var1).openCount;
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
}
