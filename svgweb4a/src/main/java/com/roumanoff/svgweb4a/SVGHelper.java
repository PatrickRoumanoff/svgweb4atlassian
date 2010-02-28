package com.roumanoff.svgweb4a;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;

import com.atlassian.confluence.pages.Attachment;

public class SVGHelper {

	public static void convertSVGToPNG(Attachment attachment, String width, String height, OutputStream out)
			throws IOException, TranscoderException {
		InputStream in = attachment.getContentsAsStream();
		try {
			TranscoderInput tIn = new TranscoderInput(in);
			convertSVGToPNG(tIn, width, height, out);
		} finally {
			in.close();
		}
	}

	public static void convertSVGToPNG(String body, String width, String height, OutputStream out) throws IOException,
			TranscoderException {
		StringReader in = new StringReader(body);
		try {
			TranscoderInput tIn = new TranscoderInput(in);
			convertSVGToPNG(tIn, width, height, out);
		} finally {
			in.close();
		}
	}

	@SuppressWarnings("unchecked")
	public static void convertSVGToPNG(TranscoderInput in, String width, String height, OutputStream out)
			throws IOException, TranscoderException {
		in.setURI("file://foo.svg");
		PNGTranscoder transcoder = new PNGTranscoder();

		Map hints = new HashMap();
		hints.put(ImageTranscoder.KEY_HEIGHT, new Float(height));
		hints.put(ImageTranscoder.KEY_WIDTH, new Float(width));
		transcoder.setTranscodingHints(hints);
		try {
			transcoder.transcode(in, new TranscoderOutput(out));
		} finally {
			out.close();
		}
	}

}