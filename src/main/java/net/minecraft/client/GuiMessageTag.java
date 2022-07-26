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
    private static final Component CHAT_NOT_SECURE_TEXT = Component.translatable("chat.tag.not_secure").withStyle(ChatFormatting.UNDERLINE);
    private static final Component CHAT_MODIFIED_TEXT = Component.translatable("chat.tag.modified").withStyle(ChatFormatting.UNDERLINE);
    private static final Component CHAT_FILTERED_TEXT = Component.translatable("chat.tag.filtered").withStyle(ChatFormatting.UNDERLINE);
    private static final int SYSTEM_INDICATOR_COLOR = 10526880;
    private static final int CHAT_NOT_SECURE_INDICATOR_COLOR = 15224664;
    private static final int CHAT_MODIFIED_INDICATOR_COLOR = 15386724;
    private static final GuiMessageTag SYSTEM = new GuiMessageTag(10526880, null, null, "System");
    private static final GuiMessageTag CHAT_NOT_SECURE = new GuiMessageTag(15224664, GuiMessageTag.Icon.CHAT_NOT_SECURE, CHAT_NOT_SECURE_TEXT, "Not Secure");
    private static final GuiMessageTag CHAT_FILTERED = new GuiMessageTag(15386724, GuiMessageTag.Icon.CHAT_MODIFIED, CHAT_FILTERED_TEXT, "Filtered");
    static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/gui/chat_tags.png");

    public static GuiMessageTag system() {
        return SYSTEM;
    }

    public static GuiMessageTag chatNotSecure() {
        return CHAT_NOT_SECURE;
    }

    public static GuiMessageTag chatModified(String param0) {
        Component var0 = Component.translatable("chat.tag.modified.original", param0);
        Component var1 = Component.empty().append(CHAT_MODIFIED_TEXT).append(CommonComponents.NEW_LINE).append(var0);
        return new GuiMessageTag(15386724, GuiMessageTag.Icon.CHAT_MODIFIED, var1, "Modified");
    }

    public static GuiMessageTag chatFiltered() {
        return CHAT_FILTERED;
    }

    @OnlyIn(Dist.CLIENT)
    public static enum Icon {
        CHAT_NOT_SECURE(0, 0, 9, 9),
        CHAT_MODIFIED(9, 0, 9, 9);

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
