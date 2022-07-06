package net.minecraft.network.chat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.util.StringRepresentable;

public record ChatTypeDecoration(String translationKey, List<ChatTypeDecoration.Parameter> parameters, Style style) {
    public static final Codec<ChatTypeDecoration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codec.STRING.fieldOf("translation_key").forGetter(ChatTypeDecoration::translationKey),
                    ChatTypeDecoration.Parameter.CODEC.listOf().fieldOf("parameters").forGetter(ChatTypeDecoration::parameters),
                    Style.FORMATTING_CODEC.optionalFieldOf("style", Style.EMPTY).forGetter(ChatTypeDecoration::style)
                )
                .apply(param0, ChatTypeDecoration::new)
    );

    public static ChatTypeDecoration withSender(String param0) {
        return new ChatTypeDecoration(param0, List.of(ChatTypeDecoration.Parameter.SENDER, ChatTypeDecoration.Parameter.CONTENT), Style.EMPTY);
    }

    public static ChatTypeDecoration incomingDirectMessage(String param0) {
        Style var0 = Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(true);
        return new ChatTypeDecoration(param0, List.of(ChatTypeDecoration.Parameter.SENDER, ChatTypeDecoration.Parameter.CONTENT), var0);
    }

    public static ChatTypeDecoration outgoingDirectMessage(String param0) {
        Style var0 = Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(true);
        return new ChatTypeDecoration(param0, List.of(ChatTypeDecoration.Parameter.TARGET, ChatTypeDecoration.Parameter.CONTENT), var0);
    }

    public static ChatTypeDecoration teamMessage(String param0) {
        return new ChatTypeDecoration(
            param0, List.of(ChatTypeDecoration.Parameter.TARGET, ChatTypeDecoration.Parameter.SENDER, ChatTypeDecoration.Parameter.CONTENT), Style.EMPTY
        );
    }

    public Component decorate(Component param0, ChatSender param1) {
        Object[] var0 = this.resolveParameters(param0, param1);
        return Component.translatable(this.translationKey, var0).withStyle(this.style);
    }

    private Component[] resolveParameters(Component param0, ChatSender param1) {
        Component[] var0 = new Component[this.parameters.size()];

        for(int var1 = 0; var1 < var0.length; ++var1) {
            ChatTypeDecoration.Parameter var2 = this.parameters.get(var1);
            var0[var1] = var2.select(param0, param1);
        }

        return var0;
    }

    public static enum Parameter implements StringRepresentable {
        SENDER("sender", (param0, param1) -> param1.name()),
        TARGET("target", (param0, param1) -> param1.targetName()),
        CONTENT("content", (param0, param1) -> param0);

        public static final Codec<ChatTypeDecoration.Parameter> CODEC = StringRepresentable.fromEnum(ChatTypeDecoration.Parameter::values);
        private final String name;
        private final ChatTypeDecoration.Parameter.Selector selector;

        private Parameter(String param0, ChatTypeDecoration.Parameter.Selector param1) {
            this.name = param0;
            this.selector = param1;
        }

        public Component select(Component param0, ChatSender param1) {
            Component var0 = this.selector.select(param0, param1);
            return Objects.requireNonNullElse(var0, CommonComponents.EMPTY);
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public interface Selector {
            @Nullable
            Component select(Component var1, ChatSender var2);
        }
    }
}
