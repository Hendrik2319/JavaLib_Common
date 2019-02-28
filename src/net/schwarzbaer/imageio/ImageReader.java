package net.schwarzbaer.imageio;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.imageio.ImageIO;

public class ImageReader {
	
	public enum Algorithm {
		MD2    ("MD2"    ), // The MD2 message digest algorithm as defined in RFC 1319.
		MD5    ("MD5"    ), // The MD5 message digest algorithm as defined in RFC 1321.
		SHA_1  ("SHA-1"  ), // SHA Hash algorithms defined in the FIPS PUB 180-4.
		SHA_224("SHA-224"), // input length < 2^64
		SHA_256("SHA-256"), // input length < 2^64
		SHA_384("SHA-384"), // input length < 2^128
		SHA_512("SHA-512"), // input length < 2^128
		;
		public final String algorithmStr;
		Algorithm(String algorithmStr) {
			this.algorithmStr = algorithmStr;
		}
	}
	
	public static ImageResult readImage(File file, Algorithm algorithm) {
		MessageDigest msgdig;
		try { msgdig = MessageDigest.getInstance(algorithm.algorithmStr); }
		catch (NoSuchAlgorithmException e1) { e1.printStackTrace(); return null; }
		
		FileInputStream fileinput;
		try { fileinput = new FileInputStream(file); }
		catch (FileNotFoundException e) { e.printStackTrace(); return null; }
		
		DigestInputStream input = new DigestInputStream(fileinput,msgdig);
		
		BufferedImage image;
		try { image = ImageIO.read(input); }
		catch (IOException e) { e.printStackTrace(); return null; }
		
		byte[] buffer = new byte[100000];
		try {while (input.read(buffer)>=0); } catch (IOException e) {}
		
		return new ImageResult(image,msgdig.digest());
	}
	
	public static class ImageResult {

		public BufferedImage image;
		public byte[] digest;

		public ImageResult(BufferedImage image, byte[] digest) {
			this.image = image;
			this.digest = digest;
		}
		
	}
}
