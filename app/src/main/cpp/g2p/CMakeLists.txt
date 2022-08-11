# openfst
add_library(openfst STATIC IMPORTED)
add_library(openfstlookahead STATIC IMPORTED)
add_library(openfstfar STATIC IMPORTED)
set_target_properties(openfst PROPERTIES IMPORTED_LOCATION ${THIRD_PARTY_DIR}/${ARCH}/lib/libfst.a)
set_target_properties(openfstlookahead PROPERTIES IMPORTED_LOCATION ${THIRD_PARTY_DIR}/${ARCH}/lib/libfstlookahead.a)
set_target_properties(openfstfar PROPERTIES IMPORTED_LOCATION ${THIRD_PARTY_DIR}/${ARCH}/lib/libfstfar.a)

# thrax
add_library(thrax STATIC IMPORTED)
set_target_properties(thrax PROPERTIES IMPORTED_LOCATION ${THIRD_PARTY_DIR}/${ARCH}/lib/libthrax.a)

# 3rdparty includes
include_directories( ${THIRD_PARTY_DIR}/${ARCH}/include )

add_library(g2p
        SHARED
        G2P.cpp
        G2PService.cpp
        rewrite-tester-utils.cc
        utildefs.cc
        Logger.cpp
        )

find_library(
        log-lib
        log
)

target_link_libraries( g2p PUBLIC
        -Wl,--whole-archive
        openfstlookahead
        openfstfar
        openfst
        thrax
        -Wl,--no-whole-archive
        ${log-lib}
        )

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS}  -Wno-deprecated")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS}  -Wno-deprecated")
