package net.minecraft.server.packs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.InclusiveRange;

public record OverlayMetadataSection(List<OverlayMetadataSection.OverlayEntry> overlays) {
    private static final Pattern DIR_VALIDATOR = Pattern.compile("[-_a-zA-Z0-9.]+");
    private static final Codec<OverlayMetadataSection> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(OverlayMetadataSection.OverlayEntry.CODEC.listOf().fieldOf("entries").forGetter(OverlayMetadataSection::overlays))
                .apply(param0, OverlayMetadataSection::new)
    );
    public static final MetadataSectionType<OverlayMetadataSection> TYPE = MetadataSectionType.fromCodec("overlays", CODEC);

    private static DataResult<String> validateOverlayDir(String param0) {
        return !DIR_VALIDATOR.matcher(param0).matches() ? DataResult.error(() -> param0 + " is not accepted directory name") : DataResult.success(param0);
    }

    public List<String> overlaysForVersion(int param0) {
        return this.overlays.stream().filter(param1 -> param1.isApplicable(param0)).map(OverlayMetadataSection.OverlayEntry::overlay).toList();
    }

    public static record OverlayEntry(InclusiveRange<Integer> format, String overlay) {
        static final Codec<OverlayMetadataSection.OverlayEntry> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        InclusiveRange.codec(Codec.INT).fieldOf("formats").forGetter(OverlayMetadataSection.OverlayEntry::format),
                        ExtraCodecs.validate(Codec.STRING, OverlayMetadataSection::validateOverlayDir)
                            .fieldOf("directory")
                            .forGetter(OverlayMetadataSection.OverlayEntry::overlay)
                    )
                    .apply(param0, OverlayMetadataSection.OverlayEntry::new)
        );

        public boolean isApplicable(int param0) {
            return this.format.isValueInRange(param0);
        }
    }
}
