package net.minecraft.world.entity.ai.village.poi;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;

public class PoiType {
    private static final Predicate<PoiType> ALL_JOBS = param0 -> Registry.VILLAGER_PROFESSION
            .stream()
            .map(VillagerProfession::getJobPoiType)
            .collect(Collectors.toSet())
            .contains(param0);
    public static final Predicate<PoiType> ALL = param0 -> true;
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
    private static final Map<BlockState, PoiType> TYPE_BY_STATE = Maps.newHashMap();
    public static final PoiType UNEMPLOYED = register("unemployed", ImmutableSet.of(), 1, null, ALL_JOBS, 1);
    public static final PoiType ARMORER = register("armorer", getBlockStates(Blocks.BLAST_FURNACE), 1, SoundEvents.VILLAGER_WORK_ARMORER, 1);
    public static final PoiType BUTCHER = register("butcher", getBlockStates(Blocks.SMOKER), 1, SoundEvents.VILLAGER_WORK_BUTCHER, 1);
    public static final PoiType CARTOGRAPHER = register("cartographer", getBlockStates(Blocks.CARTOGRAPHY_TABLE), 1, SoundEvents.VILLAGER_WORK_CARTOGRAPHER, 1);
    public static final PoiType CLERIC = register("cleric", getBlockStates(Blocks.BREWING_STAND), 1, SoundEvents.VILLAGER_WORK_CLERIC, 1);
    public static final PoiType FARMER = register("farmer", getBlockStates(Blocks.COMPOSTER), 1, SoundEvents.VILLAGER_WORK_FARMER, 1);
    public static final PoiType FISHERMAN = register("fisherman", getBlockStates(Blocks.BARREL), 1, SoundEvents.VILLAGER_WORK_FISHERMAN, 1);
    public static final PoiType FLETCHER = register("fletcher", getBlockStates(Blocks.FLETCHING_TABLE), 1, SoundEvents.VILLAGER_WORK_FLETCHER, 1);
    public static final PoiType LEATHERWORKER = register("leatherworker", getBlockStates(Blocks.CAULDRON), 1, SoundEvents.VILLAGER_WORK_LEATHERWORKER, 1);
    public static final PoiType LIBRARIAN = register("librarian", getBlockStates(Blocks.LECTERN), 1, SoundEvents.VILLAGER_WORK_LIBRARIAN, 1);
    public static final PoiType MASON = register("mason", getBlockStates(Blocks.STONECUTTER), 1, SoundEvents.VILLAGER_WORK_MASON, 1);
    public static final PoiType NITWIT = register("nitwit", ImmutableSet.of(), 1, null, 1);
    public static final PoiType SHEPHERD = register("shepherd", getBlockStates(Blocks.LOOM), 1, SoundEvents.VILLAGER_WORK_SHEPHERD, 1);
    public static final PoiType TOOLSMITH = register("toolsmith", getBlockStates(Blocks.SMITHING_TABLE), 1, SoundEvents.VILLAGER_WORK_TOOLSMITH, 1);
    public static final PoiType WEAPONSMITH = register("weaponsmith", getBlockStates(Blocks.GRINDSTONE), 1, SoundEvents.VILLAGER_WORK_WEAPONSMITH, 1);
    public static final PoiType HOME = register("home", BEDS, 1, null, 1);
    public static final PoiType MEETING = register("meeting", getBlockStates(Blocks.BELL), 32, null, 6);
    private final String name;
    private final Set<BlockState> matchingStates;
    private final int maxTickets;
    @Nullable
    private final SoundEvent soundEvent;
    private final Predicate<PoiType> predicate;
    private final int validRange;

    private static Set<BlockState> getBlockStates(Block param0) {
        return ImmutableSet.copyOf(param0.getStateDefinition().getPossibleStates());
    }

    private PoiType(String param0, Set<BlockState> param1, int param2, @Nullable SoundEvent param3, Predicate<PoiType> param4, int param5) {
        this.name = param0;
        this.matchingStates = ImmutableSet.copyOf(param1);
        this.maxTickets = param2;
        this.soundEvent = param3;
        this.predicate = param4;
        this.validRange = param5;
    }

    private PoiType(String param0, Set<BlockState> param1, int param2, @Nullable SoundEvent param3, int param4) {
        this.name = param0;
        this.matchingStates = ImmutableSet.copyOf(param1);
        this.maxTickets = param2;
        this.soundEvent = param3;
        this.predicate = param0x -> param0x == this;
        this.validRange = param4;
    }

    public int getMaxTickets() {
        return this.maxTickets;
    }

    public Predicate<PoiType> getPredicate() {
        return this.predicate;
    }

    public int getValidRange() {
        return this.validRange;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Nullable
    public SoundEvent getUseSound() {
        return this.soundEvent;
    }

    private static PoiType register(String param0, Set<BlockState> param1, int param2, @Nullable SoundEvent param3, int param4) {
        return registerBlockStates(Registry.POINT_OF_INTEREST_TYPE.register(new ResourceLocation(param0), new PoiType(param0, param1, param2, param3, param4)));
    }

    private static PoiType register(String param0, Set<BlockState> param1, int param2, @Nullable SoundEvent param3, Predicate<PoiType> param4, int param5) {
        return registerBlockStates(
            Registry.POINT_OF_INTEREST_TYPE.register(new ResourceLocation(param0), new PoiType(param0, param1, param2, param3, param4, param5))
        );
    }

    private static PoiType registerBlockStates(PoiType param0) {
        param0.matchingStates.forEach(param1 -> {
            PoiType var0x = TYPE_BY_STATE.put(param1, param0);
            if (var0x != null) {
                throw new IllegalStateException(String.format("%s is defined in too many tags", param1));
            }
        });
        return param0;
    }

    public static Optional<PoiType> forState(BlockState param0) {
        return Optional.ofNullable(TYPE_BY_STATE.get(param0));
    }

    public static Stream<BlockState> allPoiStates() {
        return TYPE_BY_STATE.keySet().stream();
    }
}
