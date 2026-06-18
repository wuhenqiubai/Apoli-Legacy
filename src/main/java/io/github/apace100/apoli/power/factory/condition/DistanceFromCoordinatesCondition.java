package io.github.apace100.apoli.power.factory.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.condition.block.BlockCondition;
import io.github.apace100.apoli.power.factory.condition.entity.EntityCondition;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.apoli.util.Shape;
import io.github.apace100.apoli.util.codec.ApoliCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.phys.Vec3;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Optional;

/**
 * @author Alluysl
 * @author BluSpring
 * Handles the registry of the distance_from_spawn condition in both block and entity conditions to avoid duplicating code.
 * */
// Using doubles and not ints because the player position is a vector of doubles and the sqrt function (for the distance) returns a double so we might as well use that precision
public record DistanceFromCoordinatesCondition(
    Reference reference, // the reference point
//  boolean checkModifiedSpawn, // whether to check for modified spawns
    Vec3 offset, // offset to the reference point
    Vec3 coordinates, // adds up (instead of replacing, for simplicity) to the prior for aliasing
    boolean ignoreX, boolean ignoreY, boolean ignoreZ, // ignore the axis in the distance calculation
    Shape shape, // the shape / distance type
    boolean scaleReferenceToDimension, // whether to scale the reference's coordinates according to the dimension it's in and the player is in
    boolean scaleDistanceToDimension, // whether to scale the calculated distance to the current dimension
    Comparison comparison, double compareTo,
    Optional<Boolean> resultOnWrongDimension, // if set and the dimension is not the same as the reference's, the value to set the condition to
    Optional<Integer> roundToDigit // if set, rounds the distance to this amount of digits (e.g. 0 for unitary values, 1 for decimals, -1 for multiples of ten)
) {
    public static final MapCodec<DistanceFromCoordinatesCondition> SPAWN_CODEC = codec(false);
    public static final MapCodec<DistanceFromCoordinatesCondition> ORIGIN_CODEC = codec(true);

    private static MapCodec<DistanceFromCoordinatesCondition> codec(boolean isFromWorldOrigin) {
        return RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                Reference.CODEC.optionalFieldOf("reference", isFromWorldOrigin ? Reference.WORLD_ORIGIN : Reference.WORLD_SPAWN)
                    .forGetter(DistanceFromCoordinatesCondition::reference),
                ApoliCodecs.VECTOR.optionalFieldOf("offset", Vec3.ZERO)
                    .forGetter(DistanceFromCoordinatesCondition::offset),
                ApoliCodecs.VECTOR.optionalFieldOf("coordinates", Vec3.ZERO)
                    .forGetter(DistanceFromCoordinatesCondition::coordinates),
                Codec.BOOL.optionalFieldOf("ignore_x", false)
                    .forGetter(DistanceFromCoordinatesCondition::ignoreX),
                Codec.BOOL.optionalFieldOf("ignore_y", false)
                    .forGetter(DistanceFromCoordinatesCondition::ignoreY),
                Codec.BOOL.optionalFieldOf("ignore_z", false)
                    .forGetter(DistanceFromCoordinatesCondition::ignoreZ),
                Shape.CODEC.optionalFieldOf("shape", Shape.CUBE)
                    .forGetter(DistanceFromCoordinatesCondition::shape),
                Codec.BOOL.optionalFieldOf("scale_reference_to_dimension", true)
                    .forGetter(DistanceFromCoordinatesCondition::scaleReferenceToDimension),
                Codec.BOOL.optionalFieldOf("scale_distance_to_dimension", false)
                    .forGetter(DistanceFromCoordinatesCondition::scaleDistanceToDimension),
                Comparison.CODEC.fieldOf("comparison")
                    .forGetter(DistanceFromCoordinatesCondition::comparison),
                Codec.DOUBLE.fieldOf("compare_to")
                    .forGetter(DistanceFromCoordinatesCondition::compareTo),
                Codec.BOOL.optionalFieldOf("result_on_wrong_dimension")
                    .forGetter(DistanceFromCoordinatesCondition::resultOnWrongDimension),
                Codec.INT.optionalFieldOf("round_to_digit")
                    .forGetter(DistanceFromCoordinatesCondition::roundToDigit)
            )
                .apply(instance, DistanceFromCoordinatesCondition::new)
        );
    }

    /**
     * Tests the distance_from_spawn condition for either a block or an entity.
     * No more and no less than one of either the block or entity argument must be null.
     * @param block the block to check the condition for
     * @param entity the entity to check the condition for
     * @return the result of the distance comparison
     * */
    public boolean test(BlockInWorld block, Entity entity) {
        boolean scaleReferenceToDimension = this.scaleReferenceToDimension(),
            setResultOnWrongDimension = this.resultOnWrongDimension().isPresent(),
            resultOnWrongDimension = this.resultOnWrongDimension().orElse(false);
        double x = 0, y = 0, z = 0;
        Vec3 pos;
        Level world;
        // Get the world and its scale from the block/entity
        if (block != null) {
            BlockPos blockPos = block.getPos();
            pos = new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ());
            LevelReader worldView = block.getLevel();
            if (!(worldView instanceof Level))
                return warnCouldNotGetObject("world", "block", compareOutOfBounds(this.comparison()));
            else
                world = (Level) worldView;
        } else {
            pos = entity.position();
            world = entity.level();
        }
        double currentDimensionCoordinateScale = world.dimensionType().coordinateScale();

        // Get the reference's scaled coordinates
        switch (this.reference()) {
            case PLAYER_SPAWN:
//                 if (entity instanceof ServerPlayerEntity) { // null instance of AnyClass is always false so the block case is covered
//
//                 }
//                 // No break on purpose (defaulting to natural spawn)
            case PLAYER_NATURAL_SPAWN: // spawn not set through commands or beds/anchors
                if (entity instanceof Player) { // && data.getBoolean("check_modified_spawn")){
                    warnOnce("Used reference '" + this.reference().getSerializedName() + "' which is not implemented yet, defaulting to world spawn.");
                }
                // No break on purpose (defaulting to world spawn)
                if (entity == null)
                    warnOnce("Used entity-condition-only reference point in block condition, defaulting to world spawn.");
            case WORLD_SPAWN:
                if (setResultOnWrongDimension && world.dimension() != Level.OVERWORLD)
                    return resultOnWrongDimension;
                BlockPos spawnPos = world.getRespawnData().pos();
                x = spawnPos.getX();
                y = spawnPos.getY();
                z = spawnPos.getZ();
                break;
            case WORLD_ORIGIN:
                break;
        }
        Vec3 coords = this.coordinates();
        Vec3 offset = this.offset();
        x += coords.x + offset.x;
        y += coords.y + offset.y;
        z += coords.z + offset.z;
        if (scaleReferenceToDimension && (x != 0 || z != 0)){
            if (currentDimensionCoordinateScale == 0) // pocket dimensions?
                // coordinate scale 0 means it takes 0 blocks to travel in the OW to travel 1 block in the dimension,
                // so the dimension is folded on 0 0, so unless the OW reference is at 0 0, it gets scaled to infinity
                return compareOutOfBounds(this.comparison());
            x /= currentDimensionCoordinateScale;
            z /= currentDimensionCoordinateScale;
        }

        // Get the distance to these coordinates
        double distance,
            xDistance = this.ignoreX() ? 0 : Math.abs(pos.x() - x),
            yDistance = this.ignoreY() ? 0 : Math.abs(pos.y() - y),
            zDistance = this.ignoreZ() ? 0 : Math.abs(pos.z() - z);

        if (this.scaleDistanceToDimension()){
            xDistance *= currentDimensionCoordinateScale;
            zDistance *= currentDimensionCoordinateScale;
        }

        distance = Shape.getDistance(this.shape(), xDistance, yDistance, zDistance);

        if (this.roundToDigit().isPresent())
            distance = new BigDecimal(distance).setScale(this.roundToDigit().orElseThrow(), RoundingMode.HALF_UP).doubleValue();

        return this.comparison().compare(distance, this.compareTo());
    }

    private static final ArrayList<Object> previousWarnings = new ArrayList<>();
    private static void warnOnce(String warning, Object key){
        if (!previousWarnings.contains(key)){
            previousWarnings.add(key);
            Apoli.LOGGER.warn(warning);
        }
    }
    private static void warnOnce(String warning){ warnOnce(warning, warning); }

    /**
     * Infers the logically meaningful result of a distance comparison for out of bounds points (different dimension with corresponding parameter set, or infinite coordinates).
     * @param comparison the comparison set in the data
     * @return the result of that comparison against out-of-bounds points
     * */
    private static boolean compareOutOfBounds(Comparison comparison){
        return comparison == Comparison.NOT_EQUAL || comparison == Comparison.GREATER_THAN || comparison == Comparison.GREATER_THAN_OR_EQUAL;
    }

    /**
     * Warns the user of an issue getting an information needed for expected behavior, but only once (doesn't spam the console).
     * @param object the object that couldn't be acquired
     * @param from the object that was supposed to provide the required object
     * @param assumption the result assumed because of the lack of information
     * @return the assumed result
     * */
    private static <T> T warnCouldNotGetObject(String object, String from, T assumption){
        warnOnce("Could not retrieve " + object + " from " + from + " for distance_from_spawn condition, assuming " + assumption + " for condition.");
        return assumption;
    }

    public enum Reference implements StringRepresentable {
        WORLD_ORIGIN("world_origin"), WORLD_SPAWN("world_spawn"),
        PLAYER_SPAWN("player_spawn"), PLAYER_NATURAL_SPAWN("player_natural_spawn")
        ;

        public static final Codec<Reference> CODEC = StringRepresentable.fromEnum(Reference::values);
        private final String serializedName;

        Reference(String serializedName) {
            this.serializedName = serializedName;
        }

        @Override
        public String getSerializedName() {
            return this.serializedName;
        }
    }

    public record BlockCond(DistanceFromCoordinatesCondition condition, boolean isSpawnCodec) implements BlockCondition {
        public static final MapCodec<BlockCond> ORIGIN_CODEC = DistanceFromCoordinatesCondition.ORIGIN_CODEC.xmap(condition -> new BlockCond(condition, false), BlockCond::condition);
        public static final MapCodec<BlockCond> SPAWN_CODEC = DistanceFromCoordinatesCondition.SPAWN_CODEC.xmap(condition -> new BlockCond(condition, true), BlockCond::condition);

        @Override
        public MapCodec<? extends BlockCondition> codec() {
            return this.isSpawnCodec() ? SPAWN_CODEC : ORIGIN_CODEC;
        }

        @Override
        public boolean test(BlockInWorld blockInWorld) {
            return this.condition().test(blockInWorld, null);
        }
    }

    public record EntityCond(DistanceFromCoordinatesCondition condition, boolean isSpawnCodec) implements EntityCondition {
        public static final MapCodec<EntityCond> ORIGIN_CODEC = DistanceFromCoordinatesCondition.ORIGIN_CODEC.xmap(condition -> new EntityCond(condition, false), EntityCond::condition);
        public static final MapCodec<EntityCond> SPAWN_CODEC = DistanceFromCoordinatesCondition.SPAWN_CODEC.xmap(condition -> new EntityCond(condition, true), EntityCond::condition);

        @Override
        public MapCodec<? extends EntityCondition> codec() {
            return this.isSpawnCodec() ? SPAWN_CODEC : ORIGIN_CODEC;
        }

        @Override
        public boolean test(Entity entity) {
            return this.condition().test(null, entity);
        }
    }
}
