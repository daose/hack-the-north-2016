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

    private String copiedText = "";

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
                copiedText = "";
                if (clipboard.hasPrimaryClip()) {
                    ClipData data = clipboard.getPrimaryClip();
                    if (data.getItemCount() > 0) {
                        copiedText = data.getItemAt(0).coerceToText(UClipService.this).toString();
                    }
                }
                Log.d(TAG, "copiedText: " + copiedText);
                ref.setValue(copiedText, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        Log.d(TAG, "onComplete");
                    }
                });
            }
        });

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (clipboard.hasPrimaryClip()) {
                    Log.d(TAG, "onDataChange: " + dataSnapshot.getValue(String.class));
                    ClipData data = ClipData.newPlainText("test", dataSnapshot.getValue(String.class));
                    if (!data.getItemAt(0).coerceToText(UClipService.this).equals(copiedText)) {
                        Log.d(TAG, "data: " + data.toString());
                        clipboard.setPrimaryClip(data);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled");
            }
        });

        //ss directory - screenshot
        final File ssDirectory = new File(Environment.getExternalStorageDirectory() + "/Pictures/Screenshots/");
        ssDirectory.mkdirs();

        ssObserver = new FileObserver(ssDirectory.toString()) {
            //move, create, delete
            @Override
            public void onEvent(int event, String path) {
                if (event == FileObserver.CLOSE_WRITE) {
                    Uri file = Uri.fromFile(new File(ssDirectory.getAbsolutePath() + "/" + path));
                    UploadTask uploadTask = ssRef.putFile(file);
                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        //only when the file gets uploaded successfully
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Uri imgDownloadUri = taskSnapshot.getDownloadUrl();
                            if (imgDownloadUri != null) {
                                Log.d(TAG, "download url: " + imgDownloadUri.toString());
                                ref.setValue(imgDownloadUri.toString(), new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        Log.d(TAG, "onComplete image");
                                    }
                                });
                            }
                        }
                    });
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
