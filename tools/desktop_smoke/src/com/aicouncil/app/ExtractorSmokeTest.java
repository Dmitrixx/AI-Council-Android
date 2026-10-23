package com.aicouncil.app;
import android.content.Context;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.*;
public class ExtractorSmokeTest {
  static byte[] zip(String[][] entries) throws Exception {
    ByteArrayOutputStream b=new ByteArrayOutputStream();
    try(ZipOutputStream z=new ZipOutputStream(b)) {
      for(String[] e:entries){ z.putNextEntry(new ZipEntry(e[0])); z.write(e[1].getBytes(StandardCharsets.UTF_8)); z.closeEntry(); }
    }
    return b.toByteArray();
  }
  static void require(boolean condition, String name) { if(!condition) throw new AssertionError(name); System.out.println("PASS: "+name); }
  public static void main(String[] args) throws Exception {
    Context c=new Context();
    byte[] docx=zip(new String[][]{{"word/document.xml", "<?xml version='1.0' encoding='UTF-8'?><w:document xmlns:w='urn:word'><w:body><w:p><w:r><w:t>Привет</w:t></w:r></w:p><w:p><w:r><w:t>AI Council</w:t></w:r></w:p></w:body></w:document>"}});
    String d=DocumentTextExtractor.extract(c,docx,"test.docx");
    require(d.contains("Привет") && d.contains("AI Council"),"DOCX extracts Cyrillic paragraphs");
    byte[] xlsx=zip(new String[][]{
      {"xl/workbook.xml", "<?xml version='1.0'?><workbook xmlns:r='urn:relationships'><sheets><sheet name='Revenue' sheetId='1' r:id='rId1'/></sheets></workbook>"},
      {"xl/_rels/workbook.xml.rels", "<Relationships><Relationship Id='rId1' Type='http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet' Target='worksheets/sheet1.xml'/></Relationships>"},
      {"xl/sharedStrings.xml", "<sst><si><t>Revenue</t></si><si><r><t>North</t></r><r><t> America</t></r></si></sst>"},
      {"xl/worksheets/sheet1.xml", "<worksheet><sheetData><row r='1'><c r='A1' t='s'><v>0</v></c><c r='B1'><v>42</v></c></row><row r='2'><c r='A2' t='inlineStr'><is><t>North</t></is></c><c r='B2'><f>SUM(B1,1)</f><v>43</v></c></row></sheetData></worksheet>"}
    });
    String x=DocumentTextExtractor.extract(c,xlsx,"test.xlsx");
    require(x.contains("Лист: Revenue") && x.contains("Revenue") && x.contains("42") && x.contains("43"),"XLSX resolves worksheet relationships, shared strings and values");
    require(x.contains("North"),"XLSX extracts inline text");
    byte[] malicious=zip(new String[][]{{"word/document.xml", "<!DOCTYPE x [<!ENTITY ext SYSTEM 'file:///etc/passwd'>]><w:document xmlns:w='urn:word'><w:body><w:p><w:t>&ext;</w:t></w:p></w:body></w:document>"}});
    boolean rejected=false;
    try { DocumentTextExtractor.extract(c,malicious,"bad.docx"); } catch(IOException ex) { rejected=true; }
    require(rejected,"DOCX containing a DOCTYPE is rejected");
    boolean unknownRejected=false;
    try { DocumentTextExtractor.extract(c,new byte[]{1},"thing.bin"); } catch(IOException ex) { unknownRejected=true; }
    require(unknownRejected,"Unsupported formats are rejected");
  }
}
