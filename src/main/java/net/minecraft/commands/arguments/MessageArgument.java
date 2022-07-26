package net.minecraft.commands.arguments;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSigningContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.network.chat.ChatDecorator;
import net.minecraft.network.chat.ChatMessageContent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;
import net.minecraft.server.players.PlayerList;
import org.slf4j.Logger;

public class MessageArgument implements SignedArgument<MessageArgument.Message> {
    private static final Collection<String> EXAMPLES = Arrays.asList("Hello world!", "foo", "@e", "Hello @p :)");
    private static final Logger LOGGER = LogUtils.getLogger();

    public static MessageArgument message() {
        return new MessageArgument();
    }

    public static Component getMessage(CommandContext<CommandSourceStack> param0, String param1) throws CommandSyntaxException {
        MessageArgument.Message var0 = param0.getArgument(param1, MessageArgument.Message.class);
        return var0.resolveComponent(param0.getSource());
    }

    public static MessageArgument.ChatMessage getChatMessage(CommandContext<CommandSourceStack> param0, String param1) throws CommandSyntaxException {
        MessageArgument.Message var0 = param0.getArgument(param1, MessageArgument.Message.class);
        Component var1 = var0.resolveComponent(param0.getSource());
        CommandSigningContext var2 = param0.getSource().getSigningContext();
        PlayerChatMessage var3 = var2.getArgument(param1);
        if (var3 == null) {
            ChatMessageContent var4 = new ChatMessageContent(var0.text, var1);
            return new MessageArgument.ChatMessage(PlayerChatMessage.system(var4));
        } else {
            return new MessageArgument.ChatMessage(ChatDecorator.attachIfNotDecorated(var3, var1));
        }
    }

    public MessageArgument.Message parse(StringReader param0) throws CommandSyntaxException {
        return MessageArgument.Message.parseText(param0, true);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public String getSignableText(MessageArgument.Message param0) {
        return param0.getText();
    }

    public CompletableFuture<Component> resolvePreview(CommandSourceStack param0, MessageArgument.Message param1) throws CommandSyntaxException {
        return param1.resolveDecoratedComponent(param0);
    }

    @Override
    public Class<MessageArgument.Message> getValueType() {
        return MessageArgument.Message.class;
    }

    static void logResolutionFailure(CommandSourceStack param0, CompletableFuture<?> param1) {
        param1.exceptionally(param1x -> {
            LOGGER.error("Encountered unexpected exception while resolving chat message argument from '{}'", param0.getDisplayName().getString(), param1x);
            return null;
        });
    }

    public static record ChatMessage(PlayerChatMessage signedArgument) {
        public void resolve(CommandSourceStack param0, Consumer<PlayerChatMessage> param1) {
            MinecraftServer var0 = param0.getServer();
            param0.getChatMessageChainer().append(() -> {
                CompletableFuture<FilteredText> var0x = this.filterPlainText(param0, this.signedArgument.signedContent().plain());
                CompletableFuture<PlayerChatMessage> var1x = var0.getChatDecorator().decorate(param0.getPlayer(), this.signedArgument);
                return CompletableFuture.allOf(var0x, var1x).thenAcceptAsync(param3 -> {
                    PlayerChatMessage var0xx = ((PlayerChatMessage)var1x.join()).filter(var0x.join().mask());
                    param1.accept(var0xx);
                }, var0);
            });
        }

        private CompletableFuture<FilteredText> filterPlainText(CommandSourceStack param0, String param1) {
            ServerPlayer var0 = param0.getPlayer();
            return var0 != null && this.signedArgument.hasSignatureFrom(var0.getUUID())
                ? var0.getTextFilter().processStreamMessage(param1)
                : CompletableFuture.completedFuture(FilteredText.passThrough(param1));
        }

        public void consume(CommandSourceStack param0) {
            if (!this.signedArgument.signer().isSystem()) {
                this.resolve(param0, param1 -> {
                    PlayerList var0 = param0.getServer().getPlayerList();
                    var0.broadcastMessageHeader(param1, Set.of());
                });
            }

        }
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

        CompletableFuture<Component> resolveDecoratedComponent(CommandSourceStack param0) throws CommandSyntaxException {
            Component var0 = this.resolveComponent(param0);
            CompletableFuture<Component> var1 = param0.getServer().getChatDecorator().decorate(param0.getPlayer(), var0);
            MessageArgument.logResolutionFailure(param0, var1);
            return var1;
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
