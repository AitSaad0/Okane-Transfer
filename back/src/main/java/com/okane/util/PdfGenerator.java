package com.okane.util;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.ByteArrayOutputStream;

public class PdfGenerator {

    public static final float PAGE_W = 595.28f;
    public static final float PAGE_H = 841.89f;
    public static final float MARGIN = 50f;
    public static final float RW = PAGE_W - 2 * MARGIN;

    private final ByteArrayOutputStream out;
    private Document document;
    private PdfWriter writer;
    private PdfContentByte cb;
    private float currentR = 0, currentG = 0, currentB = 0;
    private int currentStyle = Font.NORMAL;
    private float currentSize = 9;
    private boolean hasPage = false;

    public PdfGenerator() {
        this.out = new ByteArrayOutputStream();
        this.document = new Document(new Rectangle(PAGE_W, PAGE_H), MARGIN, MARGIN, MARGIN, MARGIN);
    }

    public void beginPage() {
        try {
            writer = PdfWriter.getInstance(document, out);
            document.open();
            cb = writer.getDirectContent();
            hasPage = true;
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }
    }

    public void endPage() {
    }

    public byte[] build() {
        if (hasPage) document.close();
        return out.toByteArray();
    }

    public void setColor(float r, float g, float b) {
        currentR = r;
        currentG = g;
        currentB = b;
    }

    public void setStrokeColor(float r, float g, float b) {
        cb.setColorStroke(new Color(r, g, b));
    }

    public void setFontBold(float size) {
        currentStyle = Font.BOLD;
        currentSize = size;
    }

    public void setFontRegular(float size) {
        currentStyle = Font.NORMAL;
        currentSize = size;
    }

    public void setFontSmall(float size) {
        currentStyle = Font.NORMAL;
        currentSize = size;
    }

    private void showText(String text, float x, float y, int align) {
        Color c = new Color(currentR, currentG, currentB);
        Font f = new Font(Font.HELVETICA, currentSize, currentStyle, c);
        ColumnText.showTextAligned(cb, align, new Phrase(text, f), x, y, 0);
    }

    public void textAt(String text, float x, float y) {
        showText(text, x, y, Element.ALIGN_LEFT);
    }

    public void textCenter(String text, float y) {
        showText(text, PAGE_W / 2f, y, Element.ALIGN_CENTER);
    }

    public void line(float x1, float y1, float x2, float y2) {
        cb.moveTo(x1, y1);
        cb.lineTo(x2, y2);
        cb.stroke();
    }

    public void rect(float x, float y, float w, float h) {
        cb.rectangle(x, y, w, h);
        cb.stroke();
    }

    public void fillRect(float x, float y, float w, float h) {
        cb.saveState();
        cb.setColorFill(new Color(currentR, currentG, currentB));
        cb.rectangle(x, y, w, h);
        cb.fill();
        cb.restoreState();
    }

    public void setLineWidth(float w) {
        cb.setLineWidth(w);
    }
}
