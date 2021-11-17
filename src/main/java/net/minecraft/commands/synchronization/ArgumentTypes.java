package net.minecraft.commands.synchronization;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.commands.arguments.AngleArgument;
import net.minecraft.commands.arguments.ColorArgument;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.EntitySummonArgument;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.commands.arguments.ItemEnchantmentArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.commands.arguments.MobEffectArgument;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.NbtTagArgument;
import net.minecraft.commands.arguments.ObjectiveArgument;
import net.minecraft.commands.arguments.ObjectiveCriteriaArgument;
import net.minecraft.commands.arguments.OperationArgument;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.commands.arguments.RangeArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.ScoreHolderArgument;
import net.minecraft.commands.arguments.ScoreboardSlotArgument;
import net.minecraft.commands.arguments.SlotArgument;
import net.minecraft.commands.arguments.TeamArgument;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.commands.arguments.coordinates.RotationArgument;
import net.minecraft.commands.arguments.coordinates.SwizzleArgument;
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemPredicateArgument;
import net.minecraft.commands.synchronization.brigadier.BrigadierArgumentSerializers;
import net.minecraft.gametest.framework.TestClassNameArgument;
import net.minecraft.gametest.framework.TestFunctionArgument;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ArgumentTypes {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<Class<?>, ArgumentTypes.Entry<?>> BY_CLASS = Maps.newHashMap();
    private static final Map<ResourceLocation, ArgumentTypes.Entry<?>> BY_NAME = Maps.newHashMap();

    public static <T extends ArgumentType<?>> void register(String param0, Class<T> param1, ArgumentSerializer<T> param2) {
        ResourceLocation var0 = new ResourceLocation(param0);
        if (BY_CLASS.containsKey(param1)) {
            throw new IllegalArgumentException("Class " + param1.getName() + " already has a serializer!");
        } else if (BY_NAME.containsKey(var0)) {
            throw new IllegalArgumentException("'" + var0 + "' is already a registered serializer!");
        } else {
            ArgumentTypes.Entry<T> var1 = new ArgumentTypes.Entry<>(param1, param2, var0);
            BY_CLASS.put(param1, var1);
            BY_NAME.put(var0, var1);
        }
    }

    public static void bootStrap() {
        BrigadierArgumentSerializers.bootstrap();
        register("entity", EntityArgument.class, new EntityArgument.Serializer());
        register("game_profile", GameProfileArgument.class, new EmptyArgumentSerializer<>(GameProfileArgument::gameProfile));
        register("block_pos", BlockPosArgument.class, new EmptyArgumentSerializer<>(BlockPosArgument::blockPos));
        register("column_pos", ColumnPosArgument.class, new EmptyArgumentSerializer<>(ColumnPosArgument::columnPos));
        register("vec3", Vec3Argument.class, new EmptyArgumentSerializer<>(Vec3Argument::vec3));
        register("vec2", Vec2Argument.class, new EmptyArgumentSerializer<>(Vec2Argument::vec2));
        register("block_state", BlockStateArgument.class, new EmptyArgumentSerializer<>(BlockStateArgument::block));
        register("block_predicate", BlockPredicateArgument.class, new EmptyArgumentSerializer<>(BlockPredicateArgument::blockPredicate));
        register("item_stack", ItemArgument.class, new EmptyArgumentSerializer<>(ItemArgument::item));
        register("item_predicate", ItemPredicateArgument.class, new EmptyArgumentSerializer<>(ItemPredicateArgument::itemPredicate));
        register("color", ColorArgument.class, new EmptyArgumentSerializer<>(ColorArgument::color));
        register("component", ComponentArgument.class, new EmptyArgumentSerializer<>(ComponentArgument::textComponent));
        register("message", MessageArgument.class, new EmptyArgumentSerializer<>(MessageArgument::message));
        register("nbt_compound_tag", CompoundTagArgument.class, new EmptyArgumentSerializer<>(CompoundTagArgument::compoundTag));
        register("nbt_tag", NbtTagArgument.class, new EmptyArgumentSerializer<>(NbtTagArgument::nbtTag));
        register("nbt_path", NbtPathArgument.class, new EmptyArgumentSerializer<>(NbtPathArgument::nbtPath));
        register("objective", ObjectiveArgument.class, new EmptyArgumentSerializer<>(ObjectiveArgument::objective));
        register("objective_criteria", ObjectiveCriteriaArgument.class, new EmptyArgumentSerializer<>(ObjectiveCriteriaArgument::criteria));
        register("operation", OperationArgument.class, new EmptyArgumentSerializer<>(OperationArgument::operation));
        register("particle", ParticleArgument.class, new EmptyArgumentSerializer<>(ParticleArgument::particle));
        register("angle", AngleArgument.class, new EmptyArgumentSerializer<>(AngleArgument::angle));
        register("rotation", RotationArgument.class, new EmptyArgumentSerializer<>(RotationArgument::rotation));
        register("scoreboard_slot", ScoreboardSlotArgument.class, new EmptyArgumentSerializer<>(ScoreboardSlotArgument::displaySlot));
        register("score_holder", ScoreHolderArgument.class, new ScoreHolderArgument.Serializer());
        register("swizzle", SwizzleArgument.class, new EmptyArgumentSerializer<>(SwizzleArgument::swizzle));
        register("team", TeamArgument.class, new EmptyArgumentSerializer<>(TeamArgument::team));
        register("item_slot", SlotArgument.class, new EmptyArgumentSerializer<>(SlotArgument::slot));
        register("resource_location", ResourceLocationArgument.class, new EmptyArgumentSerializer<>(ResourceLocationArgument::id));
        register("mob_effect", MobEffectArgument.class, new EmptyArgumentSerializer<>(MobEffectArgument::effect));
        register("function", FunctionArgument.class, new EmptyArgumentSerializer<>(FunctionArgument::functions));
        register("entity_anchor", EntityAnchorArgument.class, new EmptyArgumentSerializer<>(EntityAnchorArgument::anchor));
        register("int_range", RangeArgument.Ints.class, new EmptyArgumentSerializer<>(RangeArgument::intRange));
        register("float_range", RangeArgument.Floats.class, new EmptyArgumentSerializer<>(RangeArgument::floatRange));
        register("item_enchantment", ItemEnchantmentArgument.class, new EmptyArgumentSerializer<>(ItemEnchantmentArgument::enchantment));
        register("entity_summon", EntitySummonArgument.class, new EmptyArgumentSerializer<>(EntitySummonArgument::id));
        register("dimension", DimensionArgument.class, new EmptyArgumentSerializer<>(DimensionArgument::dimension));
        register("time", TimeArgument.class, new EmptyArgumentSerializer<>(TimeArgument::time));
        register("uuid", UuidArgument.class, new EmptyArgumentSerializer<>(UuidArgument::uuid));
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            register("test_argument", TestFunctionArgument.class, new EmptyArgumentSerializer<>(TestFunctionArgument::testFunctionArgument));
            register("test_class", TestClassNameArgument.class, new EmptyArgumentSerializer<>(TestClassNameArgument::testClassName));
        }

    }

    @Nullable
    private static ArgumentTypes.Entry<?> get(ResourceLocation param0) {
        return BY_NAME.get(param0);
    }

    @Nullable
    private static ArgumentTypes.Entry<?> get(ArgumentType<?> param0) {
        return BY_CLASS.get(param0.getClass());
    }

    public static <T extends ArgumentType<?>> void serialize(FriendlyByteBuf param0, T param1) {
        ArgumentTypes.Entry<T> var0 = get(param1);
        if (var0 == null) {
            LOGGER.error("Could not serialize {} ({}) - will not be sent to client!", param1, param1.getClass());
            param0.writeResourceLocation(new ResourceLocation(""));
        } else {
            param0.writeResourceLocation(var0.name);
            var0.serializer.serializeToNetwork(param1, param0);
        }
    }

    @Nullable
    public static ArgumentType<?> deserialize(FriendlyByteBuf param0) {
        ResourceLocation var0 = param0.readResourceLocation();
        ArgumentTypes.Entry<?> var1 = get(var0);
        if (var1 == null) {
            LOGGER.error("Could not deserialize {}", var0);
            return null;
        } else {
            return var1.serializer.deserializeFromNetwork(param0);
        }
    }

    private static <T extends ArgumentType<?>> void serializeToJson(JsonObject param0, T param1) {
        ArgumentTypes.Entry<T> var0 = get(param1);
        if (var0 == null) {
            LOGGER.error("Could not serialize argument {} ({})!", param1, param1.getClass());
            param0.addProperty("type", "unknown");
        } else {
            param0.addProperty("type", "argument");
            param0.addProperty("parser", var0.name.toString());
            JsonObject var1 = new JsonObject();
            var0.serializer.serializeToJson(param1, var1);
            if (var1.size() > 0) {
                param0.add("properties", var1);
            }
        }

    }

    public static <S> JsonObject serializeNodeToJson(CommandDispatcher<S> param0, CommandNode<S> param1) {
        JsonObject var0 = new JsonObject();
        if (param1 instanceof RootCommandNode) {
            var0.addProperty("type", "root");
        } else if (param1 instanceof LiteralCommandNode) {
            var0.addProperty("type", "literal");
        } else if (param1 instanceof ArgumentCommandNode) {
            serializeToJson(var0, ((ArgumentCommandNode)param1).getType());
        } else {
            LOGGER.error("Could not serialize node {} ({})!", param1, param1.getClass());
            var0.addProperty("type", "unknown");
        }

        JsonObject var1 = new JsonObject();

        for(CommandNode<S> var2 : param1.getChildren()) {
            var1.add(var2.getName(), serializeNodeToJson(param0, var2));
        }

        if (var1.size() > 0) {
            var0.add("children", var1);
        }

        if (param1.getCommand() != null) {
            var0.addProperty("executable", true);
        }

        if (param1.getRedirect() != null) {
            Collection<String> var3 = param0.getPath(param1.getRedirect());
            if (!var3.isEmpty()) {
                JsonArray var4 = new JsonArray();

                for(String var5 : var3) {
                    var4.add(var5);
                }

                var0.add("redirect", var4);
            }
        }

        return var0;
    }

    public static boolean isTypeRegistered(ArgumentType<?> param0) {
        return get(param0) != null;
    }

    public static <T> Set<ArgumentType<?>> findUsedArgumentTypes(CommandNode<T> param0) {
        Set<CommandNode<T>> var0 = Sets.newIdentityHashSet();
        Set<ArgumentType<?>> var1 = Sets.newHashSet();
        findUsedArgumentTypes(param0, var1, var0);
        return var1;
    }

    private static <T> void findUsedArgumentTypes(CommandNode<T> param0, Set<ArgumentType<?>> param1, Set<CommandNode<T>> param2) {
        if (param2.add(param0)) {
            if (param0 instanceof ArgumentCommandNode) {
                param1.add(((ArgumentCommandNode)param0).getType());
            }

            param0.getChildren().forEach(param2x -> findUsedArgumentTypes(param2x, param1, param2));
            CommandNode<T> var0 = param0.getRedirect();
            if (var0 != null) {
                findUsedArgumentTypes(var0, param1, param2);
            }

        }
    }

    static class Entry<T extends ArgumentType<?>> {
        public final Class<T> clazz;
        public final ArgumentSerializer<T> serializer;
        public final ResourceLocation name;

        Entry(Class<T> param0, ArgumentSerializer<T> param1, ResourceLocation param2) {
            this.clazz = param0;
            this.serializer = param1;
            this.name = param2;
        }
    }
}
