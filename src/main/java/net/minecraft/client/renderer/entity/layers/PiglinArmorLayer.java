package net.minecraft.client.renderer.entity.layers;

import javax.annotation.Nullable;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PiglinArmorLayer<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> extends HumanoidArmorLayer<T, M, A> {
    private final A headModel;

    public PiglinArmorLayer(RenderLayerParent<T, M> param0, A param1, A param2, A param3) {
        super(param0, param1, param2);
        this.headModel = param3;
    }

    @Override
    public A getArmorModel(EquipmentSlot param0) {
        return (A)(param0 == EquipmentSlot.HEAD ? this.headModel : super.getArmorModel(param0));
    }

    @Override
    protected ResourceLocation getArmorLocation(EquipmentSlot param0, ArmorItem param1, boolean param2, @Nullable String param3) {
        if (param0 == EquipmentSlot.HEAD) {
            String var0 = param3 == null ? "" : "_" + param3;
            String var1 = "textures/models/armor/" + param1.getMaterial().getName() + "_piglin_helmet" + var0 + ".png";
            return ARMOR_LOCATION_CACHE.computeIfAbsent(var1, ResourceLocation::new);
        } else {
            return super.getArmorLocation(param0, param1, param2, param3);
        }
    }
}
