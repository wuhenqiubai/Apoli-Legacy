package io.github.apace100.apoli.util;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public enum Shape implements StringRepresentable {
    CUBE("cube"), CHEBYSHEV("chebyshev"),
    STAR("star"), MANHATTAN("manhattan"),
    SPHERE("sphere"), EUCLIDEAN("euclidean");

    private final String serializedName;

    Shape(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return this.serializedName;
    }

    public static final Codec<Shape> CODEC = StringRepresentable.fromValues(Shape::values);

    public static Collection<BlockPos> getPositions(BlockPos center, Shape shape, int radius) {
        Set<BlockPos> positions = new HashSet<>();
        for(int i = -radius; i <= radius; i++) {
            for(int j = -radius; j <= radius; j++) {
                for(int k = -radius; k <= radius; k++) {
                    if(shape == Shape.CUBE || shape == Shape.CHEBYSHEV
                            || (shape == Shape.SPHERE || shape == Shape.EUCLIDEAN)
                                && i * i + j * j + k * k <= radius * radius
                                // The radius can't be negative here (the loops aren't even entered in that case)
                                // so there's no behavior change from testing that sqrt(i*i + j*j + k*k) <= radius
                            || (Math.abs(i) + Math.abs(j) + Math.abs(k)) <= radius) {
                        positions.add(new BlockPos(center.offset(i, j, k)));
                    }
                }
            }
        }
        return positions;
    }

    public static double getDistance(Shape shape, double xDistance, double yDistance, double zDistance){
        return switch (shape){
            case SPHERE, EUCLIDEAN -> Math.sqrt(xDistance * xDistance + yDistance * yDistance + zDistance * zDistance);
            case STAR, MANHATTAN -> xDistance + yDistance + zDistance;
            case CUBE, CHEBYSHEV -> Math.max(Math.max(xDistance, yDistance), zDistance);
        };
    }
}
