package fr.geii.tours.iut.p4rm;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Arrays;

/**
 * Created by corentin on 02/12/16.
 */

public class TCPClient extends Thread{
    public static String mServerIp = "172.24.1.1";
    public static final int mServerPort = 4444;

    private Socket mSocket;
    private DataOutputStream mDataOutputStream;
    private DataInputStream mDataInputStream;

    private Looper mLooper;
    private Handler mLocalHandler, mRemoteHandler;

    private int mUpdateDelay;
    private Runnable mConnectionInit;
    private Runnable mMessageListener;

    private OnConnectListener mOnConnectListener;

    private byte[] mServerMessage;


    public TCPClient(OnConnectListener connectListener, Handler remoteHandler) {
        mOnConnectListener = connectListener;
        mRemoteHandler = remoteHandler;
        mUpdateDelay = 100;
        mServerMessage = new byte[100];

        mMessageListener = new Runnable() {
            @Override
            public void run() {
                Message msg = Message.obtain();
                try {
                    if(mDataInputStream != null && mSocket.getInputStream().available() > 0) {
                        mDataInputStream.read(mServerMessage);
                        msg.obj = mServerMessage;
                        mRemoteHandler.sendMessage(msg);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                mLocalHandler.postDelayed(this, mUpdateDelay);
            }
        };
        mConnectionInit = new Runnable() {
            @Override
            public void run() {
                try {
                    InetAddress serverAddr = InetAddress.getByName(mServerIp);
                    Log.i("P4RM TCP Client", "Connecting...");
                    mSocket = new Socket();
                    mSocket.setKeepAlive(true);
                    mSocket.setTcpNoDelay(true);
                    mSocket.connect(new InetSocketAddress(serverAddr, mServerPort), 4000);
                    Log.i("P4RM TCP Client", "Connected !");

                    mDataOutputStream = new DataOutputStream(mSocket.getOutputStream());
                    mDataInputStream = new DataInputStream(mSocket.getInputStream());

                    mOnConnectListener.onConnected();

                    mLocalHandler.postDelayed(mMessageListener, mUpdateDelay);

                }catch (Exception e) {
                    Log.e("P4RM TCP Client", "Error : ", e);
                }
            }
        };
    }


    public void startClient(String serverIp){
        mServerIp = serverIp;
        mLocalHandler.post(mConnectionInit);
    }

    public void stopClient(){
        Message msg = Message.obtain();
        byte value[] = {0x3B};//';'
        msg.arg1 = 1;
        msg.obj = value;
        mLocalHandler.sendMessage(msg);

        Runnable close = new Runnable() {
            @Override
            public void run() {

                mLocalHandler.removeCallbacks(mMessageListener);
                mDataOutputStream = null;

                try {
                    mSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        mLocalHandler.postDelayed(close, 100);


    }


    @Override
    public void run() {
        Looper.prepare();
        mLooper = Looper.myLooper();

        try {
            mLocalHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    try {
                        if(mDataOutputStream != null)
                            mDataOutputStream.write((byte[]) msg.obj, 0, msg.arg1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            Looper.loop();

        } catch (Exception e) {
            Log.e("P4RM TCP Client", "Error : ", e);
        }

    }
    public void terminateLooper() {
        if(mSocket!=null) try {
            mSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mLooper.quit();
    }
    public Handler getHandler() {
        return mLocalHandler;
    }

    public boolean isConnected() {
        if(mSocket != null)
            return mSocket.isConnected()&!mSocket.isClosed();
        else return false;
    }

    public interface OnConnectListener {
        void onConnected();
    }
}
