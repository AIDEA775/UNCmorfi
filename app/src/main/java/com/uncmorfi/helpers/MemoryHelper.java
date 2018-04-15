package com.uncmorfi.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public abstract class MemoryHelper {

    public static void saveToStorage(Context context, String file, String content) {
        if (content != null) {
            try {
                OutputStreamWriter out =
                        new OutputStreamWriter(
                                context.openFileOutput(file, Context.MODE_PRIVATE));

                out.write(content);
                out.close();
                Log.i("MemoryHelper", "Written " + file + " success");
            } catch (IOException ex) {
                Log.e("MemoryHelper", "Error writing in internal memory");
            }
        }
    }

    public static String readStringFromStorage(Context context, String file) {
        try {
            BufferedReader in =
                    new BufferedReader(
                            new InputStreamReader(context.openFileInput(file)));
            String line;
            StringBuilder read = new StringBuilder();

            while((line = in.readLine()) != null)
                read.append(line);

            in.close();
            return read.toString();
        } catch (Exception ex) {
            Log.e("MemoryHelper", "Error reading in internal memory");
            return null;
        }
    }

    public static String readHeadFromStorage(Context context, String file) {
        try {
            BufferedReader in =
                    new BufferedReader(
                            new InputStreamReader(context.openFileInput(file)));

            StringBuilder read = new StringBuilder();
            String line;

            // Leer las primeras 6 lineas
            // Lo suficiente para que aparezca alguna fecha
            for (int i = 0; i < 6; i++) {
                line = in.readLine();
                read.append(line);
            }
            in.close();
            return read.toString();
        } catch (Exception ex) {
            Log.e("MemoryHelper", "Error reading head in internal memory");
            return null;
        }
    }

    public static void saveBitmapToStorage(Context context, String file, Bitmap bitmap) {
        if (bitmap != null) {
            try {
                FileOutputStream out = context.openFileOutput(file, Context.MODE_PRIVATE);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.close();
                Log.i("MemoryHelper", "Written " + file + " success");
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("MemoryHelper", "Error writing barcode in internal memory");
            }
        }
    }

    public static Bitmap readBitmapFromStorage(Context context, String file) {
        try {
            return BitmapFactory.decodeStream(context.openFileInput(file));
        } catch (IOException e) {
            Log.i("MemoryHelper", "Error reading barcode in internal memory");
            return null;
        }
    }

    public static void deleteFileInStorage(Context context, String file) {
        boolean result = context.deleteFile(file);
        if (result)
            Log.i("MemoryHelper", "Deleted file " + file);
        else
            Log.e("MemoryHelper", "Error deleting file " + file);
    }

}