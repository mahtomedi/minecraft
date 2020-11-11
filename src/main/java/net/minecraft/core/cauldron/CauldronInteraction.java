package net.minecraft.core.cauldron;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
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

public interface CauldronInteraction {
    Map<Item, CauldronInteraction> EMPTY = newInteractionMap();
    Map<Item, CauldronInteraction> WATER = newInteractionMap();
    Map<Item, CauldronInteraction> LAVA = newInteractionMap();
    Map<Item, CauldronInteraction> POWDER_SNOW = newInteractionMap();
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
                ItemStack var0 = param5.copy();
                var0.setCount(1);
                BannerBlockEntity.removeLastPattern(var0);
                if (!param3.getAbilities().instabuild) {
                    param5.shrink(1);
                }

                if (param5.isEmpty()) {
                    param3.setItemInHand(param4, var0);
                } else if (param3.getInventory().add(var0)) {
                    ((ServerPlayer)param3).refreshContainer(param3.inventoryMenu);
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

    static Object2ObjectOpenHashMap<Item, CauldronInteraction> newInteractionMap() {
        return Util.make(
            new Object2ObjectOpenHashMap<>(), param0 -> param0.defaultReturnValue((param0x, param1, param2, param3, param4, param5) -> InteractionResult.PASS)
        );
    }

    InteractionResult interact(BlockState var1, Level var2, BlockPos var3, Player var4, InteractionHand var5, ItemStack var6);

    static void bootStrap() {
        EMPTY.put(Items.WATER_BUCKET, FILL_WATER);
        EMPTY.put(Items.LAVA_BUCKET, FILL_LAVA);
        EMPTY.put(Items.POWDER_SNOW_BUCKET, FILL_POWDER_SNOW);
        EMPTY.put(Items.POTION, (param0, param1, param2, param3, param4, param5) -> {
            if (PotionUtils.getPotion(param5) != Potions.WATER) {
                return InteractionResult.PASS;
            } else {
                if (!param1.isClientSide) {
                    param3.setItemInHand(param4, ItemUtils.createFilledResult(param5, param3, new ItemStack(Items.GLASS_BOTTLE)));
                    param3.awardStat(Stats.USE_CAULDRON);
                    param1.setBlockAndUpdate(param2, Blocks.WATER_CAULDRON.defaultBlockState());
                    param1.playSound(null, param2, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                }

                return InteractionResult.sidedSuccess(param1.isClientSide);
            }
        });
        WATER.put(Items.LAVA_BUCKET, FILL_LAVA);
        WATER.put(Items.WATER_BUCKET, FILL_WATER);
        WATER.put(Items.POWDER_SNOW_BUCKET, FILL_POWDER_SNOW);
        WATER.put(
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
        WATER.put(Items.GLASS_BOTTLE, (param0, param1, param2, param3, param4, param5) -> {
            if (!param1.isClientSide) {
                param3.setItemInHand(param4, ItemUtils.createFilledResult(param5, param3, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER)));
                param3.awardStat(Stats.USE_CAULDRON);
                LayeredCauldronBlock.lowerFillLevel(param0, param1, param2);
                param1.playSound(null, param2, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            }

            return InteractionResult.sidedSuccess(param1.isClientSide);
        });
        WATER.put(Items.POTION, (param0, param1, param2, param3, param4, param5) -> {
            if (param0.getValue(LayeredCauldronBlock.LEVEL) != 3 && PotionUtils.getPotion(param5) == Potions.WATER) {
                if (!param1.isClientSide) {
                    param3.setItemInHand(param4, ItemUtils.createFilledResult(param5, param3, new ItemStack(Items.GLASS_BOTTLE)));
                    param3.awardStat(Stats.USE_CAULDRON);
                    param1.setBlockAndUpdate(param2, param0.cycle(LayeredCauldronBlock.LEVEL));
                    param1.playSound(null, param2, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                }

                return InteractionResult.sidedSuccess(param1.isClientSide);
            } else {
                return InteractionResult.PASS;
            }
        });
        WATER.put(Items.LEATHER_BOOTS, DYED_ITEM);
        WATER.put(Items.LEATHER_LEGGINGS, DYED_ITEM);
        WATER.put(Items.LEATHER_CHESTPLATE, DYED_ITEM);
        WATER.put(Items.LEATHER_HELMET, DYED_ITEM);
        WATER.put(Items.LEATHER_HORSE_ARMOR, DYED_ITEM);
        WATER.put(Items.WHITE_BANNER, BANNER);
        WATER.put(Items.GRAY_BANNER, BANNER);
        WATER.put(Items.BLACK_BANNER, BANNER);
        WATER.put(Items.BLUE_BANNER, BANNER);
        WATER.put(Items.BROWN_BANNER, BANNER);
        WATER.put(Items.CYAN_BANNER, BANNER);
        WATER.put(Items.GREEN_BANNER, BANNER);
        WATER.put(Items.LIGHT_BLUE_BANNER, BANNER);
        WATER.put(Items.LIGHT_GRAY_BANNER, BANNER);
        WATER.put(Items.LIME_BANNER, BANNER);
        WATER.put(Items.MAGENTA_BANNER, BANNER);
        WATER.put(Items.ORANGE_BANNER, BANNER);
        WATER.put(Items.PINK_BANNER, BANNER);
        WATER.put(Items.PURPLE_BANNER, BANNER);
        WATER.put(Items.RED_BANNER, BANNER);
        WATER.put(Items.YELLOW_BANNER, BANNER);
        WATER.put(Items.WHITE_SHULKER_BOX, SHULKER_BOX);
        WATER.put(Items.GRAY_SHULKER_BOX, SHULKER_BOX);
        WATER.put(Items.BLACK_SHULKER_BOX, SHULKER_BOX);
        WATER.put(Items.BLUE_SHULKER_BOX, SHULKER_BOX);
        WATER.put(Items.BROWN_SHULKER_BOX, SHULKER_BOX);
        WATER.put(Items.CYAN_SHULKER_BOX, SHULKER_BOX);
        WATER.put(Items.GREEN_SHULKER_BOX, SHULKER_BOX);
        WATER.put(Items.LIGHT_BLUE_SHULKER_BOX, SHULKER_BOX);
        WATER.put(Items.LIGHT_GRAY_SHULKER_BOX, SHULKER_BOX);
        WATER.put(Items.LIME_SHULKER_BOX, SHULKER_BOX);
        WATER.put(Items.MAGENTA_SHULKER_BOX, SHULKER_BOX);
        WATER.put(Items.ORANGE_SHULKER_BOX, SHULKER_BOX);
        WATER.put(Items.PINK_SHULKER_BOX, SHULKER_BOX);
        WATER.put(Items.PURPLE_SHULKER_BOX, SHULKER_BOX);
        WATER.put(Items.RED_SHULKER_BOX, SHULKER_BOX);
        WATER.put(Items.YELLOW_SHULKER_BOX, SHULKER_BOX);
        LAVA.put(
            Items.BUCKET,
            (param0, param1, param2, param3, param4, param5) -> fillBucket(
                    param0, param1, param2, param3, param4, param5, new ItemStack(Items.LAVA_BUCKET), param0x -> true, SoundEvents.BUCKET_FILL_LAVA
                )
        );
        LAVA.put(Items.WATER_BUCKET, FILL_WATER);
        LAVA.put(Items.POWDER_SNOW_BUCKET, FILL_POWDER_SNOW);
        POWDER_SNOW.put(
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
        POWDER_SNOW.put(Items.WATER_BUCKET, FILL_WATER);
        POWDER_SNOW.put(Items.LAVA_BUCKET, FILL_LAVA);
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
                param3.setItemInHand(param4, ItemUtils.createFilledResult(param5, param3, param6));
                param3.awardStat(Stats.USE_CAULDRON);
                param1.setBlockAndUpdate(param2, Blocks.CAULDRON.defaultBlockState());
                param1.playSound(null, param2, param8, SoundSource.BLOCKS, 1.0F, 1.0F);
            }

            return InteractionResult.sidedSuccess(param1.isClientSide);
        }
    }

    static InteractionResult emptyBucket(
        Level param0, BlockPos param1, Player param2, InteractionHand param3, ItemStack param4, BlockState param5, SoundEvent param6
    ) {
        if (!param0.isClientSide) {
            param2.setItemInHand(param3, ItemUtils.createFilledResult(param4, param2, new ItemStack(Items.BUCKET)));
            param2.awardStat(Stats.FILL_CAULDRON);
            param0.setBlockAndUpdate(param1, param5);
            param0.playSound(null, param1, param6, SoundSource.BLOCKS, 1.0F, 1.0F);
        }

        return InteractionResult.sidedSuccess(param0.isClientSide);
    }
}
