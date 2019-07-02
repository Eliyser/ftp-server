package com.haien.ftp.server;

import java.io.*;
import java.net.Socket;

/**
 * @Author haien
 * @Description 处理请求线程
 * @Date 2019/6/27
 **/
public class LogicThread extends Thread{
    Socket socket;
    //由于是简单文本输入输出，所以直接用InputStream、OutputStream
    InputStream is;
    OutputStream os;
    //是否登录标记
    boolean login=false;
    //文件存储路径：本项目下data目录
    String dataPath="data/";

    /**
     * @Author haien
     * @Description 启动线程
     * @Date 11:16 2019/6/27
     * @Param [socket]
     * @return
     **/
    public LogicThread(Socket socket) {
        this.socket = socket;
        start();
    }

    @Override
    public void run() {
        //1024足够，命令很短
        byte[] b=new byte[1024];
        String response,request;

        try {
            os=socket.getOutputStream();
            is=socket.getInputStream();
            int len=0;
            while (true) {
                //读取命令
                len = is.read(b);
                request = new String(b, 0, len);
                response="status:";

                if(request.equals("quit")){
                    break;
                }

                //用户注册
                if(request.startsWith("user")) {
                    response+=register(request);
                    os.write(response.getBytes());
                }

                //查看文件列表
                if(request.equals("dir")){
                    //未登录，要求账号
                    if(!login){
                        response=response+"332";
                    }else {
                        StringBuilder filenames = new StringBuilder();
                        list(dataPath, filenames, 0);
                        response = response + "200" + ";fileList:" + filenames.toString();
                    }

                    os.write(response.getBytes());
                }

                //下载文件
                if(request.startsWith("get")){
                    //未登录，要求账号
                    if(!login){
                        response=response+"332";
                        os.write(response.getBytes());
                    }else {
                        String filename = request.split(" ")[1];

                        String status = download(dataPath + filename, os);

                        //下载失败
                        if (status != "200") {
                            response += status;
                            os.write(response.getBytes());
                        }
                    }
                }

            }
        } catch (IOException e1) {
            e1.printStackTrace();
        } finally {
            try {
                os.close();
                is.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @Author haien
     * @Description 用户注册：客户端将命令封装为字符串：user username;pass password
     * @Date 2019/6/27
     * @Param [request]
     * @return java.lang.String
     **/
    private String register(String request){
        String[] userAndPass=request.split(";");
        String username=userAndPass[0].split(" ")[1];
        String psw=userAndPass[1].split(" ")[1];

        //参数错误：用户名、密码只能输入字母、数字和下划线

        if(!(username.matches("\\w{6,12}"))
           ||!(psw.matches("\\w{6,12}")))
            return "501";

        //注册成功
        login=true;
        return "200";
    }

    /**
     * @Author haien
     * @Description 查看文件列表:缩进显示
     * @Date 2019/6/27
     * @Param [request]
     * @return java.lang.String
     **/
    private void list(String path,StringBuilder fileNames,int tab){
        //文件存储在当前项目data目录下
        File dir=new File(path);

        if(dir.isDirectory()) {
            int len=dir.listFiles().length;
            for(int k=0;k<tab&&len>0;k++){
                fileNames.append("\t");
            }

            //获取data/下一级子文件的文件对象
            for (File subFile : dir.listFiles()) {
                fileNames.append(subFile.getName()).append("\r\n");

                len--;
                for(int k=0;k<tab&&len>0;k++){
                    fileNames.append("\t");
                }

                tab++;
                list(subFile.getPath(),fileNames,tab);
                tab--;
            }
        }

    }

    /**
     * @Author haien
     * @Description 下载文件到指定目录
     * @Date 2019/6/28
     * @Param [filename, destPath]
     * @return java.lang.String
     **/
    private String download(String filename,OutputStream os) {

        BufferedInputStream bis=null;
        try {
            bis=new BufferedInputStream(new FileInputStream(filename));

            byte[] buf=new byte[1024];
            int len=0;
            while (-1!=(len=bis.read(buf))){
                os.write(buf,0,len);
            }
            os.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            //文件不存在
            return "504";
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                //bos不能关闭

                //注意！一定要先检查bis是否为null，
                //当文件不存在时，创建bis失败，其为null，直接执行关闭操作会抛出无法捕获的异常
                if(bis!=null) {
                    bis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "200";
    }
}






























