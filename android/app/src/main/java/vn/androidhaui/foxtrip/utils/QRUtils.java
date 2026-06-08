package vn.androidhaui.foxtrip.utils;

import android.graphics.Bitmap;
import android.graphics.Color;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

public class QRUtils {

    /**
     * Chuyển đổi một chuỗi văn bản thành Bitmap mã QR
     */
    public static Bitmap generateQRCode(String text, int width, int height) {
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height);
            int bitWidth = bitMatrix.getWidth();
            int bitHeight = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(bitWidth, bitHeight, Bitmap.Config.RGB_565);

            for (int x = 0; x < bitWidth; x++) {
                for (int y = 0; y < bitHeight; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Giải mã nội dung từ một Bitmap chứa mã QR
     */
    public static String decodeQRCode(Bitmap bitmap) {
        int[] intArray = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(intArray, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        com.google.zxing.LuminanceSource source = new com.google.zxing.RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), intArray);
        com.google.zxing.BinaryBitmap binaryBitmap = new com.google.zxing.BinaryBitmap(new com.google.zxing.common.HybridBinarizer(source));

        try {
            com.google.zxing.Result result = new com.google.zxing.MultiFormatReader().decode(binaryBitmap);
            return result.getText();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
