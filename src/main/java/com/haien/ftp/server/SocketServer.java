package com.haien.ftp.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @Author haien
 * @Description ftp服务器
 * @Date 2019/6/27
 **/
public class SocketServer {
    public static int port=21;

    public static void main(String[] args) {

        ServerSocket serverSocket=null;

        try {
            //监听端口
            serverSocket=new ServerSocket(port);
            //支持多线程
            while (true){
                //获得连接
                Socket socket=serverSocket.accept();
                //开线程
                new LogicThread(socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}



























