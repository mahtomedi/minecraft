package net.minecraft.client.resources.metadata.animation;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VillagerMetaDataSection {
    public static final VillagerMetadataSectionSerializer SERIALIZER = new VillagerMetadataSectionSerializer();
    public static final String SECTION_NAME = "villager";
    private final VillagerMetaDataSection.Hat hat;

    public VillagerMetaDataSection(VillagerMetaDataSection.Hat param0) {
        this.hat = param0;
    }

    public VillagerMetaDataSection.Hat getHat() {
        return this.hat;
    }

    @OnlyIn(Dist.CLIENT)
    public static enum Hat {
        NONE("none"),
        PARTIAL("partial"),
        FULL("full");

        private static final Map<String, VillagerMetaDataSection.Hat> BY_NAME = Arrays.stream(values())
            .collect(Collectors.toMap(VillagerMetaDataSection.Hat::getName, param0 -> param0));
        private final String name;

        private Hat(String param0) {
            this.name = param0;
        }

        public String getName() {
            return this.name;
        }

        public static VillagerMetaDataSection.Hat getByName(String param0) {
            return BY_NAME.getOrDefault(param0, NONE);
        }
    }
}
