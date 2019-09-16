package com.pop.netty.chapter_10;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedFile;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

/**
 * @author Pop
 * @date 2019/9/14 15:44
 */
public class HttpFileServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final String url;

    public HttpFileServerHandler(String url) {
        this.url = url;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {

        //获得解码成果不成功
        if(!request.decoderResult().isSuccess()){
            //输出错误信息
            sendError(ctx,HttpResponseStatus.BAD_REQUEST);
            //返回
            return;
        }

        if(request.method()!= HttpMethod.GET){
            //如果是不是get方法，也输出异常
            sendError(ctx,HttpResponseStatus.METHOD_NOT_ALLOWED);
            return;
        }

        final String uri = request.uri();
        /**
         * 如果URI与允许访问的URI一致，或者是其子目录（文件），则校验通过
         * 否则返回空
         * 不合法
         * 例如
         * http://localhost:8080/http?adb=123这样是无法解析的
         */
        final String path = sanitiseUrl(uri);

        if(path==null){
            //路径不正确，依旧返回错误
            sendError(ctx,HttpResponseStatus.FORBIDDEN);
            return ;
        }

        //我们可以获得文件

        File file = new File(path);

        if(file.isHidden()||!file.exists()){
            //如果是隐藏的文件，或者是不存在的文件，都抛出异常
            sendError(ctx,HttpResponseStatus.NOT_FOUND);
            return ;
        }

        if(file.isDirectory()){
            if(uri.endsWith("/")){
                //会去遍历这个文件目录下面的所有文件
                //另外一种解析
                sendListing(ctx,file);
            }else{
                //加上/接着遍历
                //递归
                sendRedirect(ctx,uri+'/');
            }
            return;
        }
        if(!file.isFile()){
            sendError(ctx,HttpResponseStatus.FORBIDDEN);
            return  ;
        }
        RandomAccessFile randomAccessFile = null;

        try {
            randomAccessFile = new RandomAccessFile(file,"r");//只读模式打开
        } catch (FileNotFoundException e) {
            //异常处理
            sendError(ctx,HttpResponseStatus.NOT_FOUND);
            return;
        }

        long fileLength = randomAccessFile.length();
        //构造一个http response请求
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK);

        //设置文件长度
        response.headers().set("Content-Length",fileLength);
        //设置文件类型头
        setContentTypeHeader(response,file);

        if("keep-alive".equalsIgnoreCase(request.headers().get("Connection"))){//如果是KeepAlive

            response.headers().set("Connection",HttpHeaders.Values.KEEP_ALIVE);

        }
        //将请求写回

        ctx.write(response);

        ChannelFuture sendFileFuture;

        sendFileFuture = ctx.write(new ChunkedFile(randomAccessFile,0,fileLength,8192)
        ,ctx.newProgressivePromise());

        sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
            //这个方法是在传输过程中的操作
            @Override
            public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) throws Exception {
                if(total<0){//总数未知
                    System.err.println(" 传输 进度: "+progress);
                }else{
                    System.err.println(" 传输 进度: "+progress+"/"+total);
                }
            }
            //传输完毕
            @Override
            public void operationComplete(ChannelProgressiveFuture future) throws Exception {
                System.out.println(" 传输 完毕！");
            }
        });

        //如果使用chunked编码，最后需要发送一个编码结束的空消息体
        //LastHttpContent 写入，表示已经完成所有传输任务
        ChannelFuture lastContentFuture  =ctx.
                writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);

        if(!"keep-alive".equalsIgnoreCase(request.headers().get("Connection"))){
            //如果是非keep alive，最后一个发送完毕后，需要主动关闭连接
            lastContentFuture.addListener(ChannelFutureListener.CLOSE);
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        if(ctx.channel().isActive()){
            //抛出异常
            sendError(ctx,HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }


    }

    private static final Pattern INSECURD_URI = Pattern.compile(".*[<>&\"].*");

    private String sanitiseUrl(String uri){

        try {
            uri = URLDecoder.decode(uri,"utf-8");
        } catch (UnsupportedEncodingException e) {
            try {
                uri = URLDecoder.decode(uri,"ISO-8859-1");
            } catch (UnsupportedEncodingException e1) {
                throw new Error();
            }
        }
        //这一段其实是比对请求路径是否符合 /src/com/netty/file 这样的合法性
        if(!uri.startsWith(url)){return null;}
        if(!uri.startsWith("/")){return null;}

        uri = uri.replace('/',File.separatorChar);//将所有的 / 换成 \
        if(uri.contains(File.separator+".")||
            uri.contains('.'+File.separator)||uri.startsWith(".")
                    ||uri.endsWith(".")||INSECURD_URI.matcher(uri).matches()){
            return null;
        }
        //获取文件当前路径
        return System.getProperty("user.dir")+uri;
    }

    private static final Pattern ALLOWED_FILE_NAME = Pattern
            .compile("[A-Za-z0-9][-_A-Za-z0-9\\.]*");

    private static void sendListing(ChannelHandlerContext ctx,File dir){
        //返回文件列表
        FullHttpResponse response = new
                DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK);

        //设置头
        response.headers().set("Content-Type","text/html;charset=utf-8");

        StringBuilder buf = new StringBuilder();

        String dirPath = dir.getPath();

        buf.append("<!DOCTYPE html>\r\n");
        buf.append("<html><head><title>\r\n");
        buf.append(dirPath);

        buf.append("目录:");
        buf.append("</title></head><body>\r\n");
        buf.append("<h3>");
        buf.append(dirPath).append("目录");
        buf.append("</h3>\r\n");
        buf.append("<ul>");
        buf.append("<li>链接:<a href=\"../\">..</a></li>\r\n");
        for (File file : dir.listFiles()){
            if(file.isHidden()||!file.canRead()){ continue;}
            String name = file.getName();
            if(!ALLOWED_FILE_NAME.matcher(name).matches()){ continue;}
            buf.append("<li>链接:<a href=\"");
            buf.append(name);
            buf.append("\">");
            buf.append(name);
            buf.append("</a></li>\r\n");
        }

        buf.append("</ul></body></html>\r\n");

        ByteBuf b = Unpooled.copiedBuffer(buf, Charset.forName("utf-8"));

        response.content().writeBytes(b);

        b.release();

        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);

    }

    private static void sendRedirect(ChannelHandlerContext ctx,String newUri){

        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.FOUND);

        response.headers().set("Location",newUri);

        ctx.writeAndFlush(response).addListener(
                ChannelFutureListener.CLOSE
        );

    }

    private static void sendError(ChannelHandlerContext ctx,
                                  HttpResponseStatus status){
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,status,Unpooled.copiedBuffer("Failure: "+status.toString()+"\r\n",Charset.forName("utf-8"))
        );

        response.headers().set("Content-Type","text/plain;charset=utf-8");

        //什么时候需要加上这个返回
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);

    }

    private static void setContentTypeHeader(HttpResponse response,File file){

        MimetypesFileTypeMap map = new MimetypesFileTypeMap();
        response.headers().set("Content-Type",map.getContentType(file.getPath()));

    }
}
