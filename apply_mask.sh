#!/bin/sh
# use this to apply a mask .png to a bunch of background images
for f in *.jpg
do
	convert -composite "$f" "mask.png" "$f.jpg"
done
