package net.minecraft.world.level.block.entity;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.Util;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class DecoratedPotBlockEntity extends BlockEntity {
    public static final String TAG_SHARDS = "shards";
    private static final int SHARDS_IN_POT = 4;
    private final List<Item> shards = Util.make(new ArrayList<>(4), param0x -> {
        param0x.add(Items.BRICK);
        param0x.add(Items.BRICK);
        param0x.add(Items.BRICK);
        param0x.add(Items.BRICK);
    });

    public DecoratedPotBlockEntity(BlockPos param0, BlockState param1) {
        super(BlockEntityType.DECORATED_POT, param0, param1);
    }

    @Override
    protected void saveAdditional(CompoundTag param0) {
        super.saveAdditional(param0);
        saveShards(this.shards, param0);
    }

    @Override
    public void load(CompoundTag param0) {
        super.load(param0);
        if (param0.contains("shards", 9)) {
            ListTag var0 = param0.getList("shards", 8);
            this.shards.clear();
            int var1 = Math.min(4, var0.size());

            for(int var2 = 0; var2 < var1; ++var2) {
                Tag var6 = var0.get(var2);
                if (var6 instanceof StringTag var3) {
                    this.shards.add(BuiltInRegistries.ITEM.get(new ResourceLocation(var3.getAsString())));
                } else {
                    this.shards.add(Items.BRICK);
                }
            }

            int var4 = 4 - var1;

            for(int var5 = 0; var5 < var4; ++var5) {
                this.shards.add(Items.BRICK);
            }
        }

    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    public static void saveShards(List<Item> param0, CompoundTag param1) {
        ListTag var0 = new ListTag();

        for(Item var1 : param0) {
            var0.add(StringTag.valueOf(BuiltInRegistries.ITEM.getKey(var1).toString()));
        }

        param1.put("shards", var0);
    }

    public ItemStack getItem() {
        ItemStack var0 = new ItemStack(Blocks.DECORATED_POT);
        CompoundTag var1 = new CompoundTag();
        saveShards(this.shards, var1);
        BlockItem.setBlockEntityData(var0, BlockEntityType.DECORATED_POT, var1);
        return var0;
    }

    public List<Item> getShards() {
        return this.shards;
    }

    public Direction getDirection() {
        return this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
    }

    public void setFromItem(ItemStack param0) {
        CompoundTag var0 = BlockItem.getBlockEntityData(param0);
        if (var0 != null) {
            this.load(var0);
        } else {
            this.shards.clear();

            for(int var1 = 0; var1 < 4; ++var1) {
                this.shards.add(Items.BRICK);
            }
        }

    }
}
