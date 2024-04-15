package com.arny.flightlogbook.data.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;

import okhttp3.ResponseBody;

public class FileUtils {
    public static final String SDCARD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
    private static final String DOCUMENT_SEPARATOR = ":";
    private static final String FOLDER_SEPARATOR = File.separator;
    private static final String DEFAULT_SCHEME = "file";

    public static ArrayList<File> getFolderFiles(final File folder) {
        ArrayList<File> files = new ArrayList<>();
        if (folder.listFiles() != null && folder.listFiles().length > 0) {
            for (final File file : folder.listFiles()) {
                if (file.isDirectory()) {
                    getFolderFiles(file);
                } else {
                    files.add(file);
                }
            }
        }
        return files;
    }

    public static ArrayList<File> getDir(final File folder) {
        ArrayList<File> files = new ArrayList<>();
        if (folder.listFiles() != null && folder.listFiles().length > 0) {
            for (final File file : folder.listFiles()) {
                if (file.isDirectory()) {
                    files.add(file);
                    getFolderFiles(file);
                } else {
                    files.add(file);
                }
            }
        }
        return files;
    }

    public static String getDataDir(final Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).applicationInfo.dataDir;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getWorkDir(Context context) {
        try {
            return new File(context.getExternalFilesDir(null), "").getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState);
    }

    public static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(extStorageState);
    }

    public static boolean isFileExist(String fileName) {
        File file = new File(fileName);
        return file.exists() && file.isFile();
    }

    public static boolean isPathExist(String path) {
        File folder = new File(path);
        return folder.exists() || folder.mkdirs();
    }

    public static String getFilenameWithExtention(String path) {
        File file = new File(path);
        return file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf(File.separator) + 1);
    }

    public static String getFilePath(String path) {
        File file = new File(path);
        String absolutePath = file.getAbsolutePath();
        return absolutePath.substring(0, absolutePath.lastIndexOf(File.separator)) + File.separator;
    }

    public static String getFileExtension(String path) {
        File file = new File(path);
        String name = file.getName();
        try {
            return name.substring(name.lastIndexOf(".") + 1);
        } catch (Exception e) {
            return "";
        }
    }

    public static boolean writeToFile(Context context, String pathWithName, String data) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(pathWithName, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String readFromFile(Context context, String pathWithName) {
        String ret = "";
        try {
            InputStream inputStream = context.openFileInput(pathWithName);
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();
                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }
                inputStream.close();
                ret = stringBuilder.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static void createDirectories(String path) {
        File SDCardRoot;
        if (path.contains(Environment.getExternalStorageDirectory().toString())) {
            SDCardRoot = new File(path);
        } else {
            SDCardRoot = new File(SDCARD_PATH + path);
        }

        if (SDCardRoot.exists()) {
        } else {
            Log.e(FileUtils.class.getSimpleName(), "outcome for " + SDCardRoot.getAbsolutePath() + "     " + SDCardRoot.mkdirs());
        }
        File NoMedia = new File(SDCARD_PATH + path + "/.nomedia");
        if (!NoMedia.exists()) {
            try {
                NoMedia.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean renameFile(String SavePath, String filePath) {
        return new File(filePath).renameTo(new File(SavePath));
    }

    public static void moveFile(String inputPath, String inputFile, String outputPath) {
        InputStream in = null;
        OutputStream out = null;
        try {
            //create output directory if it doesn't exist
            File dir = new File(outputPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            in = new FileInputStream(inputPath + inputFile);
            out = new FileOutputStream(outputPath + inputFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file
            out.flush();
            out.close();
            out = null;

            // delete the original file
            new File(inputPath + inputFile).delete();
        } catch (Exception e) {
            Log.e("tag", e.getMessage());
        }
    }

    public static long getFolderSize(File folder) {
        long length = 0;
        folder.mkdirs();
        File[] files = folder.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                length += file.length();
            } else {
                length += getFolderSize(file);
            }
        }
        return length;
    }

    public static boolean deleteFile(File element) {
        element.mkdirs();
        if (element.isDirectory()) {
            for (File sub : element.listFiles()) {
                deleteFile(sub);
            }
        }
        return element.delete();
    }

    public static int getMediaType(String file) {
        try {
            String substring = file.substring(file.lastIndexOf("/.k/") + 4, file.lastIndexOf("/"));
            if (substring.equals(".Image")) {
                return 0;
            }
            if (substring.equals(".Video")) {
                return 1;
            }
            if (substring.equals(".Audio")) {
                return 2;
            }
            if (substring.equals(".Documents")) {
                return 3;
            }
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static String formatFileSize(long size) {
        if (size <= 0)
            return "0";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.##").format(size
                / Math.pow(1024, digitGroups))
                + " " + units[digitGroups];
    }

    public static String formatFileSize(long size, int digits) {
        if (size <= 0)
            return "0";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));

        StringBuilder digs = new StringBuilder();
        for (int i = 0; i < digits; i++) {
            digs.append("#");
        }
        return new DecimalFormat("#,##0." + digs.toString()).format(size
                / Math.pow(1024, digitGroups))
                + " " + units[digitGroups];
    }

    public static byte[] fileToBytes(File file) throws NullPointerException, IOException {
        if (file == null) {
            throw new NullPointerException("Not Found");
        }
        byte[] bytes = new byte[((int) file.length())];
        InputStream input = new FileInputStream(file);
        int iter = 0;
        while (iter < bytes.length) {
            int block = input.read(bytes, iter, bytes.length - iter);
            if (block < 0) {
                break;
            }
            iter += block;
        }
        input.close();
        return bytes;
    }

    public static File loadFile(String path) {
        File file = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            file = new File(path);
            inputStream = new FileInputStream(path);
            outputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        writeStream(inputStream, outputStream);
        return file;
    }

    private static void writeStream(InputStream inputStream, OutputStream outputStream) {
        try {
            int read;
            byte[] bytes = new byte[2048];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
            System.out.println("Done!");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static byte[] createChecksum(String filename) throws Exception {
        InputStream fis = new FileInputStream(filename);
        byte[] buffer = new byte[1024];
        MessageDigest complete = MessageDigest.getInstance("MD5");
        int numRead;
        do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);

        fis.close();
        return complete.digest();
    }

    public static String getMD5File(String filename) throws Exception {
        byte[] b = createChecksum(filename);
        String result = "";
        for (byte aB : b) {
            result += Integer.toString((aB & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

    public static byte[] readFile(File file) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(new
                FileInputStream(file));
        int bytes = (int) file.length();
        byte[] buffer = new byte[bytes];

        int readBytes = bis.read(buffer);
        bis.close();
        return buffer;
    }

    public static byte[] readBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        // Get the size of the file
        long length = file.length();
        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            throw new IOException("Could not completely read file " + file.getName() + " as it is too long (" + length + " bytes, max supported " + Integer.MAX_VALUE + ")");
        }
        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) length];
        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }
        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }
        // Close the input stream and return bytes
        is.close();
        return bytes;
    }

    public static boolean writeFile(byte[] data, String fileName) throws IOException {
        File file = new File(getFilePath(fileName));
        boolean folderFilesExist = file.exists() || file.mkdirs();
        if (!folderFilesExist) {
            return false;
        }
        createNomedia(fileName);
        FileOutputStream out = new FileOutputStream(fileName, false);
        out.write(data);
        out.close();
        return true;
    }

    private static void createNomedia(String path) {
        File NoMedia = new File(getFilePath(path) + File.separator + ".nomedia");
        if (!NoMedia.exists()) {
            try {
                NoMedia.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void writeBytesToFile(byte[] bytes, String path) throws IOException {
        if (!isPathExist(path)) {
            return;
        }
        createNomedia(path);
        File file = new File(path);
        file.createNewFile();
        BufferedOutputStream bos = null;
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bytes);
        } finally {
            if (bos != null) {
                try {
                    //flush and close the BufferedOutputStream
                    bos.flush();
                    bos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static Bitmap bytesToBitmap(byte[] bytes) {
        try {
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressLint("NewApi")
    public static String getPublicFilePath(int mediaType, String filename) {
        String dir;
        dir = FileUtils.class.getSimpleName();
        if (mediaType == 0) {
            dir = Environment.DIRECTORY_PICTURES;
        } else if (mediaType == 1) {
            dir = Environment.DIRECTORY_MOVIES;
        } else if (mediaType == 2) {
            dir = Environment.DIRECTORY_MUSIC;
        } else if (mediaType == 3) {
            dir = Environment.DIRECTORY_DOCUMENTS;
        } else {
            dir = Environment.DIRECTORY_PICTURES;
        }
        return Environment.getExternalStoragePublicDirectory(dir).toString() + "/" + filename;
    }

    public static boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            if (files == null) {
                return true;
            }
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        return path.delete();
    }

    public static File saveResponseBodyToDisk(ResponseBody body, File file) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = body.byteStream();
            outputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        writeStream(inputStream, outputStream);
        return file;
    }

    // Copy an InputStream to a File.
    public static boolean saveFile(InputStream in, File file) {
        OutputStream out = null;
        boolean result;
        try {
            out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        } finally {
            // Ensure that the InputStreams are closed even if there's an exception.
            try {
                if (out != null) {
                    out.close();
                }
                // If you want to close the "in" InputStream yourself then remove this
                // from here but ensure that you close it yourself eventually.
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static void copyFile(String inputPath, String inputFile, String outputPath) {
        InputStream in = null;
        OutputStream out = null;
        try {
            //create output directory if it doesn't exist
            File dir = new File(outputPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            in = new FileInputStream(inputPath + inputFile);
            out = new FileOutputStream(outputPath + inputFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file (You have now copied the file)
            out.flush();
            out.close();
            out = null;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static HashMap<String, String> getMediaData(Context context, Uri MediaUri) {
        Cursor mediaCursor = context.getContentResolver().query(MediaUri, null, null, null, null);
        HashMap<String, String> data = new HashMap<>();
        if (mediaCursor != null && mediaCursor.moveToFirst()) {
            do {
                for (int i = 0; i < mediaCursor.getColumnCount(); i++) {
                    data.put(mediaCursor.getColumnName(i), mediaCursor.getString(i));
                }
            } while (mediaCursor.moveToNext());
            mediaCursor.close();
        }
        return data;
    }

    public static byte[] getInpitStreamBytes(InputStream is) {
        try {
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            return buffer;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getMediaMeta(String filePath, int metaType) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(filePath);
        return retriever.extractMetadata(metaType);
    }

    public String getFileName(Uri uri, Context context) {
        if (uri.getScheme().equals("content")) {
            String fileNameFromContentUri = tryGetFileNameFromContentUri(uri, context);
            if (fileNameFromContentUri != null && !fileNameFromContentUri.isEmpty())
                return fileNameFromContentUri;
        }
        String name = uri.getLastPathSegment();
        String fileSeparator = File.separator;
        if (!name.isEmpty()) {
            if (name.contains(fileSeparator)) {
                int nameStartIndex = name.lastIndexOf(fileSeparator);
                name = name.substring(nameStartIndex + 1);
            }
            name = name.replaceAll(":", "");

            if (!DEFAULT_SCHEME.equals(uri.getScheme())) {
                String ext = "." + MimeTypeMap.getSingleton()
                        .getExtensionFromMimeType(context.getContentResolver().getType(uri));
                if (!name.contains(ext)) name = name + ext;
            }
        }
        return name;
    }

    @SuppressLint("Range")
    @Nullable
    private String tryGetFileNameFromContentUri(Uri uri, Context context) {
        try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Uri getFileUri(Context context, File file) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                String authority = context.getApplicationContext().getPackageName() + ".provider";
                return FileProvider.getUriForFile(context, authority, file);
            } else {
                return Uri.fromFile(file);
            }
        } catch (Exception e) {
            return Uri.fromFile(file);
        }
    }

    @NonNull
    public static Boolean writeToFile(String content, String path, String filename, boolean append) {
        File file = new File(path);
        file.mkdirs();
        File writeFile = new File(file, filename);
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(writeFile, append));
            bufferedWriter.write(content);
            bufferedWriter.close();
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Чтение файлов assets,в папке
     *
     * @param folder
     * @return
     */
    public static ArrayList<String> listAssetFiles(Context context, String folder) {
        ArrayList<String> fileNames = new ArrayList<>();
        if (context == null) {
            return null;
        }
        try {
            String[] files = context.getAssets().list(folder);
            Collections.addAll(fileNames, files);
            return fileNames;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static File loadFileFromAssets(Context context, String path) {
        File file = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            file = new File(path);
            inputStream = context.getAssets().open(path);
            outputStream = new FileOutputStream(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        writeStream(Objects.requireNonNull(inputStream), outputStream);
        return file;
    }

    public static String loadDataFromAssets(Context context, String path) {
        String tContents = "";
        InputStream stream;
        try {
            stream = context.getAssets().open(path);
            int size = stream.available();
            byte[] buffer = new byte[size];
            stream.read(buffer);
            stream.close();
            tContents = new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tContents;
    }
}
