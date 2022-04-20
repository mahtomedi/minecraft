package net.minecraft.network.chat.contents;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.Entity;
import org.slf4j.Logger;

public class SelectorContents implements ComponentContents {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final String pattern;
    @Nullable
    private final EntitySelector selector;
    protected final Optional<Component> separator;

    public SelectorContents(String param0, Optional<Component> param1) {
        this.pattern = param0;
        this.separator = param1;
        this.selector = parseSelector(param0);
    }

    @Nullable
    private static EntitySelector parseSelector(String param0) {
        EntitySelector var0 = null;

        try {
            EntitySelectorParser var1 = new EntitySelectorParser(new StringReader(param0));
            var0 = var1.parse();
        } catch (CommandSyntaxException var3) {
            LOGGER.warn("Invalid selector component: {}: {}", param0, var3.getMessage());
        }

        return var0;
    }

    public String getPattern() {
        return this.pattern;
    }

    @Nullable
    public EntitySelector getSelector() {
        return this.selector;
    }

    public Optional<Component> getSeparator() {
        return this.separator;
    }

    @Override
    public MutableComponent resolve(@Nullable CommandSourceStack param0, @Nullable Entity param1, int param2) throws CommandSyntaxException {
        if (param0 != null && this.selector != null) {
            Optional<? extends Component> var0 = ComponentUtils.updateForEntity(param0, this.separator, param1, param2);
            return ComponentUtils.formatList(this.selector.findEntities(param0), var0, Entity::getDisplayName);
        } else {
            return Component.empty();
        }
    }

    @Override
    public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> param0, Style param1) {
        return param0.accept(param1, this.pattern);
    }

    @Override
    public <T> Optional<T> visit(FormattedText.ContentConsumer<T> param0) {
        return param0.accept(this.pattern);
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else {
            if (param0 instanceof SelectorContents var0 && this.pattern.equals(var0.pattern) && this.separator.equals(var0.separator)) {
                return true;
            }

            return false;
        }
    }

    @Override
    public int hashCode() {
        int var0 = this.pattern.hashCode();
        return 31 * var0 + this.separator.hashCode();
    }

    @Override
    public String toString() {
        return "pattern{" + this.pattern + "}";
    }
}
