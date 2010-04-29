import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JPanel;

import org.jfree.chart.JFreeChart;
 
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.DefaultFontMapper;
import com.itextpdf.text.pdf.FontMapper;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

public class PDFFile {
	
	private PdfWriter pdf;
	private Document document;
	private float viewWidth;
	private float pageHeight;
	private float lastPlotY = 0;
	private boolean open = false;
	
	PDFFile(File file) throws DocumentException, IOException {
        document = new Document(PageSize.A4);
        pdf = PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();
        fileOpen(true);
        viewWidth  = document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin();
        pageHeight = document.getPageSize().getHeight() - document.topMargin();
	}
	
	
	
	private void fileOpen(boolean b) {
		open = b;
	}

	boolean isFileOpen() {
		return open;
	}

	public void writePanel2PDF(JPanel p) {

		PdfContentByte cb = pdf.getDirectContent( );
		PdfTemplate tp = cb.createTemplate( p.getWidth(), p.getHeight() );
		Graphics2D g2d = tp.createGraphics( p.getWidth(), p.getHeight(), new DefaultFontMapper( ) );
		
		double scaleX = viewWidth / p.getWidth();
//		g2d.translate( pf.getImageableX( ), pf.getImageableY( ) );
		g2d.scale( scaleX, scaleX );
				
		if ((getLastPlotY() - p.getHeight()) > 0 ) {
			cb.addTemplate( tp, document.leftMargin(), (getLastPlotY() - p.getHeight()) );
			setLastPlotY(getLastPlotY() - p.getHeight());
		} else {
			document.newPage();
			cb.addTemplate( tp, document.leftMargin(), (pageHeight-p.getHeight()) );
			setLastPlotY(pageHeight - p.getHeight());
		}
		p.paint( g2d );
		g2d.dispose();
	}
	
	private void setLastPlotY(float f) {
		lastPlotY = f;		
	}

	private float getLastPlotY() {
		return lastPlotY;
	}

	Document getDocument() {
		return document;
	}
	
}
