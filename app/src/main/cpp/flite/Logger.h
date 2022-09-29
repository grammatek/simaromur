/**
 * Created by Daniel Schnell.
 * Copyright (c) 2021-2022 Grammatek ehf. All rights reserved.
 */

#ifndef SIMAROMUR_LOGGER_H
#define SIMAROMUR_LOGGER_H

#include <string>
#include <thread>

#include <unistd.h>
#include <android/log.h>

namespace grammatek {

/**
 * Logger. Used for redirecting stdout/stderr to logcat from native code.
 */
class Logger {

public:
    Logger(const std::string& tagName);
    ~Logger();

    bool shutdownLogger();
private:

    std::atomic_bool m_threadEnd;
    std::string m_tag;
    std::shared_ptr<std::thread> m_thread1;
    std::shared_ptr<std::thread> m_thread2;

    int doLog(android_LogPriority priority, int pipe[2]) const;
};

} // namespace grammatek

#endif //SIMAROMUR_LOGGER_H
