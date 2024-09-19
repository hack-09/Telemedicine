package com.example.telemedicine.util;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.jitsi.meet.sdk.JitsiMeetActivity;
import java.net.URL;

import timber.log.Timber;

public class JitsiUtils {

    public static void startJitsiMeeting(Context context, String roomId) {
        try {
            Timber.tag("TAG").i("RoomId : %s", roomId);
            URL serverURL = new URL("https://meet.jit.si"); // or your own Jitsi server
            JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                    .setServerURL(serverURL)
                    .setRoom(roomId)
                    .setAudioMuted(true)
                    .setVideoMuted(true)
                    .setAudioOnly(false)
                    .build();
            JitsiMeetActivity.launch(context, options);
        } catch (Exception e) {
            Toast.makeText(context, "Error starting Jitsi meeting", Toast.LENGTH_SHORT).show();
        }
    }
}
