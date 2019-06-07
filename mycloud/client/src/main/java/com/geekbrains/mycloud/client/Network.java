package com.geekbrains.mycloud.client;

import com.geekbrains.mycloud.common.*;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;

import java.io.IOException;
import java.net.Socket;


public class Network {
    private static Socket socket;
    private static ObjectEncoderOutputStream out;
    private static ObjectDecoderInputStream in;

    public static void start() {   //инициализация сетевого соединения, определение вх и вых потоков
        try {
            socket = new Socket("localhost", 8199);
            out = new ObjectEncoderOutputStream(socket.getOutputStream());
            in = new ObjectDecoderInputStream(socket.getInputStream(), 5000 * 1024 * 1024);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void stop() { //закрытие соединения и всех потоков
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // пересылаем данные в форме объекта типа AbstractMessagе и его наследников
    public static boolean sendMsg(AbstractMessage msg) {
        try {
            out.writeObject(msg);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean sendMsg(FileMessage msg) {

        try {
            out.writeObject(msg);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    public static boolean sendAuthMes(String login, String password) {
        try {
            out.writeObject(new AuthMessage(login, password));
            out.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean sendRegMessage(String login, String password) {
        try {
            out.writeObject(new RegMessage(login, password));
            out.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean sendRefreshMessage(RefreshMessage msg) {

        try {
            out.writeObject(msg);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    //вычитываем данные из входящего раскодированного потока, возвращаем данные в форме объекта типа AbstractMessagе и его наследников
    public static AbstractMessage readObject() throws ClassNotFoundException, IOException {
        Object obj = in.readObject();
        return (AbstractMessage) obj;
    }

    public static Object readInObject() throws IOException, ClassNotFoundException {
        Object object = in.readObject();
        return object;
    }


    public static boolean sendFdm(String fileDelName) {

        try {
            out.writeObject(new FileDeleteMessage(fileDelName));
            out.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}

