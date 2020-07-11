package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private Server server;
    private String nick;

    public String getNick(){
        return this.nick;
    }

    public ClientHandler(Server server, Socket socket) {
        try {
            this.socket = socket;
            this.server = server;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            String str = in.readUTF();
                            if (str.startsWith("/auth")) {
                                String[] tokens = str.split(" ");
                                String newNick = AuthService.getNickByLoginAndPass(tokens[1], tokens[2]);

                                        if (newNick != null){
                                            if (!server.checkOnline(newNick)){
                                                nick = newNick;
                                                sendMsg("/authok");
                                                server.subscribe(ClientHandler.this);

                                                break;
                                            } else {
                                                sendMsg("Учетная запись уже занята");
                                            }
                                        } else {
                                            sendMsg("Неверный логин/пароль");

                                }
                            }
                        }

                        while (true) {
                            String str = in.readUTF();
                            if (str.equals("/end")) {
                                out.writeUTF("/serverClosed");
                                break;
                            }

                // БЫЛО                       server.broadcastMsg(nick + ": " + str);
                // СТАЛО
                // набираем /private кому-NickName Сообщение
                // передаем (отправитель, сообщение), где сообщение = /private + Кому + сообщение

                            if (str.startsWith("/private")){
                                server.privateMessage(nick, str);
                            } else {

//                                server.broadcastMsg(nick + ": " + str);
                                server.broadcastMsg(nick, str);
                            }

                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        server.unsubscribe(ClientHandler.this);
                    }

                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}