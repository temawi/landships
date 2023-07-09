package gg.landships;

import com.badlogic.gdx.Gdx;

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

    public int inBytes;
    public int outBytes;

    NetHandler(String ip, int port) {
        try {
            // try to connect
            socket = new Socket(ip, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream());

            // start a thread to read input
            thread = new Thread(new NetReader(this));
            thread.start();

            System.out.println("NetHandler: New NetReader created");
        } catch (IOException e) {
            // something went wrong
            System.out.println("NetHandler: IOException connecting");
        }
    }

    public void resetByteCounter() {
        // reset the traffic byte counters
        inBytes = 0;
        outBytes = 0;
    }

    public void write(String s) {
        // write and flush a string to the server
        writer.println(s);
        writer.flush();
    }

    public void dispose() throws IOException {
        // clean everything up
        socket.close();
        reader.close();
        writer.close();

        // stop the thread
        thread.stop();
    }
}
