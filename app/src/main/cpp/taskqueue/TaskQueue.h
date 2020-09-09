//
// Created by asalehin on 7/18/20.
//

#ifndef FAST_MIXER_TASKQUEUE_H
#define FAST_MIXER_TASKQUEUE_H

#ifndef MODULE_NAME
#define MODULE_NAME "ThreadPool"
#endif

#include <queue>
#include <atomic>
#include <thread>
#include <functional>
#include <chrono>

using namespace std;

class TaskQueue {

public:
    ~TaskQueue() {
        t.join();
        q.clear();
    }

    void start_queue() {
        is_running = true;
        t = std::move(std::thread([=]() {
            this->executor_loop();
        }));
    }

    void clear_queue() {
        q.clear();
    }

    void stop_queue() {
        is_running = false;
    }

    void enqueue(std::function<void()> f) {
        q.push_back(f);
    }

    bool isRunning() {
        return is_running;
    }

    thread t;

private:
    const char* TAG = "TaskQueue:: %d";
    deque<std::function<void()>> q;
    atomic<bool> is_running;
    std::mutex access;
    std::condition_variable cond;

    void executor_loop() {
        while (is_running) {
            std::function<void()> task;
            {
                std::unique_lock<std::mutex> lock(access);
                if(q.empty()) {
                    cond.wait_for(lock, std::chrono::duration<int, std::milli>(3));
                    continue;
                }
                task = std::move(q.front());
                q.pop_front();
            }
            task();
        }
    }
};

#endif //FAST_MIXER_TASKQUEUE_H