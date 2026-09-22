package za.co.wethinkcode.robots.command;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import za.co.wethinkcode.robots.world.World;

/**
 * Abstract class representing a command to be executed in the game.
 * This class serves as a base for all specific command implementations.
 * It contains common properties and methods that all commands share.
 * All concrete command classes should implement the singleton pattern.
 */
public abstract class Command {
    private String name;
    private String argument;
    private JsonArray arguments;

    /**
     * Constructor to create a Command object with a name.
     * Protected to allow subclasses to use it while preventing direct instantiation.
     *
     * @param name the name of the command
     */
    protected Command(String name){
        this.name = name.trim().toLowerCase();
    }

    /**
     * Constructor to create a Command object with a name and an argument.
     * Protected to allow subclasses to use it while preventing direct instantiation.
     *
     * @param name the name of the command
     * @param argument the argument for the command
     */
    protected Command(String name, String argument) {
        this(name);
        this.argument = argument != null ? argument.trim() : null;
    }

    /**
     * Constructor to create a Command object with a name, an argument, and a JsonArray of arguments.
     * Protected to allow subclasses to use it while preventing direct instantiation.
     *
     * @param name the name of the command
     * @param argument the argument for the command
     * @param arguments a JsonArray of additional arguments for the command
     */
    protected Command(String name, String argument, JsonArray arguments) {
        this(name, argument);
        this.arguments = arguments;
    }

    /**
     * Sets the argument for the command.
     *
     * @param argument the argument to set
     */
    public void setArgument(String argument) {
        this.argument = argument;
    }

    /**
     * Sets the arguments for the command.
     *
     * @param arguments the JsonArray of arguments to set
     */
    public void setArguments(JsonArray arguments) {
        this.arguments = arguments;
    }

    /**
     * Sets the name of the command.
     *
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the arguments of the command.
     * @return the arguments of the command
     */
    public JsonArray getArguments() {
        return arguments;
    }

    /**
     * Gets the name of the command.
     *
     * @return the name of the command
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the argument of the command.
     *
     * @return the argument of the command
     */
    public String getArgument() {
        return this.argument;
    }

    /**
     * Executes the command in the given world.
     * This method should be implemented by subclasses to define the specific behavior of the command.
     *
     * @param world the world in which the command is executed
     * @return a JsonObject containing the result of the command execution
     */
    public abstract JsonObject execute(World world);

}
