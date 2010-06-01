import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;

public class EvokerPDF {
	
	private PdfWriter pdf;
	private Document document;
	private boolean open = false;
	
	EvokerPDF(File file, int collections) throws DocumentException, IOException {
		int width  = (600 * collections);
        int height = 500;
        document = new Document();
        document.setPageSize(new Rectangle(width,height));
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
