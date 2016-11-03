package xyz.monkeytong.hongbao.services;

import android.accessibilityservice.AccessibilityService;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

import xyz.monkeytong.hongbao.utils.AccessibilityHelper;
import xyz.monkeytong.hongbao.utils.LogUtils;

public class AddFriendService extends AccessibilityService {

	private static final String TAG = AddFriendService.class.getSimpleName();
	private static final String PHONE_NUMBER = "18600949782";

	public static boolean success;

	@Override
	protected void onServiceConnected() {
		super.onServiceConnected();
		LogUtils.i(TAG, "onServiceConnected");
	}

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		try {
			if (success) {
				return;
			}
			String pkn = String.valueOf(event.getPackageName());
			if (!"com.tencent.mm".equals(pkn)) {
				return;
			}
			String className = event.getClassName() + "";
			LogUtils.i(TAG, "" + className);
			AccessibilityNodeInfo note = event.getSource();
			switch (className) {
				case "android.app.Notification":
					Intent intent = getPackageManager().getLaunchIntentForPackage("com.tencent.mm");
//					Intent intent = new Intent("com.tencent.mm/com.tencent.mm.plugin.search.ui.FTSMainUI");
//					intent.setComponent(new ComponentName("com.tencent.mm", "com.tencent.mm.plugin.search.ui.FTSMainUI"));
//					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
					break;
				case "com.tencent.mm.ui.LauncherUI": {
					int count = note.getChildCount();
					for (int i = 0; i < count; i++) {
						AccessibilityNodeInfo child = note.getChild(i);
						search(child);
					}
				}
				case "com.tencent.mm.plugin.profile.ui.ContactInfoUI":
				case "com.tencent.mm.plugin.search.ui.FTSMainUI": {
					int count = note.getChildCount();
					for (int i = 0; i < count; i++) {
						AccessibilityNodeInfo child = note.getChild(i);
						inputText(child, event);
					}
				}
				break;
				case "com.tencent.mm.plugin.profile.ui.SayHiWithSnsPermissionUI": {
					int count = note.getChildCount();
					for (int i = 0; i < count; i++) {
						AccessibilityNodeInfo child = note.getChild(i);
						inputText(child, event);
					}
				}
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void search(AccessibilityNodeInfo child) {
		if (child.getChildCount() != 0) {
			for (int i = 0; i < child.getChildCount(); i++) {
				AccessibilityNodeInfo noteInfo = child.getChild(i);
				if (noteInfo != null) {
					if ("android.widget.TextView".equals(noteInfo.getClassName())) {
						if (noteInfo.getParent() != null) {
							if ("android.view.View".equals(noteInfo.getParent().getClassName())) {
								AccessibilityHelper.performClick(noteInfo);
							}
						}
					}
				}
				search(noteInfo);
			}
		}
	}

	boolean input = false;

	private void inputText(AccessibilityNodeInfo child, AccessibilityEvent event) {
		if (child.getChildCount() != 0) {
			for (int i = 0; i < child.getChildCount(); i++) {
				AccessibilityNodeInfo noteInfo = child.getChild(i);
				if ("android.widget.EditText".equals(noteInfo.getClassName())) {
					Bundle arguments = new Bundle();
					arguments.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_MOVEMENT_GRANULARITY_INT,
									AccessibilityNodeInfo.MOVEMENT_GRANULARITY_WORD);
					arguments.putBoolean(
									AccessibilityNodeInfo.ACTION_ARGUMENT_EXTEND_SELECTION_BOOLEAN,
									true);
					noteInfo.performAction(
							AccessibilityNodeInfo.ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY,
							arguments);
					ClipData clip = ClipData.newPlainText("label", PHONE_NUMBER);
					ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
					clipboardManager.setPrimaryClip(clip);
					noteInfo.performAction(AccessibilityNodeInfo.ACTION_PASTE);
					input = true;
				}
				if (input = true) {
					findNodesByText(event, "查找手机/QQ号:" + PHONE_NUMBER);
				}
				findNodesByText(event, "添加到通讯录");
				findNodesByText(event, "发送");
				inputText(noteInfo, event);
			}
		}
	}

	@Override
	public void onInterrupt() {
		LogUtils.i(TAG, "onInterrupt");
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}

	/**
	 * 根据文字寻找节点
	 *
	 * @param event
	 * @param text
	 *            文字
	 */
	private void findNodesByText(AccessibilityEvent event, String text) {
		List<AccessibilityNodeInfo> nodes = event.getSource()
				.findAccessibilityNodeInfosByText(text);
		if (nodes != null && !nodes.isEmpty()) {
			for (AccessibilityNodeInfo info : nodes) {
				if (info.getParent() != null && info.getParent().isClickable()) {
					info.getParent().performAction(
							AccessibilityNodeInfo.ACTION_CLICK);
				}
				if (info.isClickable()) {
					info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
				}
				if("发送".equals(text)){
					success = true;
				}
			}
		}

	}


}
