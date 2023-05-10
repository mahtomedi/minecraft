package net.minecraft.client.renderer.debug;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collections;
import java.util.List;
import java.util.function.DoubleSupplier;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SupportBlockRenderer implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;
    private double lastUpdateTime = Double.MIN_VALUE;
    private List<Entity> surroundEntities = Collections.emptyList();

    public SupportBlockRenderer(Minecraft param0) {
        this.minecraft = param0;
    }

    @Override
    public void render(PoseStack param0, MultiBufferSource param1, double param2, double param3, double param4) {
        double var0 = (double)Util.getNanos();
        if (var0 - this.lastUpdateTime > 1.0E8) {
            this.lastUpdateTime = var0;
            Entity var1 = this.minecraft.gameRenderer.getMainCamera().getEntity();
            this.surroundEntities = ImmutableList.copyOf(var1.level().getEntities(var1, var1.getBoundingBox().inflate(16.0)));
        }

        Player var2 = this.minecraft.player;
        if (var2 != null && var2.mainSupportingBlockPos.isPresent()) {
            this.drawHighlights(param0, param1, param2, param3, param4, var2, () -> 0.0, 1.0F, 0.0F, 0.0F);
        }

        for(Entity var3 : this.surroundEntities) {
            if (var3 != var2) {
                this.drawHighlights(param0, param1, param2, param3, param4, var3, () -> this.getBias(var3), 0.0F, 1.0F, 0.0F);
            }
        }

    }

    private void drawHighlights(
        PoseStack param0,
        MultiBufferSource param1,
        double param2,
        double param3,
        double param4,
        Entity param5,
        DoubleSupplier param6,
        float param7,
        float param8,
        float param9
    ) {
        param5.mainSupportingBlockPos.ifPresent(param10 -> {
            double var0 = param6.getAsDouble();
            BlockPos var1x = param5.getOnPos();
            this.highlightPosition(var1x, param0, param2, param3, param4, param1, 0.02 + var0, param7, param8, param9);
            BlockPos var2x = param5.getOnPosLegacy();
            if (!var2x.equals(var1x)) {
                this.highlightPosition(var2x, param0, param2, param3, param4, param1, 0.04 + var0, 0.0F, 1.0F, 1.0F);
            }

        });
    }

    private double getBias(Entity param0) {
        return 0.02 * (double)(String.valueOf((double)param0.getId() + 0.132453657).hashCode() % 1000) / 1000.0;
    }

    private void highlightPosition(
        BlockPos param0,
        PoseStack param1,
        double param2,
        double param3,
        double param4,
        MultiBufferSource param5,
        double param6,
        float param7,
        float param8,
        float param9
    ) {
        double var0 = (double)param0.getX() - param2 - 2.0 * param6;
        double var1 = (double)param0.getY() - param3 - 2.0 * param6;
        double var2 = (double)param0.getZ() - param4 - 2.0 * param6;
        double var3 = var0 + 1.0 + 4.0 * param6;
        double var4 = var1 + 1.0 + 4.0 * param6;
        double var5 = var2 + 1.0 + 4.0 * param6;
        LevelRenderer.renderLineBox(param1, param5.getBuffer(RenderType.lines()), var0, var1, var2, var3, var4, var5, param7, param8, param9, 0.4F);
        LevelRenderer.renderVoxelShape(
            param1,
            param5.getBuffer(RenderType.lines()),
            this.minecraft
                .level
                .getBlockState(param0)
                .getCollisionShape(this.minecraft.level, param0, CollisionContext.empty())
                .move((double)param0.getX(), (double)param0.getY(), (double)param0.getZ()),
            -param2,
            -param3,
            -param4,
            param7,
            param8,
            param9,
            1.0F,
            false
        );
    }
}
