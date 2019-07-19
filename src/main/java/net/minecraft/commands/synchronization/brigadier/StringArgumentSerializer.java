package net.minecraft.commands.synchronization.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType.StringType;
import net.minecraft.commands.synchronization.ArgumentSerializer;
import net.minecraft.network.FriendlyByteBuf;

public class StringArgumentSerializer implements ArgumentSerializer<StringArgumentType> {
    public void serializeToNetwork(StringArgumentType param0, FriendlyByteBuf param1) {
        param1.writeEnum(param0.getType());
    }

    public StringArgumentType deserializeFromNetwork(FriendlyByteBuf param0) {
        StringType var0 = param0.readEnum(StringType.class);
        switch(var0) {
            case SINGLE_WORD:
                return StringArgumentType.word();
            case QUOTABLE_PHRASE:
                return StringArgumentType.string();
            case GREEDY_PHRASE:
            default:
                return StringArgumentType.greedyString();
        }
    }

    public void serializeToJson(StringArgumentType param0, JsonObject param1) {
        switch(param0.getType()) {
            case SINGLE_WORD:
                param1.addProperty("type", "word");
                break;
            case QUOTABLE_PHRASE:
                param1.addProperty("type", "phrase");
                break;
            case GREEDY_PHRASE:
            default:
                param1.addProperty("type", "greedy");
        }

    }
}
