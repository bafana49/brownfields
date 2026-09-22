package za.co.wethinkcode.robots.world;

import za.co.wethinkcode.robots.Position;
import za.co.wethinkcode.robots.obstacle.Obstacle;
import za.co.wethinkcode.robots.robot.Robot;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * WorldGUI provides a visual representation of the robot world,
 * displaying robots, obstacles, and other game elements using a Swing-based interface.
 */
public class WorldGUI {

    private static final int CELL_SIZE = 20;
    private static final int FRAME_WIDTH = 400;
    private static final int FRAME_HEIGHT = 560;
    /** 2D array representing the grid cells of the world */
    private final JPanel[][] gridCells;

    /** Labels used to show images/icons on each grid cell */
    private final JLabel[][] cellLabels;

    /** Displays the list of robots in the GUI */
    private final JLabel robotList = new JLabel();

    /** Stores previous positions of robots for icon cleanup */
    private final Map<String, Position> botPreviousPos = new HashMap<>();

    /** Maps robot names to their assigned avatar icons */
    private final Map<String, ImageIcon> robotSprites = new HashMap<>();

    /** Array of available robot avatar images */
    private final ImageIcon[] botAvatar = {
            getImage("/robot_2.png"),
            getImage("/robot_3.png"),
            getImage("/robot_4.png"),
            getImage("/robot_5.png")
    };

    /** Icons for different obstacle types */
    private final ImageIcon waterIcon, mountainIcon, pitIcon;

    /** Reference to the World object representing the simulation */
    private final World world;

    /**
     * Constructs the WorldGUI, initializing the grid and loading robots/obstacles.
     *
     * @param world the world instance to visualize
     */
    public WorldGUI(World world) {
        this.world = world;

        // Load obstacle icons
        waterIcon = getImage("/water.png");
        mountainIcon = getImage("/mountain.png");
        pitIcon = getImage("/pit.png");

        // Create obstacle legend labels
        JLabel legendPic1 = new JLabel("- Lake", waterIcon, JLabel.LEFT);
        JLabel legendPic2 = new JLabel("- Mountain", mountainIcon, JLabel.LEFT);
        JLabel legendPic3 = new JLabel("- Bottomless Pit", pitIcon, JLabel.LEFT);

        int height = world.getBOTTOM_RIGHT().getY() + 1;
        int width = world.getBOTTOM_RIGHT().getX() + 1;

        // Setup the top status panel
        JPanel statusPanel = new JPanel(new GridLayout(5, 1));
        statusPanel.add(new JLabel("World Size: " + width + "x" + height));
        statusPanel.add(robotList);
        statusPanel.add(legendPic1);
        statusPanel.add(legendPic2);
        statusPanel.add(legendPic3);

        // Setup grid layout to represent the world
        JPanel worldGridPanel = new JPanel(new GridLayout(height, width));
        gridCells = new JPanel[height][width];
        cellLabels = new JLabel[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                gridCells[y][x] = new JPanel(new BorderLayout());
                gridCells[y][x].setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));
                gridCells[y][x].setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                gridCells[y][x].setBackground(Color.WHITE);

                JLabel label = new JLabel();
                cellLabels[y][x] = label;
                gridCells[y][x].add(label);

                worldGridPanel.add(gridCells[y][x]);
            }
        }

        // Assign initial robot avatars and store positions
        int avatarIndex = 0;
        for (Robot r : world.getRobots()) {
            String name = r.getName();
            Position pos = r.getPosition();
            if (!robotSprites.containsKey(name)) {
                robotSprites.put(name, botAvatar[avatarIndex % botAvatar.length]);
                avatarIndex++;
            }
            botPreviousPos.put(name, pos);
        }

        drawObstacles(); // Initial obstacle rendering
        update();        // Initial robot rendering

        // Bottom control panel
        JPanel controlPanel = new JPanel();
        JButton quitButton = new JButton("Quit Server");
        quitButton.addActionListener(e -> System.exit(0));
        controlPanel.add(quitButton);

        // Main frame setup
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame();
            frame.setLayout(new BorderLayout());
            frame.add(statusPanel, BorderLayout.NORTH);
            frame.add(worldGridPanel, BorderLayout.CENTER);
            frame.add(controlPanel, BorderLayout.SOUTH);
            frame.setTitle("Toy Robot");
            frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
            frame.setAlwaysOnTop(true);
            frame.setVisible(true);
        });
    }

    /**
     * Updates the GUI by redrawing robot positions and removing icons of dead or moved robots.
     */
    public void update() {
        robotListLoad();

        // Assign avatars to newly added robots
        int index = robotSprites.size();
        for (Robot r : world.getRobots()) {
            if (!robotSprites.containsKey(r.getName())) {
                robotSprites.put(r.getName(), botAvatar[index % botAvatar.length]);
                index++;
            }
        }

        // Clear icons at old positions if robot moved
        for (Robot r : world.getRobots()) {
            String name = r.getName();
            Position newPos = r.getPosition();
            Position oldPos = botPreviousPos.get(name);
            if (oldPos != null && !oldPos.equals(newPos)) {
                if (inGrid(oldPos.getX(), oldPos.getY())) {
                    cellLabels[oldPos.getY()][oldPos.getX()].setIcon(null);
                }
            }
        }

        // Clear icons of dead robots
        for (String name : botPreviousPos.keySet()) {
            boolean isDeadBot = true;
            for (Robot r : world.getRobots()) {
                if (r.getName().equals(name)) {
                    isDeadBot = false;
                    break;
                }
            }
            if (isDeadBot) {
                int y = botPreviousPos.get(name).getY();
                int x = botPreviousPos.get(name).getX();
                if (inGrid(x, y)) {
                    cellLabels[y][x].setIcon(null);
                }
            }
        }

        // Draw robot icons at new positions
        for (Robot r : world.getRobots()) {
            String name = r.getName();
            Position pos = r.getPosition();
            ImageIcon sprite = robotSprites.get(name);

            if (!inGrid(pos.getX(), pos.getY())) {
                continue;
            }
            JLabel cellLabel = cellLabels[pos.getY()][pos.getX()];
            cellLabel.setIcon(sprite);
            cellLabel.setToolTipText(name); // Show robot name on hover

            botPreviousPos.put(name, pos); // Update last known position
        }

        drawObstacles(); // Ensure obstacles appear above background
    }

    /**
     * Draws all obstacles in the world using their associated icons.
     */
    private void drawObstacles() {
        for (Obstacle o : world.getObstacles()) {
            for (int y = o.getTopLeft().getY(); y <= o.getBottomRight().getY(); y++) {
                for (int x = o.getTopLeft().getX(); x <= o.getBottomRight().getX(); x++) {
                    if (!inGrid(x, y)) {
                        continue;
                    }
                    switch (o.getType()) {
                        case MOUNTAIN -> cellLabels[y][x].setIcon(mountainIcon);
                        case BOTTOMLESS_PIT -> cellLabels[y][x].setIcon(pitIcon);
                        case LAKE -> cellLabels[y][x].setIcon(waterIcon);
                    }
                    gridCells[y][x].setBorder(BorderFactory.createEmptyBorder());
                }
            }
        }
    }

    private boolean inGrid(int x, int y) {
        return y >= 0 && x >= 0 && y < cellLabels.length && x < cellLabels[y].length;
    }

    /**
     * Loads the robot names into the top panel's robot list label.
     */
    private void robotListLoad() {
        StringBuilder robotsStr = new StringBuilder("Robots (" + world.getRobots().size() + "): ");
        List<String> names = new ArrayList<>();
        for (Robot r : world.getRobots()) {
            names.add(r.getName());
        }
        robotsStr.append(String.join(", ", names));
        robotList.setText(robotsStr.toString());
    }

    /**
     * Loads an image from the resources folder and scales it to fit grid cells.
     *
     * @param filepath the path to the image file
     * @return a scaled ImageIcon
     */
    private ImageIcon getImage(String filepath) {
        var resource = WorldGUI.class.getResource(filepath);
        if (resource == null) {
            throw new IllegalStateException("Missing image resource: " + filepath);
        }
        ImageIcon imageIcon = new ImageIcon(resource);
        Image originalImage = imageIcon.getImage().getScaledInstance(CELL_SIZE, CELL_SIZE, Image.SCALE_SMOOTH);
        return new ImageIcon(originalImage);
    }
}
