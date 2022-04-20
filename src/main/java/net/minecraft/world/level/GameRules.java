package net.minecraft.world.level;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicLike;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

public class GameRules {
    public static final int DEFAULT_RANDOM_TICK_SPEED = 3;
    static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<GameRules.Key<?>, GameRules.Type<?>> GAME_RULE_TYPES = Maps.newTreeMap(Comparator.comparing(param0 -> param0.id));
    public static final GameRules.Key<GameRules.BooleanValue> RULE_DOFIRETICK = register(
        "doFireTick", GameRules.Category.UPDATES, GameRules.BooleanValue.create(true)
    );
    public static final GameRules.Key<GameRules.BooleanValue> RULE_MOBGRIEFING = register(
        "mobGriefing", GameRules.Category.MOBS, GameRules.BooleanValue.create(true)
    );
    public static final GameRules.Key<GameRules.BooleanValue> RULE_KEEPINVENTORY = register(
        "keepInventory", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false)
    );
    public static final GameRules.Key<GameRules.BooleanValue> RULE_DOMOBSPAWNING = register(
        "doMobSpawning", GameRules.Category.SPAWNING, GameRules.BooleanValue.create(true)
    );
    public static final GameRules.Key<GameRules.BooleanValue> RULE_DOMOBLOOT = register(
        "doMobLoot", GameRules.Category.DROPS, GameRules.BooleanValue.create(true)
    );
    public static final GameRules.Key<GameRules.BooleanValue> RULE_DOBLOCKDROPS = register(
        "doTileDrops", GameRules.Category.DROPS, GameRules.BooleanValue.create(true)
    );
    public static final GameRules.Key<GameRules.BooleanValue> RULE_DOENTITYDROPS = register(
        "doEntityDrops", GameRules.Category.DROPS, GameRules.BooleanValue.create(true)
    );
    public static final GameRules.Key<GameRules.BooleanValue> RULE_COMMANDBLOCKOUTPUT = register(
        "commandBlockOutput", GameRules.Category.CHAT, GameRules.BooleanValue.create(true)
    );
    public static final GameRules.Key<GameRules.BooleanValue> RULE_NATURAL_REGENERATION = register(
        "naturalRegeneration", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true)
    );
    public static final GameRules.Key<GameRules.BooleanValue> RULE_DAYLIGHT = register(
        "doDaylightCycle", GameRules.Category.UPDATES, GameRules.BooleanValue.create(true)
    );
    public static final GameRules.Key<GameRules.BooleanValue> RULE_LOGADMINCOMMANDS = register(
        "logAdminCommands", GameRules.Category.CHAT, GameRules.BooleanValue.create(true)
    );
    public static final GameRules.Key<GameRules.BooleanValue> RULE_SHOWDEATHMESSAGES = register(
        "showDeathMessages", GameRules.Category.CHAT, GameRules.BooleanValue.create(true)
    );
    public static final GameRules.Key<GameRules.IntegerValue> RULE_RANDOMTICKING = register(
        "randomTickSpeed", GameRules.Category.UPDATES, GameRules.IntegerValue.create(3)
    );
    public static final GameRules.Key<GameRules.BooleanValue> RULE_SENDCOMMANDFEEDBACK = register(
        "sendCommandFeedback", GameRules.Category.CHAT, GameRules.BooleanValue.create(true)
    );
    public static final GameRules.Key<GameRules.BooleanValue> RULE_REDUCEDDEBUGINFO = register(
        "reducedDebugInfo", GameRules.Category.MISC, GameRules.BooleanValue.create(false, (param0, param1) -> {
            byte var0 = (byte)(param1.get() ? 22 : 23);
    
            for(ServerPlayer var1 : param0.getPlayerList().getPlayers()) {
                var1.connection.send(new ClientboundEntityEventPacket(var1, var0));
            }
    
        })
    );
    public static final GameRules.Key<GameRules.BooleanValue> RULE_SPECTATORSGENERATECHUNKS = register(
        "spectatorsGenerateChunks", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true)
    );
    public static final GameRules.Key<GameRules.IntegerValue> RULE_SPAWN_RADIUS = register(
        "spawnRadius", GameRules.Category.PLAYER, GameRules.IntegerValue.create(10)
    );
    public static final GameRules.Key<GameRules.BooleanValue> RULE_DISABLE_ELYTRA_MOVEMENT_CHECK = register(
        "disableElytraMovementCheck", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false)
    );
    public static final GameRules.Key<GameRules.IntegerValue> RULE_MAX_ENTITY_CRAMMING = register(
        "maxEntityCramming", GameRules.Category.MOBS, GameRules.IntegerValue.create(24)
    );
    public static final GameRules.Key<GameRules.BooleanValue> RULE_WEATHER_CYCLE = register(
        "doWeatherCycle", GameRules.Category.UPDATES, GameRules.BooleanValue.create(true)
    );
    public static final GameRules.Key<GameRules.BooleanValue> RULE_LIMITED_CRAFTING = register(
        "doLimitedCrafting", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false)
    );
    public static final GameRules.Key<GameRules.IntegerValue> RULE_MAX_COMMAND_CHAIN_LENGTH = register(
        "maxCommandChainLength", GameRules.Category.MISC, GameRules.IntegerValue.create(65536)
    );
    public static final GameRules.Key<GameRules.BooleanValue> RULE_ANNOUNCE_ADVANCEMENTS = register(
        "announceAdvancements", GameRules.Category.CHAT, GameRules.BooleanValue.create(true)
    );
    public static final GameRules.Key<GameRules.BooleanValue> RULE_DISABLE_RAIDS = register(
        "disableRaids", GameRules.Category.MOBS, GameRules.BooleanValue.create(false)
    );
    public static final GameRules.Key<GameRules.BooleanValue> RULE_DOINSOMNIA = register(
        "doInsomnia", GameRules.Category.SPAWNING, GameRules.BooleanValue.create(true)
    );
    public static final GameRules.Key<GameRules.BooleanValue> RULE_DO_IMMEDIATE_RESPAWN = register(
        "doImmediateRespawn", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false, (param0, param1) -> {
            for(ServerPlayer var0 : param0.getPlayerList().getPlayers()) {
                var0.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.IMMEDIATE_RESPAWN, param1.get() ? 1.0F : 0.0F));
            }
    
        })
    );
    public static final GameRules.Key<GameRules.BooleanValue> RULE_DROWNING_DAMAGE = register(
        "drowningDamage", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true)
    );
    public static final GameRules.Key<GameRules.BooleanValue> RULE_FALL_DAMAGE = register(
        "fallDamage", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true)
    );
    public static final GameRules.Key<GameRules.BooleanValue> RULE_FIRE_DAMAGE = register(
        "fireDamage", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true)
    );
    public static final GameRules.Key<GameRules.BooleanValue> RULE_FREEZE_DAMAGE = register(
        "freezeDamage", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true)
    );
    public static final GameRules.Key<GameRules.BooleanValue> RULE_DO_PATROL_SPAWNING = register(
        "doPatrolSpawning", GameRules.Category.SPAWNING, GameRules.BooleanValue.create(true)
    );
    public static final GameRules.Key<GameRules.BooleanValue> RULE_DO_TRADER_SPAWNING = register(
        "doTraderSpawning", GameRules.Category.SPAWNING, GameRules.BooleanValue.create(true)
    );
    public static final GameRules.Key<GameRules.BooleanValue> RULE_DO_WARDEN_SPAWNING = register(
        "doWardenSpawning", GameRules.Category.SPAWNING, GameRules.BooleanValue.create(true)
    );
    public static final GameRules.Key<GameRules.BooleanValue> RULE_FORGIVE_DEAD_PLAYERS = register(
        "forgiveDeadPlayers", GameRules.Category.MOBS, GameRules.BooleanValue.create(true)
    );
    public static final GameRules.Key<GameRules.BooleanValue> RULE_UNIVERSAL_ANGER = register(
        "universalAnger", GameRules.Category.MOBS, GameRules.BooleanValue.create(false)
    );
    public static final GameRules.Key<GameRules.IntegerValue> RULE_PLAYERS_SLEEPING_PERCENTAGE = register(
        "playersSleepingPercentage", GameRules.Category.PLAYER, GameRules.IntegerValue.create(100)
    );
    private final Map<GameRules.Key<?>, GameRules.Value<?>> rules;

    private static <T extends GameRules.Value<T>> GameRules.Key<T> register(String param0, GameRules.Category param1, GameRules.Type<T> param2) {
        GameRules.Key<T> var0 = new GameRules.Key<>(param0, param1);
        GameRules.Type<?> var1 = GAME_RULE_TYPES.put(var0, param2);
        if (var1 != null) {
            throw new IllegalStateException("Duplicate game rule registration for " + param0);
        } else {
            return var0;
        }
    }

    public GameRules(DynamicLike<?> param0) {
        this();
        this.loadFromTag(param0);
    }

    public GameRules() {
        this.rules = GAME_RULE_TYPES.entrySet().stream().collect(ImmutableMap.toImmutableMap(Entry::getKey, param0 -> param0.getValue().createRule()));
    }

    private GameRules(Map<GameRules.Key<?>, GameRules.Value<?>> param0) {
        this.rules = param0;
    }

    public <T extends GameRules.Value<T>> T getRule(GameRules.Key<T> param0) {
        return (T)this.rules.get(param0);
    }

    public CompoundTag createTag() {
        CompoundTag var0 = new CompoundTag();
        this.rules.forEach((param1, param2) -> var0.putString(param1.id, param2.serialize()));
        return var0;
    }

    private void loadFromTag(DynamicLike<?> param0) {
        this.rules.forEach((param1, param2) -> param0.get(param1.id).asString().result().ifPresent(param2::deserialize));
    }

    public GameRules copy() {
        return new GameRules(this.rules.entrySet().stream().collect(ImmutableMap.toImmutableMap(Entry::getKey, param0 -> param0.getValue().copy())));
    }

    public static void visitGameRuleTypes(GameRules.GameRuleTypeVisitor param0) {
        GAME_RULE_TYPES.forEach((param1, param2) -> callVisitorCap(param0, param1, param2));
    }

    private static <T extends GameRules.Value<T>> void callVisitorCap(GameRules.GameRuleTypeVisitor param0, GameRules.Key<?> param1, GameRules.Type<?> param2) {
        param0.visit(param1, param2);
        param2.callVisitor(param0, param1);
    }

    public void assignFrom(GameRules param0, @Nullable MinecraftServer param1) {
        param0.rules.keySet().forEach(param2 -> this.assignCap(param2, param0, param1));
    }

    private <T extends GameRules.Value<T>> void assignCap(GameRules.Key<T> param0, GameRules param1, @Nullable MinecraftServer param2) {
        T var0 = param1.getRule(param0);
        this.<T>getRule(param0).setFrom(var0, param2);
    }

    public boolean getBoolean(GameRules.Key<GameRules.BooleanValue> param0) {
        return this.getRule(param0).get();
    }

    public int getInt(GameRules.Key<GameRules.IntegerValue> param0) {
        return this.getRule(param0).get();
    }

    public static class BooleanValue extends GameRules.Value<GameRules.BooleanValue> {
        private boolean value;

        static GameRules.Type<GameRules.BooleanValue> create(boolean param0, BiConsumer<MinecraftServer, GameRules.BooleanValue> param1) {
            return new GameRules.Type<>(
                BoolArgumentType::bool, param1x -> new GameRules.BooleanValue(param1x, param0), param1, GameRules.GameRuleTypeVisitor::visitBoolean
            );
        }

        static GameRules.Type<GameRules.BooleanValue> create(boolean param0) {
            return create(param0, (param0x, param1) -> {
            });
        }

        public BooleanValue(GameRules.Type<GameRules.BooleanValue> param0, boolean param1) {
            super(param0);
            this.value = param1;
        }

        @Override
        protected void updateFromArgument(CommandContext<CommandSourceStack> param0, String param1) {
            this.value = BoolArgumentType.getBool(param0, param1);
        }

        public boolean get() {
            return this.value;
        }

        public void set(boolean param0, @Nullable MinecraftServer param1) {
            this.value = param0;
            this.onChanged(param1);
        }

        @Override
        public String serialize() {
            return Boolean.toString(this.value);
        }

        @Override
        protected void deserialize(String param0) {
            this.value = Boolean.parseBoolean(param0);
        }

        @Override
        public int getCommandResult() {
            return this.value ? 1 : 0;
        }

        protected GameRules.BooleanValue getSelf() {
            return this;
        }

        protected GameRules.BooleanValue copy() {
            return new GameRules.BooleanValue(this.type, this.value);
        }

        public void setFrom(GameRules.BooleanValue param0, @Nullable MinecraftServer param1) {
            this.value = param0.value;
            this.onChanged(param1);
        }
    }

    public static enum Category {
        PLAYER("gamerule.category.player"),
        MOBS("gamerule.category.mobs"),
        SPAWNING("gamerule.category.spawning"),
        DROPS("gamerule.category.drops"),
        UPDATES("gamerule.category.updates"),
        CHAT("gamerule.category.chat"),
        MISC("gamerule.category.misc");

        private final String descriptionId;

        private Category(String param0) {
            this.descriptionId = param0;
        }

        public String getDescriptionId() {
            return this.descriptionId;
        }
    }

    public interface GameRuleTypeVisitor {
        default <T extends GameRules.Value<T>> void visit(GameRules.Key<T> param0, GameRules.Type<T> param1) {
        }

        default void visitBoolean(GameRules.Key<GameRules.BooleanValue> param0, GameRules.Type<GameRules.BooleanValue> param1) {
        }

        default void visitInteger(GameRules.Key<GameRules.IntegerValue> param0, GameRules.Type<GameRules.IntegerValue> param1) {
        }
    }

    public static class IntegerValue extends GameRules.Value<GameRules.IntegerValue> {
        private int value;

        private static GameRules.Type<GameRules.IntegerValue> create(int param0, BiConsumer<MinecraftServer, GameRules.IntegerValue> param1) {
            return new GameRules.Type<>(
                IntegerArgumentType::integer, param1x -> new GameRules.IntegerValue(param1x, param0), param1, GameRules.GameRuleTypeVisitor::visitInteger
            );
        }

        static GameRules.Type<GameRules.IntegerValue> create(int param0) {
            return create(param0, (param0x, param1) -> {
            });
        }

        public IntegerValue(GameRules.Type<GameRules.IntegerValue> param0, int param1) {
            super(param0);
            this.value = param1;
        }

        @Override
        protected void updateFromArgument(CommandContext<CommandSourceStack> param0, String param1) {
            this.value = IntegerArgumentType.getInteger(param0, param1);
        }

        public int get() {
            return this.value;
        }

        public void set(int param0, @Nullable MinecraftServer param1) {
            this.value = param0;
            this.onChanged(param1);
        }

        @Override
        public String serialize() {
            return Integer.toString(this.value);
        }

        @Override
        protected void deserialize(String param0) {
            this.value = safeParse(param0);
        }

        public boolean tryDeserialize(String param0) {
            try {
                this.value = Integer.parseInt(param0);
                return true;
            } catch (NumberFormatException var3) {
                return false;
            }
        }

        private static int safeParse(String param0) {
            if (!param0.isEmpty()) {
                try {
                    return Integer.parseInt(param0);
                } catch (NumberFormatException var2) {
                    GameRules.LOGGER.warn("Failed to parse integer {}", param0);
                }
            }

            return 0;
        }

        @Override
        public int getCommandResult() {
            return this.value;
        }

        protected GameRules.IntegerValue getSelf() {
            return this;
        }

        protected GameRules.IntegerValue copy() {
            return new GameRules.IntegerValue(this.type, this.value);
        }

        public void setFrom(GameRules.IntegerValue param0, @Nullable MinecraftServer param1) {
            this.value = param0.value;
            this.onChanged(param1);
        }
    }

    public static final class Key<T extends GameRules.Value<T>> {
        final String id;
        private final GameRules.Category category;

        public Key(String param0, GameRules.Category param1) {
            this.id = param0;
            this.category = param1;
        }

        @Override
        public String toString() {
            return this.id;
        }

        @Override
        public boolean equals(Object param0) {
            if (this == param0) {
                return true;
            } else {
                return param0 instanceof GameRules.Key && ((GameRules.Key)param0).id.equals(this.id);
            }
        }

        @Override
        public int hashCode() {
            return this.id.hashCode();
        }

        public String getId() {
            return this.id;
        }

        public String getDescriptionId() {
            return "gamerule." + this.id;
        }

        public GameRules.Category getCategory() {
            return this.category;
        }
    }

    public static class Type<T extends GameRules.Value<T>> {
        private final Supplier<ArgumentType<?>> argument;
        private final Function<GameRules.Type<T>, T> constructor;
        final BiConsumer<MinecraftServer, T> callback;
        private final GameRules.VisitorCaller<T> visitorCaller;

        Type(Supplier<ArgumentType<?>> param0, Function<GameRules.Type<T>, T> param1, BiConsumer<MinecraftServer, T> param2, GameRules.VisitorCaller<T> param3) {
            this.argument = param0;
            this.constructor = param1;
            this.callback = param2;
            this.visitorCaller = param3;
        }

        public RequiredArgumentBuilder<CommandSourceStack, ?> createArgument(String param0) {
            return Commands.argument(param0, this.argument.get());
        }

        public T createRule() {
            return this.constructor.apply(this);
        }

        public void callVisitor(GameRules.GameRuleTypeVisitor param0, GameRules.Key<T> param1) {
            this.visitorCaller.call(param0, param1, this);
        }
    }

    public abstract static class Value<T extends GameRules.Value<T>> {
        protected final GameRules.Type<T> type;

        public Value(GameRules.Type<T> param0) {
            this.type = param0;
        }

        protected abstract void updateFromArgument(CommandContext<CommandSourceStack> var1, String var2);

        public void setFromArgument(CommandContext<CommandSourceStack> param0, String param1) {
            this.updateFromArgument(param0, param1);
            this.onChanged(param0.getSource().getServer());
        }

        protected void onChanged(@Nullable MinecraftServer param0) {
            if (param0 != null) {
                this.type.callback.accept(param0, this.getSelf());
            }

        }

        protected abstract void deserialize(String var1);

        public abstract String serialize();

        @Override
        public String toString() {
            return this.serialize();
        }

        public abstract int getCommandResult();

        protected abstract T getSelf();

        protected abstract T copy();

        public abstract void setFrom(T var1, @Nullable MinecraftServer var2);
    }

    interface VisitorCaller<T extends GameRules.Value<T>> {
        void call(GameRules.GameRuleTypeVisitor var1, GameRules.Key<T> var2, GameRules.Type<T> var3);
    }
}
