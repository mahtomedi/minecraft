package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class BrushableBlockEntity extends BlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String LOOT_TABLE_TAG = "LootTable";
    private static final String LOOT_TABLE_SEED_TAG = "LootTableSeed";
    private static final String HIT_DIRECTION_TAG = "hit_direction";
    private static final String ITEM_TAG = "item";
    private static final int BRUSH_COOLDOWN_TICKS = 10;
    private static final int BRUSH_RESET_TICKS = 40;
    private static final int REQUIRED_BRUSHES_TO_BREAK = 10;
    private int brushCount;
    private long brushCountResetsAtTick;
    private long coolDownEndsAtTick;
    private ItemStack item = ItemStack.EMPTY;
    @Nullable
    private Direction hitDirection;
    @Nullable
    private ResourceLocation lootTable;
    private long lootTableSeed;

    public BrushableBlockEntity(BlockPos param0, BlockState param1) {
        super(BlockEntityType.BRUSHABLE_BLOCK, param0, param1);
    }

    public boolean brush(long param0, Player param1, Direction param2) {
        if (this.hitDirection == null) {
            this.hitDirection = param2;
        }

        this.brushCountResetsAtTick = param0 + 40L;
        if (param0 >= this.coolDownEndsAtTick && this.level instanceof ServerLevel) {
            this.coolDownEndsAtTick = param0 + 10L;
            this.unpackLootTable(param1);
            int var0 = this.getCompletionState();
            if (++this.brushCount >= 10) {
                this.brushingCompleted(param1);
                return true;
            } else {
                this.level.scheduleTick(this.getBlockPos(), this.getBlockState().getBlock(), 40);
                int var1 = this.getCompletionState();
                if (var0 != var1) {
                    BlockState var2 = this.getBlockState();
                    BlockState var3 = var2.setValue(BlockStateProperties.DUSTED, Integer.valueOf(var1));
                    this.level.setBlock(this.getBlockPos(), var3, 3);
                }

                return false;
            }
        } else {
            return false;
        }
    }

    public void unpackLootTable(Player param0) {
        if (this.lootTable != null && this.level != null && !this.level.isClientSide() && this.level.getServer() != null) {
            LootTable var0 = this.level.getServer().getLootData().getLootTable(this.lootTable);
            if (param0 instanceof ServerPlayer var1) {
                CriteriaTriggers.GENERATE_LOOT.trigger(var1, this.lootTable);
            }

            LootContext.Builder var2 = new LootContext.Builder((ServerLevel)this.level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(this.worldPosition))
                .withOptionalRandomSeed(this.lootTableSeed)
                .withLuck(param0.getLuck())
                .withParameter(LootContextParams.THIS_ENTITY, param0);
            ObjectArrayList<ItemStack> var3 = var0.getRandomItems(var2.create(LootContextParamSets.CHEST));

            this.item = switch(var3.size()) {
                case 0 -> ItemStack.EMPTY;
                case 1 -> (ItemStack)var3.get(0);
                default -> {
                    LOGGER.warn("Expected max 1 loot from loot table " + this.lootTable + " got " + var3.size());
                    yield var3.get(0);
                }
            };
            this.lootTable = null;
            this.setChanged();
        }
    }

    private void brushingCompleted(Player param0) {
        if (this.level != null && this.level.getServer() != null) {
            this.dropContent(param0);
            BlockState var0 = this.getBlockState();
            this.level.levelEvent(3008, this.getBlockPos(), Block.getId(var0));
            Block var1 = this.getBlockState().getBlock();
            Block var3;
            if (var1 instanceof BrushableBlock var2) {
                var3 = var2.getTurnsInto();
            } else {
                var3 = Blocks.AIR;
            }

            this.level.setBlock(this.worldPosition, var3.defaultBlockState(), 3);
        }
    }

    private void dropContent(Player param0) {
        if (this.level != null && this.level.getServer() != null) {
            this.unpackLootTable(param0);
            if (!this.item.isEmpty()) {
                double var0 = (double)EntityType.ITEM.getWidth();
                double var1 = 1.0 - var0;
                double var2 = var0 / 2.0;
                Direction var3 = Objects.requireNonNullElse(this.hitDirection, Direction.UP);
                BlockPos var4 = this.worldPosition.relative(var3, 1);
                double var5 = (double)var4.getX() + 0.5 * var1 + var2;
                double var6 = (double)var4.getY() + 0.5 + (double)(EntityType.ITEM.getHeight() / 2.0F);
                double var7 = (double)var4.getZ() + 0.5 * var1 + var2;
                ItemEntity var8 = new ItemEntity(this.level, var5, var6, var7, this.item.split(this.level.random.nextInt(21) + 10));
                var8.setDeltaMovement(Vec3.ZERO);
                this.level.addFreshEntity(var8);
                this.item = ItemStack.EMPTY;
            }

        }
    }

    public void checkReset() {
        if (this.level != null) {
            if (this.brushCount != 0 && this.level.getGameTime() >= this.brushCountResetsAtTick) {
                int var0 = this.getCompletionState();
                this.brushCount = Math.max(0, this.brushCount - 2);
                int var1 = this.getCompletionState();
                if (var0 != var1) {
                    this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(BlockStateProperties.DUSTED, Integer.valueOf(var1)), 3);
                }

                int var2 = 4;
                this.brushCountResetsAtTick = this.level.getGameTime() + 4L;
            }

            if (this.brushCount == 0) {
                this.hitDirection = null;
                this.brushCountResetsAtTick = 0L;
                this.coolDownEndsAtTick = 0L;
            } else {
                this.level.scheduleTick(this.getBlockPos(), this.getBlockState().getBlock(), (int)(this.brushCountResetsAtTick - this.level.getGameTime()));
            }

        }
    }

    private boolean tryLoadLootTable(CompoundTag param0) {
        if (param0.contains("LootTable", 8)) {
            this.lootTable = new ResourceLocation(param0.getString("LootTable"));
            this.lootTableSeed = param0.getLong("LootTableSeed");
            return true;
        } else {
            return false;
        }
    }

    private boolean trySaveLootTable(CompoundTag param0) {
        if (this.lootTable == null) {
            return false;
        } else {
            param0.putString("LootTable", this.lootTable.toString());
            if (this.lootTableSeed != 0L) {
                param0.putLong("LootTableSeed", this.lootTableSeed);
            }

            return true;
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag var0 = super.getUpdateTag();
        if (this.hitDirection != null) {
            var0.putInt("hit_direction", this.hitDirection.ordinal());
        }

        var0.put("item", this.item.save(new CompoundTag()));
        return var0;
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void load(CompoundTag param0) {
        if (!this.tryLoadLootTable(param0) && param0.contains("item")) {
            this.item = ItemStack.of(param0.getCompound("item"));
        }

        if (param0.contains("hit_direction")) {
            this.hitDirection = Direction.values()[param0.getInt("hit_direction")];
        }

    }

    @Override
    protected void saveAdditional(CompoundTag param0) {
        if (!this.trySaveLootTable(param0)) {
            param0.put("item", this.item.save(new CompoundTag()));
        }

    }

    public void setLootTable(ResourceLocation param0, long param1) {
        this.lootTable = param0;
        this.lootTableSeed = param1;
    }

    private int getCompletionState() {
        if (this.brushCount == 0) {
            return 0;
        } else if (this.brushCount < 3) {
            return 1;
        } else {
            return this.brushCount < 6 ? 2 : 3;
        }
    }

    @Nullable
    public Direction getHitDirection() {
        return this.hitDirection;
    }

    public ItemStack getItem() {
        return this.item;
    }
}
