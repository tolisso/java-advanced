package info.kgeorgiy.ja.malko.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class HelloUDPServer implements HelloServer {
    private DatagramSocket socket;
    private ExecutorService receiver;
    private ExecutorService senders;
    private BlockingQueue<byte[]> buffers;

    @Override
    public void start(int port, int threads) {
        try {
            socket = new DatagramSocket(port);
            socket.setSoTimeout(50);

            buffers = new ArrayBlockingQueue<>(threads);

            final int bufferSize = socket.getReceiveBufferSize();
            buffers.addAll(IntStream
                .range(0, threads)
                .mapToObj(i -> new byte[bufferSize])
                .collect(Collectors.toList()));

            receiver = Executors.newSingleThreadExecutor();
            senders = Executors.newFixedThreadPool(threads);
            receiver.submit(this::receive);
        } catch (SocketException e) {
            System.out.println("Can't create socket on port " + port + ": " + e.getMessage());
        }
    }

    private void receive() {
        while (!socket.isClosed() && !receiver.isShutdown()) {
            byte[] buffer;
            try {
                buffer = buffers.take();
            } catch (InterruptedException e) {
                break;
            }
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                if (!(e instanceof SocketTimeoutException)) {
                    System.err.println("Can't receive packet: " + e.getMessage());
                } else {
                    System.err.println("IOException while receiving packet: " + e.getMessage());
                }
                buffers.add(buffer);
                continue;
            }

            senders.submit(() -> {
                send(packet);
                buffers.add(buffer);
            });
        }
    }

    private void send(DatagramPacket packet) {
        String message = new String(
            packet.getData(),
            packet.getOffset(),
            packet.getLength());
        message = "Hello, " + message;
        byte[] data = message.getBytes();
        packet.setData(data);
        packet.setLength(data.length);
        try {
            socket.send(packet);
            System.out.println("Message got and answered: \"" + message + "\"");
        } catch (IOException e) {
            System.err.println("Can't send packet: " + e.getMessage());
        }
    }

    @Override
    public void close() {
        PoolShutdowner.shutdown(receiver);
        PoolShutdowner.shutdown(senders);
        socket.close();
    }

    public static void main(String[] args) {
        if (args == null || args.length != 2) {
            System.err.println("Expected 2 non null arguments");
            return;
        }
        for (int i = 0; i < 2; i++) {
            if (args[i] == null) {
                System.err.println("Expected 2 non null arguments");
                return;
            }
        }
        final int port;
        final int workers;
        try {
            port = Integer.parseInt(args[0]);
            workers = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.err.println("Can't parse argument: " + e);
            return;
        }

        HelloServer server = new HelloUDPServer();
        server.start(port, workers);

    }
}
