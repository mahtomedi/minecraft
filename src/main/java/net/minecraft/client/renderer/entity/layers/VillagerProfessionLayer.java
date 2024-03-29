package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.IOException;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.VillagerHeadModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.resources.metadata.animation.VillagerMetaDataSection;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VillagerProfessionLayer<T extends LivingEntity & VillagerDataHolder, M extends EntityModel<T> & VillagerHeadModel> extends RenderLayer<T, M> {
    private static final Int2ObjectMap<ResourceLocation> LEVEL_LOCATIONS = Util.make(new Int2ObjectOpenHashMap<>(), param0 -> {
        param0.put(1, new ResourceLocation("stone"));
        param0.put(2, new ResourceLocation("iron"));
        param0.put(3, new ResourceLocation("gold"));
        param0.put(4, new ResourceLocation("emerald"));
        param0.put(5, new ResourceLocation("diamond"));
    });
    private final Object2ObjectMap<VillagerType, VillagerMetaDataSection.Hat> typeHatCache = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectMap<VillagerProfession, VillagerMetaDataSection.Hat> professionHatCache = new Object2ObjectOpenHashMap<>();
    private final ResourceManager resourceManager;
    private final String path;

    public VillagerProfessionLayer(RenderLayerParent<T, M> param0, ResourceManager param1, String param2) {
        super(param0);
        this.resourceManager = param1;
        this.path = param2;
    }

    public void render(
        PoseStack param0, MultiBufferSource param1, int param2, T param3, float param4, float param5, float param6, float param7, float param8, float param9
    ) {
        if (!param3.isInvisible()) {
            VillagerData var0 = param3.getVillagerData();
            VillagerType var1 = var0.getType();
            VillagerProfession var2 = var0.getProfession();
            VillagerMetaDataSection.Hat var3 = this.getHatData(this.typeHatCache, "type", BuiltInRegistries.VILLAGER_TYPE, var1);
            VillagerMetaDataSection.Hat var4 = this.getHatData(this.professionHatCache, "profession", BuiltInRegistries.VILLAGER_PROFESSION, var2);
            M var5 = this.getParentModel();
            var5.hatVisible(var4 == VillagerMetaDataSection.Hat.NONE || var4 == VillagerMetaDataSection.Hat.PARTIAL && var3 != VillagerMetaDataSection.Hat.FULL);
            ResourceLocation var6 = this.getResourceLocation("type", BuiltInRegistries.VILLAGER_TYPE.getKey(var1));
            renderColoredCutoutModel(var5, var6, param0, param1, param2, param3, 1.0F, 1.0F, 1.0F);
            var5.hatVisible(true);
            if (var2 != VillagerProfession.NONE && !param3.isBaby()) {
                ResourceLocation var7 = this.getResourceLocation("profession", BuiltInRegistries.VILLAGER_PROFESSION.getKey(var2));
                renderColoredCutoutModel(var5, var7, param0, param1, param2, param3, 1.0F, 1.0F, 1.0F);
                if (var2 != VillagerProfession.NITWIT) {
                    ResourceLocation var8 = this.getResourceLocation(
                        "profession_level", LEVEL_LOCATIONS.get(Mth.clamp(var0.getLevel(), 1, LEVEL_LOCATIONS.size()))
                    );
                    renderColoredCutoutModel(var5, var8, param0, param1, param2, param3, 1.0F, 1.0F, 1.0F);
                }
            }

        }
    }

    private ResourceLocation getResourceLocation(String param0, ResourceLocation param1) {
        return param1.withPath(param1x -> "textures/entity/" + this.path + "/" + param0 + "/" + param1x + ".png");
    }

    public <K> VillagerMetaDataSection.Hat getHatData(
        Object2ObjectMap<K, VillagerMetaDataSection.Hat> param0, String param1, DefaultedRegistry<K> param2, K param3
    ) {
        return param0.computeIfAbsent(
            param3, param3x -> this.resourceManager.getResource(this.getResourceLocation(param1, param2.getKey(param3))).flatMap(param0x -> {
                    try {
                        return param0x.metadata().getSection(VillagerMetaDataSection.SERIALIZER).map(VillagerMetaDataSection::getHat);
                    } catch (IOException var2x) {
                        return Optional.empty();
                    }
                }).orElse(VillagerMetaDataSection.Hat.NONE)
        );
    }
}
