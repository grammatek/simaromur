/**
 * Created by Daniel Schnell on 11.03.21.
 * Copyright (C) 2021 Grammatek ehf. All rights reserved.
 */

#include "G2P.h"
#include <fst/test/algo_test.h>
#include <cstdlib>
#include <vector>
#include <fst/flags.h>

#include <thrax/compat/utils.h>
#include <fst/arc.h>
#include <fst/fst.h>
#include <fst/string.h>
#include <fst/extensions/far/far-class.h>
#include <fst/symbol-table.h>
#include <fst/vector-fst.h>
#include <thrax/grm-manager.h>
#include <thrax/symbols.h>
#include <fst/vector-fst.h>

namespace grammatek {

using ::fst::StdArc;
using ::fst::StdVectorFst;
using ::fst::StringCompiler;
using ::fst::SymbolTable;
using ::fst::TokenType;
using ::thrax::FstToStrings;
using ::thrax::GetGeneratedSymbolTable;
using ::thrax::RuleTriple;

G2P::G2P(const std::string &farFile, const std::string &rules) :
        compiler_(nullptr),
        byte_symtab_(nullptr),
        utf8_symtab_(nullptr),
        input_symtab_(nullptr),
        output_symtab_(nullptr),
        type_(TokenType::UTF8),
        m_farFile(farFile),
        m_rules(rules)
    {
        Initialize();
    }

void G2P::Initialize()
{
    try
    {
    std::set_new_handler(FailedNewHandler);
    char* argv0 = "G2P";
    char** argv  = &argv0;
    int argc = 1;
    SET_FLAGS(argv[0], &argc, &argv, true);

    CHECK(grm_.LoadArchive(m_farFile));
    rules_ = ::fst::StringSplit(m_rules, ',');
    byte_symtab_ = nullptr;
    utf8_symtab_ = nullptr;
    if (rules_.empty()) LOG(FATAL) << "rules must be specified";
    for (const auto& it:rules_)
    {
        RuleTriple triple(it);
        const auto *fst = grm_.GetFst(triple.main_rule);
        if (!fst)
        {
            LOG(FATAL) << "grm.GetFst() must be non nullptr for rule: "
                       << triple.main_rule;
        }
        StdVectorFst vfst(*fst);
        // If the input transducers in the FAR have symbol tables then we need to
        // add the appropriate symbol table(s) to the input strings, according to
        // the parse mode.
        if (vfst.InputSymbols())
        {
            if (!byte_symtab_ &&
                vfst.InputSymbols()->Name() ==
                ::thrax::function::kByteSymbolTableName)
            {
                byte_symtab_ = ::fst::WrapUnique(vfst.InputSymbols()->Copy());
            }
            else if (!utf8_symtab_ &&
                       vfst.InputSymbols()->Name() ==
                       ::thrax::function::kUtf8SymbolTableName)
            {
                utf8_symtab_ = ::fst::WrapUnique(vfst.InputSymbols()->Copy());
            }
        }
        if (!triple.pdt_parens_rule.empty())
        {
            fst = grm_.GetFst(triple.pdt_parens_rule);
            if (!fst)
            {
                LOG(FATAL) << "grm.GetFst() must be non nullptr for rule: "
                           << triple.pdt_parens_rule;
            }
        }
        if (!triple.mpdt_assignments_rule.empty())
        {
            fst = grm_.GetFst(triple.mpdt_assignments_rule);
            if (!fst)
            {
                LOG(FATAL) << "grm.GetFst() must be non nullptr for rule: "
                           << triple.mpdt_assignments_rule;
            }
        }
    }
    generated_symtab_ = GetGeneratedSymbolTable(&grm_);

    // input mode is utf-8
    compiler_ = std::make_unique<StringCompiler<StdArc>>(TokenType::UTF8);

    output_symtab_ = nullptr;
    }
    catch (const std::exception& e)
    {
        LOG(FATAL) << e.what();
    }
}

std::string G2P::process(const std::string& input, bool prepend_output)
{
    LOG(INFO) << "Input String: " << input;
    StdVectorFst input_fst;
    StdVectorFst output_fst;
    if (!compiler_->operator()(input, &input_fst))
    {
        return "Unable to parse input string.";
    }

    // Set symbols for the input, if appropriate
    if (byte_symtab_ && type_ == TokenType::BYTE)
    {
        input_fst.SetInputSymbols(byte_symtab_.get());
        input_fst.SetOutputSymbols(byte_symtab_.get());
    }
    else if (utf8_symtab_ && type_ == TokenType::UTF8)
    {
        input_fst.SetInputSymbols(utf8_symtab_.get());
        input_fst.SetOutputSymbols(utf8_symtab_.get());
    }
    else if (input_symtab_ && type_ == TokenType::SYMBOL)
    {
        input_fst.SetInputSymbols(input_symtab_.get());
        input_fst.SetOutputSymbols(input_symtab_.get());
    }
    bool succeeded = true;
    for (const auto& rule : rules_)
    {
        RuleTriple triple(rule);
        if (grm_.Rewrite(triple.main_rule, input_fst, &output_fst,
                         triple.pdt_parens_rule, triple.mpdt_assignments_rule))
        {
            input_fst = output_fst;
        }
        else
        {
            succeeded = false;
            break;
        }
    }
    std::vector<std::pair<std::string, float>> strings;
    std::set<std::string> seen;
    if (succeeded &&
        FstToStrings(output_fst, &strings, generated_symtab_.get(), type_,
                     output_symtab_.get(), 1))
    {
        std::ostringstream sstrm;
        for (auto it = strings.cbegin(); it != strings.cend(); ++it)
        {
            const auto sx = seen.find(it->first);
            if (sx != seen.end()) continue;
            if (prepend_output)
            {
                sstrm << "Output string: " << it->first;
            }
            else
            {
                sstrm << it->first;
            }
            seen.insert(it->first);
            if (it + 1 != strings.cend())
                sstrm << '\n';
        }
        return sstrm.str();
    }
    else
    {
        return "Rewrite failed.";
    }
}

} // namespace grammatek
