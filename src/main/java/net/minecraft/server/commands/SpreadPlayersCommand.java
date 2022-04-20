package net.minecraft.server.commands;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic4CommandExceptionType;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.scores.Team;

public class SpreadPlayersCommand {
    private static final int MAX_ITERATION_COUNT = 10000;
    private static final Dynamic4CommandExceptionType ERROR_FAILED_TO_SPREAD_TEAMS = new Dynamic4CommandExceptionType(
        (param0, param1, param2, param3) -> Component.translatable("commands.spreadplayers.failed.teams", param0, param1, param2, param3)
    );
    private static final Dynamic4CommandExceptionType ERROR_FAILED_TO_SPREAD_ENTITIES = new Dynamic4CommandExceptionType(
        (param0, param1, param2, param3) -> Component.translatable("commands.spreadplayers.failed.entities", param0, param1, param2, param3)
    );
    private static final Dynamic2CommandExceptionType ERROR_INVALID_MAX_HEIGHT = new Dynamic2CommandExceptionType(
        (param0, param1) -> Component.translatable("commands.spreadplayers.failed.invalid.height", param0, param1)
    );

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("spreadplayers")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    Commands.argument("center", Vec2Argument.vec2())
                        .then(
                            Commands.argument("spreadDistance", FloatArgumentType.floatArg(0.0F))
                                .then(
                                    Commands.argument("maxRange", FloatArgumentType.floatArg(1.0F))
                                        .then(
                                            Commands.argument("respectTeams", BoolArgumentType.bool())
                                                .then(
                                                    Commands.argument("targets", EntityArgument.entities())
                                                        .executes(
                                                            param0x -> spreadPlayers(
                                                                    param0x.getSource(),
                                                                    Vec2Argument.getVec2(param0x, "center"),
                                                                    FloatArgumentType.getFloat(param0x, "spreadDistance"),
                                                                    FloatArgumentType.getFloat(param0x, "maxRange"),
                                                                    param0x.getSource().getLevel().getMaxBuildHeight(),
                                                                    BoolArgumentType.getBool(param0x, "respectTeams"),
                                                                    EntityArgument.getEntities(param0x, "targets")
                                                                )
                                                        )
                                                )
                                        )
                                        .then(
                                            Commands.literal("under")
                                                .then(
                                                    Commands.argument("maxHeight", IntegerArgumentType.integer())
                                                        .then(
                                                            Commands.argument("respectTeams", BoolArgumentType.bool())
                                                                .then(
                                                                    Commands.argument("targets", EntityArgument.entities())
                                                                        .executes(
                                                                            param0x -> spreadPlayers(
                                                                                    param0x.getSource(),
                                                                                    Vec2Argument.getVec2(param0x, "center"),
                                                                                    FloatArgumentType.getFloat(param0x, "spreadDistance"),
                                                                                    FloatArgumentType.getFloat(param0x, "maxRange"),
                                                                                    IntegerArgumentType.getInteger(param0x, "maxHeight"),
                                                                                    BoolArgumentType.getBool(param0x, "respectTeams"),
                                                                                    EntityArgument.getEntities(param0x, "targets")
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private static int spreadPlayers(
        CommandSourceStack param0, Vec2 param1, float param2, float param3, int param4, boolean param5, Collection<? extends Entity> param6
    ) throws CommandSyntaxException {
        ServerLevel var0 = param0.getLevel();
        int var1 = var0.getMinBuildHeight();
        if (param4 < var1) {
            throw ERROR_INVALID_MAX_HEIGHT.create(param4, var1);
        } else {
            RandomSource var2 = RandomSource.create();
            double var3 = (double)(param1.x - param3);
            double var4 = (double)(param1.y - param3);
            double var5 = (double)(param1.x + param3);
            double var6 = (double)(param1.y + param3);
            SpreadPlayersCommand.Position[] var7 = createInitialPositions(var2, param5 ? getNumberOfTeams(param6) : param6.size(), var3, var4, var5, var6);
            spreadPositions(param1, (double)param2, var0, var2, var3, var4, var5, var6, param4, var7, param5);
            double var8 = setPlayerPositions(param6, var0, var7, param4, param5);
            param0.sendSuccess(
                Component.translatable(
                    "commands.spreadplayers.success." + (param5 ? "teams" : "entities"),
                    var7.length,
                    param1.x,
                    param1.y,
                    String.format(Locale.ROOT, "%.2f", var8)
                ),
                true
            );
            return var7.length;
        }
    }

    private static int getNumberOfTeams(Collection<? extends Entity> param0) {
        Set<Team> var0 = Sets.newHashSet();

        for(Entity var1 : param0) {
            if (var1 instanceof Player) {
                var0.add(var1.getTeam());
            } else {
                var0.add(null);
            }
        }

        return var0.size();
    }

    private static void spreadPositions(
        Vec2 param0,
        double param1,
        ServerLevel param2,
        RandomSource param3,
        double param4,
        double param5,
        double param6,
        double param7,
        int param8,
        SpreadPlayersCommand.Position[] param9,
        boolean param10
    ) throws CommandSyntaxException {
        boolean var0 = true;
        double var1 = Float.MAX_VALUE;

        int var2;
        for(var2 = 0; var2 < 10000 && var0; ++var2) {
            var0 = false;
            var1 = Float.MAX_VALUE;

            for(int var3 = 0; var3 < param9.length; ++var3) {
                SpreadPlayersCommand.Position var4 = param9[var3];
                int var5 = 0;
                SpreadPlayersCommand.Position var6 = new SpreadPlayersCommand.Position();

                for(int var7 = 0; var7 < param9.length; ++var7) {
                    if (var3 != var7) {
                        SpreadPlayersCommand.Position var8 = param9[var7];
                        double var9 = var4.dist(var8);
                        var1 = Math.min(var9, var1);
                        if (var9 < param1) {
                            ++var5;
                            var6.x += var8.x - var4.x;
                            var6.z += var8.z - var4.z;
                        }
                    }
                }

                if (var5 > 0) {
                    var6.x /= (double)var5;
                    var6.z /= (double)var5;
                    double var10 = var6.getLength();
                    if (var10 > 0.0) {
                        var6.normalize();
                        var4.moveAway(var6);
                    } else {
                        var4.randomize(param3, param4, param5, param6, param7);
                    }

                    var0 = true;
                }

                if (var4.clamp(param4, param5, param6, param7)) {
                    var0 = true;
                }
            }

            if (!var0) {
                for(SpreadPlayersCommand.Position var11 : param9) {
                    if (!var11.isSafe(param2, param8)) {
                        var11.randomize(param3, param4, param5, param6, param7);
                        var0 = true;
                    }
                }
            }
        }

        if (var1 == Float.MAX_VALUE) {
            var1 = 0.0;
        }

        if (var2 >= 10000) {
            if (param10) {
                throw ERROR_FAILED_TO_SPREAD_TEAMS.create(param9.length, param0.x, param0.y, String.format(Locale.ROOT, "%.2f", var1));
            } else {
                throw ERROR_FAILED_TO_SPREAD_ENTITIES.create(param9.length, param0.x, param0.y, String.format(Locale.ROOT, "%.2f", var1));
            }
        }
    }

    private static double setPlayerPositions(
        Collection<? extends Entity> param0, ServerLevel param1, SpreadPlayersCommand.Position[] param2, int param3, boolean param4
    ) {
        double var0 = 0.0;
        int var1 = 0;
        Map<Team, SpreadPlayersCommand.Position> var2 = Maps.newHashMap();

        for(Entity var3 : param0) {
            SpreadPlayersCommand.Position var5;
            if (param4) {
                Team var4 = var3 instanceof Player ? var3.getTeam() : null;
                if (!var2.containsKey(var4)) {
                    var2.put(var4, param2[var1++]);
                }

                var5 = var2.get(var4);
            } else {
                var5 = param2[var1++];
            }

            var3.teleportToWithTicket((double)Mth.floor(var5.x) + 0.5, (double)var5.getSpawnY(param1, param3), (double)Mth.floor(var5.z) + 0.5);
            double var7 = Double.MAX_VALUE;

            for(SpreadPlayersCommand.Position var8 : param2) {
                if (var5 != var8) {
                    double var9 = var5.dist(var8);
                    var7 = Math.min(var9, var7);
                }
            }

            var0 += var7;
        }

        return param0.size() < 2 ? 0.0 : var0 / (double)param0.size();
    }

    private static SpreadPlayersCommand.Position[] createInitialPositions(
        RandomSource param0, int param1, double param2, double param3, double param4, double param5
    ) {
        SpreadPlayersCommand.Position[] var0 = new SpreadPlayersCommand.Position[param1];

        for(int var1 = 0; var1 < var0.length; ++var1) {
            SpreadPlayersCommand.Position var2 = new SpreadPlayersCommand.Position();
            var2.randomize(param0, param2, param3, param4, param5);
            var0[var1] = var2;
        }

        return var0;
    }

    static class Position {
        double x;
        double z;

        double dist(SpreadPlayersCommand.Position param0) {
            double var0 = this.x - param0.x;
            double var1 = this.z - param0.z;
            return Math.sqrt(var0 * var0 + var1 * var1);
        }

        void normalize() {
            double var0 = this.getLength();
            this.x /= var0;
            this.z /= var0;
        }

        double getLength() {
            return Math.sqrt(this.x * this.x + this.z * this.z);
        }

        public void moveAway(SpreadPlayersCommand.Position param0) {
            this.x -= param0.x;
            this.z -= param0.z;
        }

        public boolean clamp(double param0, double param1, double param2, double param3) {
            boolean var0 = false;
            if (this.x < param0) {
                this.x = param0;
                var0 = true;
            } else if (this.x > param2) {
                this.x = param2;
                var0 = true;
            }

            if (this.z < param1) {
                this.z = param1;
                var0 = true;
            } else if (this.z > param3) {
                this.z = param3;
                var0 = true;
            }

            return var0;
        }

        public int getSpawnY(BlockGetter param0, int param1) {
            BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos(this.x, (double)(param1 + 1), this.z);
            boolean var1 = param0.getBlockState(var0).isAir();
            var0.move(Direction.DOWN);

            boolean var3;
            for(boolean var2 = param0.getBlockState(var0).isAir(); var0.getY() > param0.getMinBuildHeight(); var2 = var3) {
                var0.move(Direction.DOWN);
                var3 = param0.getBlockState(var0).isAir();
                if (!var3 && var2 && var1) {
                    return var0.getY() + 1;
                }

                var1 = var2;
            }

            return param1 + 1;
        }

        public boolean isSafe(BlockGetter param0, int param1) {
            BlockPos var0 = new BlockPos(this.x, (double)(this.getSpawnY(param0, param1) - 1), this.z);
            BlockState var1 = param0.getBlockState(var0);
            Material var2 = var1.getMaterial();
            return var0.getY() < param1 && !var2.isLiquid() && var2 != Material.FIRE;
        }

        public void randomize(RandomSource param0, double param1, double param2, double param3, double param4) {
            this.x = Mth.nextDouble(param0, param1, param3);
            this.z = Mth.nextDouble(param0, param2, param4);
        }
    }
}
