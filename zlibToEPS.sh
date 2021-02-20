#!/bin/sh

# utility to zlib decompress the streamed text, and run pdfStreamToEPS

text="$1"
output="$text.txt"
zlib-flate -uncompress < "$text" > "$output"

# utility to run pdfStreamToEPS, then merge the pdfs

java pdfStreamToEPS "$output"

