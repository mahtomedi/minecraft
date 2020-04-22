package net.minecraft.network.chat;

import java.util.function.UnaryOperator;
import net.minecraft.ChatFormatting;

public interface MutableComponent extends Component {
    MutableComponent setStyle(Style var1);

    default MutableComponent append(String param0) {
        return this.append(new TextComponent(param0));
    }

    MutableComponent append(Component var1);

    default MutableComponent withStyle(UnaryOperator<Style> param0) {
        this.setStyle(param0.apply(this.getStyle()));
        return this;
    }

    default MutableComponent withStyle(Style param0) {
        this.setStyle(param0.applyTo(this.getStyle()));
        return this;
    }

    default MutableComponent withStyle(ChatFormatting... param0) {
        this.setStyle(this.getStyle().applyFormats(param0));
        return this;
    }

    default MutableComponent withStyle(ChatFormatting param0) {
        this.setStyle(this.getStyle().applyFormat(param0));
        return this;
    }
}
