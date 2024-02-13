package Project3.Client;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class Client {
    static SocketChannel channel;
    static ByteBuffer queryBuffer;
    static ByteBuffer replyBuffer = ByteBuffer.allocate(1024);
    static Scanner keyboard = new Scanner(System.in);
    public static void main(String[] args) throws Exception {
        //expect two command-line arguments from the user
        if (args.length != 2) {
            System.out.println("Syntax: Client <serverIP> <serverPort>");
            return;
        }

        //InetAddress serverIP = InetAddress.getByName(args[0]);
        int serverPort = Integer.parseInt(args[1]);

        channel = SocketChannel.open();
        channel.connect(new InetSocketAddress(args[0], serverPort));

        //"GetTime", "GetDate"

        String command;
        System.out.println("Enter a command (type h for help)\n");



        do{
            command = keyboard.nextLine();
            switch (command.toLowerCase()){
                case "h":
                    help();
                    break;
                case "l":
                    list();
                    break;
                case "x":
                    delete();
                    break;
                case "r":
                    rename();
                    break;
                case "d":
                    download();
                    break;
                case "u":
                    upload();
                    break;
                default:
                    if(command.charAt(0)!='q'){
                        System.out.println("Invalid command\n");
                    }
            }

            channel.close();
        }while (command.charAt(0)!='q');
    }

    static void help(){
        System.out.println(
                "Commands:\n" +
                "h: display list of commands\n" +
                "l: display list of files on server\n" +
                "x: delete file on server\n" +
                "r: rename file on server\n" +
                "d: download file from server\n" +
                "u: upload file to server\n" +
                "q: quit program"
        );
    }

    static void list() throws Exception{
        queryBuffer=ByteBuffer.wrap("l".getBytes());
        channel.write(queryBuffer);
    }
    static void delete() throws IOException {
        System.out.println("Target file name: ");
        String fileName = keyboard.nextLine();
        queryBuffer=ByteBuffer.wrap(("d"+fileName).getBytes());
        channel.write(queryBuffer);
    }

    static void rename(){
        System.out.println("Target file name: ");
        String target = keyboard.nextLine();
        System.out.println("New name: ");
        String newName = keyboard.nextLine();
        queryBuffer = ByteBuffer.wrap(("r"+target+"\n"+newName).getBytes());
    }
    static void download(){
        System.out.println("Target file name: ");
        String target = keyboard.nextLine();
        queryBuffer= ByteBuffer.wrap(("d"+target).getBytes());
    }

    static void upload(){
        File target = null;
        String targetName;
        do {
            System.out.println("Target file name: ");
            targetName=keyboard.nextLine();
            target = new File("Files/"+targetName);
        } while (!target.isDirectory());
        System.out.println(target.toString());
        queryBuffer= ByteBuffer.wrap(("d"+target+target.toString()).getBytes());

    }
}
