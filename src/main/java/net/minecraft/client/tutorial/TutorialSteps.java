package net.minecraft.client.tutorial;

import java.util.function.Function;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum TutorialSteps {
    MOVEMENT("movement", MovementTutorialStepInstance::new),
    FIND_TREE("find_tree", FindTreeTutorialStepInstance::new),
    PUNCH_TREE("punch_tree", PunchTreeTutorialStepInstance::new),
    OPEN_INVENTORY("open_inventory", OpenInventoryTutorialStep::new),
    CRAFT_PLANKS("craft_planks", CraftPlanksTutorialStep::new),
    NONE("none", CompletedTutorialStepInstance::new);

    private final String name;
    private final Function<Tutorial, ? extends TutorialStepInstance> constructor;

    private <T extends TutorialStepInstance> TutorialSteps(String param0, Function<Tutorial, T> param1) {
        this.name = param0;
        this.constructor = param1;
    }

    public TutorialStepInstance create(Tutorial param0) {
        return this.constructor.apply(param0);
    }

    public String getName() {
        return this.name;
    }

    public static TutorialSteps getByName(String param0) {
        for(TutorialSteps var0 : values()) {
            if (var0.name.equals(param0)) {
                return var0;
            }
        }

        return NONE;
    }
}
