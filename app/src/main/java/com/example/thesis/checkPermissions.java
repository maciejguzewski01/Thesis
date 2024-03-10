package com.example.thesis;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class checkPermissions extends AppCompatActivity {
    private boolean isPermissionGranted[];
    private String permissions[];
    private Context context;

    public checkPermissions(Context ctx)
    {
        context = ctx;

        isPermissionGranted = new boolean[6];
        permissions = new String[6];

        permissions[0]= android.Manifest.permission.BLUETOOTH;
        permissions[1]= android.Manifest.permission.BLUETOOTH_ADMIN;
        permissions[2]= android.Manifest.permission.ACCESS_COARSE_LOCATION;
        permissions[3]= android.Manifest.permission.BLUETOOTH_CONNECT;
        permissions[4]= android.Manifest.permission.BLUETOOTH_SCAN;
        permissions[5]= android.Manifest.permission.POST_NOTIFICATIONS;
        //tu dodawac wiecej uprawnien

        for(int i=0;i<isPermissionGranted.length;i++)
        {
            isPermissionGranted[i]= ActivityCompat.checkSelfPermission(context, permissions[i]) == PackageManager.PERMISSION_GRANTED;
        }

    }

    public boolean checkAllPermissions()
    {
        for(int i=0;i<isPermissionGranted.length;i++)
        {
            if(!isPermissionGranted[i]) return false;
        }

        return true;
    }

    public boolean allPermissionsGranted()
    {
        if(checkAllPermissions()==true) return true;
        ActivityCompat.requestPermissions((Activity) context, permissions, 0);
        return checkAllPermissions();
    }


}
