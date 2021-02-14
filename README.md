pdfStreamToEPS

Copyright Erich S. Heinzle 2021
GPL 2.0

Frustrated by cloud hosted pdf content that was difficult to archive or use offline, explorations into the pdf streaming infrastructure identified binary blobs coming across to the local browser running javascript that then rendered the pdf locally.

The blobs are most easily acquired by downloading a HAR (HTML archive) from within a browser's developer tools (CTRL + SHIFT + C), which can then be unrolled into a temporary local filesystem with a utility such as 'harx', which requires go

go get -v github.com/outersky/har-tools/cmd/harx

after installing, you can decompress the HAR file as follows into a temporary local directory, i.e. tmp/

~/go/bin/harx -x ./tmp extracted_site.har

On investigating the unrolled local copy of the filesystem in ./tmp, the blobs will appear as something like

streamed_document_of_interest_01.pdf.bin
streamed_document_of_interest_02.pdf.bin
streamed_document_of_interest_01.pdf.jpg

The jpeg is typically a background image upon which the text is rendered.

The binary blobs are zlib compressed text streams that can be decompressed as follows:

zlib-flate -uncompress < streamed_document_of_interest_01.pdf.bin > raw_text.txt

The uncompressed text stream depicts a simple tree of text information that includes information such as 

number of pages in the blob, along with width, and height, followed by
a list of pages, each containing
page number
page width
page height
lists of embedded base64 encoded fonts
text arrays, typically with a label and discrete chars that draw the label

The embedded text rendering data appeared to be most easily mapped to eps format for extraction and viewing, and the java utility was developed to do this

The java utility needs to be compiled

javac pdfStreamToEPS.java

after which it can be invoked as follows

java pdfStreamToEPS raw_text.txt raw_text_2.txt another_stream.txt

the utility will inelegantly rip apart the stream(s) and generate an eps file for each page in the raw text stream, which can be viewed and/or processed further or embedded with a suitable utility.

The y-axis is inverted relative to the stream by the code for EPS purposes. 

Fonts are not extracted at this point in time, but with suitable base64-fu they can be easily converted, as the font lists are identified but discarded currently.

Obviously, if a set of sequential pdf pages are produced, they can be combined with a utility such as pdfunite, i.e.

pdfunite foo.pdf bar.pdf combined_output.pdf

Alternatively, if LaTex is being used, the eps files can be embedded within successive pages and exported as pdf.

TODO: 

export the font data in base64 for further conversion
refine the overall eps size
