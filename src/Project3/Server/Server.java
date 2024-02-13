package Project3.Server;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class Server {
    public static void main(String[] args) throws Exception {
        //expect one command-line argument from the admin
        if (args.length != 1) {
            System.out.println("Syntax: Server <serverPort>");
            return;
        }
        int port = Integer.parseInt(args[0]);

        //the listen channel always listens for connection
        // requests from clients and
        // performs the three-way handshake with new clients
        ServerSocketChannel listenChannel =
                ServerSocketChannel.open();
        listenChannel.bind(new InetSocketAddress(port));

        while(true) {
            SocketChannel serveChannel = listenChannel.accept();
            ByteBuffer request = ByteBuffer.allocate(1024);
            int numBytes = serveChannel.read(request);

            request.flip();
            String command = new String(request.toString().substring(0,0));

            switch (command.toLowerCase()){
                case "l":
                    list();
                case "x":

                case "r":

                case "u":

                case "d":

                default:

            }
        }
    }

    static void list(){

    }

    static void delete(){}

    static void rename(){}

    static void upload(){}

    static void download(){}
}
