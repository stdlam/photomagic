package com.poc.photoeditor.provider.ui.feature.adjust.hsl;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;

public class ColorFilterGenerator {
    // Based off answer from StackOverflow
    // See: http://stackoverflow.com/a/15119089/1048340

    private ColorFilterGenerator() {
        throw new AssertionError();
    }

    public static From from(Drawable drawable) {
        return new From(drawableToBitmap(drawable));
    }

    public static From from(Bitmap bitmap) {
        return new From(bitmap);
    }

    public static From from(int color) {
        return new From(color);
    }

    // --------------------------------------------------------------------------------------------

    private static final double DELTA_INDEX[] = {
            0, 0.01, 0.02, 0.04, 0.05, 0.06, 0.07, 0.08, 0.1, 0.11, 0.12, 0.14, 0.15, 0.16, 0.17, 0.18,
            0.20, 0.21, 0.22, 0.24, 0.25, 0.27, 0.28, 0.30, 0.32, 0.34, 0.36, 0.38, 0.40, 0.42, 0.44,
            0.46, 0.48, 0.5, 0.53, 0.56, 0.59, 0.62, 0.65, 0.68, 0.71, 0.74, 0.77, 0.80, 0.83, 0.86, 0.89,
            0.92, 0.95, 0.98, 1.0, 1.06, 1.12, 1.18, 1.24, 1.30, 1.36, 1.42, 1.48, 1.54, 1.60, 1.66, 1.72,
            1.78, 1.84, 1.90, 1.96, 2.0, 2.12, 2.25, 2.37, 2.50, 2.62, 2.75, 2.87, 3.0, 3.2, 3.4, 3.6,
            3.8, 4.0, 4.3, 4.7, 4.9, 5.0, 5.5, 6.0, 6.5, 6.8, 7.0, 7.3, 7.5, 7.8, 8.0, 8.4, 8.7, 9.0, 9.4,
            9.6, 9.8, 10.0
    };

    public static void adjustHue(ColorMatrix cm, float value, int baseColor) {
        value = cleanValue(value, 180f) / 180f * (float) Math.PI;
        if (value == 0) {
            return;
        }

        float cosVal = (float) Math.cos(value);
        float sinVal = (float) Math.sin(value);
        float lumR = Color.red(baseColor);
        float lumG = Color.green(baseColor);
        float lumB = Color.blue(baseColor);
        float[] mat = new float[]{
                lumR + cosVal * (1 - lumR) + sinVal * (-lumR),
                lumG + cosVal * (-lumG) + sinVal * (-lumG),
                lumB + cosVal * (-lumB) + sinVal * (1 - lumB), 0, 0,
                lumR + cosVal * (-lumR) + sinVal * (0.143f),
                lumG + cosVal * (1 - lumG) + sinVal * (0.140f),
                lumB + cosVal * (-lumB) + sinVal * (-0.283f), 0, 0,
                lumR + cosVal * (-lumR) + sinVal * (-(1 - lumR)),
                lumG + cosVal * (-lumG) + sinVal * (lumG),
                lumB + cosVal * (1 - lumB) + sinVal * (lumB), 0, 0, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f,
                0f, 1f
        };
        cm.postConcat(new ColorMatrix(mat));
    }

    public static void adjustBrightness(ColorMatrix cm, float value) {
        value = cleanValue(value, 100);
        if (value == 0) {
            return;
        }

        float[] mat = new float[]{
                1, 0, 0, 0, value, 0, 1, 0, 0, value, 0, 0, 1, 0, value, 0, 0, 0, 1, 0, 0, 0, 0, 0,
                1
        };
        cm.postConcat(new ColorMatrix(mat));
    }

    public static void adjustContrast(ColorMatrix cm, int value) {
        value = (int) cleanValue(value, 100);
        if (value == 0) {
            return;
        }
        float x;
        if (value < 0) {
            x = 127 + value / 100 * 127;
        } else {
            x = value % 1;
            if (x == 0) {
                x = (float) DELTA_INDEX[value];
            } else {
                x = (float) DELTA_INDEX[(value << 0)] * (1 - x)
                        + (float) DELTA_INDEX[(value << 0) + 1] * x;
            }
            x = x * 127 + 127;
        }

        float[] mat = new float[]{
                x / 127, 0, 0, 0, 0.5f * (127 - x), 0, x / 127, 0, 0, 0.5f * (127 - x), 0, 0,
                x / 127, 0, 0.5f * (127 - x), 0, 0, 0, 1, 0, 0, 0, 0, 0, 1
        };
        cm.postConcat(new ColorMatrix(mat));

    }

    public static void adjustSaturation(ColorMatrix cm, float value, int baseColor) {
        value = cleanValue(value, 100);
        if (value == 0) {
            return;
        }

        float x = 1 + ((value > 0) ? 3 * value / 100 : value / 100);
        float lumR = Color.red(baseColor);
        float lumG = Color.green(baseColor);
        float lumB = Color.blue(baseColor);

        float[] mat = new float[]{
                lumR * (1 - x) + x, lumG * (1 - x), lumB * (1 - x), 0, 0, lumR * (1 - x),
                lumG * (1 - x) + x, lumB * (1 - x), 0, 0, lumR * (1 - x), lumG * (1 - x),
                lumB * (1 - x) + x, 0, 0,
                0, 0, 0, 1, 0,
                0, 0, 0, 0, 1
        };
        cm.postConcat(new ColorMatrix(mat));
    }

    // --------------------------------------------------------------------------------------------

    private static float cleanValue(float p_val, float p_limit) {
        return Math.min(p_limit, Math.max(-p_limit, p_val));
    }

    private static float[] getHsv(int color) {
        float[] hsv = new float[3];
        Color.RGBToHSV(Color.red(color), Color.green(color), Color.blue(color), hsv);
        return hsv;
    }

    /**
     * Converts a {@link Drawable} to a {@link Bitmap}
     *
     * @param drawable
     *     The {@link Drawable} to convert
     * @return The converted {@link Bitmap}.
     */
    private static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof PictureDrawable) {
            PictureDrawable pictureDrawable = (PictureDrawable) drawable;
            Bitmap bitmap = Bitmap.createBitmap(pictureDrawable.getIntrinsicWidth(),
                    pictureDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawPicture(pictureDrawable.getPicture());
            return bitmap;
        }
        int width = drawable.getIntrinsicWidth();
        width = width > 0 ? width : 1;
        int height = drawable.getIntrinsicHeight();
        height = height > 0 ? height : 1;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * Calculate the average red, green, blue color values of a bitmap
     *
     * @param bitmap
     *     a {@link Bitmap}
     * @return
     */
    private static int[] getAverageColorRGB(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int size = width * height;
        int[] pixels = new int[size];
        int r, g, b;
        r = g = b = 0;
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < size; i++) {
            int pixelColor = pixels[i];
            if (pixelColor == Color.TRANSPARENT) {
                size--;
                continue;
            }
            r += Color.red(pixelColor);
            g += Color.green(pixelColor);
            b += Color.blue(pixelColor);
        }
        r /= size;
        g /= size;
        b /= size;
        return new int[]{
                r, g, b
        };
    }

    /**
     * Calculate the average color value of a bitmap
     *
     * @param bitmap
     *     a {@link Bitmap}
     * @return
     */
    private static int getAverageColor(Bitmap bitmap) {
        int[] rgb = getAverageColorRGB(bitmap);
        return Color.argb(255, rgb[0], rgb[1], rgb[2]);
    }

    // Builder
    // --------------------------------------------------------------------------------------------

    public static final class Builder {

        int hue;

        int contrast;

        int brightness;

        int saturation;

        int baseColor;

        public Builder setHue(int hue) {
            this.hue = hue;
            return this;
        }

        public Builder setContrast(int contrast) {
            this.contrast = contrast;
            return this;
        }

        public Builder setBaseColor(int color) {
            this.baseColor = color;
            return this;
        }

        public Builder setBrightness(int brightness) {
            this.brightness = brightness;
            return this;
        }

        public Builder setSaturation(int saturation) {
            this.saturation = saturation;
            return this;
        }

        public ColorFilter build() {
            ColorMatrix cm = new ColorMatrix();
            adjustHue(cm, hue / 360f, baseColor);
            adjustContrast(cm, contrast);
            adjustBrightness(cm, brightness);
            adjustSaturation(cm, saturation / 100f, baseColor);
            return new ColorMatrixColorFilter(cm);
        }
    }

    public static final class From {

        final int oldColor;

        private From(Bitmap bitmap) {
            oldColor = getAverageColor(bitmap);
        }

        private From(int oldColor) {
            this.oldColor = oldColor;
        }

        public ColorFilter to(int newColor) {
            float[] hsv1 = getHsv(oldColor);
            float[] hsv2 = getHsv(newColor);
            int hue = (int) (hsv2[0] - hsv1[0]);
            int saturation = (int) (hsv2[1] - hsv1[1]);
            int brightness = (int) (hsv2[2] - hsv1[2]);
            return new ColorFilterGenerator.Builder()
                    .setHue(hue)
                    .setSaturation(saturation)
                    .setBrightness(brightness)
                    .build();
        }
    }
}
