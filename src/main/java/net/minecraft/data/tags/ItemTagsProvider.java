package net.minecraft.data.tags;

import com.google.common.collect.Lists;
import java.nio.file.Path;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ItemTagsProvider extends TagsProvider<Item> {
    private static final Logger LOGGER = LogManager.getLogger();

    public ItemTagsProvider(DataGenerator param0) {
        super(param0, Registry.ITEM);
    }

    @Override
    protected void addTags() {
        this.copy(BlockTags.WOOL, ItemTags.WOOL);
        this.copy(BlockTags.PLANKS, ItemTags.PLANKS);
        this.copy(BlockTags.STONE_BRICKS, ItemTags.STONE_BRICKS);
        this.copy(BlockTags.WOODEN_BUTTONS, ItemTags.WOODEN_BUTTONS);
        this.copy(BlockTags.BUTTONS, ItemTags.BUTTONS);
        this.copy(BlockTags.CARPETS, ItemTags.CARPETS);
        this.copy(BlockTags.WOODEN_DOORS, ItemTags.WOODEN_DOORS);
        this.copy(BlockTags.WOODEN_STAIRS, ItemTags.WOODEN_STAIRS);
        this.copy(BlockTags.WOODEN_SLABS, ItemTags.WOODEN_SLABS);
        this.copy(BlockTags.WOODEN_FENCES, ItemTags.WOODEN_FENCES);
        this.copy(BlockTags.WOODEN_PRESSURE_PLATES, ItemTags.WOODEN_PRESSURE_PLATES);
        this.copy(BlockTags.DOORS, ItemTags.DOORS);
        this.copy(BlockTags.SAPLINGS, ItemTags.SAPLINGS);
        this.copy(BlockTags.OAK_LOGS, ItemTags.OAK_LOGS);
        this.copy(BlockTags.DARK_OAK_LOGS, ItemTags.DARK_OAK_LOGS);
        this.copy(BlockTags.BIRCH_LOGS, ItemTags.BIRCH_LOGS);
        this.copy(BlockTags.ACACIA_LOGS, ItemTags.ACACIA_LOGS);
        this.copy(BlockTags.SPRUCE_LOGS, ItemTags.SPRUCE_LOGS);
        this.copy(BlockTags.JUNGLE_LOGS, ItemTags.JUNGLE_LOGS);
        this.copy(BlockTags.CRIMSON_STEMS, ItemTags.CRIMSON_STEMS);
        this.copy(BlockTags.WARPED_STEMS, ItemTags.WARPED_STEMS);
        this.copy(BlockTags.LOGS, ItemTags.LOGS);
        this.copy(BlockTags.SAND, ItemTags.SAND);
        this.copy(BlockTags.SLABS, ItemTags.SLABS);
        this.copy(BlockTags.WALLS, ItemTags.WALLS);
        this.copy(BlockTags.STAIRS, ItemTags.STAIRS);
        this.copy(BlockTags.ANVIL, ItemTags.ANVIL);
        this.copy(BlockTags.RAILS, ItemTags.RAILS);
        this.copy(BlockTags.LEAVES, ItemTags.LEAVES);
        this.copy(BlockTags.WOODEN_TRAPDOORS, ItemTags.WOODEN_TRAPDOORS);
        this.copy(BlockTags.TRAPDOORS, ItemTags.TRAPDOORS);
        this.copy(BlockTags.SMALL_FLOWERS, ItemTags.SMALL_FLOWERS);
        this.copy(BlockTags.BEDS, ItemTags.BEDS);
        this.copy(BlockTags.FENCES, ItemTags.FENCES);
        this.copy(BlockTags.TALL_FLOWERS, ItemTags.TALL_FLOWERS);
        this.copy(BlockTags.FLOWERS, ItemTags.FLOWERS);
        this.tag(ItemTags.BANNERS)
            .add(
                Items.WHITE_BANNER,
                Items.ORANGE_BANNER,
                Items.MAGENTA_BANNER,
                Items.LIGHT_BLUE_BANNER,
                Items.YELLOW_BANNER,
                Items.LIME_BANNER,
                Items.PINK_BANNER,
                Items.GRAY_BANNER,
                Items.LIGHT_GRAY_BANNER,
                Items.CYAN_BANNER,
                Items.PURPLE_BANNER,
                Items.BLUE_BANNER,
                Items.BROWN_BANNER,
                Items.GREEN_BANNER,
                Items.RED_BANNER,
                Items.BLACK_BANNER
            );
        this.tag(ItemTags.BOATS).add(Items.OAK_BOAT, Items.SPRUCE_BOAT, Items.BIRCH_BOAT, Items.JUNGLE_BOAT, Items.ACACIA_BOAT, Items.DARK_OAK_BOAT);
        this.tag(ItemTags.FISHES).add(Items.COD, Items.COOKED_COD, Items.SALMON, Items.COOKED_SALMON, Items.PUFFERFISH, Items.TROPICAL_FISH);
        this.copy(BlockTags.STANDING_SIGNS, ItemTags.SIGNS);
        this.tag(ItemTags.MUSIC_DISCS)
            .add(
                Items.MUSIC_DISC_13,
                Items.MUSIC_DISC_CAT,
                Items.MUSIC_DISC_BLOCKS,
                Items.MUSIC_DISC_CHIRP,
                Items.MUSIC_DISC_FAR,
                Items.MUSIC_DISC_MALL,
                Items.MUSIC_DISC_MELLOHI,
                Items.MUSIC_DISC_STAL,
                Items.MUSIC_DISC_STRAD,
                Items.MUSIC_DISC_WARD,
                Items.MUSIC_DISC_11,
                Items.MUSIC_DISC_WAIT
            );
        this.tag(ItemTags.COALS).add(Items.COAL, Items.CHARCOAL);
        this.tag(ItemTags.ARROWS).add(Items.ARROW, Items.TIPPED_ARROW, Items.SPECTRAL_ARROW);
        this.tag(ItemTags.LECTERN_BOOKS).add(Items.WRITTEN_BOOK, Items.WRITABLE_BOOK);
        this.tag(ItemTags.BEACON_PAYMENT_ITEMS).add(Items.NETHERITE_INGOT, Items.EMERALD, Items.DIAMOND, Items.GOLD_INGOT, Items.IRON_INGOT);
    }

    protected void copy(Tag<Block> param0, Tag<Item> param1) {
        Tag.Builder<Item> var0 = this.tag(param1);

        for(Tag.Entry<Block> var1 : param0.getSource()) {
            Tag.Entry<Item> var2 = this.copy(var1);
            var0.add(var2);
        }

    }

    private Tag.Entry<Item> copy(Tag.Entry<Block> param0) {
        if (param0 instanceof Tag.TagEntry) {
            return new Tag.TagEntry<>(((Tag.TagEntry)param0).getId());
        } else if (param0 instanceof Tag.ValuesEntry) {
            List<Item> var0 = Lists.newArrayList();

            for(Block var1 : ((Tag.ValuesEntry)param0).getValues()) {
                Item var2 = var1.asItem();
                if (var2 == Items.AIR) {
                    LOGGER.warn("Itemless block copied to item tag: {}", Registry.BLOCK.getKey(var1));
                } else {
                    var0.add(var2);
                }
            }

            return new Tag.ValuesEntry<>(var0);
        } else {
            throw new UnsupportedOperationException("Unknown tag entry " + param0);
        }
    }

    @Override
    protected Path getPath(ResourceLocation param0) {
        return this.generator.getOutputFolder().resolve("data/" + param0.getNamespace() + "/tags/items/" + param0.getPath() + ".json");
    }

    @Override
    public String getName() {
        return "Item Tags";
    }

    @Override
    protected void useTags(TagCollection<Item> param0) {
        ItemTags.reset(param0);
    }
}
