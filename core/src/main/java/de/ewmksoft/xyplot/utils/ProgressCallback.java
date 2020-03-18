package com.bosch.adx.esidroid.reports;

/**
 * Interface for getting progress for saving AV data storing/loading.
 */
public interface ProgressCallback {

    /**
     * Method to get progress in percentage.
     *
     * @param progress progress in percentage.
     */
    void onProgress(int progress);
}
