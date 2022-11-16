package net.minecraft.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import net.minecraft.resources.ResourceLocation;

public class ResourceLocationPattern {
    public static final Codec<ResourceLocationPattern> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    ExtraCodecs.PATTERN.optionalFieldOf("namespace").forGetter(param0x -> param0x.namespacePattern),
                    ExtraCodecs.PATTERN.optionalFieldOf("path").forGetter(param0x -> param0x.pathPattern)
                )
                .apply(param0, ResourceLocationPattern::new)
    );
    private final Optional<Pattern> namespacePattern;
    private final Predicate<String> namespacePredicate;
    private final Optional<Pattern> pathPattern;
    private final Predicate<String> pathPredicate;
    private final Predicate<ResourceLocation> locationPredicate;

    private ResourceLocationPattern(Optional<Pattern> param0, Optional<Pattern> param1) {
        this.namespacePattern = param0;
        this.namespacePredicate = param0.map(Pattern::asPredicate).orElse(param0x -> true);
        this.pathPattern = param1;
        this.pathPredicate = param1.map(Pattern::asPredicate).orElse(param0x -> true);
        this.locationPredicate = param0x -> this.namespacePredicate.test(param0x.getNamespace()) && this.pathPredicate.test(param0x.getPath());
    }

    public Predicate<String> namespacePredicate() {
        return this.namespacePredicate;
    }

    public Predicate<String> pathPredicate() {
        return this.pathPredicate;
    }

    public Predicate<ResourceLocation> locationPredicate() {
        return this.locationPredicate;
    }
}
