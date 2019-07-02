package com.haien.ftp.client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * @Author haien
 * @Description ftp服务器客户端
 * @Date 2019/6/27
 **/
public class SocketClient {
    private static String ip="127.0.0.1";
    private static int port=21;

    public static void main(String[] args) {
        //打印菜单
        System.out.println("------------------------------------------------------");
        System.out.println("|欢迎使用ftp服务器！                                   |");
        System.out.println("|菜单: user username 注册用户名                        |");
        System.out.println("|      pass password 注册密码                          |");
        System.out.println("|      dir 显示文件列表                                |");
        System.out.println("|      get filename path 下载文件到指定路径(默认当前路径)|");
        System.out.println("|      quit 退出                                      |");
        System.out.println("-------------------------------------------------------");

        //用户输入
        Scanner in=new Scanner(System.in);
        String commond;

        OutputStream os=null;
        InputStream is=null;
        Socket socket=null;
        try {
            //建立连接
            socket=new Socket(ip,port);
            os=socket.getOutputStream();
            is=socket.getInputStream();
            //缓冲数组
            byte[] b=new byte[1024];
            //读取输入流长度
            int len;
            //接收反馈
            String response;
            //反馈状态码
            String status;

            //支持多次数据交换
            while(!(commond=in.nextLine().toLowerCase().trim()).equals("")){

                //退出：应关闭服务端流和socket
                if(commond.equals("quit")){
                    os.write(commond.getBytes());
                    break;
                }

                //用户注册
                if(commond.startsWith("user")){

                    //输入密码
                    String pass=in.nextLine().toLowerCase().trim();
                    //输入为空，继续输入
                    while (pass.equals("")){
                        pass=in.nextLine().toLowerCase().trim();
                    }

                    commond=commond+";"+pass;
                    os.write(commond.getBytes());
                    len=is.read(b);
                    response=new String(b,0,len);
                    status=response.split(":")[1];
                    if(status.equals("501")){
                        System.out.println("用户名、密码必须由6-12位字母、数字或下划线组成!");
                    }
                    if(status.equals("200")){
                        System.out.println("注册成功!");
                    }
                }

                //查看文件列表
                else if(commond.startsWith("get")){
                    String[] params=commond.split("\\s+");
                    int block=params.length;

                    if(block<2){
                        System.out.println("无效命令，请重新输入！");
                        continue;
                    }

                    //使用默认路径
                    String destPath="./";
                    //使用指定路径
                    if(block==3){
                        destPath=params[2];
                    }
                    if(!destPath.endsWith("/")){
                        destPath+="/";
                    }

                    File destDir=new File(destPath);
                    if(!destDir.exists()||!destDir.isDirectory()){
                        System.out.println("下载路径不存在！");
                        continue;
                    }

                    os.write(commond.getBytes());

                    //判断下载是否成功
                    len=is.read(b);
                    response=new String(b,0,len);
                    String[] entry=response.split(":");
                    if(entry.length==2){
                        if(entry[1].equals("332"))
                            System.out.println("请先注册！");
                        if(entry[1].equals("504"))
                            System.out.println("指定文件不存在！");
                    }

                    //下载成功
                    else {
                        //得到文件名
                        String filename = new File(params[1]).getName();
                        //拼接文件路径
                        destPath += filename;
                        OutputStream fos = new FileOutputStream(destPath);

                        while (-1 != len) {
                            fos.write(b, 0, len);
                            if(len<1024){
                                break;
                            }
                            len = is.read(b);
                        }

                        System.out.println("下载成功！");
                    }
                }

                //获取文件列表
                else if(commond.equals("dir")){
                    os.write(commond.getBytes());
                    len=is.read(b);
                    response=new String(b,0,len);
                    String[] msg=response.split(";");
                    if(msg.length==1){
                        status=msg[0].split(":")[1];
                        if(status.equals("332")){
                            System.out.println("请先注册！");
                        }
                        else{
                            System.out.println("命令执行失败！");
                        }
                    }else{
                        //获取文件名字符串
                        response=msg[1].split(":")[1];
                        if(response.length()==0){
                            System.out.println("当前无文件！");
                        }
                        System.out.println(response);
                    }
                }

                else{
                    System.out.println("输入错误，请重新输入！");
                }

            }

            System.out.println("Bye~");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                //关闭流和连接
                is.close();
                os.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}




























