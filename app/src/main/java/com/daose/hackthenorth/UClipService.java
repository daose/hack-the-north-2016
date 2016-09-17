package com.daose.hackthenorth;

import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.FileObserver;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

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
        //connects to Firebase
        db = FirebaseDatabase.getInstance();
        //reference to copy
        ref = db.getReference("copy");
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://uclip-e2537.appspot.com");

        //reference
        final StorageReference ssRef = storageRef.child("screenshot");

        //Get the system services of clipboard
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
                if (clipboard.hasPrimaryClip()) {
                    ClipData data = ClipData.newPlainText("copy", dataSnapshot.getValue(String.class));
                    clipboard.setPrimaryClip(data);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled");
            }
        });

        final File ssDirectory = new File(Environment.getExternalStorageDirectory() + "/Pictures/Screenshots/");
        ssDirectory.mkdirs();
//ss directory - screenshot
        ssObserver = new FileObserver(ssDirectory.toString()) {
            //move, create, delete
            @Override
            public void onEvent(int event, String path) {

                if (event == FileObserver.CREATE) {
                    Uri file = Uri.fromFile(new File(ssDirectory.getAbsolutePath() + path));
                    UploadTask uploadTask = ssRef.putFile(file);
                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        //only when the file gets uploaded successfully
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Uri imgdownload = taskSnapshot.getDownloadUrl();
                            ref.setValue(imgdownload.toString());
                        }
                    });
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
