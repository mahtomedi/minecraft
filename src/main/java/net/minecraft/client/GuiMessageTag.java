package net.minecraft.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record GuiMessageTag(int indicatorColor, @Nullable GuiMessageTag.Icon icon, @Nullable Component text, @Nullable String logTag) {
    private static final Component SYSTEM_TEXT = Component.translatable("chat.tag.system");
    private static final Component SYSTEM_TEXT_SINGLE_PLAYER = Component.translatable("chat.tag.system_single_player");
    private static final Component CHAT_NOT_SECURE_TEXT = Component.translatable("chat.tag.not_secure");
    private static final Component CHAT_MODIFIED_TEXT = Component.translatable("chat.tag.modified");
    private static final int CHAT_NOT_SECURE_INDICATOR_COLOR = 13684944;
    private static final int CHAT_MODIFIED_INDICATOR_COLOR = 6316128;
    private static final GuiMessageTag SYSTEM = new GuiMessageTag(13684944, null, SYSTEM_TEXT, "System");
    private static final GuiMessageTag SYSTEM_SINGLE_PLAYER = new GuiMessageTag(13684944, null, SYSTEM_TEXT_SINGLE_PLAYER, "System");
    private static final GuiMessageTag CHAT_NOT_SECURE = new GuiMessageTag(13684944, null, CHAT_NOT_SECURE_TEXT, "Not Secure");
    static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/gui/chat_tags.png");

    public static GuiMessageTag system() {
        return SYSTEM;
    }

    public static GuiMessageTag systemSinglePlayer() {
        return SYSTEM_SINGLE_PLAYER;
    }

    public static GuiMessageTag chatNotSecure() {
        return CHAT_NOT_SECURE;
    }

    public static GuiMessageTag chatModified(String param0) {
        Component var0 = Component.literal(param0).withStyle(ChatFormatting.GRAY);
        Component var1 = Component.empty().append(CHAT_MODIFIED_TEXT).append(CommonComponents.NEW_LINE).append(var0);
        return new GuiMessageTag(6316128, GuiMessageTag.Icon.CHAT_MODIFIED, var1, "Modified");
    }

    @OnlyIn(Dist.CLIENT)
    public static enum Icon {
        CHAT_MODIFIED(0, 0, 9, 9);

        public final int u;
        public final int v;
        public final int width;
        public final int height;

        private Icon(int param0, int param1, int param2, int param3) {
            this.u = param0;
            this.v = param1;
            this.width = param2;
            this.height = param3;
        }

        public void draw(PoseStack param0, int param1, int param2) {
            RenderSystem.setShaderTexture(0, GuiMessageTag.TEXTURE_LOCATION);
            GuiComponent.blit(param0, param1, param2, (float)this.u, (float)this.v, this.width, this.height, 32, 32);
        }
    }
}
