package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.item.DyeableHorseArmorItem;
import net.minecraft.world.item.HorseArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HorseArmorLayer extends RenderLayer<Horse, HorseModel<Horse>> {
    private final HorseModel<Horse> model = new HorseModel<>(0.1F);

    public HorseArmorLayer(RenderLayerParent<Horse, HorseModel<Horse>> param0) {
        super(param0);
    }

    public void render(Horse param0, float param1, float param2, float param3, float param4, float param5, float param6, float param7) {
        ItemStack var0 = param0.getArmor();
        if (var0.getItem() instanceof HorseArmorItem) {
            HorseArmorItem var1 = (HorseArmorItem)var0.getItem();
            this.getParentModel().copyPropertiesTo(this.model);
            this.model.prepareMobModel(param0, param1, param2, param3);
            this.bindTexture(var1.getTexture());
            if (var1 instanceof DyeableHorseArmorItem) {
                int var2 = ((DyeableHorseArmorItem)var1).getColor(var0);
                float var3 = (float)(var2 >> 16 & 0xFF) / 255.0F;
                float var4 = (float)(var2 >> 8 & 0xFF) / 255.0F;
                float var5 = (float)(var2 & 0xFF) / 255.0F;
                RenderSystem.color4f(var3, var4, var5, 1.0F);
                this.model.render(param0, param1, param2, param4, param5, param6, param7);
                return;
            }

            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.model.render(param0, param1, param2, param4, param5, param6, param7);
        }

    }

    @Override
    public boolean colorsOnDamage() {
        return false;
    }
}
