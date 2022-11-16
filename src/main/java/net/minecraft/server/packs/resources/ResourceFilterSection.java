package net.minecraft.server.packs.resources;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.util.ResourceLocationPattern;

public class ResourceFilterSection {
    private static final Codec<ResourceFilterSection> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(Codec.list(ResourceLocationPattern.CODEC).fieldOf("block").forGetter(param0x -> param0x.blockList))
                .apply(param0, ResourceFilterSection::new)
    );
    public static final MetadataSectionType<ResourceFilterSection> TYPE = MetadataSectionType.fromCodec("filter", CODEC);
    private final List<ResourceLocationPattern> blockList;

    public ResourceFilterSection(List<ResourceLocationPattern> param0) {
        this.blockList = List.copyOf(param0);
    }

    public boolean isNamespaceFiltered(String param0) {
        return this.blockList.stream().anyMatch(param1 -> param1.namespacePredicate().test(param0));
    }

    public boolean isPathFiltered(String param0) {
        return this.blockList.stream().anyMatch(param1 -> param1.pathPredicate().test(param0));
    }
}
