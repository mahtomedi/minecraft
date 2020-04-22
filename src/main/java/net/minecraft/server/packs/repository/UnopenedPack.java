package net.minecraft.server.packs.repository;

import com.mojang.brigadier.arguments.StringArgumentType;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.packs.Pack;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UnopenedPack implements AutoCloseable {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final PackMetadataSection BROKEN_ASSETS_FALLBACK = new PackMetadataSection(
        new TranslatableComponent("resourcePack.broken_assets").withStyle(new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.ITALIC}),
        SharedConstants.getCurrentVersion().getPackVersion()
    );
    private final String id;
    private final Supplier<Pack> supplier;
    private final Component title;
    private final Component description;
    private final PackCompatibility compatibility;
    private final UnopenedPack.Position defaultPosition;
    private final boolean required;
    private final boolean fixedPosition;

    @Nullable
    public static <T extends UnopenedPack> T create(
        String param0, boolean param1, Supplier<Pack> param2, UnopenedPack.UnopenedPackConstructor<T> param3, UnopenedPack.Position param4
    ) {
        try (Pack var0 = param2.get()) {
            PackMetadataSection var1 = var0.getMetadataSection(PackMetadataSection.SERIALIZER);
            if (param1 && var1 == null) {
                LOGGER.error(
                    "Broken/missing pack.mcmeta detected, fudging it into existance. Please check that your launcher has downloaded all assets for the game correctly!"
                );
                var1 = BROKEN_ASSETS_FALLBACK;
            }

            if (var1 != null) {
                return param3.create(param0, param1, param2, var0, var1, param4);
            }

            LOGGER.warn("Couldn't find pack meta for pack {}", param0);
        } catch (IOException var21) {
            LOGGER.warn("Couldn't get pack info for: {}", var21.toString());
        }

        return null;
    }

    public UnopenedPack(
        String param0,
        boolean param1,
        Supplier<Pack> param2,
        Component param3,
        Component param4,
        PackCompatibility param5,
        UnopenedPack.Position param6,
        boolean param7
    ) {
        this.id = param0;
        this.supplier = param2;
        this.title = param3;
        this.description = param4;
        this.compatibility = param5;
        this.required = param1;
        this.defaultPosition = param6;
        this.fixedPosition = param7;
    }

    public UnopenedPack(String param0, boolean param1, Supplier<Pack> param2, Pack param3, PackMetadataSection param4, UnopenedPack.Position param5) {
        this(
            param0,
            param1,
            param2,
            new TextComponent(param3.getName()),
            param4.getDescription(),
            PackCompatibility.forFormat(param4.getPackFormat()),
            param5,
            false
        );
    }

    @OnlyIn(Dist.CLIENT)
    public Component getTitle() {
        return this.title;
    }

    @OnlyIn(Dist.CLIENT)
    public Component getDescription() {
        return this.description;
    }

    public Component getChatLink(boolean param0) {
        return ComponentUtils.wrapInSquareBrackets(new TextComponent(this.id))
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

    public Pack open() {
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

    public UnopenedPack.Position getDefaultPosition() {
        return this.defaultPosition;
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (!(param0 instanceof UnopenedPack)) {
            return false;
        } else {
            UnopenedPack var0 = (UnopenedPack)param0;
            return this.id.equals(var0.id);
        }
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public void close() {
    }

    public static enum Position {
        TOP,
        BOTTOM;

        public <T, P extends UnopenedPack> int insert(List<T> param0, T param1, Function<T, P> param2, boolean param3) {
            UnopenedPack.Position var0 = param3 ? this.opposite() : this;
            if (var0 == BOTTOM) {
                int var1;
                for(var1 = 0; var1 < param0.size(); ++var1) {
                    P var2 = param2.apply(param0.get(var1));
                    if (!var2.isFixedPosition() || var2.getDefaultPosition() != this) {
                        break;
                    }
                }

                param0.add(var1, param1);
                return var1;
            } else {
                int var3;
                for(var3 = param0.size() - 1; var3 >= 0; --var3) {
                    P var4 = param2.apply(param0.get(var3));
                    if (!var4.isFixedPosition() || var4.getDefaultPosition() != this) {
                        break;
                    }
                }

                param0.add(var3 + 1, param1);
                return var3 + 1;
            }
        }

        public UnopenedPack.Position opposite() {
            return this == TOP ? BOTTOM : TOP;
        }
    }

    @FunctionalInterface
    public interface UnopenedPackConstructor<T extends UnopenedPack> {
        @Nullable
        T create(String var1, boolean var2, Supplier<Pack> var3, Pack var4, PackMetadataSection var5, UnopenedPack.Position var6);
    }
}
