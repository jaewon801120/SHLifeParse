package com.lgcns.testpilot.common.mapping.parser.nexacro;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtils {

	/** slf4j */
	private static final Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);

	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyyMMddHHmmssSSS");

	private FileUtils() {
		// should not be Instances
	}

	public static String readFileAsString(String filePath) {
		return readFileAsString(filePath, Charset.defaultCharset());
	}

	public static String readFileAsString(String filePath, Charset charset) {
		String s;
		try {
			byte[] b = getFileContent(filePath);
			s = new String(b, charset);
		} catch (Exception e) {
			s = null;
			LOGGER.error(e.getMessage(), e);
		}
		return s;
	}

	public static byte[] readFileToByte(String filePath) {
		if (filePath.startsWith(".")) {
			// �긽��寃쎈줈�씤寃쎌슦 鍮덇컪�쓣 諛섑솚�븿
			return new byte[0];
		}
		File file = new File(filePath);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (InputStream in = new FileInputStream(file)) {
			byte[] b = new byte[4096];
			int len = 0;
			while (true) {
				len = in.read(b);
				if (len < 0) {
					break;
				}
				out.write(b, 0, len);
			}
			out.flush();
		} catch (IOException ex) {
			LOGGER.error(":: ERROR : {}", ex.getMessage(), ex);
		}
		return out.toByteArray();
	}

	/**
	 * Make new directory if directory doesn't exist
	 * 
	 * @param dir
	 * @throws Exception
	 */
	public static void makeDir(String dir) throws Exception {
		/** Check if directory exists or not **/
		validatePath(dir);
		Path directory = Paths.get(dir);
		if (Files.notExists(directory)) {
			Files.createDirectories(directory);
		}
	}

	/**
	 * Append file with contents
	 * 
	 * @param dir
	 * @param fileName
	 * @param data
	 * @throws Exception
	 */
	public static void appendFile(String dir, String fileName, byte[] data) throws Exception {
		validatePath(dir);
		Path directory = Paths.get(dir);
		/** If directory doesn't exists, create directory **/
		if (Files.notExists(directory)) {
			Files.createDirectories(directory);
		}

		Path fullPath = directory.resolve(fileName);

		try {
			if (Files.notExists(fullPath)) {
				fullPath = Files.createFile(fullPath);
				Files.write(fullPath, data, StandardOpenOption.APPEND);
			} else {
				Files.write(fullPath, data, StandardOpenOption.APPEND);
			}
		} catch (Exception ex) {
			String errMsg = String.format("[%s] File wirte error", fullPath.getFileName());
			throw new UtilException(errMsg, ex);
		}
	}

	/**
	 * Append file
	 * 
	 * @param fileName
	 * @param data
	 * @throws Exception
	 */
	public static void appendFile(String fileName, byte[] data) throws Exception {
		validatePath(fileName);

		Path path = Paths.get(fileName);
		if (!Files.exists(path.getParent())) {
			Files.createDirectories(path.getParent());
		}
		try {
			if (Files.notExists(path)) {
				path = Files.createFile(path);

				Files.write(path, data, StandardOpenOption.APPEND);
			} else {
				Files.write(path, data, StandardOpenOption.APPEND);
			}
		} catch (Exception ex) {
			String errMsg = String.format("[%s] File wirte error", path.getFileName());
			throw new UtilException(errMsg, ex);
		}
	}

	/**
	 * Append file
	 * 
	 * @param fileName
	 * @param data
	 * @throws Exception
	 */
	public static void createFile(String fileName) throws Exception {
		validatePath(fileName);

		Path path = Paths.get(fileName);
		if (!Files.exists(path.getParent())) {
			Files.createDirectories(path.getParent());
		}
		try {
			if (Files.notExists(path)) {
				path = Files.createFile(path);
			}
		} catch (Exception ex) {
			String errMsg = String.format("[%s] File wirte error", path.getFileName());
			throw new UtilException(errMsg, ex);
		}
	}

	/**
	 * Write file
	 * 
	 * @param dir
	 * @param fileName
	 * @param data
	 * @throws Exception
	 */
	public static void writeFile(String dir, String fileName, String data) throws Exception {

		validatePath(dir);
		validatePath(fileName);

		Path directory = Paths.get(dir);
		if (Files.notExists(directory)) {
			Files.createDirectories(directory);
		}

		Path fullPath = directory.resolve(fileName);

		BufferedWriter writer = null;
		try {
			if (Files.notExists(fullPath)) {
				fullPath = Files.createFile(fullPath);
				writer = Files.newBufferedWriter(fullPath, Charset.forName("UTF-8"));
				writer.write(data);
			} else {
				writer = Files.newBufferedWriter(fullPath, Charset.forName("UTF-8"));
				writer.write(data);
			}
		} catch (Exception ex) {
			String errMsg = String.format("[%s] File wirte error", fullPath.getFileName());
			throw new UtilException(errMsg, ex);
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	/**
	 * Write file
	 * 
	 * @param dir
	 * @param fileName
	 * @param data
	 * @throws Exception
	 */
	public static void writeFile(String dir, String fileName, byte[] data) throws Exception {

		validatePath(dir);
		validatePath(fileName);

		Path directory = Paths.get(dir);
		if (Files.notExists(directory)) {
			Files.createDirectories(directory);
		}

		Path fullPath = directory.resolve(fileName);

		try {
			if (Files.notExists(fullPath)) {
				fullPath = Files.createFile(fullPath);
				Files.write(fullPath, data, StandardOpenOption.WRITE);
			} else {
				Files.write(fullPath, data, StandardOpenOption.WRITE);
			}
		} catch (Exception ex) {
			String errMsg = String.format("[%s] File wirte error", fullPath.getFileName());
			throw new UtilException(errMsg, ex);
		} finally {
		}
	}

	/**
	 * Read all file contents
	 * 
	 * @param filename
	 * @return
	 * @throws Exception
	 */
	public static byte[] getFileContent(String filename) throws Exception {
		validatePath(filename);

		Path path = Paths.get(filename);
		return Files.readAllBytes(path);
	}

	/**
	 * Append byte array
	 * 
	 * @param sources
	 * @return
	 * @throws Exception
	 */
	public static byte[] appendBytes(byte[]... sources) throws Exception {
		byte[] result = null;
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			for (byte[] source : sources) {
				if (!NullUtil.isNull(source)) {
					baos.write(source);
				}
			}
			result = baos.toByteArray();
		} catch (Exception ex) {
			throw new UtilException(ex);
		}

		return result;
	}

	/**
	 * validate path or filename
	 * 
	 * @param path
	 * @throws Exception
	 */
	public static void validatePath(String path) throws Exception {

		if (!NullUtil.isNull(path) && (path.contains("../") || path.contains("./"))) {
			throw new UtilException("File path or name contains invalid character");
		}
	}

	/**
	 * Generate file name
	 * 
	 * @param systemId
	 * @param curDate
	 * @param isAsis
	 * @param isReq
	 * @return
	 * @throws Exception
	 */
	public static String getDirname(String systemId, String interfaceId, String transDir, String dirFormat,
			boolean isAsis) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat(dirFormat);
		String dirDate = sdf.format(new Date());
		StringBuilder dir = new StringBuilder(transDir).append(File.separator).append(dirDate).append(File.separator)
				.append(isAsis ? "ASIS" : "TOBE").append(File.separator).append(systemId).append(File.separator)
				.append(!NullUtil.isNull(interfaceId) ? interfaceId : "");

		/** Change directory name to suit for OS **/
		Path path = Paths.get(dir.toString());
		return path.toString();
	}

	/**
	 * Generate File name
	 * 
	 * @param systemId
	 * @param interfaceId
	 * @return
	 * @throws Exception
	 */
	public static String getFilename(String systemId, String interfaceId, int procSeq, String ext) throws Exception {

		StringBuilder file = new StringBuilder(systemId).append("_").append(interfaceId).append("_")
				.append(SDF.format(new Date())).append(procSeq).append(ext);

		return file.toString();
	}
}
