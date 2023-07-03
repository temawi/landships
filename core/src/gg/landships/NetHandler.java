package gg.landships;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class NetHandler {
    public Socket socket;
    public BufferedReader reader;
    public PrintWriter writer;
    private Thread thread;

    NetHandler(String ip, int port) {
        try {
            socket = new Socket(ip, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream());

            thread = new Thread(new NetReader(this));
            thread.start();

            System.out.println("NetHandler: New NetReader created");
        } catch (IOException e) {
            System.out.println("NetHandler: IOException connecting");
        }
    }

    public void write(String s) {
        writer.println(s);
        writer.flush();
    }

    public void dispose() throws IOException {
        socket.close();
        reader.close();
        writer.close();
        thread.stop();
    }
}
