package net.minecraft.tags;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.gameevent.GameEvent;

public class GameEventTags {
    public static final TagKey<GameEvent> VIBRATIONS = create("vibrations");
    public static final TagKey<GameEvent> WARDEN_EVENTS_CAN_LISTEN = create("warden_events_can_listen");
    public static final TagKey<GameEvent> IGNORE_VIBRATIONS_SNEAKING = create("ignore_vibrations_sneaking");
    public static final TagKey<GameEvent> IGNORE_VIBRATIONS_ON_OCCLUDING_BLOCK = create("ignore_vibrations_on_occluding_block");

    private static TagKey<GameEvent> create(String param0) {
        return TagKey.create(Registry.GAME_EVENT_REGISTRY, new ResourceLocation(param0));
    }
}
