package net.minecraft.network.chat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.util.StringRepresentable;

public record ChatDecoration(String translationKey, List<ChatDecoration.Parameter> parameters, Style style) {
    public static final Codec<ChatDecoration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codec.STRING.fieldOf("translation_key").forGetter(ChatDecoration::translationKey),
                    ChatDecoration.Parameter.CODEC.listOf().fieldOf("parameters").forGetter(ChatDecoration::parameters),
                    Style.FORMATTING_CODEC.fieldOf("style").forGetter(ChatDecoration::style)
                )
                .apply(param0, ChatDecoration::new)
    );

    public static ChatDecoration withSender(String param0) {
        return new ChatDecoration(param0, List.of(ChatDecoration.Parameter.SENDER, ChatDecoration.Parameter.CONTENT), Style.EMPTY);
    }

    public static ChatDecoration directMessage(String param0) {
        Style var0 = Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(true);
        return new ChatDecoration(param0, List.of(ChatDecoration.Parameter.SENDER, ChatDecoration.Parameter.CONTENT), var0);
    }

    public static ChatDecoration teamMessage(String param0) {
        return new ChatDecoration(
            param0, List.of(ChatDecoration.Parameter.TEAM_NAME, ChatDecoration.Parameter.SENDER, ChatDecoration.Parameter.CONTENT), Style.EMPTY
        );
    }

    public Component decorate(Component param0, @Nullable ChatSender param1) {
        Object[] var0 = this.resolveParameters(param0, param1);
        return Component.translatable(this.translationKey, var0).withStyle(this.style);
    }

    private Component[] resolveParameters(Component param0, @Nullable ChatSender param1) {
        Component[] var0 = new Component[this.parameters.size()];

        for(int var1 = 0; var1 < var0.length; ++var1) {
            ChatDecoration.Parameter var2 = this.parameters.get(var1);
            var0[var1] = var2.select(param0, param1);
        }

        return var0;
    }

    public static enum Parameter implements StringRepresentable {
        SENDER("sender", (param0, param1) -> param1 != null ? param1.name() : null),
        TEAM_NAME("team_name", (param0, param1) -> param1 != null ? param1.teamName() : null),
        CONTENT("content", (param0, param1) -> param0);

        public static final Codec<ChatDecoration.Parameter> CODEC = StringRepresentable.fromEnum(ChatDecoration.Parameter::values);
        private final String name;
        private final ChatDecoration.Parameter.Selector selector;

        private Parameter(String param0, ChatDecoration.Parameter.Selector param1) {
            this.name = param0;
            this.selector = param1;
        }

        public Component select(Component param0, @Nullable ChatSender param1) {
            Component var0 = this.selector.select(param0, param1);
            return Objects.requireNonNullElse(var0, CommonComponents.EMPTY);
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public interface Selector {
            @Nullable
            Component select(Component var1, @Nullable ChatSender var2);
        }
    }
}
