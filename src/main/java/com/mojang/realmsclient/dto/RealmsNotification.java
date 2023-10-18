package com.mojang.realmsclient.dto;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.util.JsonUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PopupScreen;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsNotification {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final String NOTIFICATION_UUID = "notificationUuid";
    private static final String DISMISSABLE = "dismissable";
    private static final String SEEN = "seen";
    private static final String TYPE = "type";
    private static final String VISIT_URL = "visitUrl";
    private static final String INFO_POPUP = "infoPopup";
    static final Component BUTTON_TEXT_FALLBACK = Component.translatable("mco.notification.visitUrl.buttonText.default");
    final UUID uuid;
    final boolean dismissable;
    final boolean seen;
    final String type;

    RealmsNotification(UUID param0, boolean param1, boolean param2, String param3) {
        this.uuid = param0;
        this.dismissable = param1;
        this.seen = param2;
        this.type = param3;
    }

    public boolean seen() {
        return this.seen;
    }

    public boolean dismissable() {
        return this.dismissable;
    }

    public UUID uuid() {
        return this.uuid;
    }

    public static List<RealmsNotification> parseList(String param0) {
        List<RealmsNotification> var0 = new ArrayList<>();

        try {
            for(JsonElement var2 : JsonParser.parseString(param0).getAsJsonObject().get("notifications").getAsJsonArray()) {
                var0.add(parse(var2.getAsJsonObject()));
            }
        } catch (Exception var5) {
            LOGGER.error("Could not parse list of RealmsNotifications", (Throwable)var5);
        }

        return var0;
    }

    private static RealmsNotification parse(JsonObject param0) {
        UUID var0 = JsonUtils.getUuidOr("notificationUuid", param0, null);
        if (var0 == null) {
            throw new IllegalStateException("Missing required property notificationUuid");
        } else {
            boolean var1 = JsonUtils.getBooleanOr("dismissable", param0, true);
            boolean var2 = JsonUtils.getBooleanOr("seen", param0, false);
            String var3 = JsonUtils.getRequiredString("type", param0);
            RealmsNotification var4 = new RealmsNotification(var0, var1, var2, var3);

            return (RealmsNotification)(switch(var3) {
                case "visitUrl" -> RealmsNotification.VisitUrl.parse(var4, param0);
                case "infoPopup" -> RealmsNotification.InfoPopup.parse(var4, param0);
                default -> var4;
            });
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class InfoPopup extends RealmsNotification {
        private static final String TITLE = "title";
        private static final String MESSAGE = "message";
        private static final String IMAGE = "image";
        private static final String URL_BUTTON = "urlButton";
        private final RealmsText title;
        private final RealmsText message;
        private final ResourceLocation image;
        @Nullable
        private final RealmsNotification.UrlButton urlButton;

        private InfoPopup(
            RealmsNotification param0, RealmsText param1, RealmsText param2, ResourceLocation param3, @Nullable RealmsNotification.UrlButton param4
        ) {
            super(param0.uuid, param0.dismissable, param0.seen, param0.type);
            this.title = param1;
            this.message = param2;
            this.image = param3;
            this.urlButton = param4;
        }

        public static RealmsNotification.InfoPopup parse(RealmsNotification param0, JsonObject param1) {
            RealmsText var0 = JsonUtils.getRequired("title", param1, RealmsText::parse);
            RealmsText var1 = JsonUtils.getRequired("message", param1, RealmsText::parse);
            ResourceLocation var2 = new ResourceLocation(JsonUtils.getRequiredString("image", param1));
            RealmsNotification.UrlButton var3 = JsonUtils.getOptional("urlButton", param1, RealmsNotification.UrlButton::parse);
            return new RealmsNotification.InfoPopup(param0, var0, var1, var2, var3);
        }

        @Nullable
        public PopupScreen buildScreen(Screen param0, Consumer<UUID> param1) {
            Component var0 = this.title.createComponent();
            if (var0 == null) {
                RealmsNotification.LOGGER.warn("Realms info popup had title with no available translation: {}", this.title);
                return null;
            } else {
                PopupScreen.Builder var1 = new PopupScreen.Builder(param0, var0)
                    .setImage(this.image)
                    .setMessage(this.message.createComponent(CommonComponents.EMPTY));
                if (this.urlButton != null) {
                    var1.addButton(this.urlButton.urlText.createComponent(RealmsNotification.BUTTON_TEXT_FALLBACK), param2 -> {
                        Minecraft var0x = Minecraft.getInstance();
                        var0x.setScreen(new ConfirmLinkScreen(param3 -> {
                            if (param3) {
                                Util.getPlatform().openUri(this.urlButton.url);
                                var0x.setScreen(param0);
                            } else {
                                var0x.setScreen(param2);
                            }

                        }, this.urlButton.url, true));
                        param1.accept(this.uuid());
                    });
                }

                var1.addButton(CommonComponents.GUI_OK, param1x -> {
                    param1x.onClose();
                    param1.accept(this.uuid());
                });
                var1.onClose(() -> param1.accept(this.uuid()));
                return var1.build();
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    static record UrlButton(String url, RealmsText urlText) {
        private static final String URL = "url";
        private static final String URL_TEXT = "urlText";

        public static RealmsNotification.UrlButton parse(JsonObject param0) {
            String var0 = JsonUtils.getRequiredString("url", param0);
            RealmsText var1 = JsonUtils.getRequired("urlText", param0, RealmsText::parse);
            return new RealmsNotification.UrlButton(var0, var1);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class VisitUrl extends RealmsNotification {
        private static final String URL = "url";
        private static final String BUTTON_TEXT = "buttonText";
        private static final String MESSAGE = "message";
        private final String url;
        private final RealmsText buttonText;
        private final RealmsText message;

        private VisitUrl(RealmsNotification param0, String param1, RealmsText param2, RealmsText param3) {
            super(param0.uuid, param0.dismissable, param0.seen, param0.type);
            this.url = param1;
            this.buttonText = param2;
            this.message = param3;
        }

        public static RealmsNotification.VisitUrl parse(RealmsNotification param0, JsonObject param1) {
            String var0 = JsonUtils.getRequiredString("url", param1);
            RealmsText var1 = JsonUtils.getRequired("buttonText", param1, RealmsText::parse);
            RealmsText var2 = JsonUtils.getRequired("message", param1, RealmsText::parse);
            return new RealmsNotification.VisitUrl(param0, var0, var1, var2);
        }

        public Component getMessage() {
            return this.message.createComponent(Component.translatable("mco.notification.visitUrl.message.default"));
        }

        public Button buildOpenLinkButton(Screen param0) {
            Component var0 = this.buttonText.createComponent(RealmsNotification.BUTTON_TEXT_FALLBACK);
            return Button.builder(var0, ConfirmLinkScreen.confirmLink(param0, this.url)).build();
        }
    }
}
