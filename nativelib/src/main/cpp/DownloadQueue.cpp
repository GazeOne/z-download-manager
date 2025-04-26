//
// Created by 11988 on 2025/4/17.
//

#include "DownloadQueue.h"
#include "jni.h"
#include <thread>

namespace Download {

    DownloadQueue::DownloadQueue() {

    }

    void DownloadQueue::addCallBack(JNIEnv *env, jobject callBack) {
        callBacks.emplace_back(new CallBackWrapper(env, callBack));

        std::thread t([]() {
            std::cout << "Lambda thread!" << std::endl;
        });
        t.detach();
    }

    int DownloadQueue::compute() {
        count2.fetch_add(1);
        return 40;
    }

    void DownloadQueue::getComputeResult() {
        std::future<int> fut =  std::async(std::launch::async, [this] {return this->compute();});
        int result = fut.get(); // 阻塞获取结果
        std::cout << "Result: " << result << std::endl;
    }

    void DownloadQueue::increment() {
//        mtx.lock();
        std::lock_guard<std::mutex> lock(mtx);  // 自动加锁/解锁
        count++;
//        mtx.unlock();
    }

    void DownloadQueue::produce() {
        std::unique_lock<std::mutex> lock(mtx);
        cv.wait(lock, [this] { return ready;});
        std::cout << "produce started!" << std::endl;
    }

    void DownloadQueue::consume() {
        {
            std::unique_lock<std::mutex> lock(mtx);
            ready = true;
        }
        cv.notify_one();
    }

    void DownloadQueue::notifyAll() {
        std::cout << "start notify all" << std::endl;
        for (auto callBack: callBacks) {
            callBack->triggerSuccess("success");
            callBack->triggerFailed("message");
        }
    }

    DownloadQueue::~DownloadQueue() {
        for (auto callBack: callBacks) {
            delete callBack;
        }
        callBacks.clear();
    };
} // Download