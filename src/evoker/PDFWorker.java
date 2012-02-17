package evoker;

import java.io.IOException;

import javax.swing.SwingWorker;


public class PDFWorker extends SwingWorker{
	
	Genoplot theGenoplot;

	
	PDFWorker(Genoplot g){
		super();
		theGenoplot = g;
	}
	
	  public Object doInBackground() throws IOException {
		  theGenoplot.printPDFsInBackground();
		  return null;
	  }
	
}
