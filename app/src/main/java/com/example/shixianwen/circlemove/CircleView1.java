package com.example.shixianwen.circlemove;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.*;
import android.graphics.Paint;
import android.support.v4.view.VelocityTrackerCompat;
import android.view.*;
import android.view.View;
import android.util.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by shixianwen on 2016/1/15.
 */
public class CircleView1 extends View {
    //measure related parameters are defined below
    private float currentX = 50;
    private float currentY = 50;
    private VelocityTracker mVelocityTracker = null;

    private float xVelocity = 0;
    private float yVelocity = 0;
    //current system time in milisecond
    private long time = System.currentTimeMillis();
    private Paint p = new Paint();
    //bound of the screen
    private Rect rect = null;
    private int parentWidth = 0;
    private int parentHeight = 0;
    private final int redPointSize = 15;
    //bounds for the init rect
    private Rect initrect = null;

    //decide whether the user touch the red point at the begining
    private boolean rightLocation = false;

    //usb transmition related parameters are defined below
    public static final String TAG = "USBCommActivity";
    public static final int TIMEOUT = 10;
    private String connectionStatus = null;
    private final String sendMsg = "Hello From Server";
    private String dataMsg = "";
    private Handler mHandler = null;
    private ServerSocket server = null;
    private Socket client = null;
    private ObjectOutputStream out;
    public static InputStream nis = null;


    public void setHandeler(Handler mHandler) {
        this.mHandler = mHandler;
    }

    public CircleView1(Context context) {
        super(context);

    }


    public CircleView1(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void init() {
       // rightLocation = false;
        //    begin with a new thread to diliver the important test info.
        new Thread(initializeConnection).start();

//        String msg = "Attempting to connect";
//        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
//        // cannot make the handler here! should be created in main queue
        //mHandler = new Handler();
    }

    /**
     * Thread to initialize Socket connection
     */
    private final Runnable initializeConnection = new Thread() {
        @Override
        public void run() {
            // initialize server socket
            try {
                server = new ServerSocket(38300);
                server.setSoTimeout(CircleView1.TIMEOUT * 1000);

                //attempt to accept a connection
                client = server.accept();

                out = new ObjectOutputStream(client.getOutputStream());
                CircleView1.nis = client.getInputStream();
                try {
                    out.writeObject(sendMsg);
                    System.out.println("client >" + sendMsg);

                    byte[] bytes = new byte[1024];
                    int numRead = 0;
                    while ((numRead = CircleView1.nis.read(bytes)) >= 0) {
                       // connectionStatus = new String(bytes, 0, numRead);
                        connectionStatus = "Connected";
                        mHandler.post(showConnectionStatus);
                    }
                } catch (IOException ioException) {
                    Log.e(CircleView1.TAG, "" + ioException);
                }
            } catch (SocketTimeoutException e) {
                connectionStatus = "Connection has timed out! Please try again";
                mHandler.post(showConnectionStatus);
            } catch (IOException e) {
                Log.e(CircleView1.TAG, "" + e);
            }

        }
    };

    /**
     * Runnable to show pop-up for connection status
     */
    private final Runnable showConnectionStatus = new Runnable() {
        //----------------------------------------

        /**
         * @see java.lang.Runnable#run()
         */
        //----------------------------------------
        @Override
        public void run() {
            Toast.makeText(getContext(), connectionStatus, Toast.LENGTH_SHORT).show();
        }
    };


    //get the screen width and heights and set the start point
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        System.out.println("parentWidth = " + parentWidth);
        parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        System.out.println("parentHeight = " + parentHeight);
        currentX = parentWidth / 2;
        currentY = parentHeight / 2;
        rect = new Rect(0, 0, parentWidth, parentHeight);
        int initrectxleftconer = parentWidth / 2 - 3 * redPointSize;
        int initrectyleftconer = parentHeight / 2 - 3 * redPointSize;
        int inirectxrightconer = parentWidth / 2 + 3 * redPointSize;
        int inirectyrightconer = parentHeight / 2 + 3 * redPointSize;
        initrect = new Rect(initrectxleftconer, initrectyleftconer, inirectxrightconer, inirectyrightconer);
    }

    //draw a red circle
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        p.setColor(Color.GREEN);
        canvas.drawRect(initrect, p);
        //设置画笔的颜色
        p.setColor(Color.RED);
        //绘制一个小球
        canvas.drawCircle(currentX, currentY, redPointSize, p);
    }


    //motion dectection
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //修改currentX、currentY两个属性
        int index = event.getActionIndex();
        currentX = event.getX();
        currentY = event.getY();
        int action = event.getActionMasked();
        int pointerId = event.getPointerId(index);
        System.out.println("currentX " + currentX);
        System.out.println("currentY " + currentY);
//        orientation = event.getOrientation();
//        System.out.println("orientation=:" + orientation);

        // only works for the first time. create a small rect around the middle point
        //if user touched this place begin to do the animation
        if (!rightLocation && initrect.contains((int) currentX, (int) currentY)) {
            rightLocation = true;
        }

        //通知当前组件重绘自己
        //System.out.println("rightlocation=" + rightLocation);
        //System.out.println("x" + initrect.centerX() + "y" + initrect.centerY());
        if (rightLocation) {
            System.out.println("I can repaint myself from rightlocation");
            if ((rect != null) && (rect.contains((int) currentX, (int) currentY))) {
                System.out.println("I am repaint myself");
                invalidate();
            }
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    if (mVelocityTracker == null) {
                        // Retrieve a new VelocityTracker object to watch the velocity of a motion.
                        mVelocityTracker = VelocityTracker.obtain();
                    } else {
                        // Reset the velocity tracker back to its initial state.
                        mVelocityTracker.clear();
                    }
                    // Add a user's movement to the tracker.
                    mVelocityTracker.addMovement(event);
                    break;
                case MotionEvent.ACTION_MOVE:
                    if(mVelocityTracker == null){
                        mVelocityTracker = VelocityTracker.obtain();
                    }
                    mVelocityTracker.addMovement(event);
                    // When you want to determine the velocity, call
                    // computeCurrentVelocity(). Then call getXVelocity()
                    // and getYVelocity() to retrieve the velocity for each pointer ID.
                    mVelocityTracker.computeCurrentVelocity(1000);
                    // Log velocity of pixels per second
                    // Best practice to use VelocityTrackerCompat where possible.
                    xVelocity = VelocityTrackerCompat.getXVelocity(mVelocityTracker,
                            pointerId);
                    xVelocity = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, xVelocity, getResources().getDisplayMetrics());

                    time = System.currentTimeMillis();
                    System.out.println("time=" + time);
                    System.out.println("X velocity: " + xVelocity);
                    yVelocity = VelocityTrackerCompat.getYVelocity(mVelocityTracker, pointerId);

                    yVelocity = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, yVelocity, getResources().getDisplayMetrics());
                    System.out.println("Y velocity: " + yVelocity);
                    dataMsg = "currentX:" + currentX + " currentY:" + currentY + " xVelocity:" + xVelocity + " yVelocity:" + yVelocity + " currentTime:" + time + " parentWidth:" + parentWidth + " parentHeight:" + parentHeight;


                   Thread thread1 =  new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (dataMsg != null && out != null && !client.isClosed() && !server.isClosed()) {
                                    out.writeObject(dataMsg);
                                    System.out.println("client >" + dataMsg);
                                }
                                //每次屏幕方向一变动，socket就挂掉了。
                                else if (out == null) {
                                    System.out.println("forced to go to initial out == null");
                                    if (CircleView1.nis != null)
                                        CircleView1.nis.close();

                                    if (server != null) {
                                        server.close();
                                    }
                                    init();
                                }
                                else if(server.isClosed()|| client.isClosed()){
                                    onclose();
                                    init();
                                }

                            } catch (IOException e) {
                                onclose();
                                System.out.println("exception handler");
                                connectionStatus = "connection lost attempting to connect";
                                mHandler.post(showConnectionStatus);
                                init();
                                e.printStackTrace();
                            }
                        }
                    });
                    thread1.setPriority(10);
                    thread1.start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // Return a VelocityTracker object back to be re-used by others.
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                    break;
            }
        }

        return true;
    }

    public void onclose() {

        //mHandler = null;
        if (server != null)
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        if (client != null)
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (nis != null) {
            try {
                nis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
