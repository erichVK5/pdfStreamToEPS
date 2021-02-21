#!/bin/sh

# utility to zlib decompress the streamed text, and run pdfStreamToEPS

for f in *.bin
do
    zlib-flate -uncompress < "$f" > "$f.txt"
done
