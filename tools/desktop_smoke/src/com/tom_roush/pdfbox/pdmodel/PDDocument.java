package com.tom_roush.pdfbox.pdmodel;
import java.io.IOException;
public class PDDocument implements AutoCloseable { public static PDDocument load(byte[] b) throws IOException { return new PDDocument(); } public int getNumberOfPages(){return 1;} public void close() throws IOException{} }
