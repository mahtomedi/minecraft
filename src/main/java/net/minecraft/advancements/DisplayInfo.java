package net.minecraft.advancements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

public class DisplayInfo {
    public static final Codec<DisplayInfo> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    ItemStack.ADVANCEMENT_ICON_CODEC.fieldOf("icon").forGetter(DisplayInfo::getIcon),
                    ComponentSerialization.CODEC.fieldOf("title").forGetter(DisplayInfo::getTitle),
                    ComponentSerialization.CODEC.fieldOf("description").forGetter(DisplayInfo::getDescription),
                    ExtraCodecs.strictOptionalField(ResourceLocation.CODEC, "background").forGetter(DisplayInfo::getBackground),
                    ExtraCodecs.strictOptionalField(AdvancementType.CODEC, "frame", AdvancementType.TASK).forGetter(DisplayInfo::getType),
                    ExtraCodecs.strictOptionalField(Codec.BOOL, "show_toast", true).forGetter(DisplayInfo::shouldShowToast),
                    ExtraCodecs.strictOptionalField(Codec.BOOL, "announce_to_chat", true).forGetter(DisplayInfo::shouldAnnounceChat),
                    ExtraCodecs.strictOptionalField(Codec.BOOL, "hidden", false).forGetter(DisplayInfo::isHidden)
                )
                .apply(param0, DisplayInfo::new)
    );
    private final Component title;
    private final Component description;
    private final ItemStack icon;
    private final Optional<ResourceLocation> background;
    private final AdvancementType type;
    private final boolean showToast;
    private final boolean announceChat;
    private final boolean hidden;
    private float x;
    private float y;

    public DisplayInfo(
        ItemStack param0,
        Component param1,
        Component param2,
        Optional<ResourceLocation> param3,
        AdvancementType param4,
        boolean param5,
        boolean param6,
        boolean param7
    ) {
        this.title = param1;
        this.description = param2;
        this.icon = param0;
        this.background = param3;
        this.type = param4;
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

    public Optional<ResourceLocation> getBackground() {
        return this.background;
    }

    public AdvancementType getType() {
        return this.type;
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

    public void serializeToNetwork(FriendlyByteBuf param0) {
        param0.writeComponent(this.title);
        param0.writeComponent(this.description);
        param0.writeItem(this.icon);
        param0.writeEnum(this.type);
        int var0 = 0;
        if (this.background.isPresent()) {
            var0 |= 1;
        }

        if (this.showToast) {
            var0 |= 2;
        }

        if (this.hidden) {
            var0 |= 4;
        }

        param0.writeInt(var0);
        this.background.ifPresent(param0::writeResourceLocation);
        param0.writeFloat(this.x);
        param0.writeFloat(this.y);
    }

    public static DisplayInfo fromNetwork(FriendlyByteBuf param0) {
        Component var0 = param0.readComponentTrusted();
        Component var1 = param0.readComponentTrusted();
        ItemStack var2 = param0.readItem();
        AdvancementType var3 = param0.readEnum(AdvancementType.class);
        int var4 = param0.readInt();
        Optional<ResourceLocation> var5 = (var4 & 1) != 0 ? Optional.of(param0.readResourceLocation()) : Optional.empty();
        boolean var6 = (var4 & 2) != 0;
        boolean var7 = (var4 & 4) != 0;
        DisplayInfo var8 = new DisplayInfo(var2, var0, var1, var5, var3, var6, false, var7);
        var8.setLocation(param0.readFloat(), param0.readFloat());
        return var8;
    }
}
