package za.co.wethinkcode.robots.server;

import za.co.wethinkcode.robots.world.World;
import za.co.wethinkcode.robots.world.WorldGate;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * MultiServerEngine is a server engine that handles multiple client connections.
 * It accepts client connections, creates a new thread for each client, and allows
 * broadcasting messages to all connected clients.
 */
public class MultiServerEngine {
    private final List<PrintStream> clientOutputs = new ArrayList<>();
    private ServerSocket serverSocket;
    private Thread clientAcceptThread;
    private final World world;
    private final WorldGate worldGate;

    /**
     * Constructs a MultiServerEngine with the specified world.
     * Tests use this constructor and run world work on the caller.
     * Production passes a shared {@link WorldGate#serialized()} instance.
     */
    public MultiServerEngine(World world) {
        this(world, WorldGate.immediate());
    }

    public MultiServerEngine(World world, WorldGate worldGate) {
        this.world = world;
        this.worldGate = worldGate;
    }

    /**
     * Starts the server on the specified port.
     *
     * @throws IOException if an I/O error occurs when opening the socket
     */
    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);

        System.out.println("Server running on port " + port + " & waiting for client connections...");


        clientAcceptThread = new Thread(() -> {
            while (!serverSocket.isClosed()) {
                try {
                    Socket socket = serverSocket.accept();
                    System.out.println("New client connected: " + socket);

                    PrintStream clientOut = new PrintStream(socket.getOutputStream());
                    synchronized (clientOutputs) {
                        clientOutputs.add(clientOut);
                    }

                    new Thread(new Server(socket, world, worldGate)).start();
                } catch (IOException e) {
                    if (!serverSocket.isClosed()) {
                        System.err.println("Client accept error: " + e.getMessage());
                    }
                }
            }
        });

        clientAcceptThread.start();
    }




    public  int getPort(){
        if (serverSocket == null){
            throw new IllegalArgumentException("Server has not been started.");
        }
        return serverSocket.getLocalPort();
    }
    public void broadcastMessage(String message) {
        synchronized (clientOutputs) {
            for (PrintStream out : clientOutputs) {
                out.println(message);
                out.flush();
            }
        }
    }


    /**
     * Shuts down the server and closes all client connections.
     *
     * @throws IOException if an I/O error occurs when closing the socket
     */
    public void shutdown() throws IOException {
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
            System.out.println("Server socket closed.");
        }
        if (clientAcceptThread != null && clientAcceptThread.isAlive()) {
            clientAcceptThread.interrupt();
        }
    }


}