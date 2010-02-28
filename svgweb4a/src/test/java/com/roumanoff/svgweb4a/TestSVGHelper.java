package com.roumanoff.svgweb4a;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import junit.framework.TestCase;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;

/**
 * Testing {@link com.roumanoff.svgweb4a.SVGHelper}
 */
public class TestSVGHelper extends TestCase {
	
	public void testBasic() throws FileNotFoundException, IOException, TranscoderException {
		InputStream in  = this.getClass().getResourceAsStream("/svgweb4a.svg");
		File file = File.createTempFile("test", ".png");
//		File file = new File("/Users/Patrick/Desktop/svgweb4a.png");
		OutputStream out = new FileOutputStream(file );
		TranscoderInput tIn = new TranscoderInput(in);
		SVGHelper.convertSVGToPNG(tIn, "200", "200", out);
		System.out.println(file.getAbsolutePath());
	}
}