#!/usr/bin/perl


## Description: This script generates binary intensity files for use in evoker
## for now we assume that this file has the same snps and samples as
## the bim and fam files for the mathing bed file
##
## Usage: >./int2bnt.pl collection.chr.int --filetype="chiamo"
## Input: Intensity file in one of the accepted formats
## Output: Binary Intensity file 
## Arguments: --filetype chaimo, illuminus, affy
##
## NOTE: For illunia see illumina_parser.pl
##
## Author: JAM

use strict;
use Getopt::Long;

my $filetype = '';

GetOptions(	'filetype=s' => \$filetype );

my $inputfile = $ARGV[0];

$inputfile =~ /(.+)\.(.+)\.int/;
open (OUT, ">$1.$2.bnt") or die "Can't open output '$1.$2.bnt': $!";
	
## magic number to ensure the binary is a real evoker file not just garbage
print OUT pack('B*',"0001101000110001");
	
if ($inputfile =~ /\.gz$/){
	open (IN, "zcat $inputfile |") or die "Can't open '>zcat $inputfile': $!";
}else{
	open (IN, $inputfile) or die "Error: Can't open '$inputfile': $!";
}

## pick a file type
if ($filetype =~ /chiamo/i) {
	## chiamo files have two header lines?, one with colmn names and another with the data type of each column?
	## separated by spaces not tabs	
	<IN>;

	while (<IN>){
  		my @fields = split;
  		  		
  		for (my $i = 5; $i <= $#fields; $i++){
   	 		print OUT pack('f*',$fields[$i]);
  		}  		
	}
	
} elsif ($filetype =~ /affy/i) {
	my %header_info;
	my @allele_a;
	my @allele_b;
	my $header_line = <IN>;
	while($header_line =~ /^#%(.+)\n$/) {
		my ($param, $val) = split(/=/, $1);
		$header_info{$param} = $val; 
		$header_line = <IN>;	
	}
	my $col_headings = $header_line;
	my $SAMPLE_NUM   = $header_info{'affymetrix-algorithm-param-apt-opt-cel-count'};

	while (my $line = <IN>) {
		chomp($line);
		## parse the data lines
		my @allele = split(/\t/, $line);
		my $snp_id = $allele[0];
		if ($snp_id =~ /-A$/i) {
			@allele_a = @allele;
		} elsif ($snp_id =~ /-B$/i) {
			@allele_b = @allele;
		}
		## if both allele arrays hold the correct amount of intensity values print them out +1 is the snp id
		if (scalar(@allele_a) == $SAMPLE_NUM + 1 && scalar(@allele_b) == $SAMPLE_NUM + 1) {
			my $snp_id_a = shift(@allele_a);
			my $snp_id_b = shift(@allele_b);
			$snp_id_a =~ s/-A$//;
			$snp_id_b =~ s/-B$//;
			## make sure the two arrays correspond to the same snp
			if ($snp_id_a eq $snp_id_b) {
				for (my $i=0; $i<@allele_a; $i++) {
					print OUT pack('f*', ($allele_a[$i],$allele_b[$i]));
				}
				@allele_a = ();
				@allele_b = ();	
			} else {
				die "Affy Error: SNP IDs do not match '$snp_id_a' '$snp_id_b'";
			}
		}
	}
	
} elsif ($filetype =~ /illuminus/i) {
  	##Ênot seen one of these files yet	
  				
} else {
	## default file format described in documentation
	<IN>;

	while (<IN>){
  		my @fields = split;
  		  		
  		for (my $i = 1; $i <= $#fields; $i++){
   	 		print OUT pack('f*',$fields[$i]);
  		}	
	}
	
}

close IN;
close OUT;
  	
  	