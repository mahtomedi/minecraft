package net.minecraft.client.gui.components.toasts;

import com.google.common.collect.Queues;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Deque;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ToastComponent extends GuiComponent {
    private static final int SLOT_COUNT = 5;
    private static final int NO_SPACE = -1;
    final Minecraft minecraft;
    private final List<ToastComponent.ToastInstance<?>> visible = new ArrayList<>();
    private final BitSet occupiedSlots = new BitSet(5);
    private final Deque<Toast> queued = Queues.newArrayDeque();

    public ToastComponent(Minecraft param0) {
        this.minecraft = param0;
    }

    public void render(PoseStack param0) {
        if (!this.minecraft.options.hideGui) {
            int var0 = this.minecraft.getWindow().getGuiScaledWidth();
            this.visible.removeIf(param2 -> {
                if (param2 != null && param2.render(var0, param0)) {
                    this.occupiedSlots.clear(param2.index, param2.index + param2.slotCount);
                    return true;
                } else {
                    return false;
                }
            });
            if (!this.queued.isEmpty() && this.freeSlots() > 0) {
                this.queued.removeIf(param0x -> {
                    int var0x = param0x.slotCount();
                    int var1x = this.findFreeIndex(var0x);
                    if (var1x != -1) {
                        this.visible.add(new ToastComponent.ToastInstance<>(param0x, var1x, var0x));
                        this.occupiedSlots.set(var1x, var1x + var0x);
                        return true;
                    } else {
                        return false;
                    }
                });
            }

        }
    }

    private int findFreeIndex(int param0) {
        if (this.freeSlots() >= param0) {
            int var0 = 0;

            for(int var1 = 0; var1 < 5; ++var1) {
                if (this.occupiedSlots.get(var1)) {
                    var0 = 0;
                } else if (++var0 == param0) {
                    return var1 + 1 - var0;
                }
            }
        }

        return -1;
    }

    private int freeSlots() {
        return 5 - this.occupiedSlots.cardinality();
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
        this.occupiedSlots.clear();
        this.visible.clear();
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
        private static final long ANIMATION_TIME = 600L;
        private final T toast;
        final int index;
        final int slotCount;
        private long animationTime = -1L;
        private long visibleTime = -1L;
        private Toast.Visibility visibility = Toast.Visibility.SHOW;

        ToastInstance(T param0, int param1, int param2) {
            this.toast = param0;
            this.index = param1;
            this.slotCount = param2;
        }

        public T getToast() {
            return this.toast;
        }

        private float getVisibility(long param0) {
            float var0 = Mth.clamp((float)(param0 - this.animationTime) / 600.0F, 0.0F, 1.0F);
            var0 *= var0;
            return this.visibility == Toast.Visibility.HIDE ? 1.0F - var0 : var0;
        }

        public boolean render(int param0, PoseStack param1) {
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
            var1.translate((float)param0 - (float)this.toast.width() * this.getVisibility(var0), (float)(this.index * 32), 800.0F);
            RenderSystem.applyModelViewMatrix();
            Toast.Visibility var2 = this.toast.render(param1, ToastComponent.this, var0 - this.visibleTime);
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
