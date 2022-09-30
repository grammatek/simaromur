# 3rdparty includes
include_directories( ${THIRD_PARTY_DIR}/${ARCH}/include )

add_library(fliteservice
        SHARED
        FliteDriver.cpp
        FliteService.cpp
        Logger.cpp
        )

find_library(
        log-lib
        log
)

target_link_libraries(fliteservice PUBLIC
        dl
        ${log-lib}
        )

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS}  -Wno-deprecated")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS}  -Wno-deprecated")
