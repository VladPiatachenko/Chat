package sumdu.edu.ua;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final int clientNumber;
    private final List<ClientHandler> clients;

    private PrintWriter writer;

    public ClientHandler(
            Socket socket,
            int clientNumber,
            List<ClientHandler> clients
    ) {
        this.socket = socket;
        this.clientNumber = clientNumber;
        this.clients = clients;
    }

    @Override
    public void run() {

        String clientName = "Client-" + clientNumber;

        System.out.println(
                clientName + " connected. Thread: "
                        + Thread.currentThread().getName()
        );

        try (
                socket;

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                socket.getInputStream()
                        )
                )
        ) {

            writer = new PrintWriter(
                    socket.getOutputStream(),
                    true
            );

            String message;

            while ((message = reader.readLine()) != null) {

                System.out.println(
                        clientName + ": " + message
                );

                if ("EXIT".equalsIgnoreCase(message)) {
                    writer.println("Goodbye!");
                    break;
                }

                // BROADCAST має окрему поведінку
                if (message.toUpperCase()
                        .startsWith("BROADCAST ")) {

                    String text =
                            message.substring(10);

                    broadcast(
                            "[BROADCAST from "
                                    + clientName
                                    + "] "
                                    + text
                    );

                    continue;
                }

                String response =
                        processCommand(message);

                writer.println(response);
            }

        } catch (IOException e) {

            System.out.println(
                    clientName
                            + " connection error: "
                            + e.getMessage()
            );

        } finally {

            // Клієнт більше не активний
            clients.remove(this);

            System.out.println(
                    clientName + " disconnected"
            );
        }
    }

    private void broadcast(String message) {

        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public void sendMessage(String message) {

        if (writer != null) {
            writer.println(message);
        }
    }

    private String processCommand(String command) {

        if (command.equalsIgnoreCase("TIME")) {

            return LocalTime.now().format(
                    DateTimeFormatter.ofPattern(
                            "HH:mm:ss"
                    )
            );
        }

        int space = command.indexOf(' ');

        String operation;
        String text;

        if (space == -1) {
            operation = command.toUpperCase();
            text = "";
        } else {
            operation =
                    command.substring(0, space)
                            .toUpperCase();

            text = command.substring(space + 1);
        }

        return switch (operation) {

            case "UPPER" ->
                    text.toUpperCase();

            case "LOWER" ->
                    text.toLowerCase();

            case "REVERSE" ->
                    new StringBuilder(text)
                            .reverse()
                            .toString();

            case "LENGTH" ->
                    String.valueOf(text.length());

            default ->
                    "Unknown command";
        };
    }
}