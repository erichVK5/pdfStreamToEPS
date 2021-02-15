//
//  pdfStreamToEPS
//
//  Copyright 2021
//  Licence: GPL v2.0
//
//  by Erich S. Heinzle
//
//  for converting raw text streams into eps files
//
//  usage, where raw text(s) is in a file or files, produced
//  by deflating a zlib compressed text stream, is simply
//
//  java pdfStreamToEPS input.txt input2.txt another.txt etc.txt
//

import java.io.FileReader;
import java.io.IOException;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.lang.StringBuilder;

public class pdfStreamToEPS {

	static ArrayList<String> elements = new ArrayList<String>();
	static ArrayList<String> EPS = new ArrayList<String>();

	public static String textToEPS (String textStream, int width, int height) {
		Double shrinkFactor = 1.0; // kludge used for whole string rendering
		Double x = 0.0;
		Double xOffset = 0.0;
		Double y = 0.0;
		Double yOffset = 0.0;
		double fontScale = 12.0;
		String header = "%!PS-Adobe-3.0 EPSF-3.0\n%%Creator pdfStreamParser\n";
		header = header + "%%BoundingBox: 0 0 " + width + " " + height + "\n";
		header = header + width + " " + height + " 1\n";
		String output = "";
		String footer = "%%EOF";
		String s = "";
		String t1 = textStream.substring(7);
		String[] t2 = t1.split("]],");
		if (t2.length == 1) { // addresses format without discrete letter rendering
			System.out.println("Hmm, t2 = t1.split(\"]],\"); was of length 1");
			String[] temp = t1.split("\\]\\]");
			t2 = temp[0].split("\\],");
			for (String ww : t2) {
				System.out.println("T2: " + ww);
			}
			shrinkFactor = 0.82;//This is about right for times, will vary by font
		}
		for (String ss : t2) {
			// this split could break easily with changes to streaming format
			String[] div = ss.split("0,\\["); 
			String textHeader = div[0];
			if (div.length > 1) {
				textHeader = textHeader + "0";
			}
			// [1359,260,1541,31,0,[
			String[] headerData = textHeader.split(",");
			// text header order here appears to be y, x, width, fontScale
			System.out.println("headerData[0] : " + headerData[0]);
			yOffset = Double.parseDouble(headerData[0]);
			xOffset = Double.parseDouble(headerData[1]);
			fontScale = Double.parseDouble(headerData[3]);
			output = output + "/Times-Roman findfont\n";
                	output = output + fontScale*shrinkFactor + " scalefont\n";
			output = output + "setfont\n";
			output = output + "0 0 0 setrgbcolor\n";
			output = output + "newpath\n";
			output = output + xOffset + " " + (height - yOffset - fontScale) + " moveto\n";
			String textBody = "";
			if (div.length == 1) {
				// actionURI tags just duplicate text content it seems
				String text = headerData[5];
				if (!text.startsWith("\"actionURI")) {
					System.out.println("not an actionURI");
					// now, we censor empty text like " "
					// then substitute unicode insertions 
					if (!text.equals("\" \"")) {
						text = text.replaceAll("\"","");
						text = text.replace("\\ufffd","");
						text = text.replace(")","\\)");
						text = text.replace("(","\\(");
						text = text.replace("&amp;","&");
						text = text.replace("\\u0026","&");
						text = text.replace("&gt;","\\>");
						text = text.replace("&lt;","\\<");
						text = text.replace("\\u2014","-"); //emdash
						text = text.replace("\\u2019","'");
						text = text.replace("\\u201c","`");
						text = text.replace("\\u201d","'");
						text = text.replace("\\u00a0"," ");
						text = text.replace("\\u0027","'");
						text = text.replace("\\u2018","'");
						text = text.replace("\\u2013","-"); //dash
						text = text.replace("\\u2022","-"); //bullet
						text = text.replace("\\u25cf","-"); //circle / bullet
						text = text.replace("\\u2751","-"); //bullet / square
						text = text.replace("\\u27a2","-"); //arrow
						text = text.replace("\\u00b0"," "); //degree symbol
						// some of the content seems to have been OCR'ed
						text = text.replace("\\ufb01","fi");
						text = text.replace("\\ufb02","fl");
						text = text.replace("\\u00a9","(c)"); //copyright
						output = output + "(" + text.replaceAll("\"","") + ") show\n";
					}
				}
			} else {
			  textBody = "[" + div[1];
			  System.out.println("TextBody is: " + textBody);
			  String[] textBodyData = textBody.split("\\],\\[");
			  for (String sss : textBodyData) {
				String temp = sss.replaceAll("\\[","");
				temp = temp.replaceAll("\\]","");
				String[] textInfo = temp.split(",");
				x = Double.parseDouble(textInfo[0])*fontScale;
				output = output + (x + xOffset) + " " + (height - yOffset - fontScale) + " moveto\n";
				String glyph = textInfo[1].replaceAll("\"","");
				String unicode = "";
				if (glyph.startsWith("\\u")) {
					unicode = glyph.substring(2);
					if (unicode.equals("27a2")) {
						unicode = "2022";
					}
					glyph = "<FEFF" + unicode + ">";
					output = output + "-10 -5 " + glyph + " ashow\n";
				} else {
					glyph = "(" + glyph + ")";
				}
				// here, we take care of embedding parentheses in the eps
				// and they need some -x shift to render properly for Times New Roman
				if (glyph.equals("(()") && unicode.equals("")) {
					output = output + "-10 0 <FEFF0028> ashow\n";
				} else if (glyph.equals("())") && unicode.equals("")) {
                                        output = output + "-10 0 <FEFF0029> ashow\n";
				// and here we escape the backslash to keep the eps parsing happy 
                                } else if (glyph.equals("(\\)") && unicode.equals("")) {
                                        output = output + "(\\\\) show\n";
				} else if (!glyph.equals("( )") && unicode.equals("")) {
					output = output + glyph + " show\n";
				}
			  }
			}
			System.out.println("TextHeader:\n" + textHeader);
			System.out.println("TextBody:\n " + textBody);
		}
		return (header + output + footer);
	}

	public static void main (String [] args) throws IOException {
		StringBuilder str = new StringBuilder();
		FileReader inputStream = null;
		int depth = 0;
		int inset = 0;
		int inString = 0;
		int lBrCount = 0;
		int rBrCount = 0;
		int width = 0;
		int height = 0;
		int index = 0;
		// this is the brute force/inelegant raw text stream tree flattening code
		for (String file : args) {
			depth = 0;
                	inset = 0;
                	inString = 0;
                	lBrCount = 0;
                	rBrCount = 0;
		        try {
            			inputStream = new FileReader(file);
            			int c;
            			while ((c = inputStream.read()) != -1) {
					//System.out.println((char)c);
					if (c == '[') {
						lBrCount++;
						depth++;
						if (depth == 5) {
							str.append((char)c);
						}
					} else if (c == ']') {
						depth--;
						rBrCount++;
						str.append((char)c);
				        } else if (c == '{') {
						inset++;
						elements.add("{");
                                                str = new StringBuilder();
						//str.append((char)c);
					} else if (c == '}') {
						inset--;
						str.append("\n}");
						elements.add(str.toString());
                                                str = new StringBuilder();
						//str.append((char)c);
					} else if (c == '"' && inString == 0) {
						inString = 1;
						str.append((char)c);
					} else if (c == '"' && inString == 1) {
						inString = 0;
						str.append((char)c);
					} else if (c == ',' && inString == 0) {
						if (lBrCount == (rBrCount + 1)) {
							str.append((char)c);
							elements.add(str.toString());
							str = new StringBuilder();
						} else {
							str.append((char)c);
						}
					} else {
						str.append((char)c);
					}
            			}
        		}
			finally {
            			if (inputStream != null) {
                			inputStream.close();
            			}
        		}
		for (String s : elements) {
			if (s.startsWith("\"width\"")) {
				String[] data = s.split(":");
			        width = Integer.parseInt(data[1].replaceAll(",",""));
				//System.out.println("Width is: " + width);
			} else if (s.startsWith("\"height\"")) {
                                String[] data = s.split(":");
                                height = Integer.parseInt(data[1].replaceAll(",",""));
				//System.out.println("Height is: " + height);
			} else if (s.startsWith("\"name\"")) {
				//System.out.println("FONT(S) HERE");
			} else if (s.startsWith("\"text\"")) {
				//System.out.println(s);
			        //here we convert the text objects into EPS
			        EPS.add(textToEPS(s, width, height));	
			} else {
				//System.out.println(s);
			}
		}
		// having converted each page of content to eps, minus embedded
		// fonts, we save them out to sequentially numbered files
		// next stop is inspect with something like
		//   qpdfview *.eps
		// if ok, then convert to .pdf i.e.
		//   gimp *.eps
		// then export one at a time to outputN.pdf
		// then something like
		//   pdfunite output*.pdf mergedFile.pdf 
		for (String s : EPS) {
                	//System.out.println(s);
			String filename = "output" + index + ".eps";
			index++;
			PrintWriter output = new PrintWriter(new File(filename));
			output.write(s);
                        output.flush();
         		output.close();
        	}
		elements.clear();
		EPS.clear();
		} // iterate over provided filenames
	}
}
