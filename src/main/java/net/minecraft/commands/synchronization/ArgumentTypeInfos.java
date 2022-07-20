package net.minecraft.commands.synchronization;

import com.google.common.collect.Maps;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.Locale;
import java.util.Map;
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
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.ResourceOrTagLocationArgument;
import net.minecraft.commands.arguments.ScoreHolderArgument;
import net.minecraft.commands.arguments.ScoreboardSlotArgument;
import net.minecraft.commands.arguments.SlotArgument;
import net.minecraft.commands.arguments.TeamArgument;
import net.minecraft.commands.arguments.TemplateMirrorArgument;
import net.minecraft.commands.arguments.TemplateRotationArgument;
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
import net.minecraft.commands.synchronization.brigadier.DoubleArgumentInfo;
import net.minecraft.commands.synchronization.brigadier.FloatArgumentInfo;
import net.minecraft.commands.synchronization.brigadier.IntegerArgumentInfo;
import net.minecraft.commands.synchronization.brigadier.LongArgumentInfo;
import net.minecraft.commands.synchronization.brigadier.StringArgumentSerializer;
import net.minecraft.core.Registry;
import net.minecraft.gametest.framework.TestClassNameArgument;
import net.minecraft.gametest.framework.TestFunctionArgument;

public class ArgumentTypeInfos {
    private static final Map<Class<?>, ArgumentTypeInfo<?, ?>> BY_CLASS = Maps.newHashMap();

    private static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> ArgumentTypeInfo<A, T> register(
        Registry<ArgumentTypeInfo<?, ?>> param0, String param1, Class<? extends A> param2, ArgumentTypeInfo<A, T> param3
    ) {
        BY_CLASS.put(param2, param3);
        return Registry.register(param0, param1, param3);
    }

    public static ArgumentTypeInfo<?, ?> bootstrap(Registry<ArgumentTypeInfo<?, ?>> param0) {
        register(param0, "brigadier:bool", BoolArgumentType.class, SingletonArgumentInfo.contextFree(BoolArgumentType::bool));
        register(param0, "brigadier:float", FloatArgumentType.class, new FloatArgumentInfo());
        register(param0, "brigadier:double", DoubleArgumentType.class, new DoubleArgumentInfo());
        register(param0, "brigadier:integer", IntegerArgumentType.class, new IntegerArgumentInfo());
        register(param0, "brigadier:long", LongArgumentType.class, new LongArgumentInfo());
        register(param0, "brigadier:string", StringArgumentType.class, new StringArgumentSerializer());
        register(param0, "entity", EntityArgument.class, new EntityArgument.Info());
        register(param0, "game_profile", GameProfileArgument.class, SingletonArgumentInfo.contextFree(GameProfileArgument::gameProfile));
        register(param0, "block_pos", BlockPosArgument.class, SingletonArgumentInfo.contextFree(BlockPosArgument::blockPos));
        register(param0, "column_pos", ColumnPosArgument.class, SingletonArgumentInfo.contextFree(ColumnPosArgument::columnPos));
        register(param0, "vec3", Vec3Argument.class, SingletonArgumentInfo.contextFree(Vec3Argument::vec3));
        register(param0, "vec2", Vec2Argument.class, SingletonArgumentInfo.contextFree(Vec2Argument::vec2));
        register(param0, "block_state", BlockStateArgument.class, SingletonArgumentInfo.contextAware(BlockStateArgument::block));
        register(param0, "block_predicate", BlockPredicateArgument.class, SingletonArgumentInfo.contextAware(BlockPredicateArgument::blockPredicate));
        register(param0, "item_stack", ItemArgument.class, SingletonArgumentInfo.contextAware(ItemArgument::item));
        register(param0, "item_predicate", ItemPredicateArgument.class, SingletonArgumentInfo.contextAware(ItemPredicateArgument::itemPredicate));
        register(param0, "color", ColorArgument.class, SingletonArgumentInfo.contextFree(ColorArgument::color));
        register(param0, "component", ComponentArgument.class, SingletonArgumentInfo.contextFree(ComponentArgument::textComponent));
        register(param0, "message", MessageArgument.class, SingletonArgumentInfo.contextFree(MessageArgument::message));
        register(param0, "nbt_compound_tag", CompoundTagArgument.class, SingletonArgumentInfo.contextFree(CompoundTagArgument::compoundTag));
        register(param0, "nbt_tag", NbtTagArgument.class, SingletonArgumentInfo.contextFree(NbtTagArgument::nbtTag));
        register(param0, "nbt_path", NbtPathArgument.class, SingletonArgumentInfo.contextFree(NbtPathArgument::nbtPath));
        register(param0, "objective", ObjectiveArgument.class, SingletonArgumentInfo.contextFree(ObjectiveArgument::objective));
        register(param0, "objective_criteria", ObjectiveCriteriaArgument.class, SingletonArgumentInfo.contextFree(ObjectiveCriteriaArgument::criteria));
        register(param0, "operation", OperationArgument.class, SingletonArgumentInfo.contextFree(OperationArgument::operation));
        register(param0, "particle", ParticleArgument.class, SingletonArgumentInfo.contextFree(ParticleArgument::particle));
        register(param0, "angle", AngleArgument.class, SingletonArgumentInfo.contextFree(AngleArgument::angle));
        register(param0, "rotation", RotationArgument.class, SingletonArgumentInfo.contextFree(RotationArgument::rotation));
        register(param0, "scoreboard_slot", ScoreboardSlotArgument.class, SingletonArgumentInfo.contextFree(ScoreboardSlotArgument::displaySlot));
        register(param0, "score_holder", ScoreHolderArgument.class, new ScoreHolderArgument.Info());
        register(param0, "swizzle", SwizzleArgument.class, SingletonArgumentInfo.contextFree(SwizzleArgument::swizzle));
        register(param0, "team", TeamArgument.class, SingletonArgumentInfo.contextFree(TeamArgument::team));
        register(param0, "item_slot", SlotArgument.class, SingletonArgumentInfo.contextFree(SlotArgument::slot));
        register(param0, "resource_location", ResourceLocationArgument.class, SingletonArgumentInfo.contextFree(ResourceLocationArgument::id));
        register(param0, "mob_effect", MobEffectArgument.class, SingletonArgumentInfo.contextFree(MobEffectArgument::effect));
        register(param0, "function", FunctionArgument.class, SingletonArgumentInfo.contextFree(FunctionArgument::functions));
        register(param0, "entity_anchor", EntityAnchorArgument.class, SingletonArgumentInfo.contextFree(EntityAnchorArgument::anchor));
        register(param0, "int_range", RangeArgument.Ints.class, SingletonArgumentInfo.contextFree(RangeArgument::intRange));
        register(param0, "float_range", RangeArgument.Floats.class, SingletonArgumentInfo.contextFree(RangeArgument::floatRange));
        register(param0, "item_enchantment", ItemEnchantmentArgument.class, SingletonArgumentInfo.contextFree(ItemEnchantmentArgument::enchantment));
        register(param0, "entity_summon", EntitySummonArgument.class, SingletonArgumentInfo.contextFree(EntitySummonArgument::id));
        register(param0, "dimension", DimensionArgument.class, SingletonArgumentInfo.contextFree(DimensionArgument::dimension));
        register(param0, "time", TimeArgument.class, SingletonArgumentInfo.contextFree(TimeArgument::time));
        register(param0, "resource_or_tag", fixClassType(ResourceOrTagLocationArgument.class), new ResourceOrTagLocationArgument.Info());
        register(param0, "resource", fixClassType(ResourceKeyArgument.class), new ResourceKeyArgument.Info());
        register(param0, "template_mirror", TemplateMirrorArgument.class, SingletonArgumentInfo.contextFree(TemplateMirrorArgument::templateMirror));
        register(param0, "template_rotation", TemplateRotationArgument.class, SingletonArgumentInfo.contextFree(TemplateRotationArgument::templateRotation));
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            register(param0, "test_argument", TestFunctionArgument.class, SingletonArgumentInfo.contextFree(TestFunctionArgument::testFunctionArgument));
            register(param0, "test_class", TestClassNameArgument.class, SingletonArgumentInfo.contextFree(TestClassNameArgument::testClassName));
        }

        return register(param0, "uuid", UuidArgument.class, SingletonArgumentInfo.contextFree(UuidArgument::uuid));
    }

    private static <T extends ArgumentType<?>> Class<T> fixClassType(Class<? super T> param0) {
        return (Class<T>)param0;
    }

    public static boolean isClassRecognized(Class<?> param0) {
        return BY_CLASS.containsKey(param0);
    }

    public static <A extends ArgumentType<?>> ArgumentTypeInfo<A, ?> byClass(A param0) {
        ArgumentTypeInfo<?, ?> var0 = BY_CLASS.get(param0.getClass());
        if (var0 == null) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "Unrecognized argument type %s (%s)", param0, param0.getClass()));
        } else {
            return var0;
        }
    }

    public static <A extends ArgumentType<?>> ArgumentTypeInfo.Template<A> unpack(A param0) {
        return byClass(param0).unpack(param0);
    }
}
