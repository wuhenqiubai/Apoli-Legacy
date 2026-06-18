package io.github.apace100.apoli.util;

import com.mojang.serialization.Codec;

import java.util.function.BiFunction;

public enum Comparison {

    NONE("", (a, b) -> false),
    EQUAL("==", Double::equals),
    LESS_THAN("<", (a, b) -> a < b),
    GREATER_THAN(">", (a, b) -> a > b),
    LESS_THAN_OR_EQUAL("<=", (a, b) -> a <= b),
    GREATER_THAN_OR_EQUAL(">=", (a, b) -> a >= b),
    NOT_EQUAL("!=", (a, b) -> !a.equals(b));

    public static final Codec<Comparison> CODEC = Codec.STRING.xmap(Comparison::getFromString, Comparison::getComparisonString);

    private final String comparisonString;
    private final BiFunction<Double, Double, Boolean> comparison;

    private Comparison(String comparisonString, BiFunction<Double, Double, Boolean> comparison) {
        this.comparisonString = comparisonString;
        this.comparison = comparison;
    }

    public boolean compare(double a, double b) {
        return comparison.apply(a, b);
    }

    public String getComparisonString() {
        return comparisonString;
    }

    public static Comparison getFromString(String comparisonString) {
        return switch (comparisonString) {
            case "==" -> EQUAL;
            case "<" -> LESS_THAN;
            case ">" -> GREATER_THAN;
            case "<=" -> LESS_THAN_OR_EQUAL;
            case ">=" -> GREATER_THAN_OR_EQUAL;
            case "!=" -> NOT_EQUAL;
            default -> NONE;
        };
    }
}
