package com.example.zjt.nioserver.server;

import android.content.res.AssetManager;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;

import com.example.zjt.nioserver.thread.ThreadPool;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by zjt on 18-1-31.
 */

public class NIOServer {
    private AssetManager mAssetManager;
    private int ports[];
    private SparseIntArray mSparseArray;
    private SparseArray<String> mBoundaryArray;

    public NIOServer(AssetManager mAssetManager, int ports[]) throws IOException {
        this.mAssetManager = mAssetManager;
        this.ports = ports;
        mSparseArray = new SparseIntArray();
        mBoundaryArray = new SparseArray<>();
        ThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    go();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void go() throws IOException {
        // 创建新的选择器
        Selector selector = Selector.open();

        // 在端口上建立监听，并注册
        for (int port : ports) {
            // 开启一个通道
            ServerSocketChannel ssc = ServerSocketChannel.open();
            // 设置非阻塞模式
            ssc.configureBlocking(false);
            // 获得通道的socket套接字
            ServerSocket ss = ssc.socket();
            // 将套接字绑定到端口
            InetSocketAddress address = new InetSocketAddress(port);
            ss.bind(address);
            // 在选择器中进行注册
            ssc.register(selector, SelectionKey.OP_ACCEPT);
        }

        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        while (true) {
            int num = selector.select();
            if (num <= 0) continue;
            Set selectedKeys = selector.selectedKeys();
            Iterator it = selectedKeys.iterator();
            while (it.hasNext()) {
                SelectionKey key = (SelectionKey) it.next();
                if ((key.readyOps() & SelectionKey.OP_ACCEPT)
                        == SelectionKey.OP_ACCEPT) {
                    // 接受新连接
                    ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                    SocketChannel sc = ssc.accept();
                    sc.configureBlocking(false);

                    // 将新连接注册到选择器
                    sc.register(selector, SelectionKey.OP_READ);
                    it.remove();
                    mSparseArray.put(sc.hashCode(), 0);
                } else if ((key.readyOps() & SelectionKey.OP_READ)
                        == SelectionKey.OP_READ) {
                    // 读取数据
                    try {
                        SocketChannel sc = (SocketChannel) key.channel();
                        byteBuffer.clear();
                        sc.read(byteBuffer);
                        if (byteBuffer.position() > 0) {
                            // 线程池
                            int hash = sc.hashCode(),
                                    indexNum = mSparseArray.get(hash) + 1;
                            mSparseArray.put(hash, indexNum);
//                            ThreadPool.execute(new VueRunnable(mAssetManager, mBoundaryArray, hash, indexNum, sc, byteBuffer.array()));
                            // 不使用多线程 尝试下
                            byte[] array  = byteBuffer.array();
                            VueRunnable vueRunnable = new VueRunnable(mAssetManager, mBoundaryArray, hash, indexNum, sc, array);
                            vueRunnable.run();
                        }
                        it.remove();
                    } catch (Exception e) {
                        Log.d("exception:" + getClass().getName(), "" + e);
                    }
                }
            }
        }
    }
}
