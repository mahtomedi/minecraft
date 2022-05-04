package net.minecraft.client.color.item;

import net.minecraft.client.color.block.BlockColors;
import net.minecraft.core.IdMapper;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ItemColors {
    private static final int DEFAULT = -1;
    private final IdMapper<ItemColor> itemColors = new IdMapper<>(32);

    public static ItemColors createDefault(BlockColors param0) {
        ItemColors var0 = new ItemColors();
        var0.register(
            (param0x, param1) -> param1 > 0 ? -1 : ((DyeableLeatherItem)param0x.getItem()).getColor(param0x),
            Items.LEATHER_HELMET,
            Items.LEATHER_CHESTPLATE,
            Items.LEATHER_LEGGINGS,
            Items.LEATHER_BOOTS,
            Items.LEATHER_HORSE_ARMOR
        );
        var0.register((param0x, param1) -> GrassColor.get(0.5, 1.0), Blocks.TALL_GRASS, Blocks.LARGE_FERN);
        var0.register((param0x, param1) -> {
            if (param1 != 1) {
                return -1;
            } else {
                CompoundTag var0x = param0x.getTagElement("Explosion");
                int[] var1x = var0x != null && var0x.contains("Colors", 11) ? var0x.getIntArray("Colors") : null;
                if (var1x != null && var1x.length != 0) {
                    if (var1x.length == 1) {
                        return var1x[0];
                    } else {
                        int var2 = 0;
                        int var3x = 0;
                        int var4 = 0;

                        for(int var5 : var1x) {
                            var2 += (var5 & 0xFF0000) >> 16;
                            var3x += (var5 & 0xFF00) >> 8;
                            var4 += (var5 & 0xFF) >> 0;
                        }

                        var2 /= var1x.length;
                        var3x /= var1x.length;
                        var4 /= var1x.length;
                        return var2 << 16 | var3x << 8 | var4;
                    }
                } else {
                    return 9079434;
                }
            }
        }, Items.FIREWORK_STAR);
        var0.register((param0x, param1) -> param1 > 0 ? -1 : PotionUtils.getColor(param0x), Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION);

        for(SpawnEggItem var1 : SpawnEggItem.eggs()) {
            var0.register((param1, param2) -> var1.getColor(param2), var1);
        }

        var0.register(
            (param1, param2) -> {
                BlockState var0x = ((BlockItem)param1.getItem()).getBlock().defaultBlockState();
                return param0.getColor(var0x, null, null, param2);
            },
            Blocks.GRASS_BLOCK,
            Blocks.GRASS,
            Blocks.FERN,
            Blocks.VINE,
            Blocks.OAK_LEAVES,
            Blocks.SPRUCE_LEAVES,
            Blocks.BIRCH_LEAVES,
            Blocks.JUNGLE_LEAVES,
            Blocks.ACACIA_LEAVES,
            Blocks.DARK_OAK_LEAVES,
            Blocks.LILY_PAD
        );
        var0.register((param0x, param1) -> FoliageColor.getMangroveColor(), Blocks.MANGROVE_LEAVES);
        var0.register((param0x, param1) -> param1 == 0 ? PotionUtils.getColor(param0x) : -1, Items.TIPPED_ARROW);
        var0.register((param0x, param1) -> param1 == 0 ? -1 : MapItem.getColor(param0x), Items.FILLED_MAP);
        return var0;
    }

    public int getColor(ItemStack param0, int param1) {
        ItemColor var0 = this.itemColors.byId(Registry.ITEM.getId(param0.getItem()));
        return var0 == null ? -1 : var0.getColor(param0, param1);
    }

    public void register(ItemColor param0, ItemLike... param1) {
        for(ItemLike var0 : param1) {
            this.itemColors.addMapping(param0, Item.getId(var0.asItem()));
        }

    }
}
