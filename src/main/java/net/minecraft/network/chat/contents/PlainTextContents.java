package net.minecraft.network.chat.contents;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;

public interface PlainTextContents extends ComponentContents {
    MapCodec<PlainTextContents> CODEC = RecordCodecBuilder.mapCodec(
        param0 -> param0.group(Codec.STRING.fieldOf("text").forGetter(PlainTextContents::text)).apply(param0, PlainTextContents::create)
    );
    ComponentContents.Type<PlainTextContents> TYPE = new ComponentContents.Type<>(CODEC, "text");
    PlainTextContents EMPTY = new PlainTextContents() {
        @Override
        public String toString() {
            return "empty";
        }

        @Override
        public String text() {
            return "";
        }
    };

    static PlainTextContents create(String param0) {
        return (PlainTextContents)(param0.isEmpty() ? EMPTY : new PlainTextContents.LiteralContents(param0));
    }

    String text();

    @Override
    default ComponentContents.Type<?> type() {
        return TYPE;
    }

    public static record LiteralContents<T>(String text) implements PlainTextContents {
        @Override
        public <T> Optional<T> visit(FormattedText.ContentConsumer<T> param0) {
            return param0.accept(this.text);
        }

        @Override
        public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> param0, Style param1) {
            return param0.accept(param1, this.text);
        }

        @Override
        public String toString() {
            return "literal{" + this.text + "}";
        }
    }
}
