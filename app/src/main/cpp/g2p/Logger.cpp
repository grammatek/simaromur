/**
 * Created by Daniel Schnell.
 * Copyright (c) 2021 Grammatek ehf. All rights reserved.
 */

#include "Logger.h"

#include <android/log.h>
#include <unistd.h>

namespace grammatek {

Logger::Logger(const std::string& tagName): m_tag(tagName)
{
    /* make stdout line-buffered and stderr unbuffered */
    setvbuf(stdout, 0, _IOLBF, 0);
    setvbuf(stderr, 0, _IONBF, 0);

    /* create the pipe and redirect stdout and stderr */
    pipe(m_fd);
    dup2(m_fd[1], 1);
    dup2(m_fd[1], 2);

    m_thread = std::make_shared<std::thread>([this](){
        ssize_t rdsz;
        char buf[128];
        while ((rdsz = read(m_fd[0], buf, sizeof buf - 1)) > 0) {
            if (buf[rdsz - 1] == '\n') --rdsz;
            buf[rdsz] = 0;  /* add null-terminator */
            __android_log_write(ANDROID_LOG_DEBUG, m_tag.c_str(), buf);
        }
        return 0;
    });
}

Logger::~Logger()
{
    m_threadEnd = true;
    if (m_thread->joinable())
        m_thread->join();
}


}
