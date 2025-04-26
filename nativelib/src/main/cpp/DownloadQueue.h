//
// Created by 11988 on 2025/4/17.
//

#ifndef Z_DOWNLOAD_MANAGER_DOWNLOADQUEUE_H
#define Z_DOWNLOAD_MANAGER_DOWNLOADQUEUE_H

#include <vector>
#include "jni.h"
#include "CallBackWrapper.cpp"
#include <iostream>
#include <mutex>
#include <condition_variable>
#include <future>
#include <atomic>

namespace Download {

    class DownloadQueue {

    public:
        DownloadQueue();
        void addCallBack(JNIEnv* env, jobject callBack);
        std::vector<CallBackWrapper*> callBacks{};
        void notifyAll();
        void increment();
        void produce();
        void consume();
        int compute();
        void getComputeResult();
    private:
        ~DownloadQueue();
        std::mutex mtx;
        std::condition_variable cv;
        bool ready = false;
        int count;
        std::atomic<int> count2{0};
    };

} // Download

#endif //Z_DOWNLOAD_MANAGER_DOWNLOADQUEUE_H
