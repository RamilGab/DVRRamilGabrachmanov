package com.home.dvr;

import java.io.IOException;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.view.Surface;

public class VideoRecAndSetings {
	MediaRecorder recorder;

	public VideoRecAndSetings() {
	}

	public void newV() {
		recorder = new MediaRecorder();
	}

	public void died() {
		if (recorder != null) {
			recorder.release();
			recorder = null;
		}
	}

	public void start(String filename) {
		recorder.setOutputFile(filename);

		try {
			recorder.prepare();
			recorder.start();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void stop() {
		recorder.stop();
		recorder.reset();
	}

	public void setPreview(Surface surface) {
		recorder.setPreviewDisplay(surface);
	}

	public void setCamera(Camera camera) {
		recorder.setCamera(camera);
	}

	public void setRecorderParams(int a, int b, int c, int d, int e, int w,
			int h, int f, int g) {
		recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
		recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		recorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);

		recorder.setVideoEncodingBitRate(a);
		recorder.setAudioEncodingBitRate(b);
		recorder.setAudioSamplingRate(c);
		recorder.setAudioChannels(d);
		recorder.setVideoFrameRate(e);
		recorder.setVideoSize(w, h);
		recorder.setMaxDuration(f);
		recorder.setMaxFileSize(g);
	}
}
