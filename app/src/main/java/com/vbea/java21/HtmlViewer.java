package com.vbea.java21;

import java.net.URL;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.Manifest;
import android.app.Activity;
import android.app.SearchManager;
import android.os.Bundle;
import android.os.Handler;
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.WebStorage;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.content.Intent;
import android.content.Context;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.net.Uri;
import android.text.TextUtils;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.annotation.SuppressLint;
//import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.SearchView;
import android.support.v4.view.MenuItemCompat;
import android.support.design.widget.BottomSheetDialog;
import com.vbea.java21.data.WebHelper;
import com.vbea.java21.widget.MyWebView;
import com.vbea.java21.classes.Common;
import com.vbea.java21.classes.Util;
import com.vbea.java21.classes.SocialShare;
import com.vbea.java21.classes.AlertDialog;
import com.vbea.java21.classes.ExceptionHandler;
import com.tencent.connect.common.Constants;
import com.tencent.connect.share.QQShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import android.os.*;

public class HtmlViewer extends AppCompatActivity
{
	private SearchView searchView;
	private MenuItem searchItem;
	private Toolbar tool;
	private MyWebView webView;
	private WebView souceView;
	private TextView NightView;
	private ProgressBar webProgress;
	private BottomSheetDialog mBSDialog;
	private LinearLayout share_qq, share_qzone, share_wx, share_wxpy;
	private LinearLayout share_sina, share_web, share_link, share_more;
	private String SH_html, SH_url = "", SH_history = "", SH_hisName = "", SH_home, UA_Default;
	private int SH_search = 0, SH_UA = 0;
	private Uri cameraUri;
	private String[] Searchs, UAurls;
	private boolean ISSOURCE = false;
	private SharedPreferences spf;
	private WebSettings wset;
	private WebHelper webHelper;
	private static final int FILECHOOSER_RESULTCODE = 1;
	private static final int CHOOSE_CAMERA = 2;
	private ValueCallback<Uri> mUploadMessage;
	private ValueCallback<Uri[]> mUploadMessages;
	private String[] headItems = {"拍照","相册"};
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		Common.start(getApplicationContext());
		setTheme(MyThemes.getTheme());
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browser);
		
		webView = (MyWebView) findViewById(R.id.WebViewPage);
		souceView = (WebView) findViewById(R.id.WebViewSource);
		tool = (Toolbar) findViewById(R.id.toolbar);
		NightView = (TextView) findViewById(R.id.api_nightView);
		webProgress = (ProgressBar) findViewById(R.id.apiProgress);
		tool.setTitle("点此输入网址或搜索");
		setSupportActionBar(tool);
		if (MyThemes.isNightTheme()) NightView.setVisibility(View.VISIBLE);
		//if (Common.isSupportMD())
			//web.removeJavascriptInterface("searchBoxJavaBridge_");
		wset = webView.getSettings();
		UA_Default = wset.getUserAgentString();
		wset.setJavaScriptEnabled(true);
		wset.setSupportZoom(true);
		wset.setBuiltInZoomControls(true);
		wset.setDisplayZoomControls(false);
		wset.setDefaultTextEncodingName("UTF-8");
		//html5
		wset.setDomStorageEnabled(true);
		wset.setDatabaseEnabled(true);
		wset.setDatabasePath(getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath());
		wset.setAppCacheEnabled(true);
		wset.setAppCachePath(getApplicationContext().getDir("cache", Context.MODE_PRIVATE).getPath());
		wset.setPluginState(WebSettings.PluginState.ON);
		wset.setUseWideViewPort(true);
		wset.setLoadWithOverviewMode(true);
		wset.setUserAgentString(wset.getUserAgentString());
		wset.setGeolocationEnabled(true);
		wset.setGeolocationDatabasePath(getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath());
		wset.setCacheMode(WebSettings.LOAD_DEFAULT);
		wset.setAppCacheMaxSize(1024*1024*10);
		wset.setAllowFileAccess(true);
		wset.setRenderPriority(WebSettings.RenderPriority.HIGH);
		//wset.setGeolocationEnabled(true);
		//wset.setJavaScriptCanOpenWindowsAutomatically(true);
		wset.setSupportMultipleWindows(false);
		webView.addJavascriptInterface(new JavaScriptShowCode(), "showcode");
		onSetting();
		mHandler.sendEmptyMessageDelayed(0, 300);
		webView.setWebChromeClient(new MyWebChromeClient());
		
		webView.setWebViewClient(new WebViewClient()
		{
			@Override
			public boolean shouldOverrideUrlLoading(WebView v, final String url)
			{
				if (!URLUtil.isValidUrl(url))
				{
					if (url.toLowerCase().startsWith("vbea://"))
					{
						SH_url = url.replace("vbea://", "http://");
						loadUrls(url);
					}
					else
					{
						Util.showConfirmCancelDialog(HtmlViewer.this, "提示", "网页想要打开第三方应用，是否继续？", new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface d, int s)
							{
								try
								{
									Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
									intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
									startActivity(intent);
								}
								catch (Exception e)
								{
									Util.toastShortMessage(getApplicationContext(), "未安装该应用");
									//ExceptionHandler.log("HtmlDialog", e.toString());
								}
							}
						});
					}
					return true;
				}
				/*else// if (!SH_url.equals(url))
				{
					//Util.toastShortMessage(getApplicationContext(), SH_url + "\n" + url);
					SH_url = url;
					mHandler.sendEmptyMessageDelayed(1, 300);
					//return true;
				}*/
				return super.shouldOverrideUrlLoading(v, url);
			}
			
			@Override
			public void onPageStarted(WebView v, String url, Bitmap icon)
			{
				SH_url = url;
				if (searchItem != null)
					searchItem.collapseActionView();
				super.onPageStarted(v, url, icon);
			}
			
			@Override
			public void onPageFinished(WebView view, String url)
			{
				SH_url = url;
				addHistory();
				view.loadUrl("javascript:window.showcode.show(document.getElementsByTagName('html')[0].outerHTML);");
				tool.setTitle(view.getTitle());
				//view.loadUrl("javascript:close_adtip();");
				super.onPageFinished(view, url);
			}
		});
		
		webView.setDownloadListener(new DownloadListener()
		{
			@Override
			public void onDownloadStart(final String url, String userAgent, String disposition, String mimetype, long length)
			{
				Util.showConfirmCancelDialog(HtmlViewer.this, "下载文件", "文件名："+getDowloadFileName(disposition, url).trim() + "\n大小：" + Util.getFormatSize(length) + "\n确定要下载该文件？", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface d, int s)
					{
						try
						{
							Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
							startActivity(intent);
						}
						catch (Exception e)
						{
							Util.toastShortMessage(getApplicationContext(), "未安装下载管理器");
						}
					}
				});
			}
		});
		
		tool.setNavigationOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				if (ISSOURCE)
					goPage();
				else
					supportFinishAfterTransition();
			}
		});
		
		tool.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if (searchItem != null)
				{
					searchItem.expandActionView();
					searchView.setQuery(webView.getUrl(), false);
				}
			}
		});
	}
	
	private void init()
	{
		Intent intent = getIntent();
		if (intent != null && intent.getAction() != null)
		{
			if (intent.getAction().equals(Intent.ACTION_VIEW))
				loadUrls(intent.getDataString());
			else if (intent.getAction().equals(Intent.ACTION_SEND))
			{
				String share = intent.getStringExtra(Intent.EXTRA_TEXT);
				if (!Util.isNullOrEmpty(share))
				{
					if (share.indexOf("http") > 0)
						loadUrls(share.substring(share.indexOf("http")));
					else
						loadUrls(share);
				}
				
			}
			else if (intent.getAction().equals(intent.ACTION_WEB_SEARCH))
				loadUrlsSearch(intent.getStringExtra(SearchManager.QUERY));
		}
		else
		{
			if (!Util.isNullOrEmpty(SH_home))
				loadUrls(SH_home);
		}
	}
	
	private Bitmap getShareBitmap()
	{
		View v = webView;
		v.setDrawingCacheEnabled(true);
		v.buildDrawingCache();
		return Bitmap.createScaledBitmap(v.getDrawingCache(), 120, 120, true);
	}
	
	private void loadUrls(String url)
	{
		tool.setTitle(url);
		//SH_url = url;
		if (Util.isNullOrEmpty(url))
			return;
		//if (url.indexOf("http://") == 0 || url.indexOf("https://") == 0 || url.indexOf("ftp://") == 0 || url.indexOf("file://") == 0)
		if (URLUtil.isValidUrl(url))
			webView.loadUrl(url);
		else if (url.indexOf("vbea://") == 0)
			webView.loadUrl(url.replace("vbea:", "http:"));
		else if (url.indexOf(".") > 0)
		{
			String s = "http://" + url;
			if (URLUtil.isValidUrl(s) && isUrl(s))
				webView.loadUrl(s);
			else
				loadUrlsSearch(url);
		}
		else
			loadUrlsSearch(url);
	}
	//搜索引擎
	private void loadUrlsSearch(String key)
	{
		webView.loadUrl(String.format(Searchs[SH_search], key));
	}
	
	public boolean isUrl(String url)
	{
		Pattern pat = Pattern.compile("(http|https|file|ftp)+[://]+(\\S+\\.)+[\\w-]+(/[\\w-_./?%&=]*)?.*");
		Matcher mat = pat.matcher(url);
		return mat.matches();
	}
	
	public boolean isValidHome()
	{
		if (SH_home.length() > 0)
		{
			int begin = SH_home.indexOf("://");
			if (begin > 0)
			{
				if (SH_home.substring(begin).length() > 0)
					return true;
			}
		}
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.web_menu, menu);
		searchItem = menu.findItem(R.id.item_urls);
		searchView = (SearchView) searchItem.getActionView();
		searchItem.setActionView(searchView);
		searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener()
		{
			@Override
			public void onFocusChange(View p1, boolean p2)
			{
				webView.setNestedScrollingEnabled(!p2);
				if (!p2)
					searchItem.collapseActionView();
			}
		});
		ImageView image = (ImageView) searchView.findViewById(android.support.v7.appcompat.R.id.search_mag_icon);
		image.setVisibility(View.GONE);
		searchItem.setVisible(false);
		searchView.setQueryHint("搜索或输入网址");
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
		{
			@Override
			public boolean onQueryTextSubmit(String str)
			{
				loadUrls(str);
				if (ISSOURCE)
					goPage();
				searchView.clearFocus();
				searchItem.collapseActionView();
				//searchView.setQuery("", false);
				return true;
			}

			@Override
		 	public boolean onQueryTextChange(String p1)
		 	{
		 		return false;
		 	}
		});
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		menu.findItem(R.id.item_back).setVisible(webView.canGoBack() && !ISSOURCE);
		menu.findItem(R.id.item_forward).setVisible(webView.canGoForward() && !ISSOURCE);
		menu.findItem(R.id.item_androidshare).setVisible(!SH_url.equals(""));
		menu.findItem(R.id.item_code).setVisible(!SH_url.equals(""));
		menu.findItem(R.id.item_home).setVisible(isValidHome());
		menu.findItem(R.id.item_code).setTitle(ISSOURCE ? "返回" : "查看源");
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == R.id.item_back)
			webView.goBack();
		else if (item.getItemId() == R.id.item_forward)
			webView.goForward();
		else if (item.getItemId() == R.id.item_home)
			loadUrls(SH_home);
		else if (item.getItemId() == R.id.item_flush)
		{
			if (Common.isNet(this) && !ISSOURCE)
				webView.reload();
		}
		else if (item.getItemId() == R.id.item_addbook)
			showBookmark();
		else if (item.getItemId() == R.id.item_history)
			Common.startActivityForResult(HtmlViewer.this, History.class, 520);
		else if (item.getItemId() == R.id.item_androidshare)
		{
			mBSDialog = new BottomSheetDialog(this);
			View view = getLayoutInflater().inflate(R.layout.sharelayout, null);
			share_qq = (LinearLayout) view.findViewById(R.id.btn_share_qq);
			share_qzone = (LinearLayout) view.findViewById(R.id.btn_share_qzone);
			share_wx = (LinearLayout) view.findViewById(R.id.btn_share_wx);
			share_wxpy = (LinearLayout) view.findViewById(R.id.btn_share_wxline);
			share_sina = (LinearLayout) view.findViewById(R.id.btn_share_sina);
			share_web = (LinearLayout) view.findViewById(R.id.btn_share_browser);
			share_link = (LinearLayout) view.findViewById(R.id.btn_share_copylink);
			share_more = (LinearLayout) view.findViewById(R.id.btn_share_more);
			mBSDialog.setContentView(view);
			mBSDialog.show();
			share_qq.setOnClickListener(new QQShareListener());
			share_qzone.setOnClickListener(new QQShareListener());
			share_more.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View v)
				{
					Intent intent = new Intent(Intent.ACTION_SEND);
					intent.putExtra(Intent.EXTRA_TEXT, SH_url);
					intent.setType("text/html");
					startActivity(intent);
					mBSDialog.dismiss();
				}
			});
			share_web.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View v)
				{
					Intent intent = new Intent(Intent.ACTION_VIEW);
					Uri uri = Uri.parse(SH_url);
					intent.setData(uri);
					startActivity(intent);
				}
			});
			share_link.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View v)
				{
					mBSDialog.dismiss();
					ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
					cm.setPrimaryClip(ClipData.newRawUri("url", Uri.parse(SH_url)));
					Util.toastShortMessage(getApplicationContext(), "已复制到剪贴板");
				}
			});
			share_wx.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View v)
				{
					SocialShare.shareToWeixin(webView.getTitle(), webView.getTitle(), SH_url, getShareBitmap());
				}
			});
			share_wxpy.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View v)
				{
					SocialShare.shareToWeixinZone(webView.getTitle(), webView.getTitle(), SH_url, getShareBitmap());
				}
			});
			share_sina.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View v)
				{
					SocialShare.shareToWeixinFavorite(webView.getTitle(), webView.getTitle(), SH_url, getShareBitmap());
				}
			});
		}
		else if (item.getItemId() == R.id.item_code)
		{
			if (Util.isNullOrEmpty(webView.getUrl()))
				return true;
			if (ISSOURCE)
				goPage();
			else
			{
				if (!Util.isNullOrEmpty(SH_html))
				{
					ISSOURCE = true;
					souceView.setVisibility(View.VISIBLE);
					webView.setVisibility(View.GONE);
					souceView.loadData(SH_html, "text/plain; charset=UTF-8", null);
					tool.setTitle("view-source:"+webView.getUrl());
					searchView.setQuery(webView.getUrl(), false);
				}
			}
		}
		else if (item.getItemId() == R.id.item_setting)
		{
			Common.startActivityOptions(HtmlViewer.this, BrowserSet.class);
		}
		return true;
	}
	
	IUiListener qqShareListener = new IUiListener()
	{
		@Override
        public void onCancel()
		{
			Util.toastShortMessage(getApplicationContext(), "分享取消");
        }
        @Override
        public void onComplete(Object response)
		{
			Util.toastShortMessage(getApplicationContext(), "分享成功");
		}
        @Override
        public void onError(UiError e)
		{
			//Util.toastShortMessage(getApplicationContext(), "onError: " + e.errorMessage + "e");
		}
	};
	
	class QQShareListener implements View.OnClickListener
	{
		public void onClick(View v)
		{
			Bundle params = new Bundle();
			params.putString(QQShare.SHARE_TO_QQ_TITLE, webView.getTitle());
			params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, SH_url);
			params.putString(QQShare.SHARE_TO_QQ_SUMMARY, webView.getTitle());
			params.putString(QQShare.SHARE_TO_QQ_APP_NAME,"21天学通Java");
			params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
			SocialShare.shareToQQ(HtmlViewer.this, params, v.getId()==R.id.btn_share_qzone, qqShareListener);
		}
	}
	
	public void goPage()
	{
		webView.setVisibility(View.VISIBLE);
		souceView.setVisibility(View.GONE);
		souceView.loadData("", "text/plain", null);
		tool.setTitle(webView.getTitle());
		searchItem.collapseActionView();
		ISSOURCE = false;
	}
	
	public String getDowloadFileName(String disposition, String url)
	{
		Pattern pattern = Pattern.compile("\"(.*)\"");
		Matcher matcher = pattern.matcher(disposition);
		while (matcher.find())
			return Util.trim(matcher.group(), '"');
		String suffixes="avi|mpeg|3gp|mp3|mp4|wav|jpeg|gif|jpg|png|apk|exe|pdf|rar|zip|docx|doc";  
		Pattern pat=Pattern.compile("[\\w]+[\\.]("+suffixes+")");
		matcher = pat.matcher(url);
		while(matcher.find())
			return matcher.group();
		return disposition;
	}
	
	/*public void showHtmlCode()
	{
		SH_htmlUrl = SH_url;
	}*/
	
	private void showBookmark()
	{
		if (Util.isNullOrEmpty(SH_url))
		{
			Common.startActivityForResult(HtmlViewer.this, Bookmark.class, 520);
			return;
		}
		AlertDialog dialog = new AlertDialog(this);
		dialog.setTitle(R.string.bookmark);
		dialog.setItems(R.array.array_bookmark, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface p1, int p2)
			{
				if (p2 == 0)
					Common.startActivityForResult(HtmlViewer.this, Bookmark.class, 520);
				else
					addBookmark();
				p1.dismiss();
			}
		});
		dialog.show();
	}
	
	private void addBookmark()
	{
		if (webHelper != null)
		{
			if (webHelper.addBookmark(webView.getTitle(), webView.getUrl()) > 0)
				Util.toastShortMessage(this, "添加书签成功");
			else
				Util.toastShortMessage(this, "添加失败");
		}
	}
	
	private void addHistory()
	{
		//添加历史记录 静默操作
		if (webHelper != null && !SH_history.equals(SH_url) && !SH_hisName.equals(webView.getTitle()))
		{
			SH_history = SH_url;
			SH_hisName = webView.getTitle();
			webHelper.addHistory(SH_hisName, SH_url);
		}
	}
	
	private void onSetting()
	{
		if (spf == null)
			spf = getSharedPreferences("java21", MODE_PRIVATE);
		if (Searchs == null)
			Searchs = getResources().getStringArray(R.array.array_search_url);
		if (UAurls == null)
		{
			UAurls = getResources().getStringArray(R.array.array_ua);
			UAurls[0] = UA_Default;
		}
		if (webHelper == null)
			webHelper = new WebHelper(this);
		SH_home = spf.getString("web_home", "");
		SH_search = spf.getInt("web_search", 0);
		int ua = spf.getInt("web_ua", 0);
		if (SH_UA != ua)
		{
			SH_UA = ua;
			wset.setUserAgentString(UAurls[ua]);
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
	}

	@Override
    //设置回退 
    //覆盖Activity类的onKeyDown(int keyCoder,KeyEvent event)方法 
    public boolean onKeyDown(int keyCode, KeyEvent event)
	{ 
        if ((keyCode == KeyEvent.KEYCODE_BACK) && event.getAction() == KeyEvent.ACTION_DOWN)
		{
			if (ISSOURCE)
				goPage();
			else if (webView.canGoBack())
            	webView.goBack(); //goBack()表示返回WebView的上一页面 
			else
				supportFinishAfterTransition();
            return true; 
        } 
        return super.onKeyDown(keyCode, event);
	}
	
	public void selectImage()
	{
		AlertDialog dialogBuild = new AlertDialog(HtmlViewer.this);
		dialogBuild.setItems(headItems, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int item)
			{
				dialog.dismiss();
				switch (item)
				{
					case 0:
						if (Util.hasAndroidN())
						{
							if (!Util.hasAllPermissions(HtmlViewer.this, Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE))
								Util.requestPermission(HtmlViewer.this, 1001, Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
							else
								startCamera();
						}
						else
							startCamera();
						break;
					case 1:
						Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
						intent.setType("image/*");
						Intent intent2 = Intent.createChooser(intent, "选择图片");
						startActivityForResult(intent2, FILECHOOSER_RESULTCODE);
						break;
				}
			}
		});
		dialogBuild.setOnCancelListener(new DialogInterface.OnCancelListener()
		{
			public void onCancel(DialogInterface d)
			{
				if (mUploadMessages != null)
				{
					//Uri[] uris = new Uri[1];
					//uris[0] = Uri.parse("");
					mUploadMessages.onReceiveValue(null);
					mUploadMessages = null;
				}
				else if (mUploadMessage != null)
				{
					mUploadMessage.onReceiveValue(Uri.parse(""));
					mUploadMessage = null;
				}
			}
		});
		dialogBuild.show();
	}
	
	private void startCamera()
	{
		try
		{
			Intent intent1 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			File file = new File(Common.getTempImagePath());
			cameraUri = MyFileProvider.getUriForFile(getApplicationContext(), Common.FileProvider, file);
			intent1.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
			startActivityForResult(intent1, CHOOSE_CAMERA);
		}
		catch (Exception e)
		{
			ExceptionHandler.log("startCamera_1", e.toString());
		}
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
	{
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == 1001 && Util.hasAllPermissionsGranted(grantResults))
			startCamera();
	}

	@Override
	protected void onPause()
	{
		super.onPause();
	}

	@Override
	protected void onResume()
	{
		onSetting();
		if (mBSDialog != null)
			mBSDialog.dismiss();
		super.onResume();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (resultCode == Activity.RESULT_OK)
		{
			switch (requestCode)
			{
				case CHOOSE_CAMERA:
					if (mUploadMessage != null)
					{
						File f = new File(Common.getTempImagePath());
						if (!f.exists())
							cameraUri = Uri.parse("");
						else
						{
							ContentValues values = new ContentValues();
							values.put(MediaStore.Images.Media.DATA, f.getAbsolutePath());
							values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
							getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
						}
						onReceiveValue(cameraUri);
					}
					break;
				case FILECHOOSER_RESULTCODE:
					if (mUploadMessage != null && data != null)
						onReceiveValue(data.getData());
					break;
				case Constants.REQUEST_QQ_SHARE:
					Tencent.handleResultData(data, qqShareListener);
					break;
				case Constants.REQUEST_QZONE_SHARE:
					Tencent.handleResultData(data, qqShareListener);
					break;
				case 520://History & Bookmark
					if (data != null)
						webView.loadUrl(data.getDataString());
					break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	private void onReceiveValue(Uri uri)
	{
		if (uri == null)
			uri = Uri.parse("");
		if (mUploadMessage != null)
			mUploadMessage.onReceiveValue(uri);
		if (mUploadMessages != null)
			mUploadMessages.onReceiveValue(new Uri[]{uri});
		mUploadMessage = null;
		mUploadMessages = null;
	}
	
	public class MyWebChromeClient extends WebChromeClient
	{
		@Override
		public void onReceivedTitle(WebView view, String title)
		{
			super.onReceivedTitle(view, title);
			tool.setTitle(title);
		}

		@Override
		public void onProgressChanged(WebView view, int newProgress)
		{
			webProgress.setProgress(newProgress);
			webProgress.setVisibility(newProgress >= 100 ? View.INVISIBLE : View.VISIBLE);
		}

		public void onExceededDatabaseQuota(String url, String databasein, long quota, long estime, long totalq, WebStorage.QuotaUpdater update)
		{
			update.updateQuota(5 * 1024 * 1024);
		}
		
		public void onGeolocationPermissionsShowPrompt(final String origin, final android.webkit.GeolocationPermissions.Callback callback)
		{
			Util.showConfirmCancelDialog(HtmlViewer.this, "提示", "来自"+origin+"的网页正在请求定位，请确认是否允许？", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface d, int s)
				{
					callback.invoke(origin, true, false);
				}
			});
		}

		// For Android 3.0-
		public void openFileChooser(ValueCallback<Uri> uploadMsg)
		{
			openFileChooser(uploadMsg, "", "");
		}

		// For Android 3.0+
		public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType)
		{
			openFileChooser(uploadMsg, acceptType, "");
		}

		// For Android 4.1
		public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture)
		{
			if (mUploadMessage != null)
				mUploadMessage.onReceiveValue(null);
			mUploadMessage = uploadMsg;
			String type = Util.isNullOrEmpty(acceptType) ? "*/*" : acceptType;
			if (type.equals("image/*"))
				selectImage();
			else
			{
				Intent i = new Intent(Intent.ACTION_GET_CONTENT);
				i.addCategory(Intent.CATEGORY_OPENABLE);
				i.setType(type);
				startActivityForResult(Intent.createChooser(i, "上传文件"), FILECHOOSER_RESULTCODE);
			}
		}

		//Android 5.0+
		@Override
		@SuppressLint("NewApi")
		public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams)
		{
			if (mUploadMessages != null)
				mUploadMessages.onReceiveValue(null);
			mUploadMessages = filePathCallback;
			String type = "*/*";
			if (fileChooserParams != null && fileChooserParams.getAcceptTypes() != null && fileChooserParams.getAcceptTypes().length > 0)
				type = fileChooserParams.getAcceptTypes()[0];
			type = Util.isNullOrEmpty(type) ? "*/*" : type;
			if (type.equals("image/*"))
				selectImage();
			else
			{
				Intent i = new Intent(Intent.ACTION_GET_CONTENT);
				i.addCategory(Intent.CATEGORY_OPENABLE);
				i.setType(type);
				startActivityForResult(Intent.createChooser(i, "文件上传"), FILECHOOSER_RESULTCODE);
			}
			return true;
		}
	}
	
	Handler mHandler = new Handler()
	{

		@Override
		public void handleMessage(Message msg)
		{
			if (msg.what == 0)
				init();
			else if (msg.what == 1)
				webView.loadUrl(SH_url);
			super.handleMessage(msg);
		}
	};
	
	class JavaScriptShowCode
	{
		@JavascriptInterface
		public void show(String html)
		{
			SH_html = "<!DOCTYPE html>"+html;
		}
	}
}
