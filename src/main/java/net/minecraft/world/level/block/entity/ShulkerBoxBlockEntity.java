package net.minecraft.world.level.block.entity;

import java.util.List;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
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
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ShulkerBoxBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer, TickableBlockEntity {
    private static final int[] SLOTS = IntStream.range(0, 27).toArray();
    private NonNullList<ItemStack> itemStacks = NonNullList.withSize(27, ItemStack.EMPTY);
    private int openCount;
    private ShulkerBoxBlockEntity.AnimationStatus animationStatus = ShulkerBoxBlockEntity.AnimationStatus.CLOSED;
    private float progress;
    private float progressOld;
    private DyeColor color;
    private boolean loadColorFromBlock;

    public ShulkerBoxBlockEntity(@Nullable DyeColor param0) {
        super(BlockEntityType.SHULKER_BOX);
        this.color = param0;
    }

    public ShulkerBoxBlockEntity() {
        this(null);
        this.loadColorFromBlock = true;
    }

    @Override
    public void tick() {
        this.updateAnimation();
        if (this.animationStatus == ShulkerBoxBlockEntity.AnimationStatus.OPENING || this.animationStatus == ShulkerBoxBlockEntity.AnimationStatus.CLOSING) {
            this.moveCollidedEntities();
        }

    }

    protected void updateAnimation() {
        this.progressOld = this.progress;
        switch(this.animationStatus) {
            case CLOSED:
                this.progress = 0.0F;
                break;
            case OPENING:
                this.progress += 0.1F;
                if (this.progress >= 1.0F) {
                    this.moveCollidedEntities();
                    this.animationStatus = ShulkerBoxBlockEntity.AnimationStatus.OPENED;
                    this.progress = 1.0F;
                    this.doNeighborUpdates();
                }
                break;
            case CLOSING:
                this.progress -= 0.1F;
                if (this.progress <= 0.0F) {
                    this.animationStatus = ShulkerBoxBlockEntity.AnimationStatus.CLOSED;
                    this.progress = 0.0F;
                    this.doNeighborUpdates();
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
        return this.getBoundingBox(param0.getValue(ShulkerBoxBlock.FACING));
    }

    public AABB getBoundingBox(Direction param0) {
        float var0 = this.getProgress(1.0F);
        return Shapes.block()
            .bounds()
            .expandTowards(
                (double)(0.5F * var0 * (float)param0.getStepX()),
                (double)(0.5F * var0 * (float)param0.getStepY()),
                (double)(0.5F * var0 * (float)param0.getStepZ())
            );
    }

    private AABB getTopBoundingBox(Direction param0) {
        Direction var0 = param0.getOpposite();
        return this.getBoundingBox(param0).contract((double)var0.getStepX(), (double)var0.getStepY(), (double)var0.getStepZ());
    }

    private void moveCollidedEntities() {
        BlockState var0 = this.level.getBlockState(this.getBlockPos());
        if (var0.getBlock() instanceof ShulkerBoxBlock) {
            Direction var1 = var0.getValue(ShulkerBoxBlock.FACING);
            AABB var2 = this.getTopBoundingBox(var1).move(this.worldPosition);
            List<Entity> var3 = this.level.getEntities(null, var2);
            if (!var3.isEmpty()) {
                for(int var4 = 0; var4 < var3.size(); ++var4) {
                    Entity var5 = var3.get(var4);
                    if (var5.getPistonPushReaction() != PushReaction.IGNORE) {
                        double var6 = 0.0;
                        double var7 = 0.0;
                        double var8 = 0.0;
                        AABB var9 = var5.getBoundingBox();
                        switch(var1.getAxis()) {
                            case X:
                                if (var1.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
                                    var6 = var2.maxX - var9.minX;
                                } else {
                                    var6 = var9.maxX - var2.minX;
                                }

                                var6 += 0.01;
                                break;
                            case Y:
                                if (var1.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
                                    var7 = var2.maxY - var9.minY;
                                } else {
                                    var7 = var9.maxY - var2.minY;
                                }

                                var7 += 0.01;
                                break;
                            case Z:
                                if (var1.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
                                    var8 = var2.maxZ - var9.minZ;
                                } else {
                                    var8 = var9.maxZ - var2.minZ;
                                }

                                var8 += 0.01;
                        }

                        var5.move(
                            MoverType.SHULKER_BOX, new Vec3(var6 * (double)var1.getStepX(), var7 * (double)var1.getStepY(), var8 * (double)var1.getStepZ())
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
                this.doNeighborUpdates();
            }

            if (param1 == 1) {
                this.animationStatus = ShulkerBoxBlockEntity.AnimationStatus.OPENING;
                this.doNeighborUpdates();
            }

            return true;
        } else {
            return super.triggerEvent(param0, param1);
        }
    }

    private void doNeighborUpdates() {
        this.getBlockState().updateNeighbourShapes(this.getLevel(), this.getBlockPos(), 3);
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
    public boolean isEmpty() {
        for(ItemStack var0 : this.itemStacks) {
            if (!var0.isEmpty()) {
                return false;
            }
        }

        return true;
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

    @OnlyIn(Dist.CLIENT)
    public DyeColor getColor() {
        if (this.loadColorFromBlock) {
            this.color = ShulkerBoxBlock.getColorFromBlock(this.getBlockState().getBlock());
            this.loadColorFromBlock = false;
        }

        return this.color;
    }

    @Override
    protected AbstractContainerMenu createMenu(int param0, Inventory param1) {
        return new ShulkerBoxMenu(param0, param1, this);
    }

    public static enum AnimationStatus {
        CLOSED,
        OPENING,
        OPENED,
        CLOSING;
    }
}
