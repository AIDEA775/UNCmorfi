package com.uncmorfi.helpers;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
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

    public static String readFileFromStorage(Context context, String file) {
        try {
            BufferedReader in =
                    new BufferedReader(
                            new InputStreamReader(context.openFileInput(file)));
            String line;
            StringBuilder read = new StringBuilder();
            while((line = in.readLine()) != null) {
                read.append(line);
            }
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
            String line = in.readLine();
            in.close();
            return line;
        } catch (Exception ex) {
            Log.e("MemoryHelper", "Error reading head in internal memory");
            return null;
        }
    }
}
