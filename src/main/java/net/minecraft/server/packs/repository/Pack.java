package net.minecraft.server.packs.repository;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.packs.FeatureFlagsMetadataSection;
import net.minecraft.server.packs.OverlayMetadataSection;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.util.InclusiveRange;
import net.minecraft.world.flag.FeatureFlagSet;
import org.slf4j.Logger;

public class Pack {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final String id;
    private final Pack.ResourcesSupplier resources;
    private final Component title;
    private final Pack.Info info;
    private final Pack.Position defaultPosition;
    private final boolean required;
    private final boolean fixedPosition;
    private final PackSource packSource;

    @Nullable
    public static Pack readMetaAndCreate(
        String param0, Component param1, boolean param2, Pack.ResourcesSupplier param3, PackType param4, Pack.Position param5, PackSource param6
    ) {
        int var0 = SharedConstants.getCurrentVersion().getPackVersion(param4);
        Pack.Info var1 = readPackInfo(param0, param3, var0);
        return var1 != null ? create(param0, param1, param2, param3, var1, param5, false, param6) : null;
    }

    public static Pack create(
        String param0,
        Component param1,
        boolean param2,
        Pack.ResourcesSupplier param3,
        Pack.Info param4,
        Pack.Position param5,
        boolean param6,
        PackSource param7
    ) {
        return new Pack(param0, param2, param3, param1, param4, param5, param6, param7);
    }

    private Pack(
        String param0,
        boolean param1,
        Pack.ResourcesSupplier param2,
        Component param3,
        Pack.Info param4,
        Pack.Position param5,
        boolean param6,
        PackSource param7
    ) {
        this.id = param0;
        this.resources = param2;
        this.title = param3;
        this.info = param4;
        this.required = param1;
        this.defaultPosition = param5;
        this.fixedPosition = param6;
        this.packSource = param7;
    }

    @Nullable
    public static Pack.Info readPackInfo(String param0, Pack.ResourcesSupplier param1, int param2) {
        try {
            Pack.Info var11;
            try (PackResources var0 = param1.openPrimary(param0)) {
                PackMetadataSection var1 = var0.getMetadataSection(PackMetadataSection.TYPE);
                if (var1 == null) {
                    LOGGER.warn("Missing metadata in pack {}", param0);
                    return null;
                }

                FeatureFlagsMetadataSection var2 = var0.getMetadataSection(FeatureFlagsMetadataSection.TYPE);
                FeatureFlagSet var3 = var2 != null ? var2.flags() : FeatureFlagSet.of();
                InclusiveRange<Integer> var4 = getDeclaredPackVersions(param0, var1);
                PackCompatibility var5 = PackCompatibility.forVersion(var4, param2);
                OverlayMetadataSection var6 = var0.getMetadataSection(OverlayMetadataSection.TYPE);
                List<String> var7 = var6 != null ? var6.overlaysForVersion(param2) : List.of();
                var11 = new Pack.Info(var1.description(), var5, var3, var7);
            }

            return var11;
        } catch (Exception var14) {
            LOGGER.warn("Failed to read pack {} metadata", param0, var14);
            return null;
        }
    }

    private static InclusiveRange<Integer> getDeclaredPackVersions(String param0, PackMetadataSection param1) {
        int var0 = param1.packFormat();
        if (param1.supportedFormats().isEmpty()) {
            return new InclusiveRange<>(var0);
        } else {
            InclusiveRange<Integer> var1 = param1.supportedFormats().get();
            if (!var1.isValueInRange(var0)) {
                LOGGER.warn("Pack {} declared support for versions {} but declared main format is {}, defaulting to {}", param0, var1, var0, var0);
                return new InclusiveRange<>(var0);
            } else {
                return var1;
            }
        }
    }

    public Component getTitle() {
        return this.title;
    }

    public Component getDescription() {
        return this.info.description();
    }

    public Component getChatLink(boolean param0) {
        return ComponentUtils.wrapInSquareBrackets(this.packSource.decorate(Component.literal(this.id)))
            .withStyle(
                param1 -> param1.withColor(param0 ? ChatFormatting.GREEN : ChatFormatting.RED)
                        .withInsertion(StringArgumentType.escapeIfRequired(this.id))
                        .withHoverEvent(
                            new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.empty().append(this.title).append("\n").append(this.info.description))
                        )
            );
    }

    public PackCompatibility getCompatibility() {
        return this.info.compatibility();
    }

    public FeatureFlagSet getRequestedFeatures() {
        return this.info.requestedFeatures();
    }

    public PackResources open() {
        return this.resources.openFull(this.id, this.info);
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

    public static record Info(Component description, PackCompatibility compatibility, FeatureFlagSet requestedFeatures, List<String> overlays) {
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

    public interface ResourcesSupplier {
        PackResources openPrimary(String var1);

        PackResources openFull(String var1, Pack.Info var2);
    }
}
