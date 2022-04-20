package net.minecraft.network.chat.contents;

import java.util.Optional;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;

public record LiteralContents<T>(String text) implements ComponentContents {
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
