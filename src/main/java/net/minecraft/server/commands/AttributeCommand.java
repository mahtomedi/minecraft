package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class AttributeCommand {
    private static final DynamicCommandExceptionType ERROR_NOT_LIVING_ENTITY = new DynamicCommandExceptionType(
        param0 -> new TranslatableComponent("commands.attribute.failed.entity", param0)
    );
    private static final Dynamic2CommandExceptionType ERROR_NO_SUCH_ATTRIBUTE = new Dynamic2CommandExceptionType(
        (param0, param1) -> new TranslatableComponent("commands.attribute.failed.no_attribute", param0, param1)
    );
    private static final Dynamic3CommandExceptionType ERROR_NO_SUCH_MODIFIER = new Dynamic3CommandExceptionType(
        (param0, param1, param2) -> new TranslatableComponent("commands.attribute.failed.no_modifier", param1, param0, param2)
    );
    private static final Dynamic3CommandExceptionType ERROR_MODIFIER_ALREADY_PRESENT = new Dynamic3CommandExceptionType(
        (param0, param1, param2) -> new TranslatableComponent("commands.attribute.failed.modifier_already_present", param2, param1, param0)
    );

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("attribute")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    Commands.argument("target", EntityArgument.entity())
                        .then(
                            Commands.argument("attribute", ResourceKeyArgument.key(Registry.ATTRIBUTE_REGISTRY))
                                .then(
                                    Commands.literal("get")
                                        .executes(
                                            param0x -> getAttributeValue(
                                                    param0x.getSource(),
                                                    EntityArgument.getEntity(param0x, "target"),
                                                    ResourceKeyArgument.getAttribute(param0x, "attribute"),
                                                    1.0
                                                )
                                        )
                                        .then(
                                            Commands.argument("scale", DoubleArgumentType.doubleArg())
                                                .executes(
                                                    param0x -> getAttributeValue(
                                                            param0x.getSource(),
                                                            EntityArgument.getEntity(param0x, "target"),
                                                            ResourceKeyArgument.getAttribute(param0x, "attribute"),
                                                            DoubleArgumentType.getDouble(param0x, "scale")
                                                        )
                                                )
                                        )
                                )
                                .then(
                                    Commands.literal("base")
                                        .then(
                                            Commands.literal("set")
                                                .then(
                                                    Commands.argument("value", DoubleArgumentType.doubleArg())
                                                        .executes(
                                                            param0x -> setAttributeBase(
                                                                    param0x.getSource(),
                                                                    EntityArgument.getEntity(param0x, "target"),
                                                                    ResourceKeyArgument.getAttribute(param0x, "attribute"),
                                                                    DoubleArgumentType.getDouble(param0x, "value")
                                                                )
                                                        )
                                                )
                                        )
                                        .then(
                                            Commands.literal("get")
                                                .executes(
                                                    param0x -> getAttributeBase(
                                                            param0x.getSource(),
                                                            EntityArgument.getEntity(param0x, "target"),
                                                            ResourceKeyArgument.getAttribute(param0x, "attribute"),
                                                            1.0
                                                        )
                                                )
                                                .then(
                                                    Commands.argument("scale", DoubleArgumentType.doubleArg())
                                                        .executes(
                                                            param0x -> getAttributeBase(
                                                                    param0x.getSource(),
                                                                    EntityArgument.getEntity(param0x, "target"),
                                                                    ResourceKeyArgument.getAttribute(param0x, "attribute"),
                                                                    DoubleArgumentType.getDouble(param0x, "scale")
                                                                )
                                                        )
                                                )
                                        )
                                )
                                .then(
                                    Commands.literal("modifier")
                                        .then(
                                            Commands.literal("add")
                                                .then(
                                                    Commands.argument("uuid", UuidArgument.uuid())
                                                        .then(
                                                            Commands.argument("name", StringArgumentType.string())
                                                                .then(
                                                                    Commands.argument("value", DoubleArgumentType.doubleArg())
                                                                        .then(
                                                                            Commands.literal("add")
                                                                                .executes(
                                                                                    param0x -> addModifier(
                                                                                            param0x.getSource(),
                                                                                            EntityArgument.getEntity(param0x, "target"),
                                                                                            ResourceKeyArgument.getAttribute(param0x, "attribute"),
                                                                                            UuidArgument.getUuid(param0x, "uuid"),
                                                                                            StringArgumentType.getString(param0x, "name"),
                                                                                            DoubleArgumentType.getDouble(param0x, "value"),
                                                                                            AttributeModifier.Operation.ADDITION
                                                                                        )
                                                                                )
                                                                        )
                                                                        .then(
                                                                            Commands.literal("multiply")
                                                                                .executes(
                                                                                    param0x -> addModifier(
                                                                                            param0x.getSource(),
                                                                                            EntityArgument.getEntity(param0x, "target"),
                                                                                            ResourceKeyArgument.getAttribute(param0x, "attribute"),
                                                                                            UuidArgument.getUuid(param0x, "uuid"),
                                                                                            StringArgumentType.getString(param0x, "name"),
                                                                                            DoubleArgumentType.getDouble(param0x, "value"),
                                                                                            AttributeModifier.Operation.MULTIPLY_TOTAL
                                                                                        )
                                                                                )
                                                                        )
                                                                        .then(
                                                                            Commands.literal("multiply_base")
                                                                                .executes(
                                                                                    param0x -> addModifier(
                                                                                            param0x.getSource(),
                                                                                            EntityArgument.getEntity(param0x, "target"),
                                                                                            ResourceKeyArgument.getAttribute(param0x, "attribute"),
                                                                                            UuidArgument.getUuid(param0x, "uuid"),
                                                                                            StringArgumentType.getString(param0x, "name"),
                                                                                            DoubleArgumentType.getDouble(param0x, "value"),
                                                                                            AttributeModifier.Operation.MULTIPLY_BASE
                                                                                        )
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                        .then(
                                            Commands.literal("remove")
                                                .then(
                                                    Commands.argument("uuid", UuidArgument.uuid())
                                                        .executes(
                                                            param0x -> removeModifier(
                                                                    param0x.getSource(),
                                                                    EntityArgument.getEntity(param0x, "target"),
                                                                    ResourceKeyArgument.getAttribute(param0x, "attribute"),
                                                                    UuidArgument.getUuid(param0x, "uuid")
                                                                )
                                                        )
                                                )
                                        )
                                        .then(
                                            Commands.literal("value")
                                                .then(
                                                    Commands.literal("get")
                                                        .then(
                                                            Commands.argument("uuid", UuidArgument.uuid())
                                                                .executes(
                                                                    param0x -> getAttributeModifier(
                                                                            param0x.getSource(),
                                                                            EntityArgument.getEntity(param0x, "target"),
                                                                            ResourceKeyArgument.getAttribute(param0x, "attribute"),
                                                                            UuidArgument.getUuid(param0x, "uuid"),
                                                                            1.0
                                                                        )
                                                                )
                                                                .then(
                                                                    Commands.argument("scale", DoubleArgumentType.doubleArg())
                                                                        .executes(
                                                                            param0x -> getAttributeModifier(
                                                                                    param0x.getSource(),
                                                                                    EntityArgument.getEntity(param0x, "target"),
                                                                                    ResourceKeyArgument.getAttribute(param0x, "attribute"),
                                                                                    UuidArgument.getUuid(param0x, "uuid"),
                                                                                    DoubleArgumentType.getDouble(param0x, "scale")
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

    private static AttributeInstance getAttributeInstance(Entity param0, Attribute param1) throws CommandSyntaxException {
        AttributeInstance var0 = getLivingEntity(param0).getAttributes().getInstance(param1);
        if (var0 == null) {
            throw ERROR_NO_SUCH_ATTRIBUTE.create(param0.getName(), new TranslatableComponent(param1.getDescriptionId()));
        } else {
            return var0;
        }
    }

    private static LivingEntity getLivingEntity(Entity param0) throws CommandSyntaxException {
        if (!(param0 instanceof LivingEntity)) {
            throw ERROR_NOT_LIVING_ENTITY.create(param0.getName());
        } else {
            return (LivingEntity)param0;
        }
    }

    private static LivingEntity getEntityWithAttribute(Entity param0, Attribute param1) throws CommandSyntaxException {
        LivingEntity var0 = getLivingEntity(param0);
        if (!var0.getAttributes().hasAttribute(param1)) {
            throw ERROR_NO_SUCH_ATTRIBUTE.create(param0.getName(), new TranslatableComponent(param1.getDescriptionId()));
        } else {
            return var0;
        }
    }

    private static int getAttributeValue(CommandSourceStack param0, Entity param1, Attribute param2, double param3) throws CommandSyntaxException {
        LivingEntity var0 = getEntityWithAttribute(param1, param2);
        double var1 = var0.getAttributeValue(param2);
        param0.sendSuccess(
            new TranslatableComponent("commands.attribute.value.get.success", new TranslatableComponent(param2.getDescriptionId()), param1.getName(), var1),
            false
        );
        return (int)(var1 * param3);
    }

    private static int getAttributeBase(CommandSourceStack param0, Entity param1, Attribute param2, double param3) throws CommandSyntaxException {
        LivingEntity var0 = getEntityWithAttribute(param1, param2);
        double var1 = var0.getAttributeBaseValue(param2);
        param0.sendSuccess(
            new TranslatableComponent("commands.attribute.base_value.get.success", new TranslatableComponent(param2.getDescriptionId()), param1.getName(), var1),
            false
        );
        return (int)(var1 * param3);
    }

    private static int getAttributeModifier(CommandSourceStack param0, Entity param1, Attribute param2, UUID param3, double param4) throws CommandSyntaxException {
        LivingEntity var0 = getEntityWithAttribute(param1, param2);
        AttributeMap var1 = var0.getAttributes();
        if (!var1.hasModifier(param2, param3)) {
            throw ERROR_NO_SUCH_MODIFIER.create(param1.getName(), new TranslatableComponent(param2.getDescriptionId()), param3);
        } else {
            double var2 = var1.getModifierValue(param2, param3);
            param0.sendSuccess(
                new TranslatableComponent(
                    "commands.attribute.modifier.value.get.success", param3, new TranslatableComponent(param2.getDescriptionId()), param1.getName(), var2
                ),
                false
            );
            return (int)(var2 * param4);
        }
    }

    private static int setAttributeBase(CommandSourceStack param0, Entity param1, Attribute param2, double param3) throws CommandSyntaxException {
        getAttributeInstance(param1, param2).setBaseValue(param3);
        param0.sendSuccess(
            new TranslatableComponent(
                "commands.attribute.base_value.set.success", new TranslatableComponent(param2.getDescriptionId()), param1.getName(), param3
            ),
            false
        );
        return 1;
    }

    private static int addModifier(
        CommandSourceStack param0, Entity param1, Attribute param2, UUID param3, String param4, double param5, AttributeModifier.Operation param6
    ) throws CommandSyntaxException {
        AttributeInstance var0 = getAttributeInstance(param1, param2);
        AttributeModifier var1 = new AttributeModifier(param3, param4, param5, param6);
        if (var0.hasModifier(var1)) {
            throw ERROR_MODIFIER_ALREADY_PRESENT.create(param1.getName(), new TranslatableComponent(param2.getDescriptionId()), param3);
        } else {
            var0.addPermanentModifier(var1);
            param0.sendSuccess(
                new TranslatableComponent(
                    "commands.attribute.modifier.add.success", param3, new TranslatableComponent(param2.getDescriptionId()), param1.getName()
                ),
                false
            );
            return 1;
        }
    }

    private static int removeModifier(CommandSourceStack param0, Entity param1, Attribute param2, UUID param3) throws CommandSyntaxException {
        AttributeInstance var0 = getAttributeInstance(param1, param2);
        if (var0.removePermanentModifier(param3)) {
            param0.sendSuccess(
                new TranslatableComponent(
                    "commands.attribute.modifier.remove.success", param3, new TranslatableComponent(param2.getDescriptionId()), param1.getName()
                ),
                false
            );
            return 1;
        } else {
            throw ERROR_NO_SUCH_MODIFIER.create(param1.getName(), new TranslatableComponent(param2.getDescriptionId()), param3);
        }
    }
}
