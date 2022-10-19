package net.minecraft.commands.arguments;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSigningContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;

public class MessageArgument implements SignedArgument<MessageArgument.Message> {
    private static final Collection<String> EXAMPLES = Arrays.asList("Hello world!", "foo", "@e", "Hello @p :)");

    public static MessageArgument message() {
        return new MessageArgument();
    }

    public static Component getMessage(CommandContext<CommandSourceStack> param0, String param1) throws CommandSyntaxException {
        MessageArgument.Message var0 = param0.getArgument(param1, MessageArgument.Message.class);
        return var0.resolveComponent(param0.getSource());
    }

    public static void resolveChatMessage(CommandContext<CommandSourceStack> param0, String param1, Consumer<PlayerChatMessage> param2) throws CommandSyntaxException {
        MessageArgument.Message var0 = param0.getArgument(param1, MessageArgument.Message.class);
        CommandSourceStack var1 = param0.getSource();
        Component var2 = var0.resolveComponent(var1);
        CommandSigningContext var3 = var1.getSigningContext();
        PlayerChatMessage var4 = var3.getArgument(param1);
        if (var4 != null) {
            resolveSignedMessage(param2, var1, var4.withUnsignedContent(var2));
        } else {
            resolveDisguisedMessage(param2, var1, PlayerChatMessage.system(var0.text).withUnsignedContent(var2));
        }

    }

    private static void resolveSignedMessage(Consumer<PlayerChatMessage> param0, CommandSourceStack param1, PlayerChatMessage param2) {
        MinecraftServer var0 = param1.getServer();
        CompletableFuture<FilteredText> var1 = filterPlainText(param1, param2);
        CompletableFuture<Component> var2 = var0.getChatDecorator().decorate(param1.getPlayer(), param2.decoratedContent());
        param1.getChatMessageChainer().append(param4 -> CompletableFuture.allOf(var1, var2).thenAcceptAsync(param4x -> {
                PlayerChatMessage var0x = param2.withUnsignedContent(var2.join()).filter(var1.join().mask());
                param0.accept(var0x);
            }, param4));
    }

    private static void resolveDisguisedMessage(Consumer<PlayerChatMessage> param0, CommandSourceStack param1, PlayerChatMessage param2) {
        MinecraftServer var0 = param1.getServer();
        CompletableFuture<Component> var1 = var0.getChatDecorator().decorate(param1.getPlayer(), param2.decoratedContent());
        param1.getChatMessageChainer().append(param3 -> var1.thenAcceptAsync(param2x -> param0.accept(param2.withUnsignedContent(param2x)), param3));
    }

    private static CompletableFuture<FilteredText> filterPlainText(CommandSourceStack param0, PlayerChatMessage param1) {
        ServerPlayer var0 = param0.getPlayer();
        return var0 != null && param1.hasSignatureFrom(var0.getUUID())
            ? var0.getTextFilter().processStreamMessage(param1.signedContent())
            : CompletableFuture.completedFuture(FilteredText.passThrough(param1.signedContent()));
    }

    public MessageArgument.Message parse(StringReader param0) throws CommandSyntaxException {
        return MessageArgument.Message.parseText(param0, true);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public static class Message {
        final String text;
        private final MessageArgument.Part[] parts;

        public Message(String param0, MessageArgument.Part[] param1) {
            this.text = param0;
            this.parts = param1;
        }

        public String getText() {
            return this.text;
        }

        public MessageArgument.Part[] getParts() {
            return this.parts;
        }

        Component resolveComponent(CommandSourceStack param0) throws CommandSyntaxException {
            return this.toComponent(param0, param0.hasPermission(2));
        }

        public Component toComponent(CommandSourceStack param0, boolean param1) throws CommandSyntaxException {
            if (this.parts.length != 0 && param1) {
                MutableComponent var0 = Component.literal(this.text.substring(0, this.parts[0].getStart()));
                int var1 = this.parts[0].getStart();

                for(MessageArgument.Part var2 : this.parts) {
                    Component var3 = var2.toComponent(param0);
                    if (var1 < var2.getStart()) {
                        var0.append(this.text.substring(var1, var2.getStart()));
                    }

                    if (var3 != null) {
                        var0.append(var3);
                    }

                    var1 = var2.getEnd();
                }

                if (var1 < this.text.length()) {
                    var0.append(this.text.substring(var1));
                }

                return var0;
            } else {
                return Component.literal(this.text);
            }
        }

        public static MessageArgument.Message parseText(StringReader param0, boolean param1) throws CommandSyntaxException {
            String var0 = param0.getString().substring(param0.getCursor(), param0.getTotalLength());
            if (!param1) {
                param0.setCursor(param0.getTotalLength());
                return new MessageArgument.Message(var0, new MessageArgument.Part[0]);
            } else {
                List<MessageArgument.Part> var1 = Lists.newArrayList();
                int var2 = param0.getCursor();

                while(true) {
                    int var3;
                    EntitySelector var5;
                    while(true) {
                        if (!param0.canRead()) {
                            return new MessageArgument.Message(var0, var1.toArray(new MessageArgument.Part[0]));
                        }

                        if (param0.peek() == '@') {
                            var3 = param0.getCursor();

                            try {
                                EntitySelectorParser var4 = new EntitySelectorParser(param0);
                                var5 = var4.parse();
                                break;
                            } catch (CommandSyntaxException var8) {
                                if (var8.getType() != EntitySelectorParser.ERROR_MISSING_SELECTOR_TYPE
                                    && var8.getType() != EntitySelectorParser.ERROR_UNKNOWN_SELECTOR_TYPE) {
                                    throw var8;
                                }

                                param0.setCursor(var3 + 1);
                            }
                        } else {
                            param0.skip();
                        }
                    }

                    var1.add(new MessageArgument.Part(var3 - var2, param0.getCursor() - var2, var5));
                }
            }
        }
    }

    public static class Part {
        private final int start;
        private final int end;
        private final EntitySelector selector;

        public Part(int param0, int param1, EntitySelector param2) {
            this.start = param0;
            this.end = param1;
            this.selector = param2;
        }

        public int getStart() {
            return this.start;
        }

        public int getEnd() {
            return this.end;
        }

        public EntitySelector getSelector() {
            return this.selector;
        }

        @Nullable
        public Component toComponent(CommandSourceStack param0) throws CommandSyntaxException {
            return EntitySelector.joinNames(this.selector.findEntities(param0));
        }
    }
}
