package net.minecraft.commands;

import com.mojang.brigadier.exceptions.BuiltInExceptionProvider;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.network.chat.Component;

public class BrigadierExceptions implements BuiltInExceptionProvider {
    private static final Dynamic2CommandExceptionType DOUBLE_TOO_SMALL = new Dynamic2CommandExceptionType(
        (param0, param1) -> Component.translatable("argument.double.low", param1, param0)
    );
    private static final Dynamic2CommandExceptionType DOUBLE_TOO_BIG = new Dynamic2CommandExceptionType(
        (param0, param1) -> Component.translatable("argument.double.big", param1, param0)
    );
    private static final Dynamic2CommandExceptionType FLOAT_TOO_SMALL = new Dynamic2CommandExceptionType(
        (param0, param1) -> Component.translatable("argument.float.low", param1, param0)
    );
    private static final Dynamic2CommandExceptionType FLOAT_TOO_BIG = new Dynamic2CommandExceptionType(
        (param0, param1) -> Component.translatable("argument.float.big", param1, param0)
    );
    private static final Dynamic2CommandExceptionType INTEGER_TOO_SMALL = new Dynamic2CommandExceptionType(
        (param0, param1) -> Component.translatable("argument.integer.low", param1, param0)
    );
    private static final Dynamic2CommandExceptionType INTEGER_TOO_BIG = new Dynamic2CommandExceptionType(
        (param0, param1) -> Component.translatable("argument.integer.big", param1, param0)
    );
    private static final Dynamic2CommandExceptionType LONG_TOO_SMALL = new Dynamic2CommandExceptionType(
        (param0, param1) -> Component.translatable("argument.long.low", param1, param0)
    );
    private static final Dynamic2CommandExceptionType LONG_TOO_BIG = new Dynamic2CommandExceptionType(
        (param0, param1) -> Component.translatable("argument.long.big", param1, param0)
    );
    private static final DynamicCommandExceptionType LITERAL_INCORRECT = new DynamicCommandExceptionType(
        param0 -> Component.translatable("argument.literal.incorrect", param0)
    );
    private static final SimpleCommandExceptionType READER_EXPECTED_START_OF_QUOTE = new SimpleCommandExceptionType(
        Component.translatable("parsing.quote.expected.start")
    );
    private static final SimpleCommandExceptionType READER_EXPECTED_END_OF_QUOTE = new SimpleCommandExceptionType(
        Component.translatable("parsing.quote.expected.end")
    );
    private static final DynamicCommandExceptionType READER_INVALID_ESCAPE = new DynamicCommandExceptionType(
        param0 -> Component.translatable("parsing.quote.escape", param0)
    );
    private static final DynamicCommandExceptionType READER_INVALID_BOOL = new DynamicCommandExceptionType(
        param0 -> Component.translatable("parsing.bool.invalid", param0)
    );
    private static final DynamicCommandExceptionType READER_INVALID_INT = new DynamicCommandExceptionType(
        param0 -> Component.translatable("parsing.int.invalid", param0)
    );
    private static final SimpleCommandExceptionType READER_EXPECTED_INT = new SimpleCommandExceptionType(Component.translatable("parsing.int.expected"));
    private static final DynamicCommandExceptionType READER_INVALID_LONG = new DynamicCommandExceptionType(
        param0 -> Component.translatable("parsing.long.invalid", param0)
    );
    private static final SimpleCommandExceptionType READER_EXPECTED_LONG = new SimpleCommandExceptionType(Component.translatable("parsing.long.expected"));
    private static final DynamicCommandExceptionType READER_INVALID_DOUBLE = new DynamicCommandExceptionType(
        param0 -> Component.translatable("parsing.double.invalid", param0)
    );
    private static final SimpleCommandExceptionType READER_EXPECTED_DOUBLE = new SimpleCommandExceptionType(Component.translatable("parsing.double.expected"));
    private static final DynamicCommandExceptionType READER_INVALID_FLOAT = new DynamicCommandExceptionType(
        param0 -> Component.translatable("parsing.float.invalid", param0)
    );
    private static final SimpleCommandExceptionType READER_EXPECTED_FLOAT = new SimpleCommandExceptionType(Component.translatable("parsing.float.expected"));
    private static final SimpleCommandExceptionType READER_EXPECTED_BOOL = new SimpleCommandExceptionType(Component.translatable("parsing.bool.expected"));
    private static final DynamicCommandExceptionType READER_EXPECTED_SYMBOL = new DynamicCommandExceptionType(
        param0 -> Component.translatable("parsing.expected", param0)
    );
    private static final SimpleCommandExceptionType DISPATCHER_UNKNOWN_COMMAND = new SimpleCommandExceptionType(
        Component.translatable("command.unknown.command")
    );
    private static final SimpleCommandExceptionType DISPATCHER_UNKNOWN_ARGUMENT = new SimpleCommandExceptionType(
        Component.translatable("command.unknown.argument")
    );
    private static final SimpleCommandExceptionType DISPATCHER_EXPECTED_ARGUMENT_SEPARATOR = new SimpleCommandExceptionType(
        Component.translatable("command.expected.separator")
    );
    private static final DynamicCommandExceptionType DISPATCHER_PARSE_EXCEPTION = new DynamicCommandExceptionType(
        param0 -> Component.translatable("command.exception", param0)
    );

    @Override
    public Dynamic2CommandExceptionType doubleTooLow() {
        return DOUBLE_TOO_SMALL;
    }

    @Override
    public Dynamic2CommandExceptionType doubleTooHigh() {
        return DOUBLE_TOO_BIG;
    }

    @Override
    public Dynamic2CommandExceptionType floatTooLow() {
        return FLOAT_TOO_SMALL;
    }

    @Override
    public Dynamic2CommandExceptionType floatTooHigh() {
        return FLOAT_TOO_BIG;
    }

    @Override
    public Dynamic2CommandExceptionType integerTooLow() {
        return INTEGER_TOO_SMALL;
    }

    @Override
    public Dynamic2CommandExceptionType integerTooHigh() {
        return INTEGER_TOO_BIG;
    }

    @Override
    public Dynamic2CommandExceptionType longTooLow() {
        return LONG_TOO_SMALL;
    }

    @Override
    public Dynamic2CommandExceptionType longTooHigh() {
        return LONG_TOO_BIG;
    }

    @Override
    public DynamicCommandExceptionType literalIncorrect() {
        return LITERAL_INCORRECT;
    }

    @Override
    public SimpleCommandExceptionType readerExpectedStartOfQuote() {
        return READER_EXPECTED_START_OF_QUOTE;
    }

    @Override
    public SimpleCommandExceptionType readerExpectedEndOfQuote() {
        return READER_EXPECTED_END_OF_QUOTE;
    }

    @Override
    public DynamicCommandExceptionType readerInvalidEscape() {
        return READER_INVALID_ESCAPE;
    }

    @Override
    public DynamicCommandExceptionType readerInvalidBool() {
        return READER_INVALID_BOOL;
    }

    @Override
    public DynamicCommandExceptionType readerInvalidInt() {
        return READER_INVALID_INT;
    }

    @Override
    public SimpleCommandExceptionType readerExpectedInt() {
        return READER_EXPECTED_INT;
    }

    @Override
    public DynamicCommandExceptionType readerInvalidLong() {
        return READER_INVALID_LONG;
    }

    @Override
    public SimpleCommandExceptionType readerExpectedLong() {
        return READER_EXPECTED_LONG;
    }

    @Override
    public DynamicCommandExceptionType readerInvalidDouble() {
        return READER_INVALID_DOUBLE;
    }

    @Override
    public SimpleCommandExceptionType readerExpectedDouble() {
        return READER_EXPECTED_DOUBLE;
    }

    @Override
    public DynamicCommandExceptionType readerInvalidFloat() {
        return READER_INVALID_FLOAT;
    }

    @Override
    public SimpleCommandExceptionType readerExpectedFloat() {
        return READER_EXPECTED_FLOAT;
    }

    @Override
    public SimpleCommandExceptionType readerExpectedBool() {
        return READER_EXPECTED_BOOL;
    }

    @Override
    public DynamicCommandExceptionType readerExpectedSymbol() {
        return READER_EXPECTED_SYMBOL;
    }

    @Override
    public SimpleCommandExceptionType dispatcherUnknownCommand() {
        return DISPATCHER_UNKNOWN_COMMAND;
    }

    @Override
    public SimpleCommandExceptionType dispatcherUnknownArgument() {
        return DISPATCHER_UNKNOWN_ARGUMENT;
    }

    @Override
    public SimpleCommandExceptionType dispatcherExpectedArgumentSeparator() {
        return DISPATCHER_EXPECTED_ARGUMENT_SEPARATOR;
    }

    @Override
    public DynamicCommandExceptionType dispatcherParseException() {
        return DISPATCHER_PARSE_EXCEPTION;
    }
}
