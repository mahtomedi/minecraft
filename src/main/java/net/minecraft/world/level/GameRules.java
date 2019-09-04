package net.minecraft.world.level;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GameRules {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<GameRules.Key<?>, GameRules.Type<?>> GAME_RULE_TYPES = Maps.newTreeMap(Comparator.comparing(param0 -> param0.id));
    public static final GameRules.Key<GameRules.BooleanValue> RULE_DOFIRETICK = register("doFireTick", GameRules.BooleanValue.create(true));
    public static final GameRules.Key<GameRules.BooleanValue> RULE_MOBGRIEFING = register("mobGriefing", GameRules.BooleanValue.create(true));
    public static final GameRules.Key<GameRules.BooleanValue> RULE_KEEPINVENTORY = register("keepInventory", GameRules.BooleanValue.create(false));
    public static final GameRules.Key<GameRules.BooleanValue> RULE_DOMOBSPAWNING = register("doMobSpawning", GameRules.BooleanValue.create(true));
    public static final GameRules.Key<GameRules.BooleanValue> RULE_DOMOBLOOT = register("doMobLoot", GameRules.BooleanValue.create(true));
    public static final GameRules.Key<GameRules.BooleanValue> RULE_DOBLOCKDROPS = register("doTileDrops", GameRules.BooleanValue.create(true));
    public static final GameRules.Key<GameRules.BooleanValue> RULE_DOENTITYDROPS = register("doEntityDrops", GameRules.BooleanValue.create(true));
    public static final GameRules.Key<GameRules.BooleanValue> RULE_COMMANDBLOCKOUTPUT = register("commandBlockOutput", GameRules.BooleanValue.create(true));
    public static final GameRules.Key<GameRules.BooleanValue> RULE_NATURAL_REGENERATION = register("naturalRegeneration", GameRules.BooleanValue.create(true));
    public static final GameRules.Key<GameRules.BooleanValue> RULE_DAYLIGHT = register("doDaylightCycle", GameRules.BooleanValue.create(true));
    public static final GameRules.Key<GameRules.BooleanValue> RULE_LOGADMINCOMMANDS = register("logAdminCommands", GameRules.BooleanValue.create(true));
    public static final GameRules.Key<GameRules.BooleanValue> RULE_SHOWDEATHMESSAGES = register("showDeathMessages", GameRules.BooleanValue.create(true));
    public static final GameRules.Key<GameRules.IntegerValue> RULE_RANDOMTICKING = register("randomTickSpeed", GameRules.IntegerValue.create(3));
    public static final GameRules.Key<GameRules.BooleanValue> RULE_SENDCOMMANDFEEDBACK = register("sendCommandFeedback", GameRules.BooleanValue.create(true));
    public static final GameRules.Key<GameRules.BooleanValue> RULE_REDUCEDDEBUGINFO = register(
        "reducedDebugInfo", GameRules.BooleanValue.create(false, (param0, param1) -> {
            byte var0 = (byte)(param1.get() ? 22 : 23);
    
            for(ServerPlayer var1 : param0.getPlayerList().getPlayers()) {
                var1.connection.send(new ClientboundEntityEventPacket(var1, var0));
            }
    
        })
    );
    public static final GameRules.Key<GameRules.BooleanValue> RULE_SPECTATORSGENERATECHUNKS = register(
        "spectatorsGenerateChunks", GameRules.BooleanValue.create(true)
    );
    public static final GameRules.Key<GameRules.IntegerValue> RULE_SPAWN_RADIUS = register("spawnRadius", GameRules.IntegerValue.create(10));
    public static final GameRules.Key<GameRules.BooleanValue> RULE_DISABLE_ELYTRA_MOVEMENT_CHECK = register(
        "disableElytraMovementCheck", GameRules.BooleanValue.create(false)
    );
    public static final GameRules.Key<GameRules.IntegerValue> RULE_MAX_ENTITY_CRAMMING = register("maxEntityCramming", GameRules.IntegerValue.create(24));
    public static final GameRules.Key<GameRules.BooleanValue> RULE_WEATHER_CYCLE = register("doWeatherCycle", GameRules.BooleanValue.create(true));
    public static final GameRules.Key<GameRules.BooleanValue> RULE_LIMITED_CRAFTING = register("doLimitedCrafting", GameRules.BooleanValue.create(false));
    public static final GameRules.Key<GameRules.IntegerValue> RULE_MAX_COMMAND_CHAIN_LENGTH = register(
        "maxCommandChainLength", GameRules.IntegerValue.create(65536)
    );
    public static final GameRules.Key<GameRules.BooleanValue> RULE_ANNOUNCE_ADVANCEMENTS = register("announceAdvancements", GameRules.BooleanValue.create(true));
    public static final GameRules.Key<GameRules.BooleanValue> RULE_DISABLE_RAIDS = register("disableRaids", GameRules.BooleanValue.create(false));
    public static final GameRules.Key<GameRules.BooleanValue> RULE_DOINSOMNIA = register("doInsomnia", GameRules.BooleanValue.create(true));
    public static final GameRules.Key<GameRules.BooleanValue> RULE_DO_IMMEDIATE_RESPAWN = register(
        "doImmediateRespawn", GameRules.BooleanValue.create(false, (param0, param1) -> {
            for(ServerPlayer var0 : param0.getPlayerList().getPlayers()) {
                var0.connection.send(new ClientboundGameEventPacket(11, param1.get() ? 1.0F : 0.0F));
            }
    
        })
    );
    public static final GameRules.Key<GameRules.BooleanValue> RULE_DROWNING_DAMAGE = register("drowningDamage", GameRules.BooleanValue.create(true));
    public static final GameRules.Key<GameRules.BooleanValue> RULE_FALL_DAMAGE = register("fallDamage", GameRules.BooleanValue.create(true));
    public static final GameRules.Key<GameRules.BooleanValue> RULE_FIRE_DAMAGE = register("fireDamage", GameRules.BooleanValue.create(true));
    private final Map<GameRules.Key<?>, GameRules.Value<?>> rules = GAME_RULE_TYPES.entrySet()
        .stream()
        .collect(ImmutableMap.toImmutableMap(Entry::getKey, param0 -> param0.getValue().createRule()));

    private static <T extends GameRules.Value<T>> GameRules.Key<T> register(String param0, GameRules.Type<T> param1) {
        GameRules.Key<T> var0 = new GameRules.Key<>(param0);
        GameRules.Type<?> var1 = GAME_RULE_TYPES.put(var0, param1);
        if (var1 != null) {
            throw new IllegalStateException("Duplicate game rule registration for " + param0);
        } else {
            return var0;
        }
    }

    public <T extends GameRules.Value<T>> T getRule(GameRules.Key<T> param0) {
        return (T)this.rules.get(param0);
    }

    public CompoundTag createTag() {
        CompoundTag var0 = new CompoundTag();
        this.rules.forEach((param1, param2) -> var0.putString(param1.id, param2.serialize()));
        return var0;
    }

    public void loadFromTag(CompoundTag param0) {
        this.rules.forEach((param1, param2) -> param2.deserialize(param0.getString(param1.id)));
    }

    public static void visitGameRuleTypes(GameRules.GameRuleTypeVisitor param0) {
        GAME_RULE_TYPES.forEach((param1, param2) -> cap(param0, param1, param2));
    }

    private static <T extends GameRules.Value<T>> void cap(GameRules.GameRuleTypeVisitor param0, GameRules.Key<?> param1, GameRules.Type<?> param2) {
        param0.visit(param1, param2);
    }

    public boolean getBoolean(GameRules.Key<GameRules.BooleanValue> param0) {
        return this.getRule(param0).get();
    }

    public int getInt(GameRules.Key<GameRules.IntegerValue> param0) {
        return this.getRule(param0).get();
    }

    public static class BooleanValue extends GameRules.Value<GameRules.BooleanValue> {
        private boolean value;

        private static GameRules.Type<GameRules.BooleanValue> create(boolean param0, BiConsumer<MinecraftServer, GameRules.BooleanValue> param1) {
            return new GameRules.Type<>(BoolArgumentType::bool, param1x -> new GameRules.BooleanValue(param1x, param0), param1);
        }

        private static GameRules.Type<GameRules.BooleanValue> create(boolean param0) {
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
        protected String serialize() {
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
    }

    @FunctionalInterface
    public interface GameRuleTypeVisitor {
        <T extends GameRules.Value<T>> void visit(GameRules.Key<T> var1, GameRules.Type<T> var2);
    }

    public static class IntegerValue extends GameRules.Value<GameRules.IntegerValue> {
        private int value;

        private static GameRules.Type<GameRules.IntegerValue> create(int param0, BiConsumer<MinecraftServer, GameRules.IntegerValue> param1) {
            return new GameRules.Type<>(IntegerArgumentType::integer, param1x -> new GameRules.IntegerValue(param1x, param0), param1);
        }

        private static GameRules.Type<GameRules.IntegerValue> create(int param0) {
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

        @Override
        protected String serialize() {
            return Integer.toString(this.value);
        }

        @Override
        protected void deserialize(String param0) {
            this.value = safeParse(param0);
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
    }

    public static final class Key<T extends GameRules.Value<T>> {
        private final String id;

        public Key(String param0) {
            this.id = param0;
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
    }

    public static class Type<T extends GameRules.Value<T>> {
        private final Supplier<ArgumentType<?>> argument;
        private final Function<GameRules.Type<T>, T> constructor;
        private final BiConsumer<MinecraftServer, T> callback;

        private Type(Supplier<ArgumentType<?>> param0, Function<GameRules.Type<T>, T> param1, BiConsumer<MinecraftServer, T> param2) {
            this.argument = param0;
            this.constructor = param1;
            this.callback = param2;
        }

        public RequiredArgumentBuilder<CommandSourceStack, ?> createArgument(String param0) {
            return Commands.argument(param0, this.argument.get());
        }

        public T createRule() {
            return this.constructor.apply(this);
        }
    }

    public abstract static class Value<T extends GameRules.Value<T>> {
        private final GameRules.Type<T> type;

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

        protected abstract String serialize();

        @Override
        public String toString() {
            return this.serialize();
        }

        public abstract int getCommandResult();

        protected abstract T getSelf();
    }
}
