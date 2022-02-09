/**
 * Created by Daniel Schnell.
 * Copyright (c) 2021-2022 Grammatek ehf. All rights reserved.
 */

#ifndef SIMAROMUR_LOGGER_H
#define SIMAROMUR_LOGGER_H

#include <string>
#include <thread>

#include <unistd.h>


namespace grammatek {


/**
 * Logger. Used for redirecting stdout/stderr to logcat
 */
class Logger {

public:
    Logger(const std::string& tagName);
    ~Logger();
private:
    int m_fd[2];
    std::atomic_bool   m_threadEnd = false;
    std::string m_tag;
    std::shared_ptr<std::thread> m_thread;
};

} // namespace grammatek


#endif //SIMAROMUR_LOGGER_H
