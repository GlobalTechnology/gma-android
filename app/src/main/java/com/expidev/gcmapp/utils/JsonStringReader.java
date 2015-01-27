package com.expidev.gcmapp.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by William.Randall on 1/15/2015.
 */
public class JsonStringReader
{
    public static String readFully(InputStream inputStream, String encoding) throws IOException
    {
        return new String(readFully(inputStream), encoding);
    }

    private static byte[] readFully(InputStream inputStream) throws IOException
    {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1)
        {
            byteArrayOutputStream.write(buffer, 0, length);
        }
        return byteArrayOutputStream.toByteArray();
    }
}
