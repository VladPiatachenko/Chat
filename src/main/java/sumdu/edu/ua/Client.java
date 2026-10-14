package sumdu.edu.ua;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private static final String HOST = "localhost";
    private static final int PORT = 8887;

    public static void main(String[] args) {

        try (
                Socket socket =
                        new Socket(HOST, PORT);

                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        socket.getInputStream()
                                )
                        );

                PrintWriter writer =
                        new PrintWriter(
                                socket.getOutputStream(),
                                true
                        );

                Scanner scanner =
                        new Scanner(System.in)
        ) {

            System.out.println(
                    "Connected to server."
            );

            // Окремий потік постійно слухає сервер
            Thread listener = new Thread(() -> {

                try {

                    String message;

                    while ((message =
                            reader.readLine()) != null) {

                        System.out.println(
                                "\nServer: " + message
                        );

                        System.out.print(
                                "Enter command: "
                        );
                    }

                } catch (IOException e) {

                    System.out.println(
                            "Connection closed."
                    );
                }
            });

            listener.start();

            // main thread відповідає лише за введення
            while (true) {

                System.out.print(
                        "Enter command: "
                );

                String command =
                        scanner.nextLine();

                writer.println(command);

                if ("EXIT".equalsIgnoreCase(command)) {
                    break;
                }
            }

            listener.join();

        } catch (IOException |
                 InterruptedException e) {

            System.out.println(
                    "Client error: "
                            + e.getMessage()
            );
        }
    }
}