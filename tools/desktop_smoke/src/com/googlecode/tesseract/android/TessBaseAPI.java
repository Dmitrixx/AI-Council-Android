package com.googlecode.tesseract.android;
import android.graphics.Bitmap;
public class TessBaseAPI {
    public static final int OEM_LSTM_ONLY = 1;
    public boolean init(String datapath, String language, int engineMode) { return true; }
    public void setImage(Bitmap bitmap) { }
    public String getUTF8Text() { return ""; }
    public void recycle() { }
}
