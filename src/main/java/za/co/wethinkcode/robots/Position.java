package za.co.wethinkcode.robots;

import za.co.wethinkcode.robots.config.Config;
import za.co.wethinkcode.robots.obstacle.Obstacle;
import za.co.wethinkcode.robots.obstacle.ObstacleType;
import za.co.wethinkcode.robots.robot.Robot;

import java.util.List;

import static za.co.wethinkcode.robots.UpdateResponse.*;
import static za.co.wethinkcode.robots.UpdateResponse.FAILURE_OBSTRUCTED;

/**
 * The Position class represents a position in a 2D space.
 * It contains x and y coordinates and provides methods to access them.
 */
public class Position {
    private final int x;
    private final int y;

    /**
     * Constructor to initialize the position with x and y coordinates.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    /**
     * Checks if the current position is equal to another position.
     *
     * @param o the object to compare with
     * @return true if the positions are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Position position = (Position) o;

        if (x != position.x) return false;
        return y == position.y;
    }

    /**
     * Returns the hash code of the position.
     *
     * @return the hash code of the position
     */
    @Override
    public String toString(){
        return "(x: "+x+", y: "+y+")";
    }

    /**
     * Checks if the current position is within a specified rectangular area.
     *
     * @param topLeft the top-left corner of the rectangle
     * @param bottomRight the bottom-right corner of the rectangle
     * @return true if the position is within the rectangle, false otherwise
     */
    public boolean isIn(Position topLeft, Position bottomRight) {
        boolean withinTop = y >= topLeft.getY();
        boolean withinBottom = y <= bottomRight.getY();
        boolean withinLeft = x >= topLeft.getX();
        boolean withinRight = x <= bottomRight.getX();
        return withinTop && withinBottom && withinLeft && withinRight;
    }

    /**
     * Calculates the distance between this position and anotherPosition
     * @param anotherPosition A position to calculate distance from.
     * @param direction direction of anotherPosition from this position.
     * @return The distance between this position and anotherPosition.
     */
    public int distanceFrom(Position anotherPosition, Direction direction) {
        return switch (direction) {
            case NORTH, SOUTH -> Math.abs(y - anotherPosition.getY());
            case EAST, WEST -> Math.abs((x - anotherPosition.getX()));

        };
    }

    /**
     * Calculates a new position based on the current robot's direction and the number of steps.
     * @param nrSteps The number of steps to move.
     * @return The new position after moving.
     */
    public Position newPos(Direction direction , int nrSteps) {
        int newX = x;
        int newY = y;
        switch (direction) {
            case NORTH -> newY -= nrSteps;
            case SOUTH -> newY += nrSteps;
            case WEST -> newX -= nrSteps;
            case EAST -> newX += nrSteps;
        }

        return new Position(newX, newY);
    }
}