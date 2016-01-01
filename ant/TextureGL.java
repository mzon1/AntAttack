package org.example.ant;

import java.util.Random;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLDebugHelper;
import android.opengl.GLU;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.TextView;

public class TextureGL extends Activity implements OnClickListener {
	
	float x = 0.0f;							//면의 회전각 변수
    float cliff[] = {0.0f, 0.0f, 
    					0.0f, 0.0f, 
    					0.0f, 0.0f, 
    					0.0f, 0.0f};		//각 면의 개미의 올라가는 속도
    
    float touchX, touchY; 					//터치 했을 때 x,y 좌표를 받아 온다.
    int touchAction;
    
    int cnt = 0;							//개미가 정상에 도달했을 때 1씩 증가하는 변수
    
    //int time = 0;							//엄밀히 말하면 보편적인 시간은 아니고 한 프레임이 그려질 때 시간
    
    float speed = 0.05f;					//개미가 올라가는 속도
    
    int delay = 50;							//랜덤 함수를 써서 개미가 없어졌을 때 다시 생길 때 딜레이
    
    int width, height;						//폰 액정의 가로 세로
    float realx, realy;						//게임 내 뷰 공간의 좌표
    
	boolean start[] = 
			{false, false, 					//각 면의 개미가 출발했으면 중복 출발의 막기 위한 변수
			false, false, 
			false, false, 
			false, false};
	
	Position pos[] = {new Position(), new Position(), 	//각 개미는 고유의 뷰 공간내의 좌표를 가짐 그것을 담는 객체
			new Position(), new Position(),
			new Position(), new Position(), 
			new Position(), new Position()};
	
	Random rand = new Random();				//랜덤 함수 생성

    // used to send messages back to this thread
    public final Handler mHandler = new Handler();
    
    // the view to draw the FPS on
    public TextView mFPSText;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAndroidSurface = new BasicGLSurfaceView(this);

        setContentView(R.layout.main);
        FrameLayout v = (FrameLayout) findViewById(R.id.gl_container);
        v.addView(mAndroidSurface);
        
        mFPSText = (TextView)findViewById(R.id.fps_text);
        View button1 = this.findViewById(R.id.Button01);
        button1.setOnClickListener(this);
        View button2 = this.findViewById(R.id.Button02);
        button2.setOnClickListener( this);
    }
    
    public void onClick(View v) {
    	switch (v.getId()) {
		case R.id.Button01:
			x-=10;
			break;
		case R.id.Button02:
			x+=10;
			break;
    	}	
	}

    private class BasicGLSurfaceView extends SurfaceView implements
            SurfaceHolder.Callback {
        SurfaceHolder mAndroidHolder;

         BasicGLSurfaceView(Context context) {
            super(context);
            mAndroidHolder = getHolder();
            mAndroidHolder.addCallback(this);
            mAndroidHolder.setType(SurfaceHolder.SURFACE_TYPE_GPU);

            setFocusable(true);
            // if the following is off, key events will stop coming in
            setFocusableInTouchMode(true);
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                int height) {
        }

        public void surfaceCreated(SurfaceHolder holder) {
            mGLThread = new BasicGLThread(this);
            
            mGLThread.start();
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            if (mGLThread != null) {
                mGLThread.requestStop();
            }
        }

        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
    		// TODO Auto-generated method stub
    		
    		if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT)			//키 다운 이벤트로 오른쪽 키가 눌리면 뷰 공간 좌표가 10도 회전
    			x-=10;											//왼쪽 키가 눌리면 -10도 회전
    		else if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)
    			x+=10;
    		
    		return super.onKeyDown(keyCode, event);
        }
      
        public boolean onTouchEvent(MotionEvent event) {	//터치 이벤트
    		// TODO Auto-generated method stub
			touchX = event.getX();  				//이 부분이 터치했을 때 x,y좌표를 가지고 온다
			touchY = event.getY();  			
			touchAction = event.getAction();  	
			
			realx = -(touchX - (float)width/2)/24.0f;		//뷰 공간 좌표로 바꿔 준다.
			realy = -(touchY - (float)height/2)/28.0f;
			
			for(int i = 0; i < 8; i++)
			{
				if(realy < pos[i].getY()+1 && realy >pos[i].getY()-1 && realx < pos[i].getX()+1 && realx > pos[i].getX()-1 && pos[i].getZ()>2)
	            {
	            	start[i] = false;		//충돌체크 하는 부분
	            	cliff[i] = 0.0f;
	            }
			}
		
    		return true;
    	}
        
        
        
    }
    
    BasicGLThread mGLThread;
    private class BasicGLThread extends Thread {
        SurfaceView sv;
        BasicGLThread(SurfaceView view) {
            sv = view;
        }       
        private boolean animState = true;      
        private boolean mDone = false;
        public void run() {
            initEGL();
            initGL();
            

            TexCubeSmallGLUT cube = new TexCubeSmallGLUT(1);	//나무통 객체 생성
            
            Ant ant = new Ant(1);
            
            
            // create room for two textures
            mGL.glEnable(GL10.GL_TEXTURE_2D);
            
            // use this texture unit
            mGL.glActiveTexture(GL10.GL_TEXTURE0);
            
            int[] textures = new int[1];
            mGL.glGenTextures(1, textures, 0); 
            cube.setTex(mGL, sv.getContext(), textures[0], R.drawable.tree); 

            mGL.glMatrixMode(GL10.GL_MODELVIEW);
            mGL.glLoadIdentity();
            GLU.gluLookAt(mGL, 0, 0, 20f, 0, 0, 0, 0, 1, 0f);		//바라보는 좌표
            while (!mDone) {
            	//time++;				//한번 그려질 때 마다 증가
            	/*if(time%1000 == 0)
            	{
            		speed*=2;		//일정 시간이 지나면 속도증가, 생성 딜레이 감소 
            		if(delay > 0)		
            			delay-=15;
            	}*/
                if (animState) {
                    mGL.glClear(GL10.GL_COLOR_BUFFER_BIT
                            | GL10.GL_DEPTH_BUFFER_BIT);
                    mGL.glPushMatrix();
                    mGL.glRotatef(x, 0f, 1f, 0f);		//화면 전체를 회전 할 때 변수 x는 각도를 의미
                    //mGL.glColor4f(1f, 0f, 0f, 1f);
                    
                    cube.draw(mGL);						//나무통 그림
                    
                    
                    /*for(int i = 0; i < 8; i++)
                    {
                    	 if(start[i]){						//각각의 개미는 일단 
     	                    mGL.glPushMatrix();
     	                    mGL.glRotatef((-90+(45*i)), 0f, 1f, 0f);			//각 면에 알맞게 회전
     	                    mGL.glTranslatef(0, -6+cliff[i], 0);	//수직으로만 증가 현재로는.....
     	                    mGL.glTranslatef(4, 0, 0);				//면에 나올 수 있게 면이 나온 만큼 증가
     	                    ant.draw(mGL);					//개미 그리기
     	                    mGL.glPopMatrix();
     	                    
     	                    pos[i].setPosition((float)Math.cos(((i*45)+90+x)*Math.PI/180)*4, -6 + cliff[i], (float)Math.sin((i*45)+(90+x)*Math.PI/180)*4);
     	                    //위치 저장 
     	                    cliff[i] += speed;	//한번 그려질 때마다 스피드 만큼 올라감
     	                    
     	                    if(cliff[i] > 12)
     	                    {
     	                    	start[i] = false;	//만약 맨 위에 도달하면 현재 일 처리
     	                    	cliff[i] = 0.0f;
     	                    	cnt++;
     	                    }	                    
                         }
                    }*/
                    
                    if(start[0]){						//각각의 개미는 일단 
	                    mGL.glPushMatrix();
	                    mGL.glRotatef(-90, 0f, 1f, 0f);			//각 면에 알맞게 회전
	                    mGL.glTranslatef(0, -6+cliff[0], 0);	//수직으로만 증가 현재로는.....
	                    mGL.glTranslatef(4, 0, 0);				//면에 나올 수 있게 면이 나온 만큼 증가
	                    ant.draw(mGL);					//개미 그리기
	                    mGL.glPopMatrix();
	                    
	                    pos[0].setPosition((float)Math.cos((90+x)*Math.PI/180)*4, -6 + cliff[0], (float)Math.sin((90+x)*Math.PI/180)*4);
	                    //위치 저장 
	                    cliff[0] += speed;	//한번 그려질 때마다 스피드 만큼 올라감
	                    
	                    if(cliff[0] > 12)
	                    {
	                    	start[0] = false;	//만약 맨 위에 도달하면 현재 일 처리
	                    	cliff[0] = 0.0f;
	                    	cnt++;
	                    }	                    
                    }
                    							//나 머지 면의 개미도 똑같은 일 처리
                    if(start[1]){
	                    mGL.glPushMatrix();
	                    mGL.glRotatef(-45, 0f, 1f, 0f);
	                    mGL.glTranslatef(0, -6+cliff[1], 0);
	                    mGL.glTranslatef(4, 0, 0);
	                    ant.draw(mGL);
	                    mGL.glPopMatrix();
	                    
	                    pos[1].setPosition((float)Math.cos((135+x)*Math.PI/180)*4, -6 + cliff[1], (float)Math.sin((135+x)*Math.PI/180)*4);
	                    
	                    cliff[1] += speed;
	                    
	                    if(cliff[1] > 12)
	                    {
	                    	start[1] = false;
	                    	cliff[1] = 0.0f;
	                    	cnt++;
	                    }
                    }
                    
                    if(start[2]){
	                    mGL.glPushMatrix();
	                    mGL.glRotatef(0, 0f, 1f, 0f);
	                    mGL.glTranslatef(0, -6+cliff[2], 0);
	                    mGL.glTranslatef(4, 0, 0);
	                    ant.draw(mGL);
	                    mGL.glPopMatrix();
	                    
	                    pos[2].setPosition((float)Math.cos((180+x)*Math.PI/180)*4, -6 + cliff[2], (float)Math.sin((180+x)*Math.PI/180)*4);
	                    
	                    cliff[2] += speed;
	                    
	                    if(cliff[2] > 12)
	                    {
	                    	start[2] = false;
	                    	cliff[2] = 0.0f;
	                    	cnt++;
	                    }
	                    
                    }
                   
                    if(start[3]){
	                    mGL.glPushMatrix();
	                    mGL.glRotatef(45, 0f, 1f, 0f);
	                    mGL.glTranslatef(0, -6+cliff[3], 0);
	                    mGL.glTranslatef(4, 0, 0);
	                    ant.draw(mGL);
	                    mGL.glPopMatrix();
	                    
	                    pos[3].setPosition((float)Math.cos((225+x)*Math.PI/180)*4, -6 + cliff[3], (float)Math.sin((225+x)*Math.PI/180)*4);
	                    
	                    cliff[3] += speed;
	                    
	                    if(cliff[3] > 12)
	                    {
	                    	start[3] = false;
	                    	cliff[3] = 0.0f;
	                    	cnt++;
	                    }
 	                    
                    }
                    
                    if(start[4]){
	                    mGL.glPushMatrix();
	                    mGL.glRotatef(90, 0f, 1f, 0f);
	                    mGL.glTranslatef(0, -6+cliff[4], 0);
	                    mGL.glTranslatef(4, 0, 0);
	                    ant.draw(mGL);
	                    mGL.glPopMatrix();
	                    
	                    pos[4].setPosition((float)Math.cos((270+x)*Math.PI/180)*4, -6 + cliff[4], (float)Math.sin((270+x)*Math.PI/180)*4);
	                    
	                    cliff[4] += speed;
	                    
	                    if(cliff[4] > 12)
	                    {
	                    	start[4] = false;
	                    	cliff[4] = 0.0f;
	                    	cnt++;
	                    }
                    }
                    
                    if(start[5]){
	                    mGL.glPushMatrix();
	                    mGL.glRotatef(135, 0f, 1f, 0f);
	                    mGL.glTranslatef(0, -6+cliff[5], 0);
	                    mGL.glTranslatef(4, 0, 0);
	                    ant.draw(mGL);
	                    mGL.glPopMatrix();
	                    
	                    pos[5].setPosition((float)Math.cos((315+x)*Math.PI/180)*4, -6 + cliff[5], (float)Math.sin((315+x)*Math.PI/180)*4);
	                    
	                    cliff[5] += speed;
	                    
	                    if(cliff[5] > 12)
	                    {
	                    	start[5] = false;
	                    	cliff[5] = 0.0f;
	                    	cnt++;
	                    }
                    }
                    
                    if(start[6]){
	                    mGL.glPushMatrix();
	                    mGL.glRotatef(180, 0f, 1f, 0f);
	                    mGL.glTranslatef(0, -6+cliff[6], 0);
	                    mGL.glTranslatef(4, 0, 0);
	                    ant.draw(mGL);
	                    mGL.glPopMatrix();
	                    
	                    pos[6].setPosition((float)Math.cos((360+x)*Math.PI/180)*4, -6 + cliff[6], (float)Math.sin((360+x)*Math.PI/180)*4);
	                    
	                    cliff[6] += speed;
	                    
	                    if(cliff[6] > 12)
	                    {
	                    	start[6] = false;
	                    	cliff[6] = 0.0f;
	                    	cnt++;
	                    }
                    }
                    
                    if(start[7]){
	                    mGL.glPushMatrix();
	                    mGL.glRotatef(225, 0f, 1f, 0f);
	                    mGL.glTranslatef(0, -6+cliff[7], 0);
	                    mGL.glTranslatef(4, 0, 0);
	                    ant.draw(mGL);
	                    mGL.glPopMatrix();
	                    
	                    pos[7].setPosition((float)Math.cos((415+x)*Math.PI/180)*4, -6 + cliff[7], (float)Math.sin((415+x)*Math.PI/180)*4);
	                    
	                    cliff[7] += speed;
	                    
	                    if(cliff[7] > 12)
	                    {
	                    	start[7] = false;
	                    	cliff[7] = 0.0f;
	                    	cnt++;
	                    }
                    }
                 
                    
                    mGL.glPopMatrix();
                    
                    mEGL.eglSwapBuffers(mGLDisplay, mGLSurface);

                    mHandler.post(new Runnable() { 
                    	public void run() { 
                    		mFPSText.setText("missant = " + cnt /*+ "  time = " +time*/);
                    	}
                    });
                }
                
                for(int i = 0; i < 8; i++ )
                {
                	if(!start[i])				//개미를 생성해 주는 부분
                    {
                    	if(rand.nextInt(delay) == 1)
                    	{
                    		start[i] = true;
                    	}
                    }
                }               
        	}
        }
        
        public void requestStop() {
            mDone = true;
            try {
                join();
            } catch (InterruptedException e) {
                Log.e("GL", "failed to stop gl thread", e);
            }
            
            cleanupGL();
        }
        
        private void cleanupGL() {
            mEGL.eglMakeCurrent(mGLDisplay, EGL10.EGL_NO_SURFACE,
                    EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
            mEGL.eglDestroySurface(mGLDisplay, mGLSurface);
            mEGL.eglDestroyContext(mGLDisplay, mGLContext);
            mEGL.eglTerminate(mGLDisplay);

            Log.i("GL", "GL Cleaned up");
        }
        
        public void initGL( ) {
        	x = 0.0f;
            width = sv.getWidth();
            height = sv.getHeight();
            mGL.glViewport(0, 0, width, height);
            mGL.glMatrixMode(GL10.GL_PROJECTION);
            mGL.glLoadIdentity();
            float aspect = (float) width/height;
            GLU.gluPerspective(mGL, 45.0f, aspect, 1.0f, 30.0f);
            mGL.glClearColor(0.5f,0.5f,0.5f,0.5f);
            mGL.glClearDepthf(1.0f);
             
             
             // light
            mGL.glEnable(GL10.GL_LIGHTING);
            
            // the first light
            mGL.glEnable(GL10.GL_LIGHT0);
            
            // ambient values
            mGL.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, new float[] {0.1f, 0.1f, 0.1f, 1f}, 0);
            
            // light that reflects in all directions
            mGL.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, new float[] {1f, 1f, 1f, 1f}, 0);
            
            // place it in projection space
            mGL.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, new float[] {10f, 0f, 10f, 1}, 0);
            
            // allow our object colors to create the diffuse/ambient material setting
            mGL.glEnable(GL10.GL_COLOR_MATERIAL);
             
             // some rendering options
             mGL.glShadeModel(GL10.GL_SMOOTH);
             
             mGL.glEnable(GL10.GL_DEPTH_TEST);
             //mGL.glDepthFunc(GL10.GL_LEQUAL);
             mGL.glEnable(GL10.GL_CULL_FACE);
             
             mGL.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT,GL10.GL_NICEST);
             
             // the only way to draw primitives with OpenGL ES
             mGL.glEnableClientState(GL10.GL_VERTEX_ARRAY);

            Log.i("GL", "GL initialized");
        }
        
        public void initEGL() {
            mEGL = (EGL10) GLDebugHelper.wrap(EGLContext.getEGL(),
                    GLDebugHelper.CONFIG_CHECK_GL_ERROR
                            | GLDebugHelper.CONFIG_CHECK_THREAD,  null);
            
            mGLDisplay = mEGL.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

            int[] curGLVersion = new int[2];
            mEGL.eglInitialize(mGLDisplay, curGLVersion);

            Log.i("GL", "GL version = " + curGLVersion[0] + "."
                    + curGLVersion[1]);

            EGLConfig[] configs = new EGLConfig[1];
            int[] num_config = new int[1];
            mEGL.eglChooseConfig(mGLDisplay, mConfigSpec, configs, 1,
                    num_config);
            mGLConfig = configs[0];

            mGLSurface = mEGL.eglCreateWindowSurface(mGLDisplay, mGLConfig, sv
                    .getHolder(), null);

            mGLContext = mEGL.eglCreateContext(mGLDisplay, mGLConfig,
                    EGL10.EGL_NO_CONTEXT, null);

            mEGL.eglMakeCurrent(mGLDisplay, mGLSurface, mGLSurface, mGLContext);
            mGL = (GL10) GLDebugHelper.wrap(mGLContext.getGL(),
                    GLDebugHelper.CONFIG_CHECK_GL_ERROR
                            | GLDebugHelper.CONFIG_CHECK_THREAD
                            | GLDebugHelper.CONFIG_LOG_ARGUMENT_NAMES, null);
        }
        
        // main OpenGL variables
        GL10 mGL;
        EGL10 mEGL;
        EGLDisplay mGLDisplay;
        EGLConfig mGLConfig;
        EGLSurface mGLSurface;
        EGLContext mGLContext;
        int[] mConfigSpec = { EGL10.EGL_RED_SIZE, 5, 
                EGL10.EGL_GREEN_SIZE, 6, EGL10.EGL_BLUE_SIZE, 5, 
                EGL10.EGL_DEPTH_SIZE, 16, EGL10.EGL_NONE };
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Music.play(this, R.raw.game);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Music.stop(this);
    }


    SurfaceView mAndroidSurface;
}