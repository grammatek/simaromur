#!/bin/bash

# Filter rows of file1.tsv based on column 1 in file2.tsv. It's assumed that the files are
# tab-separated.
# If a third argument is provided, the rows that are missing in file1.tsv are written to the
# specified file.

# Check if at least two arguments are given
if [ "$#" -lt 2 ]; then
    echo "Usage: $0 <file1.tsv> <file2.tsv> [missing_in_file1.tsv]"
    exit 1
fi

# Assign input arguments to variables for clarity
file1="$1"
file2="$2"
missing_file="${3:-}"


# Check if at least two arguments are given
if [ "$#" -lt 2 ]; then
    echo "Usage: $0 <file1.tsv> <file2.tsv> [missing_in_file1.tsv]"
    exit 1
fi

# Assign input arguments to variables for clarity
file1="$1"
file2="$2"
missing_file="${3:-}"

# Use awk to handle both matching and missing entries
awk -F '\t' -v missing="$missing_file" '
BEGIN {
    # Initialize an array to track keys seen in file2
    split("", keysSeenInFile2);
}

# Process file2 to populate keysSeenInFile2 and track keys for missing entries check
NR == FNR {
    keysSeenInFile2[$1];
    next;
}

# Process file1 to print matching rows
FNR < NR && ($1 in keysSeenInFile2) {
    print;
    # Remove from keysSeenInFile2 to identify missing later
    delete keysSeenInFile2[$1];
}

# End block to handle missing entries if specified
END {
    if (missing != "") {
        for (key in keysSeenInFile2) {
            print key > missing;
        }
    }
}' "$file2" "$file1" "$file2" | sort
