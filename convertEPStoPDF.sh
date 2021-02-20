#!/bin/sh

# utility to convert outputXXXX.eps into distinct pdfs then merge all into final pdf

outputPDF="$1_complete.pdf"

for f in output*.eps
do
    output="$f.pdf"
    convert "$f" "$output"
done

pdfunite *eps.pdf "$outputPDF"

rm output????.eps
rm output????.eps.pdf
