package net.minecraft.client.gui.screens.worldselection;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.util.worldupdate.WorldUpgrader;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class OptimizeWorldScreen extends Screen {
    private static final Object2IntMap<DimensionType> DIMENSION_COLORS = Util.make(new Object2IntOpenCustomHashMap<>(Util.identityStrategy()), param0 -> {
        param0.put(DimensionType.OVERWORLD, -13408734);
        param0.put(DimensionType.NETHER, -10075085);
        param0.put(DimensionType.THE_END, -8943531);
        param0.defaultReturnValue(-2236963);
    });
    private final BooleanConsumer callback;
    private final WorldUpgrader upgrader;

    public static OptimizeWorldScreen create(BooleanConsumer param0, LevelStorageSource.LevelStorageAccess param1, boolean param2) {
        WorldData var0 = param1.getDataTag();
        return new OptimizeWorldScreen(param0, param1, var0, param2);
    }

    private OptimizeWorldScreen(BooleanConsumer param0, LevelStorageSource.LevelStorageAccess param1, WorldData param2, boolean param3) {
        super(new TranslatableComponent("optimizeWorld.title", param2.getLevelName()));
        this.callback = param0;
        this.upgrader = new WorldUpgrader(param1, this.minecraft.getFixerUpper(), param2, param3);
    }

    @Override
    protected void init() {
        super.init();
        this.addButton(new Button(this.width / 2 - 100, this.height / 4 + 150, 200, 20, CommonComponents.GUI_CANCEL, param0 -> {
            this.upgrader.cancel();
            this.callback.accept(false);
        }));
    }

    @Override
    public void tick() {
        if (this.upgrader.isFinished()) {
            this.callback.accept(true);
        }

    }

    @Override
    public void onClose() {
        this.callback.accept(false);
    }

    @Override
    public void removed() {
        this.upgrader.cancel();
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        this.drawCenteredString(param0, this.font, this.title, this.width / 2, 20, 16777215);
        int var0 = this.width / 2 - 150;
        int var1 = this.width / 2 + 150;
        int var2 = this.height / 4 + 100;
        int var3 = var2 + 10;
        this.drawCenteredString(param0, this.font, this.upgrader.getStatus(), this.width / 2, var2 - 9 - 2, 10526880);
        if (this.upgrader.getTotalChunks() > 0) {
            fill(param0, var0 - 1, var2 - 1, var1 + 1, var3 + 1, -16777216);
            this.drawString(param0, this.font, I18n.get("optimizeWorld.info.converted", this.upgrader.getConverted()), var0, 40, 10526880);
            this.drawString(param0, this.font, I18n.get("optimizeWorld.info.skipped", this.upgrader.getSkipped()), var0, 40 + 9 + 3, 10526880);
            this.drawString(param0, this.font, I18n.get("optimizeWorld.info.total", this.upgrader.getTotalChunks()), var0, 40 + (9 + 3) * 2, 10526880);
            int var4 = 0;

            for(DimensionType var5 : DimensionType.getAllTypes()) {
                int var6 = Mth.floor(this.upgrader.dimensionProgress(var5) * (float)(var1 - var0));
                fill(param0, var0 + var4, var2, var0 + var4 + var6, var3, DIMENSION_COLORS.getInt(var5));
                var4 += var6;
            }

            int var7 = this.upgrader.getConverted() + this.upgrader.getSkipped();
            this.drawCenteredString(param0, this.font, var7 + " / " + this.upgrader.getTotalChunks(), this.width / 2, var2 + 2 * 9 + 2, 10526880);
            this.drawCenteredString(
                param0, this.font, Mth.floor(this.upgrader.getProgress() * 100.0F) + "%", this.width / 2, var2 + (var3 - var2) / 2 - 9 / 2, 10526880
            );
        }

        super.render(param0, param1, param2, param3);
    }
}
