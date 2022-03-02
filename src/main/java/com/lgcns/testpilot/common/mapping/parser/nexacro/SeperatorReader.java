package com.lgcns.testpilot.common.mapping.parser.nexacro;

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Reader;

public class SeperatorReader extends BufferedReader {
	private CharArrayWriter buffer;
	private char seperator;

	public SeperatorReader(Reader in, char seperator) {
		super(in);

		this.seperator = seperator;
	}

	public SeperatorReader(Reader in, int size, char seperator) {
		super(in, size);

		this.seperator = seperator;
	}

	public String readLine() throws IOException {
		if (this.buffer == null) {
			this.buffer = new CharArrayWriter();
		} else {
			this.buffer.reset();
		}
		boolean isEnd = false;
		for (;;) {
			int ch = read();
			if (ch == -1) {
				isEnd = true;
				break;
			}
			if (ch == this.seperator) {
				isEnd = false;
				break;
			}
			this.buffer.write(ch);
		}
		String str = this.buffer.toString();
		if (str.length() == 0) {
			return isEnd ? null : str;
		}
		return str;
	}
}