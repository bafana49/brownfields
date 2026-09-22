package za.co.wethinkcode.robots.maze;
import za.co.wethinkcode.robots.config.Config;

import za.co.wethinkcode.robots.obstacle.Obstacle;
import za.co.wethinkcode.robots.obstacle.ObstacleType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The Maze class represents a maze with obstacles.
 * It contains a list of obstacles and provides methods to access them.
 */
public class Maze {

    private List<Obstacle> obstacleList = new ArrayList<>();

    /**
     * Constructor for the Maze class.
     * It loads the configuration and initializes the maze with obstacles based on the configuration.
     */
    public Maze(String mode){
        if (Config.CLI_OBSTACLES_SET) {
            for (Config.CliObstacle cell : Config.CLI_OBSTACLES) {
                obstacleList.add(new Obstacle(cell.x, cell.y, cell.x, cell.y, cell.type));
            }
            return;
        }

        ensureConfigLoaded();
        mode = mode.isEmpty() ? Config.OBSTACLE_MODE: mode ;
        if (mode.equals("Random")){
            Random r = new Random();
            randomize(r.nextInt(2,6));

        } else if (isInt(mode)) {
            randomize(Integer.parseInt(mode));
        } else if (mode.contains(",")) {
            try {
                String [] obs = mode.split(" ");
                String [] parts;
                for(String o : obs){

                    String oType = o.split("-")[0];
                    parts = o.split("-")[1].split(":");
                    int topLeftX = Integer.parseInt(parts[0].split(",")[0]);
                    int topLeftY = Integer.parseInt(parts[0].split(",")[1]);
                    int bottomRightX = Integer.parseInt(parts[1].split(",")[0]);
                    int bottomRightY = Integer.parseInt(parts[1].split(",")[1]);
                    ObstacleType type = switch (oType){
                        case "BP"-> ObstacleType.BOTTOMLESS_PIT;
                        case "M"-> ObstacleType.MOUNTAIN;
                        case "L"-> ObstacleType.LAKE;
                        default -> throw new IllegalStateException("Unexpected value: " + oType);
                    };

                    obstacleList.add(new Obstacle(topLeftX,topLeftY,bottomRightX,bottomRightY,type));

                }
            }catch (Exception e){
                System.out.println("Cannot get Obstacles from Config " + e.getMessage());
                obstacleList = new ArrayList<>();
            }
        }
    }

    /**
     * Gets the list of obstacles in the maze.
     * @return The list of obstacles.
     */
    public List<Obstacle> getObstacles() {
        return obstacleList;
    }

    private void ensureConfigLoaded() {
        if (Config.WIDTH == 0 || Config.HEIGHT == 0) {
            Config.loadConfig("config.properties");
        }
    }

    /**
     * @param n The number of random obstacles to be generated.
     * adds "n" random types of obstacles in the maze
     * Refactored to fix coordinate bug where Y coordinate was using Config.WIDTH instead of Config.HEIGHT.
     * This would cause obstacles to be generated outside the valid world bounds when width != height.
     */
    private void randomize(int n){
        ensureConfigLoaded();
        Random r = new Random();
        Obstacle newObstacle;
        for (int i = 0;i<n;i++){

            while(true){
                int topLeftX = r.nextInt(1,Config.WIDTH-1);
                int topLeftY = r.nextInt(1,Config.HEIGHT-1);
                ObstacleType type = ObstacleType.values()[r.nextInt(0,3)];
                int size = r.nextInt(2,10);

                newObstacle = new Obstacle(topLeftX,topLeftY,size,type);
                boolean canAdd = true;
                for(Obstacle o : obstacleList){
                    if(o.isOverlapping(newObstacle)){
                        canAdd = false;
                    }
                }
                if(canAdd){
                    break;
                }
            }
            obstacleList.add(newObstacle);
        }
    }

    /**
     * Checks if a string can be parsed as an integer.
     * @param n The string to check.
     * @return true if the string can be parsed as an integer, false otherwise.
     */
    private boolean isInt(String n){
        try{
            int num = Integer.parseInt(n);
            return true;
        }catch (Exception e){
            return false;
        }
    }

}
