package net.minecraft.server.advancements;

import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.DisplayInfo;

public class AdvancementVisibilityEvaluator {
    private static final int VISIBILITY_DEPTH = 2;

    private static AdvancementVisibilityEvaluator.VisibilityRule evaluateVisibilityRule(Advancement param0, boolean param1) {
        Optional<DisplayInfo> var0 = param0.display();
        if (var0.isEmpty()) {
            return AdvancementVisibilityEvaluator.VisibilityRule.HIDE;
        } else if (param1) {
            return AdvancementVisibilityEvaluator.VisibilityRule.SHOW;
        } else {
            return var0.get().isHidden() ? AdvancementVisibilityEvaluator.VisibilityRule.HIDE : AdvancementVisibilityEvaluator.VisibilityRule.NO_CHANGE;
        }
    }

    private static boolean evaluateVisiblityForUnfinishedNode(Stack<AdvancementVisibilityEvaluator.VisibilityRule> param0) {
        for(int var0 = 0; var0 <= 2; ++var0) {
            AdvancementVisibilityEvaluator.VisibilityRule var1 = param0.peek(var0);
            if (var1 == AdvancementVisibilityEvaluator.VisibilityRule.SHOW) {
                return true;
            }

            if (var1 == AdvancementVisibilityEvaluator.VisibilityRule.HIDE) {
                return false;
            }
        }

        return false;
    }

    private static boolean evaluateVisibility(
        AdvancementNode param0,
        Stack<AdvancementVisibilityEvaluator.VisibilityRule> param1,
        Predicate<AdvancementNode> param2,
        AdvancementVisibilityEvaluator.Output param3
    ) {
        boolean var0 = param2.test(param0);
        AdvancementVisibilityEvaluator.VisibilityRule var1 = evaluateVisibilityRule(param0.advancement(), var0);
        boolean var2 = var0;
        param1.push(var1);

        for(AdvancementNode var3 : param0.children()) {
            var2 |= evaluateVisibility(var3, param1, param2, param3);
        }

        boolean var4 = var2 || evaluateVisiblityForUnfinishedNode(param1);
        param1.pop();
        param3.accept(param0, var4);
        return var2;
    }

    public static void evaluateVisibility(AdvancementNode param0, Predicate<AdvancementNode> param1, AdvancementVisibilityEvaluator.Output param2) {
        AdvancementNode var0 = param0.root();
        Stack<AdvancementVisibilityEvaluator.VisibilityRule> var1 = new ObjectArrayList<>();

        for(int var2 = 0; var2 <= 2; ++var2) {
            var1.push(AdvancementVisibilityEvaluator.VisibilityRule.NO_CHANGE);
        }

        evaluateVisibility(var0, var1, param1, param2);
    }

    @FunctionalInterface
    public interface Output {
        void accept(AdvancementNode var1, boolean var2);
    }

    static enum VisibilityRule {
        SHOW,
        HIDE,
        NO_CHANGE;
    }
}
