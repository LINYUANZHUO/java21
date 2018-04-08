package com.vbea.java21.classes;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.text.SimpleDateFormat;

import android.app.Activity;
import android.os.Build;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.util.Pair;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import com.vbea.java21.R;
import com.vbea.java21.ActivityManager;
import com.vbea.java21.audio.SoundLoad;
import com.vbea.java21.audio.AudioService;
import com.vbea.java21.data.Users;
import com.vbea.java21.data.Tips;
import com.vbea.java21.data.Copys;
import com.vbea.java21.MyThemes;
import com.vbea.secret.*;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobWrapper;
import cn.bmob.v3.datatype.BmobDate;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.DownloadFileListener;
import cn.bmob.v3.exception.BmobException;
import com.tencent.tauth.Tencent;

public class Common
{
	private static boolean IsRun = false;//是否运行
	public static int APP_THEME_ID = -1;//主题
	public static int APP_BACK_ID = 0;//背景图片
	public static int VERSION_CODE = 0;//版本号
	public static int AUTO_LOGIN_MODE = 0;//自动登录类型，1为普通，2为QQ
	public static String SDATE;//激活凭证
	public static String KEY;//密钥
	public static String SID;//激活时间
	public static String USERID;//用户名
	public static String USERPASS;//加密密码
	public static boolean IS_ACTIVE = false;//是否注册
	public static boolean NO_ADV = false;//去广告
	public static boolean WEL_ADV = true;//欢迎页广告
	//public static boolean EYESHIELD = false;//护眼模式
	//public static boolean PRO = false;//专业版
	public static SoundLoad SOUND = null;//音乐池
	public static boolean AUDIO = false;//是否显示音乐
	public static boolean MUSIC = true;//是否开启音乐
	public static boolean TIPS = true;//是否开启消息通知
	public final static boolean HULUXIA = false;//是否葫芦侠特别版
	public static Users mUser;
	public static boolean IsChangeICON = false;
	public static int AUDIO_STUDY_STATE = 0;
	public static int JAVA_TEXT_SIZE = 2;
	public static AudioService audioService;
	private static final String defaultKey = "JAVA8-APP-KEY21-APK-VBEST";
	public static String FileProvider;
	public static final String LocalPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/ZDApp/";
	public static List<Tips> mTips = null;
	public static List<String> READ_Android, READ_J2EE, READ_AndroidAdvance;
	public static InboxManager myInbox;
	private static long lastTipsTime;
	private static Copys copyMsg;
	public static String OldSerialNo;
	public static void start(Context context)
	{
		startBmob(context);
		if (IsRun)
			return;
		IsRun = true;
		SharedPreferences spf = context.getSharedPreferences("java21", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = spf.edit();
		init(spf);
		FileProvider = context.getApplicationContext().getPackageName() + ".fileprovider";
		if (spf.getString("key", "").trim().equals("") || spf.getString("date","").trim().equals(""))
		{
			IS_ACTIVE = false;
			editor.putBoolean("app", false);
		}
		if (IS_ACTIVE && regist(KEY))
		{
			editor.putBoolean("app", true);
			editor.putBoolean("active", true);
		}
		else
		{
			IS_ACTIVE = false;
			editor.putBoolean("app", false);
			editor.putBoolean("active", false);
			editor.putString("key", defaultKey);
		}
		if (getDrawerBack() == null)
			editor.putInt("back", 0);
		editor.commit();
		init(spf);
		SocialShare.onStart(context);
		if (isNet(context))
			getTips();
	}
	
	public static void reStart(Context context)
	{
		IsRun = false;
		start(context);
	}
	
	private static void startBmob(Context context)
	{
		if (BmobWrapper.getInstance() == null)
			Bmob.initialize(context, "1aa46b02605279e1a84935073af9fc82");
	}
	
	public static void update(Context context, boolean check)
	{
		int code = 0;
		SharedPreferences spf = context.getSharedPreferences("java21", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = spf.edit();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		code = context.getResources().getInteger(R.integer.versionCode);
		if (check)
			editor.putInt("check", code);
		else
		{
			String codes = "";
			try
			{
				k dec = new k();
				codes = dec.encrypt(format.format(new Date()));
				File cathe = new File(LocalPath + File.separator + ".nomedia");
				if (!cathe.exists())
					cathe.createNewFile();
			}
			catch (Exception e)
			{
			}
			editor.putInt("check", code);
			editor.putString("checkCode", codes);
		}
		editor.putBoolean("chartip", false);
		editor.putBoolean("androidtip", false);
		editor.commit();
		init(spf);
	}
	
	public static String getSettingJson(SettingUtil utils)
	{
		utils.addSettings(SettingUtil.SET_THEME, APP_THEME_ID);
		utils.addSettings(SettingUtil.SET_BACKIMG, APP_BACK_ID);
		utils.addSettings(SettingUtil.SET_FONTSIZE, JAVA_TEXT_SIZE);
		return utils.getJsonString();
	}
	
	public static boolean checkUpdateSetting(Context context)
	{
		if (!mUser.serialNo.equals(Util.getSerialNo(context)))
		{
			try
			{
				SettingUtil utils = new SettingUtil();
				if (!Util.isNullOrEmpty(mUser.settings))
				{
					utils.synaxSetting(mUser.settings);
					APP_BACK_ID = utils.getIntValue(SettingUtil.SET_BACKIMG);
					APP_THEME_ID = utils.getIntValue(SettingUtil.SET_THEME);
					JAVA_TEXT_SIZE = utils.getIntValue(SettingUtil.SET_FONTSIZE);
					return true;
				}
			}
			catch (Exception e)
			{
				ExceptionHandler.log("checkUpdateSetting", e);
			}
		}
		return false;
	}
	
	public static void updateUserLogin(Context context)
	{
		OldSerialNo = mUser.serialNo;
		Date now = new Date();
		if (mUser.lastLogin != null)
		{
			mUser.device = Util.getDeviceId(context);
			mUser.serialNo = Util.getSerialNo(context);
			Date loginDate = new Date(BmobDate.getTimeStamp(mUser.lastLogin.getDate()));
			if (loginDate.getDate() != now.getDate())
			{
				if (now.getYear() >= loginDate.getYear() || now.getMonth() >= loginDate.getMonth())
				{
					if (mUser.dated != null)
						mUser.dated += 1;
					else
						mUser.dated = 1;
				}
			}
			mUser.lastLogin = new BmobDate(now);
			updateUser();
		}
		else
		{
			mUser.lastLogin = new BmobDate(now);
			mUser.dated = 1;
			updateUser();
		}
	}
	
	public static void updateUser()
	{
		try
		{
			if (mUser == null)
				return;
			Users user = new Users();
			user.setObjectId(mUser.getObjectId());
			user.psd = mUser.psd;
			user.nickname = mUser.nickname;
			user.birthday = mUser.birthday;
			user.address = mUser.address;
			user.key = mUser.key;
			user.mark = mUser.mark;
			user.gender = mUser.gender;
			user.icon = mUser.icon;
			user.qq = mUser.qq;
			user.qqId = mUser.qqId;
			user.mobile = mUser.mobile;
			user.settings = mUser.settings;
			user.lastLogin = mUser.lastLogin;
			user.dated = mUser.dated;
			user.device = mUser.device;
			user.serialNo = mUser.serialNo;
			user.update(new UpdateListener()
			{
				public void done(BmobException e)
				{
					if (e != null)
						ExceptionHandler.log("Bmob_updateUser", e.toString());
				}
			});
		}
		catch (Exception e)
		{
			ExceptionHandler.log("updateUser", e.toString());
		}
	}
	
	private static void init(SharedPreferences spf)
	{
		VERSION_CODE = spf.getInt("check", 0);
		APP_THEME_ID = spf.getInt("theme", 0);
		APP_BACK_ID = spf.getInt("back", 0);
		SDATE = spf.getString("checkCode", "");
		IS_ACTIVE = spf.getBoolean("app", false);
		MUSIC = spf.getBoolean("music", true);
		KEY = spf.getString("key", "");
		SID = spf.getString("date","");
		NO_ADV = spf.getBoolean("noadv", false);
		WEL_ADV = spf.getBoolean("weladv", true);
		USERID = spf.getString("uid", "");
		USERPASS = spf.getString("sid", "");
		AUTO_LOGIN_MODE = spf.getInt("loginmode", 0);
		TIPS = spf.getBoolean("tips", true);
		JAVA_TEXT_SIZE = spf.getInt("java_size", 2);
		READ_Android = new ArrayList<String>();
		READ_J2EE = new ArrayList<String>();
		READ_AndroidAdvance = new ArrayList<String>();
		String[] android = spf.getString("read_android", "").split(",");
		if (android != null && android.length > 0)
		{
			for (String s : android)
				READ_Android.add(s);
		}
		String[] android2 = spf.getString("read_android2", "").split(",");
		if (android2 != null && android2.length > 0)
		{
			for (String s : android2)
				READ_AndroidAdvance.add(s);
		}
		String[] javaee = spf.getString("read_javaee", "").split(",");
		if (javaee != null && javaee.length > 0)
		{
			for (String s : javaee)
				READ_J2EE.add(s);
		}
	}
	
	public static InboxManager getInbox()
	{
		if (myInbox == null)
			myInbox = new InboxManager();
		return myInbox;
	}
	
	public static void addAndroidRead(String num)
	{
		AUDIO_STUDY_STATE+=1;
		if (READ_Android.contains(num))
			return;
		READ_Android.add(num);
	}
	
	public static void addAndroid2Read(String num)
	{
		AUDIO_STUDY_STATE+=1;
		if (READ_AndroidAdvance.contains(num))
			return;
		READ_AndroidAdvance.add(num);
	}
	
	public static void addJavaEeRead(String num)
	{
		AUDIO_STUDY_STATE+=1;
		if (READ_J2EE.contains(num))
			return;
		READ_J2EE.add(num);
	}
	
	public static void clearReadHistory()
	{
		READ_J2EE.clear();
		READ_Android.clear();
		READ_AndroidAdvance.clear();
	}
	
	public static boolean canLogin()
	{
		if (AUTO_LOGIN_MODE <= 0 || USERID.equals("") || USERPASS.equals("") || mUser != null)
			return false;
		return true;
	}
	
	//自动登录
	public static void Login(Context context, LoginListener listener)
	{
		if (AUTO_LOGIN_MODE == 1)
			Login(context, USERID, USERPASS, 3, listener);
		else if (AUTO_LOGIN_MODE == 2)
			qqLogin(context, USERPASS, listener);
	}
	
	public static boolean isAudio()
	{
		if (AUDIO && audioService != null)
		{
			if (audioService.isPlay())
				return false;
		}
		return AUDIO;
	}
	
	public static boolean isNoadv()
	{
		return (Common.IS_ACTIVE && Common.NO_ADV);
	}
	
	public static boolean isWeladv()
	{
		return (!Common.IS_ACTIVE || Common.WEL_ADV);
	}
	
	public static boolean isVipUser()
	{
		if (mUser != null && mUser.role != null && IS_ACTIVE)
			return mUser.role >= 10;
		return false;
	}
	
	public static boolean isAdminUser()
	{
		if (mUser != null && mUser.role != null)
			return mUser.role == 10;
		return false;
	}
	
	public static void saveUserIconByName(String name)
	{
		BmobQuery<Users> sql = new BmobQuery<Users>();
		sql.addWhereEqualTo("name", name);
		sql.findObjects(new FindListener<Users>()
		{
			@Override
			public void done(List<Users> list, BmobException e)
			{
				if (e == null && list.size() > 0)
					saveIcon(list.get(0));
			}
		});
	}
	
	public static void qqLogin(final Context context, String openId, final LoginListener listener)
	{
		BmobQuery<Users> sql = new BmobQuery<Users>();
		sql.addWhereEqualTo("qqId", openId);
		//sql.addWhereEqualTo("valid", true);
		sql.findObjects(new FindListener<Users>()
		{
			@Override
			public void done(List<Users> list, BmobException e)
			{
				if (e == null)
				{
					if (list.size() > 0)
					{
						mUser = list.get(0);
						if (!mUser.valid)
						{
							if (listener != null)
								listener.onLogin(2);
							return;
						}
						SharedPreferences spf = context.getSharedPreferences("java21", Context.MODE_PRIVATE);
						SharedPreferences.Editor editor = spf.edit();
						editor.putString("uid", Common.mUser.name);
						editor.putString("sid", Common.mUser.qqId);
						editor.putInt("loginmode", 2);
						USERID = mUser.name;
						SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						editor.putString("key", KEY);
						if (!isVipUser())
							editor.putBoolean("weladv", true);
						if (!IS_ACTIVE)//自动激活
						{
							KEY = mUser.key;
							if (regist(KEY))
							{
								IS_ACTIVE = true;
								editor.putBoolean("autok", true);
								editor.putString("date", format.format(new Date()));
								editor.putBoolean("app", true);
							}
						}
						editor.commit();
						updateUserLogin(context);
						if (listener != null)
							listener.onLogin(1);
						IsChangeICON = true;
					}
					else {
						if (listener != null)
							listener.onLogin(0);
					}
				}
				else
				{
					//ExceptionHandler.log("登录失败:"+e.toString());
					if (listener != null)
						listener.onError(e.toString());
				}
			}
		});
	}
	
	public static void Login(final Context context, final String username, final String pasdword, final int mode, final LoginListener listener)
	{
		BmobQuery<Users> sql1 = new BmobQuery<Users>();
		sql1.addWhereEqualTo("name", username);
		BmobQuery<Users> sql2 = new BmobQuery<Users>();
		sql2.addWhereEqualTo("email", username);
		BmobQuery<Users> sql3 = new BmobQuery<Users>();
		sql3.addWhereEqualTo("mobile", username);
		List<BmobQuery<Users>> sqls = new ArrayList<BmobQuery<Users>>();
		sqls.add(sql1);
		sqls.add(sql2);
		sqls.add(sql3);
		BmobQuery<Users> sql = new BmobQuery<Users>();
		sql.or(sqls);
		sql.addWhereEqualTo("psd", pasdword);
		//sql.addWhereEqualTo("valid", true);
		sql.findObjects(new FindListener<Users>()
		{
			@Override
			public void done(List<Users> list, BmobException e)
			{
				if (e == null)
				{
					if (list.size() > 0)
					{
						Common.mUser = list.get(0);
						SharedPreferences spf = context.getSharedPreferences("java21", Context.MODE_PRIVATE);
						SharedPreferences.Editor editor = spf.edit();
						if (mode < 3)
						{
							editor.putString("uid", mode == 1 ? Common.mUser.name : "");
							editor.putString("sid", mode == 1 ? Common.mUser.psd : "");
							editor.putInt("loginmode", mode);
							USERID = username;
							if (!IS_ACTIVE)//自动激活
							{
								KEY = mUser.key;
								if (regist(KEY))
								{
									SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
									IS_ACTIVE = true;
									editor.putBoolean("autok", true);
									editor.putString("date", format.format(new Date()));
									editor.putBoolean("app", true);
								}
							}
						}
						else if (!mUser.valid)
						{
							if (listener != null)
								listener.onLogin(2);
							mUser = null;
							return;
						}
						if (!isVipUser())
							editor.putBoolean("weladv", true);
						editor.putString("key", KEY);
						editor.commit();
						updateUserLogin(context);
						if (listener != null)
							listener.onLogin(1);
						IsChangeICON = true;
					}
					else
					{
						if (listener != null)
							listener.onLogin(0);
					}
				}
				else
				{
					//ExceptionHandler.log("登录失败:"+e.toString());
					if (listener != null)
						listener.onError(e.toString());
				}
			}
		});
	}
	
	public static void Logout()
	{
		mUser = null;
		IsChangeICON = true;
		if (myInbox != null)
		{
			myInbox.logout();
			myInbox = null;
		}
	}
	
	public static void Logout(Context context)
	{
		mUser = null;
		IsChangeICON = true;
		SharedPreferences spf = context.getSharedPreferences("java21", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = spf.edit();
		editor.putInt("loginmode", 0);
		editor.commit();
		AUTO_LOGIN_MODE = 0;
	}
	
	public static boolean isLogin()
	{
		return (mUser != null);
	}
	
	private static boolean regist(String key)
	{
		try
		{
			if (key.length() == 25 && !key.equals(defaultKey))
			{
				Keys plug = new SecretKey(key).getInstance();
				return plug.verify();
			}
			else
				return false;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	
	public static boolean isNet(Context context)
	{
		ConnectivityManager zdapp = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (zdapp == null)
		{
			return false;
		}
		else
		{
			NetworkInfo[] info = zdapp.getAllNetworkInfo();
			if (info != null)
			{
				for (int i=0; i<info.length; i++)
				{
					if (info[i].getState() == NetworkInfo.State.CONNECTED)
					{
						return true;
					}
				}
				//zdapp.getActiveNetworkInfo().isAvailable()
			}
		}
		return false;
	}
	
	public static String getCachePath()
	{
		return LocalPath + "Cache/";
	}
	
	public static String getIconPath()
	{
		return LocalPath + "Portrait/";
	}
	
	public static String getDrawImagePath()
	{
		return getIconPath() + "back.jpg";
	}
	
	public static String getTempImagePath()
	{
		return getCachePath() + "temp.jpg";
	}
	
	public static Bitmap getIcon()
	{
		if (mUser.icon != null)
		{
			File path = new File(getIconPath());
			if (!path.exists())
			{
				path.mkdirs();
			}
			File file = new File(path, mUser.icon.getFilename());
			if (file.exists())
				return BitmapFactory.decodeFile(file.getAbsolutePath());
			else
			{
				try
				{
					mUser.icon.download(file, new DownloadFileListener()
					{
						@Override
						public void done(String p1, BmobException p2)
						{
							
						}

						@Override
						public void onProgress(Integer p1, long p2)
						{

						}
					});
				}
				catch (Exception e)
				{
					ExceptionHandler.log("getIcon", e.toString());
				}
			}
		}
		return null;
	}
	
	public static Bitmap getIcon(String username)
	{
		File file = new File(getCachePath(), username + ".png");
		if (file.exists())
			return BitmapFactory.decodeFile(file.getAbsolutePath());
		else
			saveUserIconByName(username);
		return null;
	}
	
	public static void saveIcon(Users user)
	{
		if (user.icon == null)
			return;
		File path = new File(getCachePath());
		if (!path.exists())
			path.mkdirs();
		File file = new File(path, user.name + ".png");
		if (!file.exists())
		{
			try
			{
				user.icon.download(file, new DownloadFileListener()
				{
					@Override
					public void done(String p1, BmobException p2)
					{

					}

					@Override
					public void onProgress(Integer p1, long p2)
					{

					}
				});
			}
			catch (Exception e)
			{
				ExceptionHandler.log("saveIcon", e.toString());
			}
		}
	}
	
	public static void setIcon(final ImageView v, final Context context, boolean downed)
	{
		if (!downed)
			v.setImageDrawable(getRoundedIconDrawable(context, BitmapFactory.decodeResource(context.getResources(), R.mipmap.head)));
		if (mUser != null && mUser.icon != null)
		{
			File path = new File(getIconPath());
			if (!path.exists())
				path.mkdirs();
			File file = new File(path, mUser.icon.getFilename());
			if (file.exists())
				v.setImageDrawable(getRoundedIconDrawable(context, BitmapFactory.decodeFile(file.getAbsolutePath())));
			else
			{
				try
				{
					mUser.icon.download(file, new DownloadFileListener()
					{
						@Override
						public void done(String p1, BmobException p2)
						{
							setIcon(v, context, true);
						}

						@Override
						public void onProgress(Integer p1, long p2)
						{
						}
					});
				}
				catch (Exception e)
				{
					ExceptionHandler.log("getIcon", e.toString());
				}
			}
		}
	}
	
	public static void setMyIcon(ImageView v, Context context, RoundedBitmapDrawable defaultIcon)
	{
		if (mUser != null && mUser.icon != null)
		{
			File file = new File(getIconPath(), mUser.icon.getFilename());
			if (file.exists())
				v.setImageDrawable(getRoundedIconDrawable(context, BitmapFactory.decodeFile(file.getAbsolutePath())));
			else
				v.setImageDrawable(defaultIcon);
		}
	}
	
	public static BitmapDrawable getHomeBack()
	{
		Bitmap bit = getDrawerBack();
		if (bit != null)
			return new BitmapDrawable(bit);
		return null;
	}
	
	public static Bitmap getDrawerBack()
	{
		File path = new File(getIconPath());
		if (!path.exists())
		{
			path.mkdirs();
		}
		File file = new File(getDrawImagePath());
		if (file.exists())
			return BitmapFactory.decodeFile(file.getAbsolutePath());
		return null;
	}
	
	public static RoundedBitmapDrawable getRoundedIconDrawable(Context context, Bitmap src)
	{
		if (src != null)
		{
			RoundedBitmapDrawable rbd = RoundedBitmapDrawableFactory.create(context.getResources(), src);
			rbd.setCornerRadius(src.getWidth()/2);
			rbd.setAntiAlias(true);//设置反走样
			return rbd;
		}
		return null;
	}
	
	public static boolean isMyUser(String name)
	{
		if (mUser != null && !Util.isNullOrEmpty(name))
		{
			return mUser.name.equals(name);
		}
		return false;
	}
	
	public static boolean isContainsUser(String nameList)
	{
		if (mUser != null && !Util.isNullOrEmpty(nameList))
		{
			return nameList.contains(mUser.name);
		}
		return false;
	}
	
	public static String getUsername()
	{
		if (mUser != null)
			return mUser.name;
		return "";
	}
	
	public static void getTestMsg()
	{
		BmobQuery<Copys> sql = new BmobQuery<Copys>();
		sql.addWhereEqualTo("enable", true);
		sql.findObjects(new FindListener<Copys>()
		{
			@Override
			public void done(List<Copys> list, BmobException e)
			{
				if (e == null && list.size() > 0)
				{
					copyMsg = list.get(0);
				}
			}
		});
	}
	
	public static Copys getCopyMsg()
	{
		return copyMsg;
	}
	
	public static void getTips()
	{
		lastTipsTime = System.currentTimeMillis();
		BmobQuery<Tips> sql1 = new BmobQuery<Tips>();
		sql1.addWhereGreaterThanOrEqualTo("dates", new BmobDate(new Date()));
		BmobQuery<Tips> sql2 = new BmobQuery<Tips>();
		sql2.addWhereEqualTo("islong", true);
		List<BmobQuery<Tips>> sqls = new ArrayList<BmobQuery<Tips>>();
		sqls.add(sql1);
		sqls.add(sql2);
		BmobQuery<Tips> sql = new BmobQuery<Tips>();
		sql.or(sqls);
		sql.addWhereEqualTo("enable", true);
		if (!TIPS)
			sql.addWhereEqualTo("openSMS", true);
		sql.findObjects(new FindListener<Tips>()
		{
			@Override
			public void done(List<Tips> list, BmobException e)
			{
				if (e == null)
				{
					if (list.size() > 0)
						mTips = list;
					else
						mTips.clear();
				}
			}
		});
	}
	
	public static Tips getTip()
	{
		try
		{
			if (mTips != null)
			{
				if (mTips.size() == 0)
				{
					getTips();
					return null;
				}
				else if (System.currentTimeMillis() - lastTipsTime > 60000)
					getTips();
				int ma = (int)(Math.random() * (double)(mTips.size()));
				if (ma >= mTips.size())
					ma = mTips.size() - 1;
				return mTips.get(ma);
			}
			else
				getTips();
		}
		catch (Exception e)
		{
			ExceptionHandler.log("getTip_Random", e.toString());
		}
		return null;
	}
	
	public static boolean isSupportMD()
	{
		return Build.VERSION.SDK_INT > 20;
	}
	
	public static void startActivityOption(Activity context, Intent intent, View view, String shareName)
	{
		startActivityOptions(context, intent, view, shareName, false);
	}
	
	public static void startActivityOptions(Activity context, Intent intent, View view, String shareName)
	{
		startActivityOptions(context, intent, view, shareName, true);
	}
	
	public static void startActivityOptions(Activity context, Class<?> cls)
	{
		startActivityOptions(context, new Intent(context, cls));
	}
	
	public static void startActivityForResult(Activity context, Class<?> cls, int requestCode)
	{
		startActivityForResult(requestCode, context, new Intent(context, cls));
	}
	
	public static void startActivityOptions(Activity context, Intent intent, Pair<View,String>...pairs)
	{
		try
		{
			if (isSupportMD())
			{
				ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(context, pairs);
				context.startActivity(intent, options.toBundle());
			}
			else
			{
				context.startActivity(intent);
				context.overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
			}
			ActivityManager.getInstance().startActivity(context);
		}
		catch (Exception e)
		{
			context.startActivity(intent);
			ExceptionHandler.log("StartActivityOptionsPair", e.toString());
		}
	}
	
	public static void startActivityForResult(int requestCode, Activity context, Intent intent, Pair<View,String>...pairs)
	{
		try
		{
			if (isSupportMD())
			{
				ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(context, pairs);
				context.startActivityForResult(intent, requestCode, options.toBundle());
			}
			else
			{
				context.startActivityForResult(intent, requestCode);
				context.overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
			}
			ActivityManager.getInstance().startActivity(context);
		}
		catch (Exception e)
		{
			context.startActivityForResult(intent, requestCode);
			ExceptionHandler.log("StartActivityForResultPair", e.toString());
		}
	}
	
	private static void startActivityOptions(Activity context, Intent intent, View view, String shareName, boolean setName)
	{
		try
		{
			if (isSupportMD())
			{
				if (setName)
					view.setTransitionName(shareName);
				ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(context, view, shareName);
				context.startActivity(intent, options.toBundle());
				ActivityManager.getInstance().startActivity(context);
			}
			else
				startActivityOptions(context, intent);
		}
		catch (Exception e)
		{
			context.startActivity(intent);
			ExceptionHandler.log("StartActivityOptionsShare", e.toString());
		}
	}
	
	public interface LoginListener
	{
		void onLogin(int code);
		void onError(String error);
	}
	
	public static void gc(Context c)
	{
		//gc垃圾回收: 恢复到初始化状态
		IsRun = false;//运行状态
		AUTO_LOGIN_MODE = 0;//自动登录类型，1为普通，2为QQ
		SDATE = null;//激活凭证
		KEY = null;//密钥
		SID = null;//激活时间
		USERID = null;//用户名
		USERPASS = null;//加密密码
		IS_ACTIVE = false;//是否注册
		NO_ADV = false;//去广告
		WEL_ADV = true;//欢迎页广告
		SOUND = null;//音乐池
		mUser = null;//登录用户
		FileProvider = null;//文件提供者
		mTips = null;//通知中心
		READ_Android = null;
		READ_J2EE = null;
		READ_AndroidAdvance = null;
		myInbox = null;//消息中心
		copyMsg = null;
		//停止正在运行的音乐服务
		if (audioService != null)
		{
			if (audioService.isPlay())
				audioService.Stop();
			c.stopService(new Intent(c, AudioService.class));
		}
		//清空音节码加载池
		if (SOUND != null)
		{
			SOUND.clear();
			SOUND = null;
		}
		System.gc();
	}
}
