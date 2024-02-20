import com.sun.security.jgss.GSSUtil;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class Client {
    static String directoryPathClient = "src\\ClientFiles\\";
    static SocketChannel channel;
    static ByteBuffer queryBuffer;
    static ByteBuffer replyBuffer = ByteBuffer.allocate(1000);
    static Scanner keyboard = new Scanner(System.in);

    public static void main(String[] args) throws Exception {
        //expect two command-line arguments from the user
        if (args.length != 2) {
            System.out.println("Syntax: Client <serverIP> <serverPort>");
            return;
        }

        int serverPort = Integer.parseInt(args[1]);

        channel = SocketChannel.open();
        channel.connect(new InetSocketAddress(args[0], serverPort));

        String command;
        System.out.println("Enter a command (type H for help)\n");

        do {

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
                    System.out.println("enter the name of the file in the " +
                            "server directory to be transfered");




                    String targetName=keyboard.nextLine();

                    queryBuffer=ByteBuffer.wrap(("d"+"\n"+targetName+"\n").getBytes());
                    channel.write(queryBuffer);




                    FileOutputStream fs = new FileOutputStream(directoryPathClient +targetName, true);
                    FileChannel fc = fs.getChannel();
                    ByteBuffer reply = ByteBuffer.allocate(1000);
                    while (channel.read(reply) >= 0 ){
                        reply.flip();
                        fc.write(reply);
                        reply.clear();
                    }
                    break;

                case "u":
                    System.out.println("Target file name: ");
                    String target = keyboard.nextLine();


                    queryBuffer= ByteBuffer.wrap(("u\n"+target).getBytes());

                    FileInputStream fis = new FileInputStream(directoryPathClient+ target);
                    FileChannel fic = fis.getChannel();
                    ByteBuffer content = ByteBuffer.allocate(1000);
                    while (fic.read(content) >= 0){
                        content.flip();
                        channel.write(content);
                        content.clear();
                    }
                    fis.close();

                    break;

                default:
                    if(command.charAt(0)!='q'){
                        System.out.println("Invalid command\n");
                    }
            }


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
        queryBuffer=ByteBuffer.wrap(("l"+"\n").getBytes());
        channel.write(queryBuffer);

        int bytesRead = channel.read(replyBuffer);
        replyBuffer.flip();
        byte[] replyArray = new byte[bytesRead];
        replyBuffer.get(replyArray);
        System.out.println(new String(replyArray));

    }
    static void delete() throws IOException {
        System.out.println("Target file name: ");
        String fileName = keyboard.nextLine();
        queryBuffer=ByteBuffer.wrap(("x\n"+fileName+"\n").getBytes());
        channel.write(queryBuffer);

        int bytesRead = channel.read(replyBuffer);
        replyBuffer.flip();
        byte[] replyArray = new byte[bytesRead];
        replyBuffer.get(replyArray);
        System.out.println(new String(replyArray));
    }

    static void rename() throws IOException {
        System.out.println("file to rename: ");
        String target = keyboard.nextLine();
        System.out.println("New name: ");
        String newName = keyboard.nextLine();
        queryBuffer = ByteBuffer.wrap(("r\n"+target+"\n"+newName+"\n").getBytes());
        channel.write(queryBuffer);

        int bytesRead = channel.read(replyBuffer);
        replyBuffer.flip();
        byte[] replyArray = new byte[bytesRead];
        replyBuffer.get(replyArray);
        System.out.println(new String(replyArray));

    }


}

