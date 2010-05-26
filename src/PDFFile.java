import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;

public class PDFFile {
	
	private PdfWriter pdf;
	private Document document;
	private boolean open = false;
	
	PDFFile(File file) throws DocumentException, IOException {
        document = new Document(PageSize.A4);
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
