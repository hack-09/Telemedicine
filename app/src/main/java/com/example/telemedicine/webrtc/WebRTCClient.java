package com.example.telemedicine.webrtc;

import android.content.Context;
import com.example.telemedicine.utils.DataModel;
import com.example.telemedicine.utils.DataModelType;
import com.google.gson.Gson;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.List;

public class WebRTCClient {

    private final Gson gson = new Gson();
    private final Context context;
    private final String username;
    private EglBase.Context eglBaseContext = EglBase.create().getEglBaseContext();
    private PeerConnectionFactory peerConnectionFactory;
    private PeerConnection peerConnection;
    private List<PeerConnection.IceServer> iceServers = new ArrayList<>();
    private CameraVideoCapturer videoCapturer;
    private VideoSource localVideoSource;
    private AudioSource localAudioSource;
    private String localTrackId = "local_track";
    private VideoTrack localVideoTrack, remoteVideoTrack;
    private AudioTrack localAudioTrack;
    private MediaConstraints mediaConstraints = new MediaConstraints();

    public Listener listener;

    public WebRTCClient(Context context, PeerConnection.Observer observer, String username) {
        this.context = context;
        this.username = username;
        initPeerConnectionFactory();
        peerConnectionFactory = createPeerConnectionFactory();
        iceServers.add(PeerConnection.IceServer.builder("turn:a.relay.metered.ca:443?transport=tcp")
                .setUsername("83eebabf8b4cce9d5dbcb649")
                .setPassword("2D7JvfkOQtBdYW3R")
                .createIceServer());
        peerConnection = createPeerConnection(observer);
        localVideoSource = peerConnectionFactory.createVideoSource(false);
        localAudioSource = peerConnectionFactory.createAudioSource(new MediaConstraints());
        mediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
    }

    private void initPeerConnectionFactory() {
        PeerConnectionFactory.InitializationOptions options = PeerConnectionFactory.InitializationOptions.builder(context)
                .setFieldTrials("WebRTC-H264HighProfile/Enabled/")
                .setEnableInternalTracer(true)
                .createInitializationOptions();
        PeerConnectionFactory.initialize(options);
    }

    private PeerConnectionFactory createPeerConnectionFactory() {
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        return PeerConnectionFactory.builder()
                .setVideoEncoderFactory(new DefaultVideoEncoderFactory(eglBaseContext, true, true))
                .setVideoDecoderFactory(new DefaultVideoDecoderFactory(eglBaseContext))
                .setOptions(options)
                .createPeerConnectionFactory();
    }

    private PeerConnection createPeerConnection(PeerConnection.Observer observer) {
        return peerConnectionFactory.createPeerConnection(iceServers, observer);
    }

    public void initSurfaceViewRenderer(SurfaceViewRenderer view) {
        view.init(eglBaseContext, null);
        view.setMirror(true);
        view.setEnableHardwareScaler(true);
    }

    public void initLocalSurfaceView(SurfaceViewRenderer view) {
        initSurfaceViewRenderer(view);
        startLocalVideoStreaming(view);
    }

    private void startLocalVideoStreaming(SurfaceViewRenderer view) {
        SurfaceTextureHelper helper = SurfaceTextureHelper.create(Thread.currentThread().getName(), eglBaseContext);
        videoCapturer = getVideoCapturer();
        videoCapturer.initialize(helper, context, localVideoSource.getCapturerObserver());
        videoCapturer.startCapture(480, 360, 15);
        localVideoTrack = peerConnectionFactory.createVideoTrack(localTrackId + "_video", localVideoSource);
        localVideoTrack.addSink(view);

        localAudioTrack = peerConnectionFactory.createAudioTrack(localTrackId + "_audio", localAudioSource);
        peerConnection.addTrack(localVideoTrack);
        peerConnection.addTrack(localAudioTrack);
    }

    private CameraVideoCapturer getVideoCapturer() {
        Camera2Enumerator enumerator = new Camera2Enumerator(context);
        String[] deviceNames = enumerator.getDeviceNames();
        for (String device : deviceNames) {
            if (enumerator.isFrontFacing(device)) {
                return enumerator.createCapturer(device, null);
            }
        }
        throw new IllegalStateException("Front-facing camera not found");
    }

    public void initRemoteSurfaceView(SurfaceViewRenderer view) {
        initSurfaceViewRenderer(view);
        if (remoteVideoTrack != null) {
            remoteVideoTrack.addSink(view);
        }
    }

    public void call(String target) {
        peerConnection.createOffer(new MySdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                peerConnection.setLocalDescription(new MySdpObserver() {
                    @Override
                    public void onSetSuccess() {
                        listener.onTransferDataToOtherPeer(new DataModel(target, username, sessionDescription.description, DataModelType.Offer));
                    }
                }, sessionDescription);
            }
        }, mediaConstraints);
    }

    public void answer(String target) {
        peerConnection.createAnswer(new MySdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                peerConnection.setLocalDescription(new MySdpObserver() {
                    @Override
                    public void onSetSuccess() {
                        listener.onTransferDataToOtherPeer(new DataModel(target, username, sessionDescription.description, DataModelType.Answer));
                    }
                }, sessionDescription);
            }
        }, mediaConstraints);
    }

    public void onRemoteSessionReceived(SessionDescription sessionDescription) {
        peerConnection.setRemoteDescription(new MySdpObserver(), sessionDescription);
    }

    public void addIceCandidate(IceCandidate iceCandidate) {
        peerConnection.addIceCandidate(iceCandidate);
    }

    public void sendIceCandidate(IceCandidate iceCandidate, String target) {
        listener.onTransferDataToOtherPeer(new DataModel(target, username, gson.toJson(iceCandidate), DataModelType.IceCandidate));
    }

    public void switchCamera() {
        videoCapturer.switchCamera(null);
    }

    public void toggleVideo(boolean shouldBeMuted) {
        localVideoTrack.setEnabled(!shouldBeMuted);
    }

    public void toggleAudio(boolean shouldBeMuted) {
        localAudioTrack.setEnabled(!shouldBeMuted);
    }

    public void closeConnection() {
        if (localVideoTrack != null) {
            localVideoTrack.dispose();
        }
        if (localAudioTrack != null) {
            localAudioTrack.dispose();
        }
        if (videoCapturer != null) {
            try {
                videoCapturer.stopCapture();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            videoCapturer.dispose();
        }
        if (peerConnection != null) {
            peerConnection.close();
        }
    }

    public interface Listener {
        void onTransferDataToOtherPeer(DataModel model);
    }
}
