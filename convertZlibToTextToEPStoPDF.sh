#!/bin/sh

# utility to deflate pdfStream xlib,
# then convert resulting text to EPS pages
# then convert the EPS pages into PDF pages
# then merge the pdfs into one file

# use this utility if there is no need to edit the EPS files before merging as pdf

./zlibToEPS.sh $1

./convertEPStoPDF.sh $1
