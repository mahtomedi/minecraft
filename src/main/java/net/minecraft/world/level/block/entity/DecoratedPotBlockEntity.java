package net.minecraft.world.level.block.entity;

import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class DecoratedPotBlockEntity extends BlockEntity {
    public static final String TAG_SHERDS = "sherds";
    private DecoratedPotBlockEntity.Decorations decorations = DecoratedPotBlockEntity.Decorations.EMPTY;

    public DecoratedPotBlockEntity(BlockPos param0, BlockState param1) {
        super(BlockEntityType.DECORATED_POT, param0, param1);
    }

    @Override
    protected void saveAdditional(CompoundTag param0) {
        super.saveAdditional(param0);
        this.decorations.save(param0);
    }

    @Override
    public void load(CompoundTag param0) {
        super.load(param0);
        this.decorations = DecoratedPotBlockEntity.Decorations.load(param0);
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    public Direction getDirection() {
        return this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
    }

    public DecoratedPotBlockEntity.Decorations getDecorations() {
        return this.decorations;
    }

    public void setFromItem(ItemStack param0) {
        this.decorations = DecoratedPotBlockEntity.Decorations.load(BlockItem.getBlockEntityData(param0));
    }

    public ItemStack getItem() {
        return createDecoratedPotItem(this.decorations);
    }

    public static ItemStack createDecoratedPotItem(DecoratedPotBlockEntity.Decorations param0) {
        ItemStack var0 = Items.DECORATED_POT.getDefaultInstance();
        CompoundTag var1 = param0.save(new CompoundTag());
        BlockItem.setBlockEntityData(var0, BlockEntityType.DECORATED_POT, var1);
        return var0;
    }

    public static record Decorations(Item back, Item left, Item right, Item front) {
        public static final DecoratedPotBlockEntity.Decorations EMPTY = new DecoratedPotBlockEntity.Decorations(
            Items.BRICK, Items.BRICK, Items.BRICK, Items.BRICK
        );

        public CompoundTag save(CompoundTag param0) {
            if (this.equals(EMPTY)) {
                return param0;
            } else {
                ListTag var0 = new ListTag();
                this.sorted().forEach(param1 -> var0.add(StringTag.valueOf(BuiltInRegistries.ITEM.getKey(param1).toString())));
                param0.put("sherds", var0);
                return param0;
            }
        }

        public Stream<Item> sorted() {
            return Stream.of(this.back, this.left, this.right, this.front);
        }

        public static DecoratedPotBlockEntity.Decorations load(@Nullable CompoundTag param0) {
            if (param0 != null && param0.contains("sherds", 9)) {
                ListTag var0 = param0.getList("sherds", 8);
                return new DecoratedPotBlockEntity.Decorations(itemFromTag(var0, 0), itemFromTag(var0, 1), itemFromTag(var0, 2), itemFromTag(var0, 3));
            } else {
                return EMPTY;
            }
        }

        private static Item itemFromTag(ListTag param0, int param1) {
            if (param1 >= param0.size()) {
                return Items.BRICK;
            } else {
                Tag var0 = param0.get(param1);
                return BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(var0.getAsString()));
            }
        }
    }
}
