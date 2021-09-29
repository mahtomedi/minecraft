package net.minecraft.commands.arguments;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;

public class MessageArgument implements ArgumentType<MessageArgument.Message> {
    private static final Collection<String> EXAMPLES = Arrays.asList("Hello world!", "foo", "@e", "Hello @p :)");

    public static MessageArgument message() {
        return new MessageArgument();
    }

    public static Component getMessage(CommandContext<CommandSourceStack> param0, String param1) throws CommandSyntaxException {
        return param0.getArgument(param1, MessageArgument.Message.class).toComponent(param0.getSource(), param0.getSource().hasPermission(2));
    }

    public MessageArgument.Message parse(StringReader param0) throws CommandSyntaxException {
        return MessageArgument.Message.parseText(param0, true);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public static class Message {
        private final String text;
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

        public Component toComponent(CommandSourceStack param0, boolean param1) throws CommandSyntaxException {
            if (this.parts.length != 0 && param1) {
                MutableComponent var0 = new TextComponent(this.text.substring(0, this.parts[0].getStart()));
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
                return new TextComponent(this.text);
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
