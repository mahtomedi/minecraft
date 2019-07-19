package net.minecraft.network.chat;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;

public class ScoreComponent extends BaseComponent implements ContextAwareComponent {
    private final String name;
    @Nullable
    private final EntitySelector selector;
    private final String objective;
    private String value = "";

    public ScoreComponent(String param0, String param1) {
        this.name = param0;
        this.objective = param1;
        EntitySelector var0 = null;

        try {
            EntitySelectorParser var1 = new EntitySelectorParser(new StringReader(param0));
            var0 = var1.parse();
        } catch (CommandSyntaxException var5) {
        }

        this.selector = var0;
    }

    public String getName() {
        return this.name;
    }

    public String getObjective() {
        return this.objective;
    }

    public void setValue(String param0) {
        this.value = param0;
    }

    @Override
    public String getContents() {
        return this.value;
    }

    private void resolve(CommandSourceStack param0) {
        MinecraftServer var0 = param0.getServer();
        if (var0 != null && var0.isInitialized() && StringUtil.isNullOrEmpty(this.value)) {
            Scoreboard var1 = var0.getScoreboard();
            Objective var2 = var1.getObjective(this.objective);
            if (var1.hasPlayerScore(this.name, var2)) {
                Score var3 = var1.getOrCreatePlayerScore(this.name, var2);
                this.setValue(String.format("%d", var3.getScore()));
            } else {
                this.value = "";
            }
        }

    }

    public ScoreComponent copy() {
        ScoreComponent var0 = new ScoreComponent(this.name, this.objective);
        var0.setValue(this.value);
        return var0;
    }

    @Override
    public Component resolve(@Nullable CommandSourceStack param0, @Nullable Entity param1, int param2) throws CommandSyntaxException {
        if (param0 == null) {
            return this.copy();
        } else {
            String var1;
            if (this.selector != null) {
                List<? extends Entity> var0 = this.selector.findEntities(param0);
                if (var0.isEmpty()) {
                    var1 = this.name;
                } else {
                    if (var0.size() != 1) {
                        throw EntityArgument.ERROR_NOT_SINGLE_ENTITY.create();
                    }

                    var1 = var0.get(0).getScoreboardName();
                }
            } else {
                var1 = this.name;
            }

            String var5 = param1 != null && var1.equals("*") ? param1.getScoreboardName() : var1;
            ScoreComponent var6 = new ScoreComponent(var5, this.objective);
            var6.setValue(this.value);
            var6.resolve(param0);
            return var6;
        }
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (!(param0 instanceof ScoreComponent)) {
            return false;
        } else {
            ScoreComponent var0 = (ScoreComponent)param0;
            return this.name.equals(var0.name) && this.objective.equals(var0.objective) && super.equals(param0);
        }
    }

    @Override
    public String toString() {
        return "ScoreComponent{name='"
            + this.name
            + '\''
            + "objective='"
            + this.objective
            + '\''
            + ", siblings="
            + this.siblings
            + ", style="
            + this.getStyle()
            + '}';
    }
}
