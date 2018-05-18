package com.vbea.java21.audio;

import android.app.Service;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Handler;
import android.os.Message;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.widget.RemoteViews;
import android.media.AudioManager;

import android.content.Intent;
import com.vbea.java21.More;
import com.vbea.java21.classes.Common;
import com.vbea.java21.classes.ExceptionHandler;
import com.vbea.java21.R;
import com.vbea.java21.classes.Util;

public class AudioService extends Service
{
	private AudioManager audioManager;
	private NotificationManager notiManager;
	//private AudioPlayListener listener;
	private boolean isPlaying = false, isPlay = false, isReceived = false, isPaused = false;
	public boolean loop = false, order = true;
	public int what = -1, max = 0,current = 0;//, result = 0;
	public Music music;
	private MusicThread mThread;
	private String[] strmusic;
	public String mid = "";
	private final String PLAY_CLOSE = "action.close", PLAY_NEXT = "action.next", ALARM_ALERT = "com.android.deskclock.ALARM_ALERT";
	
	private final IBinder binder = new AudioBinder();
	
	@Override
	public IBinder onBind(Intent p1)
	{
		return binder;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		notiManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onRebind(Intent intent)
	{
		super.onRebind(intent);
	}

	public void registerReceiver()
	{
		if (isReceived)
			unregisterReceiver(receiver);
		IntentFilter filter = new IntentFilter();
		filter.addAction(PLAY_CLOSE);
		filter.addAction(PLAY_NEXT);
		filter.addAction(ALARM_ALERT);
		filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
		registerReceiver(receiver, filter);
		isReceived = true;
	}

	@Override
	public void unregisterReceiver(BroadcastReceiver receiver)
	{
		super.unregisterReceiver(receiver);
		isReceived = false;
	}

	@Override
	public void onDestroy()
	{
		Stop();
		super.onDestroy();
	}
	
	//发送通知
	public void createNotification()
	{
		if (music == null)
			return;
		RemoteViews view = new RemoteViews(getPackageName(), R.layout.music_noti);
		Notification.Builder builder = new Notification.Builder(this);
		builder.setSmallIcon(R.mipmap.ic_launcher);
		builder.setOngoing(true).setContent(view);
		builder.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, More.class), PendingIntent.FLAG_CANCEL_CURRENT));
		view.setTextViewText(R.id.noti_text, getMusicName());
		view.setOnClickPendingIntent(R.id.noti_close, PendingIntent.getBroadcast(this, 1, new Intent(PLAY_CLOSE), PendingIntent.FLAG_UPDATE_CURRENT));
		view.setOnClickPendingIntent(R.id.noti_next, PendingIntent.getBroadcast(this, 2, new Intent(PLAY_NEXT), PendingIntent.FLAG_UPDATE_CURRENT));
		notiManager.notify(1, builder.build());
		registerReceiver();
	}
	
	/*public void setOnAudioPlayListener(AudioPlayListener lis)
	{
		listener = lis;
	}*/
	
	//播放音乐
	public void play(int mu)
	{
		if (Common.SOUND == null)
			return;
		if (mThread != null)
		{
			if (what != mu || !isPlaying)
			{
				what = mu;
				music = Common.SOUND.getMusic(what);
				mThread.init();
			}
			else
				Stop();
		}
		else
		{
			what = mu;
			music = Common.SOUND.getMusic(what);
			if (music != null)
			{
				mThread = new MusicThread();
				mThread.start();
				audioManager.requestAudioFocus(afListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
			}
		}
	}
	
	public void Stop()
	{
		isPlay = isPlaying = false;
		current = max = 0;
		what = -1;
		mThread = null;
		notiManager.cancelAll();
		if (isReceived)
			unregisterReceiver(receiver);
		audioManager.abandonAudioFocus(afListener);
	}
	
	public void Pause()
	{
		isPlay = isPlaying = false;
		isPaused = true;
		mThread = null;
	}
	
	public boolean isPlay()
	{
		return isPlaying;
	}
	
	public boolean isPause()
	{
		return isPaused;
	}
	
	public void setLoop(boolean _loop)
	{
		loop = _loop;
		if (order && loop)
			order = false;
	}
	
	public void setOrder(boolean _order)
	{
		order = _order;
		if (loop && order)
			loop = false;
	}
	
	public String getMusicName()
	{
		if (music != null)
			return music.getName();
		return "";
	}
	
	public boolean playNext()
	{
		what+=1;
		music = Common.SOUND.getMusic(what);
		return music != null;
	}
	
	private BroadcastReceiver receiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			switch (intent.getAction())
			{
				case PLAY_NEXT:
					playNext();
					mThread.init();
					break;
				case PLAY_CLOSE:
					Stop();
					break;
				case ALARM_ALERT:
					Stop();
					break;
				case AudioManager.ACTION_AUDIO_BECOMING_NOISY:
					Stop();
					break;
			}
		}
	};
	
	private AudioManager.OnAudioFocusChangeListener afListener = new AudioManager.OnAudioFocusChangeListener()
	{
		@Override
		public void onAudioFocusChange(int code)
		{
			switch (code)
			{
				case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
					//pause
					Pause();
					break;
				case AudioManager.AUDIOFOCUS_GAIN:
					//resume
					break;
				case AudioManager.AUDIOFOCUS_LOSS:
					Stop();
					break;
			}
		}
	};
	
	public class AudioBinder extends Binder
	{
		public AudioService getService()
		{
			return AudioService.this;
		}
	}
	
	class MusicThread extends Thread //implements Runnable
	{
		long longs = 0;
		long shortx = 0;
		
		public void init()
		{
			if (music != null)
			{
				strmusic = music.getKeys();
				longs = music.max;
				shortx = music.min;
				zero();
				isPlay = max > 0;
			}
			else
				stoped();
		}
		
		@Override
		public void run()
		{
			try
			{
				init();
				isPlaying = isPlay;
				sleep(500);
				while (isPlay)
				{
					if (isPlay)
					{
						synchronized (this)
						{
							mid = strmusic[current].trim();
							if (mid.indexOf("_") > 0)
							{
								String[] s = mid.split("_");
								for (String _mid : s)
								{
									if (!isPlaying)
										break;
									mid = _mid.trim();
									if (play(false))
										Thread.sleep(shortx/2);
									else
										Thread.sleep(shortx/4);
								}
								sleep(shortx/2);
								current++;
							}
							else
							{
								if (play(true))
									Thread.sleep(longs);
								else
									Thread.sleep(shortx);
							}
						}
					}
					if (current == max)
					{
						mid = "";
						if (isPlay)
						{
							if (loop)
							{
								current = 0;
								sleep(1000);
							}
							else if (order)
							{
								sleep(500);
								if (playNext())
									init();
								else
									isPlay = false;
							}
							else
								isPlay = false;
						}
					}
				}
				isPlaying = false;
			}
			catch (Exception e)
			{
				stoped();
				ExceptionHandler.log("AudioServise.Thread(" + current + ")", e.toString());
			}
			finally
			{
				stoped();
			}
		}

		public void stoped()
		{
			isPlay = isPlaying = false;
			what = -1;
			zero();
			Stop();
		}
		
		private void zero()
		{
			if (strmusic != null)
			{
				max = strmusic.length;
				createNotification();
			}
			else
				max = 0;
			current = 0;
			mid = "";
		}
		
		public void runpl(final String...music)
		{
			try
			{
				Common.SOUND.play(music);
			}
			catch (Exception e)
			{
				isPlay = false;
			}
		}
		
		public boolean play(boolean nor)
		{
			if (mid.indexOf("-") > 0)
				runpl(mid.split("-"));
			else
				runpl(mid);
			if (nor)
				current++;
			if (mid.equals("-"))
			{
				mid = "";
				return true;
			}
			else if (mid.equals("_"))
			{
				mid = "";
				return false;
			}
			else
				return Character.isUpperCase(mid.charAt(0));
		}
	}
}
