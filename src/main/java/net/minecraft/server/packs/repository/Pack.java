package net.minecraft.server.packs.repository;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.packs.FeatureFlagsMetadataSection;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.world.flag.FeatureFlagSet;
import org.slf4j.Logger;

public class Pack {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final String id;
    private final Pack.ResourcesSupplier resources;
    private final Component title;
    private final Component description;
    private final PackCompatibility compatibility;
    private final FeatureFlagSet requestedFeatures;
    private final Pack.Position defaultPosition;
    private final boolean required;
    private final boolean fixedPosition;
    private final PackSource packSource;

    @Nullable
    public static Pack readMetaAndCreate(
        String param0, Component param1, boolean param2, Pack.ResourcesSupplier param3, PackType param4, Pack.Position param5, PackSource param6
    ) {
        Pack.Info var0 = readPackInfo(param0, param3);
        return var0 != null ? create(param0, param1, param2, param3, var0, param4, param5, false, param6) : null;
    }

    public static Pack create(
        String param0,
        Component param1,
        boolean param2,
        Pack.ResourcesSupplier param3,
        Pack.Info param4,
        PackType param5,
        Pack.Position param6,
        boolean param7,
        PackSource param8
    ) {
        return new Pack(param0, param2, param3, param1, param4, param4.compatibility(param5), param6, param7, param8);
    }

    private Pack(
        String param0,
        boolean param1,
        Pack.ResourcesSupplier param2,
        Component param3,
        Pack.Info param4,
        PackCompatibility param5,
        Pack.Position param6,
        boolean param7,
        PackSource param8
    ) {
        this.id = param0;
        this.resources = param2;
        this.title = param3;
        this.description = param4.description();
        this.compatibility = param5;
        this.requestedFeatures = param4.requestedFeatures();
        this.required = param1;
        this.defaultPosition = param6;
        this.fixedPosition = param7;
        this.packSource = param8;
    }

    @Nullable
    public static Pack.Info readPackInfo(String param0, Pack.ResourcesSupplier param1) {
        try {
            Pack.Info var6;
            try (PackResources var0 = param1.open(param0)) {
                PackMetadataSection var1 = var0.getMetadataSection(PackMetadataSection.TYPE);
                if (var1 == null) {
                    LOGGER.warn("Missing metadata in pack {}", param0);
                    return null;
                }

                FeatureFlagsMetadataSection var2 = var0.getMetadataSection(FeatureFlagsMetadataSection.TYPE);
                FeatureFlagSet var3 = var2 != null ? var2.flags() : FeatureFlagSet.of();
                var6 = new Pack.Info(var1.getDescription(), var1.getPackFormat(), var3);
            }

            return var6;
        } catch (Exception var9) {
            LOGGER.warn("Failed to read pack metadata", (Throwable)var9);
            return null;
        }
    }

    public Component getTitle() {
        return this.title;
    }

    public Component getDescription() {
        return this.description;
    }

    public Component getChatLink(boolean param0) {
        return ComponentUtils.wrapInSquareBrackets(this.packSource.decorate(Component.literal(this.id)))
            .withStyle(
                param1 -> param1.withColor(param0 ? ChatFormatting.GREEN : ChatFormatting.RED)
                        .withInsertion(StringArgumentType.escapeIfRequired(this.id))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.empty().append(this.title).append("\n").append(this.description)))
            );
    }

    public PackCompatibility getCompatibility() {
        return this.compatibility;
    }

    public FeatureFlagSet getRequestedFeatures() {
        return this.requestedFeatures;
    }

    public PackResources open() {
        return this.resources.open(this.id);
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

    public static record Info(Component description, int format, FeatureFlagSet requestedFeatures) {
        public PackCompatibility compatibility(PackType param0) {
            return PackCompatibility.forFormat(this.format, param0);
        }
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

    @FunctionalInterface
    public interface ResourcesSupplier {
        PackResources open(String var1);
    }
}
