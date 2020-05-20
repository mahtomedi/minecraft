package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;
import net.minecraft.util.datafix.PackedBitStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChunkPalettedStorageFix extends DataFix {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final BitSet VIRTUAL = new BitSet(256);
    private static final BitSet FIX = new BitSet(256);
    private static final Dynamic<?> PUMPKIN = BlockStateData.parse("{Name:'minecraft:pumpkin'}");
    private static final Dynamic<?> SNOWY_PODZOL = BlockStateData.parse("{Name:'minecraft:podzol',Properties:{snowy:'true'}}");
    private static final Dynamic<?> SNOWY_GRASS = BlockStateData.parse("{Name:'minecraft:grass_block',Properties:{snowy:'true'}}");
    private static final Dynamic<?> SNOWY_MYCELIUM = BlockStateData.parse("{Name:'minecraft:mycelium',Properties:{snowy:'true'}}");
    private static final Dynamic<?> UPPER_SUNFLOWER = BlockStateData.parse("{Name:'minecraft:sunflower',Properties:{half:'upper'}}");
    private static final Dynamic<?> UPPER_LILAC = BlockStateData.parse("{Name:'minecraft:lilac',Properties:{half:'upper'}}");
    private static final Dynamic<?> UPPER_TALL_GRASS = BlockStateData.parse("{Name:'minecraft:tall_grass',Properties:{half:'upper'}}");
    private static final Dynamic<?> UPPER_LARGE_FERN = BlockStateData.parse("{Name:'minecraft:large_fern',Properties:{half:'upper'}}");
    private static final Dynamic<?> UPPER_ROSE_BUSH = BlockStateData.parse("{Name:'minecraft:rose_bush',Properties:{half:'upper'}}");
    private static final Dynamic<?> UPPER_PEONY = BlockStateData.parse("{Name:'minecraft:peony',Properties:{half:'upper'}}");
    private static final Map<String, Dynamic<?>> FLOWER_POT_MAP = DataFixUtils.make(Maps.newHashMap(), param0 -> {
        param0.put("minecraft:air0", BlockStateData.parse("{Name:'minecraft:flower_pot'}"));
        param0.put("minecraft:red_flower0", BlockStateData.parse("{Name:'minecraft:potted_poppy'}"));
        param0.put("minecraft:red_flower1", BlockStateData.parse("{Name:'minecraft:potted_blue_orchid'}"));
        param0.put("minecraft:red_flower2", BlockStateData.parse("{Name:'minecraft:potted_allium'}"));
        param0.put("minecraft:red_flower3", BlockStateData.parse("{Name:'minecraft:potted_azure_bluet'}"));
        param0.put("minecraft:red_flower4", BlockStateData.parse("{Name:'minecraft:potted_red_tulip'}"));
        param0.put("minecraft:red_flower5", BlockStateData.parse("{Name:'minecraft:potted_orange_tulip'}"));
        param0.put("minecraft:red_flower6", BlockStateData.parse("{Name:'minecraft:potted_white_tulip'}"));
        param0.put("minecraft:red_flower7", BlockStateData.parse("{Name:'minecraft:potted_pink_tulip'}"));
        param0.put("minecraft:red_flower8", BlockStateData.parse("{Name:'minecraft:potted_oxeye_daisy'}"));
        param0.put("minecraft:yellow_flower0", BlockStateData.parse("{Name:'minecraft:potted_dandelion'}"));
        param0.put("minecraft:sapling0", BlockStateData.parse("{Name:'minecraft:potted_oak_sapling'}"));
        param0.put("minecraft:sapling1", BlockStateData.parse("{Name:'minecraft:potted_spruce_sapling'}"));
        param0.put("minecraft:sapling2", BlockStateData.parse("{Name:'minecraft:potted_birch_sapling'}"));
        param0.put("minecraft:sapling3", BlockStateData.parse("{Name:'minecraft:potted_jungle_sapling'}"));
        param0.put("minecraft:sapling4", BlockStateData.parse("{Name:'minecraft:potted_acacia_sapling'}"));
        param0.put("minecraft:sapling5", BlockStateData.parse("{Name:'minecraft:potted_dark_oak_sapling'}"));
        param0.put("minecraft:red_mushroom0", BlockStateData.parse("{Name:'minecraft:potted_red_mushroom'}"));
        param0.put("minecraft:brown_mushroom0", BlockStateData.parse("{Name:'minecraft:potted_brown_mushroom'}"));
        param0.put("minecraft:deadbush0", BlockStateData.parse("{Name:'minecraft:potted_dead_bush'}"));
        param0.put("minecraft:tallgrass2", BlockStateData.parse("{Name:'minecraft:potted_fern'}"));
        param0.put("minecraft:cactus0", BlockStateData.getTag(2240));
    });
    private static final Map<String, Dynamic<?>> SKULL_MAP = DataFixUtils.make(Maps.newHashMap(), param0 -> {
        mapSkull(param0, 0, "skeleton", "skull");
        mapSkull(param0, 1, "wither_skeleton", "skull");
        mapSkull(param0, 2, "zombie", "head");
        mapSkull(param0, 3, "player", "head");
        mapSkull(param0, 4, "creeper", "head");
        mapSkull(param0, 5, "dragon", "head");
    });
    private static final Map<String, Dynamic<?>> DOOR_MAP = DataFixUtils.make(Maps.newHashMap(), param0 -> {
        mapDoor(param0, "oak_door", 1024);
        mapDoor(param0, "iron_door", 1136);
        mapDoor(param0, "spruce_door", 3088);
        mapDoor(param0, "birch_door", 3104);
        mapDoor(param0, "jungle_door", 3120);
        mapDoor(param0, "acacia_door", 3136);
        mapDoor(param0, "dark_oak_door", 3152);
    });
    private static final Map<String, Dynamic<?>> NOTE_BLOCK_MAP = DataFixUtils.make(Maps.newHashMap(), param0 -> {
        for(int var0 = 0; var0 < 26; ++var0) {
            param0.put("true" + var0, BlockStateData.parse("{Name:'minecraft:note_block',Properties:{powered:'true',note:'" + var0 + "'}}"));
            param0.put("false" + var0, BlockStateData.parse("{Name:'minecraft:note_block',Properties:{powered:'false',note:'" + var0 + "'}}"));
        }

    });
    private static final Int2ObjectMap<String> DYE_COLOR_MAP = DataFixUtils.make(new Int2ObjectOpenHashMap<>(), param0 -> {
        param0.put(0, "white");
        param0.put(1, "orange");
        param0.put(2, "magenta");
        param0.put(3, "light_blue");
        param0.put(4, "yellow");
        param0.put(5, "lime");
        param0.put(6, "pink");
        param0.put(7, "gray");
        param0.put(8, "light_gray");
        param0.put(9, "cyan");
        param0.put(10, "purple");
        param0.put(11, "blue");
        param0.put(12, "brown");
        param0.put(13, "green");
        param0.put(14, "red");
        param0.put(15, "black");
    });
    private static final Map<String, Dynamic<?>> BED_BLOCK_MAP = DataFixUtils.make(Maps.newHashMap(), param0 -> {
        for(Entry<String> var0 : DYE_COLOR_MAP.int2ObjectEntrySet()) {
            if (!Objects.equals(var0.getValue(), "red")) {
                addBeds(param0, var0.getIntKey(), var0.getValue());
            }
        }

    });
    private static final Map<String, Dynamic<?>> BANNER_BLOCK_MAP = DataFixUtils.make(Maps.newHashMap(), param0 -> {
        for(Entry<String> var0 : DYE_COLOR_MAP.int2ObjectEntrySet()) {
            if (!Objects.equals(var0.getValue(), "white")) {
                addBanners(param0, 15 - var0.getIntKey(), var0.getValue());
            }
        }

    });
    private static final Dynamic<?> AIR = BlockStateData.getTag(0);

    public ChunkPalettedStorageFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    private static void mapSkull(Map<String, Dynamic<?>> param0, int param1, String param2, String param3) {
        param0.put(param1 + "north", BlockStateData.parse("{Name:'minecraft:" + param2 + "_wall_" + param3 + "',Properties:{facing:'north'}}"));
        param0.put(param1 + "east", BlockStateData.parse("{Name:'minecraft:" + param2 + "_wall_" + param3 + "',Properties:{facing:'east'}}"));
        param0.put(param1 + "south", BlockStateData.parse("{Name:'minecraft:" + param2 + "_wall_" + param3 + "',Properties:{facing:'south'}}"));
        param0.put(param1 + "west", BlockStateData.parse("{Name:'minecraft:" + param2 + "_wall_" + param3 + "',Properties:{facing:'west'}}"));

        for(int var0 = 0; var0 < 16; ++var0) {
            param0.put(param1 + "" + var0, BlockStateData.parse("{Name:'minecraft:" + param2 + "_" + param3 + "',Properties:{rotation:'" + var0 + "'}}"));
        }

    }

    private static void mapDoor(Map<String, Dynamic<?>> param0, String param1, int param2) {
        param0.put(
            "minecraft:" + param1 + "eastlowerleftfalsefalse",
            BlockStateData.parse("{Name:'minecraft:" + param1 + "',Properties:{facing:'east',half:'lower',hinge:'left',open:'false',powered:'false'}}")
        );
        param0.put(
            "minecraft:" + param1 + "eastlowerleftfalsetrue",
            BlockStateData.parse("{Name:'minecraft:" + param1 + "',Properties:{facing:'east',half:'lower',hinge:'left',open:'false',powered:'true'}}")
        );
        param0.put(
            "minecraft:" + param1 + "eastlowerlefttruefalse",
            BlockStateData.parse("{Name:'minecraft:" + param1 + "',Properties:{facing:'east',half:'lower',hinge:'left',open:'true',powered:'false'}}")
        );
        param0.put(
            "minecraft:" + param1 + "eastlowerlefttruetrue",
            BlockStateData.parse("{Name:'minecraft:" + param1 + "',Properties:{facing:'east',half:'lower',hinge:'left',open:'true',powered:'true'}}")
        );
        param0.put("minecraft:" + param1 + "eastlowerrightfalsefalse", BlockStateData.getTag(param2));
        param0.put(
            "minecraft:" + param1 + "eastlowerrightfalsetrue",
            BlockStateData.parse("{Name:'minecraft:" + param1 + "',Properties:{facing:'east',half:'lower',hinge:'right',open:'false',powered:'true'}}")
        );
        param0.put("minecraft:" + param1 + "eastlowerrighttruefalse", BlockStateData.getTag(param2 + 4));
        param0.put(
            "minecraft:" + param1 + "eastlowerrighttruetrue",
            BlockStateData.parse("{Name:'minecraft:" + param1 + "',Properties:{facing:'east',half:'lower',hinge:'right',open:'true',powered:'true'}}")
        );
        param0.put("minecraft:" + param1 + "eastupperleftfalsefalse", BlockStateData.getTag(param2 + 8));
        param0.put("minecraft:" + param1 + "eastupperleftfalsetrue", BlockStateData.getTag(param2 + 10));
        param0.put(
            "minecraft:" + param1 + "eastupperlefttruefalse",
            BlockStateData.parse("{Name:'minecraft:" + param1 + "',Properties:{facing:'east',half:'upper',hinge:'left',open:'true',powered:'false'}}")
        );
        param0.put(
            "minecraft:" + param1 + "eastupperlefttruetrue",
            BlockStateData.parse("{Name:'minecraft:" + param1 + "',Properties:{facing:'east',half:'upper',hinge:'left',open:'true',powered:'true'}}")
        );
        param0.put("minecraft:" + param1 + "eastupperrightfalsefalse", BlockStateData.getTag(param2 + 9));
        param0.put("minecraft:" + param1 + "eastupperrightfalsetrue", BlockStateData.getTag(param2 + 11));
        param0.put(
            "minecraft:" + param1 + "eastupperrighttruefalse",
            BlockStateData.parse("{Name:'minecraft:" + param1 + "',Properties:{facing:'east',half:'upper',hinge:'right',open:'true',powered:'false'}}")
        );
        param0.put(
            "minecraft:" + param1 + "eastupperrighttruetrue",
            BlockStateData.parse("{Name:'minecraft:" + param1 + "',Properties:{facing:'east',half:'upper',hinge:'right',open:'true',powered:'true'}}")
        );
        param0.put(
            "minecraft:" + param1 + "northlowerleftfalsefalse",
            BlockStateData.parse("{Name:'minecraft:" + param1 + "',Properties:{facing:'north',half:'lower',hinge:'left',open:'false',powered:'false'}}")
        );
        param0.put(
            "minecraft:" + param1 + "northlowerleftfalsetrue",
            BlockStateData.parse("{Name:'minecraft:" + param1 + "',Properties:{facing:'north',half:'lower',hinge:'left',open:'false',powered:'true'}}")
        );
        param0.put(
            "minecraft:" + param1 + "northlowerlefttruefalse",
            BlockStateData.parse("{Name:'minecraft:" + param1 + "',Properties:{facing:'north',half:'lower',hinge:'left',open:'true',powered:'false'}}")
        );
        param0.put(
            "minecraft:" + param1 + "northlowerlefttruetrue",
            BlockStateData.parse("{Name:'minecraft:" + param1 + "',Properties:{facing:'north',half:'lower',hinge:'left',open:'true',powered:'true'}}")
        );
        param0.put("minecraft:" + param1 + "northlowerrightfalsefalse", BlockStateData.getTag(param2 + 3));
        param0.put(
            "minecraft:" + param1 + "northlowerrightfalsetrue",
            BlockStateData.parse("{Name:'minecraft:" + param1 + "',Properties:{facing:'north',half:'lower',hinge:'right',open:'false',powered:'true'}}")
        );
        param0.put("minecraft:" + param1 + "northlowerrighttruefalse", BlockStateData.getTag(param2 + 7));
        param0.put(
            "minecraft:" + param1 + "northlowerrighttruetrue",
            BlockStateData.parse("{Name:'minecraft:" + param1 + "',Properties:{facing:'north',half:'lower',hinge:'right',open:'true',powered:'true'}}")
        );
        param0.put(
            "minecraft:" + param1 + "northupperleftfalsefalse",
            BlockStateData.parse("{Name:'minecraft:" + param1 + "',Properties:{facing:'north',half:'upper',hinge:'left',open:'false',powered:'false'}}")
        );
        param0.put(
            "minecraft:" + param1 + "northupperleftfalsetrue",
            BlockStateData.parse("{Name:'minecraft:" + param1 + "',Properties:{facing:'north',half:'upper',hinge:'left',open:'false',powered:'true'}}")
        );
        param0.put(
            "minecraft:" + param1 + "northupperlefttruefalse",
            BlockStateData.parse("{Name:'minecraft:" + param1 + "',Properties:{facing:'north',half:'upper',hinge:'left',open:'true',powered:'false'}}")
        );
        param0.put(
            "minecraft:" + param1 + "northupperlefttruetrue",
            BlockStateData.parse("{Name:'minecraft:" + param1 + "',Properties:{facing:'north',half:'upper',hinge:'left',open:'true',powered:'true'}}")
        );
        param0.put(
            "minecraft:" + param1 + "northupperrightfalsefalse",
            BlockStateData.parse("{Name:'minecraft:" + param1 + "',Properties:{facing:'north',half:'upper',hinge:'right',open:'false',powered:'false'}}")
        );
        param0.put(
            "minecraft:" + param1 + "northupperrightfalsetrue",
            BlockStateData.parse("{Name:'minecraft:" + param1 + "',Properties:{facing:'north',half:'upper',hinge:'right',open:'false',powered:'true'}}")
        );
        param0.put(
            "minecraft:" + param1 + "northupperrighttruefalse",
            BlockStateData.parse("{Name:'minecraft:" + param1 + "',Properties:{facing:'north',half:'upper',hinge:'right',open:'true',powered:'false'}}")
        );
        param0.put(
            "minecraft:" + param1 + "northupperrighttruetrue",
            BlockStateData.parse("{Name:'minecraft:" + param1 + "',Properties:{facing:'north',half:'upper',hinge:'right',open:'true',powered:'true'}}")
        );
        param0.put(
            "minecraft:" + param1 + "southlowerleftfalsefalse",
            BlockStateData.parse("{Name:'minecraft:" + param1 + "',Properties:{facing:'south',half:'lower',hinge:'left',open:'false',powered:'false'}}")
        );
        param0.put(
            "minecraft:" + param1 + "southlowerleftfalsetrue",
            BlockStateData.parse("{Name:'minecraft:" + param1 + "',Properties:{facing:'south',half:'lower',hinge:'left',open:'false',powered:'true'}}")
        );
        param0.put(
            "minecraft:" + param1 + "southlowerlefttruefalse",
            BlockStateData.parse("{Name:'minecraft:" + param1 + "',Properties:{facing:'south',half:'lower',hinge:'left',open:'true',powered:'false'}}")
        );
        param0.put(
            "minecraft:" + param1 + "southlowerlefttruetrue",
            BlockStateData.parse("{Name:'minecraft:" + param1 + "',Properties:{facing:'south',half:'lower',hinge:'left',open:'true',powered:'true'}}")
        );
        param0.put("minecraft:" + param1 + "southlowerrightfalsefalse", BlockStateData.getTag(param2 + 1));
        param0.put(
            "minecraft:" + param1 + "southlowerrightfalsetrue",
            BlockStateData.parse("{Name:'minecraft:" + param1 + "',Properties:{facing:'south',half:'lower',hinge:'right',open:'false',powered:'true'}}")
        );
        param0.put("minecraft:" + param1 + "southlowerrighttruefalse", BlockStateData.getTag(param2 + 5));
        param0.put(
            "minecraft:" + param1 + "southlowerrighttruetrue",
            BlockStateData.parse("{Name:'minecraft:" + param1 + "',Properties:{facing:'south',half:'lower',hinge:'right',open:'true',powered:'true'}}")
        );
        param0.put(
            "minecraft:" + param1 + "southupperleftfalsefalse",
            BlockStateData.parse("{Name:'minecraft:" + param1 + "',Properties:{facing:'south',half:'upper',hinge:'left',open:'false',powered:'false'}}")
        );
        param0.put(
            "minecraft:" + param1 + "southupperleftfalsetrue",
            BlockStateData.parse("{Name:'minecraft:" + param1 + "',Properties:{facing:'south',half:'upper',hinge:'left',open:'false',powered:'true'}}")
        );
        param0.put(
            "minecraft:" + param1 + "southupperlefttruefalse",
            BlockStateData.parse("{Name:'minecraft:" + param1 + "',Properties:{facing:'south',half:'upper',hinge:'left',open:'true',powered:'false'}}")
        );
        param0.put(
            "minecraft:" + param1 + "southupperlefttruetrue",
            BlockStateData.parse("{Name:'minecraft:" + param1 + "',Properties:{facing:'south',half:'upper',hinge:'left',open:'true',powered:'true'}}")
        );
        param0.put(
            "minecraft:" + param1 + "southupperrightfalsefalse",
            BlockStateData.parse("{Name:'minecraft:" + param1 + "',Properties:{facing:'south',half:'upper',hinge:'right',open:'false',powered:'false'}}")
        );
        param0.put(
            "minecraft:" + param1 + "southupperrightfalsetrue",
            BlockStateData.parse("{Name:'minecraft:" + param1 + "',Properties:{facing:'south',half:'upper',hinge:'right',open:'false',powered:'true'}}")
        );
        param0.put(
            "minecraft:" + param1 + "southupperrighttruefalse",
            BlockStateData.parse("{Name:'minecraft:" + param1 + "',Properties:{facing:'south',half:'upper',hinge:'right',open:'true',powered:'false'}}")
        );
        param0.put(
            "minecraft:" + param1 + "southupperrighttruetrue",
            BlockStateData.parse("{Name:'minecraft:" + param1 + "',Properties:{facing:'south',half:'upper',hinge:'right',open:'true',powered:'true'}}")
        );
        param0.put(
            "minecraft:" + param1 + "westlowerleftfalsefalse",
            BlockStateData.parse("{Name:'minecraft:" + param1 + "',Properties:{facing:'west',half:'lower',hinge:'left',open:'false',powered:'false'}}")
        );
        param0.put(
            "minecraft:" + param1 + "westlowerleftfalsetrue",
            BlockStateData.parse("{Name:'minecraft:" + param1 + "',Properties:{facing:'west',half:'lower',hinge:'left',open:'false',powered:'true'}}")
        );
        param0.put(
            "minecraft:" + param1 + "westlowerlefttruefalse",
            BlockStateData.parse("{Name:'minecraft:" + param1 + "',Properties:{facing:'west',half:'lower',hinge:'left',open:'true',powered:'false'}}")
        );
        param0.put(
            "minecraft:" + param1 + "westlowerlefttruetrue",
            BlockStateData.parse("{Name:'minecraft:" + param1 + "',Properties:{facing:'west',half:'lower',hinge:'left',open:'true',powered:'true'}}")
        );
        param0.put("minecraft:" + param1 + "westlowerrightfalsefalse", BlockStateData.getTag(param2 + 2));
        param0.put(
            "minecraft:" + param1 + "westlowerrightfalsetrue",
            BlockStateData.parse("{Name:'minecraft:" + param1 + "',Properties:{facing:'west',half:'lower',hinge:'right',open:'false',powered:'true'}}")
        );
        param0.put("minecraft:" + param1 + "westlowerrighttruefalse", BlockStateData.getTag(param2 + 6));
        param0.put(
            "minecraft:" + param1 + "westlowerrighttruetrue",
            BlockStateData.parse("{Name:'minecraft:" + param1 + "',Properties:{facing:'west',half:'lower',hinge:'right',open:'true',powered:'true'}}")
        );
        param0.put(
            "minecraft:" + param1 + "westupperleftfalsefalse",
            BlockStateData.parse("{Name:'minecraft:" + param1 + "',Properties:{facing:'west',half:'upper',hinge:'left',open:'false',powered:'false'}}")
        );
        param0.put(
            "minecraft:" + param1 + "westupperleftfalsetrue",
            BlockStateData.parse("{Name:'minecraft:" + param1 + "',Properties:{facing:'west',half:'upper',hinge:'left',open:'false',powered:'true'}}")
        );
        param0.put(
            "minecraft:" + param1 + "westupperlefttruefalse",
            BlockStateData.parse("{Name:'minecraft:" + param1 + "',Properties:{facing:'west',half:'upper',hinge:'left',open:'true',powered:'false'}}")
        );
        param0.put(
            "minecraft:" + param1 + "westupperlefttruetrue",
            BlockStateData.parse("{Name:'minecraft:" + param1 + "',Properties:{facing:'west',half:'upper',hinge:'left',open:'true',powered:'true'}}")
        );
        param0.put(
            "minecraft:" + param1 + "westupperrightfalsefalse",
            BlockStateData.parse("{Name:'minecraft:" + param1 + "',Properties:{facing:'west',half:'upper',hinge:'right',open:'false',powered:'false'}}")
        );
        param0.put(
            "minecraft:" + param1 + "westupperrightfalsetrue",
            BlockStateData.parse("{Name:'minecraft:" + param1 + "',Properties:{facing:'west',half:'upper',hinge:'right',open:'false',powered:'true'}}")
        );
        param0.put(
            "minecraft:" + param1 + "westupperrighttruefalse",
            BlockStateData.parse("{Name:'minecraft:" + param1 + "',Properties:{facing:'west',half:'upper',hinge:'right',open:'true',powered:'false'}}")
        );
        param0.put(
            "minecraft:" + param1 + "westupperrighttruetrue",
            BlockStateData.parse("{Name:'minecraft:" + param1 + "',Properties:{facing:'west',half:'upper',hinge:'right',open:'true',powered:'true'}}")
        );
    }

    private static void addBeds(Map<String, Dynamic<?>> param0, int param1, String param2) {
        param0.put(
            "southfalsefoot" + param1, BlockStateData.parse("{Name:'minecraft:" + param2 + "_bed',Properties:{facing:'south',occupied:'false',part:'foot'}}")
        );
        param0.put(
            "westfalsefoot" + param1, BlockStateData.parse("{Name:'minecraft:" + param2 + "_bed',Properties:{facing:'west',occupied:'false',part:'foot'}}")
        );
        param0.put(
            "northfalsefoot" + param1, BlockStateData.parse("{Name:'minecraft:" + param2 + "_bed',Properties:{facing:'north',occupied:'false',part:'foot'}}")
        );
        param0.put(
            "eastfalsefoot" + param1, BlockStateData.parse("{Name:'minecraft:" + param2 + "_bed',Properties:{facing:'east',occupied:'false',part:'foot'}}")
        );
        param0.put(
            "southfalsehead" + param1, BlockStateData.parse("{Name:'minecraft:" + param2 + "_bed',Properties:{facing:'south',occupied:'false',part:'head'}}")
        );
        param0.put(
            "westfalsehead" + param1, BlockStateData.parse("{Name:'minecraft:" + param2 + "_bed',Properties:{facing:'west',occupied:'false',part:'head'}}")
        );
        param0.put(
            "northfalsehead" + param1, BlockStateData.parse("{Name:'minecraft:" + param2 + "_bed',Properties:{facing:'north',occupied:'false',part:'head'}}")
        );
        param0.put(
            "eastfalsehead" + param1, BlockStateData.parse("{Name:'minecraft:" + param2 + "_bed',Properties:{facing:'east',occupied:'false',part:'head'}}")
        );
        param0.put(
            "southtruehead" + param1, BlockStateData.parse("{Name:'minecraft:" + param2 + "_bed',Properties:{facing:'south',occupied:'true',part:'head'}}")
        );
        param0.put(
            "westtruehead" + param1, BlockStateData.parse("{Name:'minecraft:" + param2 + "_bed',Properties:{facing:'west',occupied:'true',part:'head'}}")
        );
        param0.put(
            "northtruehead" + param1, BlockStateData.parse("{Name:'minecraft:" + param2 + "_bed',Properties:{facing:'north',occupied:'true',part:'head'}}")
        );
        param0.put(
            "easttruehead" + param1, BlockStateData.parse("{Name:'minecraft:" + param2 + "_bed',Properties:{facing:'east',occupied:'true',part:'head'}}")
        );
    }

    private static void addBanners(Map<String, Dynamic<?>> param0, int param1, String param2) {
        for(int var0 = 0; var0 < 16; ++var0) {
            param0.put("" + var0 + "_" + param1, BlockStateData.parse("{Name:'minecraft:" + param2 + "_banner',Properties:{rotation:'" + var0 + "'}}"));
        }

        param0.put("north_" + param1, BlockStateData.parse("{Name:'minecraft:" + param2 + "_wall_banner',Properties:{facing:'north'}}"));
        param0.put("south_" + param1, BlockStateData.parse("{Name:'minecraft:" + param2 + "_wall_banner',Properties:{facing:'south'}}"));
        param0.put("west_" + param1, BlockStateData.parse("{Name:'minecraft:" + param2 + "_wall_banner',Properties:{facing:'west'}}"));
        param0.put("east_" + param1, BlockStateData.parse("{Name:'minecraft:" + param2 + "_wall_banner',Properties:{facing:'east'}}"));
    }

    public static String getName(Dynamic<?> param0) {
        return param0.get("Name").asString("");
    }

    public static String getProperty(Dynamic<?> param0, String param1) {
        return param0.get("Properties").get(param1).asString("");
    }

    public static int idFor(CrudeIncrementalIntIdentityHashBiMap<Dynamic<?>> param0, Dynamic<?> param1) {
        int var0 = param0.getId(param1);
        if (var0 == -1) {
            var0 = param0.add(param1);
        }

        return var0;
    }

    private Dynamic<?> fix(Dynamic<?> param0) {
        Optional<? extends Dynamic<?>> var0 = param0.get("Level").result();
        return var0.isPresent() && var0.get().get("Sections").asStreamOpt().result().isPresent()
            ? param0.set("Level", new ChunkPalettedStorageFix.UpgradeChunk(var0.get()).write())
            : param0;
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> var0 = this.getInputSchema().getType(References.CHUNK);
        Type<?> var1 = this.getOutputSchema().getType(References.CHUNK);
        return this.writeFixAndRead("ChunkPalettedStorageFix", var0, var1, this::fix);
    }

    public static int getSideMask(boolean param0, boolean param1, boolean param2, boolean param3) {
        int var0 = 0;
        if (param2) {
            if (param1) {
                var0 |= 2;
            } else if (param0) {
                var0 |= 128;
            } else {
                var0 |= 1;
            }
        } else if (param3) {
            if (param0) {
                var0 |= 32;
            } else if (param1) {
                var0 |= 8;
            } else {
                var0 |= 16;
            }
        } else if (param1) {
            var0 |= 4;
        } else if (param0) {
            var0 |= 64;
        }

        return var0;
    }

    static {
        FIX.set(2);
        FIX.set(3);
        FIX.set(110);
        FIX.set(140);
        FIX.set(144);
        FIX.set(25);
        FIX.set(86);
        FIX.set(26);
        FIX.set(176);
        FIX.set(177);
        FIX.set(175);
        FIX.set(64);
        FIX.set(71);
        FIX.set(193);
        FIX.set(194);
        FIX.set(195);
        FIX.set(196);
        FIX.set(197);
        VIRTUAL.set(54);
        VIRTUAL.set(146);
        VIRTUAL.set(25);
        VIRTUAL.set(26);
        VIRTUAL.set(51);
        VIRTUAL.set(53);
        VIRTUAL.set(67);
        VIRTUAL.set(108);
        VIRTUAL.set(109);
        VIRTUAL.set(114);
        VIRTUAL.set(128);
        VIRTUAL.set(134);
        VIRTUAL.set(135);
        VIRTUAL.set(136);
        VIRTUAL.set(156);
        VIRTUAL.set(163);
        VIRTUAL.set(164);
        VIRTUAL.set(180);
        VIRTUAL.set(203);
        VIRTUAL.set(55);
        VIRTUAL.set(85);
        VIRTUAL.set(113);
        VIRTUAL.set(188);
        VIRTUAL.set(189);
        VIRTUAL.set(190);
        VIRTUAL.set(191);
        VIRTUAL.set(192);
        VIRTUAL.set(93);
        VIRTUAL.set(94);
        VIRTUAL.set(101);
        VIRTUAL.set(102);
        VIRTUAL.set(160);
        VIRTUAL.set(106);
        VIRTUAL.set(107);
        VIRTUAL.set(183);
        VIRTUAL.set(184);
        VIRTUAL.set(185);
        VIRTUAL.set(186);
        VIRTUAL.set(187);
        VIRTUAL.set(132);
        VIRTUAL.set(139);
        VIRTUAL.set(199);
    }

    static class DataLayer {
        private final byte[] data;

        public DataLayer() {
            this.data = new byte[2048];
        }

        public DataLayer(byte[] param0) {
            this.data = param0;
            if (param0.length != 2048) {
                throw new IllegalArgumentException("ChunkNibbleArrays should be 2048 bytes not: " + param0.length);
            }
        }

        public int get(int param0, int param1, int param2) {
            int var0 = this.getPosition(param1 << 8 | param2 << 4 | param0);
            return this.isFirst(param1 << 8 | param2 << 4 | param0) ? this.data[var0] & 15 : this.data[var0] >> 4 & 15;
        }

        private boolean isFirst(int param0) {
            return (param0 & 1) == 0;
        }

        private int getPosition(int param0) {
            return param0 >> 1;
        }
    }

    public static enum Direction {
        DOWN(ChunkPalettedStorageFix.Direction.AxisDirection.NEGATIVE, ChunkPalettedStorageFix.Direction.Axis.Y),
        UP(ChunkPalettedStorageFix.Direction.AxisDirection.POSITIVE, ChunkPalettedStorageFix.Direction.Axis.Y),
        NORTH(ChunkPalettedStorageFix.Direction.AxisDirection.NEGATIVE, ChunkPalettedStorageFix.Direction.Axis.Z),
        SOUTH(ChunkPalettedStorageFix.Direction.AxisDirection.POSITIVE, ChunkPalettedStorageFix.Direction.Axis.Z),
        WEST(ChunkPalettedStorageFix.Direction.AxisDirection.NEGATIVE, ChunkPalettedStorageFix.Direction.Axis.X),
        EAST(ChunkPalettedStorageFix.Direction.AxisDirection.POSITIVE, ChunkPalettedStorageFix.Direction.Axis.X);

        private final ChunkPalettedStorageFix.Direction.Axis axis;
        private final ChunkPalettedStorageFix.Direction.AxisDirection axisDirection;

        private Direction(ChunkPalettedStorageFix.Direction.AxisDirection param0, ChunkPalettedStorageFix.Direction.Axis param1) {
            this.axis = param1;
            this.axisDirection = param0;
        }

        public ChunkPalettedStorageFix.Direction.AxisDirection getAxisDirection() {
            return this.axisDirection;
        }

        public ChunkPalettedStorageFix.Direction.Axis getAxis() {
            return this.axis;
        }

        public static enum Axis {
            X,
            Y,
            Z;
        }

        public static enum AxisDirection {
            POSITIVE(1),
            NEGATIVE(-1);

            private final int step;

            private AxisDirection(int param0) {
                this.step = param0;
            }

            public int getStep() {
                return this.step;
            }
        }
    }

    static class Section {
        private final CrudeIncrementalIntIdentityHashBiMap<Dynamic<?>> palette = new CrudeIncrementalIntIdentityHashBiMap<>(32);
        private final List<Dynamic<?>> listTag;
        private final Dynamic<?> section;
        private final boolean hasData;
        private final Int2ObjectMap<IntList> toFix = new Int2ObjectLinkedOpenHashMap<>();
        private final IntList update = new IntArrayList();
        public final int y;
        private final Set<Dynamic<?>> seen = Sets.newIdentityHashSet();
        private final int[] buffer = new int[4096];

        public Section(Dynamic<?> param0) {
            this.listTag = Lists.newArrayList();
            this.section = param0;
            this.y = param0.get("Y").asInt(0);
            this.hasData = param0.get("Blocks").result().isPresent();
        }

        public Dynamic<?> getBlock(int param0) {
            if (param0 >= 0 && param0 <= 4095) {
                Dynamic<?> var0 = this.palette.byId(this.buffer[param0]);
                return var0 == null ? ChunkPalettedStorageFix.AIR : var0;
            } else {
                return ChunkPalettedStorageFix.AIR;
            }
        }

        public void setBlock(int param0, Dynamic<?> param1) {
            if (this.seen.add(param1)) {
                this.listTag.add("%%FILTER_ME%%".equals(ChunkPalettedStorageFix.getName(param1)) ? ChunkPalettedStorageFix.AIR : param1);
            }

            this.buffer[param0] = ChunkPalettedStorageFix.idFor(this.palette, param1);
        }

        public int upgrade(int param0) {
            if (!this.hasData) {
                return param0;
            } else {
                ByteBuffer var0 = this.section.get("Blocks").asByteBufferOpt().result().get();
                ChunkPalettedStorageFix.DataLayer var1 = this.section
                    .get("Data")
                    .asByteBufferOpt()
                    .map(param0x -> new ChunkPalettedStorageFix.DataLayer(DataFixUtils.toArray(param0x)))
                    .result()
                    .orElseGet(ChunkPalettedStorageFix.DataLayer::new);
                ChunkPalettedStorageFix.DataLayer var2 = this.section
                    .get("Add")
                    .asByteBufferOpt()
                    .map(param0x -> new ChunkPalettedStorageFix.DataLayer(DataFixUtils.toArray(param0x)))
                    .result()
                    .orElseGet(ChunkPalettedStorageFix.DataLayer::new);
                this.seen.add(ChunkPalettedStorageFix.AIR);
                ChunkPalettedStorageFix.idFor(this.palette, ChunkPalettedStorageFix.AIR);
                this.listTag.add(ChunkPalettedStorageFix.AIR);

                for(int var3 = 0; var3 < 4096; ++var3) {
                    int var4 = var3 & 15;
                    int var5 = var3 >> 8 & 15;
                    int var6 = var3 >> 4 & 15;
                    int var7 = var2.get(var4, var5, var6) << 12 | (var0.get(var3) & 255) << 4 | var1.get(var4, var5, var6);
                    if (ChunkPalettedStorageFix.FIX.get(var7 >> 4)) {
                        this.addFix(var7 >> 4, var3);
                    }

                    if (ChunkPalettedStorageFix.VIRTUAL.get(var7 >> 4)) {
                        int var8 = ChunkPalettedStorageFix.getSideMask(var4 == 0, var4 == 15, var6 == 0, var6 == 15);
                        if (var8 == 0) {
                            this.update.add(var3);
                        } else {
                            param0 |= var8;
                        }
                    }

                    this.setBlock(var3, BlockStateData.getTag(var7));
                }

                return param0;
            }
        }

        private void addFix(int param0, int param1) {
            IntList var0 = this.toFix.get(param0);
            if (var0 == null) {
                var0 = new IntArrayList();
                this.toFix.put(param0, var0);
            }

            var0.add(param1);
        }

        public Dynamic<?> write() {
            Dynamic<?> var0 = this.section;
            if (!this.hasData) {
                return var0;
            } else {
                var0 = var0.set("Palette", var0.createList(this.listTag.stream()));
                int var1 = Math.max(4, DataFixUtils.ceillog2(this.seen.size()));
                PackedBitStorage var2 = new PackedBitStorage(var1, 4096);

                for(int var3 = 0; var3 < this.buffer.length; ++var3) {
                    var2.set(var3, this.buffer[var3]);
                }

                var0 = var0.set("BlockStates", var0.createLongList(Arrays.stream(var2.getRaw())));
                var0 = var0.remove("Blocks");
                var0 = var0.remove("Data");
                return var0.remove("Add");
            }
        }
    }

    static final class UpgradeChunk {
        private int sides;
        private final ChunkPalettedStorageFix.Section[] sections = new ChunkPalettedStorageFix.Section[16];
        private final Dynamic<?> level;
        private final int x;
        private final int z;
        private final Int2ObjectMap<Dynamic<?>> blockEntities = new Int2ObjectLinkedOpenHashMap<>(16);

        public UpgradeChunk(Dynamic<?> param0) {
            this.level = param0;
            this.x = param0.get("xPos").asInt(0) << 4;
            this.z = param0.get("zPos").asInt(0) << 4;
            param0.get("TileEntities")
                .asStreamOpt()
                .result()
                .ifPresent(
                    param0x -> param0x.forEach(
                            param0xx -> {
                                int var0x = param0xx.get("x").asInt(0) - this.x & 15;
                                int var1x = param0xx.get("y").asInt(0);
                                int var2x = param0xx.get("z").asInt(0) - this.z & 15;
                                int var3x = var1x << 8 | var2x << 4 | var0x;
                                if (this.blockEntities.put(var3x, param0xx) != null) {
                                    ChunkPalettedStorageFix.LOGGER
                                        .warn("In chunk: {}x{} found a duplicate block entity at position: [{}, {}, {}]", this.x, this.z, var0x, var1x, var2x);
                                }
            
                            }
                        )
                );
            boolean var0 = param0.get("convertedFromAlphaFormat").asBoolean(false);
            param0.get("Sections").asStreamOpt().result().ifPresent(param0x -> param0x.forEach(param0xx -> {
                    ChunkPalettedStorageFix.Section var0x = new ChunkPalettedStorageFix.Section(param0xx);
                    this.sides = var0x.upgrade(this.sides);
                    this.sections[var0x.y] = var0x;
                }));

            for(ChunkPalettedStorageFix.Section var1 : this.sections) {
                if (var1 != null) {
                    for(java.util.Map.Entry<Integer, IntList> var2 : var1.toFix.entrySet()) {
                        int var3 = var1.y << 12;
                        switch(var2.getKey()) {
                            case 2:
                                for(int var4 : var2.getValue()) {
                                    var4 |= var3;
                                    Dynamic<?> var5 = this.getBlock(var4);
                                    if ("minecraft:grass_block".equals(ChunkPalettedStorageFix.getName(var5))) {
                                        String var6 = ChunkPalettedStorageFix.getName(this.getBlock(relative(var4, ChunkPalettedStorageFix.Direction.UP)));
                                        if ("minecraft:snow".equals(var6) || "minecraft:snow_layer".equals(var6)) {
                                            this.setBlock(var4, ChunkPalettedStorageFix.SNOWY_GRASS);
                                        }
                                    }
                                }
                                break;
                            case 3:
                                for(int var7 : var2.getValue()) {
                                    var7 |= var3;
                                    Dynamic<?> var8 = this.getBlock(var7);
                                    if ("minecraft:podzol".equals(ChunkPalettedStorageFix.getName(var8))) {
                                        String var9 = ChunkPalettedStorageFix.getName(this.getBlock(relative(var7, ChunkPalettedStorageFix.Direction.UP)));
                                        if ("minecraft:snow".equals(var9) || "minecraft:snow_layer".equals(var9)) {
                                            this.setBlock(var7, ChunkPalettedStorageFix.SNOWY_PODZOL);
                                        }
                                    }
                                }
                                break;
                            case 25:
                                for(int var13 : var2.getValue()) {
                                    var13 |= var3;
                                    Dynamic<?> var14 = this.removeBlockEntity(var13);
                                    if (var14 != null) {
                                        String var15 = Boolean.toString(var14.get("powered").asBoolean(false))
                                            + (byte)Math.min(Math.max(var14.get("note").asInt(0), 0), 24);
                                        this.setBlock(
                                            var13,
                                            ChunkPalettedStorageFix.NOTE_BLOCK_MAP.getOrDefault(var15, ChunkPalettedStorageFix.NOTE_BLOCK_MAP.get("false0"))
                                        );
                                    }
                                }
                                break;
                            case 26:
                                for(int var16 : var2.getValue()) {
                                    var16 |= var3;
                                    Dynamic<?> var17 = this.getBlockEntity(var16);
                                    Dynamic<?> var18 = this.getBlock(var16);
                                    if (var17 != null) {
                                        int var19 = var17.get("color").asInt(0);
                                        if (var19 != 14 && var19 >= 0 && var19 < 16) {
                                            String var20 = ChunkPalettedStorageFix.getProperty(var18, "facing")
                                                + ChunkPalettedStorageFix.getProperty(var18, "occupied")
                                                + ChunkPalettedStorageFix.getProperty(var18, "part")
                                                + var19;
                                            if (ChunkPalettedStorageFix.BED_BLOCK_MAP.containsKey(var20)) {
                                                this.setBlock(var16, ChunkPalettedStorageFix.BED_BLOCK_MAP.get(var20));
                                            }
                                        }
                                    }
                                }
                                break;
                            case 64:
                            case 71:
                            case 193:
                            case 194:
                            case 195:
                            case 196:
                            case 197:
                                for(int var38 : var2.getValue()) {
                                    var38 |= var3;
                                    Dynamic<?> var39 = this.getBlock(var38);
                                    if (ChunkPalettedStorageFix.getName(var39).endsWith("_door")) {
                                        Dynamic<?> var40 = this.getBlock(var38);
                                        if ("lower".equals(ChunkPalettedStorageFix.getProperty(var40, "half"))) {
                                            int var41 = relative(var38, ChunkPalettedStorageFix.Direction.UP);
                                            Dynamic<?> var42 = this.getBlock(var41);
                                            String var43 = ChunkPalettedStorageFix.getName(var40);
                                            if (var43.equals(ChunkPalettedStorageFix.getName(var42))) {
                                                String var44 = ChunkPalettedStorageFix.getProperty(var40, "facing");
                                                String var45 = ChunkPalettedStorageFix.getProperty(var40, "open");
                                                String var46 = var0 ? "left" : ChunkPalettedStorageFix.getProperty(var42, "hinge");
                                                String var47 = var0 ? "false" : ChunkPalettedStorageFix.getProperty(var42, "powered");
                                                this.setBlock(var38, ChunkPalettedStorageFix.DOOR_MAP.get(var43 + var44 + "lower" + var46 + var45 + var47));
                                                this.setBlock(var41, ChunkPalettedStorageFix.DOOR_MAP.get(var43 + var44 + "upper" + var46 + var45 + var47));
                                            }
                                        }
                                    }
                                }
                                break;
                            case 86:
                                for(int var26 : var2.getValue()) {
                                    var26 |= var3;
                                    Dynamic<?> var27 = this.getBlock(var26);
                                    if ("minecraft:carved_pumpkin".equals(ChunkPalettedStorageFix.getName(var27))) {
                                        String var28 = ChunkPalettedStorageFix.getName(this.getBlock(relative(var26, ChunkPalettedStorageFix.Direction.DOWN)));
                                        if ("minecraft:grass_block".equals(var28) || "minecraft:dirt".equals(var28)) {
                                            this.setBlock(var26, ChunkPalettedStorageFix.PUMPKIN);
                                        }
                                    }
                                }
                                break;
                            case 110:
                                for(int var10 : var2.getValue()) {
                                    var10 |= var3;
                                    Dynamic<?> var11 = this.getBlock(var10);
                                    if ("minecraft:mycelium".equals(ChunkPalettedStorageFix.getName(var11))) {
                                        String var12 = ChunkPalettedStorageFix.getName(this.getBlock(relative(var10, ChunkPalettedStorageFix.Direction.UP)));
                                        if ("minecraft:snow".equals(var12) || "minecraft:snow_layer".equals(var12)) {
                                            this.setBlock(var10, ChunkPalettedStorageFix.SNOWY_MYCELIUM);
                                        }
                                    }
                                }
                                break;
                            case 140:
                                for(int var29 : var2.getValue()) {
                                    var29 |= var3;
                                    Dynamic<?> var30 = this.removeBlockEntity(var29);
                                    if (var30 != null) {
                                        String var31 = var30.get("Item").asString("") + var30.get("Data").asInt(0);
                                        this.setBlock(
                                            var29,
                                            ChunkPalettedStorageFix.FLOWER_POT_MAP
                                                .getOrDefault(var31, ChunkPalettedStorageFix.FLOWER_POT_MAP.get("minecraft:air0"))
                                        );
                                    }
                                }
                                break;
                            case 144:
                                for(int var32 : var2.getValue()) {
                                    var32 |= var3;
                                    Dynamic<?> var33 = this.getBlockEntity(var32);
                                    if (var33 != null) {
                                        String var34 = String.valueOf(var33.get("SkullType").asInt(0));
                                        String var35 = ChunkPalettedStorageFix.getProperty(this.getBlock(var32), "facing");
                                        String var37;
                                        if (!"up".equals(var35) && !"down".equals(var35)) {
                                            var37 = var34 + var35;
                                        } else {
                                            var37 = var34 + String.valueOf(var33.get("Rot").asInt(0));
                                        }

                                        var33.remove("SkullType");
                                        var33.remove("facing");
                                        var33.remove("Rot");
                                        this.setBlock(
                                            var32, ChunkPalettedStorageFix.SKULL_MAP.getOrDefault(var37, ChunkPalettedStorageFix.SKULL_MAP.get("0north"))
                                        );
                                    }
                                }
                                break;
                            case 175:
                                for(int var48 : var2.getValue()) {
                                    var48 |= var3;
                                    Dynamic<?> var49 = this.getBlock(var48);
                                    if ("upper".equals(ChunkPalettedStorageFix.getProperty(var49, "half"))) {
                                        Dynamic<?> var50 = this.getBlock(relative(var48, ChunkPalettedStorageFix.Direction.DOWN));
                                        String var51 = ChunkPalettedStorageFix.getName(var50);
                                        if ("minecraft:sunflower".equals(var51)) {
                                            this.setBlock(var48, ChunkPalettedStorageFix.UPPER_SUNFLOWER);
                                        } else if ("minecraft:lilac".equals(var51)) {
                                            this.setBlock(var48, ChunkPalettedStorageFix.UPPER_LILAC);
                                        } else if ("minecraft:tall_grass".equals(var51)) {
                                            this.setBlock(var48, ChunkPalettedStorageFix.UPPER_TALL_GRASS);
                                        } else if ("minecraft:large_fern".equals(var51)) {
                                            this.setBlock(var48, ChunkPalettedStorageFix.UPPER_LARGE_FERN);
                                        } else if ("minecraft:rose_bush".equals(var51)) {
                                            this.setBlock(var48, ChunkPalettedStorageFix.UPPER_ROSE_BUSH);
                                        } else if ("minecraft:peony".equals(var51)) {
                                            this.setBlock(var48, ChunkPalettedStorageFix.UPPER_PEONY);
                                        }
                                    }
                                }
                                break;
                            case 176:
                            case 177:
                                for(int var21 : var2.getValue()) {
                                    var21 |= var3;
                                    Dynamic<?> var22 = this.getBlockEntity(var21);
                                    Dynamic<?> var23 = this.getBlock(var21);
                                    if (var22 != null) {
                                        int var24 = var22.get("Base").asInt(0);
                                        if (var24 != 15 && var24 >= 0 && var24 < 16) {
                                            String var25 = ChunkPalettedStorageFix.getProperty(var23, var2.getKey() == 176 ? "rotation" : "facing")
                                                + "_"
                                                + var24;
                                            if (ChunkPalettedStorageFix.BANNER_BLOCK_MAP.containsKey(var25)) {
                                                this.setBlock(var21, ChunkPalettedStorageFix.BANNER_BLOCK_MAP.get(var25));
                                            }
                                        }
                                    }
                                }
                        }
                    }
                }
            }

        }

        @Nullable
        private Dynamic<?> getBlockEntity(int param0) {
            return this.blockEntities.get(param0);
        }

        @Nullable
        private Dynamic<?> removeBlockEntity(int param0) {
            return this.blockEntities.remove(param0);
        }

        public static int relative(int param0, ChunkPalettedStorageFix.Direction param1) {
            switch(param1.getAxis()) {
                case X:
                    int var0 = (param0 & 15) + param1.getAxisDirection().getStep();
                    return var0 >= 0 && var0 <= 15 ? param0 & -16 | var0 : -1;
                case Y:
                    int var1 = (param0 >> 8) + param1.getAxisDirection().getStep();
                    return var1 >= 0 && var1 <= 255 ? param0 & 0xFF | var1 << 8 : -1;
                case Z:
                    int var2 = (param0 >> 4 & 15) + param1.getAxisDirection().getStep();
                    return var2 >= 0 && var2 <= 15 ? param0 & -241 | var2 << 4 : -1;
                default:
                    return -1;
            }
        }

        private void setBlock(int param0, Dynamic<?> param1) {
            if (param0 >= 0 && param0 <= 65535) {
                ChunkPalettedStorageFix.Section var0 = this.getSection(param0);
                if (var0 != null) {
                    var0.setBlock(param0 & 4095, param1);
                }
            }
        }

        @Nullable
        private ChunkPalettedStorageFix.Section getSection(int param0) {
            int var0 = param0 >> 12;
            return var0 < this.sections.length ? this.sections[var0] : null;
        }

        public Dynamic<?> getBlock(int param0) {
            if (param0 >= 0 && param0 <= 65535) {
                ChunkPalettedStorageFix.Section var0 = this.getSection(param0);
                return var0 == null ? ChunkPalettedStorageFix.AIR : var0.getBlock(param0 & 4095);
            } else {
                return ChunkPalettedStorageFix.AIR;
            }
        }

        public Dynamic<?> write() {
            Dynamic<?> var0 = this.level;
            if (this.blockEntities.isEmpty()) {
                var0 = var0.remove("TileEntities");
            } else {
                var0 = var0.set("TileEntities", var0.createList(this.blockEntities.values().stream()));
            }

            Dynamic<?> var1 = var0.emptyMap();
            List<Dynamic<?>> var2 = Lists.newArrayList();

            for(ChunkPalettedStorageFix.Section var3 : this.sections) {
                if (var3 != null) {
                    var2.add(var3.write());
                    var1 = var1.set(String.valueOf(var3.y), var1.createIntList(Arrays.stream(var3.update.toIntArray())));
                }
            }

            Dynamic<?> var4 = var0.emptyMap();
            var4 = var4.set("Sides", var4.createByte((byte)this.sides));
            var4 = var4.set("Indices", var1);
            return var0.set("UpgradeData", var4).set("Sections", var4.createList(var2.stream()));
        }
    }
}
