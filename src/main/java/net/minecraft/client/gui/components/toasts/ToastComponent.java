package net.minecraft.client.gui.components.toasts;

import com.google.common.collect.Queues;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Arrays;
import java.util.Deque;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ToastComponent extends GuiComponent {
    private final Minecraft minecraft;
    private final ToastComponent.ToastInstance<?>[] visible = new ToastComponent.ToastInstance[5];
    private final Deque<Toast> queued = Queues.newArrayDeque();

    public ToastComponent(Minecraft param0) {
        this.minecraft = param0;
    }

    public void render(PoseStack param0) {
        if (!this.minecraft.options.hideGui) {
            for(int var0 = 0; var0 < this.visible.length; ++var0) {
                ToastComponent.ToastInstance<?> var1 = this.visible[var0];
                if (var1 != null && var1.render(this.minecraft.getWindow().getGuiScaledWidth(), var0, param0)) {
                    this.visible[var0] = null;
                }

                if (this.visible[var0] == null && !this.queued.isEmpty()) {
                    this.visible[var0] = new ToastComponent.ToastInstance(this.queued.removeFirst());
                }
            }

        }
    }

    @Nullable
    public <T extends Toast> T getToast(Class<? extends T> param0, Object param1) {
        for(ToastComponent.ToastInstance<?> var0 : this.visible) {
            if (var0 != null && param0.isAssignableFrom(var0.getToast().getClass()) && var0.getToast().getToken().equals(param1)) {
                return (T)var0.getToast();
            }
        }

        for(Toast var1 : this.queued) {
            if (param0.isAssignableFrom(var1.getClass()) && var1.getToken().equals(param1)) {
                return (T)var1;
            }
        }

        return null;
    }

    public void clear() {
        Arrays.fill(this.visible, null);
        this.queued.clear();
    }

    public void addToast(Toast param0) {
        this.queued.add(param0);
    }

    public Minecraft getMinecraft() {
        return this.minecraft;
    }

    @OnlyIn(Dist.CLIENT)
    class ToastInstance<T extends Toast> {
        private final T toast;
        private long animationTime = -1L;
        private long visibleTime = -1L;
        private Toast.Visibility visibility = Toast.Visibility.SHOW;

        private ToastInstance(T param0) {
            this.toast = param0;
        }

        public T getToast() {
            return this.toast;
        }

        private float getVisibility(long param0) {
            float var0 = Mth.clamp((float)(param0 - this.animationTime) / 600.0F, 0.0F, 1.0F);
            var0 *= var0;
            return this.visibility == Toast.Visibility.HIDE ? 1.0F - var0 : var0;
        }

        public boolean render(int param0, int param1, PoseStack param2) {
            long var0 = Util.getMillis();
            if (this.animationTime == -1L) {
                this.animationTime = var0;
                this.visibility.playSound(ToastComponent.this.minecraft.getSoundManager());
            }

            if (this.visibility == Toast.Visibility.SHOW && var0 - this.animationTime <= 600L) {
                this.visibleTime = var0;
            }

            PoseStack var1 = RenderSystem.getModelViewStack();
            var1.pushPose();
            var1.translate(
                (double)((float)param0 - (float)this.toast.width() * this.getVisibility(var0)), (double)(param1 * this.toast.height()), (double)(800 + param1)
            );
            RenderSystem.applyModelViewMatrix();
            Toast.Visibility var2 = this.toast.render(param2, ToastComponent.this, var0 - this.visibleTime);
            var1.popPose();
            RenderSystem.applyModelViewMatrix();
            if (var2 != this.visibility) {
                this.animationTime = var0 - (long)((int)((1.0F - this.getVisibility(var0)) * 600.0F));
                this.visibility = var2;
                this.visibility.playSound(ToastComponent.this.minecraft.getSoundManager());
            }

            return this.visibility == Toast.Visibility.HIDE && var0 - this.animationTime > 600L;
        }
    }
}
