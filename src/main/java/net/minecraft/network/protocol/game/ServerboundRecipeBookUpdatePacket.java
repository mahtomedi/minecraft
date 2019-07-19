package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ServerboundRecipeBookUpdatePacket implements Packet<ServerGamePacketListener> {
    private ServerboundRecipeBookUpdatePacket.Purpose purpose;
    private ResourceLocation recipe;
    private boolean guiOpen;
    private boolean filteringCraftable;
    private boolean furnaceGuiOpen;
    private boolean furnaceFilteringCraftable;
    private boolean blastFurnaceGuiOpen;
    private boolean blastFurnaceFilteringCraftable;
    private boolean smokerGuiOpen;
    private boolean smokerFilteringCraftable;

    public ServerboundRecipeBookUpdatePacket() {
    }

    public ServerboundRecipeBookUpdatePacket(Recipe<?> param0) {
        this.purpose = ServerboundRecipeBookUpdatePacket.Purpose.SHOWN;
        this.recipe = param0.getId();
    }

    @OnlyIn(Dist.CLIENT)
    public ServerboundRecipeBookUpdatePacket(boolean param0, boolean param1, boolean param2, boolean param3, boolean param4, boolean param5) {
        this.purpose = ServerboundRecipeBookUpdatePacket.Purpose.SETTINGS;
        this.guiOpen = param0;
        this.filteringCraftable = param1;
        this.furnaceGuiOpen = param2;
        this.furnaceFilteringCraftable = param3;
        this.blastFurnaceGuiOpen = param4;
        this.blastFurnaceFilteringCraftable = param5;
        this.smokerGuiOpen = param4;
        this.smokerFilteringCraftable = param5;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.purpose = param0.readEnum(ServerboundRecipeBookUpdatePacket.Purpose.class);
        if (this.purpose == ServerboundRecipeBookUpdatePacket.Purpose.SHOWN) {
            this.recipe = param0.readResourceLocation();
        } else if (this.purpose == ServerboundRecipeBookUpdatePacket.Purpose.SETTINGS) {
            this.guiOpen = param0.readBoolean();
            this.filteringCraftable = param0.readBoolean();
            this.furnaceGuiOpen = param0.readBoolean();
            this.furnaceFilteringCraftable = param0.readBoolean();
            this.blastFurnaceGuiOpen = param0.readBoolean();
            this.blastFurnaceFilteringCraftable = param0.readBoolean();
            this.smokerGuiOpen = param0.readBoolean();
            this.smokerFilteringCraftable = param0.readBoolean();
        }

    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeEnum(this.purpose);
        if (this.purpose == ServerboundRecipeBookUpdatePacket.Purpose.SHOWN) {
            param0.writeResourceLocation(this.recipe);
        } else if (this.purpose == ServerboundRecipeBookUpdatePacket.Purpose.SETTINGS) {
            param0.writeBoolean(this.guiOpen);
            param0.writeBoolean(this.filteringCraftable);
            param0.writeBoolean(this.furnaceGuiOpen);
            param0.writeBoolean(this.furnaceFilteringCraftable);
            param0.writeBoolean(this.blastFurnaceGuiOpen);
            param0.writeBoolean(this.blastFurnaceFilteringCraftable);
            param0.writeBoolean(this.smokerGuiOpen);
            param0.writeBoolean(this.smokerFilteringCraftable);
        }

    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleRecipeBookUpdatePacket(this);
    }

    public ServerboundRecipeBookUpdatePacket.Purpose getPurpose() {
        return this.purpose;
    }

    public ResourceLocation getRecipe() {
        return this.recipe;
    }

    public boolean isGuiOpen() {
        return this.guiOpen;
    }

    public boolean isFilteringCraftable() {
        return this.filteringCraftable;
    }

    public boolean isFurnaceGuiOpen() {
        return this.furnaceGuiOpen;
    }

    public boolean isFurnaceFilteringCraftable() {
        return this.furnaceFilteringCraftable;
    }

    public boolean isBlastFurnaceGuiOpen() {
        return this.blastFurnaceGuiOpen;
    }

    public boolean isBlastFurnaceFilteringCraftable() {
        return this.blastFurnaceFilteringCraftable;
    }

    public boolean isSmokerGuiOpen() {
        return this.smokerGuiOpen;
    }

    public boolean isSmokerFilteringCraftable() {
        return this.smokerFilteringCraftable;
    }

    public static enum Purpose {
        SHOWN,
        SETTINGS;
    }
}
