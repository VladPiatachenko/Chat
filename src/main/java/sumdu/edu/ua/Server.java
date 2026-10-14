package sumdu.edu.ua;

import sumdu.edu.ua.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {

    private static final int PORT = 8887;

    private static final AtomicInteger clientCounter =
            new AtomicInteger(0);

    // Усі підключені клієнти
    private static final List<ClientHandler> clients =
            new CopyOnWriteArrayList<>();

    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            System.out.println("Server started on port: " + PORT);
            System.out.println("Waiting for clients...\n");

            while (true) {

                Socket clientSocket = serverSocket.accept();

                int clientNumber =
                        clientCounter.incrementAndGet();

                ClientHandler handler =
                        new ClientHandler(
                                clientSocket,
                                clientNumber,
                                clients
                        );

                // Додаємо нового клієнта до списку
                clients.add(handler);

                Thread thread = new Thread(
                        handler,
                        "Client-" + clientNumber
                );

                thread.start();
            }

        } catch (IOException e) {
            System.out.println(
                    "Server error: " + e.getMessage()
            );
        }
    }
}
