#!/software/bin/perl

## Description: This script generates both .bnt and .bed files from an illumina file
## Usage: ./illumina_parser illumina_file.txt
## Input: an illumina file 
## Output: .bnt and .bed files 
## Arguments:
## Author: JAM

use strict;

my $inputfile = $ARGV[0];
open(HEAD, $inputfile) or die "cant open input '$inputfile': $!";

## split files by chromosome?
my $chr = 1;

$inputfile =~ /(.+)\.txt/;
my $name = $1;

##Êremove and store the header details
my %headerinfo;
my $header_line = <HEAD>;
while ($header_line !~ /\[Data\]+/) {
	$header_line =~ s/\r//;
	chomp($header_line);
	my @header_values = split(/\t/, $header_line);
	if (scalar(@header_values) == 2) {
		$headerinfo{$header_values[0]} = $header_values[1];
	}
	$header_line = <HEAD>;
}
## capture the data column names
my $col_names = <HEAD>;

my $NUM_SAMPLES = $headerinfo{'Num Samples'};




open (TMP, "> $name.tmp") or die "cant open tmp output file : $!";
while (<HEAD>) {
	print TMP $_;
}
close(HEAD);
close(TMP);

## sort the remaining data in the file first by snp id then sample id
unlink("$name.tmp") if system("sort -k 1,1 -k 2,2 $name.tmp > $name.tmp.sort") == 0;

## now parse the intensity values and genotypes
open(IN, "$name.tmp.sort") or die "Can't open sorted data: $!";
open(BNT, "> $name.$chr.bnt") or die "Can't open bnt output file: $!";
open(BED, "> $name.$chr.bed") or die "Can't open bed output file: $!";

## magic number to ensure the binary is a real evoker file not just garbage
print BNT pack('B*',"0001101000110001");
## plink magic number and snp major mode
print BED pack('B*',"011011000001101100000001");

## arrays to hold all the intensity and genotype values for single snp
my @int_vals;
my @genotypes;

## parse the first line of data
my $first_line = <IN>;
$first_line =~ s/\r//;
chomp($first_line);
my @first_values = split(/\t/, $first_line);
my $prev_snp_id = $first_values[0]; 
push(@int_vals, [$first_values[7], $first_values[8]]);
push(@genotypes, [$first_values[2], $first_values[3]]);

##Êloop through each line and record all the intensity values for each ind then break and print when the snp id changes
while(my $line = <IN>) {
	$line =~ s/\r//;
	chomp($line);
	my @values = split(/\t/, $line);
	my $snp_id = $values[0];
	if ($snp_id eq $prev_snp_id) {
		push(@int_vals, [$values[7], $values[8]]);
		push(@genotypes, [$values[2], $values[3]]);
	} else {
		if (scalar(@int_vals) == $NUM_SAMPLES && scalar(@genotypes) == $NUM_SAMPLES ) {
			## print out intenisty values
			for my $aInt (@int_vals) {
				print BNT pack('f*', $aInt->[0]);
				print BNT pack('f*', $aInt->[1]);
			}
			## print out genotype values
			my $byte_counter = 0;
			my $byte;
			my $first = undef;
				
			for my $aGeno (@genotypes) {
				my $a = $aGeno->[0];
				my $b = $aGeno->[1];
				my $individual;
				## if the genotypes are defined and are not missing
				if (($a && $b) && ($a !~ /-/ && $b !~ /-/)) {
					if ($a eq $b) {
						## set which is the first genotype
						$first = $a.$b unless defined($first);
						if ($a.$b eq $first) {
							## Homozygote first
							$individual = "00";							
						} else {
							## Homozygote second
							$individual = "11";
						}
					} else {
						## Heterozygote
						$individual = "10";
					}	
				} else {
					##Êmissing genotype
					$individual = "01";
				}
				$byte = $individual.$byte;
				$byte_counter++;
			
				if ($byte_counter == 4){
					#we've completed a byte, so write it.
					print BED pack('B*',$byte);
					$byte_counter = 0;
					$byte = "";
      			}		
			}
			
			if ($byte_counter != 0){
      			for (my $i = 0; $i < 4 - $byte_counter; $i++){
					$byte = "00".$byte;
      			}
      			print BED pack('B*',$byte);
    		}
		
			@int_vals = ();
			@genotypes = ();
			## record the values that tripped the change in snp
			push(@int_vals, [$values[7], $values[8]]);
			push(@genotypes, [$values[2], $values[3]]);
			
		} else {
			die "Error: Size of intensity/genotype array does not match number of samples\n";
		}
	}
	$prev_snp_id = $snp_id;	
}
close(IN);

## print the last snp
for my $aInt (@int_vals) {
	print BNT pack('f*', $aInt->[0]);
	print BNT pack('f*', $aInt->[1]);
}

my $byte_counter = 0;
my $byte;
my $first = undef;
				
for my $aGeno (@genotypes) {
	my $a = $aGeno->[0];
	my $b = $aGeno->[1];
	my $individual;
		
	if (($a && $b) && ($a !~ /-/ && $b !~ /-/)) {
		if ($a eq $b) {
			$first = $a.$b unless defined($first);
			if ($a.$b eq $first) {
				## Homozygote first
				$individual = "00";							
			} else {
				## Homozygote second
				$individual = "11";
			}
		} else {
			## Heterozygote
			$individual = "01";
		}	
	} else {
		##Êmissing genotype
		$individual = "10";
	}
	$byte = $individual.$byte;
	$byte_counter++;
	
	if ($byte_counter == 4){
		print BED pack('B*',$byte);
		$byte_counter = 0;
		$byte = "";
 	}		
}
			
if ($byte_counter != 0){
	for (my $i = 0; $i < 4 - $byte_counter; $i++){
		$byte = "00".$byte;
	}
	print BED pack('B*',$byte);
}

close(BNT);
close(BED);

## if it looks like printing the bnt and bed filed worked delete the tmp sorted file
#if (-s "$inputfile.bnt" && -s "$inputfile.bed") {
#	unlink("$inputfile.tmp.sort");
#}

