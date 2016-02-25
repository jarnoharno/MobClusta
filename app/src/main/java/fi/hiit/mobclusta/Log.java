package fi.hiit.mobclusta;

public class Log {
    public static final String TAG = "mob-clusta";
    public static void v(String format, Object... args)
    {
        android.util.Log.d("mc-computation", String.format(format, args));
    }
    public static void d(String format, Object... args)
    {
        android.util.Log.d(TAG, String.format(format, args));
    }
    public static void w(String format, Object... args)
    {
        android.util.Log.w(TAG, String.format(format, args));
    }
    public static void e(String format, Object... args)
    {
        android.util.Log.e(TAG, String.format(format, args));
    }
}
