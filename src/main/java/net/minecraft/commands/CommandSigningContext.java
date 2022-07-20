package net.minecraft.commands;

import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.network.chat.PlayerChatMessage;

public interface CommandSigningContext {
    CommandSigningContext ANONYMOUS = new CommandSigningContext() {
        @Nullable
        @Override
        public PlayerChatMessage getArgument(String param0) {
            return null;
        }
    };

    @Nullable
    PlayerChatMessage getArgument(String var1);

    public static record SignedArguments(Map<String, PlayerChatMessage> arguments) implements CommandSigningContext {
        @Nullable
        @Override
        public PlayerChatMessage getArgument(String param0) {
            return this.arguments.get(param0);
        }
    }
}
