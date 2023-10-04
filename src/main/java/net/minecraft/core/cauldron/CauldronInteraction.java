package net.minecraft.core.cauldron;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public interface CauldronInteraction {
    Map<String, CauldronInteraction.InteractionMap> INTERACTIONS = new Object2ObjectArrayMap<>();
    Codec<CauldronInteraction.InteractionMap> CODEC = ExtraCodecs.stringResolverCodec(CauldronInteraction.InteractionMap::name, INTERACTIONS::get);
    CauldronInteraction.InteractionMap EMPTY = newInteractionMap("empty");
    CauldronInteraction.InteractionMap WATER = newInteractionMap("water");
    CauldronInteraction.InteractionMap LAVA = newInteractionMap("lava");
    CauldronInteraction.InteractionMap POWDER_SNOW = newInteractionMap("powder_snow");
    CauldronInteraction FILL_WATER = (param0, param1, param2, param3, param4, param5) -> emptyBucket(
            param1,
            param2,
            param3,
            param4,
            param5,
            Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, Integer.valueOf(3)),
            SoundEvents.BUCKET_EMPTY
        );
    CauldronInteraction FILL_LAVA = (param0, param1, param2, param3, param4, param5) -> emptyBucket(
            param1, param2, param3, param4, param5, Blocks.LAVA_CAULDRON.defaultBlockState(), SoundEvents.BUCKET_EMPTY_LAVA
        );
    CauldronInteraction FILL_POWDER_SNOW = (param0, param1, param2, param3, param4, param5) -> emptyBucket(
            param1,
            param2,
            param3,
            param4,
            param5,
            Blocks.POWDER_SNOW_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, Integer.valueOf(3)),
            SoundEvents.BUCKET_EMPTY_POWDER_SNOW
        );
    CauldronInteraction SHULKER_BOX = (param0, param1, param2, param3, param4, param5) -> {
        Block var0 = Block.byItem(param5.getItem());
        if (!(var0 instanceof ShulkerBoxBlock)) {
            return InteractionResult.PASS;
        } else {
            if (!param1.isClientSide) {
                ItemStack var1 = new ItemStack(Blocks.SHULKER_BOX);
                if (param5.hasTag()) {
                    var1.setTag(param5.getTag().copy());
                }

                param3.setItemInHand(param4, var1);
                param3.awardStat(Stats.CLEAN_SHULKER_BOX);
                LayeredCauldronBlock.lowerFillLevel(param0, param1, param2);
            }

            return InteractionResult.sidedSuccess(param1.isClientSide);
        }
    };
    CauldronInteraction BANNER = (param0, param1, param2, param3, param4, param5) -> {
        if (BannerBlockEntity.getPatternCount(param5) <= 0) {
            return InteractionResult.PASS;
        } else {
            if (!param1.isClientSide) {
                ItemStack var0 = param5.copyWithCount(1);
                BannerBlockEntity.removeLastPattern(var0);
                if (!param3.getAbilities().instabuild) {
                    param5.shrink(1);
                }

                if (param5.isEmpty()) {
                    param3.setItemInHand(param4, var0);
                } else if (param3.getInventory().add(var0)) {
                    param3.inventoryMenu.sendAllDataToRemote();
                } else {
                    param3.drop(var0, false);
                }

                param3.awardStat(Stats.CLEAN_BANNER);
                LayeredCauldronBlock.lowerFillLevel(param0, param1, param2);
            }

            return InteractionResult.sidedSuccess(param1.isClientSide);
        }
    };
    CauldronInteraction DYED_ITEM = (param0, param1, param2, param3, param4, param5) -> {
        Item var0 = param5.getItem();
        if (!(var0 instanceof DyeableLeatherItem)) {
            return InteractionResult.PASS;
        } else {
            DyeableLeatherItem var1 = (DyeableLeatherItem)var0;
            if (!var1.hasCustomColor(param5)) {
                return InteractionResult.PASS;
            } else {
                if (!param1.isClientSide) {
                    var1.clearColor(param5);
                    param3.awardStat(Stats.CLEAN_ARMOR);
                    LayeredCauldronBlock.lowerFillLevel(param0, param1, param2);
                }

                return InteractionResult.sidedSuccess(param1.isClientSide);
            }
        }
    };

    static CauldronInteraction.InteractionMap newInteractionMap(String param0) {
        Object2ObjectOpenHashMap<Item, CauldronInteraction> var0 = new Object2ObjectOpenHashMap<>();
        var0.defaultReturnValue((param0x, param1, param2, param3, param4, param5) -> InteractionResult.PASS);
        CauldronInteraction.InteractionMap var1 = new CauldronInteraction.InteractionMap(param0, var0);
        INTERACTIONS.put(param0, var1);
        return var1;
    }

    InteractionResult interact(BlockState var1, Level var2, BlockPos var3, Player var4, InteractionHand var5, ItemStack var6);

    static void bootStrap() {
        Map<Item, CauldronInteraction> var0 = EMPTY.map();
        addDefaultInteractions(var0);
        var0.put(Items.POTION, (param0, param1, param2, param3, param4, param5) -> {
            if (PotionUtils.getPotion(param5) != Potions.WATER) {
                return InteractionResult.PASS;
            } else {
                if (!param1.isClientSide) {
                    Item var0x = param5.getItem();
                    param3.setItemInHand(param4, ItemUtils.createFilledResult(param5, param3, new ItemStack(Items.GLASS_BOTTLE)));
                    param3.awardStat(Stats.USE_CAULDRON);
                    param3.awardStat(Stats.ITEM_USED.get(var0x));
                    param1.setBlockAndUpdate(param2, Blocks.WATER_CAULDRON.defaultBlockState());
                    param1.playSound(null, param2, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                    param1.gameEvent(null, GameEvent.FLUID_PLACE, param2);
                }

                return InteractionResult.sidedSuccess(param1.isClientSide);
            }
        });
        Map<Item, CauldronInteraction> var1 = WATER.map();
        addDefaultInteractions(var1);
        var1.put(
            Items.BUCKET,
            (param0, param1, param2, param3, param4, param5) -> fillBucket(
                    param0,
                    param1,
                    param2,
                    param3,
                    param4,
                    param5,
                    new ItemStack(Items.WATER_BUCKET),
                    param0x -> param0x.getValue(LayeredCauldronBlock.LEVEL) == 3,
                    SoundEvents.BUCKET_FILL
                )
        );
        var1.put(Items.GLASS_BOTTLE, (param0, param1, param2, param3, param4, param5) -> {
            if (!param1.isClientSide) {
                Item var0x = param5.getItem();
                param3.setItemInHand(param4, ItemUtils.createFilledResult(param5, param3, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER)));
                param3.awardStat(Stats.USE_CAULDRON);
                param3.awardStat(Stats.ITEM_USED.get(var0x));
                LayeredCauldronBlock.lowerFillLevel(param0, param1, param2);
                param1.playSound(null, param2, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                param1.gameEvent(null, GameEvent.FLUID_PICKUP, param2);
            }

            return InteractionResult.sidedSuccess(param1.isClientSide);
        });
        var1.put(Items.POTION, (param0, param1, param2, param3, param4, param5) -> {
            if (param0.getValue(LayeredCauldronBlock.LEVEL) != 3 && PotionUtils.getPotion(param5) == Potions.WATER) {
                if (!param1.isClientSide) {
                    param3.setItemInHand(param4, ItemUtils.createFilledResult(param5, param3, new ItemStack(Items.GLASS_BOTTLE)));
                    param3.awardStat(Stats.USE_CAULDRON);
                    param3.awardStat(Stats.ITEM_USED.get(param5.getItem()));
                    param1.setBlockAndUpdate(param2, param0.cycle(LayeredCauldronBlock.LEVEL));
                    param1.playSound(null, param2, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                    param1.gameEvent(null, GameEvent.FLUID_PLACE, param2);
                }

                return InteractionResult.sidedSuccess(param1.isClientSide);
            } else {
                return InteractionResult.PASS;
            }
        });
        var1.put(Items.LEATHER_BOOTS, DYED_ITEM);
        var1.put(Items.LEATHER_LEGGINGS, DYED_ITEM);
        var1.put(Items.LEATHER_CHESTPLATE, DYED_ITEM);
        var1.put(Items.LEATHER_HELMET, DYED_ITEM);
        var1.put(Items.LEATHER_HORSE_ARMOR, DYED_ITEM);
        var1.put(Items.WHITE_BANNER, BANNER);
        var1.put(Items.GRAY_BANNER, BANNER);
        var1.put(Items.BLACK_BANNER, BANNER);
        var1.put(Items.BLUE_BANNER, BANNER);
        var1.put(Items.BROWN_BANNER, BANNER);
        var1.put(Items.CYAN_BANNER, BANNER);
        var1.put(Items.GREEN_BANNER, BANNER);
        var1.put(Items.LIGHT_BLUE_BANNER, BANNER);
        var1.put(Items.LIGHT_GRAY_BANNER, BANNER);
        var1.put(Items.LIME_BANNER, BANNER);
        var1.put(Items.MAGENTA_BANNER, BANNER);
        var1.put(Items.ORANGE_BANNER, BANNER);
        var1.put(Items.PINK_BANNER, BANNER);
        var1.put(Items.PURPLE_BANNER, BANNER);
        var1.put(Items.RED_BANNER, BANNER);
        var1.put(Items.YELLOW_BANNER, BANNER);
        var1.put(Items.WHITE_SHULKER_BOX, SHULKER_BOX);
        var1.put(Items.GRAY_SHULKER_BOX, SHULKER_BOX);
        var1.put(Items.BLACK_SHULKER_BOX, SHULKER_BOX);
        var1.put(Items.BLUE_SHULKER_BOX, SHULKER_BOX);
        var1.put(Items.BROWN_SHULKER_BOX, SHULKER_BOX);
        var1.put(Items.CYAN_SHULKER_BOX, SHULKER_BOX);
        var1.put(Items.GREEN_SHULKER_BOX, SHULKER_BOX);
        var1.put(Items.LIGHT_BLUE_SHULKER_BOX, SHULKER_BOX);
        var1.put(Items.LIGHT_GRAY_SHULKER_BOX, SHULKER_BOX);
        var1.put(Items.LIME_SHULKER_BOX, SHULKER_BOX);
        var1.put(Items.MAGENTA_SHULKER_BOX, SHULKER_BOX);
        var1.put(Items.ORANGE_SHULKER_BOX, SHULKER_BOX);
        var1.put(Items.PINK_SHULKER_BOX, SHULKER_BOX);
        var1.put(Items.PURPLE_SHULKER_BOX, SHULKER_BOX);
        var1.put(Items.RED_SHULKER_BOX, SHULKER_BOX);
        var1.put(Items.YELLOW_SHULKER_BOX, SHULKER_BOX);
        Map<Item, CauldronInteraction> var2 = LAVA.map();
        var2.put(
            Items.BUCKET,
            (param0, param1, param2, param3, param4, param5) -> fillBucket(
                    param0, param1, param2, param3, param4, param5, new ItemStack(Items.LAVA_BUCKET), param0x -> true, SoundEvents.BUCKET_FILL_LAVA
                )
        );
        addDefaultInteractions(var2);
        Map<Item, CauldronInteraction> var3 = POWDER_SNOW.map();
        var3.put(
            Items.BUCKET,
            (param0, param1, param2, param3, param4, param5) -> fillBucket(
                    param0,
                    param1,
                    param2,
                    param3,
                    param4,
                    param5,
                    new ItemStack(Items.POWDER_SNOW_BUCKET),
                    param0x -> param0x.getValue(LayeredCauldronBlock.LEVEL) == 3,
                    SoundEvents.BUCKET_FILL_POWDER_SNOW
                )
        );
        addDefaultInteractions(var3);
    }

    static void addDefaultInteractions(Map<Item, CauldronInteraction> param0) {
        param0.put(Items.LAVA_BUCKET, FILL_LAVA);
        param0.put(Items.WATER_BUCKET, FILL_WATER);
        param0.put(Items.POWDER_SNOW_BUCKET, FILL_POWDER_SNOW);
    }

    static InteractionResult fillBucket(
        BlockState param0,
        Level param1,
        BlockPos param2,
        Player param3,
        InteractionHand param4,
        ItemStack param5,
        ItemStack param6,
        Predicate<BlockState> param7,
        SoundEvent param8
    ) {
        if (!param7.test(param0)) {
            return InteractionResult.PASS;
        } else {
            if (!param1.isClientSide) {
                Item var0 = param5.getItem();
                param3.setItemInHand(param4, ItemUtils.createFilledResult(param5, param3, param6));
                param3.awardStat(Stats.USE_CAULDRON);
                param3.awardStat(Stats.ITEM_USED.get(var0));
                param1.setBlockAndUpdate(param2, Blocks.CAULDRON.defaultBlockState());
                param1.playSound(null, param2, param8, SoundSource.BLOCKS, 1.0F, 1.0F);
                param1.gameEvent(null, GameEvent.FLUID_PICKUP, param2);
            }

            return InteractionResult.sidedSuccess(param1.isClientSide);
        }
    }

    static InteractionResult emptyBucket(
        Level param0, BlockPos param1, Player param2, InteractionHand param3, ItemStack param4, BlockState param5, SoundEvent param6
    ) {
        if (!param0.isClientSide) {
            Item var0 = param4.getItem();
            param2.setItemInHand(param3, ItemUtils.createFilledResult(param4, param2, new ItemStack(Items.BUCKET)));
            param2.awardStat(Stats.FILL_CAULDRON);
            param2.awardStat(Stats.ITEM_USED.get(var0));
            param0.setBlockAndUpdate(param1, param5);
            param0.playSound(null, param1, param6, SoundSource.BLOCKS, 1.0F, 1.0F);
            param0.gameEvent(null, GameEvent.FLUID_PLACE, param1);
        }

        return InteractionResult.sidedSuccess(param0.isClientSide);
    }

    public static record InteractionMap(String name, Map<Item, CauldronInteraction> map) {
    }
}
