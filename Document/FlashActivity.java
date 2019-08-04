package com.binxin.zdapp

import android.app.Activity;

public class FlashActivity extends Activity
{
	static private Camera camera = null;  
	private Parameters parameters = null; 
    
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_flash);//������ֻ��������ť�������͹ر�
		ctx = this;
	}
	//������
	public void start(View v)
	{
		handler.post(startThread);
		handler.post(closeThread);
	}
	//�ر�
	public void close(View v)
	{
		handler.removeCallbacks(startThread);
		handler.removeCallbacks(closeThread);
		flashclose();
		camera.stopPreview();
		camera.release();
		camera=null;
	}
	private void flashopen()
	{
		if(camera==null)
		{
			camera = Camera.open(); 
		}
		parameters = camera.getParameters();  
		parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
		camera.setParameters(parameters);
		camera.startPreview();
	}  
	private void flashclose()
	{
		if(camera==null)
		{
			camera = Camera.open(); 
		}
		parameters = camera.getParameters();  
		parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
		camera.setParameters(parameters);
	}
	Runnable startThread = new Runnable()
	{  
		//��Ҫִ�еĲ���д���̶߳����run��������  
		public void run()
		{  
			System.out.println("updateThread");  
			flashopen();
			try
			{
				Thread.sleep(100);
				flashclose();
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
			handler.post(startThread);
		}
	}; 
	Runnable closeThread = new Runnable()
	{  
		//��Ҫִ�еĲ���д���̶߳����run��������  
		public void run()
		{  
			System.out.println("updateThread");  
			flashclose();	
			try
			{
				Thread.sleep(100);
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
			handler.post(closeThread);
		}
	};
	Handler handler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			super.handleMessage(msg);
		}
	};
}