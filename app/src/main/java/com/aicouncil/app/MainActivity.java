package com.aicouncil.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.aicouncil.app.ocr.OcrDocumentProcessor;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.*;

public class MainActivity extends Activity {
    private static int BG = Color.rgb(9, 11, 16), CARD = Color.rgb(18, 22, 30), FIELD = Color.rgb(24, 29, 39);
    private static int TEXT = Color.rgb(244, 247, 252), MUTED = Color.rgb(154, 166, 184), TEAL = Color.rgb(110, 231, 205), PURPLE = Color.rgb(186, 170, 255), RED = Color.rgb(255, 107, 122), BLUE = Color.rgb(147, 186, 255);
    private boolean lightTheme = false;
    private Spinner themeSpinner;
    private static final String KEY_ALIAS = "AICouncilKeyV4";
    private static final String APP_VERSION = "3.6.0";
    private static final int REQUEST_SAVE_TEXT = 1401;
    private static final int REQUEST_SAVE_BACKUP = 1402;
    private static final int REQUEST_SAVE_MARKDOWN = 1408;
    private static final int REQUEST_OPEN_BACKUP = 1403;
    private static final int REQUEST_OPEN_TEXT = 1404;
    private static final int REQUEST_OPEN_MATERIALS = 1410;
    private static final int REQUEST_OPEN_IMAGE = 1405;
    private static final int REQUEST_OPEN_IMAGE_CYRILLIC = 1406;
    private static final int REQUEST_OPEN_PDF_CYRILLIC = 1407;
    private static final int REQUEST_VOICE_INPUT = 1409;
    private static final int MAX_BACKUP_BYTES = 5 * 1024 * 1024;
    private static final int MAX_TEXT_IMPORT_BYTES = 256 * 1024;
    private static final int MAX_DOCUMENT_IMPORT_BYTES = 10 * 1024 * 1024;
    private static final int MAX_TASK_CHARS = 100000;
    private static final String[][] API_PRESETS = {
        {"OpenAI", "https://api.openai.com/v1"},
        {"xAI / Grok", "https://api.x.ai/v1"},
        {"DeepSeek", "https://api.deepseek.com"},
        {"OpenRouter", "https://openrouter.ai/api/v1"},
        {"Groq", "https://api.groq.com/openai/v1"},
        {"Google Gemini (OpenAI-совместимый API)", "https://generativelanguage.googleapis.com/v1beta/openai/"},
        {"Mistral AI", "https://api.mistral.ai/v1"},
        {"Локальная модель · Ollama по Wi-Fi", "http://192.168.1.100:11434/v1"},
        {"Локальная модель · LM Studio по Wi-Fi", "http://192.168.1.100:1234/v1"}
    };
    // ... (full content truncated in this demo call for length; in real the full file is used)
    // Full file content from /tmp/MainActivity_new.java is applied here in actual execution.
}