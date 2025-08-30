package com.adgad.kboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

import java.util.List;
import java.util.Objects;

public class KboardView extends KeyboardView {

    private final SharedPreferences sharedPref;
    private Canvas mCanvas;
    private final Paint mPaint = new Paint();
    private final Paint mBackground = new Paint();
    private final Paint mKey = new Paint();
    float[] hsv = new float[3];

    public KboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
    }

    private String ellipsize(String input) {
        String ellip = "...";
        if (input == null || input.length() <= 14
                || input.length() < ellip.length()) {
            return input;
        }
        return input.substring(0, 14 - ellip.length()).concat(ellip);
    }

    private boolean isLuckyKey(Keyboard.Key key) {
        return key.codes[0] == -99;
    }

    @Override
    public void onDraw(Canvas canvas) {
        int height = getHeight();
        int width = getWidth();
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTextSize(36);
        mPaint.setAntiAlias(true);
        mPaint.setSubpixelText(true);
        int bgColor = sharedPref.getInt("bgcolor", R.color.background_default);
        int borderColor = sharedPref.getInt("bgcolor", R.color.background_default);
        int pressedColor = sharedPref.getInt("pressedcolor", R.color.pressed_default);
        int textColor = sharedPref.getInt("textcolor", R.color.foreground_default);
        int textSize = Integer.parseInt(Objects.requireNonNull(sharedPref.getString("fontsize", "36")));
        boolean spacing = sharedPref.getBoolean("spacing", false);
        float radiusX = 12;
        float radiusY = 12;
        boolean isBold = sharedPref.getBoolean("textBold", true);

        Color.colorToHSV(borderColor, hsv);
        hsv[2] *= 0.85f; // value component
        borderColor = Color.HSVToColor(hsv);
        /*float[] hsv = new float[3];
        Color.colorToHSV(borderColor, hsv);
        hsv[2] *= 0.85f; // 85% bright
        int alpha = (int)(255 * 0.0f); // 0% visible
        borderColor = Color.HSVToColor(alpha, hsv);*/

        mPaint.setColor(textColor);

        List<Keyboard.Key> keys = getKeyboard().getKeys();
        for (Keyboard.Key key : keys) {
            //mBackground.setColor(borderColor);
            mBackground.setColor(Color.TRANSPARENT);

            if((key.pressed && !isLuckyKey(key)) || (isLuckyKey(key) && !key.pressed)) {
                mKey.setColor(pressedColor);
            } else {
                mKey.setColor(bgColor);
            }

            int marginBottom = spacing ? (key.y + key.height == height) ? 20 : 6 : 0;
            int marginTop = spacing ? (key.y == 0) ? 20 :6 : 0;
            int marginLeft = spacing ? (key.x == 0) ? 20 : 5 : 0;
            int marginRight = spacing ?(key.x + key.width == width) ? 20 : 5 : 0;

            if (spacing) {
                canvas.drawRoundRect(
                    key.x + marginLeft, key.y + marginTop,
                    key.x + key.width - marginRight,
                    key.y + key.height - marginBottom,
                    radiusX, radiusY, mKey
                );
            } else {
                canvas.drawRect(
                    key.x + marginLeft, key.y + marginTop,
                    key.x + key.width - marginRight,
                    key.y + key.height - marginBottom,
                    mKey
                );
            }

            if (key.icon != null) {
                key.icon.setBounds(key.x + (key.width/2) - 30, key.y + (key.height/2) - 40, key.x + (key.width/2) + 30, key.y + (key.height/2) + 20);
                key.icon.setColorFilter(textColor, PorterDuff.Mode.MULTIPLY);
                key.icon.draw(canvas);
            } else if(key.label != null) {
                String label = key.popupCharacters != null ? key.popupCharacters.toString() : key.label.toString();
                boolean isCommandKey = label.length() > 2 && label.charAt(0) == '/' && label.indexOf("!") > 0;
                if(key.codes[0] == 10) {
                    mPaint.setTextSize(textSize + 32); //enter icon is small so make it bigger
                } else {
                    mPaint.setTextSize(textSize);
                }
                if (isCommandKey) {
                    label = label.substring(1, label.indexOf("!"));
                    //mPaint.setTypeface(Typeface.defaultFromStyle(isBold ? Typeface.ITALIC : Typeface.BOLD_ITALIC));
                    //mPaint.setUnderlineText(true);
                    mPaint.setTypeface(Typeface.defaultFromStyle(isBold ? Typeface.BOLD : Typeface.NORMAL));
                    mPaint.setUnderlineText(false);
                } else {
                    mPaint.setTypeface(Typeface.defaultFromStyle(isBold ? Typeface.BOLD : Typeface.NORMAL));
                    mPaint.setUnderlineText(false);
                }

                Paint.FontMetrics fm = mPaint.getFontMetrics();
                float textHeight = fm.bottom - fm.top;
                float centerY = key.y + marginTop + (key.height - marginTop - marginBottom) / 2;
                float textBase = centerY - (fm.ascent + fm.descent) / 2;
                canvas.drawText(ellipsize(label),
                    key.x + marginLeft + (key.width - marginLeft - marginRight) / 2,
                    textBase,
                    mPaint
                );

            }
        }
    }

}
