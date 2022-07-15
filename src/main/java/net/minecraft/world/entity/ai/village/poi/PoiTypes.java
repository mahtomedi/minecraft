package net.minecraft.world.entity.ai.village.poi;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;

public class PoiTypes {
    public static final ResourceKey<PoiType> ARMORER = createKey("armorer");
    public static final ResourceKey<PoiType> BUTCHER = createKey("butcher");
    public static final ResourceKey<PoiType> CARTOGRAPHER = createKey("cartographer");
    public static final ResourceKey<PoiType> CLERIC = createKey("cleric");
    public static final ResourceKey<PoiType> FARMER = createKey("farmer");
    public static final ResourceKey<PoiType> FISHERMAN = createKey("fisherman");
    public static final ResourceKey<PoiType> FLETCHER = createKey("fletcher");
    public static final ResourceKey<PoiType> LEATHERWORKER = createKey("leatherworker");
    public static final ResourceKey<PoiType> LIBRARIAN = createKey("librarian");
    public static final ResourceKey<PoiType> MASON = createKey("mason");
    public static final ResourceKey<PoiType> SHEPHERD = createKey("shepherd");
    public static final ResourceKey<PoiType> TOOLSMITH = createKey("toolsmith");
    public static final ResourceKey<PoiType> WEAPONSMITH = createKey("weaponsmith");
    public static final ResourceKey<PoiType> HOME = createKey("home");
    public static final ResourceKey<PoiType> MEETING = createKey("meeting");
    public static final ResourceKey<PoiType> BEEHIVE = createKey("beehive");
    public static final ResourceKey<PoiType> BEE_NEST = createKey("bee_nest");
    public static final ResourceKey<PoiType> NETHER_PORTAL = createKey("nether_portal");
    public static final ResourceKey<PoiType> LODESTONE = createKey("lodestone");
    public static final ResourceKey<PoiType> LIGHTNING_ROD = createKey("lightning_rod");
    private static final Set<BlockState> BEDS = ImmutableList.of(
            Blocks.RED_BED,
            Blocks.BLACK_BED,
            Blocks.BLUE_BED,
            Blocks.BROWN_BED,
            Blocks.CYAN_BED,
            Blocks.GRAY_BED,
            Blocks.GREEN_BED,
            Blocks.LIGHT_BLUE_BED,
            Blocks.LIGHT_GRAY_BED,
            Blocks.LIME_BED,
            Blocks.MAGENTA_BED,
            Blocks.ORANGE_BED,
            Blocks.PINK_BED,
            Blocks.PURPLE_BED,
            Blocks.WHITE_BED,
            Blocks.YELLOW_BED
        )
        .stream()
        .flatMap(param0 -> param0.getStateDefinition().getPossibleStates().stream())
        .filter(param0 -> param0.getValue(BedBlock.PART) == BedPart.HEAD)
        .collect(ImmutableSet.toImmutableSet());
    private static final Set<BlockState> CAULDRONS = ImmutableList.of(Blocks.CAULDRON, Blocks.LAVA_CAULDRON, Blocks.WATER_CAULDRON, Blocks.POWDER_SNOW_CAULDRON)
        .stream()
        .flatMap(param0 -> param0.getStateDefinition().getPossibleStates().stream())
        .collect(ImmutableSet.toImmutableSet());
    private static final Map<BlockState, Holder<PoiType>> TYPE_BY_STATE = Maps.newHashMap();
    protected static final Set<BlockState> ALL_STATES = new ObjectOpenHashSet<>(TYPE_BY_STATE.keySet());

    private static Set<BlockState> getBlockStates(Block param0) {
        return ImmutableSet.copyOf(param0.getStateDefinition().getPossibleStates());
    }

    private static ResourceKey<PoiType> createKey(String param0) {
        return ResourceKey.create(Registry.POINT_OF_INTEREST_TYPE_REGISTRY, new ResourceLocation(param0));
    }

    private static PoiType register(Registry<PoiType> param0, ResourceKey<PoiType> param1, Set<BlockState> param2, int param3, int param4) {
        PoiType var0 = new PoiType(param2, param3, param4);
        Registry.register(param0, param1, var0);
        registerBlockStates(param0.getHolderOrThrow(param1));
        return var0;
    }

    private static void registerBlockStates(Holder<PoiType> param0) {
        param0.value()
            .matchingStates()
            .forEach(
                param1 -> {
                    Holder<PoiType> var0x = TYPE_BY_STATE.put(param1, param0);
                    if (var0x != null) {
                        throw (IllegalStateException)Util.pauseInIde(
                            new IllegalStateException(String.format(Locale.ROOT, "%s is defined in more than one PoI type", param1))
                        );
                    }
                }
            );
    }

    public static Optional<Holder<PoiType>> forState(BlockState param0) {
        return Optional.ofNullable(TYPE_BY_STATE.get(param0));
    }

    public static PoiType bootstrap(Registry<PoiType> param0) {
        register(param0, ARMORER, getBlockStates(Blocks.BLAST_FURNACE), 1, 1);
        register(param0, BUTCHER, getBlockStates(Blocks.SMOKER), 1, 1);
        register(param0, CARTOGRAPHER, getBlockStates(Blocks.CARTOGRAPHY_TABLE), 1, 1);
        register(param0, CLERIC, getBlockStates(Blocks.BREWING_STAND), 1, 1);
        register(param0, FARMER, getBlockStates(Blocks.COMPOSTER), 1, 1);
        register(param0, FISHERMAN, getBlockStates(Blocks.BARREL), 1, 1);
        register(param0, FLETCHER, getBlockStates(Blocks.FLETCHING_TABLE), 1, 1);
        register(param0, LEATHERWORKER, CAULDRONS, 1, 1);
        register(param0, LIBRARIAN, getBlockStates(Blocks.LECTERN), 1, 1);
        register(param0, MASON, getBlockStates(Blocks.STONECUTTER), 1, 1);
        register(param0, SHEPHERD, getBlockStates(Blocks.LOOM), 1, 1);
        register(param0, TOOLSMITH, getBlockStates(Blocks.SMITHING_TABLE), 1, 1);
        register(param0, WEAPONSMITH, getBlockStates(Blocks.GRINDSTONE), 1, 1);
        register(param0, HOME, BEDS, 1, 1);
        register(param0, MEETING, getBlockStates(Blocks.BELL), 32, 6);
        register(param0, BEEHIVE, getBlockStates(Blocks.BEEHIVE), 0, 1);
        register(param0, BEE_NEST, getBlockStates(Blocks.BEE_NEST), 0, 1);
        register(param0, NETHER_PORTAL, getBlockStates(Blocks.NETHER_PORTAL), 0, 1);
        register(param0, LODESTONE, getBlockStates(Blocks.LODESTONE), 0, 1);
        return register(param0, LIGHTNING_ROD, getBlockStates(Blocks.LIGHTNING_ROD), 0, 1);
    }
}
