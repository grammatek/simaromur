/**
 * Created by Daniel Schnell on 11.03.21.
 * Copyright (C) 2021 Grammatek ehf. All rights reserved.
 */

#ifndef SIMAROMUR_G2P_H
#define SIMAROMUR_G2P_H

#include <memory>
#include <string>
#include <vector>

#include <fst/compat.h>
#include <thrax/compat/compat.h>
#include <fst/arc.h>
#include <fst/fst.h>
#include <fst/string.h>
#include <fst/symbol-table.h>
#include <thrax/grm-manager.h>
#include "utildefs.h"

namespace grammatek {

class G2P
{
public:
    G2P(const std::string &farFile, const std::string &rules);
    ~G2P() = default;

    /**
     * Does the heavy lifting of initializing all data structures
     */
    void Initialize();

    /**
     * Runs the input through the FSTs and returns phonemes.
     *
     * @param input             UTF-8 string for input
     * @param prepend_output    Prepends "Output string:" to each line if prepend_output
     * is true.
     *
     * @return
     */
    std::string process(const std::string& input, bool prepend_output = false);

private:

    ::thrax::GrmManagerSpec<::fst::StdArc> grm_;
    std::vector<std::string> rules_;
    std::unique_ptr<::fst::StringCompiler<::fst::StdArc>> compiler_;
    std::unique_ptr<::fst::SymbolTable> byte_symtab_;
    std::unique_ptr<::fst::SymbolTable> utf8_symtab_;
    std::unique_ptr<::fst::SymbolTable> generated_symtab_;
    std::unique_ptr<::fst::SymbolTable> input_symtab_;
    ::fst::TokenType type_;
    std::unique_ptr<::fst::SymbolTable> output_symtab_;
    const std::string m_farFile;
    const std::string m_rules;

    G2P(const G2P&) = delete;
    G2P& operator=(const G2P&) = delete;
};

}

#endif //SIMAROMUR_G2P_H
