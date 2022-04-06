package net.minecraft.commands.synchronization.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType.StringType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;

public class StringArgumentSerializer implements ArgumentTypeInfo<StringArgumentType, StringArgumentSerializer.Template> {
    public void serializeToNetwork(StringArgumentSerializer.Template param0, FriendlyByteBuf param1) {
        param1.writeEnum(param0.type);
    }

    public StringArgumentSerializer.Template deserializeFromNetwork(FriendlyByteBuf param0) {
        StringType var0 = param0.readEnum(StringType.class);
        return new StringArgumentSerializer.Template(var0);
    }

    public void serializeToJson(StringArgumentSerializer.Template param0, JsonObject param1) {
        param1.addProperty("type", switch(param0.type) {
            case SINGLE_WORD -> "word";
            case QUOTABLE_PHRASE -> "phrase";
            case GREEDY_PHRASE -> "greedy";
        });
    }

    public StringArgumentSerializer.Template unpack(StringArgumentType param0) {
        return new StringArgumentSerializer.Template(param0.getType());
    }

    public final class Template implements ArgumentTypeInfo.Template<StringArgumentType> {
        final StringType type;

        public Template(StringType param1) {
            this.type = param1;
        }

        public StringArgumentType instantiate(CommandBuildContext param0) {
            return switch(this.type) {
                case SINGLE_WORD -> StringArgumentType.word();
                case QUOTABLE_PHRASE -> StringArgumentType.string();
                case GREEDY_PHRASE -> StringArgumentType.greedyString();
            };
        }

        @Override
        public ArgumentTypeInfo<StringArgumentType, ?> type() {
            return StringArgumentSerializer.this;
        }
    }
}
