package net.minecraft.network.chat;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.world.entity.Entity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SelectorComponent extends BaseComponent implements ContextAwareComponent {
    private static final Logger LOGGER = LogManager.getLogger();
    private final String pattern;
    @Nullable
    private final EntitySelector selector;

    public SelectorComponent(String param0) {
        this.pattern = param0;
        EntitySelector var0 = null;

        try {
            EntitySelectorParser var1 = new EntitySelectorParser(new StringReader(param0));
            var0 = var1.parse();
        } catch (CommandSyntaxException var4) {
            LOGGER.warn("Invalid selector component: {}", param0, var4.getMessage());
        }

        this.selector = var0;
    }

    public String getPattern() {
        return this.pattern;
    }

    @Override
    public MutableComponent resolve(@Nullable CommandSourceStack param0, @Nullable Entity param1, int param2) throws CommandSyntaxException {
        return (MutableComponent)(param0 != null && this.selector != null
            ? EntitySelector.joinNames(this.selector.findEntities(param0))
            : new TextComponent(""));
    }

    @Override
    public String getContents() {
        return this.pattern;
    }

    public SelectorComponent plainCopy() {
        return new SelectorComponent(this.pattern);
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (!(param0 instanceof SelectorComponent)) {
            return false;
        } else {
            SelectorComponent var0 = (SelectorComponent)param0;
            return this.pattern.equals(var0.pattern) && super.equals(param0);
        }
    }

    @Override
    public String toString() {
        return "SelectorComponent{pattern='" + this.pattern + '\'' + ", siblings=" + this.siblings + ", style=" + this.getStyle() + '}';
    }
}
