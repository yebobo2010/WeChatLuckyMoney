package xyz.monkeytong.hongbao.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.io.File;
import java.util.List;

public class AutomaticInstallService extends AccessibilityService {

	// 大多数的手机包名一样，联想部分机型的手机不一样
	private String[] packageNames = { "com.tencent.qq", "com.android.packageinstaller",
			"com.lenovo.security", "com.lenovo.safecenter" };

	@Override
	protected void onServiceConnected() {
		super.onServiceConnected();
		AccessibilityServiceInfo mAccessibilityServiceInfo = new AccessibilityServiceInfo();
		// 响应事件的类型，这里是全部的响应事件（长按，单击，滑动等）
		mAccessibilityServiceInfo.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED | AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED | AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
		// 反馈给用户的类型，这里是语音提示
		mAccessibilityServiceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
		mAccessibilityServiceInfo.notificationTimeout = 100;
		// 过滤的包名
		mAccessibilityServiceInfo.packageNames = packageNames;
		setServiceInfo(mAccessibilityServiceInfo);
	}

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		installApplication(event);
	}

	@Override
	public void onInterrupt() {

	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}

	/**
	 * 查找关键字并执行点击按钮的操作
	 *
	 * @param event
	 */
	private void installApplication(AccessibilityEvent event) {
		if (event.getSource() != null
				&& isContainInPackages(event.getPackageName().toString())
				&& !isInstall()) {

			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			String path = Environment.getExternalStorageState() + "/MJTVWeather-leshi-release.apk";
			intent.setDataAndType(Uri.parse("file://" + path), "application/vnd.android.package-archive");

//			findNodesByText(event, "下一步");
//			findNodesByText(event, "继续安装");
			findNodesByText(event, "安装");
			findNodesByText(event, "打开");
		}
	}

	/**
	 * 判断包名
	 *
	 * @param str
	 *            当前界面包名
	 * @return
	 */
	private boolean isContainInPackages(String str) {
		boolean flag = false;
		for (int i = 0; i < packageNames.length; i++) {
			if ((packageNames[i]).equals(str)) {
				flag = true;
				return flag;
			}
		}
		return flag;
	}

	private boolean isInstall() {
		PackageManager manager = getPackageManager();
		List<PackageInfo> infoList = manager.getInstalledPackages(0);
		for (PackageInfo info : infoList) {
			if ("com.moji.tvweather".equals(info.packageName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 根据文字寻找节点
	 *
	 * @param event
	 * @param text
	 *            文字
	 */
	private void findNodesByText(AccessibilityEvent event, String text) {
		List<AccessibilityNodeInfo> nodes = event.getSource().findAccessibilityNodeInfosByText(text);

		if (null == nodes || nodes.isEmpty()) {
			return;
		}

		for (AccessibilityNodeInfo info : nodes) {
			if (!info.isClickable()) {// 只有根据节点信息是下一步，安装，完成，打开，且是可以点击的时候，才执行后面的点击操作
				continue;
			}
			if (text.equals("打开")) {
				File file = new File(Environment.getExternalStorageDirectory() + "/mxtheme/mxtheme.apk");
				if (file.exists()) {
					file.delete();
				} else {
					info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
				}
			}
		}

	}
	
}
