package com.officeslip.Util;


import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;

public class ExifUtil_20180917
{
    public Bitmap rotateBitmap(String path)
    {
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        try
        {
            int nOrientation = getExifOrientation(path);
            if(nOrientation == 1)
            {
                return bitmap;
            }

            Matrix matrix = new Matrix();
            switch (nOrientation) {
                case 2:
                    matrix.setScale(-1, 1);
                    break;
                case 3:
                    matrix.setRotate(180);
                    break;
                case 4:
                    matrix.setRotate(180);
                    matrix.postScale(-1, 1);
                    break;
                case 5:
                    matrix.setRotate(90);
                    matrix.postScale(-1, 1);
                    break;
                case 6:
                    matrix.setRotate(90);
                    break;
                case 7:
                    matrix.setRotate(-90);
                    matrix.postScale(-1, 1);
                    break;
                case 8:
                    matrix.setRotate(-90);
                    break;
                default:
                    return bitmap;
            }

            try {
                Bitmap oriented = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                bitmap.recycle();
                return oriented;
            } catch (Exception e) {
                Logger.Companion.WriteException(this.getClass().getName(), "rotateBitmap", e, 7);
                return bitmap;
            }
        } catch (Exception e) {
            Logger.Companion.WriteException(this.getClass().getName(), "rotateBitmap", e, 7);
        }

        return bitmap;
    }

    private static int getExifOrientation(String src) throws Exception {
        int orientation = 1;

        /**
         * if your are targeting only api level >= 5
         * ExifInterface exif = new ExifInterface(src);
         * orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
         */
        if (Build.VERSION.SDK_INT >= 5) {
            Class<?> exifClass = Class.forName("android.media.ExifInterface");
            Constructor<?> exifConstructor = exifClass.getConstructor(new Class[] { String.class });
            Object exifInstance = exifConstructor.newInstance(new Object[] { src });
            Method getAttributeInt = exifClass.getMethod("getAttributeInt", new Class[] { String.class, int.class });
            Field tagOrientationField = exifClass.getField("TAG_ORIENTATION");
            String tagOrientation = (String) tagOrientationField.get(null);
            orientation = (Integer) getAttributeInt.invoke(exifInstance, new Object[] { tagOrientation, 1});
        }

        return orientation;
    }
}