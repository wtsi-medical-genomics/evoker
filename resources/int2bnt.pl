#!/usr/bin/perl

## Description: This script generates binary intensity files in the format required by Evoker
## Note: We assume that the input intensity file has the same snps and samples as the bim and fam files for the mathing bed file
##
## Usage: >./int2bnt.pl collection.chr.int --filetype="illuminus"
## Input: Intensity file in one of the accepted formats named in the form collection.chromosome.int
## Output: Binary Intensity file collection.chromosome.bnt
## Arguments: --filetype [chaimo | affy | illuminus]
## default format:
##	A matrix of intensities with SNPs as rows and individuals as pairs of whitespaceÐseparated columns. 
##Êchaimo input format:  
##Ê	Tab-delimited plain text, one line per SNP, consisting of AffyID, RSID, position, alleleA, alleleB and one pair of intensities per sample for each of the two alleles
## affy birdsuite format: 
##	Birdsuite allele_summary file, which has the intensities of each allele of each SNP in matrix format. (two lines per SNP, one for each allele)
## illuminus format: 
##	Tab-delimited plain text, one line per SNP, consisting of ID, position, alleles and one pair of intensities per sample for each of the two alleles
## 
##ÊTODO: deal with NA values in chiamo and affy files

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

if ($filetype =~ /chiamo/i) {	 
	my $header = <IN>;
	while (my $line = <IN>){
  		chomp($line);
  		my @fields = split(/\s+/, $line);
  		for (my $i = 5; $i < scalar(@fields); $i++){
   	 		print OUT pack('f*', $fields[$i]);
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
	## the line which broke out of the while loop contains the column headings
	my $col_headings = $header_line;
	## generate a family file for testing
	
	
	my $SAMPLE_NUM   = $header_info{'affymetrix-algorithm-param-apt-opt-cel-count'};

	while (my $line = <IN>) {
		chomp($line);
		my @allele = split(/\t/, $line);
		my $snp_id = $allele[0];
		if ($snp_id =~ /-A$/i) {
			@allele_a = @allele;
		} elsif ($snp_id =~ /-B$/i) {
			@allele_b = @allele;
		}
		## when the A and B allele data for a SNP is parsed print the intensity values out, this is tested by both allele arrays being the correct size  (+1 is the SNP id)
		if (scalar(@allele_a) == $SAMPLE_NUM + 1 && scalar(@allele_b) == $SAMPLE_NUM + 1) {
			my $snp_id_a = shift(@allele_a);
			my $snp_id_b = shift(@allele_b);
			## remove the trailing allele
			$snp_id_a =~ s/-A$//;
			$snp_id_b =~ s/-B$//;
			if ($snp_id_a eq $snp_id_b) {
				for (my $i=0; $i<@allele_a; $i++) {
					print OUT pack('f*', ($allele_a[$i],$allele_b[$i]));
				}
				@allele_a = ();
				@allele_b = ();	
			} else {
				die "Affy Error: IDs do not match '$snp_id_a' '$snp_id_b'";
			}
		}
	}
} elsif ($filetype =~ /illuminus/i) {
  	my $header = <IN>;
	while (my $line = <IN>){
  		chomp($line);
  		my @fields = split(/\s+/, $line);
  		for (my $i = 3; $i < scalar(@fields); $i++){
   	 		my $int = $fields[$i];
   	 		if ($int eq 'NaN') {
   	 			print OUT pack('f*', -1);
   	 		} else {
   	 			print OUT pack('f*', $int);	
   	 		}
  		}  		
	}				
} else {
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
  	
  	