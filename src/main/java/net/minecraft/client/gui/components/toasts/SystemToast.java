package net.minecraft.client.gui.components.toasts;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SystemToast implements Toast {
    private static final ResourceLocation BACKGROUND_SPRITE = new ResourceLocation("toast/system");
    private static final int MAX_LINE_SIZE = 200;
    private static final int LINE_SPACING = 12;
    private static final int MARGIN = 10;
    private final SystemToast.SystemToastId id;
    private Component title;
    private List<FormattedCharSequence> messageLines;
    private long lastChanged;
    private boolean changed;
    private final int width;
    private boolean forceHide;

    public SystemToast(SystemToast.SystemToastId param0, Component param1, @Nullable Component param2) {
        this(
            param0,
            param1,
            nullToEmpty(param2),
            Math.max(160, 30 + Math.max(Minecraft.getInstance().font.width(param1), param2 == null ? 0 : Minecraft.getInstance().font.width(param2)))
        );
    }

    public static SystemToast multiline(Minecraft param0, SystemToast.SystemToastId param1, Component param2, Component param3) {
        Font var0 = param0.font;
        List<FormattedCharSequence> var1 = var0.split(param3, 200);
        int var2 = Math.max(200, var1.stream().mapToInt(var0::width).max().orElse(200));
        return new SystemToast(param1, param2, var1, var2 + 30);
    }

    private SystemToast(SystemToast.SystemToastId param0, Component param1, List<FormattedCharSequence> param2, int param3) {
        this.id = param0;
        this.title = param1;
        this.messageLines = param2;
        this.width = param3;
    }

    private static ImmutableList<FormattedCharSequence> nullToEmpty(@Nullable Component param0) {
        return param0 == null ? ImmutableList.of() : ImmutableList.of(param0.getVisualOrderText());
    }

    @Override
    public int width() {
        return this.width;
    }

    @Override
    public int height() {
        return 20 + Math.max(this.messageLines.size(), 1) * 12;
    }

    public void forceHide() {
        this.forceHide = true;
    }

    @Override
    public Toast.Visibility render(GuiGraphics param0, ToastComponent param1, long param2) {
        if (this.changed) {
            this.lastChanged = param2;
            this.changed = false;
        }

        int var0 = this.width();
        if (var0 == 160 && this.messageLines.size() <= 1) {
            param0.blitSprite(BACKGROUND_SPRITE, 0, 0, var0, this.height());
        } else {
            int var1 = this.height();
            int var2 = 28;
            int var3 = Math.min(4, var1 - 28);
            this.renderBackgroundRow(param0, var0, 0, 0, 28);

            for(int var4 = 28; var4 < var1 - var3; var4 += 10) {
                this.renderBackgroundRow(param0, var0, 16, var4, Math.min(16, var1 - var4 - var3));
            }

            this.renderBackgroundRow(param0, var0, 32 - var3, var1 - var3, var3);
        }

        if (this.messageLines.isEmpty()) {
            param0.drawString(param1.getMinecraft().font, this.title, 18, 12, -256, false);
        } else {
            param0.drawString(param1.getMinecraft().font, this.title, 18, 7, -256, false);

            for(int var5 = 0; var5 < this.messageLines.size(); ++var5) {
                param0.drawString(param1.getMinecraft().font, this.messageLines.get(var5), 18, 18 + var5 * 12, -1, false);
            }
        }

        double var6 = (double)this.id.displayTime * param1.getNotificationDisplayTimeMultiplier();
        long var7 = param2 - this.lastChanged;
        return !this.forceHide && (double)var7 < var6 ? Toast.Visibility.SHOW : Toast.Visibility.HIDE;
    }

    private void renderBackgroundRow(GuiGraphics param0, int param1, int param2, int param3, int param4) {
        int var0 = param2 == 0 ? 20 : 5;
        int var1 = Math.min(60, param1 - var0);
        ResourceLocation var2 = BACKGROUND_SPRITE;
        param0.blitSprite(var2, 160, 32, 0, param2, 0, param3, var0, param4);

        for(int var3 = var0; var3 < param1 - var1; var3 += 64) {
            param0.blitSprite(var2, 160, 32, 32, param2, var3, param3, Math.min(64, param1 - var3 - var1), param4);
        }

        param0.blitSprite(var2, 160, 32, 160 - var1, param2, param1 - var1, param3, var1, param4);
    }

    public void reset(Component param0, @Nullable Component param1) {
        this.title = param0;
        this.messageLines = nullToEmpty(param1);
        this.changed = true;
    }

    public SystemToast.SystemToastId getToken() {
        return this.id;
    }

    public static void add(ToastComponent param0, SystemToast.SystemToastId param1, Component param2, @Nullable Component param3) {
        param0.addToast(new SystemToast(param1, param2, param3));
    }

    public static void addOrUpdate(ToastComponent param0, SystemToast.SystemToastId param1, Component param2, @Nullable Component param3) {
        SystemToast var0 = param0.getToast(SystemToast.class, param1);
        if (var0 == null) {
            add(param0, param1, param2, param3);
        } else {
            var0.reset(param2, param3);
        }

    }

    public static void forceHide(ToastComponent param0, SystemToast.SystemToastId param1) {
        SystemToast var0 = param0.getToast(SystemToast.class, param1);
        if (var0 != null) {
            var0.forceHide();
        }

    }

    public static void onWorldAccessFailure(Minecraft param0, String param1) {
        add(param0.getToasts(), SystemToast.SystemToastId.WORLD_ACCESS_FAILURE, Component.translatable("selectWorld.access_failure"), Component.literal(param1));
    }

    public static void onWorldDeleteFailure(Minecraft param0, String param1) {
        add(param0.getToasts(), SystemToast.SystemToastId.WORLD_ACCESS_FAILURE, Component.translatable("selectWorld.delete_failure"), Component.literal(param1));
    }

    public static void onPackCopyFailure(Minecraft param0, String param1) {
        add(param0.getToasts(), SystemToast.SystemToastId.PACK_COPY_FAILURE, Component.translatable("pack.copyFailure"), Component.literal(param1));
    }

    @OnlyIn(Dist.CLIENT)
    public static class SystemToastId {
        public static final SystemToast.SystemToastId NARRATOR_TOGGLE = new SystemToast.SystemToastId();
        public static final SystemToast.SystemToastId WORLD_BACKUP = new SystemToast.SystemToastId();
        public static final SystemToast.SystemToastId PACK_LOAD_FAILURE = new SystemToast.SystemToastId();
        public static final SystemToast.SystemToastId WORLD_ACCESS_FAILURE = new SystemToast.SystemToastId();
        public static final SystemToast.SystemToastId PACK_COPY_FAILURE = new SystemToast.SystemToastId();
        public static final SystemToast.SystemToastId PERIODIC_NOTIFICATION = new SystemToast.SystemToastId();
        public static final SystemToast.SystemToastId UNSECURE_SERVER_WARNING = new SystemToast.SystemToastId(10000L);
        final long displayTime;

        public SystemToastId(long param0) {
            this.displayTime = param0;
        }

        public SystemToastId() {
            this(5000L);
        }
    }
}
