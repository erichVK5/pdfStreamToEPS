#!/bin/sh

# utility to zlib decompress the streamed text, and run pdfStreamToEPS

text="$1"
output="$text.txt"
zlib-flate -uncompress < "$text" > "$output"

# we now run pdfStreamToEPS, then merge the pdfs

java pdfStreamToEPS "$output"

# after this, if the outputXXXX.eps files all look ok, can merge into one pdf
# by running
#
#   convertEPStoPDF.sh filename
#
# to produce filename_complete.pdf
