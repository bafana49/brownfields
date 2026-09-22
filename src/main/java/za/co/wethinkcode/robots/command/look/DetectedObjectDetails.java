package za.co.wethinkcode.robots.command.look;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import za.co.wethinkcode.robots.Direction;
import za.co.wethinkcode.robots.Position;
import za.co.wethinkcode.robots.obstacle.Obstacle;
import za.co.wethinkcode.robots.robot.Robot;
import za.co.wethinkcode.robots.world.World;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static za.co.wethinkcode.robots.config.Config.*;
import static za.co.wethinkcode.robots.Direction.*;

/**
 * This class handles the detection of objects (edges, obstacles, robots) in the world.
 */
public class DetectedObjectDetails {
    private final List<ObjectDetail> objectDetails;

    /**
     * Constructs a new DetectedObjectDetails object.
     *
     * @param currentPos The current position of the robot
     * @param world The world in which the robot exists
     */
    public DetectedObjectDetails(Position currentPos, World world) {
        objectDetails = new CopyOnWriteArrayList<>();

        Map<Direction, List<ObjectDetail>> obstaclesByDirection = detectObstacles(currentPos, world.getObstacles());
        Map<Direction, List<ObjectDetail>> edgesByDirection = detectEdges(currentPos, obstaclesByDirection);
        Map<Direction, List<ObjectDetail>> robotsByDirection = detectRobots(world.getBots(), currentPos, obstaclesByDirection);

        Map<Direction, List<ObjectDetail>> allObjectsByDirection = combineAndSortObjects(
                obstaclesByDirection, edgesByDirection, robotsByDirection
        );

        processObjectsByDirection(allObjectsByDirection);
    }

    /**
     * Combines and sorts detected objects by direction and distance.
     */
    private Map<Direction, List<ObjectDetail>> combineAndSortObjects(
            Map<Direction, List<ObjectDetail>> obstacles,
            Map<Direction, List<ObjectDetail>> edges,
            Map<Direction, List<ObjectDetail>> robots) {

        Map<Direction, List<ObjectDetail>> allObjects = new EnumMap<>(Direction.class);
        Arrays.stream(Direction.values()).forEach(dir -> {
            List<ObjectDetail> objectsInDir = new ArrayList<>();
            objectsInDir.addAll(obstacles.getOrDefault(dir, Collections.emptyList()));
            objectsInDir.addAll(edges.getOrDefault(dir, Collections.emptyList()));
            objectsInDir.addAll(robots.getOrDefault(dir, Collections.emptyList()));
            objectsInDir.sort(Comparator.comparingInt(ObjectDetail::getDistance));
            allObjects.put(dir, objectsInDir);
        });
        return allObjects;
    }

    /**
     * Processes objects in each direction, stopping if a mountain is encountered.
     */
    private void processObjectsByDirection(Map<Direction, List<ObjectDetail>> allObjectsByDirection) {
        allObjectsByDirection.forEach((dir, objectsInDir) -> {
            for (ObjectDetail detail : objectsInDir) {
                objectDetails.add(detail);
                if (detail.getType().equals("MOUNTAIN")) {
                    break;
                }
            }
        });
    }

    /**
     * Gets all details of detected objects in JSON format.
     *
     * @return A JsonArray containing details of all detected objects
     */
    public JsonArray getSeenObjectDetails() {
        JsonArray objectsJsonArray = new JsonArray();
        for (ObjectDetail detail : objectDetails) {
            JsonObject foundObjectJson = new JsonObject();
            foundObjectJson.addProperty("direction", detail.getDirection().toString());
            foundObjectJson.addProperty("type", toProtocolType(detail.getType()));
            foundObjectJson.addProperty("distance", detail.getDistance());
            Optional.ofNullable(detail.getName()).ifPresent(name -> foundObjectJson.addProperty("name", name));
            Optional.ofNullable(detail.getObstacleType()).ifPresent(type -> foundObjectJson.addProperty("obstacle_type", type.toString()));
            objectsJsonArray.add(foundObjectJson);
        }
        return objectsJsonArray;
    }

    /**
     * Reference-server protocol uses OBSTACLE for all obstacle kinds.
     */
    private String toProtocolType(String type) {
        if ("ROBOT".equals(type) || "EDGE".equals(type)) {
            return type;
        }
        return "OBSTACLE";
    }

    /**
     * Finds all edges within the visible distance.
     *
     * @param currentPos Current robot's position
     * @param obstaclesByDirection Map of obstacles by direction to check for blocking mountains
     * @return Map of directions to lists of detected edge details
     */
    private Map<Direction, List<ObjectDetail>> detectEdges(Position currentPos,
                                                           Map<Direction, List<ObjectDetail>> obstaclesByDirection) {
        Map<Direction, List<ObjectDetail>> edgesByDirection = new EnumMap<>(Direction.class);
        int x = currentPos.getX();
        int y = currentPos.getY();

        Map<Direction, Boolean> edgeChecks = Map.of(
                NORTH, y - VISIBILITY < 0,
                WEST, x - VISIBILITY < 0,
                SOUTH, y + VISIBILITY >= HEIGHT,
                EAST, x + VISIBILITY >= WIDTH
        );

        edgeChecks.forEach((direction, isEdgeVisible) -> {
            if (isEdgeVisible && !isDirectionBlockedByMountain(direction, obstaclesByDirection)) {
                Position edgePos = getEdgePosition(currentPos, direction);
                int distance = currentPos.distanceFrom(edgePos, direction);
                if (isBehindMountain(direction, distance, obstaclesByDirection)) {
                    edgesByDirection.computeIfAbsent(direction, k -> new ArrayList<>())
                            .add(new ObjectDetail("EDGE", distance, direction));
                }
            }
        });
        return edgesByDirection;
    }

    /**
     * Gets the position of the edge in a given direction.
     *
     * @param currentPos Current robot's position
     * @param direction Direction to check
     * @return Position of the edge
     */
    private Position getEdgePosition(Position currentPos, Direction direction) {
        int x = currentPos.getX();
        int y = currentPos.getY();
        return switch (direction) {
            case NORTH -> new Position(x, 0);
            case WEST -> new Position(0, y);
            case SOUTH -> new Position(x, HEIGHT - 1);
            case EAST -> new Position(WIDTH - 1, y);
        };
    }

    /**
     * Detects obstacles within visibility range.
     *
     * @param currentPos Current robot's position
     * @param obstacles List of obstacles in the world
     * @return Map of directions to lists of detected obstacle details
     */
    private Map<Direction, List<ObjectDetail>> detectObstacles(Position currentPos, List<Obstacle> obstacles) {
        Map<Direction, List<ObjectDetail>> obstaclesByDirection = new EnumMap<>(Direction.class);
        Arrays.stream(Direction.values()).forEach(dir -> obstaclesByDirection.put(dir, new ArrayList<>()));

        obstacles.forEach(obstacle ->
                Arrays.stream(Direction.values()).forEach(dir -> {
                    Position visibilityPos = currentPos.newPos(dir, VISIBILITY);
                    if (isObstacleInDirection(currentPos, obstacle, dir, visibilityPos)) {
                        Position refPoint = getObstacleReferencePoint(obstacle, dir);
                        int distance = currentPos.distanceFrom(refPoint, dir);
                        if (distance <= VISIBILITY) {
                            obstaclesByDirection.get(dir).add(new ObjectDetail(obstacle.getType().toString(), distance, dir));
                        }
                    }
                })
        );
        obstaclesByDirection.values().forEach(obstaclesInDir -> obstaclesInDir.sort(Comparator.comparingInt(ObjectDetail::getDistance)));
        return obstaclesByDirection;
    }

    /**
     * Determines if an obstacle is potentially visible in a specific direction.
     */
    private boolean isObstacleInDirection(Position currentPos, Obstacle obstacle, Direction dir, Position targetPos) {
        Position topLeft = obstacle.getTopLeft();
        Position bottomRight = obstacle.getBottomRight();

        return switch (dir) {
            case NORTH -> currentPos.getY() > bottomRight.getY() && targetPos.getY() <= bottomRight.getY() &&
                    isBetween(currentPos.getX(), topLeft.getX(), bottomRight.getX());
            case SOUTH -> currentPos.getY() < topLeft.getY() && targetPos.getY() >= topLeft.getY() &&
                    isBetween(currentPos.getX(), topLeft.getX(), bottomRight.getX());
            case EAST -> currentPos.getX() < topLeft.getX() && targetPos.getX() >= topLeft.getX() &&
                    isBetween(currentPos.getY(), topLeft.getY(), bottomRight.getY());
            case WEST -> currentPos.getX() > bottomRight.getX() && targetPos.getX() <= bottomRight.getX() &&
                    isBetween(currentPos.getY(), topLeft.getY(), bottomRight.getY());
        };
    }

    /**
     * Helper method to check if a value is between two bounds (inclusive).
     */
    private boolean isBetween(int value, int min, int max) {
        return value >= min && value <= max;
    }

    /**
     * Gets the reference point of an obstacle based on viewing direction.
     */
    private Position getObstacleReferencePoint(Obstacle obstacle, Direction dir) {
        return switch (dir) {
            case NORTH, WEST -> obstacle.getBottomRight();
            case SOUTH, EAST -> obstacle.getTopLeft();
        };
    }

    /**
     * Detects robots within visibility range, accounting for mountains blocking vision.
     *
     * @param robots List of all robots in the world
     * @param currentPos Current robot's position
     * @param obstaclesByDirection Map of obstacles by direction to check for blocking mountains
     * @return Map of directions to lists of detected robot details
     */
    private Map<Direction, List<ObjectDetail>> detectRobots(List<Robot> robots, Position currentPos,
                                                            Map<Direction, List<ObjectDetail>> obstaclesByDirection) {
        Map<Direction, List<ObjectDetail>> robotsByDirection = new EnumMap<>(Direction.class);

        robots.stream()
                .filter(robot -> !currentPos.equals(robot.getPosition()))
                .forEach(robot -> {
                    Position robotPos = robot.getPosition();
                    Direction direction = getDirectionToRobot(currentPos, robotPos);

                    if (direction != null) {
                        int distance = currentPos.distanceFrom(robotPos, direction);
                        if (distance <= VISIBILITY && isBehindMountain(direction, distance, obstaclesByDirection)) {
                            robotsByDirection.computeIfAbsent(direction, k -> new ArrayList<>())
                                    .add(new ObjectDetail("ROBOT", distance, direction));
                        }
                    }
                });
        return robotsByDirection;
    }

    /**
     * Determines the cardinal direction from currentPos to robotPos, or null if not cardinal.
     */
    private Direction getDirectionToRobot(Position currentPos, Position robotPos) {
        if (currentPos.getX() == robotPos.getX()) {
            return robotPos.getY() > currentPos.getY() ? SOUTH : NORTH;
        } else if (currentPos.getY() == robotPos.getY()) {
            return robotPos.getX() > currentPos.getX() ? EAST : WEST;
        }
        return null;
    }

    /**
     * Checks if a direction is blocked by a mountain.
     */
    private boolean isDirectionBlockedByMountain(Direction direction, Map<Direction, List<ObjectDetail>> obstaclesByDirection) {
        return obstaclesByDirection.getOrDefault(direction, Collections.emptyList()).stream()
                .anyMatch(obj -> obj.getType().equals("MOUNTAIN"));
    }

    /**
     * Checks if an object at a specific distance is behind a mountain.
     */
    private boolean isBehindMountain(Direction direction, int objectDistance, Map<Direction, List<ObjectDetail>> obstaclesByDirection) {
        return obstaclesByDirection.getOrDefault(direction, Collections.emptyList()).stream()
                .filter(obj -> obj.getType().equals("MOUNTAIN"))
                .noneMatch(mountain -> mountain.getDistance() < objectDistance);
    }
}