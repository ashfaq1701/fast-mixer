//
// Created by asalehin on 7/18/20.
//

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
    TaskQueue() {
        deque<std::function<void()>> qu;
        q = std::make_unique<deque<std::function<void()>>>(qu);
        start_queue();
    }

    void stop_queue() {
        is_running = false;
    }

    void enqueue(std::function<void()> f) {
        q->push_back(f);
    }

    void clear_queue() {
        if (q) {
            q->clear();
        }
    }
private:
    const char* TAG = "TaskQueue:: %d";
    std::unique_ptr<deque<std::function<void()>>> q;
    atomic<bool> is_running;
    thread t;

    void start_queue() {
        is_running = true;
        t = thread([this] {
            this->executor_loop();
        });
        t.detach();
    }

    void executor_loop() {
        while (is_running) {
            if (!q->empty()) {
                auto f = q->front();
                f();
                q->pop_front();
                std::this_thread::sleep_for(std::chrono::microseconds (1000));
            }
        }
    }
};