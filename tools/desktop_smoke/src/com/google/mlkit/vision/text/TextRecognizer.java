package com.google.mlkit.vision.text;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
public interface TextRecognizer extends AutoCloseable {
    Task<Text> process(InputImage image);
    @Override void close();
}
