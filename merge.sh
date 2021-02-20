#!/bin/sh

# using imagemagick utility v6
# convert  {background} {overlay} [{mask}] [-compose {method}] -composite   {result}
background="$1"
overlay="$2"
output="$overlay.pdf"
convert -composite "$background" "$overlay" "$output"
