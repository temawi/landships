package gg.landships.server;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    static ExecutorService pool = Executors.newFixedThreadPool(10);
    static ServerSocket serverSocket;
    static ArrayList<Client> clients = new ArrayList<>();
    static boolean silent = false;

    public static void broadcast(String s) {
        for(Client c: new ArrayList<>(clients)) {
            c.out.println(s);
            c.out.flush();
        }
    }

    public static JSONObject getJoinPacket(Client c) {
        JSONObject obj = new JSONObject();
        obj.put("type", 0);
        obj.put("id", clients.indexOf(c));
        return obj;
    }

    public static JSONObject addIdToPacket(JSONObject o, Client c) {
        o.put("id", clients.indexOf(c));
        return o;
    }

    public static void main(String[] args) throws IOException {
        serverSocket = new ServerSocket(27015);
        System.out.println("gg.landships.server.Server running port " + serverSocket.getLocalPort());

        try {
            if("-silent".equals(args[0])) {
                silent = true;
            }
        } catch (IndexOutOfBoundsException ignored) {}

        while(true) {
            final Socket newSocket = serverSocket.accept();
            final Client newClient = new Client(newSocket);

            System.out.println("New client connected from " + newSocket.getInetAddress());
            clients.add(newClient);

            broadcast(getJoinPacket(newClient).toJSONString());

            pool.execute(new Runnable() {
                @Override
                public void run() {
                    while(true) {
                        try {
                            String line = newClient.in.readLine();
                            if(!silent) {System.out.println("TrafficLog: " + line);}

                            if(line == null) { throw new IOException(); }

                            JSONParser parser = new JSONParser();
                            JSONObject object = (JSONObject) parser.parse(line);
                            addIdToPacket(object, newClient);

                            if((long)object.get("type") != 0) {
                                broadcast(object.toJSONString());
                            }
                        } catch (IOException e) {
                            System.out.println("Terminating client at " + newSocket.getInetAddress());
                            return;
                        } catch (ParseException ignored) {}
                    }
                }
            });
        }
    }
}
