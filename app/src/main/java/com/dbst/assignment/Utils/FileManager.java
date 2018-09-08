package com.dbst.assignment.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import com.facebook.android.crypto.keychain.AndroidConceal;
import com.facebook.android.crypto.keychain.SharedPrefsBackedKeyChain;
import com.facebook.crypto.Crypto;
import com.facebook.crypto.CryptoConfig;
import com.facebook.crypto.Entity;
import com.facebook.crypto.keychain.KeyChain;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class FileManager {


    /*
     * return file name from url
     * */
    public static String getFileNameFromUrl(String imageName) throws MalformedURLException {
        try{

            URL url = new URL(imageName);
            String urlString = url.getFile();
            String fileName = urlString.substring(urlString.lastIndexOf('/') + 1).split("\\?")[0].split("#")[0];
            String file = fileName.substring(0, fileName.lastIndexOf('.'));
            return file;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }


    /*
     * Save images in appropriate directories
     * */
    public static File saveImages(Context context, Bitmap bitmap, String imageName, File storeDirName) throws IOException {
        File rootDir = new File(Environment.getExternalStorageDirectory()
                + File.separator + "/.DBST");
        File channelIdDir = new File(rootDir, "/." + "DBSTImages");
        boolean rootfolderexist = rootDir.exists();
        boolean channelFolderExist = channelIdDir.exists();
        if (!rootfolderexist) {
            try {
                if (Environment.getExternalStorageDirectory().canWrite()) {
                    rootDir.mkdirs();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!channelFolderExist) {
            try {
                if (Environment.getExternalStorageDirectory().canWrite()) {
                    channelIdDir.mkdirs();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        rootDir.mkdirs();
        channelIdDir.mkdirs();
        String image = null;
        KeyChain keyChain = new SharedPrefsBackedKeyChain(context, CryptoConfig.KEY_256);
        Crypto crypto = AndroidConceal.get().createDefaultCrypto(keyChain);
        Log.d("Crypto",""+crypto);
        try {
            image = getImageName(FileManager.getFileNameFromUrl(imageName));
            // Log.d("Firestore-ImageNames", "-----" + getImageName(FileManager.getFileNameFromUrl(imageName)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        File file = new File(storeDirName, image);
        if (file.exists())
            return null;
        try {
            FileOutputStream out = new FileOutputStream(file);
            OutputStream fileStream = new BufferedOutputStream(out);
            Log.d("File-Stream",""+fileStream);
            // Creates an output stream which encrypts the data as
            // it is written to it and writes it out to the file.
            OutputStream outputStream = crypto.getCipherOutputStream(
                    fileStream,
                    Entity.create("entity_id"));
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);

            // Write plaintext to it.
            outputStream.write(stream.toByteArray());
            outputStream.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
     * get images names with extension of .jpg
     * */
    public static String getImageName(String imageName) {
        String fileName = imageName + ".jpg";
        return fileName;
    }


    /*
     * get the files from appropriate Directories
     * */
    public static ArrayList<File> getFileFromSDCard(String channelId, String specificDirName) {
        File[] listFile;
        ArrayList<File> getOfflinePaths = new ArrayList<>();
        File file = new File(Environment.getExternalStorageDirectory()
                + File.separator + "/.VDROPFan" + "/." + channelId + "/." + specificDirName);
        if (file.isDirectory()) {
            listFile = file.listFiles();
            for (int i = 0; i < listFile.length; i++) {
                getOfflinePaths.add(listFile[i].getAbsoluteFile());
            }
        }
        Log.d("Firestore-FilePaths", "" + getOfflinePaths.size());
        return getOfflinePaths;

    }


    /*
     * return File from each ChannelId and appropriate Folder.
     * */

    public static File getDirectoryName(String directoryName) {
        File rootDir = new File(Environment.getExternalStorageDirectory()
                + File.separator + "/.DBST");
        File channelIdDir = new File(rootDir, "/." + "DBSTImages");
        return channelIdDir;
    }


    // convert Bitmap to bytes
    private static byte[] bitmapToBytes(Bitmap photo) {

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }



}


