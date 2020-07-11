package server;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.Vector;

public class Server {
    private Vector<ClientHandler> clients;

    public Server() {
        ServerSocket server = null;
        Socket socket = null;

        try {
            AuthService.connect();
            server = new ServerSocket(8189);
            System.out.println("Сервер запущен!");
            clients = new Vector<>();

            while (true) {
                socket = server.accept();
                System.out.println("Клиент подключился");
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            AuthService.disconnect();
        }
    }

    public void subscribe(ClientHandler client) {
        clients.add(client);
    }

    public void unsubscribe(ClientHandler client) {
        clients.remove(client);
    }


    //   БЫЛО
//    public void broadcastMsg(String msg) {
//        for (ClientHandler o : clients) {
//            o.sendMsg(msg);
//        }
//    }
// СТАЛО        Отправить сообщение всему Вектору с именем отправителя, а себе Я:
    public void broadcastMsg(String nick, String msg) {
        String msg2 = null;
        for (ClientHandler o : clients) {
            if (o.getNick().equals(nick)) {
                msg2 = ("Я: " + msg);
                o.sendMsg(msg2);
            } else {
                o.sendMsg(nick + ": " + msg);
            }
        }
    }


    public boolean checkOnline(String nick) {
        for (ClientHandler o : clients) {
            if (o.getNick().equals(nick)) {
                return true;
            }
        }
        return false;
    }

    // получаем на входе Nickname отправителя и сообщение str
    //str = /private пробел Кому пробел Сообщение

    public void privateMessage(String messageFromNickname, String str) {

        String[] tokens = str.split(" "); // делим наше сообщение на 0-1-2(и может даже больще слов) токены
        String messageToNickname = tokens[1];


        StringBuilder msg = new StringBuilder();
        for (int i = 2; i < tokens.length; i++) {
            msg.append(tokens[i]);
            msg.append(" ");
        }

        // Перебрать вектор и отправить сообщение клиенту с именем клиента

        boolean Sended = false;
        for (ClientHandler o : clients) {
            if (o.getNick().equals(messageToNickname)) {
                o.sendMsg("Сообщение от " + messageFromNickname + ": " + msg.toString());
                for (ClientHandler o2 : clients) {
                    if (o2.getNick().equals(messageFromNickname)) {
                        o2.sendMsg("Сообщение только для " + messageToNickname + ": " + msg.toString());
                        Sended = true;
                    }
                }
            }
        }
        if (!Sended) {
            for (ClientHandler o : clients) {
                if (o.getNick().equals(messageFromNickname)) {
                    o.sendMsg("Доставка сообщения невозможна");
                }

            }
        }
    }
}




