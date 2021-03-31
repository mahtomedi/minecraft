package net.minecraft.advancements;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class DisplayInfo {
    private final Component title;
    private final Component description;
    private final ItemStack icon;
    private final ResourceLocation background;
    private final FrameType frame;
    private final boolean showToast;
    private final boolean announceChat;
    private final boolean hidden;
    private float x;
    private float y;

    public DisplayInfo(
        ItemStack param0,
        Component param1,
        Component param2,
        @Nullable ResourceLocation param3,
        FrameType param4,
        boolean param5,
        boolean param6,
        boolean param7
    ) {
        this.title = param1;
        this.description = param2;
        this.icon = param0;
        this.background = param3;
        this.frame = param4;
        this.showToast = param5;
        this.announceChat = param6;
        this.hidden = param7;
    }

    public void setLocation(float param0, float param1) {
        this.x = param0;
        this.y = param1;
    }

    public Component getTitle() {
        return this.title;
    }

    public Component getDescription() {
        return this.description;
    }

    public ItemStack getIcon() {
        return this.icon;
    }

    @Nullable
    public ResourceLocation getBackground() {
        return this.background;
    }

    public FrameType getFrame() {
        return this.frame;
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public boolean shouldShowToast() {
        return this.showToast;
    }

    public boolean shouldAnnounceChat() {
        return this.announceChat;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public static DisplayInfo fromJson(JsonObject param0) {
        Component var0 = Component.Serializer.fromJson(param0.get("title"));
        Component var1 = Component.Serializer.fromJson(param0.get("description"));
        if (var0 != null && var1 != null) {
            ItemStack var2 = getIcon(GsonHelper.getAsJsonObject(param0, "icon"));
            ResourceLocation var3 = param0.has("background") ? new ResourceLocation(GsonHelper.getAsString(param0, "background")) : null;
            FrameType var4 = param0.has("frame") ? FrameType.byName(GsonHelper.getAsString(param0, "frame")) : FrameType.TASK;
            boolean var5 = GsonHelper.getAsBoolean(param0, "show_toast", true);
            boolean var6 = GsonHelper.getAsBoolean(param0, "announce_to_chat", true);
            boolean var7 = GsonHelper.getAsBoolean(param0, "hidden", false);
            return new DisplayInfo(var2, var0, var1, var3, var4, var5, var6, var7);
        } else {
            throw new JsonSyntaxException("Both title and description must be set");
        }
    }

    private static ItemStack getIcon(JsonObject param0) {
        if (!param0.has("item")) {
            throw new JsonSyntaxException("Unsupported icon type, currently only items are supported (add 'item' key)");
        } else {
            Item var0 = GsonHelper.getAsItem(param0, "item");
            if (param0.has("data")) {
                throw new JsonParseException("Disallowed data tag found");
            } else {
                ItemStack var1 = new ItemStack(var0);
                if (param0.has("nbt")) {
                    try {
                        CompoundTag var2 = TagParser.parseTag(GsonHelper.convertToString(param0.get("nbt"), "nbt"));
                        var1.setTag(var2);
                    } catch (CommandSyntaxException var4) {
                        throw new JsonSyntaxException("Invalid nbt tag: " + var4.getMessage());
                    }
                }

                return var1;
            }
        }
    }

    public void serializeToNetwork(FriendlyByteBuf param0) {
        param0.writeComponent(this.title);
        param0.writeComponent(this.description);
        param0.writeItem(this.icon);
        param0.writeEnum(this.frame);
        int var0 = 0;
        if (this.background != null) {
            var0 |= 1;
        }

        if (this.showToast) {
            var0 |= 2;
        }

        if (this.hidden) {
            var0 |= 4;
        }

        param0.writeInt(var0);
        if (this.background != null) {
            param0.writeResourceLocation(this.background);
        }

        param0.writeFloat(this.x);
        param0.writeFloat(this.y);
    }

    public static DisplayInfo fromNetwork(FriendlyByteBuf param0) {
        Component var0 = param0.readComponent();
        Component var1 = param0.readComponent();
        ItemStack var2 = param0.readItem();
        FrameType var3 = param0.readEnum(FrameType.class);
        int var4 = param0.readInt();
        ResourceLocation var5 = (var4 & 1) != 0 ? param0.readResourceLocation() : null;
        boolean var6 = (var4 & 2) != 0;
        boolean var7 = (var4 & 4) != 0;
        DisplayInfo var8 = new DisplayInfo(var2, var0, var1, var5, var3, var6, false, var7);
        var8.setLocation(param0.readFloat(), param0.readFloat());
        return var8;
    }

    public JsonElement serializeToJson() {
        JsonObject var0 = new JsonObject();
        var0.add("icon", this.serializeIcon());
        var0.add("title", Component.Serializer.toJsonTree(this.title));
        var0.add("description", Component.Serializer.toJsonTree(this.description));
        var0.addProperty("frame", this.frame.getName());
        var0.addProperty("show_toast", this.showToast);
        var0.addProperty("announce_to_chat", this.announceChat);
        var0.addProperty("hidden", this.hidden);
        if (this.background != null) {
            var0.addProperty("background", this.background.toString());
        }

        return var0;
    }

    private JsonObject serializeIcon() {
        JsonObject var0 = new JsonObject();
        var0.addProperty("item", Registry.ITEM.getKey(this.icon.getItem()).toString());
        if (this.icon.hasTag()) {
            var0.addProperty("nbt", this.icon.getTag().toString());
        }

        return var0;
    }
}
