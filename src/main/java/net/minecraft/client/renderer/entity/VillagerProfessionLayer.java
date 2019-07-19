package net.minecraft.client.renderer.entity;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.IOException;
import net.minecraft.Util;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.resources.metadata.animation.VillagerMetaDataSection;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VillagerProfessionLayer<T extends LivingEntity & VillagerDataHolder, M extends EntityModel<T> & VillagerHeadModel>
    extends RenderLayer<T, M>
    implements ResourceManagerReloadListener {
    private static final Int2ObjectMap<ResourceLocation> LEVEL_LOCATIONS = Util.make(new Int2ObjectOpenHashMap<>(), param0 -> {
        param0.put(1, new ResourceLocation("stone"));
        param0.put(2, new ResourceLocation("iron"));
        param0.put(3, new ResourceLocation("gold"));
        param0.put(4, new ResourceLocation("emerald"));
        param0.put(5, new ResourceLocation("diamond"));
    });
    private final Object2ObjectMap<VillagerType, VillagerMetaDataSection.Hat> typeHatCache = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectMap<VillagerProfession, VillagerMetaDataSection.Hat> professionHatCache = new Object2ObjectOpenHashMap<>();
    private final ReloadableResourceManager resourceManager;
    private final String path;

    public VillagerProfessionLayer(RenderLayerParent<T, M> param0, ReloadableResourceManager param1, String param2) {
        super(param0);
        this.resourceManager = param1;
        this.path = param2;
        param1.registerReloadListener(this);
    }

    public void render(T param0, float param1, float param2, float param3, float param4, float param5, float param6, float param7) {
        if (!param0.isInvisible()) {
            VillagerData var0 = param0.getVillagerData();
            VillagerType var1 = var0.getType();
            VillagerProfession var2 = var0.getProfession();
            VillagerMetaDataSection.Hat var3 = this.getHatData(this.typeHatCache, "type", Registry.VILLAGER_TYPE, var1);
            VillagerMetaDataSection.Hat var4 = this.getHatData(this.professionHatCache, "profession", Registry.VILLAGER_PROFESSION, var2);
            M var5 = this.getParentModel();
            this.bindTexture(this.getResourceLocation("type", Registry.VILLAGER_TYPE.getKey(var1)));
            var5.hatVisible(var4 == VillagerMetaDataSection.Hat.NONE || var4 == VillagerMetaDataSection.Hat.PARTIAL && var3 != VillagerMetaDataSection.Hat.FULL);
            var5.render(param0, param1, param2, param4, param5, param6, param7);
            var5.hatVisible(true);
            if (var2 != VillagerProfession.NONE && !param0.isBaby()) {
                this.bindTexture(this.getResourceLocation("profession", Registry.VILLAGER_PROFESSION.getKey(var2)));
                var5.render(param0, param1, param2, param4, param5, param6, param7);
                this.bindTexture(this.getResourceLocation("profession_level", LEVEL_LOCATIONS.get(Mth.clamp(var0.getLevel(), 1, LEVEL_LOCATIONS.size()))));
                var5.render(param0, param1, param2, param4, param5, param6, param7);
            }

        }
    }

    @Override
    public boolean colorsOnDamage() {
        return true;
    }

    private ResourceLocation getResourceLocation(String param0, ResourceLocation param1) {
        return new ResourceLocation(param1.getNamespace(), "textures/entity/" + this.path + "/" + param0 + "/" + param1.getPath() + ".png");
    }

    public <K> VillagerMetaDataSection.Hat getHatData(
        Object2ObjectMap<K, VillagerMetaDataSection.Hat> param0, String param1, DefaultedRegistry<K> param2, K param3
    ) {
        return param0.computeIfAbsent(param3, param3x -> {
            try (Resource var0 = this.resourceManager.getResource(this.getResourceLocation(param1, param2.getKey(param3)))) {
                VillagerMetaDataSection var1x = var0.getMetadata(VillagerMetaDataSection.SERIALIZER);
                if (var1x != null) {
                    return var1x.getHat();
                }
            } catch (IOException var21) {
            }

            return VillagerMetaDataSection.Hat.NONE;
        });
    }

    @Override
    public void onResourceManagerReload(ResourceManager param0) {
        this.professionHatCache.clear();
        this.typeHatCache.clear();
    }
}
