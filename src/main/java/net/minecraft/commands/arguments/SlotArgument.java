package net.minecraft.commands.arguments;

import com.google.common.collect.Maps;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;

public class SlotArgument implements ArgumentType<Integer> {
    private static final Collection<String> EXAMPLES = Arrays.asList("container.5", "12", "weapon");
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_SLOT = new DynamicCommandExceptionType(
        param0 -> Component.translatableEscape("slot.unknown", param0)
    );
    private static final Map<String, Integer> SLOTS = Util.make(Maps.newHashMap(), param0 -> {
        for(int var0 = 0; var0 < 54; ++var0) {
            param0.put("container." + var0, var0);
        }

        for(int var1 = 0; var1 < 9; ++var1) {
            param0.put("hotbar." + var1, var1);
        }

        for(int var2 = 0; var2 < 27; ++var2) {
            param0.put("inventory." + var2, 9 + var2);
        }

        for(int var3 = 0; var3 < 27; ++var3) {
            param0.put("enderchest." + var3, 200 + var3);
        }

        for(int var4 = 0; var4 < 8; ++var4) {
            param0.put("villager." + var4, 300 + var4);
        }

        for(int var5 = 0; var5 < 15; ++var5) {
            param0.put("horse." + var5, 500 + var5);
        }

        param0.put("weapon", EquipmentSlot.MAINHAND.getIndex(98));
        param0.put("weapon.mainhand", EquipmentSlot.MAINHAND.getIndex(98));
        param0.put("weapon.offhand", EquipmentSlot.OFFHAND.getIndex(98));
        param0.put("armor.head", EquipmentSlot.HEAD.getIndex(100));
        param0.put("armor.chest", EquipmentSlot.CHEST.getIndex(100));
        param0.put("armor.legs", EquipmentSlot.LEGS.getIndex(100));
        param0.put("armor.feet", EquipmentSlot.FEET.getIndex(100));
        param0.put("horse.saddle", 400);
        param0.put("horse.armor", 401);
        param0.put("horse.chest", 499);
    });

    public static SlotArgument slot() {
        return new SlotArgument();
    }

    public static int getSlot(CommandContext<CommandSourceStack> param0, String param1) {
        return param0.getArgument(param1, Integer.class);
    }

    public Integer parse(StringReader param0) throws CommandSyntaxException {
        String var0 = param0.readUnquotedString();
        if (!SLOTS.containsKey(var0)) {
            throw ERROR_UNKNOWN_SLOT.create(var0);
        } else {
            return SLOTS.get(var0);
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> param0, SuggestionsBuilder param1) {
        return SharedSuggestionProvider.suggest(SLOTS.keySet(), param1);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
