package net.minecraft.client.gui.components;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Iterator;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEventListener;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SubtitleOverlay extends GuiComponent implements SoundEventListener {
    private static final long DISPLAY_TIME = 3000L;
    private final Minecraft minecraft;
    private final List<SubtitleOverlay.Subtitle> subtitles = Lists.newArrayList();
    private boolean isListening;

    public SubtitleOverlay(Minecraft param0) {
        this.minecraft = param0;
    }

    public void render(PoseStack param0) {
        if (!this.isListening && this.minecraft.options.showSubtitles().get()) {
            this.minecraft.getSoundManager().addListener(this);
            this.isListening = true;
        } else if (this.isListening && !this.minecraft.options.showSubtitles().get()) {
            this.minecraft.getSoundManager().removeListener(this);
            this.isListening = false;
        }

        if (this.isListening && !this.subtitles.isEmpty()) {
            Vec3 var0 = new Vec3(this.minecraft.player.getX(), this.minecraft.player.getEyeY(), this.minecraft.player.getZ());
            Vec3 var1 = new Vec3(0.0, 0.0, -1.0)
                .xRot(-this.minecraft.player.getXRot() * (float) (Math.PI / 180.0))
                .yRot(-this.minecraft.player.getYRot() * (float) (Math.PI / 180.0));
            Vec3 var2 = new Vec3(0.0, 1.0, 0.0)
                .xRot(-this.minecraft.player.getXRot() * (float) (Math.PI / 180.0))
                .yRot(-this.minecraft.player.getYRot() * (float) (Math.PI / 180.0));
            Vec3 var3 = var1.cross(var2);
            int var4 = 0;
            int var5 = 0;
            double var6 = this.minecraft.options.notificationDisplayTime().get();
            Iterator<SubtitleOverlay.Subtitle> var7 = this.subtitles.iterator();

            while(var7.hasNext()) {
                SubtitleOverlay.Subtitle var8 = var7.next();
                if ((double)var8.getTime() + 3000.0 * var6 <= (double)Util.getMillis()) {
                    var7.remove();
                } else {
                    var5 = Math.max(var5, this.minecraft.font.width(var8.getText()));
                }
            }

            var5 += this.minecraft.font.width("<") + this.minecraft.font.width(" ") + this.minecraft.font.width(">") + this.minecraft.font.width(" ");

            for(SubtitleOverlay.Subtitle var9 : this.subtitles) {
                int var10 = 255;
                Component var11 = var9.getText();
                Vec3 var12 = var9.getLocation().subtract(var0).normalize();
                double var13 = -var3.dot(var12);
                double var14 = -var1.dot(var12);
                boolean var15 = var14 > 0.5;
                int var16 = var5 / 2;
                int var17 = 9;
                int var18 = var17 / 2;
                float var19 = 1.0F;
                int var20 = this.minecraft.font.width(var11);
                int var21 = Mth.floor(Mth.clampedLerp(255.0F, 75.0F, (float)(Util.getMillis() - var9.getTime()) / (float)(3000.0 * var6)));
                int var22 = var21 << 16 | var21 << 8 | var21;
                param0.pushPose();
                param0.translate(
                    (float)this.minecraft.getWindow().getGuiScaledWidth() - (float)var16 * 1.0F - 2.0F,
                    (float)(this.minecraft.getWindow().getGuiScaledHeight() - 35) - (float)(var4 * (var17 + 1)) * 1.0F,
                    0.0F
                );
                param0.scale(1.0F, 1.0F, 1.0F);
                fill(param0, -var16 - 1, -var18 - 1, var16 + 1, var18 + 1, this.minecraft.options.getBackgroundColor(0.8F));
                int var23 = var22 + -16777216;
                if (!var15) {
                    if (var13 > 0.0) {
                        drawString(param0, this.minecraft.font, ">", var16 - this.minecraft.font.width(">"), -var18, var23);
                    } else if (var13 < 0.0) {
                        drawString(param0, this.minecraft.font, "<", -var16, -var18, var23);
                    }
                }

                drawString(param0, this.minecraft.font, var11, -var20 / 2, -var18, var23);
                param0.popPose();
                ++var4;
            }

        }
    }

    @Override
    public void onPlaySound(SoundInstance param0, WeighedSoundEvents param1) {
        if (param1.getSubtitle() != null) {
            Component var0 = param1.getSubtitle();
            if (!this.subtitles.isEmpty()) {
                for(SubtitleOverlay.Subtitle var1 : this.subtitles) {
                    if (var1.getText().equals(var0)) {
                        var1.refresh(new Vec3(param0.getX(), param0.getY(), param0.getZ()));
                        return;
                    }
                }
            }

            this.subtitles.add(new SubtitleOverlay.Subtitle(var0, new Vec3(param0.getX(), param0.getY(), param0.getZ())));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Subtitle {
        private final Component text;
        private long time;
        private Vec3 location;

        public Subtitle(Component param0, Vec3 param1) {
            this.text = param0;
            this.location = param1;
            this.time = Util.getMillis();
        }

        public Component getText() {
            return this.text;
        }

        public long getTime() {
            return this.time;
        }

        public Vec3 getLocation() {
            return this.location;
        }

        public void refresh(Vec3 param0) {
            this.location = param0;
            this.time = Util.getMillis();
        }
    }
}
