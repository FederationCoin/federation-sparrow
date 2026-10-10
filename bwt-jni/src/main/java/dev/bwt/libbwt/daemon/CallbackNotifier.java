package dev.bwt.libbwt.daemon;

public interface CallbackNotifier {
    void onBooting(long duration);
    void onSyncProgress(float progress, int timestamp);
    void onScanProgress(float progress, int timestamp);
    void onElectrumReady(String addr);
    void onHttpReady(String addr);
    void onReady();
}
