package net.minecraft.server.packs.repository;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import org.slf4j.Logger;

public class Pack {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final String id;
    private final Supplier<PackResources> supplier;
    private final Component title;
    private final Component description;
    private final PackCompatibility compatibility;
    private final Pack.Position defaultPosition;
    private final boolean required;
    private final boolean fixedPosition;
    private final PackSource packSource;

    @Nullable
    public static Pack create(
        String param0, boolean param1, Supplier<PackResources> param2, Pack.PackConstructor param3, Pack.Position param4, PackSource param5
    ) {
        try {
            Pack var8;
            try (PackResources var0 = param2.get()) {
                PackMetadataSection var1 = var0.getMetadataSection(PackMetadataSection.SERIALIZER);
                if (var1 == null) {
                    LOGGER.warn("Couldn't find pack meta for pack {}", param0);
                    return null;
                }

                var8 = param3.create(param0, new TextComponent(var0.getName()), param1, param2, var1, param4, param5);
            }

            return var8;
        } catch (IOException var11) {
            LOGGER.warn("Couldn't get pack info for: {}", var11.toString());
            return null;
        }
    }

    public Pack(
        String param0,
        boolean param1,
        Supplier<PackResources> param2,
        Component param3,
        Component param4,
        PackCompatibility param5,
        Pack.Position param6,
        boolean param7,
        PackSource param8
    ) {
        this.id = param0;
        this.supplier = param2;
        this.title = param3;
        this.description = param4;
        this.compatibility = param5;
        this.required = param1;
        this.defaultPosition = param6;
        this.fixedPosition = param7;
        this.packSource = param8;
    }

    public Pack(
        String param0,
        Component param1,
        boolean param2,
        Supplier<PackResources> param3,
        PackMetadataSection param4,
        PackType param5,
        Pack.Position param6,
        PackSource param7
    ) {
        this(param0, param2, param3, param1, param4.getDescription(), PackCompatibility.forMetadata(param4, param5), param6, false, param7);
    }

    public Component getTitle() {
        return this.title;
    }

    public Component getDescription() {
        return this.description;
    }

    public Component getChatLink(boolean param0) {
        return ComponentUtils.wrapInSquareBrackets(this.packSource.decorate(new TextComponent(this.id)))
            .withStyle(
                param1 -> param1.withColor(param0 ? ChatFormatting.GREEN : ChatFormatting.RED)
                        .withInsertion(StringArgumentType.escapeIfRequired(this.id))
                        .withHoverEvent(
                            new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("").append(this.title).append("\n").append(this.description))
                        )
            );
    }

    public PackCompatibility getCompatibility() {
        return this.compatibility;
    }

    public PackResources open() {
        return this.supplier.get();
    }

    public String getId() {
        return this.id;
    }

    public boolean isRequired() {
        return this.required;
    }

    public boolean isFixedPosition() {
        return this.fixedPosition;
    }

    public Pack.Position getDefaultPosition() {
        return this.defaultPosition;
    }

    public PackSource getPackSource() {
        return this.packSource;
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (!(param0 instanceof Pack)) {
            return false;
        } else {
            Pack var0 = (Pack)param0;
            return this.id.equals(var0.id);
        }
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @FunctionalInterface
    public interface PackConstructor {
        @Nullable
        Pack create(String var1, Component var2, boolean var3, Supplier<PackResources> var4, PackMetadataSection var5, Pack.Position var6, PackSource var7);
    }

    public static enum Position {
        TOP,
        BOTTOM;

        public <T> int insert(List<T> param0, T param1, Function<T, Pack> param2, boolean param3) {
            Pack.Position var0 = param3 ? this.opposite() : this;
            if (var0 == BOTTOM) {
                int var1;
                for(var1 = 0; var1 < param0.size(); ++var1) {
                    Pack var2 = param2.apply(param0.get(var1));
                    if (!var2.isFixedPosition() || var2.getDefaultPosition() != this) {
                        break;
                    }
                }

                param0.add(var1, param1);
                return var1;
            } else {
                int var3;
                for(var3 = param0.size() - 1; var3 >= 0; --var3) {
                    Pack var4 = param2.apply(param0.get(var3));
                    if (!var4.isFixedPosition() || var4.getDefaultPosition() != this) {
                        break;
                    }
                }

                param0.add(var3 + 1, param1);
                return var3 + 1;
            }
        }

        public Pack.Position opposite() {
            return this == TOP ? BOTTOM : TOP;
        }
    }
}
