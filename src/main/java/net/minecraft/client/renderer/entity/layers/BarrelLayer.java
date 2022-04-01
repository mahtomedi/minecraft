package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BarrelLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    public BarrelLayer(RenderLayerParent<T, M> param0) {
        super(param0);
    }

    public void render(
        PoseStack param0, MultiBufferSource param1, int param2, T param3, float param4, float param5, float param6, float param7, float param8, float param9
    ) {
        ItemStack var0 = param3.getItemBySlot(EquipmentSlot.HEAD);
        if (!var0.isEmpty() && var0.is(Items.BARREL)) {
            BlockState var1 = Blocks.BARREL.defaultBlockState();
            if (var1.hasProperty(BlockStateProperties.OPEN)) {
                BooleanProperty var10001;
                boolean var10002;
                label35: {
                    var10001 = BlockStateProperties.OPEN;
                    if (param3 instanceof Player var2 && var2.isCrouching()) {
                        var10002 = true;
                        break label35;
                    }

                    var10002 = false;
                }

                var1 = var1.setValue(var10001, Boolean.valueOf(!var10002));
            }

            if (var1.hasProperty(BlockStateProperties.FACING)) {
                var1 = var1.setValue(BlockStateProperties.FACING, Direction.UP);
            }

            label29: {
                param0.pushPose();
                if (param3 instanceof Player var3 && var3.isCrouching()) {
                    param0.scale(1.07F, 1.07F, 1.07F);
                    param0.translate(-0.5, 0.28, -0.5);
                    break label29;
                }

                param0.translate(-0.5, -0.25, -0.5);
            }

            Minecraft.getInstance().getBlockRenderer().renderSingleBlock(var1, param0, param1, param2, OverlayTexture.NO_OVERLAY);
            param0.popPose();
        }
    }
}
