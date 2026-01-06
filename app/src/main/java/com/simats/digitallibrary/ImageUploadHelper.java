package com.simats.digitallibrary;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Helper class for image upload operations
 * Handles compression, validation, and conversion
 */
public class ImageUploadHelper {

    private static final int MAX_IMAGE_DIMENSION = 1024; // Max width/height in pixels
    private static final int IMAGE_QUALITY = 85; // JPEG compression quality (0-100)
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB in bytes

    /**
     * Validate image file size
     */
    public static boolean isValidFileSize(Context context, Uri imageUri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream == null)
                return false;

            long size = inputStream.available();
            inputStream.close();

            return size <= MAX_FILE_SIZE;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get file extension from URI
     */
    public static String getFileExtension(Context context, Uri uri) {
        String mimeType = context.getContentResolver().getType(uri);
        if (mimeType == null)
            return "jpg";

        if (mimeType.equals("image/jpeg") || mimeType.equals("image/jpg")) {
            return "jpg";
        } else if (mimeType.equals("image/png")) {
            return "png";
        }
        return "jpg";
    }

    /**
     * Compress and convert image to byte array
     * Automatically resizes if dimensions exceed MAX_IMAGE_DIMENSION
     */
    public static byte[] compressImage(Context context, Uri imageUri) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
        if (inputStream == null) {
            throw new IOException("Failed to open image stream");
        }

        // Decode image
        Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
        inputStream.close();

        if (originalBitmap == null) {
            throw new IOException("Failed to decode image");
        }

        // Calculate new dimensions
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();
        float scaleFactor = 1.0f;

        if (width > MAX_IMAGE_DIMENSION || height > MAX_IMAGE_DIMENSION) {
            scaleFactor = Math.min(
                    (float) MAX_IMAGE_DIMENSION / width,
                    (float) MAX_IMAGE_DIMENSION / height);
        }

        int newWidth = Math.round(width * scaleFactor);
        int newHeight = Math.round(height * scaleFactor);

        // Resize bitmap
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);

        // Don't forget to recycle original if it's different
        if (resizedBitmap != originalBitmap) {
            originalBitmap.recycle();
        }

        // Compress to JPEG
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();

        // Recycle bitmap
        resizedBitmap.recycle();
        byteArrayOutputStream.close();

        return imageBytes;
    }

    /**
     * Validate image format
     */
    public static boolean isValidImageFormat(Context context, Uri imageUri) {
        String mimeType = context.getContentResolver().getType(imageUri);
        return mimeType != null && (mimeType.equals("image/jpeg") ||
                mimeType.equals("image/jpg") ||
                mimeType.equals("image/png"));
    }

    /**
     * Get image dimensions without loading full bitmap (efficient)
     */
    public static int[] getImageDimensions(Context context, Uri imageUri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream == null)
                return new int[] { 0, 0 };

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();

            return new int[] { options.outWidth, options.outHeight };
        } catch (IOException e) {
            e.printStackTrace();
            return new int[] { 0, 0 };
        }
    }
}
