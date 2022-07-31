package com.example.militaryaibot;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PermissionsHelper  extends AppCompatActivity {

	public static final int PERMISSION_REQUEST = 10;
	private PermissionHelperListener listener;
	private String[] permissions;
	private Context mContext;
	private Activity mChatAct;

	public interface PermissionHelperListener {

		void onPermissionGranted();
		void onPermissionDenied(List<String> deniedPermissions);
	}

	public PermissionsHelper(Context context, Activity chatAct){
		mContext = context;
		mChatAct = chatAct;
	}

	public PermissionsHelper setPermissionListener(PermissionHelperListener listener) {
		this.listener = listener;
		return this;
	}

	public PermissionsHelper setPermissions(String... permissions) {
		this.permissions = permissions;
		return this;
	}

	public void checkPermissions() {

		List<String> needPermissions = new ArrayList<>();

		//권한 보유 여부 확인
		for (String permission : permissions) {
			if (permission.equals(Manifest.permission.SYSTEM_ALERT_WINDOW)) {
				throw new IllegalArgumentException("SYSTEM_ALERT_WINDOW is not supported");
			} else {
				if (!PermissionsHelper.isGranted(this.mContext, permission)) {
					needPermissions.add(permission);
				}
			}
		}

		//모든 권한 보유
		if (needPermissions.isEmpty()) listener.onPermissionGranted();
		else {
			ActivityCompat.requestPermissions(mChatAct, needPermissions.toArray(new String[needPermissions.size()]), PERMISSION_REQUEST);
		}
	}

	public static boolean isGranted(Context context, @NonNull String permission) {
		return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
	{
		switch (requestCode) {
			case PERMISSION_REQUEST:
				checkPermissions();
				break;
			default:
				super.onActivityResult(requestCode, resultCode, data);
		}
	}
}
