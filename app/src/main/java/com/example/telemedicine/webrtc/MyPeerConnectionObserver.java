package com.example.telemedicine.webrtc;

import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.RtpReceiver;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoTrack;

public class MyPeerConnectionObserver implements PeerConnection.Observer {

    private VideoTrack remoteVideoTrack;
    private SurfaceViewRenderer remoteSurfaceView; // Add this in the constructor if needed

    public MyPeerConnectionObserver(SurfaceViewRenderer remoteSurfaceView) {
        this.remoteSurfaceView = remoteSurfaceView;
    }

    @Override
    public void onAddStream(MediaStream mediaStream) {
        if (mediaStream.videoTracks.size() > 0) {
            remoteVideoTrack = mediaStream.videoTracks.get(0);
            remoteVideoTrack.addSink(remoteSurfaceView);  // Render the remote stream to the remote view
        }
    }

    @Override
    public void onIceCandidate(IceCandidate iceCandidate) {
        // Handle ICE candidates
    }

    @Override
    public void onSignalingChange(PeerConnection.SignalingState signalingState) {}

    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {}

    @Override
    public void onIceConnectionReceivingChange(boolean b) {}

    @Override
    public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {}

    @Override
    public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {}

    @Override
    public void onRemoveStream(MediaStream mediaStream) {}

    @Override
    public void onDataChannel(DataChannel dataChannel) {}

    @Override
    public void onRenegotiationNeeded() {}

    @Override
    public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {}
}
