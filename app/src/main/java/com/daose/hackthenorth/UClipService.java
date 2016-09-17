package com.daose.hackthenorth;

import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UClipService extends Service {

    private FirebaseDatabase db;
    private DatabaseReference ref;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //TODO do something useful
        return Service.START_STICKY;
    }

    @Override
    public void onCreate() {
        db = FirebaseDatabase.getInstance();
        ref = db.getReference("copy");

        final ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        clipboard.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {
            @Override
            public void onPrimaryClipChanged() {
                String copiedText = "";
                if (clipboard.hasPrimaryClip()) {
                    ClipData data = clipboard.getPrimaryClip();
                    if (data.getItemCount() > 0) {
                        copiedText = data.getItemAt(0).coerceToText(UClipService.this).toString();
                    }
                }
                ref.setValue(copiedText);
            }
        });

    }

    @Override
    public void onDestroy() {

    }
}
