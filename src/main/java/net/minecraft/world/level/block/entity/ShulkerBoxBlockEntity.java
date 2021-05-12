package net.minecraft.world.level.block.entity;

import java.util.List;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ShulkerBoxBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer {
    public static final int COLUMNS = 9;
    public static final int ROWS = 3;
    public static final int CONTAINER_SIZE = 27;
    public static final int EVENT_SET_OPEN_COUNT = 1;
    public static final int OPENING_TICK_LENGTH = 10;
    public static final float MAX_LID_HEIGHT = 0.5F;
    public static final float MAX_LID_ROTATION = 270.0F;
    public static final String ITEMS_TAG = "Items";
    private static final int[] SLOTS = IntStream.range(0, 27).toArray();
    private NonNullList<ItemStack> itemStacks = NonNullList.withSize(27, ItemStack.EMPTY);
    private int openCount;
    private ShulkerBoxBlockEntity.AnimationStatus animationStatus = ShulkerBoxBlockEntity.AnimationStatus.CLOSED;
    private float progress;
    private float progressOld;
    @Nullable
    private final DyeColor color;

    public ShulkerBoxBlockEntity(@Nullable DyeColor param0, BlockPos param1, BlockState param2) {
        super(BlockEntityType.SHULKER_BOX, param1, param2);
        this.color = param0;
    }

    public ShulkerBoxBlockEntity(BlockPos param0, BlockState param1) {
        super(BlockEntityType.SHULKER_BOX, param0, param1);
        this.color = ShulkerBoxBlock.getColorFromBlock(param1.getBlock());
    }

    public static void tick(Level param0, BlockPos param1, BlockState param2, ShulkerBoxBlockEntity param3) {
        param3.updateAnimation(param0, param1, param2);
    }

    private void updateAnimation(Level param0, BlockPos param1, BlockState param2) {
        this.progressOld = this.progress;
        switch(this.animationStatus) {
            case CLOSED:
                this.progress = 0.0F;
                break;
            case OPENING:
                this.progress += 0.1F;
                if (this.progress >= 1.0F) {
                    this.animationStatus = ShulkerBoxBlockEntity.AnimationStatus.OPENED;
                    this.progress = 1.0F;
                    doNeighborUpdates(param0, param1, param2);
                }

                this.moveCollidedEntities(param0, param1, param2);
                break;
            case CLOSING:
                this.progress -= 0.1F;
                if (this.progress <= 0.0F) {
                    this.animationStatus = ShulkerBoxBlockEntity.AnimationStatus.CLOSED;
                    this.progress = 0.0F;
                    doNeighborUpdates(param0, param1, param2);
                }
                break;
            case OPENED:
                this.progress = 1.0F;
        }

    }

    public ShulkerBoxBlockEntity.AnimationStatus getAnimationStatus() {
        return this.animationStatus;
    }

    public AABB getBoundingBox(BlockState param0) {
        return Shulker.getProgressAabb(param0.getValue(ShulkerBoxBlock.FACING), 0.5F * this.getProgress(1.0F));
    }

    private void moveCollidedEntities(Level param0, BlockPos param1, BlockState param2) {
        if (param2.getBlock() instanceof ShulkerBoxBlock) {
            Direction var0 = param2.getValue(ShulkerBoxBlock.FACING);
            AABB var1 = Shulker.getProgressDeltaAabb(var0, this.progressOld, this.progress).move(param1);
            List<Entity> var2 = param0.getEntities(null, var1);
            if (!var2.isEmpty()) {
                for(int var3 = 0; var3 < var2.size(); ++var3) {
                    Entity var4 = var2.get(var3);
                    if (var4.getPistonPushReaction() != PushReaction.IGNORE) {
                        var4.move(
                            MoverType.SHULKER_BOX,
                            new Vec3(
                                (var1.getXsize() + 0.01) * (double)var0.getStepX(),
                                (var1.getYsize() + 0.01) * (double)var0.getStepY(),
                                (var1.getZsize() + 0.01) * (double)var0.getStepZ()
                            )
                        );
                    }
                }

            }
        }
    }

    @Override
    public int getContainerSize() {
        return this.itemStacks.size();
    }

    @Override
    public boolean triggerEvent(int param0, int param1) {
        if (param0 == 1) {
            this.openCount = param1;
            if (param1 == 0) {
                this.animationStatus = ShulkerBoxBlockEntity.AnimationStatus.CLOSING;
                doNeighborUpdates(this.getLevel(), this.worldPosition, this.getBlockState());
            }

            if (param1 == 1) {
                this.animationStatus = ShulkerBoxBlockEntity.AnimationStatus.OPENING;
                doNeighborUpdates(this.getLevel(), this.worldPosition, this.getBlockState());
            }

            return true;
        } else {
            return super.triggerEvent(param0, param1);
        }
    }

    private static void doNeighborUpdates(Level param0, BlockPos param1, BlockState param2) {
        param2.updateNeighbourShapes(param0, param1, 3);
    }

    @Override
    public void startOpen(Player param0) {
        if (!param0.isSpectator()) {
            if (this.openCount < 0) {
                this.openCount = 0;
            }

            ++this.openCount;
            this.level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), 1, this.openCount);
            if (this.openCount == 1) {
                this.level.gameEvent(param0, GameEvent.CONTAINER_OPEN, this.worldPosition);
                this.level
                    .playSound(null, this.worldPosition, SoundEvents.SHULKER_BOX_OPEN, SoundSource.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.9F);
            }
        }

    }

    @Override
    public void stopOpen(Player param0) {
        if (!param0.isSpectator()) {
            --this.openCount;
            this.level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), 1, this.openCount);
            if (this.openCount <= 0) {
                this.level.gameEvent(param0, GameEvent.CONTAINER_CLOSE, this.worldPosition);
                this.level
                    .playSound(null, this.worldPosition, SoundEvents.SHULKER_BOX_CLOSE, SoundSource.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.9F);
            }
        }

    }

    @Override
    protected Component getDefaultName() {
        return new TranslatableComponent("container.shulkerBox");
    }

    @Override
    public void load(CompoundTag param0) {
        super.load(param0);
        this.loadFromTag(param0);
    }

    @Override
    public CompoundTag save(CompoundTag param0) {
        super.save(param0);
        return this.saveToTag(param0);
    }

    public void loadFromTag(CompoundTag param0) {
        this.itemStacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(param0) && param0.contains("Items", 9)) {
            ContainerHelper.loadAllItems(param0, this.itemStacks);
        }

    }

    public CompoundTag saveToTag(CompoundTag param0) {
        if (!this.trySaveLootTable(param0)) {
            ContainerHelper.saveAllItems(param0, this.itemStacks, false);
        }

        return param0;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.itemStacks;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> param0) {
        this.itemStacks = param0;
    }

    @Override
    public int[] getSlotsForFace(Direction param0) {
        return SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int param0, ItemStack param1, @Nullable Direction param2) {
        return !(Block.byItem(param1.getItem()) instanceof ShulkerBoxBlock);
    }

    @Override
    public boolean canTakeItemThroughFace(int param0, ItemStack param1, Direction param2) {
        return true;
    }

    public float getProgress(float param0) {
        return Mth.lerp(param0, this.progressOld, this.progress);
    }

    @Nullable
    public DyeColor getColor() {
        return this.color;
    }

    @Override
    protected AbstractContainerMenu createMenu(int param0, Inventory param1) {
        return new ShulkerBoxMenu(param0, param1, this);
    }

    public boolean isClosed() {
        return this.animationStatus == ShulkerBoxBlockEntity.AnimationStatus.CLOSED;
    }

    public static enum AnimationStatus {
        CLOSED,
        OPENING,
        OPENED,
        CLOSING;
    }
}
