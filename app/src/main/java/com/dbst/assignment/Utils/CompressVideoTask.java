/*
 * Copyright (c) 2016 VDROP OPERATIONS PTE LTD
 */
package com.dbst.assignment.Utils;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.dbst.assignment.application.MyApplication;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;


/**
 * Asyn Task class to compress the video by using ffmpeg command
 */

public class CompressVideoTask extends AsyncTask<Void, Void, String> {

    private String filePath;
    private final Context context;
    private VideoCompressResponse compressResponse = null;
    private String outputPath = MyApplication.getInstance().getCompressedClip();

    public CompressVideoTask(Context context) {
        this.context = context;
    }

    /**
     * @param filePath
     * @param compressResponse
     */
    public void compressVideo(String filePath, VideoCompressResponse compressResponse) {
        this.filePath = filePath;
        this.compressResponse = compressResponse;
    }

    @Override
    protected String doInBackground(Void... params) {

        final String TAG = "Compress Video";
        Log.i("IN", TAG);

        //command for compressing the video and save in the storage
        //ab: audio bitrate, ac: to set the audio channels, ar: audio sampling rate
        String[] complexCommand = {"-y", "-i", filePath, "-strict", "experimental", "-s", "640x480",
                "-r", "25", "-vcodec", "mpeg4", "-b", "800k", "-ab", "48000", "-ac", "2", "-ar",
                "22050", outputPath};

        FFmpeg ffmpeg = FFmpeg.getInstance(context);
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler());
            ffmpeg.execute(complexCommand, new ExecuteBinaryResponseHandler() {

                @Override
                public void onStart() {
                    Log.i(TAG, Constants.START);
                    compressResponse.onCompressResponse(Constants.START, outputPath);
                }

                @Override
                public void onProgress(String message) {
                    Log.i(TAG, Constants.PROGRESS);
                    compressResponse.onCompressResponse(Constants.PROGRESS, outputPath);
                }

                @Override
                public void onFailure(String message) {
                    Log.i(TAG, Constants.FAILURE + message);
                    compressResponse.onCompressResponse(Constants.FAILURE, outputPath);
                }

                @Override
                public void onSuccess(String message) {
                    Log.i(TAG, Constants.SUCCESS);
                    compressResponse.onCompressResponse(Constants.SUCCESS, outputPath);
                }

                @Override
                public void onFinish() {
                    Log.i(TAG, Constants.FINISH);
                    compressResponse.onCompressResponse(Constants.FINISH, outputPath);
                }
            });

        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
        } catch (FFmpegCommandAlreadyRunningException e) {
            Log.e("ffmpegAlreadyRunning", e.getMessage());
            ffmpeg.killRunningProcesses();
        }
        return outputPath;
    }

    @Override
    protected void onPostExecute(String response) {
    }

    public interface VideoCompressResponse {
        void onCompressResponse(String response, String compressedFilePath);
    }


}

