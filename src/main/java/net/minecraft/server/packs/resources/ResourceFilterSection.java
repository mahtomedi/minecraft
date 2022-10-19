package net.minecraft.server.packs.resources;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.util.ExtraCodecs;

public class ResourceFilterSection {
    private static final Codec<ResourceFilterSection> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(Codec.list(ResourceFilterSection.ResourceLocationPattern.CODEC).fieldOf("block").forGetter(param0x -> param0x.blockList))
                .apply(param0, ResourceFilterSection::new)
    );
    public static final MetadataSectionType<ResourceFilterSection> TYPE = MetadataSectionType.fromCodec("filter", CODEC);
    private final List<ResourceFilterSection.ResourceLocationPattern> blockList;

    public ResourceFilterSection(List<ResourceFilterSection.ResourceLocationPattern> param0) {
        this.blockList = List.copyOf(param0);
    }

    public boolean isNamespaceFiltered(String param0) {
        return this.blockList.stream().anyMatch(param1 -> param1.namespacePredicate.test(param0));
    }

    public boolean isPathFiltered(String param0) {
        return this.blockList.stream().anyMatch(param1 -> param1.pathPredicate.test(param0));
    }

    static class ResourceLocationPattern implements Predicate<ResourceLocation> {
        static final Codec<ResourceFilterSection.ResourceLocationPattern> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        ExtraCodecs.PATTERN.optionalFieldOf("namespace").forGetter(param0x -> param0x.namespacePattern),
                        ExtraCodecs.PATTERN.optionalFieldOf("path").forGetter(param0x -> param0x.pathPattern)
                    )
                    .apply(param0, ResourceFilterSection.ResourceLocationPattern::new)
        );
        private final Optional<Pattern> namespacePattern;
        final Predicate<String> namespacePredicate;
        private final Optional<Pattern> pathPattern;
        final Predicate<String> pathPredicate;

        private ResourceLocationPattern(Optional<Pattern> param0, Optional<Pattern> param1) {
            this.namespacePattern = param0;
            this.namespacePredicate = param0.map(Pattern::asPredicate).orElse(param0x -> true);
            this.pathPattern = param1;
            this.pathPredicate = param1.map(Pattern::asPredicate).orElse(param0x -> true);
        }

        public boolean test(ResourceLocation param0) {
            return this.namespacePredicate.test(param0.getNamespace()) && this.pathPredicate.test(param0.getPath());
        }
    }
}
