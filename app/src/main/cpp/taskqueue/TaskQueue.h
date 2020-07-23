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

using namespace std;

class TaskQueue {

public:
    TaskQueue() {
        start_queue();
    }

    void stop_queue() {
        is_running = false;
    }

    void enqueue(std::function<void()> f) {
        q.push(f);
    }

private:
    const char* TAG = "TaskQueue:: %d";
    queue<std::function<void()>> q;
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
            if (!q.empty()) {
                auto f = q.front();
                f();
                q.pop();
            }
        }
    }
};

#endif //FAST_MIXER_TASKQUEUE_H