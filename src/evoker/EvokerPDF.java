package evoker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;

public class EvokerPDF {
	
	private PdfWriter pdf;
	private Document document;
	private boolean open = false;
	final int PLOT_WIDTH = 500;
	final int PLOT_HEIGHT = 500;



	EvokerPDF(File file, int numCollections) throws DocumentException, IOException {
        document = new Document();
        document.setPageSize(new Rectangle(PLOT_WIDTH * numCollections, PLOT_HEIGHT));
        document.setMargins(10, 10, 10, 10);
        pdf = PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();
        setFileOpen(true);
	}

	private void setFileOpen(boolean b) {
		open = b;
	}

	boolean isFileOpen() {
		return open;
	}
		
	Document getDocument() {
		return document;
	}
	
}
