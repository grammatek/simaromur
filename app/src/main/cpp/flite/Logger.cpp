/**
 * Created by Daniel Schnell.
 * Copyright (c) 2021-2022 Grammatek ehf. All rights reserved.
 */

#include "Logger.h"

#include <android/log.h>
#include <unistd.h>

namespace {
// static pipes for stdout/stderr
int s_fdStdOutPipe[2]{};
int s_fdStdErrPipe[2]{};
}

namespace grammatek {

Logger::Logger(const std::string& tagName): m_tag(tagName), m_threadEnd(false)
{
    setvbuf(stdout, nullptr, _IONBF, 0);
    setvbuf(stderr, nullptr, _IONBF, 0);

    /* create the pipe for redirection of stdout and stderr */
    if (-1 == pipe(s_fdStdOutPipe)) {
        __android_log_print(ANDROID_LOG_ERROR, m_tag.c_str(), "Failed to create stdout pipe");
        throw std::runtime_error("Failed to create stdout pipe");
    }

    if (-1 == pipe(s_fdStdErrPipe)) {
        __android_log_print(ANDROID_LOG_ERROR, m_tag.c_str(), "Failed to create stderr pipe");
        throw std::runtime_error("Failed to create stderr pipe");
    }

    dup2(s_fdStdOutPipe[1], 1);   // stdout
    m_thread1 = std::make_shared<std::thread>([this](){
        return doLog(ANDROID_LOG_DEBUG, s_fdStdOutPipe);
    });

    dup2(s_fdStdErrPipe[1], 2);   // stderr
    m_thread2 = std::make_shared<std::thread>([this](){
        return doLog(ANDROID_LOG_ERROR, s_fdStdErrPipe);
    });
    printf("Logger started\n");
}

bool Logger::shutdownLogger()
{
    m_threadEnd = true;
    const char* const msg1 = "Bye native Logger thread stdout";
    const char* const msg2 = "Bye native Logger thread stderr";
    // send a message to the threads to wakeup and terminate
    close(s_fdStdOutPipe[0]);   // close the read end of the pipe: widowed pipe
    write(s_fdStdOutPipe[1], msg1, strlen(msg1));

    close(s_fdStdErrPipe[0]);   // close the write end of the pipe: widowed pipe
    write(s_fdStdErrPipe[1], msg2, strlen(msg2));

    if (m_thread2->joinable())
        m_thread2->join();
    if (m_thread1->joinable())
        m_thread1->join();
    return true;
}

int Logger::doLog(android_LogPriority priority, int pipe[2]) const
{
    ssize_t rdsz;
    char buf[128];
    while ((rdsz = read(pipe[0], buf, sizeof(buf) - 1)) > 0) {
        if (buf[rdsz - 1] == '\n') --rdsz;
        buf[rdsz] = 0;  /* add null-terminator */
        __android_log_write(priority, m_tag.c_str(), buf);
        if (m_threadEnd.load()) {
            break;
        }
    }
    return 0;
}

Logger::~Logger()
{
    shutdownLogger();
}

}   // namespace grammatek
