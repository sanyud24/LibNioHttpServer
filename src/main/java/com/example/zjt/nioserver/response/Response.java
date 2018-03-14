package com.example.zjt.nioserver.response;
import com.example.zjt.nioserver.constant.Constant;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

/**
 * Created by zjt on 18-2-1.
 */

public class Response {
    private Charset mCharset;

    public Response(){
        mCharset = Constant.getInstance().charset;
    }

    public ByteBuffer encode(String str) {
        return mCharset.encode(str);
    }

    public String responseHead(Status status, String mimeType) {
        return "HTTP/1.1 " + status.getDescription() + "\r\n" +
                "Server: NIO_SERVER_1.0\r\n" +
                "Charset: UTF-8\r\n" +
                "Content-Type: " + mimeType + "\r\n" +
                "Cache-Control: no-cache\r\n" +
                "Access-Control-Allow-Origin: *\r\n";
    }

    public String responseHead(Status status, String mimeType, String contentLength) {
        return "HTTP/1.1 " + status.getDescription() + "\r\n" +
                "Server: NIO_SERVER_1.0\r\n" +
                "Charset: UTF-8\r\n" +
                "Content-Type: " + mimeType + "\r\n" +
                "Cache-Control: no-cache\r\n" +
                "Access-Control-Allow-Origin: *\r\n" +
                "Content-Length: " + contentLength + "\r\n\r\n";
    }

    public void response(SocketChannel socketChannel, InputStream inputStream,
                         Status status, String mimeType) throws IOException {
        if (inputStream != null) {
            ByteBuffer headerBuffer = ByteBuffer.wrap(responseHead(status, mimeType,
                    String.valueOf(inputStream.available())).getBytes(mCharset));
            while (headerBuffer.hasRemaining()){
                socketChannel.write(headerBuffer);
            }
            byte[] read = new byte[1024 * 1024];
            int len = 0;
            while ((len = inputStream.read(read)) > 0) {
                ByteBuffer writeBuffer = ByteBuffer.wrap(read,0,len);
                while(writeBuffer.hasRemaining()) {
                    socketChannel.write(writeBuffer);
                }
            }
        } else {
            ByteBuffer headerBuffer = encode(responseHead(status, mimeType, "0"));
            headerBuffer.flip();
            socketChannel.write(headerBuffer);
        }
    }

    public void response(SocketChannel socketChannel, String data,
                         Status status, String mimeType) throws IOException {
        if (data != null) {
            ByteBuffer dataBuffer = ByteBuffer.wrap(data.getBytes(mCharset));
            ByteBuffer headerBuffer = ByteBuffer.wrap(responseHead(status, mimeType,
                    String.valueOf(dataBuffer.remaining())).getBytes(mCharset));
            while (headerBuffer.hasRemaining()){
                socketChannel.write(headerBuffer);
            }
            while (dataBuffer.hasRemaining()){
                socketChannel.write(dataBuffer);
            }
        } else {
            ByteBuffer headerBuffer = encode(responseHead(status, mimeType, "0"));
            headerBuffer.flip();
            socketChannel.write(headerBuffer);
        }
    }
}
