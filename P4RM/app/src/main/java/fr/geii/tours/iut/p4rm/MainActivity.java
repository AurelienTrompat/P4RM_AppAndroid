package fr.geii.tours.iut.p4rm;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.nio.charset.Charset;
import java.util.Arrays;

public class MainActivity extends Activity {

    private JoystickView jv;
    private PaintView pv;
    private TextView tvX, tvY, tvL1, tvL2, tvL3, tvAcc, tvMag;
    private SeekBar sbServo;
    private EditText etIP;
    private Button bConnect, bRD, bRP;
    private ToggleButton tbEnUS, tbEnServo;
    private ImageButton ibST, ibSAT;
    private TabHost th;
    private TabHost.TabSpec ts;
    private boolean mFirstPos = true;

    private TCPClient mNetworkThread;

    private Handler mHandler;

    private TCPClient.OnConnectListener mOnConnectListener = new TCPClient.OnConnectListener() {
        @Override
        public void onConnected() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    byte values[] = {(byte) 'M', (byte) 'M'};
                    String s = th.getCurrentTabTag();
                    bConnect.setText(R.string.disconnect);
                    sendMode(s);
                }
            });
        }
    };

    private View.OnClickListener mBConnectListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(!mNetworkThread.isConnected()) {
                mNetworkThread.startClient(etIP.getText().toString());
            }else {
                mNetworkThread.stopClient();
                bConnect.setText(R.string.connect);
            }

        }
    };

    private TabHost.OnTabChangeListener mOnTabChangedListener = new TabHost.OnTabChangeListener() {
        @Override
        public void onTabChanged(String s) {
            if (mNetworkThread.isConnected()) {
                sendMode(s);
            }
        }
    };

    private View.OnClickListener mRDListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            byte values[] = {(byte) 'D', (byte) 'R'};
            sendData(values);
        }
    };
    private View.OnClickListener mRPListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            byte values[] = {(byte) 'P', (byte) 'R'};
            tvX.setText("0");
            tvY.setText("0");
            pv.clear();
            mFirstPos=true;
            sendData(values);
        }
    };

    private View.OnTouchListener mSTListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            byte values[] = {(byte) 'm', (byte) 'R', (byte) 'T'};
            if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                ibSAT.setEnabled(false);
                sendData(values);
            }
            else if (motionEvent.getAction() == MotionEvent.ACTION_UP)
            {
                values[2]=(byte) 'S';
                ibSAT.setEnabled(true);
                sendData(values);
            }
            return false;
        }
    };

    private View.OnTouchListener mSATListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            byte values[] = {(byte) 'm', (byte) 'R', (byte) 'A'};
            if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                ibST.setEnabled(false);
                sendData(values);
            }
            else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                values[2] = (byte) 'S';
                ibST.setEnabled(true);
                sendData(values);
            }
            return false;
        }
    };

    private ToggleButton.OnCheckedChangeListener mEnUsListener = new ToggleButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            byte values[] = {(byte) 'D', (byte) 'U', 0};
            if(b){
                values[2]=1;
            }
            sendData(values);
        }
    };

    private ToggleButton.OnCheckedChangeListener mEnServoListener = new ToggleButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            byte values[] = {(byte) 'D', (byte) 'S', 0};
            if(b){
                sbServo.setEnabled(true);
                sbServo.setProgress(60);
                values[2]=1;
            }
            else {
                sbServo.setProgress(0);
                sbServo.setEnabled(false);
            }
            sendData(values);
        }
    };

    private SeekBar.OnSeekBarChangeListener mServoListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            byte values[] = {(byte) 'D', (byte) 's', 0};
            if(b) {
                values[2]=(byte)(120-i);
                sendData(values);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };


    JoystickView.OnJoystickMoveListener mJoystickMoveListener = new JoystickView.OnJoystickMoveListener() {
        @Override
        public void onValueChanged(short xAxis, short yAxis, short angle, short power) {
            byte values[] = {(byte) 'm', (byte) 'J', (byte) xAxis, (byte) yAxis};
            sendData(values);
        }
    };




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        th = (TabHost) findViewById(R.id.th);
        jv = (JoystickView) findViewById(R.id.jv);
        pv = (PaintView) findViewById(R.id.pv);

        tvX = (TextView) findViewById(R.id.tvX);
        tvY = (TextView) findViewById(R.id.tvY);
        tvL1 = (TextView) findViewById(R.id.tvL1);
        tvL2 = (TextView) findViewById(R.id.tvL2);
        tvL3 = (TextView) findViewById(R.id.tvL3);
        tvAcc = (TextView) findViewById(R.id.tvAcc);
        tvMag = (TextView) findViewById(R.id.tvMag);

        sbServo = (SeekBar) findViewById(R.id.sbServo);

        etIP = (EditText) findViewById(R.id.etIP);
        bConnect = (Button) findViewById(R.id.bConnect);
        bRD = (Button) findViewById(R.id.bRD);
        bRP = (Button) findViewById(R.id.bRP);
        ibST = (ImageButton) findViewById(R.id.ibST);
        ibSAT = (ImageButton) findViewById(R.id.ibSAT);

        tbEnUS = (ToggleButton) findViewById(R.id.tbEnUS);
        tbEnServo = (ToggleButton) findViewById(R.id.tbEnServo);

        th.setOnTabChangedListener(mOnTabChangedListener);
        jv.setOnJoystickMoveListener(mJoystickMoveListener);
        bConnect.setOnClickListener(mBConnectListener);
        bRD.setOnClickListener(mRDListener);
        bRP.setOnClickListener(mRPListener);
        ibST.setOnTouchListener(mSTListener);
        ibSAT.setOnTouchListener(mSATListener);
        tbEnUS.setOnCheckedChangeListener(mEnUsListener);
        tbEnServo.setOnCheckedChangeListener(mEnServoListener);
        sbServo.setOnSeekBarChangeListener(mServoListener);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                //System.out.println(Byte.toString(((byte[])msg.obj)[0]));
                decodeData((byte[]) msg.obj);
            }
        };

        mNetworkThread = new TCPClient(mOnConnectListener, mHandler);

        th.setup();

        View v1 = LayoutInflater.from(th.getContext()).inflate(R.layout.tabs_bg, null);
        TextView tv1 = (TextView) v1.findViewById(R.id.tabsText);
        tv1.setText(R.string.tabManuel);

        View v2 = LayoutInflater.from(th.getContext()).inflate(R.layout.tabs_bg, null);
        TextView tv2 = (TextView) v2.findViewById(R.id.tabsText);
        tv2.setText(R.string.tabAuto);

        View v3 = LayoutInflater.from(th.getContext()).inflate(R.layout.tabs_bg, null);
        TextView tv3 = (TextView) v3.findViewById(R.id.tabsText);
        tv3.setText(R.string.tabDebug);

        ts = th.newTabSpec("Manuel");
        ts.setContent(R.id.tabManuel);
        ts.setIndicator(v1);
        th.addTab(ts);

        ts = th.newTabSpec("Auto");
        ts.setContent(R.id.tabAuto);
        ts.setIndicator(v2);
        th.addTab(ts);

        ts = th.newTabSpec("Debug");
        ts.setContent(R.id.tabDebug);
        ts.setIndicator(v3);
        th.addTab(ts);

        sbServo.setEnabled(false);

    }

    @Override
    protected void onStart() {
        super.onStart();
        mNetworkThread.start();
        bConnect.setText(R.string.connect);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mNetworkThread.stopClient();
        mNetworkThread.terminateLooper();
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        mNetworkThread = new TCPClient(mOnConnectListener, mHandler);
    }

    private void sendData(byte[] values) {
        Message msg = Message.obtain();
        msg.arg1 = values.length;
        msg.obj = values;
        mNetworkThread.getHandler().sendMessage(msg);
    }

    private void decodeData(byte[] data) {
        System.out.println(Arrays.toString(data));
        switch(data[0])
        {
            case 0x3B: {//';'
                mNetworkThread.stopClient();
                bConnect.setText(R.string.connect);
                break;
            }
            case 0x50 : {//'P'
                if(data[1] == 0x50) {//'P'
                    updatePos(Arrays.copyOfRange(data, 2, 6));

                }
                break;
            }
            case 0x44: {//'D'
                switch(data[1])
                {
                    case 0x4C: {//'L'
                        handleLaser(Arrays.copyOfRange(data, 2, 5));
                        break;
                    }
                    case 0x41: {//'A'
                        handleAccel(Arrays.copyOfRange(data, 2, 6));
                        break;
                    }
                    case 0x4D: {//'M'
                        handleMagn(Arrays.copyOfRange(data, 2, 4));
                        break;
                    }
                    case 0x55: {//'U'
                        System.out.println("U");
                        tbEnUS.setOnCheckedChangeListener(null);
                        if(data[2]>0)
                            tbEnUS.setChecked(true);
                        else
                            tbEnUS.setChecked(false);
                        tbEnUS.setOnCheckedChangeListener(mEnUsListener);
                        break;
                    }
                    case 0x53: {//'S'
                        tbEnServo.setOnCheckedChangeListener(null);
                        if(data[2]==0) {
                            tbEnServo.setChecked(false);
                            sbServo.setProgress(0);
                            sbServo.setEnabled(false);
                        }
                        tbEnServo.setOnCheckedChangeListener(mEnServoListener);
                        break;
                    }
                }
                break;
            }

        }
    }
    private void updatePos(byte[] data) {
        System.out.println(data[0]);
        System.out.println(data[1]);
        System.out.println(data[2]);
        System.out.println(data[3]);

        int x = ((int)data[0]<<8) | data[1] & 0xFF;
        int y = ((int)data[2]<<8) | data[3] & 0xFF;

        tvX.setText(Integer.toString(x));
        tvY.setText(Integer.toString(y));

        System.out.println(x);
        System.out.println(y);

        if(mFirstPos) {
            pv.moveTo(x, y);
            mFirstPos=false;
        }
        else
            pv.drawTo(x, y);


    }
    private void handleLaser(byte[] data) {
        int x = ((int)data[1]<<8) | data[2] & 0xFF;
        String s = Integer.toString(x)+" cm";
        switch(data[0])
        {
            case 0x47: {
                tvL1.setText(s);
                break;
            }
            case 0x41: {
                tvL2.setText(s);
                break;
            }
            case 0x44: {
                tvL3.setText(s);
                break;
            }
        }

    }

    private void handleAccel(byte[] data) {
        String s = new String(data, Charset.forName("US-ASCII"));
        s+=" g";
        tvAcc.setText(s);
    }


    private void handleMagn(byte[] data) {
        int x = ((int)data[0]<<8) | data[1] & 0xFF;
        tvMag.setText(Integer.toString(x)+"°");
    }

    private void sendMode(String s) {
        byte values[] = {(byte) 'M', (byte) 'M'};
        if (s == "Auto") {
            values[1] = (byte) 'A';
        } else if (s == "Debug")
            values[1] = (byte) 'D';
        sendData(values);
    }

}
