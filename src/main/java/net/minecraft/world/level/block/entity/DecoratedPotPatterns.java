package net.minecraft.world.level.block.entity;

import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class DecoratedPotPatterns {
    public static final String BASE_NAME = "decorated_pot_base";
    public static final ResourceKey<String> BASE = create("decorated_pot_base");
    public static final String BRICK_NAME = "decorated_pot_side";
    public static final String ARCHER_NAME = "pottery_pattern_archer";
    public static final String PRIZE_NAME = "pottery_pattern_prize";
    public static final String ARMS_UP_NAME = "pottery_pattern_arms_up";
    public static final String SKULL_NAME = "pottery_pattern_skull";
    public static final ResourceKey<String> BRICK = create("decorated_pot_side");
    public static final ResourceKey<String> ARCHER = create("pottery_pattern_archer");
    public static final ResourceKey<String> PRIZE = create("pottery_pattern_prize");
    public static final ResourceKey<String> ARMS_UP = create("pottery_pattern_arms_up");
    public static final ResourceKey<String> SKULL = create("pottery_pattern_skull");
    private static final Map<Item, ResourceKey<String>> ITEM_TO_POT_TEXTURE = Map.ofEntries(
        new Entry[]{
            Map.entry(Items.POTTERY_SHARD_ARCHER, ARCHER),
            Map.entry(Items.POTTERY_SHARD_PRIZE, PRIZE),
            Map.entry(Items.POTTERY_SHARD_ARMS_UP, ARMS_UP),
            Map.entry(Items.POTTERY_SHARD_SKULL, SKULL),
            Map.entry(Items.BRICK, BRICK)
        }
    );

    private static ResourceKey<String> create(String param0) {
        return ResourceKey.create(Registries.DECORATED_POT_PATTERNS, new ResourceLocation(param0));
    }

    public static ResourceLocation location(ResourceKey<String> param0) {
        return param0.location().withPrefix("entity/decorated_pot/");
    }

    @Nullable
    public static ResourceKey<String> getResourceKey(Item param0) {
        return ITEM_TO_POT_TEXTURE.get(param0);
    }

    public static String bootstrap(Registry<String> param0) {
        Registry.register(param0, ARCHER, "pottery_pattern_archer");
        Registry.register(param0, PRIZE, "pottery_pattern_prize");
        Registry.register(param0, ARMS_UP, "pottery_pattern_arms_up");
        Registry.register(param0, SKULL, "pottery_pattern_skull");
        Registry.register(param0, BRICK, "decorated_pot_side");
        return Registry.register(param0, BASE, "decorated_pot_base");
    }
}
