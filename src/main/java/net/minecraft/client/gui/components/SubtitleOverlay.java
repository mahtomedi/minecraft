package net.minecraft.client.gui.components;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
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
        if (!this.isListening && this.minecraft.options.showSubtitles) {
            this.minecraft.getSoundManager().addListener(this);
            this.isListening = true;
        } else if (this.isListening && !this.minecraft.options.showSubtitles) {
            this.minecraft.getSoundManager().removeListener(this);
            this.isListening = false;
        }

        if (this.isListening && !this.subtitles.isEmpty()) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
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
            Iterator<SubtitleOverlay.Subtitle> var6 = this.subtitles.iterator();

            while(var6.hasNext()) {
                SubtitleOverlay.Subtitle var7 = var6.next();
                if (var7.getTime() + 3000L <= Util.getMillis()) {
                    var6.remove();
                } else {
                    var5 = Math.max(var5, this.minecraft.font.width(var7.getText()));
                }
            }

            var5 += this.minecraft.font.width("<") + this.minecraft.font.width(" ") + this.minecraft.font.width(">") + this.minecraft.font.width(" ");

            for(SubtitleOverlay.Subtitle var8 : this.subtitles) {
                int var9 = 255;
                Component var10 = var8.getText();
                Vec3 var11 = var8.getLocation().subtract(var0).normalize();
                double var12 = -var3.dot(var11);
                double var13 = -var1.dot(var11);
                boolean var14 = var13 > 0.5;
                int var15 = var5 / 2;
                int var16 = 9;
                int var17 = var16 / 2;
                float var18 = 1.0F;
                int var19 = this.minecraft.font.width(var10);
                int var20 = Mth.floor(Mth.clampedLerp(255.0, 75.0, (double)((float)(Util.getMillis() - var8.getTime()) / 3000.0F)));
                int var21 = var20 << 16 | var20 << 8 | var20;
                param0.pushPose();
                param0.translate(
                    (double)((float)this.minecraft.getWindow().getGuiScaledWidth() - (float)var15 * 1.0F - 2.0F),
                    (double)((float)(this.minecraft.getWindow().getGuiScaledHeight() - 30) - (float)(var4 * (var16 + 1)) * 1.0F),
                    0.0
                );
                param0.scale(1.0F, 1.0F, 1.0F);
                fill(param0, -var15 - 1, -var17 - 1, var15 + 1, var17 + 1, this.minecraft.options.getBackgroundColor(0.8F));
                RenderSystem.enableBlend();
                if (!var14) {
                    if (var12 > 0.0) {
                        this.minecraft.font.draw(param0, ">", (float)(var15 - this.minecraft.font.width(">")), (float)(-var17), var21 + -16777216);
                    } else if (var12 < 0.0) {
                        this.minecraft.font.draw(param0, "<", (float)(-var15), (float)(-var17), var21 + -16777216);
                    }
                }

                this.minecraft.font.draw(param0, var10, (float)(-var19 / 2), (float)(-var17), var21 + -16777216);
                param0.popPose();
                ++var4;
            }

            RenderSystem.disableBlend();
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
