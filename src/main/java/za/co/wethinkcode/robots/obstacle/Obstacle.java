package za.co.wethinkcode.robots.obstacle;

import za.co.wethinkcode.robots.Position;

/**
 * The Obstacle class represents an obstacle in the world.
 * It contains information about the obstacle's position and size.
 */
public class Obstacle {

    private final String name;
    private final int topLeftX;
    private final int topLeftY;
    private final int bottomRightX;
    private final int bottomRightY;
    private final int obstacleSize;
    private final ObstacleType type;


    /**
     * Constructor to create an obstacle with specified coordinates.
     *
     * @param topLeftX the x-coordinate of the top-left corner
     * @param topLeftY the y-coordinate of the top-left corner
     * @param bottomRightX the x-coordinate of the bottom-right corner
     * @param bottomRightY the y-coordinate of the bottom-right corner
     * @param type - type of obstacle
     */
    public Obstacle(int topLeftX, int topLeftY, int bottomRightX, int bottomRightY,ObstacleType type) {
        this.topLeftX = topLeftX;
        this.topLeftY = topLeftY;
        this.bottomRightX = bottomRightX;
        this.bottomRightY = bottomRightY;
        this.obstacleSize = bottomRightX - topLeftX;
        this.type = type;
        this.name = "OBSTACLE";

    }

    /**
     *  Constructor overload 1
     * @param topLeftX the x-coordinate of the top-left corner
     * @param topLeftY the y-coordinate of the top-left corner
     * @param size - size of obstacle
     * @param type - type of obstacle [Mountain,lake,pit]
     */
    public Obstacle(int topLeftX, int topLeftY,int size,ObstacleType type){
        this.topLeftX = topLeftX;
        this.topLeftY = topLeftY;
        this.obstacleSize = size;
        this.bottomRightX = topLeftX + size;
        this.bottomRightY = topLeftY + size;
        this.type = type;
        this.name = type+" "+size;
    }

    /**
     * Gets the x-coordinate of the top-left corner.
     * @return the x-coordinate of the top-left corner
     */
    public Position getBottomRight(){
        return new Position(bottomRightX,bottomRightY);
    }

    /**
     * Gets position of obstacle.
     * @return Position of the obstacle.
     */
    public Position getPosition(){
        return new Position(
                topLeftX + obstacleSize,
                topLeftY + obstacleSize
        );
    }

    /**
     * Gets the x-coordinate of the top-left corner.
     * @return the x-coordinate of the top-left corner
     */
    public Position getTopLeft(){
        return new Position(topLeftX,topLeftY);
    }
    public ObstacleType getType(){return type;}



    /**
     * Checks if this obstacle blocks access to the specified position.
     *
     * @param position the position to check
     * @return return `true` if the x,y coordinate falls within the obstacle's area
     */
    public boolean blocksPosition(Position position) {
        return position.getX() >= this.topLeftX && position.getX() <= this.bottomRightX && position.getY() >= this.topLeftY && position.getY() <= this.bottomRightY;
    }


    /**
     * Checks if this obstacle blocks the path that goes from coordinate (x1, y1) to (x2, y2).
     * Since our robot can only move in horizontal or vertical lines (no diagonals yet), we can assume that either x1==x2 or y1==y2.
     *
     * @param a first position
     * @param b second position
     * @return `true` if this obstacle is in the way
     */
    public boolean blocksPath(Position a, Position b) {
        if (a.getX() == b.getX()) {
            return a.getX() >= this.topLeftX && a.getX() <= this.bottomRightX && ((a.getY() >= this.topLeftY && b.getY() <= this.bottomRightY) || (b.getY() >= this.topLeftY && a.getY() <= this.bottomRightY));
        } else if (a.getY() == b.getY()) {
            return a.getY() >= this.topLeftY && a.getY() <= this.bottomRightY && ((a.getX() >= this.topLeftX && b.getX() <= this.bottomRightX) || (b.getX() >= this.topLeftX && a.getX() <= this.bottomRightX));
        }
        return false;
    }

    /**
     * this Helper function checks if some obstacle overlaps this one
     * @param anotherObstacle - another Obstacle to be checked if it overlaps the current
     * @return - boolean true/false [if anotherObstacle overlaps the other]
     */
    public boolean isOverlapping(Obstacle anotherObstacle){

        for(int x=anotherObstacle.getTopLeft().getX(); x<anotherObstacle.getBottomRight().getX();x++){
            for(int y=anotherObstacle.getTopLeft().getY(); y<anotherObstacle.getBottomRight().getY();y++){
                if(blocksPosition(new Position(x,y))){
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Returns a string representation of the obstacle.
     * @return A string representation of the obstacle's position and size.
     */
    @Override
    public String toString(){
        Position topLeft = new Position(topLeftX,topLeftY);
        Position bottomRight = new Position(bottomRightX,bottomRightY);
        return type + " : { " + "TopLeft-> "+ topLeft +" BottomRight-> "+ bottomRight + " } ";
    }



    /**
     * Gets the size of the obstacle.
     * @return the size of the obstacle
     */
    public String getName() {
        return name;
    }
}
