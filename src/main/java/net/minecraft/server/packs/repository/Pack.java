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
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Pack implements AutoCloseable {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final PackMetadataSection BROKEN_ASSETS_FALLBACK = new PackMetadataSection(
        new TranslatableComponent("resourcePack.broken_assets").withStyle(new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.ITALIC}),
        SharedConstants.getCurrentVersion().getPackVersion()
    );
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
    public static <T extends Pack> T create(
        String param0, boolean param1, Supplier<PackResources> param2, Pack.PackConstructor<T> param3, Pack.Position param4, PackSource param5
    ) {
        try (PackResources var0 = param2.get()) {
            PackMetadataSection var1 = var0.getMetadataSection(PackMetadataSection.SERIALIZER);
            if (param1 && var1 == null) {
                LOGGER.error(
                    "Broken/missing pack.mcmeta detected, fudging it into existance. Please check that your launcher has downloaded all assets for the game correctly!"
                );
                var1 = BROKEN_ASSETS_FALLBACK;
            }

            if (var1 != null) {
                return param3.create(param0, param1, param2, var0, var1, param4, param5);
            }

            LOGGER.warn("Couldn't find pack meta for pack {}", param0);
        } catch (IOException var22) {
            LOGGER.warn("Couldn't get pack info for: {}", var22.toString());
        }

        return null;
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
        boolean param1,
        Supplier<PackResources> param2,
        PackResources param3,
        PackMetadataSection param4,
        Pack.Position param5,
        PackSource param6
    ) {
        this(
            param0,
            param1,
            param2,
            new TextComponent(param3.getName()),
            param4.getDescription(),
            PackCompatibility.forFormat(param4.getPackFormat()),
            param5,
            false,
            param6
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

    @OnlyIn(Dist.CLIENT)
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

    @Override
    public void close() {
    }

    @FunctionalInterface
    public interface PackConstructor<T extends Pack> {
        @Nullable
        T create(String var1, boolean var2, Supplier<PackResources> var3, PackResources var4, PackMetadataSection var5, Pack.Position var6, PackSource var7);
    }

    public static enum Position {
        TOP,
        BOTTOM;

        public <T, P extends Pack> int insert(List<T> param0, T param1, Function<T, P> param2, boolean param3) {
            Pack.Position var0 = param3 ? this.opposite() : this;
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

        public Pack.Position opposite() {
            return this == TOP ? BOTTOM : TOP;
        }
    }
}
