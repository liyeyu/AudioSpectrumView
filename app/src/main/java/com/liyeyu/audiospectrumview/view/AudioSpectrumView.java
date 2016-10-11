package com.liyeyu.audiospectrumview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.liyeyu.audiospectrumview.R;

import java.util.Random;

/**
 * Created by Liyeyu on 2016/10/11.
 */

public class AudioSpectrumView extends SurfaceView implements SurfaceHolder.Callback {
    private int W;
    private int H;
    private int columnW;
    private int SpecW = 20;
    private int SpecTopM = 20;
    private int columnCount = 15;
    private int rectBlockH = 10;
    private boolean canDraw = true;
    private boolean isShowBlock = true;
    private int point_X[];
    private int point_Y[];
    private int point_x[];
    private int point_y[];
    private Rect rect_spec[];
    private Rect rect_block[];
    private SurfaceHolder mHolder;
    private Random mRandom = new Random();
    private Paint mPaint;
    private int specColor;
    private int bgColor;
    private int interval = 150;
    private Canvas mCanvas;

    public AudioSpectrumView(Context context) {
        super(context);
    }

    public AudioSpectrumView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AudioSpectrumView);
        bgColor = a.getColor(R.styleable.AudioSpectrumView_spec_bg,Color.TRANSPARENT);
        specColor = a.getColor(R.styleable.AudioSpectrumView_spec_color,Color.WHITE);
        SpecW = a.getDimensionPixelOffset(R.styleable.AudioSpectrumView_spec_w,SpecW);
        SpecTopM = a.getDimensionPixelOffset(R.styleable.AudioSpectrumView_spec_top_m,SpecTopM);
        rectBlockH = a.getDimensionPixelOffset(R.styleable.AudioSpectrumView_spec_block_h,rectBlockH);
        columnCount = a.getInt(R.styleable.AudioSpectrumView_spec_count,columnCount);
        isShowBlock = a.getBoolean(R.styleable.AudioSpectrumView_spec_block,isShowBlock);
        interval = a.getInt(R.styleable.AudioSpectrumView_spec_interval,interval);
        mHolder = getHolder();
        mHolder.addCallback(this);
        //设置背景透明
        setZOrderOnTop(true);
        mHolder.setFormat(PixelFormat.TRANSLUCENT);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        canDraw = true;
        initLoop();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        W = width;
        H = height;
        columnW = W/(columnCount+1);
        //处理不合理的宽度
        if(columnW<=SpecW){
            SpecW = columnW - 10;
        }
        initPoint();
        initLoop();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        canDraw = false;
    }

    private void initLoop(){
        Thread mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (canDraw){
                    try {
                        Thread.sleep(interval);
                        initRectSpec();
                        draw();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        mThread.start();
    }

    private void draw(){
        try {
            mCanvas = mHolder.lockCanvas();
            if(mCanvas!=null){
                //清屏
                mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                mCanvas.drawColor(bgColor);
                for (int i = 0; i < columnCount; i++) {
                    mCanvas.drawRect(rect_spec[i],mPaint);
                    if (isShowBlock){
                        mCanvas.drawRect(rect_block[i],mPaint);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(mCanvas!=null){
                mHolder.unlockCanvasAndPost(mCanvas);
            }
        }
    }

    private void initPoint(){
        point_X = new int[columnCount];
        point_Y = new int[columnCount];
        point_x = new int[columnCount];
        point_y = new int[columnCount];
        rect_spec = new Rect[columnCount];
        rect_block = new Rect[columnCount];
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(specColor);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.FILL);
    }

    private void initRectSpec() {
        for (int i = 0; i < columnCount; i++) {
            point_X[i] = columnW*(i+1);
            point_Y[i] = mRandom.nextInt(H-SpecTopM);
            rect_spec[i] = new Rect(point_X[i] - SpecW/2,H - point_Y[i],point_X[i] + SpecW/2,H);
        }
        if (isShowBlock){
            initRectBlock();
        }
    }

    private void initRectBlock() {
        for (int i = 0; i < columnCount; i++) {
            point_x[i] = point_X[i];
            //当前的频谱条柱高度比之前小，下降趋势，顶部方块逐步下降
            if(point_y[i]>point_Y[i]-rectBlockH){
                point_y[i] = point_y[i]-rectBlockH;
            }else if(point_y[i]<point_Y[i]-rectBlockH){
                point_y[i] = point_Y[i] + rectBlockH;
            }else{
                point_y[i] = point_Y[i];
            }
            rect_block[i] = new Rect(point_x[i] - SpecW/2,H - point_y[i],point_x[i] + SpecW/2,H - point_y[i] + rectBlockH);
        }
    }

    public boolean isCanDraw() {
        return canDraw;
    }

    public void setCanDraw(boolean canDraw) {
        this.canDraw = canDraw;
    }

    public boolean isShowBlock() {
        return isShowBlock;
    }

    public void setShowBlock(boolean showBlock) {
        isShowBlock = showBlock;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

}
