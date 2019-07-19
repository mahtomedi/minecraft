package net.minecraft.client.renderer.entity.layers;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HumanoidArmorLayer<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> extends AbstractArmorLayer<T, M, A> {
    public HumanoidArmorLayer(RenderLayerParent<T, M> param0, A param1, A param2) {
        super(param0, param1, param2);
    }

    @Override
    protected void setPartVisibility(A param0, EquipmentSlot param1) {
        this.hideAllArmor(param0);
        switch(param1) {
            case HEAD:
                param0.head.visible = true;
                param0.hat.visible = true;
                break;
            case CHEST:
                param0.body.visible = true;
                param0.rightArm.visible = true;
                param0.leftArm.visible = true;
                break;
            case LEGS:
                param0.body.visible = true;
                param0.rightLeg.visible = true;
                param0.leftLeg.visible = true;
                break;
            case FEET:
                param0.rightLeg.visible = true;
                param0.leftLeg.visible = true;
        }

    }

    @Override
    protected void hideAllArmor(A param0) {
        param0.setAllVisible(false);
    }
}
