package net.minecraft.world.level.gameevent;

import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class GameEvent {
    public static final GameEvent BLOCK_ACTIVATE = register("block_activate");
    public static final GameEvent BLOCK_ATTACH = register("block_attach");
    public static final GameEvent BLOCK_CHANGE = register("block_change");
    public static final GameEvent BLOCK_CLOSE = register("block_close");
    public static final GameEvent BLOCK_DEACTIVATE = register("block_deactivate");
    public static final GameEvent BLOCK_DESTROY = register("block_destroy");
    public static final GameEvent BLOCK_DETACH = register("block_detach");
    public static final GameEvent BLOCK_OPEN = register("block_open");
    public static final GameEvent BLOCK_PLACE = register("block_place");
    public static final GameEvent CONTAINER_CLOSE = register("container_close");
    public static final GameEvent CONTAINER_OPEN = register("container_open");
    public static final GameEvent DISPENSE_FAIL = register("dispense_fail");
    public static final GameEvent DRINK = register("drink");
    public static final GameEvent EAT = register("eat");
    public static final GameEvent ELYTRA_GLIDE = register("elytra_glide");
    public static final GameEvent ENTITY_DAMAGE = register("entity_damage");
    public static final GameEvent ENTITY_DIE = register("entity_die");
    public static final GameEvent ENTITY_INTERACT = register("entity_interact");
    public static final GameEvent ENTITY_PLACE = register("entity_place");
    public static final GameEvent ENTITY_ROAR = register("entity_roar");
    public static final GameEvent ENTITY_SHAKE = register("entity_shake");
    public static final GameEvent EQUIP = register("equip");
    public static final GameEvent EXPLODE = register("explode");
    public static final GameEvent FLAP = register("flap");
    public static final GameEvent FLUID_PICKUP = register("fluid_pickup");
    public static final GameEvent FLUID_PLACE = register("fluid_place");
    public static final GameEvent HIT_GROUND = register("hit_ground");
    public static final GameEvent INSTRUMENT_PLAY = register("instrument_play");
    public static final GameEvent ITEM_INTERACT_FINISH = register("item_interact_finish");
    public static final GameEvent ITEM_INTERACT_START = register("item_interact_start");
    public static final GameEvent JUKEBOX_PLAY = register("jukebox_play", 10);
    public static final GameEvent JUKEBOX_STOP_PLAY = register("jukebox_stop_play", 10);
    public static final GameEvent LIGHTNING_STRIKE = register("lightning_strike");
    public static final GameEvent NOTE_BLOCK_PLAY = register("note_block_play");
    public static final GameEvent PISTON_CONTRACT = register("piston_contract");
    public static final GameEvent PISTON_EXTEND = register("piston_extend");
    public static final GameEvent PRIME_FUSE = register("prime_fuse");
    public static final GameEvent PROJECTILE_LAND = register("projectile_land");
    public static final GameEvent PROJECTILE_SHOOT = register("projectile_shoot");
    public static final GameEvent SCULK_SENSOR_TENDRILS_CLICKING = register("sculk_sensor_tendrils_clicking");
    public static final GameEvent SHEAR = register("shear");
    public static final GameEvent SHRIEK = register("shriek", 32);
    public static final GameEvent SPLASH = register("splash");
    public static final GameEvent STEP = register("step");
    public static final GameEvent SWIM = register("swim");
    public static final GameEvent TELEPORT = register("teleport");
    public static final int DEFAULT_NOTIFICATION_RADIUS = 16;
    private final String name;
    private final int notificationRadius;
    private final Holder.Reference<GameEvent> builtInRegistryHolder = Registry.GAME_EVENT.createIntrusiveHolder(this);

    public GameEvent(String param0, int param1) {
        this.name = param0;
        this.notificationRadius = param1;
    }

    public String getName() {
        return this.name;
    }

    public int getNotificationRadius() {
        return this.notificationRadius;
    }

    private static GameEvent register(String param0) {
        return register(param0, 16);
    }

    private static GameEvent register(String param0, int param1) {
        return Registry.register(Registry.GAME_EVENT, param0, new GameEvent(param0, param1));
    }

    @Override
    public String toString() {
        return "Game Event{ " + this.name + " , " + this.notificationRadius + "}";
    }

    @Deprecated
    public Holder.Reference<GameEvent> builtInRegistryHolder() {
        return this.builtInRegistryHolder;
    }

    public boolean is(TagKey<GameEvent> param0) {
        return this.builtInRegistryHolder.is(param0);
    }

    public static record Context(@Nullable Entity sourceEntity, @Nullable BlockState affectedState) {
        public static GameEvent.Context of(@Nullable Entity param0) {
            return new GameEvent.Context(param0, null);
        }

        public static GameEvent.Context of(@Nullable BlockState param0) {
            return new GameEvent.Context(null, param0);
        }

        public static GameEvent.Context of(@Nullable Entity param0, @Nullable BlockState param1) {
            return new GameEvent.Context(param0, param1);
        }
    }

    public static final class ListenerInfo implements Comparable<GameEvent.ListenerInfo> {
        private final GameEvent gameEvent;
        private final Vec3 source;
        private final GameEvent.Context context;
        private final GameEventListener recipient;
        private final double distanceToRecipient;

        public ListenerInfo(GameEvent param0, Vec3 param1, GameEvent.Context param2, GameEventListener param3, Vec3 param4) {
            this.gameEvent = param0;
            this.source = param1;
            this.context = param2;
            this.recipient = param3;
            this.distanceToRecipient = param1.distanceToSqr(param4);
        }

        public int compareTo(GameEvent.ListenerInfo param0) {
            return Double.compare(this.distanceToRecipient, param0.distanceToRecipient);
        }

        public GameEvent gameEvent() {
            return this.gameEvent;
        }

        public Vec3 source() {
            return this.source;
        }

        public GameEvent.Context context() {
            return this.context;
        }

        public GameEventListener recipient() {
            return this.recipient;
        }
    }
}
