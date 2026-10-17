package dev.bwt.libbwt.daemon;

public class NativeBwtDaemon {
    public static native long start(String config, CallbackNotifier callback);
    public static native void shutdown(long ptr);
}
