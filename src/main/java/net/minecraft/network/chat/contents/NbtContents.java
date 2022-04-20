package net.minecraft.network.chat.contents;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.logging.LogUtils;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import org.slf4j.Logger;

public class NbtContents implements ComponentContents {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final boolean interpreting;
    private final Optional<Component> separator;
    private final String nbtPathPattern;
    private final DataSource dataSource;
    @Nullable
    protected final NbtPathArgument.NbtPath compiledNbtPath;

    public NbtContents(String param0, boolean param1, Optional<Component> param2, DataSource param3) {
        this(param0, compileNbtPath(param0), param1, param2, param3);
    }

    private NbtContents(String param0, @Nullable NbtPathArgument.NbtPath param1, boolean param2, Optional<Component> param3, DataSource param4) {
        this.nbtPathPattern = param0;
        this.compiledNbtPath = param1;
        this.interpreting = param2;
        this.separator = param3;
        this.dataSource = param4;
    }

    @Nullable
    private static NbtPathArgument.NbtPath compileNbtPath(String param0) {
        try {
            return new NbtPathArgument().parse(new StringReader(param0));
        } catch (CommandSyntaxException var2) {
            return null;
        }
    }

    public String getNbtPath() {
        return this.nbtPathPattern;
    }

    public boolean isInterpreting() {
        return this.interpreting;
    }

    public Optional<Component> getSeparator() {
        return this.separator;
    }

    public DataSource getDataSource() {
        return this.dataSource;
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else {
            if (param0 instanceof NbtContents var0
                && this.dataSource.equals(var0.dataSource)
                && this.separator.equals(var0.separator)
                && this.interpreting == var0.interpreting
                && this.nbtPathPattern.equals(var0.nbtPathPattern)) {
                return true;
            }

            return false;
        }
    }

    @Override
    public int hashCode() {
        int var0 = super.hashCode();
        var0 = 31 * var0 + (this.interpreting ? 1 : 0);
        var0 = 31 * var0 + this.separator.hashCode();
        var0 = 31 * var0 + this.nbtPathPattern.hashCode();
        return 31 * var0 + this.dataSource.hashCode();
    }

    @Override
    public String toString() {
        return "nbt{" + this.dataSource + ", interpreting=" + this.interpreting + ", separator=" + this.separator + "}";
    }

    @Override
    public MutableComponent resolve(@Nullable CommandSourceStack param0, @Nullable Entity param1, int param2) throws CommandSyntaxException {
        if (param0 != null && this.compiledNbtPath != null) {
            Stream<String> var0 = this.dataSource.getData(param0).flatMap(param0x -> {
                try {
                    return this.compiledNbtPath.get(param0x).stream();
                } catch (CommandSyntaxException var3x) {
                    return Stream.empty();
                }
            }).map(Tag::getAsString);
            if (this.interpreting) {
                Component var1 = DataFixUtils.orElse(
                    ComponentUtils.updateForEntity(param0, this.separator, param1, param2), ComponentUtils.DEFAULT_NO_STYLE_SEPARATOR
                );
                return var0.flatMap(param3 -> {
                    try {
                        MutableComponent var0x = Component.Serializer.fromJson(param3);
                        return Stream.of(ComponentUtils.updateForEntity(param0, var0x, param1, param2));
                    } catch (Exception var5x) {
                        LOGGER.warn("Failed to parse component: {}", param3, var5x);
                        return Stream.of();
                    }
                }).reduce((param1x, param2x) -> param1x.append(var1).append(param2x)).orElseGet(Component::empty);
            } else {
                return ComponentUtils.updateForEntity(param0, this.separator, param1, param2)
                    .map(
                        param1x -> var0.map(Component::literal)
                                .reduce((param1xx, param2x) -> param1xx.append(param1x).append(param2x))
                                .orElseGet(Component::empty)
                    )
                    .orElseGet(() -> Component.literal(var0.collect(Collectors.joining(", "))));
            }
        } else {
            return Component.empty();
        }
    }
}
