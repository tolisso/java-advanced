package info.kgeorgiy.ja.malko.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class HelloUDPClient implements HelloClient {

    @Override
    public void run(final String host, final int port, final String prefix, final int threads, final int requests) {
        final SocketAddress address = new InetSocketAddress(host, port);
        final ExecutorService pool = Executors.newFixedThreadPool(threads);
        IntStream.range(0, threads).forEach(i -> pool.submit(() ->
            send(i, prefix, address, requests)));
        PoolShutdowner.shutdown(pool);
    }

    private void send(final int number, final String prefix, final SocketAddress address,
                      final int requests) {
        try (final DatagramSocket socket = new DatagramSocket()) {
            final byte[] buffer = new byte[socket.getReceiveBufferSize()];
            final DatagramPacket packet =
                new DatagramPacket(new byte[0], 0, address);
            socket.setSoTimeout(100);
            for (int i = 0; i < requests; i++) {
                while (!socket.isClosed()) {
                    String requestString = buildRequest(prefix, number, i);
                    byte[] requestBody = (requestString).getBytes();

                    packet.setData(requestBody, 0, requestBody.length);

                    try {
                        socket.send(packet);
                    } catch (final IOException e) {
                        System.err.println("Can't send packet \"" + requestString +
                            "\": " + e.getMessage());
                        continue;
                    }

                    try {
                        packet.setData(buffer);
                        socket.receive(packet);
                        final String response = new String(packet.getData(), packet.getOffset(),
                            packet.getLength());
                        if (!checkResponse(number, i, response)) {
                            System.err.println("Can't receive packet \"" + requestString +
                                "\": " + "corrupted answer");
                            continue;
                        }
                        System.out.println(response);
                        break;
                    } catch (final IOException e) {
                        System.err.println("Can't receive packet \"" + requestString +
                            "\": " + e.getMessage());
                    }
                }
            }
        } catch (final SocketException exc) {
            System.err.println("Can't create socket: " + exc.getMessage());
        }
    }

    public static String buildRequest(String prefix, int number,  int i) {
        return prefix + number + "_" + i;
    }

    public static boolean checkResponse(int number, int i, String response) {
        return response.matches("[\\D]*" + number + "[\\D]*" + i + "[\\D]*");
    }

    public static void main(String[] args) {
        if (args == null || args.length != 5) {
            System.err.println("Expected 5 non null arguments");
            return;
        }
        for (int i = 0; i < 5; i++) {
            if (args[i] == null) {
                System.err.println("Expected 5 non null arguments");
                return;
            }
        }

        final String host = args[0];
        final int port;
        final String prefix = args[2];
        final int threads;
        final int requests;
        try {
            port = Integer.parseInt(args[1]);
            threads = Integer.parseInt(args[3]);
            requests = Integer.parseInt(args[4]);
        } catch (NumberFormatException e) {
            System.err.println("Can't parse argument: " + e.getMessage());
            return;
        }

        HelloClient client = new HelloUDPClient();
        client.run(host, port, prefix, threads, requests);
    }
}
