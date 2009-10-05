#!/usr/bin/perl

## Description: This script createds the .bed, .bim, .bnt and .fam files required by Evoker from Oxford format files
## Usage: ./oxford_parser /dir 'genotype_cutoff'
## Input: The script needs to be passed the full path to a directory containing the following files:
## 		Study_chr_illumina.gen.bin.gz
## 		Study_chr_illumina.int.bin.gz
## 		study_chr_illumina.snp
## 		Study_affy.sample
## Output: The script will create the four files required by evoker:
##		Study_chr.bed
##		Study_chr.bim
##		Study_chr.bnt
##		Study.fam
## Arguments: genotype probability cutoff
## Note: The *int.bin.gz file is already in a format that Evoker can understand and simply needs unzipping and renaming in the format 'study_chr.bnt'
##
## Author: jm20@sanger.ac.uk


use strict;

my $dir;
my $cutoff;

if (scalar(@ARGV) == 2) {
	$dir = $ARGV[0];
	$cutoff = $ARGV[1];	
} elsif (scalar(@ARGV) == 1) {
	$dir = $ARGV[0];
	$cutoff = 0.9;
} else {
	die "Incorrect Number of Arguments\n";
}

## TODO: make sure the path ends with a /
## TODO: convert chromosomes X,Y,XY,MT to numbers?

opendir( DIR, "$dir" ) or die "Can't open '$dir': $!";

while ( my $file = readdir(DIR) ) {

	if ( $file =~ /.gen.bin.gz$/ ) {
		## genotype file
		open( GEN, "zcat $dir$file |" ) or die "Can't open gen file '$file': $!";
		$file =~ /^(\w+)_(\d+)/;
		
		open( BED, "> $dir$1.$2.bed" ) or die "Can't open output '$1.$2.bed': $!";
		#magic number and SNP-major mode.
		print BED pack( 'B*', "011011000001101100000001" );

		my $bsnp_num;
		read( GEN, $bsnp_num, 4 );
		my $snp_num = unpack( 'i*', $bsnp_num );
		my $bind_num;
		read( GEN, $bind_num, 4 );
		my $ind_num = unpack( 'i*', $bind_num );
		$ind_num = $ind_num/3;

		## for each snp
		for ( my $i = 0 ; $i < $snp_num ; $i++ ) {
			my $bytecounter = 0;
			my $byte        = "";
			my $individual;
			## for all the inds in a snp work out the genotypes
			for ( my $j = 0 ; $j < $ind_num ; $j++ ) {
				## get the next three float values (12 bytes)
				## AA prob
				my $b_aa;
				read( GEN, $b_aa, 4 );
				my $aa = unpack( 'f*', $b_aa );
				## AB prob
				my $b_ab;
				read( GEN, $b_ab, 4 );
				my $ab = unpack( 'f*', $b_ab );
				## BB prob
				my $b_bb;
				read( GEN, $b_bb, 4 );
				my $bb = unpack( 'f*', $b_bb );

				if ( $aa > $cutoff ) {
					$individual = "00";
				}
				elsif ( $ab > $cutoff ) {
					$individual = "11";
				}
				elsif ( $bb > $cutoff ) {
					$individual = "10";
				}
				else {
					## missing
					$individual = "01";
				}

				$byte = $individual . $byte;
				$bytecounter++;

				if ( $bytecounter == 4 ) {
					#we've completed a byte, so write it.
					print BED pack( 'B*', $byte );
					$bytecounter = 0;
					$byte        = "";
				}
			}

			if ( $bytecounter != 0 ) {
				for ( my $k = 0 ; $k < 4 - $bytecounter ; $k++ ) {
					$byte = "00" . $byte;
				}
				print BED pack( 'B*', $byte );
			}

		}

	}
	elsif ( $file =~ /.snp$/ ) {
		open( SNP, $dir . $file ) or die "Can't open snp file '$file': $!";
		$file =~ /^(\w+)_(\d+)/;
		open( BIM, "> $dir$1.$2.bim" ) or die "Can't open output '$1.$2.bim': $!";
		my $chr   = $2;
		
		while ( my $line = <SNP> ) {
			chomp($line);
			my @values   = split( /\s+/, $line );
			my $snp_id   = $values[1];
			my $pos      = $values[2];
			my $allele_a = $values[3];
			my $allele_b = $values[4];
			## 'chromosome' 'snp identifier' 'Genetic distance (morgans)' 'Base-pair position (bp units)' 'Allele A' 'Allele B'
			print BIM "$chr $snp_id 0 $pos $allele_a $allele_b\n";
		}
		close(SNP);
		close(BIM);

	}
	elsif ( $file =~ /.sample$/ ) {
		open( SAM, $dir . $file ) or die "Can't open Sample file '$file': $!";
		$file =~ /^(\w+)_/;
		open( FAM, "> $dir$1.fam" ) or die "Can't open output '$1.fam': $!";

		my $header = <SAM>;
		my $header2 = <SAM>;
		while ( my $line = <SAM> ) {
			chomp($line);
			my @values    = split( /\s+/, $line );
			my $sample_id = $values[1];
			my $sex       = $values[4];
			## 'Family ID' 'Individual ID' 'Paternal ID' 'Maternal ID' 'Sex' 'Phenotype'
			print FAM "$sample_id $sample_id 0 0 $sex 0\n";
		}
		close(SAM);
		close(FAM);

	}
	elsif ( $file =~ /.int.bin.gz$/ ) {
		$file =~ /^(\w+)_(\d+)/;
		## unzip the intensity file and create a new file using the naming scheme Evoker expects
		system("zcat $dir$file > $dir$1.$2.bnt");

	}
}
