package net.minecraft.client.resources;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.Pack;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LegacyResourcePackAdapter implements Pack {
    private final Pack source;
    private final Map<ResourceLocation, ResourceLocation> patches;
    public static final Map<ResourceLocation, ResourceLocation> V3 = Util.make(
        () -> {
            Builder<ResourceLocation, ResourceLocation> var0 = ImmutableMap.builder();
            var0.put(new ResourceLocation("textures/block/melon_stem.png"), new ResourceLocation("textures/block/pumpkin_stem.png"));
            var0.put(new ResourceLocation("textures/block/anvil.png"), new ResourceLocation("textures/blocks/anvil_base.png"));
            var0.put(new ResourceLocation("textures/block/anvil_top.png"), new ResourceLocation("textures/blocks/anvil_top_damaged_0.png"));
            var0.put(new ResourceLocation("textures/block/chipped_anvil_top.png"), new ResourceLocation("textures/blocks/anvil_top_damaged_1.png"));
            var0.put(new ResourceLocation("textures/block/damaged_anvil_top.png"), new ResourceLocation("textures/blocks/anvil_top_damaged_2.png"));
            var0.put(new ResourceLocation("textures/block/beacon.png"), new ResourceLocation("textures/blocks/beacon.png"));
            var0.put(new ResourceLocation("textures/block/bedrock.png"), new ResourceLocation("textures/blocks/bedrock.png"));
            var0.put(new ResourceLocation("textures/block/beetroots_stage0.png"), new ResourceLocation("textures/blocks/beetroots_stage_0.png"));
            var0.put(new ResourceLocation("textures/block/beetroots_stage1.png"), new ResourceLocation("textures/blocks/beetroots_stage_1.png"));
            var0.put(new ResourceLocation("textures/block/beetroots_stage2.png"), new ResourceLocation("textures/blocks/beetroots_stage_2.png"));
            var0.put(new ResourceLocation("textures/block/beetroots_stage3.png"), new ResourceLocation("textures/blocks/beetroots_stage_3.png"));
            var0.put(new ResourceLocation("textures/block/bone_block_side.png"), new ResourceLocation("textures/blocks/bone_block_side.png"));
            var0.put(new ResourceLocation("textures/block/bone_block_top.png"), new ResourceLocation("textures/blocks/bone_block_top.png"));
            var0.put(new ResourceLocation("textures/block/bookshelf.png"), new ResourceLocation("textures/blocks/bookshelf.png"));
            var0.put(new ResourceLocation("textures/block/brewing_stand.png"), new ResourceLocation("textures/blocks/brewing_stand.png"));
            var0.put(new ResourceLocation("textures/block/brewing_stand_base.png"), new ResourceLocation("textures/blocks/brewing_stand_base.png"));
            var0.put(new ResourceLocation("textures/block/bricks.png"), new ResourceLocation("textures/blocks/brick.png"));
            var0.put(new ResourceLocation("textures/block/cactus_bottom.png"), new ResourceLocation("textures/blocks/cactus_bottom.png"));
            var0.put(new ResourceLocation("textures/block/cactus_side.png"), new ResourceLocation("textures/blocks/cactus_side.png"));
            var0.put(new ResourceLocation("textures/block/cactus_top.png"), new ResourceLocation("textures/blocks/cactus_top.png"));
            var0.put(new ResourceLocation("textures/block/cake_bottom.png"), new ResourceLocation("textures/blocks/cake_bottom.png"));
            var0.put(new ResourceLocation("textures/block/cake_inner.png"), new ResourceLocation("textures/blocks/cake_inner.png"));
            var0.put(new ResourceLocation("textures/block/cake_side.png"), new ResourceLocation("textures/blocks/cake_side.png"));
            var0.put(new ResourceLocation("textures/block/cake_top.png"), new ResourceLocation("textures/blocks/cake_top.png"));
            var0.put(new ResourceLocation("textures/block/carrots_stage0.png"), new ResourceLocation("textures/blocks/carrots_stage_0.png"));
            var0.put(new ResourceLocation("textures/block/carrots_stage1.png"), new ResourceLocation("textures/blocks/carrots_stage_1.png"));
            var0.put(new ResourceLocation("textures/block/carrots_stage2.png"), new ResourceLocation("textures/blocks/carrots_stage_2.png"));
            var0.put(new ResourceLocation("textures/block/carrots_stage3.png"), new ResourceLocation("textures/blocks/carrots_stage_3.png"));
            var0.put(new ResourceLocation("textures/block/cauldron_bottom.png"), new ResourceLocation("textures/blocks/cauldron_bottom.png"));
            var0.put(new ResourceLocation("textures/block/cauldron_inner.png"), new ResourceLocation("textures/blocks/cauldron_inner.png"));
            var0.put(new ResourceLocation("textures/block/cauldron_side.png"), new ResourceLocation("textures/blocks/cauldron_side.png"));
            var0.put(new ResourceLocation("textures/block/cauldron_top.png"), new ResourceLocation("textures/blocks/cauldron_top.png"));
            var0.put(new ResourceLocation("textures/block/chain_command_block_back.png"), new ResourceLocation("textures/blocks/chain_command_block_back.png"));
            var0.put(
                new ResourceLocation("textures/block/chain_command_block_conditional.png"),
                new ResourceLocation("textures/blocks/chain_command_block_conditional.png")
            );
            var0.put(
                new ResourceLocation("textures/block/chain_command_block_front.png"), new ResourceLocation("textures/blocks/chain_command_block_front.png")
            );
            var0.put(new ResourceLocation("textures/block/chain_command_block_side.png"), new ResourceLocation("textures/blocks/chain_command_block_side.png"));
            var0.put(new ResourceLocation("textures/block/chorus_flower.png"), new ResourceLocation("textures/blocks/chorus_flower.png"));
            var0.put(new ResourceLocation("textures/block/chorus_flower_dead.png"), new ResourceLocation("textures/blocks/chorus_flower_dead.png"));
            var0.put(new ResourceLocation("textures/block/chorus_plant.png"), new ResourceLocation("textures/blocks/chorus_plant.png"));
            var0.put(new ResourceLocation("textures/block/clay.png"), new ResourceLocation("textures/blocks/clay.png"));
            var0.put(new ResourceLocation("textures/block/coal_block.png"), new ResourceLocation("textures/blocks/coal_block.png"));
            var0.put(new ResourceLocation("textures/block/coal_ore.png"), new ResourceLocation("textures/blocks/coal_ore.png"));
            var0.put(new ResourceLocation("textures/block/coarse_dirt.png"), new ResourceLocation("textures/blocks/coarse_dirt.png"));
            var0.put(new ResourceLocation("textures/block/cobblestone.png"), new ResourceLocation("textures/blocks/cobblestone.png"));
            var0.put(new ResourceLocation("textures/block/mossy_cobblestone.png"), new ResourceLocation("textures/blocks/cobblestone_mossy.png"));
            var0.put(new ResourceLocation("textures/block/cocoa_stage0.png"), new ResourceLocation("textures/blocks/cocoa_stage_0.png"));
            var0.put(new ResourceLocation("textures/block/cocoa_stage1.png"), new ResourceLocation("textures/blocks/cocoa_stage_1.png"));
            var0.put(new ResourceLocation("textures/block/cocoa_stage2.png"), new ResourceLocation("textures/blocks/cocoa_stage_2.png"));
            var0.put(new ResourceLocation("textures/block/command_block_back.png"), new ResourceLocation("textures/blocks/command_block_back.png"));
            var0.put(
                new ResourceLocation("textures/block/command_block_conditional.png"), new ResourceLocation("textures/blocks/command_block_conditional.png")
            );
            var0.put(new ResourceLocation("textures/block/command_block_front.png"), new ResourceLocation("textures/blocks/command_block_front.png"));
            var0.put(new ResourceLocation("textures/block/command_block_side.png"), new ResourceLocation("textures/blocks/command_block_side.png"));
            var0.put(new ResourceLocation("textures/block/comparator.png"), new ResourceLocation("textures/blocks/comparator_off.png"));
            var0.put(new ResourceLocation("textures/block/comparator_on.png"), new ResourceLocation("textures/blocks/comparator_on.png"));
            var0.put(new ResourceLocation("textures/block/black_concrete.png"), new ResourceLocation("textures/blocks/concrete_black.png"));
            var0.put(new ResourceLocation("textures/block/blue_concrete.png"), new ResourceLocation("textures/blocks/concrete_blue.png"));
            var0.put(new ResourceLocation("textures/block/brown_concrete.png"), new ResourceLocation("textures/blocks/concrete_brown.png"));
            var0.put(new ResourceLocation("textures/block/cyan_concrete.png"), new ResourceLocation("textures/blocks/concrete_cyan.png"));
            var0.put(new ResourceLocation("textures/block/gray_concrete.png"), new ResourceLocation("textures/blocks/concrete_gray.png"));
            var0.put(new ResourceLocation("textures/block/green_concrete.png"), new ResourceLocation("textures/blocks/concrete_green.png"));
            var0.put(new ResourceLocation("textures/block/light_blue_concrete.png"), new ResourceLocation("textures/blocks/concrete_light_blue.png"));
            var0.put(new ResourceLocation("textures/block/lime_concrete.png"), new ResourceLocation("textures/blocks/concrete_lime.png"));
            var0.put(new ResourceLocation("textures/block/magenta_concrete.png"), new ResourceLocation("textures/blocks/concrete_magenta.png"));
            var0.put(new ResourceLocation("textures/block/orange_concrete.png"), new ResourceLocation("textures/blocks/concrete_orange.png"));
            var0.put(new ResourceLocation("textures/block/pink_concrete.png"), new ResourceLocation("textures/blocks/concrete_pink.png"));
            var0.put(new ResourceLocation("textures/block/black_concrete_powder.png"), new ResourceLocation("textures/blocks/concrete_powder_black.png"));
            var0.put(new ResourceLocation("textures/block/blue_concrete_powder.png"), new ResourceLocation("textures/blocks/concrete_powder_blue.png"));
            var0.put(new ResourceLocation("textures/block/brown_concrete_powder.png"), new ResourceLocation("textures/blocks/concrete_powder_brown.png"));
            var0.put(new ResourceLocation("textures/block/cyan_concrete_powder.png"), new ResourceLocation("textures/blocks/concrete_powder_cyan.png"));
            var0.put(new ResourceLocation("textures/block/gray_concrete_powder.png"), new ResourceLocation("textures/blocks/concrete_powder_gray.png"));
            var0.put(new ResourceLocation("textures/block/green_concrete_powder.png"), new ResourceLocation("textures/blocks/concrete_powder_green.png"));
            var0.put(
                new ResourceLocation("textures/block/light_blue_concrete_powder.png"), new ResourceLocation("textures/blocks/concrete_powder_light_blue.png")
            );
            var0.put(new ResourceLocation("textures/block/lime_concrete_powder.png"), new ResourceLocation("textures/blocks/concrete_powder_lime.png"));
            var0.put(new ResourceLocation("textures/block/magenta_concrete_powder.png"), new ResourceLocation("textures/blocks/concrete_powder_magenta.png"));
            var0.put(new ResourceLocation("textures/block/orange_concrete_powder.png"), new ResourceLocation("textures/blocks/concrete_powder_orange.png"));
            var0.put(new ResourceLocation("textures/block/pink_concrete_powder.png"), new ResourceLocation("textures/blocks/concrete_powder_pink.png"));
            var0.put(new ResourceLocation("textures/block/purple_concrete_powder.png"), new ResourceLocation("textures/blocks/concrete_powder_purple.png"));
            var0.put(new ResourceLocation("textures/block/red_concrete_powder.png"), new ResourceLocation("textures/blocks/concrete_powder_red.png"));
            var0.put(new ResourceLocation("textures/block/light_gray_concrete_powder.png"), new ResourceLocation("textures/blocks/concrete_powder_silver.png"));
            var0.put(new ResourceLocation("textures/block/white_concrete_powder.png"), new ResourceLocation("textures/blocks/concrete_powder_white.png"));
            var0.put(new ResourceLocation("textures/block/yellow_concrete_powder.png"), new ResourceLocation("textures/blocks/concrete_powder_yellow.png"));
            var0.put(new ResourceLocation("textures/block/purple_concrete.png"), new ResourceLocation("textures/blocks/concrete_purple.png"));
            var0.put(new ResourceLocation("textures/block/red_concrete.png"), new ResourceLocation("textures/blocks/concrete_red.png"));
            var0.put(new ResourceLocation("textures/block/light_gray_concrete.png"), new ResourceLocation("textures/blocks/concrete_silver.png"));
            var0.put(new ResourceLocation("textures/block/white_concrete.png"), new ResourceLocation("textures/blocks/concrete_white.png"));
            var0.put(new ResourceLocation("textures/block/yellow_concrete.png"), new ResourceLocation("textures/blocks/concrete_yellow.png"));
            var0.put(new ResourceLocation("textures/block/crafting_table_front.png"), new ResourceLocation("textures/blocks/crafting_table_front.png"));
            var0.put(new ResourceLocation("textures/block/crafting_table_side.png"), new ResourceLocation("textures/blocks/crafting_table_side.png"));
            var0.put(new ResourceLocation("textures/block/crafting_table_top.png"), new ResourceLocation("textures/blocks/crafting_table_top.png"));
            var0.put(
                new ResourceLocation("textures/block/daylight_detector_inverted_top.png"),
                new ResourceLocation("textures/blocks/daylight_detector_inverted_top.png")
            );
            var0.put(new ResourceLocation("textures/block/daylight_detector_side.png"), new ResourceLocation("textures/blocks/daylight_detector_side.png"));
            var0.put(new ResourceLocation("textures/block/daylight_detector_top.png"), new ResourceLocation("textures/blocks/daylight_detector_top.png"));
            var0.put(new ResourceLocation("textures/block/dead_bush.png"), new ResourceLocation("textures/blocks/deadbush.png"));
            var0.put(new ResourceLocation("textures/block/debug.png"), new ResourceLocation("textures/blocks/debug.png"));
            var0.put(new ResourceLocation("textures/block/debug2.png"), new ResourceLocation("textures/blocks/debug2.png"));
            var0.put(new ResourceLocation("textures/block/destroy_stage_0.png"), new ResourceLocation("textures/blocks/destroy_stage_0.png"));
            var0.put(new ResourceLocation("textures/block/destroy_stage_1.png"), new ResourceLocation("textures/blocks/destroy_stage_1.png"));
            var0.put(new ResourceLocation("textures/block/destroy_stage_2.png"), new ResourceLocation("textures/blocks/destroy_stage_2.png"));
            var0.put(new ResourceLocation("textures/block/destroy_stage_3.png"), new ResourceLocation("textures/blocks/destroy_stage_3.png"));
            var0.put(new ResourceLocation("textures/block/destroy_stage_4.png"), new ResourceLocation("textures/blocks/destroy_stage_4.png"));
            var0.put(new ResourceLocation("textures/block/destroy_stage_5.png"), new ResourceLocation("textures/blocks/destroy_stage_5.png"));
            var0.put(new ResourceLocation("textures/block/destroy_stage_6.png"), new ResourceLocation("textures/blocks/destroy_stage_6.png"));
            var0.put(new ResourceLocation("textures/block/destroy_stage_7.png"), new ResourceLocation("textures/blocks/destroy_stage_7.png"));
            var0.put(new ResourceLocation("textures/block/destroy_stage_8.png"), new ResourceLocation("textures/blocks/destroy_stage_8.png"));
            var0.put(new ResourceLocation("textures/block/destroy_stage_9.png"), new ResourceLocation("textures/blocks/destroy_stage_9.png"));
            var0.put(new ResourceLocation("textures/block/diamond_block.png"), new ResourceLocation("textures/blocks/diamond_block.png"));
            var0.put(new ResourceLocation("textures/block/diamond_ore.png"), new ResourceLocation("textures/blocks/diamond_ore.png"));
            var0.put(new ResourceLocation("textures/block/dirt.png"), new ResourceLocation("textures/blocks/dirt.png"));
            var0.put(new ResourceLocation("textures/block/podzol_side.png"), new ResourceLocation("textures/blocks/dirt_podzol_side.png"));
            var0.put(new ResourceLocation("textures/block/podzol_top.png"), new ResourceLocation("textures/blocks/dirt_podzol_top.png"));
            var0.put(new ResourceLocation("textures/block/dispenser_front.png"), new ResourceLocation("textures/blocks/dispenser_front_horizontal.png"));
            var0.put(new ResourceLocation("textures/block/dispenser_front_vertical.png"), new ResourceLocation("textures/blocks/dispenser_front_vertical.png"));
            var0.put(new ResourceLocation("textures/block/acacia_door_bottom.png"), new ResourceLocation("textures/blocks/door_acacia_lower.png"));
            var0.put(new ResourceLocation("textures/block/acacia_door_top.png"), new ResourceLocation("textures/blocks/door_acacia_upper.png"));
            var0.put(new ResourceLocation("textures/block/birch_door_bottom.png"), new ResourceLocation("textures/blocks/door_birch_lower.png"));
            var0.put(new ResourceLocation("textures/block/birch_door_top.png"), new ResourceLocation("textures/blocks/door_birch_upper.png"));
            var0.put(new ResourceLocation("textures/block/dark_oak_door_bottom.png"), new ResourceLocation("textures/blocks/door_dark_oak_lower.png"));
            var0.put(new ResourceLocation("textures/block/dark_oak_door_top.png"), new ResourceLocation("textures/blocks/door_dark_oak_upper.png"));
            var0.put(new ResourceLocation("textures/block/iron_door_bottom.png"), new ResourceLocation("textures/blocks/door_iron_lower.png"));
            var0.put(new ResourceLocation("textures/block/iron_door_top.png"), new ResourceLocation("textures/blocks/door_iron_upper.png"));
            var0.put(new ResourceLocation("textures/block/jungle_door_bottom.png"), new ResourceLocation("textures/blocks/door_jungle_lower.png"));
            var0.put(new ResourceLocation("textures/block/jungle_door_top.png"), new ResourceLocation("textures/blocks/door_jungle_upper.png"));
            var0.put(new ResourceLocation("textures/block/spruce_door_bottom.png"), new ResourceLocation("textures/blocks/door_spruce_lower.png"));
            var0.put(new ResourceLocation("textures/block/spruce_door_top.png"), new ResourceLocation("textures/blocks/door_spruce_upper.png"));
            var0.put(new ResourceLocation("textures/block/oak_door_bottom.png"), new ResourceLocation("textures/blocks/door_wood_lower.png"));
            var0.put(new ResourceLocation("textures/block/oak_door_top.png"), new ResourceLocation("textures/blocks/door_wood_upper.png"));
            var0.put(new ResourceLocation("textures/block/large_fern_bottom.png"), new ResourceLocation("textures/blocks/double_plant_fern_bottom.png"));
            var0.put(new ResourceLocation("textures/block/large_fern_top.png"), new ResourceLocation("textures/blocks/double_plant_fern_top.png"));
            var0.put(new ResourceLocation("textures/block/tall_grass_bottom.png"), new ResourceLocation("textures/blocks/double_plant_grass_bottom.png"));
            var0.put(new ResourceLocation("textures/block/tall_grass_top.png"), new ResourceLocation("textures/blocks/double_plant_grass_top.png"));
            var0.put(new ResourceLocation("textures/block/peony_bottom.png"), new ResourceLocation("textures/blocks/double_plant_paeonia_bottom.png"));
            var0.put(new ResourceLocation("textures/block/peony_top.png"), new ResourceLocation("textures/blocks/double_plant_paeonia_top.png"));
            var0.put(new ResourceLocation("textures/block/rose_bush_bottom.png"), new ResourceLocation("textures/blocks/double_plant_rose_bottom.png"));
            var0.put(new ResourceLocation("textures/block/rose_bush_top.png"), new ResourceLocation("textures/blocks/double_plant_rose_top.png"));
            var0.put(new ResourceLocation("textures/block/sunflower_back.png"), new ResourceLocation("textures/blocks/double_plant_sunflower_back.png"));
            var0.put(new ResourceLocation("textures/block/sunflower_bottom.png"), new ResourceLocation("textures/blocks/double_plant_sunflower_bottom.png"));
            var0.put(new ResourceLocation("textures/block/sunflower_front.png"), new ResourceLocation("textures/blocks/double_plant_sunflower_front.png"));
            var0.put(new ResourceLocation("textures/block/sunflower_top.png"), new ResourceLocation("textures/blocks/double_plant_sunflower_top.png"));
            var0.put(new ResourceLocation("textures/block/lilac_bottom.png"), new ResourceLocation("textures/blocks/double_plant_syringa_bottom.png"));
            var0.put(new ResourceLocation("textures/block/lilac_top.png"), new ResourceLocation("textures/blocks/double_plant_syringa_top.png"));
            var0.put(new ResourceLocation("textures/block/dragon_egg.png"), new ResourceLocation("textures/blocks/dragon_egg.png"));
            var0.put(new ResourceLocation("textures/block/dropper_front.png"), new ResourceLocation("textures/blocks/dropper_front_horizontal.png"));
            var0.put(new ResourceLocation("textures/block/dropper_front_vertical.png"), new ResourceLocation("textures/blocks/dropper_front_vertical.png"));
            var0.put(new ResourceLocation("textures/block/emerald_block.png"), new ResourceLocation("textures/blocks/emerald_block.png"));
            var0.put(new ResourceLocation("textures/block/emerald_ore.png"), new ResourceLocation("textures/blocks/emerald_ore.png"));
            var0.put(new ResourceLocation("textures/block/enchanting_table_bottom.png"), new ResourceLocation("textures/blocks/enchanting_table_bottom.png"));
            var0.put(new ResourceLocation("textures/block/enchanting_table_side.png"), new ResourceLocation("textures/blocks/enchanting_table_side.png"));
            var0.put(new ResourceLocation("textures/block/enchanting_table_top.png"), new ResourceLocation("textures/blocks/enchanting_table_top.png"));
            var0.put(new ResourceLocation("textures/block/end_stone_bricks.png"), new ResourceLocation("textures/blocks/end_bricks.png"));
            var0.put(new ResourceLocation("textures/block/end_rod.png"), new ResourceLocation("textures/blocks/end_rod.png"));
            var0.put(new ResourceLocation("textures/block/end_stone.png"), new ResourceLocation("textures/blocks/end_stone.png"));
            var0.put(new ResourceLocation("textures/block/end_portal_frame_eye.png"), new ResourceLocation("textures/blocks/endframe_eye.png"));
            var0.put(new ResourceLocation("textures/block/end_portal_frame_side.png"), new ResourceLocation("textures/blocks/endframe_side.png"));
            var0.put(new ResourceLocation("textures/block/end_portal_frame_top.png"), new ResourceLocation("textures/blocks/endframe_top.png"));
            var0.put(new ResourceLocation("textures/block/farmland.png"), new ResourceLocation("textures/blocks/farmland_dry.png"));
            var0.put(new ResourceLocation("textures/block/farmland_moist.png"), new ResourceLocation("textures/blocks/farmland_wet.png"));
            var0.put(new ResourceLocation("textures/block/fern.png"), new ResourceLocation("textures/blocks/fern.png"));
            var0.put(new ResourceLocation("textures/block/fire_0.png"), new ResourceLocation("textures/blocks/fire_layer_0.png"));
            var0.put(new ResourceLocation("textures/block/fire_1.png"), new ResourceLocation("textures/blocks/fire_layer_1.png"));
            var0.put(new ResourceLocation("textures/block/allium.png"), new ResourceLocation("textures/blocks/flower_allium.png"));
            var0.put(new ResourceLocation("textures/block/blue_orchid.png"), new ResourceLocation("textures/blocks/flower_blue_orchid.png"));
            var0.put(new ResourceLocation("textures/block/dandelion.png"), new ResourceLocation("textures/blocks/flower_dandelion.png"));
            var0.put(new ResourceLocation("textures/block/azure_bluet.png"), new ResourceLocation("textures/blocks/flower_houstonia.png"));
            var0.put(new ResourceLocation("textures/block/oxeye_daisy.png"), new ResourceLocation("textures/blocks/flower_oxeye_daisy.png"));
            var0.put(new ResourceLocation("textures/block/flower_pot.png"), new ResourceLocation("textures/blocks/flower_pot.png"));
            var0.put(new ResourceLocation("textures/block/poppy.png"), new ResourceLocation("textures/blocks/flower_rose.png"));
            var0.put(new ResourceLocation("textures/block/orange_tulip.png"), new ResourceLocation("textures/blocks/flower_tulip_orange.png"));
            var0.put(new ResourceLocation("textures/block/pink_tulip.png"), new ResourceLocation("textures/blocks/flower_tulip_pink.png"));
            var0.put(new ResourceLocation("textures/block/red_tulip.png"), new ResourceLocation("textures/blocks/flower_tulip_red.png"));
            var0.put(new ResourceLocation("textures/block/white_tulip.png"), new ResourceLocation("textures/blocks/flower_tulip_white.png"));
            var0.put(new ResourceLocation("textures/block/frosted_ice_0.png"), new ResourceLocation("textures/blocks/frosted_ice_0.png"));
            var0.put(new ResourceLocation("textures/block/frosted_ice_1.png"), new ResourceLocation("textures/blocks/frosted_ice_1.png"));
            var0.put(new ResourceLocation("textures/block/frosted_ice_2.png"), new ResourceLocation("textures/blocks/frosted_ice_2.png"));
            var0.put(new ResourceLocation("textures/block/frosted_ice_3.png"), new ResourceLocation("textures/blocks/frosted_ice_3.png"));
            var0.put(new ResourceLocation("textures/block/furnace_front.png"), new ResourceLocation("textures/blocks/furnace_front_off.png"));
            var0.put(new ResourceLocation("textures/block/furnace_front_on.png"), new ResourceLocation("textures/blocks/furnace_front_on.png"));
            var0.put(new ResourceLocation("textures/block/furnace_side.png"), new ResourceLocation("textures/blocks/furnace_side.png"));
            var0.put(new ResourceLocation("textures/block/furnace_top.png"), new ResourceLocation("textures/blocks/furnace_top.png"));
            var0.put(new ResourceLocation("textures/block/glass.png"), new ResourceLocation("textures/blocks/glass.png"));
            var0.put(new ResourceLocation("textures/block/black_stained_glass.png"), new ResourceLocation("textures/blocks/glass_black.png"));
            var0.put(new ResourceLocation("textures/block/blue_stained_glass.png"), new ResourceLocation("textures/blocks/glass_blue.png"));
            var0.put(new ResourceLocation("textures/block/brown_stained_glass.png"), new ResourceLocation("textures/blocks/glass_brown.png"));
            var0.put(new ResourceLocation("textures/block/cyan_stained_glass.png"), new ResourceLocation("textures/blocks/glass_cyan.png"));
            var0.put(new ResourceLocation("textures/block/gray_stained_glass.png"), new ResourceLocation("textures/blocks/glass_gray.png"));
            var0.put(new ResourceLocation("textures/block/green_stained_glass.png"), new ResourceLocation("textures/blocks/glass_green.png"));
            var0.put(new ResourceLocation("textures/block/light_blue_stained_glass.png"), new ResourceLocation("textures/blocks/glass_light_blue.png"));
            var0.put(new ResourceLocation("textures/block/lime_stained_glass.png"), new ResourceLocation("textures/blocks/glass_lime.png"));
            var0.put(new ResourceLocation("textures/block/magenta_stained_glass.png"), new ResourceLocation("textures/blocks/glass_magenta.png"));
            var0.put(new ResourceLocation("textures/block/orange_stained_glass.png"), new ResourceLocation("textures/blocks/glass_orange.png"));
            var0.put(new ResourceLocation("textures/block/glass_pane_top.png"), new ResourceLocation("textures/blocks/glass_pane_top.png"));
            var0.put(new ResourceLocation("textures/block/black_stained_glass_pane_top.png"), new ResourceLocation("textures/blocks/glass_pane_top_black.png"));
            var0.put(new ResourceLocation("textures/block/blue_stained_glass_pane_top.png"), new ResourceLocation("textures/blocks/glass_pane_top_blue.png"));
            var0.put(new ResourceLocation("textures/block/brown_stained_glass_pane_top.png"), new ResourceLocation("textures/blocks/glass_pane_top_brown.png"));
            var0.put(new ResourceLocation("textures/block/cyan_stained_glass_pane_top.png"), new ResourceLocation("textures/blocks/glass_pane_top_cyan.png"));
            var0.put(new ResourceLocation("textures/block/gray_stained_glass_pane_top.png"), new ResourceLocation("textures/blocks/glass_pane_top_gray.png"));
            var0.put(new ResourceLocation("textures/block/green_stained_glass_pane_top.png"), new ResourceLocation("textures/blocks/glass_pane_top_green.png"));
            var0.put(
                new ResourceLocation("textures/block/light_blue_stained_glass_pane_top.png"),
                new ResourceLocation("textures/blocks/glass_pane_top_light_blue.png")
            );
            var0.put(new ResourceLocation("textures/block/lime_stained_glass_pane_top.png"), new ResourceLocation("textures/blocks/glass_pane_top_lime.png"));
            var0.put(
                new ResourceLocation("textures/block/magenta_stained_glass_pane_top.png"), new ResourceLocation("textures/blocks/glass_pane_top_magenta.png")
            );
            var0.put(
                new ResourceLocation("textures/block/orange_stained_glass_pane_top.png"), new ResourceLocation("textures/blocks/glass_pane_top_orange.png")
            );
            var0.put(new ResourceLocation("textures/block/pink_stained_glass_pane_top.png"), new ResourceLocation("textures/blocks/glass_pane_top_pink.png"));
            var0.put(
                new ResourceLocation("textures/block/purple_stained_glass_pane_top.png"), new ResourceLocation("textures/blocks/glass_pane_top_purple.png")
            );
            var0.put(new ResourceLocation("textures/block/red_stained_glass_pane_top.png"), new ResourceLocation("textures/blocks/glass_pane_top_red.png"));
            var0.put(
                new ResourceLocation("textures/block/light_gray_stained_glass_pane_top.png"), new ResourceLocation("textures/blocks/glass_pane_top_silver.png")
            );
            var0.put(new ResourceLocation("textures/block/white_stained_glass_pane_top.png"), new ResourceLocation("textures/blocks/glass_pane_top_white.png"));
            var0.put(
                new ResourceLocation("textures/block/yellow_stained_glass_pane_top.png"), new ResourceLocation("textures/blocks/glass_pane_top_yellow.png")
            );
            var0.put(new ResourceLocation("textures/block/pink_stained_glass.png"), new ResourceLocation("textures/blocks/glass_pink.png"));
            var0.put(new ResourceLocation("textures/block/purple_stained_glass.png"), new ResourceLocation("textures/blocks/glass_purple.png"));
            var0.put(new ResourceLocation("textures/block/red_stained_glass.png"), new ResourceLocation("textures/blocks/glass_red.png"));
            var0.put(new ResourceLocation("textures/block/light_gray_stained_glass.png"), new ResourceLocation("textures/blocks/glass_silver.png"));
            var0.put(new ResourceLocation("textures/block/white_stained_glass.png"), new ResourceLocation("textures/blocks/glass_white.png"));
            var0.put(new ResourceLocation("textures/block/yellow_stained_glass.png"), new ResourceLocation("textures/blocks/glass_yellow.png"));
            var0.put(new ResourceLocation("textures/block/black_glazed_terracotta.png"), new ResourceLocation("textures/blocks/glazed_terracotta_black.png"));
            var0.put(new ResourceLocation("textures/block/blue_glazed_terracotta.png"), new ResourceLocation("textures/blocks/glazed_terracotta_blue.png"));
            var0.put(new ResourceLocation("textures/block/brown_glazed_terracotta.png"), new ResourceLocation("textures/blocks/glazed_terracotta_brown.png"));
            var0.put(new ResourceLocation("textures/block/cyan_glazed_terracotta.png"), new ResourceLocation("textures/blocks/glazed_terracotta_cyan.png"));
            var0.put(new ResourceLocation("textures/block/gray_glazed_terracotta.png"), new ResourceLocation("textures/blocks/glazed_terracotta_gray.png"));
            var0.put(new ResourceLocation("textures/block/green_glazed_terracotta.png"), new ResourceLocation("textures/blocks/glazed_terracotta_green.png"));
            var0.put(
                new ResourceLocation("textures/block/light_blue_glazed_terracotta.png"),
                new ResourceLocation("textures/blocks/glazed_terracotta_light_blue.png")
            );
            var0.put(new ResourceLocation("textures/block/lime_glazed_terracotta.png"), new ResourceLocation("textures/blocks/glazed_terracotta_lime.png"));
            var0.put(
                new ResourceLocation("textures/block/magenta_glazed_terracotta.png"), new ResourceLocation("textures/blocks/glazed_terracotta_magenta.png")
            );
            var0.put(new ResourceLocation("textures/block/orange_glazed_terracotta.png"), new ResourceLocation("textures/blocks/glazed_terracotta_orange.png"));
            var0.put(new ResourceLocation("textures/block/pink_glazed_terracotta.png"), new ResourceLocation("textures/blocks/glazed_terracotta_pink.png"));
            var0.put(new ResourceLocation("textures/block/purple_glazed_terracotta.png"), new ResourceLocation("textures/blocks/glazed_terracotta_purple.png"));
            var0.put(new ResourceLocation("textures/block/red_glazed_terracotta.png"), new ResourceLocation("textures/blocks/glazed_terracotta_red.png"));
            var0.put(
                new ResourceLocation("textures/block/light_gray_glazed_terracotta.png"), new ResourceLocation("textures/blocks/glazed_terracotta_silver.png")
            );
            var0.put(new ResourceLocation("textures/block/white_glazed_terracotta.png"), new ResourceLocation("textures/blocks/glazed_terracotta_white.png"));
            var0.put(new ResourceLocation("textures/block/yellow_glazed_terracotta.png"), new ResourceLocation("textures/blocks/glazed_terracotta_yellow.png"));
            var0.put(new ResourceLocation("textures/block/glowstone.png"), new ResourceLocation("textures/blocks/glowstone.png"));
            var0.put(new ResourceLocation("textures/block/gold_block.png"), new ResourceLocation("textures/blocks/gold_block.png"));
            var0.put(new ResourceLocation("textures/block/gold_ore.png"), new ResourceLocation("textures/blocks/gold_ore.png"));
            var0.put(new ResourceLocation("textures/block/grass_path_side.png"), new ResourceLocation("textures/blocks/grass_path_side.png"));
            var0.put(new ResourceLocation("textures/block/grass_path_top.png"), new ResourceLocation("textures/blocks/grass_path_top.png"));
            var0.put(new ResourceLocation("textures/block/grass_block_side.png"), new ResourceLocation("textures/blocks/grass_side.png"));
            var0.put(new ResourceLocation("textures/block/grass_block_side_overlay.png"), new ResourceLocation("textures/blocks/grass_side_overlay.png"));
            var0.put(new ResourceLocation("textures/block/grass_block_snow.png"), new ResourceLocation("textures/blocks/grass_side_snowed.png"));
            var0.put(new ResourceLocation("textures/block/grass_block_top.png"), new ResourceLocation("textures/blocks/grass_top.png"));
            var0.put(new ResourceLocation("textures/block/gravel.png"), new ResourceLocation("textures/blocks/gravel.png"));
            var0.put(new ResourceLocation("textures/block/terracotta.png"), new ResourceLocation("textures/blocks/hardened_clay.png"));
            var0.put(new ResourceLocation("textures/block/black_terracotta.png"), new ResourceLocation("textures/blocks/hardened_clay_stained_black.png"));
            var0.put(new ResourceLocation("textures/block/blue_terracotta.png"), new ResourceLocation("textures/blocks/hardened_clay_stained_blue.png"));
            var0.put(new ResourceLocation("textures/block/brown_terracotta.png"), new ResourceLocation("textures/blocks/hardened_clay_stained_brown.png"));
            var0.put(new ResourceLocation("textures/block/cyan_terracotta.png"), new ResourceLocation("textures/blocks/hardened_clay_stained_cyan.png"));
            var0.put(new ResourceLocation("textures/block/gray_terracotta.png"), new ResourceLocation("textures/blocks/hardened_clay_stained_gray.png"));
            var0.put(new ResourceLocation("textures/block/green_terracotta.png"), new ResourceLocation("textures/blocks/hardened_clay_stained_green.png"));
            var0.put(
                new ResourceLocation("textures/block/light_blue_terracotta.png"), new ResourceLocation("textures/blocks/hardened_clay_stained_light_blue.png")
            );
            var0.put(new ResourceLocation("textures/block/lime_terracotta.png"), new ResourceLocation("textures/blocks/hardened_clay_stained_lime.png"));
            var0.put(new ResourceLocation("textures/block/magenta_terracotta.png"), new ResourceLocation("textures/blocks/hardened_clay_stained_magenta.png"));
            var0.put(new ResourceLocation("textures/block/orange_terracotta.png"), new ResourceLocation("textures/blocks/hardened_clay_stained_orange.png"));
            var0.put(new ResourceLocation("textures/block/pink_terracotta.png"), new ResourceLocation("textures/blocks/hardened_clay_stained_pink.png"));
            var0.put(new ResourceLocation("textures/block/purple_terracotta.png"), new ResourceLocation("textures/blocks/hardened_clay_stained_purple.png"));
            var0.put(new ResourceLocation("textures/block/red_terracotta.png"), new ResourceLocation("textures/blocks/hardened_clay_stained_red.png"));
            var0.put(new ResourceLocation("textures/block/light_gray_terracotta.png"), new ResourceLocation("textures/blocks/hardened_clay_stained_silver.png"));
            var0.put(new ResourceLocation("textures/block/white_terracotta.png"), new ResourceLocation("textures/blocks/hardened_clay_stained_white.png"));
            var0.put(new ResourceLocation("textures/block/yellow_terracotta.png"), new ResourceLocation("textures/blocks/hardened_clay_stained_yellow.png"));
            var0.put(new ResourceLocation("textures/block/hay_block_side.png"), new ResourceLocation("textures/blocks/hay_block_side.png"));
            var0.put(new ResourceLocation("textures/block/hay_block_top.png"), new ResourceLocation("textures/blocks/hay_block_top.png"));
            var0.put(new ResourceLocation("textures/block/hopper_inside.png"), new ResourceLocation("textures/blocks/hopper_inside.png"));
            var0.put(new ResourceLocation("textures/block/hopper_outside.png"), new ResourceLocation("textures/blocks/hopper_outside.png"));
            var0.put(new ResourceLocation("textures/block/hopper_top.png"), new ResourceLocation("textures/blocks/hopper_top.png"));
            var0.put(new ResourceLocation("textures/block/ice.png"), new ResourceLocation("textures/blocks/ice.png"));
            var0.put(new ResourceLocation("textures/block/packed_ice.png"), new ResourceLocation("textures/blocks/ice_packed.png"));
            var0.put(new ResourceLocation("textures/block/iron_bars.png"), new ResourceLocation("textures/blocks/iron_bars.png"));
            var0.put(new ResourceLocation("textures/block/iron_block.png"), new ResourceLocation("textures/blocks/iron_block.png"));
            var0.put(new ResourceLocation("textures/block/iron_ore.png"), new ResourceLocation("textures/blocks/iron_ore.png"));
            var0.put(new ResourceLocation("textures/block/iron_trapdoor.png"), new ResourceLocation("textures/blocks/iron_trapdoor.png"));
            var0.put(new ResourceLocation("textures/block/item_frame.png"), new ResourceLocation("textures/blocks/itemframe_background.png"));
            var0.put(new ResourceLocation("textures/block/jukebox_side.png"), new ResourceLocation("textures/blocks/jukebox_side.png"));
            var0.put(new ResourceLocation("textures/block/jukebox_top.png"), new ResourceLocation("textures/blocks/jukebox_top.png"));
            var0.put(new ResourceLocation("textures/block/ladder.png"), new ResourceLocation("textures/blocks/ladder.png"));
            var0.put(new ResourceLocation("textures/block/lapis_block.png"), new ResourceLocation("textures/blocks/lapis_block.png"));
            var0.put(new ResourceLocation("textures/block/lapis_ore.png"), new ResourceLocation("textures/blocks/lapis_ore.png"));
            var0.put(new ResourceLocation("textures/block/lava_flow.png"), new ResourceLocation("textures/blocks/lava_flow.png"));
            var0.put(new ResourceLocation("textures/block/lava_still.png"), new ResourceLocation("textures/blocks/lava_still.png"));
            var0.put(new ResourceLocation("textures/block/acacia_leaves.png"), new ResourceLocation("textures/blocks/leaves_acacia.png"));
            var0.put(new ResourceLocation("textures/block/dark_oak_leaves.png"), new ResourceLocation("textures/blocks/leaves_big_oak.png"));
            var0.put(new ResourceLocation("textures/block/birch_leaves.png"), new ResourceLocation("textures/blocks/leaves_birch.png"));
            var0.put(new ResourceLocation("textures/block/jungle_leaves.png"), new ResourceLocation("textures/blocks/leaves_jungle.png"));
            var0.put(new ResourceLocation("textures/block/oak_leaves.png"), new ResourceLocation("textures/blocks/leaves_oak.png"));
            var0.put(new ResourceLocation("textures/block/spruce_leaves.png"), new ResourceLocation("textures/blocks/leaves_spruce.png"));
            var0.put(new ResourceLocation("textures/block/lever.png"), new ResourceLocation("textures/blocks/lever.png"));
            var0.put(new ResourceLocation("textures/block/acacia_log.png"), new ResourceLocation("textures/blocks/log_acacia.png"));
            var0.put(new ResourceLocation("textures/block/acacia_log_top.png"), new ResourceLocation("textures/blocks/log_acacia_top.png"));
            var0.put(new ResourceLocation("textures/block/dark_oak_log.png"), new ResourceLocation("textures/blocks/log_big_oak.png"));
            var0.put(new ResourceLocation("textures/block/dark_oak_log_top.png"), new ResourceLocation("textures/blocks/log_big_oak_top.png"));
            var0.put(new ResourceLocation("textures/block/birch_log.png"), new ResourceLocation("textures/blocks/log_birch.png"));
            var0.put(new ResourceLocation("textures/block/birch_log_top.png"), new ResourceLocation("textures/blocks/log_birch_top.png"));
            var0.put(new ResourceLocation("textures/block/jungle_log.png"), new ResourceLocation("textures/blocks/log_jungle.png"));
            var0.put(new ResourceLocation("textures/block/jungle_log_top.png"), new ResourceLocation("textures/blocks/log_jungle_top.png"));
            var0.put(new ResourceLocation("textures/block/oak_log.png"), new ResourceLocation("textures/blocks/log_oak.png"));
            var0.put(new ResourceLocation("textures/block/oak_log_top.png"), new ResourceLocation("textures/blocks/log_oak_top.png"));
            var0.put(new ResourceLocation("textures/block/spruce_log.png"), new ResourceLocation("textures/blocks/log_spruce.png"));
            var0.put(new ResourceLocation("textures/block/spruce_log_top.png"), new ResourceLocation("textures/blocks/log_spruce_top.png"));
            var0.put(new ResourceLocation("textures/block/magma.png"), new ResourceLocation("textures/blocks/magma.png"));
            var0.put(new ResourceLocation("textures/block/melon_side.png"), new ResourceLocation("textures/blocks/melon_side.png"));
            var0.put(new ResourceLocation("textures/block/attached_melon_stem.png"), new ResourceLocation("textures/blocks/melon_stem_connected.png"));
            var0.put(new ResourceLocation("textures/blocks/pumpkin_stem_disconnected.png"), new ResourceLocation("textures/blocks/melon_stem_disconnected.png"));
            var0.put(new ResourceLocation("textures/block/melon_top.png"), new ResourceLocation("textures/blocks/melon_top.png"));
            var0.put(new ResourceLocation("textures/block/spawner.png"), new ResourceLocation("textures/blocks/mob_spawner.png"));
            var0.put(new ResourceLocation("textures/block/mushroom_block_inside.png"), new ResourceLocation("textures/blocks/mushroom_block_inside.png"));
            var0.put(new ResourceLocation("textures/block/brown_mushroom_block.png"), new ResourceLocation("textures/blocks/mushroom_block_skin_brown.png"));
            var0.put(new ResourceLocation("textures/block/red_mushroom_block.png"), new ResourceLocation("textures/blocks/mushroom_block_skin_red.png"));
            var0.put(new ResourceLocation("textures/block/mushroom_stem.png"), new ResourceLocation("textures/blocks/mushroom_block_skin_stem.png"));
            var0.put(new ResourceLocation("textures/block/brown_mushroom.png"), new ResourceLocation("textures/blocks/mushroom_brown.png"));
            var0.put(new ResourceLocation("textures/block/red_mushroom.png"), new ResourceLocation("textures/blocks/mushroom_red.png"));
            var0.put(new ResourceLocation("textures/block/mycelium_side.png"), new ResourceLocation("textures/blocks/mycelium_side.png"));
            var0.put(new ResourceLocation("textures/block/mycelium_top.png"), new ResourceLocation("textures/blocks/mycelium_top.png"));
            var0.put(new ResourceLocation("textures/block/nether_bricks.png"), new ResourceLocation("textures/blocks/nether_brick.png"));
            var0.put(new ResourceLocation("textures/block/nether_wart_block.png"), new ResourceLocation("textures/blocks/nether_wart_block.png"));
            var0.put(new ResourceLocation("textures/block/nether_wart_stage0.png"), new ResourceLocation("textures/blocks/nether_wart_stage_0.png"));
            var0.put(new ResourceLocation("textures/block/nether_wart_stage1.png"), new ResourceLocation("textures/blocks/nether_wart_stage_1.png"));
            var0.put(new ResourceLocation("textures/block/nether_wart_stage2.png"), new ResourceLocation("textures/blocks/nether_wart_stage_2.png"));
            var0.put(new ResourceLocation("textures/block/netherrack.png"), new ResourceLocation("textures/blocks/netherrack.png"));
            var0.put(new ResourceLocation("textures/block/note_block.png"), new ResourceLocation("textures/blocks/noteblock.png"));
            var0.put(new ResourceLocation("textures/block/observer_back.png"), new ResourceLocation("textures/blocks/observer_back.png"));
            var0.put(new ResourceLocation("textures/block/observer_back_on.png"), new ResourceLocation("textures/blocks/observer_back_lit.png"));
            var0.put(new ResourceLocation("textures/block/observer_front.png"), new ResourceLocation("textures/blocks/observer_front.png"));
            var0.put(new ResourceLocation("textures/block/observer_side.png"), new ResourceLocation("textures/blocks/observer_side.png"));
            var0.put(new ResourceLocation("textures/block/observer_top.png"), new ResourceLocation("textures/blocks/observer_top.png"));
            var0.put(new ResourceLocation("textures/block/obsidian.png"), new ResourceLocation("textures/blocks/obsidian.png"));
            var0.put(new ResourceLocation("textures/block/piston_bottom.png"), new ResourceLocation("textures/blocks/piston_bottom.png"));
            var0.put(new ResourceLocation("textures/block/piston_inner.png"), new ResourceLocation("textures/blocks/piston_inner.png"));
            var0.put(new ResourceLocation("textures/block/piston_side.png"), new ResourceLocation("textures/blocks/piston_side.png"));
            var0.put(new ResourceLocation("textures/block/piston_top.png"), new ResourceLocation("textures/blocks/piston_top_normal.png"));
            var0.put(new ResourceLocation("textures/block/piston_top_sticky.png"), new ResourceLocation("textures/blocks/piston_top_sticky.png"));
            var0.put(new ResourceLocation("textures/block/acacia_planks.png"), new ResourceLocation("textures/blocks/planks_acacia.png"));
            var0.put(new ResourceLocation("textures/block/dark_oak_planks.png"), new ResourceLocation("textures/blocks/planks_big_oak.png"));
            var0.put(new ResourceLocation("textures/block/birch_planks.png"), new ResourceLocation("textures/blocks/planks_birch.png"));
            var0.put(new ResourceLocation("textures/block/jungle_planks.png"), new ResourceLocation("textures/blocks/planks_jungle.png"));
            var0.put(new ResourceLocation("textures/block/oak_planks.png"), new ResourceLocation("textures/blocks/planks_oak.png"));
            var0.put(new ResourceLocation("textures/block/spruce_planks.png"), new ResourceLocation("textures/blocks/planks_spruce.png"));
            var0.put(new ResourceLocation("textures/block/nether_portal.png"), new ResourceLocation("textures/blocks/portal.png"));
            var0.put(new ResourceLocation("textures/block/potatoes_stage0.png"), new ResourceLocation("textures/blocks/potatoes_stage_0.png"));
            var0.put(new ResourceLocation("textures/block/potatoes_stage1.png"), new ResourceLocation("textures/blocks/potatoes_stage_1.png"));
            var0.put(new ResourceLocation("textures/block/potatoes_stage2.png"), new ResourceLocation("textures/blocks/potatoes_stage_2.png"));
            var0.put(new ResourceLocation("textures/block/potatoes_stage3.png"), new ResourceLocation("textures/blocks/potatoes_stage_3.png"));
            var0.put(new ResourceLocation("textures/block/prismarine_bricks.png"), new ResourceLocation("textures/blocks/prismarine_bricks.png"));
            var0.put(new ResourceLocation("textures/block/dark_prismarine.png"), new ResourceLocation("textures/blocks/prismarine_dark.png"));
            var0.put(new ResourceLocation("textures/block/prismarine.png"), new ResourceLocation("textures/blocks/prismarine_rough.png"));
            var0.put(new ResourceLocation("textures/block/carved_pumpkin.png"), new ResourceLocation("textures/blocks/pumpkin_face_off.png"));
            var0.put(new ResourceLocation("textures/block/jack_o_lantern.png"), new ResourceLocation("textures/blocks/pumpkin_face_on.png"));
            var0.put(new ResourceLocation("textures/block/pumpkin_side.png"), new ResourceLocation("textures/blocks/pumpkin_side.png"));
            var0.put(new ResourceLocation("textures/block/attached_pumpkin_stem.png"), new ResourceLocation("textures/blocks/pumpkin_stem_connected.png"));
            var0.put(new ResourceLocation("textures/block/pumpkin_top.png"), new ResourceLocation("textures/blocks/pumpkin_top.png"));
            var0.put(new ResourceLocation("textures/block/purpur_block.png"), new ResourceLocation("textures/blocks/purpur_block.png"));
            var0.put(new ResourceLocation("textures/block/purpur_pillar.png"), new ResourceLocation("textures/blocks/purpur_pillar.png"));
            var0.put(new ResourceLocation("textures/block/purpur_pillar_top.png"), new ResourceLocation("textures/blocks/purpur_pillar_top.png"));
            var0.put(new ResourceLocation("textures/block/quartz_block_bottom.png"), new ResourceLocation("textures/blocks/quartz_block_bottom.png"));
            var0.put(new ResourceLocation("textures/block/chiseled_quartz_block.png"), new ResourceLocation("textures/blocks/quartz_block_chiseled.png"));
            var0.put(
                new ResourceLocation("textures/block/chiseled_quartz_block_top.png"), new ResourceLocation("textures/blocks/quartz_block_chiseled_top.png")
            );
            var0.put(new ResourceLocation("textures/block/quartz_pillar.png"), new ResourceLocation("textures/blocks/quartz_block_lines.png"));
            var0.put(new ResourceLocation("textures/block/quartz_pillar_top.png"), new ResourceLocation("textures/blocks/quartz_block_lines_top.png"));
            var0.put(new ResourceLocation("textures/block/quartz_block_side.png"), new ResourceLocation("textures/blocks/quartz_block_side.png"));
            var0.put(new ResourceLocation("textures/block/quartz_block_top.png"), new ResourceLocation("textures/blocks/quartz_block_top.png"));
            var0.put(new ResourceLocation("textures/block/nether_quartz_ore.png"), new ResourceLocation("textures/blocks/quartz_ore.png"));
            var0.put(new ResourceLocation("textures/block/activator_rail.png"), new ResourceLocation("textures/blocks/rail_activator.png"));
            var0.put(new ResourceLocation("textures/block/activator_rail_on.png"), new ResourceLocation("textures/blocks/rail_activator_powered.png"));
            var0.put(new ResourceLocation("textures/block/detector_rail.png"), new ResourceLocation("textures/blocks/rail_detector.png"));
            var0.put(new ResourceLocation("textures/block/detector_rail_on.png"), new ResourceLocation("textures/blocks/rail_detector_powered.png"));
            var0.put(new ResourceLocation("textures/block/powered_rail.png"), new ResourceLocation("textures/blocks/rail_golden.png"));
            var0.put(new ResourceLocation("textures/block/powered_rail_on.png"), new ResourceLocation("textures/blocks/rail_golden_powered.png"));
            var0.put(new ResourceLocation("textures/block/rail.png"), new ResourceLocation("textures/blocks/rail_normal.png"));
            var0.put(new ResourceLocation("textures/block/rail_corner.png"), new ResourceLocation("textures/blocks/rail_normal_turned.png"));
            var0.put(new ResourceLocation("textures/block/red_nether_bricks.png"), new ResourceLocation("textures/blocks/red_nether_brick.png"));
            var0.put(new ResourceLocation("textures/block/red_sand.png"), new ResourceLocation("textures/blocks/red_sand.png"));
            var0.put(new ResourceLocation("textures/block/red_sandstone_bottom.png"), new ResourceLocation("textures/blocks/red_sandstone_bottom.png"));
            var0.put(new ResourceLocation("textures/block/chiseled_red_sandstone.png"), new ResourceLocation("textures/blocks/red_sandstone_carved.png"));
            var0.put(new ResourceLocation("textures/block/red_sandstone.png"), new ResourceLocation("textures/blocks/red_sandstone_normal.png"));
            var0.put(new ResourceLocation("textures/block/cut_red_sandstone.png"), new ResourceLocation("textures/blocks/red_sandstone_smooth.png"));
            var0.put(new ResourceLocation("textures/block/red_sandstone_top.png"), new ResourceLocation("textures/blocks/red_sandstone_top.png"));
            var0.put(new ResourceLocation("textures/block/redstone_block.png"), new ResourceLocation("textures/blocks/redstone_block.png"));
            var0.put(new ResourceLocation("textures/block/redstone_dust_dot.png"), new ResourceLocation("textures/blocks/redstone_dust_dot.png"));
            var0.put(new ResourceLocation("textures/block/redstone_dust_line0.png"), new ResourceLocation("textures/blocks/redstone_dust_line0.png"));
            var0.put(new ResourceLocation("textures/block/redstone_dust_line1.png"), new ResourceLocation("textures/blocks/redstone_dust_line1.png"));
            var0.put(new ResourceLocation("textures/block/redstone_dust_overlay.png"), new ResourceLocation("textures/blocks/redstone_dust_overlay.png"));
            var0.put(new ResourceLocation("textures/block/redstone_lamp.png"), new ResourceLocation("textures/blocks/redstone_lamp_off.png"));
            var0.put(new ResourceLocation("textures/block/redstone_lamp_on.png"), new ResourceLocation("textures/blocks/redstone_lamp_on.png"));
            var0.put(new ResourceLocation("textures/block/redstone_ore.png"), new ResourceLocation("textures/blocks/redstone_ore.png"));
            var0.put(new ResourceLocation("textures/block/redstone_torch_off.png"), new ResourceLocation("textures/blocks/redstone_torch_off.png"));
            var0.put(new ResourceLocation("textures/block/redstone_torch.png"), new ResourceLocation("textures/blocks/redstone_torch_on.png"));
            var0.put(new ResourceLocation("textures/block/sugar_cane.png"), new ResourceLocation("textures/blocks/reeds.png"));
            var0.put(new ResourceLocation("textures/block/repeater.png"), new ResourceLocation("textures/blocks/repeater_off.png"));
            var0.put(new ResourceLocation("textures/block/repeater_on.png"), new ResourceLocation("textures/blocks/repeater_on.png"));
            var0.put(
                new ResourceLocation("textures/block/repeating_command_block_back.png"),
                new ResourceLocation("textures/blocks/repeating_command_block_back.png")
            );
            var0.put(
                new ResourceLocation("textures/block/repeating_command_block_conditional.png"),
                new ResourceLocation("textures/blocks/repeating_command_block_conditional.png")
            );
            var0.put(
                new ResourceLocation("textures/block/repeating_command_block_front.png"),
                new ResourceLocation("textures/blocks/repeating_command_block_front.png")
            );
            var0.put(
                new ResourceLocation("textures/block/repeating_command_block_side.png"),
                new ResourceLocation("textures/blocks/repeating_command_block_side.png")
            );
            var0.put(new ResourceLocation("textures/block/sand.png"), new ResourceLocation("textures/blocks/sand.png"));
            var0.put(new ResourceLocation("textures/block/sandstone_bottom.png"), new ResourceLocation("textures/blocks/sandstone_bottom.png"));
            var0.put(new ResourceLocation("textures/block/chiseled_sandstone.png"), new ResourceLocation("textures/blocks/sandstone_carved.png"));
            var0.put(new ResourceLocation("textures/block/sandstone.png"), new ResourceLocation("textures/blocks/sandstone_normal.png"));
            var0.put(new ResourceLocation("textures/block/cut_sandstone.png"), new ResourceLocation("textures/blocks/sandstone_smooth.png"));
            var0.put(new ResourceLocation("textures/block/sandstone_top.png"), new ResourceLocation("textures/blocks/sandstone_top.png"));
            var0.put(new ResourceLocation("textures/block/acacia_sapling.png"), new ResourceLocation("textures/blocks/sapling_acacia.png"));
            var0.put(new ResourceLocation("textures/block/birch_sapling.png"), new ResourceLocation("textures/blocks/sapling_birch.png"));
            var0.put(new ResourceLocation("textures/block/jungle_sapling.png"), new ResourceLocation("textures/blocks/sapling_jungle.png"));
            var0.put(new ResourceLocation("textures/block/oak_sapling.png"), new ResourceLocation("textures/blocks/sapling_oak.png"));
            var0.put(new ResourceLocation("textures/block/dark_oak_sapling.png"), new ResourceLocation("textures/blocks/sapling_roofed_oak.png"));
            var0.put(new ResourceLocation("textures/block/spruce_sapling.png"), new ResourceLocation("textures/blocks/sapling_spruce.png"));
            var0.put(new ResourceLocation("textures/block/sea_lantern.png"), new ResourceLocation("textures/blocks/sea_lantern.png"));
            var0.put(new ResourceLocation("textures/block/black_shulker_box.png"), new ResourceLocation("textures/blocks/shulker_top_black.png"));
            var0.put(new ResourceLocation("textures/block/blue_shulker_box.png"), new ResourceLocation("textures/blocks/shulker_top_blue.png"));
            var0.put(new ResourceLocation("textures/block/brown_shulker_box.png"), new ResourceLocation("textures/blocks/shulker_top_brown.png"));
            var0.put(new ResourceLocation("textures/block/cyan_shulker_box.png"), new ResourceLocation("textures/blocks/shulker_top_cyan.png"));
            var0.put(new ResourceLocation("textures/block/gray_shulker_box.png"), new ResourceLocation("textures/blocks/shulker_top_gray.png"));
            var0.put(new ResourceLocation("textures/block/green_shulker_box.png"), new ResourceLocation("textures/blocks/shulker_top_green.png"));
            var0.put(new ResourceLocation("textures/block/light_blue_shulker_box.png"), new ResourceLocation("textures/blocks/shulker_top_light_blue.png"));
            var0.put(new ResourceLocation("textures/block/lime_shulker_box.png"), new ResourceLocation("textures/blocks/shulker_top_lime.png"));
            var0.put(new ResourceLocation("textures/block/magenta_shulker_box.png"), new ResourceLocation("textures/blocks/shulker_top_magenta.png"));
            var0.put(new ResourceLocation("textures/block/orange_shulker_box.png"), new ResourceLocation("textures/blocks/shulker_top_orange.png"));
            var0.put(new ResourceLocation("textures/block/pink_shulker_box.png"), new ResourceLocation("textures/blocks/shulker_top_pink.png"));
            var0.put(new ResourceLocation("textures/block/purple_shulker_box.png"), new ResourceLocation("textures/blocks/shulker_top_purple.png"));
            var0.put(new ResourceLocation("textures/block/red_shulker_box.png"), new ResourceLocation("textures/blocks/shulker_top_red.png"));
            var0.put(new ResourceLocation("textures/block/light_gray_shulker_box.png"), new ResourceLocation("textures/blocks/shulker_top_silver.png"));
            var0.put(new ResourceLocation("textures/block/white_shulker_box.png"), new ResourceLocation("textures/blocks/shulker_top_white.png"));
            var0.put(new ResourceLocation("textures/block/yellow_shulker_box.png"), new ResourceLocation("textures/blocks/shulker_top_yellow.png"));
            var0.put(new ResourceLocation("textures/block/slime_block.png"), new ResourceLocation("textures/blocks/slime.png"));
            var0.put(new ResourceLocation("textures/block/snow.png"), new ResourceLocation("textures/blocks/snow.png"));
            var0.put(new ResourceLocation("textures/block/soul_sand.png"), new ResourceLocation("textures/blocks/soul_sand.png"));
            var0.put(new ResourceLocation("textures/block/sponge.png"), new ResourceLocation("textures/blocks/sponge.png"));
            var0.put(new ResourceLocation("textures/block/wet_sponge.png"), new ResourceLocation("textures/blocks/sponge_wet.png"));
            var0.put(new ResourceLocation("textures/block/stone.png"), new ResourceLocation("textures/blocks/stone.png"));
            var0.put(new ResourceLocation("textures/block/andesite.png"), new ResourceLocation("textures/blocks/stone_andesite.png"));
            var0.put(new ResourceLocation("textures/block/polished_andesite.png"), new ResourceLocation("textures/blocks/stone_andesite_smooth.png"));
            var0.put(new ResourceLocation("textures/block/diorite.png"), new ResourceLocation("textures/blocks/stone_diorite.png"));
            var0.put(new ResourceLocation("textures/block/polished_diorite.png"), new ResourceLocation("textures/blocks/stone_diorite_smooth.png"));
            var0.put(new ResourceLocation("textures/block/granite.png"), new ResourceLocation("textures/blocks/stone_granite.png"));
            var0.put(new ResourceLocation("textures/block/polished_granite.png"), new ResourceLocation("textures/blocks/stone_granite_smooth.png"));
            var0.put(new ResourceLocation("textures/block/stone_slab_side.png"), new ResourceLocation("textures/blocks/stone_slab_side.png"));
            var0.put(new ResourceLocation("textures/block/stone_slab_top.png"), new ResourceLocation("textures/blocks/stone_slab_top.png"));
            var0.put(new ResourceLocation("textures/block/stone_bricks.png"), new ResourceLocation("textures/blocks/stonebrick.png"));
            var0.put(new ResourceLocation("textures/block/chiseled_stone_bricks.png"), new ResourceLocation("textures/blocks/stonebrick_carved.png"));
            var0.put(new ResourceLocation("textures/block/cracked_stone_bricks.png"), new ResourceLocation("textures/blocks/stonebrick_cracked.png"));
            var0.put(new ResourceLocation("textures/block/mossy_stone_bricks.png"), new ResourceLocation("textures/blocks/stonebrick_mossy.png"));
            var0.put(new ResourceLocation("textures/block/structure_block.png"), new ResourceLocation("textures/blocks/structure_block.png"));
            var0.put(new ResourceLocation("textures/block/structure_block_corner.png"), new ResourceLocation("textures/blocks/structure_block_corner.png"));
            var0.put(new ResourceLocation("textures/block/structure_block_data.png"), new ResourceLocation("textures/blocks/structure_block_data.png"));
            var0.put(new ResourceLocation("textures/block/structure_block_load.png"), new ResourceLocation("textures/blocks/structure_block_load.png"));
            var0.put(new ResourceLocation("textures/block/structure_block_save.png"), new ResourceLocation("textures/blocks/structure_block_save.png"));
            var0.put(new ResourceLocation("textures/block/grass.png"), new ResourceLocation("textures/blocks/tallgrass.png"));
            var0.put(new ResourceLocation("textures/block/tnt_bottom.png"), new ResourceLocation("textures/blocks/tnt_bottom.png"));
            var0.put(new ResourceLocation("textures/block/tnt_side.png"), new ResourceLocation("textures/blocks/tnt_side.png"));
            var0.put(new ResourceLocation("textures/block/tnt_top.png"), new ResourceLocation("textures/blocks/tnt_top.png"));
            var0.put(new ResourceLocation("textures/block/torch.png"), new ResourceLocation("textures/blocks/torch_on.png"));
            var0.put(new ResourceLocation("textures/block/oak_trapdoor.png"), new ResourceLocation("textures/blocks/trapdoor.png"));
            var0.put(new ResourceLocation("textures/block/tripwire.png"), new ResourceLocation("textures/blocks/trip_wire.png"));
            var0.put(new ResourceLocation("textures/block/tripwire_hook.png"), new ResourceLocation("textures/blocks/trip_wire_source.png"));
            var0.put(new ResourceLocation("textures/block/vine.png"), new ResourceLocation("textures/blocks/vine.png"));
            var0.put(new ResourceLocation("textures/block/water_flow.png"), new ResourceLocation("textures/blocks/water_flow.png"));
            var0.put(new ResourceLocation("textures/block/water_overlay.png"), new ResourceLocation("textures/blocks/water_overlay.png"));
            var0.put(new ResourceLocation("textures/block/water_still.png"), new ResourceLocation("textures/blocks/water_still.png"));
            var0.put(new ResourceLocation("textures/block/lily_pad.png"), new ResourceLocation("textures/blocks/waterlily.png"));
            var0.put(new ResourceLocation("textures/block/cobweb.png"), new ResourceLocation("textures/blocks/web.png"));
            var0.put(new ResourceLocation("textures/block/wheat_stage0.png"), new ResourceLocation("textures/blocks/wheat_stage_0.png"));
            var0.put(new ResourceLocation("textures/block/wheat_stage1.png"), new ResourceLocation("textures/blocks/wheat_stage_1.png"));
            var0.put(new ResourceLocation("textures/block/wheat_stage2.png"), new ResourceLocation("textures/blocks/wheat_stage_2.png"));
            var0.put(new ResourceLocation("textures/block/wheat_stage3.png"), new ResourceLocation("textures/blocks/wheat_stage_3.png"));
            var0.put(new ResourceLocation("textures/block/wheat_stage4.png"), new ResourceLocation("textures/blocks/wheat_stage_4.png"));
            var0.put(new ResourceLocation("textures/block/wheat_stage5.png"), new ResourceLocation("textures/blocks/wheat_stage_5.png"));
            var0.put(new ResourceLocation("textures/block/wheat_stage6.png"), new ResourceLocation("textures/blocks/wheat_stage_6.png"));
            var0.put(new ResourceLocation("textures/block/wheat_stage7.png"), new ResourceLocation("textures/blocks/wheat_stage_7.png"));
            var0.put(new ResourceLocation("textures/block/black_wool.png"), new ResourceLocation("textures/blocks/wool_colored_black.png"));
            var0.put(new ResourceLocation("textures/block/blue_wool.png"), new ResourceLocation("textures/blocks/wool_colored_blue.png"));
            var0.put(new ResourceLocation("textures/block/brown_wool.png"), new ResourceLocation("textures/blocks/wool_colored_brown.png"));
            var0.put(new ResourceLocation("textures/block/cyan_wool.png"), new ResourceLocation("textures/blocks/wool_colored_cyan.png"));
            var0.put(new ResourceLocation("textures/block/gray_wool.png"), new ResourceLocation("textures/blocks/wool_colored_gray.png"));
            var0.put(new ResourceLocation("textures/block/green_wool.png"), new ResourceLocation("textures/blocks/wool_colored_green.png"));
            var0.put(new ResourceLocation("textures/block/light_blue_wool.png"), new ResourceLocation("textures/blocks/wool_colored_light_blue.png"));
            var0.put(new ResourceLocation("textures/block/lime_wool.png"), new ResourceLocation("textures/blocks/wool_colored_lime.png"));
            var0.put(new ResourceLocation("textures/block/magenta_wool.png"), new ResourceLocation("textures/blocks/wool_colored_magenta.png"));
            var0.put(new ResourceLocation("textures/block/orange_wool.png"), new ResourceLocation("textures/blocks/wool_colored_orange.png"));
            var0.put(new ResourceLocation("textures/block/pink_wool.png"), new ResourceLocation("textures/blocks/wool_colored_pink.png"));
            var0.put(new ResourceLocation("textures/block/purple_wool.png"), new ResourceLocation("textures/blocks/wool_colored_purple.png"));
            var0.put(new ResourceLocation("textures/block/red_wool.png"), new ResourceLocation("textures/blocks/wool_colored_red.png"));
            var0.put(new ResourceLocation("textures/block/light_gray_wool.png"), new ResourceLocation("textures/blocks/wool_colored_silver.png"));
            var0.put(new ResourceLocation("textures/block/white_wool.png"), new ResourceLocation("textures/blocks/wool_colored_white.png"));
            var0.put(new ResourceLocation("textures/block/yellow_wool.png"), new ResourceLocation("textures/blocks/wool_colored_yellow.png"));
            var0.put(new ResourceLocation("textures/entity/bed/light_gray.png"), new ResourceLocation("textures/entity/bed/silver.png"));
            var0.put(new ResourceLocation("textures/entity/boat/acacia.png"), new ResourceLocation("textures/entity/boat/boat_acacia.png"));
            var0.put(new ResourceLocation("textures/entity/boat/birch.png"), new ResourceLocation("textures/entity/boat/boat_birch.png"));
            var0.put(new ResourceLocation("textures/entity/boat/dark_oak.png"), new ResourceLocation("textures/entity/boat/boat_darkoak.png"));
            var0.put(new ResourceLocation("textures/entity/boat/jungle.png"), new ResourceLocation("textures/entity/boat/boat_jungle.png"));
            var0.put(new ResourceLocation("textures/entity/boat/oak.png"), new ResourceLocation("textures/entity/boat/boat_oak.png"));
            var0.put(new ResourceLocation("textures/entity/boat/spruce.png"), new ResourceLocation("textures/entity/boat/boat_spruce.png"));
            var0.put(new ResourceLocation("textures/block/conduit.png"), new ResourceLocation("textures/entity/conduit/break_particle.png"));
            var0.put(new ResourceLocation("textures/entity/end_crystal/end_crystal.png"), new ResourceLocation("textures/entity/endercrystal/endercrystal.png"));
            var0.put(
                new ResourceLocation("textures/entity/end_crystal/end_crystal_beam.png"),
                new ResourceLocation("textures/entity/endercrystal/endercrystal_beam.png")
            );
            var0.put(new ResourceLocation("textures/entity/illager/evoker_fangs.png"), new ResourceLocation("textures/entity/illager/fangs.png"));
            var0.put(new ResourceLocation("textures/entity/illager/illusioner.png"), new ResourceLocation("textures/entity/illager/illusionist.png"));
            var0.put(new ResourceLocation("textures/entity/llama/decor/black.png"), new ResourceLocation("textures/entity/llama/decor/decor_black.png"));
            var0.put(new ResourceLocation("textures/entity/llama/decor/blue.png"), new ResourceLocation("textures/entity/llama/decor/decor_blue.png"));
            var0.put(new ResourceLocation("textures/entity/llama/decor/brown.png"), new ResourceLocation("textures/entity/llama/decor/decor_brown.png"));
            var0.put(new ResourceLocation("textures/entity/llama/decor/cyan.png"), new ResourceLocation("textures/entity/llama/decor/decor_cyan.png"));
            var0.put(new ResourceLocation("textures/entity/llama/decor/gray.png"), new ResourceLocation("textures/entity/llama/decor/decor_gray.png"));
            var0.put(new ResourceLocation("textures/entity/llama/decor/green.png"), new ResourceLocation("textures/entity/llama/decor/decor_green.png"));
            var0.put(
                new ResourceLocation("textures/entity/llama/decor/light_blue.png"), new ResourceLocation("textures/entity/llama/decor/decor_light_blue.png")
            );
            var0.put(new ResourceLocation("textures/entity/llama/decor/lime.png"), new ResourceLocation("textures/entity/llama/decor/decor_lime.png"));
            var0.put(new ResourceLocation("textures/entity/llama/decor/magenta.png"), new ResourceLocation("textures/entity/llama/decor/decor_magenta.png"));
            var0.put(new ResourceLocation("textures/entity/llama/decor/orange.png"), new ResourceLocation("textures/entity/llama/decor/decor_orange.png"));
            var0.put(new ResourceLocation("textures/entity/llama/decor/pink.png"), new ResourceLocation("textures/entity/llama/decor/decor_pink.png"));
            var0.put(new ResourceLocation("textures/entity/llama/decor/purple.png"), new ResourceLocation("textures/entity/llama/decor/decor_purple.png"));
            var0.put(new ResourceLocation("textures/entity/llama/decor/red.png"), new ResourceLocation("textures/entity/llama/decor/decor_red.png"));
            var0.put(new ResourceLocation("textures/entity/llama/decor/light_gray.png"), new ResourceLocation("textures/entity/llama/decor/decor_silver.png"));
            var0.put(new ResourceLocation("textures/entity/llama/decor/white.png"), new ResourceLocation("textures/entity/llama/decor/decor_white.png"));
            var0.put(new ResourceLocation("textures/entity/llama/decor/yellow.png"), new ResourceLocation("textures/entity/llama/decor/decor_yellow.png"));
            var0.put(new ResourceLocation("textures/entity/llama/brown.png"), new ResourceLocation("textures/entity/llama/llama_brown.png"));
            var0.put(new ResourceLocation("textures/entity/llama/creamy.png"), new ResourceLocation("textures/entity/llama/llama_creamy.png"));
            var0.put(new ResourceLocation("textures/entity/llama/gray.png"), new ResourceLocation("textures/entity/llama/llama_gray.png"));
            var0.put(new ResourceLocation("textures/entity/llama/white.png"), new ResourceLocation("textures/entity/llama/llama_white.png"));
            var0.put(new ResourceLocation("textures/entity/shulker/shulker.png"), new ResourceLocation("textures/entity/shulker/shulker_purple.png"));
            var0.put(new ResourceLocation("textures/entity/shulker/shulker_light_gray.png"), new ResourceLocation("textures/entity/shulker/shulker_silver.png"));
            var0.put(new ResourceLocation("textures/entity/snow_golem.png"), new ResourceLocation("textures/entity/snowman.png"));
            var0.put(new ResourceLocation("textures/item/acacia_boat.png"), new ResourceLocation("textures/items/acacia_boat.png"));
            var0.put(new ResourceLocation("textures/item/apple.png"), new ResourceLocation("textures/items/apple.png"));
            var0.put(new ResourceLocation("textures/item/golden_apple.png"), new ResourceLocation("textures/items/apple_golden.png"));
            var0.put(new ResourceLocation("textures/item/arrow.png"), new ResourceLocation("textures/items/arrow.png"));
            var0.put(new ResourceLocation("textures/item/barrier.png"), new ResourceLocation("textures/items/barrier.png"));
            var0.put(new ResourceLocation("textures/item/cooked_beef.png"), new ResourceLocation("textures/items/beef_cooked.png"));
            var0.put(new ResourceLocation("textures/item/beef.png"), new ResourceLocation("textures/items/beef_raw.png"));
            var0.put(new ResourceLocation("textures/item/beetroot.png"), new ResourceLocation("textures/items/beetroot.png"));
            var0.put(new ResourceLocation("textures/item/beetroot_seeds.png"), new ResourceLocation("textures/items/beetroot_seeds.png"));
            var0.put(new ResourceLocation("textures/item/beetroot_soup.png"), new ResourceLocation("textures/items/beetroot_soup.png"));
            var0.put(new ResourceLocation("textures/item/birch_boat.png"), new ResourceLocation("textures/items/birch_boat.png"));
            var0.put(new ResourceLocation("textures/item/blaze_powder.png"), new ResourceLocation("textures/items/blaze_powder.png"));
            var0.put(new ResourceLocation("textures/item/blaze_rod.png"), new ResourceLocation("textures/items/blaze_rod.png"));
            var0.put(new ResourceLocation("textures/item/bone.png"), new ResourceLocation("textures/items/bone.png"));
            var0.put(new ResourceLocation("textures/item/enchanted_book.png"), new ResourceLocation("textures/items/book_enchanted.png"));
            var0.put(new ResourceLocation("textures/item/book.png"), new ResourceLocation("textures/items/book_normal.png"));
            var0.put(new ResourceLocation("textures/item/writable_book.png"), new ResourceLocation("textures/items/book_writable.png"));
            var0.put(new ResourceLocation("textures/item/written_book.png"), new ResourceLocation("textures/items/book_written.png"));
            var0.put(new ResourceLocation("textures/item/bow_pulling_0.png"), new ResourceLocation("textures/items/bow_pulling_0.png"));
            var0.put(new ResourceLocation("textures/item/bow_pulling_1.png"), new ResourceLocation("textures/items/bow_pulling_1.png"));
            var0.put(new ResourceLocation("textures/item/bow_pulling_2.png"), new ResourceLocation("textures/items/bow_pulling_2.png"));
            var0.put(new ResourceLocation("textures/item/bow.png"), new ResourceLocation("textures/items/bow_standby.png"));
            var0.put(new ResourceLocation("textures/item/bowl.png"), new ResourceLocation("textures/items/bowl.png"));
            var0.put(new ResourceLocation("textures/item/bread.png"), new ResourceLocation("textures/items/bread.png"));
            var0.put(new ResourceLocation("textures/item/brewing_stand.png"), new ResourceLocation("textures/items/brewing_stand.png"));
            var0.put(new ResourceLocation("textures/item/brick.png"), new ResourceLocation("textures/items/brick.png"));
            var0.put(new ResourceLocation("textures/item/broken_elytra.png"), new ResourceLocation("textures/items/broken_elytra.png"));
            var0.put(new ResourceLocation("textures/item/bucket.png"), new ResourceLocation("textures/items/bucket_empty.png"));
            var0.put(new ResourceLocation("textures/item/lava_bucket.png"), new ResourceLocation("textures/items/bucket_lava.png"));
            var0.put(new ResourceLocation("textures/item/milk_bucket.png"), new ResourceLocation("textures/items/bucket_milk.png"));
            var0.put(new ResourceLocation("textures/item/water_bucket.png"), new ResourceLocation("textures/items/bucket_water.png"));
            var0.put(new ResourceLocation("textures/item/cake.png"), new ResourceLocation("textures/items/cake.png"));
            var0.put(new ResourceLocation("textures/item/carrot.png"), new ResourceLocation("textures/items/carrot.png"));
            var0.put(new ResourceLocation("textures/item/golden_carrot.png"), new ResourceLocation("textures/items/carrot_golden.png"));
            var0.put(new ResourceLocation("textures/item/carrot_on_a_stick.png"), new ResourceLocation("textures/items/carrot_on_a_stick.png"));
            var0.put(new ResourceLocation("textures/item/cauldron.png"), new ResourceLocation("textures/items/cauldron.png"));
            var0.put(new ResourceLocation("textures/item/chainmail_boots.png"), new ResourceLocation("textures/items/chainmail_boots.png"));
            var0.put(new ResourceLocation("textures/item/chainmail_chestplate.png"), new ResourceLocation("textures/items/chainmail_chestplate.png"));
            var0.put(new ResourceLocation("textures/item/chainmail_helmet.png"), new ResourceLocation("textures/items/chainmail_helmet.png"));
            var0.put(new ResourceLocation("textures/item/chainmail_leggings.png"), new ResourceLocation("textures/items/chainmail_leggings.png"));
            var0.put(new ResourceLocation("textures/item/charcoal.png"), new ResourceLocation("textures/items/charcoal.png"));
            var0.put(new ResourceLocation("textures/item/cooked_chicken.png"), new ResourceLocation("textures/items/chicken_cooked.png"));
            var0.put(new ResourceLocation("textures/item/chicken.png"), new ResourceLocation("textures/items/chicken_raw.png"));
            var0.put(new ResourceLocation("textures/item/chorus_fruit.png"), new ResourceLocation("textures/items/chorus_fruit.png"));
            var0.put(new ResourceLocation("textures/item/popped_chorus_fruit.png"), new ResourceLocation("textures/items/chorus_fruit_popped.png"));
            var0.put(new ResourceLocation("textures/item/clay_ball.png"), new ResourceLocation("textures/items/clay_ball.png"));
            var0.put(new ResourceLocation("textures/item/clock_00.png"), new ResourceLocation("textures/items/clock_00.png"));
            var0.put(new ResourceLocation("textures/item/clock_01.png"), new ResourceLocation("textures/items/clock_01.png"));
            var0.put(new ResourceLocation("textures/item/clock_02.png"), new ResourceLocation("textures/items/clock_02.png"));
            var0.put(new ResourceLocation("textures/item/clock_03.png"), new ResourceLocation("textures/items/clock_03.png"));
            var0.put(new ResourceLocation("textures/item/clock_04.png"), new ResourceLocation("textures/items/clock_04.png"));
            var0.put(new ResourceLocation("textures/item/clock_05.png"), new ResourceLocation("textures/items/clock_05.png"));
            var0.put(new ResourceLocation("textures/item/clock_06.png"), new ResourceLocation("textures/items/clock_06.png"));
            var0.put(new ResourceLocation("textures/item/clock_07.png"), new ResourceLocation("textures/items/clock_07.png"));
            var0.put(new ResourceLocation("textures/item/clock_08.png"), new ResourceLocation("textures/items/clock_08.png"));
            var0.put(new ResourceLocation("textures/item/clock_09.png"), new ResourceLocation("textures/items/clock_09.png"));
            var0.put(new ResourceLocation("textures/item/clock_10.png"), new ResourceLocation("textures/items/clock_10.png"));
            var0.put(new ResourceLocation("textures/item/clock_11.png"), new ResourceLocation("textures/items/clock_11.png"));
            var0.put(new ResourceLocation("textures/item/clock_12.png"), new ResourceLocation("textures/items/clock_12.png"));
            var0.put(new ResourceLocation("textures/item/clock_13.png"), new ResourceLocation("textures/items/clock_13.png"));
            var0.put(new ResourceLocation("textures/item/clock_14.png"), new ResourceLocation("textures/items/clock_14.png"));
            var0.put(new ResourceLocation("textures/item/clock_15.png"), new ResourceLocation("textures/items/clock_15.png"));
            var0.put(new ResourceLocation("textures/item/clock_16.png"), new ResourceLocation("textures/items/clock_16.png"));
            var0.put(new ResourceLocation("textures/item/clock_17.png"), new ResourceLocation("textures/items/clock_17.png"));
            var0.put(new ResourceLocation("textures/item/clock_18.png"), new ResourceLocation("textures/items/clock_18.png"));
            var0.put(new ResourceLocation("textures/item/clock_19.png"), new ResourceLocation("textures/items/clock_19.png"));
            var0.put(new ResourceLocation("textures/item/clock_20.png"), new ResourceLocation("textures/items/clock_20.png"));
            var0.put(new ResourceLocation("textures/item/clock_21.png"), new ResourceLocation("textures/items/clock_21.png"));
            var0.put(new ResourceLocation("textures/item/clock_22.png"), new ResourceLocation("textures/items/clock_22.png"));
            var0.put(new ResourceLocation("textures/item/clock_23.png"), new ResourceLocation("textures/items/clock_23.png"));
            var0.put(new ResourceLocation("textures/item/clock_24.png"), new ResourceLocation("textures/items/clock_24.png"));
            var0.put(new ResourceLocation("textures/item/clock_25.png"), new ResourceLocation("textures/items/clock_25.png"));
            var0.put(new ResourceLocation("textures/item/clock_26.png"), new ResourceLocation("textures/items/clock_26.png"));
            var0.put(new ResourceLocation("textures/item/clock_27.png"), new ResourceLocation("textures/items/clock_27.png"));
            var0.put(new ResourceLocation("textures/item/clock_28.png"), new ResourceLocation("textures/items/clock_28.png"));
            var0.put(new ResourceLocation("textures/item/clock_29.png"), new ResourceLocation("textures/items/clock_29.png"));
            var0.put(new ResourceLocation("textures/item/clock_30.png"), new ResourceLocation("textures/items/clock_30.png"));
            var0.put(new ResourceLocation("textures/item/clock_31.png"), new ResourceLocation("textures/items/clock_31.png"));
            var0.put(new ResourceLocation("textures/item/clock_32.png"), new ResourceLocation("textures/items/clock_32.png"));
            var0.put(new ResourceLocation("textures/item/clock_33.png"), new ResourceLocation("textures/items/clock_33.png"));
            var0.put(new ResourceLocation("textures/item/clock_34.png"), new ResourceLocation("textures/items/clock_34.png"));
            var0.put(new ResourceLocation("textures/item/clock_35.png"), new ResourceLocation("textures/items/clock_35.png"));
            var0.put(new ResourceLocation("textures/item/clock_36.png"), new ResourceLocation("textures/items/clock_36.png"));
            var0.put(new ResourceLocation("textures/item/clock_37.png"), new ResourceLocation("textures/items/clock_37.png"));
            var0.put(new ResourceLocation("textures/item/clock_38.png"), new ResourceLocation("textures/items/clock_38.png"));
            var0.put(new ResourceLocation("textures/item/clock_39.png"), new ResourceLocation("textures/items/clock_39.png"));
            var0.put(new ResourceLocation("textures/item/clock_40.png"), new ResourceLocation("textures/items/clock_40.png"));
            var0.put(new ResourceLocation("textures/item/clock_41.png"), new ResourceLocation("textures/items/clock_41.png"));
            var0.put(new ResourceLocation("textures/item/clock_42.png"), new ResourceLocation("textures/items/clock_42.png"));
            var0.put(new ResourceLocation("textures/item/clock_43.png"), new ResourceLocation("textures/items/clock_43.png"));
            var0.put(new ResourceLocation("textures/item/clock_44.png"), new ResourceLocation("textures/items/clock_44.png"));
            var0.put(new ResourceLocation("textures/item/clock_45.png"), new ResourceLocation("textures/items/clock_45.png"));
            var0.put(new ResourceLocation("textures/item/clock_46.png"), new ResourceLocation("textures/items/clock_46.png"));
            var0.put(new ResourceLocation("textures/item/clock_47.png"), new ResourceLocation("textures/items/clock_47.png"));
            var0.put(new ResourceLocation("textures/item/clock_48.png"), new ResourceLocation("textures/items/clock_48.png"));
            var0.put(new ResourceLocation("textures/item/clock_49.png"), new ResourceLocation("textures/items/clock_49.png"));
            var0.put(new ResourceLocation("textures/item/clock_50.png"), new ResourceLocation("textures/items/clock_50.png"));
            var0.put(new ResourceLocation("textures/item/clock_51.png"), new ResourceLocation("textures/items/clock_51.png"));
            var0.put(new ResourceLocation("textures/item/clock_52.png"), new ResourceLocation("textures/items/clock_52.png"));
            var0.put(new ResourceLocation("textures/item/clock_53.png"), new ResourceLocation("textures/items/clock_53.png"));
            var0.put(new ResourceLocation("textures/item/clock_54.png"), new ResourceLocation("textures/items/clock_54.png"));
            var0.put(new ResourceLocation("textures/item/clock_55.png"), new ResourceLocation("textures/items/clock_55.png"));
            var0.put(new ResourceLocation("textures/item/clock_56.png"), new ResourceLocation("textures/items/clock_56.png"));
            var0.put(new ResourceLocation("textures/item/clock_57.png"), new ResourceLocation("textures/items/clock_57.png"));
            var0.put(new ResourceLocation("textures/item/clock_58.png"), new ResourceLocation("textures/items/clock_58.png"));
            var0.put(new ResourceLocation("textures/item/clock_59.png"), new ResourceLocation("textures/items/clock_59.png"));
            var0.put(new ResourceLocation("textures/item/clock_60.png"), new ResourceLocation("textures/items/clock_60.png"));
            var0.put(new ResourceLocation("textures/item/clock_61.png"), new ResourceLocation("textures/items/clock_61.png"));
            var0.put(new ResourceLocation("textures/item/clock_62.png"), new ResourceLocation("textures/items/clock_62.png"));
            var0.put(new ResourceLocation("textures/item/clock_63.png"), new ResourceLocation("textures/items/clock_63.png"));
            var0.put(new ResourceLocation("textures/item/coal.png"), new ResourceLocation("textures/items/coal.png"));
            var0.put(new ResourceLocation("textures/item/comparator.png"), new ResourceLocation("textures/items/comparator.png"));
            var0.put(new ResourceLocation("textures/item/compass_00.png"), new ResourceLocation("textures/items/compass_00.png"));
            var0.put(new ResourceLocation("textures/item/compass_01.png"), new ResourceLocation("textures/items/compass_01.png"));
            var0.put(new ResourceLocation("textures/item/compass_02.png"), new ResourceLocation("textures/items/compass_02.png"));
            var0.put(new ResourceLocation("textures/item/compass_03.png"), new ResourceLocation("textures/items/compass_03.png"));
            var0.put(new ResourceLocation("textures/item/compass_04.png"), new ResourceLocation("textures/items/compass_04.png"));
            var0.put(new ResourceLocation("textures/item/compass_05.png"), new ResourceLocation("textures/items/compass_05.png"));
            var0.put(new ResourceLocation("textures/item/compass_06.png"), new ResourceLocation("textures/items/compass_06.png"));
            var0.put(new ResourceLocation("textures/item/compass_07.png"), new ResourceLocation("textures/items/compass_07.png"));
            var0.put(new ResourceLocation("textures/item/compass_08.png"), new ResourceLocation("textures/items/compass_08.png"));
            var0.put(new ResourceLocation("textures/item/compass_09.png"), new ResourceLocation("textures/items/compass_09.png"));
            var0.put(new ResourceLocation("textures/item/compass_10.png"), new ResourceLocation("textures/items/compass_10.png"));
            var0.put(new ResourceLocation("textures/item/compass_11.png"), new ResourceLocation("textures/items/compass_11.png"));
            var0.put(new ResourceLocation("textures/item/compass_12.png"), new ResourceLocation("textures/items/compass_12.png"));
            var0.put(new ResourceLocation("textures/item/compass_13.png"), new ResourceLocation("textures/items/compass_13.png"));
            var0.put(new ResourceLocation("textures/item/compass_14.png"), new ResourceLocation("textures/items/compass_14.png"));
            var0.put(new ResourceLocation("textures/item/compass_15.png"), new ResourceLocation("textures/items/compass_15.png"));
            var0.put(new ResourceLocation("textures/item/compass_16.png"), new ResourceLocation("textures/items/compass_16.png"));
            var0.put(new ResourceLocation("textures/item/compass_17.png"), new ResourceLocation("textures/items/compass_17.png"));
            var0.put(new ResourceLocation("textures/item/compass_18.png"), new ResourceLocation("textures/items/compass_18.png"));
            var0.put(new ResourceLocation("textures/item/compass_19.png"), new ResourceLocation("textures/items/compass_19.png"));
            var0.put(new ResourceLocation("textures/item/compass_20.png"), new ResourceLocation("textures/items/compass_20.png"));
            var0.put(new ResourceLocation("textures/item/compass_21.png"), new ResourceLocation("textures/items/compass_21.png"));
            var0.put(new ResourceLocation("textures/item/compass_22.png"), new ResourceLocation("textures/items/compass_22.png"));
            var0.put(new ResourceLocation("textures/item/compass_23.png"), new ResourceLocation("textures/items/compass_23.png"));
            var0.put(new ResourceLocation("textures/item/compass_24.png"), new ResourceLocation("textures/items/compass_24.png"));
            var0.put(new ResourceLocation("textures/item/compass_25.png"), new ResourceLocation("textures/items/compass_25.png"));
            var0.put(new ResourceLocation("textures/item/compass_26.png"), new ResourceLocation("textures/items/compass_26.png"));
            var0.put(new ResourceLocation("textures/item/compass_27.png"), new ResourceLocation("textures/items/compass_27.png"));
            var0.put(new ResourceLocation("textures/item/compass_28.png"), new ResourceLocation("textures/items/compass_28.png"));
            var0.put(new ResourceLocation("textures/item/compass_29.png"), new ResourceLocation("textures/items/compass_29.png"));
            var0.put(new ResourceLocation("textures/item/compass_30.png"), new ResourceLocation("textures/items/compass_30.png"));
            var0.put(new ResourceLocation("textures/item/compass_31.png"), new ResourceLocation("textures/items/compass_31.png"));
            var0.put(new ResourceLocation("textures/item/cookie.png"), new ResourceLocation("textures/items/cookie.png"));
            var0.put(new ResourceLocation("textures/item/dark_oak_boat.png"), new ResourceLocation("textures/items/dark_oak_boat.png"));
            var0.put(new ResourceLocation("textures/item/diamond.png"), new ResourceLocation("textures/items/diamond.png"));
            var0.put(new ResourceLocation("textures/item/diamond_axe.png"), new ResourceLocation("textures/items/diamond_axe.png"));
            var0.put(new ResourceLocation("textures/item/diamond_boots.png"), new ResourceLocation("textures/items/diamond_boots.png"));
            var0.put(new ResourceLocation("textures/item/diamond_chestplate.png"), new ResourceLocation("textures/items/diamond_chestplate.png"));
            var0.put(new ResourceLocation("textures/item/diamond_helmet.png"), new ResourceLocation("textures/items/diamond_helmet.png"));
            var0.put(new ResourceLocation("textures/item/diamond_hoe.png"), new ResourceLocation("textures/items/diamond_hoe.png"));
            var0.put(new ResourceLocation("textures/item/diamond_horse_armor.png"), new ResourceLocation("textures/items/diamond_horse_armor.png"));
            var0.put(new ResourceLocation("textures/item/diamond_leggings.png"), new ResourceLocation("textures/items/diamond_leggings.png"));
            var0.put(new ResourceLocation("textures/item/diamond_pickaxe.png"), new ResourceLocation("textures/items/diamond_pickaxe.png"));
            var0.put(new ResourceLocation("textures/item/diamond_shovel.png"), new ResourceLocation("textures/items/diamond_shovel.png"));
            var0.put(new ResourceLocation("textures/item/diamond_sword.png"), new ResourceLocation("textures/items/diamond_sword.png"));
            var0.put(new ResourceLocation("textures/item/acacia_door.png"), new ResourceLocation("textures/items/door_acacia.png"));
            var0.put(new ResourceLocation("textures/item/birch_door.png"), new ResourceLocation("textures/items/door_birch.png"));
            var0.put(new ResourceLocation("textures/item/dark_oak_door.png"), new ResourceLocation("textures/items/door_dark_oak.png"));
            var0.put(new ResourceLocation("textures/item/iron_door.png"), new ResourceLocation("textures/items/door_iron.png"));
            var0.put(new ResourceLocation("textures/item/jungle_door.png"), new ResourceLocation("textures/items/door_jungle.png"));
            var0.put(new ResourceLocation("textures/item/spruce_door.png"), new ResourceLocation("textures/items/door_spruce.png"));
            var0.put(new ResourceLocation("textures/item/oak_door.png"), new ResourceLocation("textures/items/door_wood.png"));
            var0.put(new ResourceLocation("textures/item/dragon_breath.png"), new ResourceLocation("textures/items/dragon_breath.png"));
            var0.put(new ResourceLocation("textures/item/ink_sac.png"), new ResourceLocation("textures/items/dye_powder_black.png"));
            var0.put(new ResourceLocation("textures/item/lapis_lazuli.png"), new ResourceLocation("textures/items/dye_powder_blue.png"));
            var0.put(new ResourceLocation("textures/item/cocoa_beans.png"), new ResourceLocation("textures/items/dye_powder_brown.png"));
            var0.put(new ResourceLocation("textures/item/cyan_dye.png"), new ResourceLocation("textures/items/dye_powder_cyan.png"));
            var0.put(new ResourceLocation("textures/item/gray_dye.png"), new ResourceLocation("textures/items/dye_powder_gray.png"));
            var0.put(new ResourceLocation("textures/item/green_dye.png"), new ResourceLocation("textures/items/dye_powder_green.png"));
            var0.put(new ResourceLocation("textures/item/light_blue_dye.png"), new ResourceLocation("textures/items/dye_powder_light_blue.png"));
            var0.put(new ResourceLocation("textures/item/lime_dye.png"), new ResourceLocation("textures/items/dye_powder_lime.png"));
            var0.put(new ResourceLocation("textures/item/magenta_dye.png"), new ResourceLocation("textures/items/dye_powder_magenta.png"));
            var0.put(new ResourceLocation("textures/item/orange_dye.png"), new ResourceLocation("textures/items/dye_powder_orange.png"));
            var0.put(new ResourceLocation("textures/item/pink_dye.png"), new ResourceLocation("textures/items/dye_powder_pink.png"));
            var0.put(new ResourceLocation("textures/item/purple_dye.png"), new ResourceLocation("textures/items/dye_powder_purple.png"));
            var0.put(new ResourceLocation("textures/item/red_dye.png"), new ResourceLocation("textures/items/dye_powder_red.png"));
            var0.put(new ResourceLocation("textures/item/light_gray_dye.png"), new ResourceLocation("textures/items/dye_powder_silver.png"));
            var0.put(new ResourceLocation("textures/item/bone_meal.png"), new ResourceLocation("textures/items/dye_powder_white.png"));
            var0.put(new ResourceLocation("textures/item/yellow_dye.png"), new ResourceLocation("textures/items/dye_powder_yellow.png"));
            var0.put(new ResourceLocation("textures/item/egg.png"), new ResourceLocation("textures/items/egg.png"));
            var0.put(new ResourceLocation("textures/item/elytra.png"), new ResourceLocation("textures/items/elytra.png"));
            var0.put(new ResourceLocation("textures/item/emerald.png"), new ResourceLocation("textures/items/emerald.png"));
            var0.put(new ResourceLocation("textures/item/empty_armor_slot_boots.png"), new ResourceLocation("textures/items/empty_armor_slot_boots.png"));
            var0.put(
                new ResourceLocation("textures/item/empty_armor_slot_chestplate.png"), new ResourceLocation("textures/items/empty_armor_slot_chestplate.png")
            );
            var0.put(new ResourceLocation("textures/item/empty_armor_slot_helmet.png"), new ResourceLocation("textures/items/empty_armor_slot_helmet.png"));
            var0.put(new ResourceLocation("textures/item/empty_armor_slot_leggings.png"), new ResourceLocation("textures/items/empty_armor_slot_leggings.png"));
            var0.put(new ResourceLocation("textures/item/empty_armor_slot_shield.png"), new ResourceLocation("textures/items/empty_armor_slot_shield.png"));
            var0.put(new ResourceLocation("textures/item/end_crystal.png"), new ResourceLocation("textures/items/end_crystal.png"));
            var0.put(new ResourceLocation("textures/item/ender_eye.png"), new ResourceLocation("textures/items/ender_eye.png"));
            var0.put(new ResourceLocation("textures/item/ender_pearl.png"), new ResourceLocation("textures/items/ender_pearl.png"));
            var0.put(new ResourceLocation("textures/item/experience_bottle.png"), new ResourceLocation("textures/items/experience_bottle.png"));
            var0.put(new ResourceLocation("textures/item/feather.png"), new ResourceLocation("textures/items/feather.png"));
            var0.put(new ResourceLocation("textures/item/fire_charge.png"), new ResourceLocation("textures/items/fireball.png"));
            var0.put(new ResourceLocation("textures/item/firework_rocket.png"), new ResourceLocation("textures/items/fireworks.png"));
            var0.put(new ResourceLocation("textures/item/firework_star.png"), new ResourceLocation("textures/items/fireworks_charge.png"));
            var0.put(new ResourceLocation("textures/item/firework_star_overlay.png"), new ResourceLocation("textures/items/fireworks_charge_overlay.png"));
            var0.put(new ResourceLocation("textures/item/tropical_fish.png"), new ResourceLocation("textures/items/fish_clownfish_raw.png"));
            var0.put(new ResourceLocation("textures/item/cooked_cod.png"), new ResourceLocation("textures/items/fish_cod_cooked.png"));
            var0.put(new ResourceLocation("textures/item/cod.png"), new ResourceLocation("textures/items/fish_cod_raw.png"));
            var0.put(new ResourceLocation("textures/item/pufferfish.png"), new ResourceLocation("textures/items/fish_pufferfish_raw.png"));
            var0.put(new ResourceLocation("textures/item/cooked_salmon.png"), new ResourceLocation("textures/items/fish_salmon_cooked.png"));
            var0.put(new ResourceLocation("textures/item/salmon.png"), new ResourceLocation("textures/items/fish_salmon_raw.png"));
            var0.put(new ResourceLocation("textures/item/fishing_rod_cast.png"), new ResourceLocation("textures/items/fishing_rod_cast.png"));
            var0.put(new ResourceLocation("textures/item/fishing_rod.png"), new ResourceLocation("textures/items/fishing_rod_uncast.png"));
            var0.put(new ResourceLocation("textures/item/flint.png"), new ResourceLocation("textures/items/flint.png"));
            var0.put(new ResourceLocation("textures/item/flint_and_steel.png"), new ResourceLocation("textures/items/flint_and_steel.png"));
            var0.put(new ResourceLocation("textures/item/flower_pot.png"), new ResourceLocation("textures/items/flower_pot.png"));
            var0.put(new ResourceLocation("textures/item/ghast_tear.png"), new ResourceLocation("textures/items/ghast_tear.png"));
            var0.put(new ResourceLocation("textures/item/glowstone_dust.png"), new ResourceLocation("textures/items/glowstone_dust.png"));
            var0.put(new ResourceLocation("textures/item/golden_axe.png"), new ResourceLocation("textures/items/gold_axe.png"));
            var0.put(new ResourceLocation("textures/item/golden_boots.png"), new ResourceLocation("textures/items/gold_boots.png"));
            var0.put(new ResourceLocation("textures/item/golden_chestplate.png"), new ResourceLocation("textures/items/gold_chestplate.png"));
            var0.put(new ResourceLocation("textures/item/golden_helmet.png"), new ResourceLocation("textures/items/gold_helmet.png"));
            var0.put(new ResourceLocation("textures/item/golden_hoe.png"), new ResourceLocation("textures/items/gold_hoe.png"));
            var0.put(new ResourceLocation("textures/item/golden_horse_armor.png"), new ResourceLocation("textures/items/gold_horse_armor.png"));
            var0.put(new ResourceLocation("textures/item/gold_ingot.png"), new ResourceLocation("textures/items/gold_ingot.png"));
            var0.put(new ResourceLocation("textures/item/golden_leggings.png"), new ResourceLocation("textures/items/gold_leggings.png"));
            var0.put(new ResourceLocation("textures/item/gold_nugget.png"), new ResourceLocation("textures/items/gold_nugget.png"));
            var0.put(new ResourceLocation("textures/item/golden_pickaxe.png"), new ResourceLocation("textures/items/gold_pickaxe.png"));
            var0.put(new ResourceLocation("textures/item/golden_shovel.png"), new ResourceLocation("textures/items/gold_shovel.png"));
            var0.put(new ResourceLocation("textures/item/golden_sword.png"), new ResourceLocation("textures/items/gold_sword.png"));
            var0.put(new ResourceLocation("textures/item/gunpowder.png"), new ResourceLocation("textures/items/gunpowder.png"));
            var0.put(new ResourceLocation("textures/item/hopper.png"), new ResourceLocation("textures/items/hopper.png"));
            var0.put(new ResourceLocation("textures/item/iron_axe.png"), new ResourceLocation("textures/items/iron_axe.png"));
            var0.put(new ResourceLocation("textures/item/iron_boots.png"), new ResourceLocation("textures/items/iron_boots.png"));
            var0.put(new ResourceLocation("textures/item/iron_chestplate.png"), new ResourceLocation("textures/items/iron_chestplate.png"));
            var0.put(new ResourceLocation("textures/item/iron_helmet.png"), new ResourceLocation("textures/items/iron_helmet.png"));
            var0.put(new ResourceLocation("textures/item/iron_hoe.png"), new ResourceLocation("textures/items/iron_hoe.png"));
            var0.put(new ResourceLocation("textures/item/iron_horse_armor.png"), new ResourceLocation("textures/items/iron_horse_armor.png"));
            var0.put(new ResourceLocation("textures/item/iron_ingot.png"), new ResourceLocation("textures/items/iron_ingot.png"));
            var0.put(new ResourceLocation("textures/item/iron_leggings.png"), new ResourceLocation("textures/items/iron_leggings.png"));
            var0.put(new ResourceLocation("textures/item/iron_nugget.png"), new ResourceLocation("textures/items/iron_nugget.png"));
            var0.put(new ResourceLocation("textures/item/iron_pickaxe.png"), new ResourceLocation("textures/items/iron_pickaxe.png"));
            var0.put(new ResourceLocation("textures/item/iron_shovel.png"), new ResourceLocation("textures/items/iron_shovel.png"));
            var0.put(new ResourceLocation("textures/item/iron_sword.png"), new ResourceLocation("textures/items/iron_sword.png"));
            var0.put(new ResourceLocation("textures/item/item_frame.png"), new ResourceLocation("textures/items/item_frame.png"));
            var0.put(new ResourceLocation("textures/item/jungle_boat.png"), new ResourceLocation("textures/items/jungle_boat.png"));
            var0.put(new ResourceLocation("textures/item/knowledge_book.png"), new ResourceLocation("textures/items/knowledge_book.png"));
            var0.put(new ResourceLocation("textures/item/lead.png"), new ResourceLocation("textures/items/lead.png"));
            var0.put(new ResourceLocation("textures/item/leather.png"), new ResourceLocation("textures/items/leather.png"));
            var0.put(new ResourceLocation("textures/item/leather_boots.png"), new ResourceLocation("textures/items/leather_boots.png"));
            var0.put(new ResourceLocation("textures/item/leather_boots_overlay.png"), new ResourceLocation("textures/items/leather_boots_overlay.png"));
            var0.put(new ResourceLocation("textures/item/leather_chestplate.png"), new ResourceLocation("textures/items/leather_chestplate.png"));
            var0.put(
                new ResourceLocation("textures/item/leather_chestplate_overlay.png"), new ResourceLocation("textures/items/leather_chestplate_overlay.png")
            );
            var0.put(new ResourceLocation("textures/item/leather_helmet.png"), new ResourceLocation("textures/items/leather_helmet.png"));
            var0.put(new ResourceLocation("textures/item/leather_helmet_overlay.png"), new ResourceLocation("textures/items/leather_helmet_overlay.png"));
            var0.put(new ResourceLocation("textures/item/leather_leggings.png"), new ResourceLocation("textures/items/leather_leggings.png"));
            var0.put(new ResourceLocation("textures/item/leather_leggings_overlay.png"), new ResourceLocation("textures/items/leather_leggings_overlay.png"));
            var0.put(new ResourceLocation("textures/item/magma_cream.png"), new ResourceLocation("textures/items/magma_cream.png"));
            var0.put(new ResourceLocation("textures/item/map.png"), new ResourceLocation("textures/items/map_empty.png"));
            var0.put(new ResourceLocation("textures/item/filled_map.png"), new ResourceLocation("textures/items/map_filled.png"));
            var0.put(new ResourceLocation("textures/item/filled_map_markings.png"), new ResourceLocation("textures/items/map_filled_markings.png"));
            var0.put(new ResourceLocation("textures/item/melon_slice.png"), new ResourceLocation("textures/items/melon.png"));
            var0.put(new ResourceLocation("textures/item/glistering_melon_slice.png"), new ResourceLocation("textures/items/melon_speckled.png"));
            var0.put(new ResourceLocation("textures/item/chest_minecart.png"), new ResourceLocation("textures/items/minecart_chest.png"));
            var0.put(new ResourceLocation("textures/item/command_block_minecart.png"), new ResourceLocation("textures/items/minecart_command_block.png"));
            var0.put(new ResourceLocation("textures/item/furnace_minecart.png"), new ResourceLocation("textures/items/minecart_furnace.png"));
            var0.put(new ResourceLocation("textures/item/hopper_minecart.png"), new ResourceLocation("textures/items/minecart_hopper.png"));
            var0.put(new ResourceLocation("textures/item/minecart.png"), new ResourceLocation("textures/items/minecart_normal.png"));
            var0.put(new ResourceLocation("textures/item/tnt_minecart.png"), new ResourceLocation("textures/items/minecart_tnt.png"));
            var0.put(new ResourceLocation("textures/item/mushroom_stew.png"), new ResourceLocation("textures/items/mushroom_stew.png"));
            var0.put(new ResourceLocation("textures/item/cooked_mutton.png"), new ResourceLocation("textures/items/mutton_cooked.png"));
            var0.put(new ResourceLocation("textures/item/mutton.png"), new ResourceLocation("textures/items/mutton_raw.png"));
            var0.put(new ResourceLocation("textures/item/name_tag.png"), new ResourceLocation("textures/items/name_tag.png"));
            var0.put(new ResourceLocation("textures/item/nether_star.png"), new ResourceLocation("textures/items/nether_star.png"));
            var0.put(new ResourceLocation("textures/item/nether_wart.png"), new ResourceLocation("textures/items/nether_wart.png"));
            var0.put(new ResourceLocation("textures/item/nether_brick.png"), new ResourceLocation("textures/items/netherbrick.png"));
            var0.put(new ResourceLocation("textures/item/oak_boat.png"), new ResourceLocation("textures/items/oak_boat.png"));
            var0.put(new ResourceLocation("textures/item/painting.png"), new ResourceLocation("textures/items/painting.png"));
            var0.put(new ResourceLocation("textures/item/paper.png"), new ResourceLocation("textures/items/paper.png"));
            var0.put(new ResourceLocation("textures/item/cooked_porkchop.png"), new ResourceLocation("textures/items/porkchop_cooked.png"));
            var0.put(new ResourceLocation("textures/item/porkchop.png"), new ResourceLocation("textures/items/porkchop_raw.png"));
            var0.put(new ResourceLocation("textures/item/potato.png"), new ResourceLocation("textures/items/potato.png"));
            var0.put(new ResourceLocation("textures/item/baked_potato.png"), new ResourceLocation("textures/items/potato_baked.png"));
            var0.put(new ResourceLocation("textures/item/poisonous_potato.png"), new ResourceLocation("textures/items/potato_poisonous.png"));
            var0.put(new ResourceLocation("textures/item/potion.png"), new ResourceLocation("textures/items/potion_bottle_drinkable.png"));
            var0.put(new ResourceLocation("textures/item/glass_bottle.png"), new ResourceLocation("textures/items/potion_bottle_empty.png"));
            var0.put(new ResourceLocation("textures/item/lingering_potion.png"), new ResourceLocation("textures/items/potion_bottle_lingering.png"));
            var0.put(new ResourceLocation("textures/item/splash_potion.png"), new ResourceLocation("textures/items/potion_bottle_splash.png"));
            var0.put(new ResourceLocation("textures/item/potion_overlay.png"), new ResourceLocation("textures/items/potion_overlay.png"));
            var0.put(new ResourceLocation("textures/item/prismarine_crystals.png"), new ResourceLocation("textures/items/prismarine_crystals.png"));
            var0.put(new ResourceLocation("textures/item/prismarine_shard.png"), new ResourceLocation("textures/items/prismarine_shard.png"));
            var0.put(new ResourceLocation("textures/item/pumpkin_pie.png"), new ResourceLocation("textures/items/pumpkin_pie.png"));
            var0.put(new ResourceLocation("textures/item/quartz.png"), new ResourceLocation("textures/items/quartz.png"));
            var0.put(new ResourceLocation("textures/item/cooked_rabbit.png"), new ResourceLocation("textures/items/rabbit_cooked.png"));
            var0.put(new ResourceLocation("textures/item/rabbit_foot.png"), new ResourceLocation("textures/items/rabbit_foot.png"));
            var0.put(new ResourceLocation("textures/item/rabbit_hide.png"), new ResourceLocation("textures/items/rabbit_hide.png"));
            var0.put(new ResourceLocation("textures/item/rabbit.png"), new ResourceLocation("textures/items/rabbit_raw.png"));
            var0.put(new ResourceLocation("textures/item/rabbit_stew.png"), new ResourceLocation("textures/items/rabbit_stew.png"));
            var0.put(new ResourceLocation("textures/item/music_disc_11.png"), new ResourceLocation("textures/items/record_11.png"));
            var0.put(new ResourceLocation("textures/item/music_disc_13.png"), new ResourceLocation("textures/items/record_13.png"));
            var0.put(new ResourceLocation("textures/item/music_disc_blocks.png"), new ResourceLocation("textures/items/record_blocks.png"));
            var0.put(new ResourceLocation("textures/item/music_disc_cat.png"), new ResourceLocation("textures/items/record_cat.png"));
            var0.put(new ResourceLocation("textures/item/music_disc_chirp.png"), new ResourceLocation("textures/items/record_chirp.png"));
            var0.put(new ResourceLocation("textures/item/music_disc_far.png"), new ResourceLocation("textures/items/record_far.png"));
            var0.put(new ResourceLocation("textures/item/music_disc_mall.png"), new ResourceLocation("textures/items/record_mall.png"));
            var0.put(new ResourceLocation("textures/item/music_disc_mellohi.png"), new ResourceLocation("textures/items/record_mellohi.png"));
            var0.put(new ResourceLocation("textures/item/music_disc_stal.png"), new ResourceLocation("textures/items/record_stal.png"));
            var0.put(new ResourceLocation("textures/item/music_disc_strad.png"), new ResourceLocation("textures/items/record_strad.png"));
            var0.put(new ResourceLocation("textures/item/music_disc_wait.png"), new ResourceLocation("textures/items/record_wait.png"));
            var0.put(new ResourceLocation("textures/item/music_disc_ward.png"), new ResourceLocation("textures/items/record_ward.png"));
            var0.put(new ResourceLocation("textures/item/redstone.png"), new ResourceLocation("textures/items/redstone_dust.png"));
            var0.put(new ResourceLocation("textures/item/sugar_cane.png"), new ResourceLocation("textures/items/reeds.png"));
            var0.put(new ResourceLocation("textures/item/repeater.png"), new ResourceLocation("textures/items/repeater.png"));
            var0.put(new ResourceLocation("textures/item/rotten_flesh.png"), new ResourceLocation("textures/items/rotten_flesh.png"));
            var0.put(new ResourceLocation("textures/item/ruby.png"), new ResourceLocation("textures/items/ruby.png"));
            var0.put(new ResourceLocation("textures/item/saddle.png"), new ResourceLocation("textures/items/saddle.png"));
            var0.put(new ResourceLocation("textures/item/melon_seeds.png"), new ResourceLocation("textures/items/seeds_melon.png"));
            var0.put(new ResourceLocation("textures/item/pumpkin_seeds.png"), new ResourceLocation("textures/items/seeds_pumpkin.png"));
            var0.put(new ResourceLocation("textures/item/wheat_seeds.png"), new ResourceLocation("textures/items/seeds_wheat.png"));
            var0.put(new ResourceLocation("textures/item/shears.png"), new ResourceLocation("textures/items/shears.png"));
            var0.put(new ResourceLocation("textures/item/shulker_shell.png"), new ResourceLocation("textures/items/shulker_shell.png"));
            var0.put(new ResourceLocation("textures/item/sign.png"), new ResourceLocation("textures/items/sign.png"));
            var0.put(new ResourceLocation("textures/item/slime_ball.png"), new ResourceLocation("textures/items/slimeball.png"));
            var0.put(new ResourceLocation("textures/item/snowball.png"), new ResourceLocation("textures/items/snowball.png"));
            var0.put(new ResourceLocation("textures/item/spawn_egg.png"), new ResourceLocation("textures/items/spawn_egg.png"));
            var0.put(new ResourceLocation("textures/item/spawn_egg_overlay.png"), new ResourceLocation("textures/items/spawn_egg_overlay.png"));
            var0.put(new ResourceLocation("textures/item/spectral_arrow.png"), new ResourceLocation("textures/items/spectral_arrow.png"));
            var0.put(new ResourceLocation("textures/item/spider_eye.png"), new ResourceLocation("textures/items/spider_eye.png"));
            var0.put(new ResourceLocation("textures/item/fermented_spider_eye.png"), new ResourceLocation("textures/items/spider_eye_fermented.png"));
            var0.put(new ResourceLocation("textures/item/spruce_boat.png"), new ResourceLocation("textures/items/spruce_boat.png"));
            var0.put(new ResourceLocation("textures/item/stick.png"), new ResourceLocation("textures/items/stick.png"));
            var0.put(new ResourceLocation("textures/item/stone_axe.png"), new ResourceLocation("textures/items/stone_axe.png"));
            var0.put(new ResourceLocation("textures/item/stone_hoe.png"), new ResourceLocation("textures/items/stone_hoe.png"));
            var0.put(new ResourceLocation("textures/item/stone_pickaxe.png"), new ResourceLocation("textures/items/stone_pickaxe.png"));
            var0.put(new ResourceLocation("textures/item/stone_shovel.png"), new ResourceLocation("textures/items/stone_shovel.png"));
            var0.put(new ResourceLocation("textures/item/stone_sword.png"), new ResourceLocation("textures/items/stone_sword.png"));
            var0.put(new ResourceLocation("textures/item/string.png"), new ResourceLocation("textures/items/string.png"));
            var0.put(new ResourceLocation("textures/item/structure_void.png"), new ResourceLocation("textures/items/structure_void.png"));
            var0.put(new ResourceLocation("textures/item/sugar.png"), new ResourceLocation("textures/items/sugar.png"));
            var0.put(new ResourceLocation("textures/item/tipped_arrow_base.png"), new ResourceLocation("textures/items/tipped_arrow_base.png"));
            var0.put(new ResourceLocation("textures/item/tipped_arrow_head.png"), new ResourceLocation("textures/items/tipped_arrow_head.png"));
            var0.put(new ResourceLocation("textures/item/totem_of_undying.png"), new ResourceLocation("textures/items/totem.png"));
            var0.put(new ResourceLocation("textures/item/wheat.png"), new ResourceLocation("textures/items/wheat.png"));
            var0.put(new ResourceLocation("textures/item/wooden_axe.png"), new ResourceLocation("textures/items/wood_axe.png"));
            var0.put(new ResourceLocation("textures/item/wooden_hoe.png"), new ResourceLocation("textures/items/wood_hoe.png"));
            var0.put(new ResourceLocation("textures/item/wooden_pickaxe.png"), new ResourceLocation("textures/items/wood_pickaxe.png"));
            var0.put(new ResourceLocation("textures/item/wooden_shovel.png"), new ResourceLocation("textures/items/wood_shovel.png"));
            var0.put(new ResourceLocation("textures/item/wooden_sword.png"), new ResourceLocation("textures/items/wood_sword.png"));
            var0.put(new ResourceLocation("textures/item/armor_stand.png"), new ResourceLocation("textures/items/wooden_armorstand.png"));
            return var0.build();
        }
    );

    private static ResourceLocation toMetaLocation(ResourceLocation param0) {
        return new ResourceLocation(param0.getNamespace(), param0.getPath() + ".mcmeta");
    }

    public LegacyResourcePackAdapter(Pack param0, Map<ResourceLocation, ResourceLocation> param1) {
        this.source = param0;
        Builder<ResourceLocation, ResourceLocation> var0 = ImmutableMap.builder();

        for(Entry<ResourceLocation, ResourceLocation> var1 : param1.entrySet()) {
            var0.put(var1);
            var0.put(toMetaLocation(var1.getKey()), toMetaLocation(var1.getValue()));
        }

        this.patches = var0.build();
    }

    private ResourceLocation map(ResourceLocation param0) {
        return this.patches.getOrDefault(param0, param0);
    }

    @Override
    public InputStream getRootResource(String param0) throws IOException {
        return this.source.getRootResource(param0);
    }

    @Override
    public InputStream getResource(PackType param0, ResourceLocation param1) throws IOException {
        return this.source.getResource(param0, this.map(param1));
    }

    @Override
    public Collection<ResourceLocation> getResources(PackType param0, String param1, String param2, int param3, Predicate<String> param4) {
        return Collections.emptyList();
    }

    @Override
    public boolean hasResource(PackType param0, ResourceLocation param1) {
        return this.source.hasResource(param0, this.map(param1));
    }

    @Override
    public Set<String> getNamespaces(PackType param0) {
        return this.source.getNamespaces(param0);
    }

    @Nullable
    @Override
    public <T> T getMetadataSection(MetadataSectionSerializer<T> param0) throws IOException {
        return this.source.getMetadataSection(param0);
    }

    @Override
    public String getName() {
        return this.source.getName();
    }

    @Override
    public void close() throws IOException {
        this.source.close();
    }
}
