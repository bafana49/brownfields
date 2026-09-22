package za.co.wethinkcode.robots.persistence.orm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.Direction;
import za.co.wethinkcode.robots.OperationalStatus;
import za.co.wethinkcode.robots.obstacle.ObstacleType;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Data Object classes.
 * These tests verify the data objects that represent database entities.
 *
 * Note: WorldDataDO, ObstacleDataDO, PositionDataDO, and RobotDataDO are now implemented.
 * General data object tests (JSON serialization, null handling, property validation) are not yet implemented.
 */
class DataObjectTests {

    /**
     * Tests WorldDataDO creation and properties.
     * Verifies that a world data object can be created with the correct properties.
     */
    @Test
    @DisplayName("WorldDataDO should be created with correct properties")
    void worldDataDO_creation_withCorrectProperties() {
        // Test no-arg constructor with default values
        WorldDataDO world1 = new WorldDataDO();
        assertEquals(0, world1.getId());
        assertNull(world1.getName());
        assertEquals(0, world1.getWidth());
        assertEquals(0, world1.getHeight());

        // Test constructor with all fields
        WorldDataDO world2 = new WorldDataDO(1, "mars", 100, 100);
        assertEquals(1, world2.getId());
        assertEquals("mars", world2.getName());
        assertEquals(100, world2.getWidth());
        assertEquals(100, world2.getHeight());

        // Test constructor without id (for new records)
        WorldDataDO world3 = new WorldDataDO("earth", 200, 150);
        assertEquals(0, world3.getId());
        assertEquals("earth", world3.getName());
        assertEquals(200, world3.getWidth());
        assertEquals(150, world3.getHeight());

        // Test setters update properties correctly
        world1.setId(5);
        world1.setName("jupiter");
        world1.setWidth(300);
        world1.setHeight(250);
        assertEquals(5, world1.getId());
        assertEquals("jupiter", world1.getName());
        assertEquals(300, world1.getWidth());
        assertEquals(250, world1.getHeight());
    }

    /**
     * Tests WorldDataDO equals and hashCode methods.
     * Verifies that data objects with the same values are considered equal.
     */
    @Test
    @DisplayName("WorldDataDO equals and hashCode should work correctly")
    void worldDataDO_equalsAndHashCode_workCorrectly() {
        WorldDataDO world1 = new WorldDataDO(1, "mars", 100, 100);
        WorldDataDO world2 = new WorldDataDO(1, "mars", 100, 100);
        WorldDataDO world3 = new WorldDataDO(2, "earth", 200, 150);

        // Two objects with same values are equal
        assertEquals(world1, world2);

        // Two objects with different values are not equal
        assertNotEquals(world1, world3);

        // Equal objects have same hashCode
        assertEquals(world1.hashCode(), world2.hashCode());

        // Object is not equal to null
        assertNotEquals(world1, null);

        // Object is not equal to different type
        assertNotEquals(world1, "not a WorldDataDO");
    }

    /**
     * Tests ObstacleDataDO creation and properties.
     * Verifies that an obstacle data object can be created with the correct properties.
     */
    @Test
    @DisplayName("ObstacleDataDO should be created with correct properties")
    void obstacleDataDO_creation_withCorrectProperties() {
        // Test no-arg constructor with default values
        ObstacleDataDO obstacle1 = new ObstacleDataDO();
        assertEquals(0, obstacle1.getId());
        assertEquals(0, obstacle1.getWorldId());
        assertNull(obstacle1.getType());
        assertEquals(0, obstacle1.getTopLeftX());
        assertEquals(0, obstacle1.getTopLeftY());
        assertEquals(0, obstacle1.getBottomRightX());
        assertEquals(0, obstacle1.getBottomRightY());

        // Test constructor with all fields
        ObstacleDataDO obstacle2 = new ObstacleDataDO(1, 5, ObstacleType.MOUNTAIN, 10, 10, 15, 15);
        assertEquals(1, obstacle2.getId());
        assertEquals(5, obstacle2.getWorldId());
        assertEquals(ObstacleType.MOUNTAIN, obstacle2.getType());
        assertEquals(10, obstacle2.getTopLeftX());
        assertEquals(10, obstacle2.getTopLeftY());
        assertEquals(15, obstacle2.getBottomRightX());
        assertEquals(15, obstacle2.getBottomRightY());

        // Test constructor without id (for new records)
        ObstacleDataDO obstacle3 = new ObstacleDataDO(5, ObstacleType.LAKE, 20, 20, 25, 25);
        assertEquals(0, obstacle3.getId());
        assertEquals(5, obstacle3.getWorldId());
        assertEquals(ObstacleType.LAKE, obstacle3.getType());
        assertEquals(20, obstacle3.getTopLeftX());
        assertEquals(20, obstacle3.getTopLeftY());
        assertEquals(25, obstacle3.getBottomRightX());
        assertEquals(25, obstacle3.getBottomRightY());

        // Test setters update properties correctly
        obstacle1.setId(2);
        obstacle1.setWorldId(10);
        obstacle1.setType(ObstacleType.BOTTOMLESS_PIT);
        obstacle1.setTopLeftX(30);
        obstacle1.setTopLeftY(30);
        obstacle1.setBottomRightX(35);
        obstacle1.setBottomRightY(35);
        assertEquals(2, obstacle1.getId());
        assertEquals(10, obstacle1.getWorldId());
        assertEquals(ObstacleType.BOTTOMLESS_PIT, obstacle1.getType());
        assertEquals(30, obstacle1.getTopLeftX());
        assertEquals(30, obstacle1.getTopLeftY());
        assertEquals(35, obstacle1.getBottomRightX());
        assertEquals(35, obstacle1.getBottomRightY());
    }

    /**
     * Tests ObstacleDataDO equals and hashCode methods.
     * Verifies that data objects with the same values are considered equal.
     */
    @Test
    @DisplayName("ObstacleDataDO equals and hashCode should work correctly")
    void obstacleDataDO_equalsAndHashCode_workCorrectly() {
        ObstacleDataDO obstacle1 = new ObstacleDataDO(1, 5, ObstacleType.MOUNTAIN, 10, 10, 15, 15);
        ObstacleDataDO obstacle2 = new ObstacleDataDO(1, 5, ObstacleType.MOUNTAIN, 10, 10, 15, 15);
        ObstacleDataDO obstacle3 = new ObstacleDataDO(2, 6, ObstacleType.LAKE, 20, 20, 25, 25);

        // Two objects with same values are equal
        assertEquals(obstacle1, obstacle2);

        // Two objects with different values are not equal
        assertNotEquals(obstacle1, obstacle3);

        // Equal objects have same hashCode
        assertEquals(obstacle1.hashCode(), obstacle2.hashCode());

        // Object is not equal to null
        assertNotEquals(obstacle1, null);

        // Object is not equal to different type
        assertNotEquals(obstacle1, "not an ObstacleDataDO");
    }

    /**
     * Tests RobotDataDO creation and properties.
     * Verifies that a robot data object can be created with the correct properties.
     */
    @Test
    @DisplayName("RobotDataDO should be created with correct properties")
    void robotDataDO_creation_withCorrectProperties() {
        // Test no-arg constructor with default values
        RobotDataDO robot1 = new RobotDataDO();
        assertEquals(0, robot1.getId());
        assertNull(robot1.getName());
        assertNull(robot1.getType());
        assertEquals(0, robot1.getPositionX());
        assertEquals(0, robot1.getPositionY());
        assertNull(robot1.getDirection());
        assertNull(robot1.getStatus());
        assertEquals(0, robot1.getShield());
        assertEquals(0, robot1.getShots());
        assertEquals(0, robot1.getMaxShots());
        assertEquals(0, robot1.getMaxShields());
        assertEquals(0, robot1.getBulletDistance());

        // Test constructor with all fields
        RobotDataDO robot2 = new RobotDataDO(1, "HAL", "sniper", 10, 10, Direction.NORTH, 
            OperationalStatus.NORMAL, 1, 1, 1, 1, 5);
        assertEquals(1, robot2.getId());
        assertEquals("HAL", robot2.getName());
        assertEquals("sniper", robot2.getType());
        assertEquals(10, robot2.getPositionX());
        assertEquals(10, robot2.getPositionY());
        assertEquals(Direction.NORTH, robot2.getDirection());
        assertEquals(OperationalStatus.NORMAL, robot2.getStatus());
        assertEquals(1, robot2.getShield());
        assertEquals(1, robot2.getShots());
        assertEquals(1, robot2.getMaxShots());
        assertEquals(1, robot2.getMaxShields());
        assertEquals(5, robot2.getBulletDistance());

        // Test constructor without id (for new records)
        RobotDataDO robot3 = new RobotDataDO("TERMINATOR", "soldier", 20, 20, Direction.EAST, 
            OperationalStatus.NORMAL, 3, 3, 3, 3, 3);
        assertEquals(0, robot3.getId());
        assertEquals("TERMINATOR", robot3.getName());
        assertEquals("soldier", robot3.getType());
        assertEquals(20, robot3.getPositionX());
        assertEquals(20, robot3.getPositionY());
        assertEquals(Direction.EAST, robot3.getDirection());
        assertEquals(OperationalStatus.NORMAL, robot3.getStatus());
        assertEquals(3, robot3.getShield());
        assertEquals(3, robot3.getShots());
        assertEquals(3, robot3.getMaxShots());
        assertEquals(3, robot3.getMaxShields());
        assertEquals(3, robot3.getBulletDistance());

        // Test setters update properties correctly
        robot1.setId(2);
        robot1.setName("R2D2");
        robot1.setType("hitbot");
        robot1.setPositionX(30);
        robot1.setPositionY(30);
        robot1.setDirection(Direction.SOUTH);
        robot1.setStatus(OperationalStatus.DEAD);
        robot1.setShield(0);
        robot1.setShots(0);
        robot1.setMaxShots(0);
        robot1.setMaxShields(0);
        robot1.setBulletDistance(0);
        assertEquals(2, robot1.getId());
        assertEquals("R2D2", robot1.getName());
        assertEquals("hitbot", robot1.getType());
        assertEquals(30, robot1.getPositionX());
        assertEquals(30, robot1.getPositionY());
        assertEquals(Direction.SOUTH, robot1.getDirection());
        assertEquals(OperationalStatus.DEAD, robot1.getStatus());
        assertEquals(0, robot1.getShield());
        assertEquals(0, robot1.getShots());
        assertEquals(0, robot1.getMaxShots());
        assertEquals(0, robot1.getMaxShields());
        assertEquals(0, robot1.getBulletDistance());
    }

    /**
     * Tests RobotDataDO equals and hashCode methods.
     * Verifies that data objects with the same values are considered equal.
     */
    @Test
    @DisplayName("RobotDataDO equals and hashCode should work correctly")
    void robotDataDO_equalsAndHashCode_workCorrectly() {
        RobotDataDO robot1 = new RobotDataDO(1, "HAL", "sniper", 10, 10, Direction.NORTH, 
            OperationalStatus.NORMAL, 1, 1, 1, 1, 5);
        RobotDataDO robot2 = new RobotDataDO(1, "HAL", "sniper", 10, 10, Direction.NORTH, 
            OperationalStatus.NORMAL, 1, 1, 1, 1, 5);
        RobotDataDO robot3 = new RobotDataDO(2, "TERMINATOR", "soldier", 20, 20, Direction.EAST, 
            OperationalStatus.NORMAL, 3, 3, 3, 3, 3);

        // Two objects with same values are equal
        assertEquals(robot1, robot2);

        // Two objects with different values are not equal
        assertNotEquals(robot1, robot3);

        // Equal objects have same hashCode
        assertEquals(robot1.hashCode(), robot2.hashCode());

        // Object is not equal to null
        assertNotEquals(robot1, null);

        // Object is not equal to different type
        assertNotEquals(robot1, "not a RobotDataDO");
    }

    /**
     * Tests PositionDataDO creation and properties.
     * Verifies that a position data object can be created with the correct properties.
     */
    @Test
    @DisplayName("PositionDataDO should be created with correct properties")
    void positionDataDO_creation_withCorrectProperties() {
        // Test no-arg constructor with default values
        PositionDataDO position1 = new PositionDataDO();
        assertEquals(0, position1.getX());
        assertEquals(0, position1.getY());

        // Test constructor with coordinate values
        PositionDataDO position2 = new PositionDataDO(10, 20);
        assertEquals(10, position2.getX());
        assertEquals(20, position2.getY());

        // Test setters update properties correctly
        position1.setX(30);
        position1.setY(40);
        assertEquals(30, position1.getX());
        assertEquals(40, position1.getY());
    }

    /**
     * Tests PositionDataDO equals and hashCode methods.
     * Verifies that data objects with the same values are considered equal.
     */
    @Test
    @DisplayName("PositionDataDO equals and hashCode should work correctly")
    void positionDataDO_equalsAndHashCode_workCorrectly() {
        PositionDataDO position1 = new PositionDataDO(10, 20);
        PositionDataDO position2 = new PositionDataDO(10, 20);
        PositionDataDO position3 = new PositionDataDO(30, 40);

        // Two objects with same values are equal
        assertEquals(position1, position2);

        // Two objects with different values are not equal
        assertNotEquals(position1, position3);

        // Equal objects have same hashCode
        assertEquals(position1.hashCode(), position2.hashCode());

        // Object is not equal to null
        assertNotEquals(position1, null);

        // Object is not equal to different type
        assertNotEquals(position1, "not a PositionDataDO");
    }

    /**
     * Tests that data objects can be converted to/from JSON using Gson (already on classpath).
     * Verifies serialization and deserialization of data objects.
     */
    @Test
    @DisplayName("Data objects should support JSON serialization")
    void dataObjects_jsonSerialization_worksCorrectly() {
        com.google.gson.Gson gson = new com.google.gson.Gson();

        // WorldDataDO round-trip
        WorldDataDO world = new WorldDataDO(1, "mars", 100, 200);
        String worldJson = gson.toJson(world);
        assertNotNull(worldJson);
        assertTrue(worldJson.contains("mars"));
        WorldDataDO worldBack = gson.fromJson(worldJson, WorldDataDO.class);
        assertEquals(world.getId(),     worldBack.getId());
        assertEquals(world.getName(),   worldBack.getName());
        assertEquals(world.getWidth(),  worldBack.getWidth());
        assertEquals(world.getHeight(), worldBack.getHeight());

        // ObstacleDataDO round-trip
        ObstacleDataDO obstacle = new ObstacleDataDO(2, 1, ObstacleType.LAKE, 3, 3, 4, 4);
        String obstacleJson = gson.toJson(obstacle);
        assertNotNull(obstacleJson);
        ObstacleDataDO obstacleBack = gson.fromJson(obstacleJson, ObstacleDataDO.class);
        assertEquals(obstacle.getId(),           obstacleBack.getId());
        assertEquals(obstacle.getWorldId(),      obstacleBack.getWorldId());
        assertEquals(obstacle.getType(),         obstacleBack.getType());
        assertEquals(obstacle.getTopLeftX(),     obstacleBack.getTopLeftX());
        assertEquals(obstacle.getBottomRightY(), obstacleBack.getBottomRightY());

        // PositionDataDO round-trip
        PositionDataDO position = new PositionDataDO(10, 20);
        String posJson = gson.toJson(position);
        assertNotNull(posJson);
        PositionDataDO posBack = gson.fromJson(posJson, PositionDataDO.class);
        assertEquals(position.getX(), posBack.getX());
        assertEquals(position.getY(), posBack.getY());

        // RobotDataDO round-trip
        RobotDataDO robot = new RobotDataDO(1, "HAL", "sniper", 5, 5,
                Direction.NORTH, OperationalStatus.NORMAL, 2, 3, 5, 3, 10);
        String robotJson = gson.toJson(robot);
        assertNotNull(robotJson);
        assertTrue(robotJson.contains("HAL"));
        RobotDataDO robotBack = gson.fromJson(robotJson, RobotDataDO.class);
        assertEquals(robot.getName(),           robotBack.getName());
        assertEquals(robot.getType(),           robotBack.getType());
        assertEquals(robot.getBulletDistance(), robotBack.getBulletDistance());
    }

    /**
     * Tests that data objects handle null values gracefully.
     */
    @Test
    @DisplayName("Data objects should handle null values gracefully")
    void dataObjects_nullValues_handledGracefully() {
        // WorldDataDO — nullable name
        WorldDataDO world = new WorldDataDO();
        assertNull(world.getName());
        world.setName(null);  // must not throw
        assertNull(world.getName());
        world.setName("earth");
        assertEquals("earth", world.getName());
        world.setName(null);
        assertNull(world.getName());

        // ObstacleDataDO — nullable type
        ObstacleDataDO obstacle = new ObstacleDataDO();
        assertNull(obstacle.getType());
        obstacle.setType(null); // must not throw
        assertNull(obstacle.getType());
        obstacle.setType(ObstacleType.MOUNTAIN);
        assertEquals(ObstacleType.MOUNTAIN, obstacle.getType());
        obstacle.setType(null);
        assertNull(obstacle.getType());

        // RobotDataDO — nullable name, type, direction, status
        RobotDataDO robot = new RobotDataDO();
        assertNull(robot.getName());
        assertNull(robot.getType());
        assertNull(robot.getDirection());
        assertNull(robot.getStatus());
        robot.setName(null);
        robot.setType(null);
        robot.setDirection(null);
        robot.setStatus(null);
        assertNull(robot.getName());
        assertNull(robot.getType());
        assertNull(robot.getDirection());
        assertNull(robot.getStatus());

        // Setting non-null values after null should work
        robot.setName("HAL");
        robot.setDirection(Direction.EAST);
        robot.setStatus(OperationalStatus.NORMAL);
        assertEquals("HAL", robot.getName());
        assertEquals(Direction.EAST, robot.getDirection());
        assertEquals(OperationalStatus.NORMAL, robot.getStatus());
    }

    /**
     * Tests that data objects validate / accept expected property values correctly.
     */
    @Test
    @DisplayName("Data objects should validate their properties")
    void dataObjects_propertyValidation_worksCorrectly() {
        // Coordinates — zero, positive and negative int values are all accepted
        ObstacleDataDO obstacle = new ObstacleDataDO();
        obstacle.setTopLeftX(0);
        obstacle.setTopLeftY(-5);      // negative allowed (world can have negative coords)
        obstacle.setBottomRightX(100);
        obstacle.setBottomRightY(200);
        assertEquals(0,    obstacle.getTopLeftX());
        assertEquals(-5,   obstacle.getTopLeftY());
        assertEquals(100,  obstacle.getBottomRightX());
        assertEquals(200,  obstacle.getBottomRightY());

        // All ObstacleType enum values are accepted
        for (ObstacleType type : ObstacleType.values()) {
            obstacle.setType(type);
            assertEquals(type, obstacle.getType());
        }

        // All Direction enum values are accepted
        RobotDataDO robot = new RobotDataDO();
        for (Direction dir : Direction.values()) {
            robot.setDirection(dir);
            assertEquals(dir, robot.getDirection());
        }

        // All OperationalStatus enum values are accepted
        for (OperationalStatus status : OperationalStatus.values()) {
            robot.setStatus(status);
            assertEquals(status, robot.getStatus());
        }

        // World dimensions — positive values accepted
        WorldDataDO world = new WorldDataDO();
        world.setWidth(Integer.MAX_VALUE);
        world.setHeight(1);
        assertEquals(Integer.MAX_VALUE, world.getWidth());
        assertEquals(1, world.getHeight());

        // Robot numeric stats — any int value accepted (game logic enforces bounds)
        robot.setShield(0);
        robot.setShots(100);
        robot.setMaxShots(Integer.MAX_VALUE);
        robot.setBulletDistance(-1);
        assertEquals(0,                 robot.getShield());
        assertEquals(100,               robot.getShots());
        assertEquals(Integer.MAX_VALUE, robot.getMaxShots());
        assertEquals(-1,                robot.getBulletDistance());
    }


    /**
     * Tests that data objects have public no-arg constructors.
     * Required by EoDSQL for data object instantiation.
     */
    @Test
    @DisplayName("Data objects should have public no-arg constructors")
    void dataObjects_noArgConstructor_exists() {
        // Test WorldDataDO has public no-arg constructor
        WorldDataDO world = new WorldDataDO();
        assertNotNull(world);
        assertEquals(0, world.getId());
        assertNull(world.getName());
        assertEquals(0, world.getWidth());
        assertEquals(0, world.getHeight());

        // Test ObstacleDataDO has public no-arg constructor
        ObstacleDataDO obstacle = new ObstacleDataDO();
        assertNotNull(obstacle);
        assertEquals(0, obstacle.getId());
        assertEquals(0, obstacle.getWorldId());
        assertNull(obstacle.getType());
        assertEquals(0, obstacle.getTopLeftX());
        assertEquals(0, obstacle.getTopLeftY());
        assertEquals(0, obstacle.getBottomRightX());
        assertEquals(0, obstacle.getBottomRightY());

        // Test PositionDataDO has public no-arg constructor
        PositionDataDO position = new PositionDataDO();
        assertNotNull(position);
        assertEquals(0, position.getX());
        assertEquals(0, position.getY());

        // Test RobotDataDO has public no-arg constructor
        RobotDataDO robot = new RobotDataDO();
        assertNotNull(robot);
        assertEquals(0, robot.getId());
        assertNull(robot.getName());
        assertNull(robot.getType());
        assertEquals(0, robot.getPositionX());
        assertEquals(0, robot.getPositionY());
        assertNull(robot.getDirection());
        assertNull(robot.getStatus());
        assertEquals(0, robot.getShield());
        assertEquals(0, robot.getShots());
        assertEquals(0, robot.getMaxShots());
        assertEquals(0, robot.getMaxShields());
        assertEquals(0, robot.getBulletDistance());
    }

    /**
     * Tests that data object properties are accessible.
     * EoDSQL requires either public fields or public getters/setters.
     */
    @Test
    @DisplayName("Data object properties should be accessible")
    void dataObjects_properties_accessible() {
        // Test WorldDataDO properties are accessible via getters/setters
        WorldDataDO world = new WorldDataDO();
        world.setId(1);
        world.setName("mars");
        world.setWidth(100);
        world.setHeight(100);

        assertEquals(1, world.getId());
        assertEquals("mars", world.getName());
        assertEquals(100, world.getWidth());
        assertEquals(100, world.getHeight());

        // Test ObstacleDataDO properties are accessible via getters/setters
        ObstacleDataDO obstacle = new ObstacleDataDO();
        obstacle.setId(1);
        obstacle.setWorldId(5);
        obstacle.setType(ObstacleType.MOUNTAIN);
        obstacle.setTopLeftX(10);
        obstacle.setTopLeftY(10);
        obstacle.setBottomRightX(15);
        obstacle.setBottomRightY(15);

        assertEquals(1, obstacle.getId());
        assertEquals(5, obstacle.getWorldId());
        assertEquals(ObstacleType.MOUNTAIN, obstacle.getType());
        assertEquals(10, obstacle.getTopLeftX());
        assertEquals(10, obstacle.getTopLeftY());
        assertEquals(15, obstacle.getBottomRightX());
        assertEquals(15, obstacle.getBottomRightY());

        // Test PositionDataDO properties are accessible via getters/setters
        PositionDataDO position = new PositionDataDO();
        position.setX(10);
        position.setY(20);

        assertEquals(10, position.getX());
        assertEquals(20, position.getY());

        // Test RobotDataDO properties are accessible via getters/setters
        RobotDataDO robot = new RobotDataDO();
        robot.setId(1);
        robot.setName("HAL");
        robot.setType("sniper");
        robot.setPositionX(10);
        robot.setPositionY(10);
        robot.setDirection(Direction.NORTH);
        robot.setStatus(OperationalStatus.NORMAL);
        robot.setShield(1);
        robot.setShots(1);
        robot.setMaxShots(1);
        robot.setMaxShields(1);
        robot.setBulletDistance(5);

        assertEquals(1, robot.getId());
        assertEquals("HAL", robot.getName());
        assertEquals("sniper", robot.getType());
        assertEquals(10, robot.getPositionX());
        assertEquals(10, robot.getPositionY());
        assertEquals(Direction.NORTH, robot.getDirection());
        assertEquals(OperationalStatus.NORMAL, robot.getStatus());
        assertEquals(1, robot.getShield());
        assertEquals(1, robot.getShots());
        assertEquals(1, robot.getMaxShots());
        assertEquals(1, robot.getMaxShields());
        assertEquals(5, robot.getBulletDistance());
    }

    /**
     * Tests that data objects have proper structure for ORM frameworks.
     * Verifies the class has required properties and follows JavaBean conventions.
     */
    @Test
    @DisplayName("Data objects should have proper ORM structure")
    void dataObjects_eodsqlAnnotations_workCorrectly() {
        // Test WorldDataDO has proper structure for ORM frameworks
        WorldDataDO world = new WorldDataDO();
        world.setId(1);
        world.setName("mars");
        world.setWidth(100);
        world.setHeight(100);

        // Verify all properties can be set and retrieved
        assertEquals(1, world.getId());
        assertEquals("mars", world.getName());
        assertEquals(100, world.getWidth());
        assertEquals(100, world.getHeight());

        // Verify toString works for debugging
        String worldToString = world.toString();
        assertNotNull(worldToString);
        assertTrue(worldToString.contains("WorldDataDO"));
        assertTrue(worldToString.contains("mars"));

        // Test ObstacleDataDO has proper structure for ORM frameworks
        ObstacleDataDO obstacle = new ObstacleDataDO();
        obstacle.setId(1);
        obstacle.setWorldId(5);
        obstacle.setType(ObstacleType.MOUNTAIN);
        obstacle.setTopLeftX(10);
        obstacle.setTopLeftY(10);
        obstacle.setBottomRightX(15);
        obstacle.setBottomRightY(15);

        // Verify all properties can be set and retrieved
        assertEquals(1, obstacle.getId());
        assertEquals(5, obstacle.getWorldId());
        assertEquals(ObstacleType.MOUNTAIN, obstacle.getType());
        assertEquals(10, obstacle.getTopLeftX());
        assertEquals(10, obstacle.getTopLeftY());
        assertEquals(15, obstacle.getBottomRightX());
        assertEquals(15, obstacle.getBottomRightY());

        // Verify toString works for debugging
        String obstacleToString = obstacle.toString();
        assertNotNull(obstacleToString);
        assertTrue(obstacleToString.contains("ObstacleDataDO"));
        assertTrue(obstacleToString.contains("MOUNTAIN"));

        // Test PositionDataDO has proper structure for ORM frameworks
        PositionDataDO position = new PositionDataDO();
        position.setX(10);
        position.setY(20);

        // Verify all properties can be set and retrieved
        assertEquals(10, position.getX());
        assertEquals(20, position.getY());

        // Verify toString works for debugging
        String positionToString = position.toString();
        assertNotNull(positionToString);
        assertTrue(positionToString.contains("PositionDataDO"));

        // Test RobotDataDO has proper structure for ORM frameworks
        RobotDataDO robot = new RobotDataDO();
        robot.setId(1);
        robot.setName("HAL");
        robot.setType("sniper");
        robot.setPositionX(10);
        robot.setPositionY(10);
        robot.setDirection(Direction.NORTH);
        robot.setStatus(OperationalStatus.NORMAL);
        robot.setShield(1);
        robot.setShots(1);
        robot.setMaxShots(1);
        robot.setMaxShields(1);
        robot.setBulletDistance(5);

        // Verify all properties can be set and retrieved
        assertEquals(1, robot.getId());
        assertEquals("HAL", robot.getName());
        assertEquals("sniper", robot.getType());
        assertEquals(10, robot.getPositionX());
        assertEquals(10, robot.getPositionY());
        assertEquals(Direction.NORTH, robot.getDirection());
        assertEquals(OperationalStatus.NORMAL, robot.getStatus());
        assertEquals(1, robot.getShield());
        assertEquals(1, robot.getShots());
        assertEquals(1, robot.getMaxShots());
        assertEquals(1, robot.getMaxShields());
        assertEquals(5, robot.getBulletDistance());

        // Verify toString works for debugging
        String robotToString = robot.toString();
        assertNotNull(robotToString);
        assertTrue(robotToString.contains("RobotDataDO"));
        assertTrue(robotToString.contains("HAL"));
    }
}