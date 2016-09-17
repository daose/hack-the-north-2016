package com.daose.hackthenorth;

import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Environment;
import android.os.FileObserver;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;

public class UClipService extends Service {
    private static final String TAG = UClipService.class.getSimpleName();

    private FirebaseDatabase db;
    private DatabaseReference ref;

    private FileObserver ssObserver;

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

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(clipboard.hasPrimaryClip()) {
                    ClipData data = ClipData.newPlainText("copy", dataSnapshot.getValue(String.class));
                    clipboard.setPrimaryClip(data);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled");
            }
        });

        File ssDirectory = new File(Environment.getExternalStorageDirectory() + "/Pictures/Screenshots/");
        ssDirectory.mkdirs();

        ssObserver = new FileObserver(ssDirectory.toString()) {
            @Override
            public void onEvent(int event, String path) {
                if(event == FileObserver.CREATE) {
                    Log.d(TAG, "event: " + event + " path: " + path);
                }
            }
        };
        ssObserver.startWatching();
    }

    @Override
    public void onDestroy() {
        ssObserver.stopWatching();
    }
}
