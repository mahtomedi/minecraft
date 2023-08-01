package net.minecraft.client.gui.screens.advancements;

import net.minecraft.advancements.FrameType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum AdvancementWidgetType {
    OBTAINED(
        new ResourceLocation("advancements/box_obtained"),
        new ResourceLocation("advancements/task_frame_obtained"),
        new ResourceLocation("advancements/challenge_frame_obtained"),
        new ResourceLocation("advancements/goal_frame_obtained")
    ),
    UNOBTAINED(
        new ResourceLocation("advancements/box_unobtained"),
        new ResourceLocation("advancements/task_frame_unobtained"),
        new ResourceLocation("advancements/challenge_frame_unobtained"),
        new ResourceLocation("advancements/goal_frame_unobtained")
    );

    private final ResourceLocation boxSprite;
    private final ResourceLocation taskFrameSprite;
    private final ResourceLocation challengeFrameSprite;
    private final ResourceLocation goalFrameSprite;

    private AdvancementWidgetType(ResourceLocation param0, ResourceLocation param1, ResourceLocation param2, ResourceLocation param3) {
        this.boxSprite = param0;
        this.taskFrameSprite = param1;
        this.challengeFrameSprite = param2;
        this.goalFrameSprite = param3;
    }

    public ResourceLocation boxSprite() {
        return this.boxSprite;
    }

    public ResourceLocation frameSprite(FrameType param0) {
        return switch(param0) {
            case TASK -> this.taskFrameSprite;
            case CHALLENGE -> this.challengeFrameSprite;
            case GOAL -> this.goalFrameSprite;
        };
    }
}
