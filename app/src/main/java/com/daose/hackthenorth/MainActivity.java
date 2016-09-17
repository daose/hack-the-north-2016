package com.daose.hackthenorth;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView view = (TextView) findViewById(R.id.text);

        final ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        clipboard.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {
            @Override
            public void onPrimaryClipChanged() {
                String copiedText = "";
                if (clipboard.hasPrimaryClip()) {
                    ClipData data = clipboard.getPrimaryClip();
                    if (data.getItemCount() > 0) {
                        copiedText = data.getItemAt(0).coerceToText(MainActivity.this).toString();
                    }
                }
                view.setText(copiedText);
            }
        });

    }
}
