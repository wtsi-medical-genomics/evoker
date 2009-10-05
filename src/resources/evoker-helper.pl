#!/usr/bin/perl

use strict;
use POSIX qw(ceil floor);
use IO::Uncompress::Gunzip qw(gunzip $GunzipError);

my $snp        = $ARGV[0];
my $chr        = $ARGV[1];
my $collection = $ARGV[2];
my $index      = $ARGV[3];
my $numinds    = $ARGV[4];
my $tot_snps   = $ARGV[5];
my $oxford     = $ARGV[6];
my $platform   = $ARGV[7];
my $cutoff     = 0.9;
my $magic_num;
my $bytesPerRecord;
my $buf;

open (BNTOUT, ">$collection.$snp.bnt");
open (BEDOUT, ">$collection.$snp.bed");

if ($oxford) {
	
	## Genotype Data
	my $file;
	## perl 5+ includes this module, so hopefully most users will have it.
	if (-s "$collection\_$chr\_$platform.gen.bin.gz") {
	    $file = new IO::Uncompress::Gunzip "$collection\_$chr\_$platform.gen.bin.gz";
	} else {
	    open($file, "<","$collection\_$chr\_$platform.gen.bin");
	}

	#magic number and SNP-major mode.
	print BEDOUT pack('B*',"011011000001101100000001");
	
	#jump to position
	$bytesPerRecord = $numinds*12;
	seek ($file, ($index*$bytesPerRecord)+8, 0);
	
	my $bytecounter = 0;
	my $byte        = "";
	my $individual;
	for ( my $i = 0 ; $i < $numinds ; $i++ ) {
	    my $b_aa;
	    read( $file, $b_aa, 4 );
	    my $aa = unpack( 'f*', $b_aa );
	    
	    my $b_ab;
	    read( $file, $b_ab, 4 );
	    my $ab = unpack( 'f*', $b_ab );
	    
	    my $b_bb;
	    read( $file, $b_bb, 4 );
	    my $bb = unpack( 'f*', $b_bb );

	    if ( $aa > $cutoff ) {
		$individual = "00";
	    }
	    elsif ( $ab > $cutoff ) {
		$individual = "10";
	    }
	    elsif ( $bb > $cutoff ) {
		$individual = "11";
	    }
	    else {
		## missing genotype
		$individual = "01";
	    }
	    
	    $byte = $individual.$byte;
	    $bytecounter++;
	    if ( $bytecounter == 4 ) {
		## completed a byte, so write it.
		print BEDOUT pack( 'B*', $byte );
		$bytecounter = 0;
		$byte        = "";
	    }
	}
	## fill up any empty bytes
	if ( $bytecounter != 0 ) {
		for ( my $k = 0 ; $k < 4 - $bytecounter ; $k++ ) {
			$byte = "00" . $byte;
		}
		print BEDOUT pack( 'B*', $byte );
	}
	
	## Intensity Data
	my $file;
	## perl 5+ includes this module, so hopefully most users will have it.
	if (-s "$collection\_$chr\_$platform.int.bin.gz") {
	    $file = new IO::Uncompress::Gunzip "$collection\_$chr\_$platform.int.bin.gz";
	} else {
	    open ($file, "<", "$collection\_$chr\_$platform.int.bin");
	}

	read ($file, $magic_num, 8);
	print BNTOUT $magic_num;

	#jump to position
	$bytesPerRecord = $numinds*8;
	seek ($file, ($index*$bytesPerRecord)+8, 0);
	read ($file, $buf, $bytesPerRecord);	
	print BNTOUT $buf;
	
} else {
	#generate the .bed file for just this SNP.
	$bytesPerRecord = ceil($numinds/4);
	open (BED, "$collection.$chr.bed");
		
	#magic number and SNP-major mode.
	read(BED, $magic_num, 3);
	print BEDOUT $magic_num;
	
	#jump to this SNP (the +3 is for the meta-data, as above)
	seek (BED, ($index*$bytesPerRecord)+3,0);
	read (BED, $buf, $bytesPerRecord);
	print BEDOUT $buf;

	#generate the .bnt file for just this SNP.
	$bytesPerRecord = $numinds*8;
		
	open (BNT, "$collection.$chr.bnt");
	
	read (BNT, $magic_num, 2);
	print BNTOUT $magic_num;

	#jump to position
	seek (BNT, ($index*$bytesPerRecord)+2, 0);
	read (BNT, $buf, $bytesPerRecord);	
	print BNTOUT $buf;
}

close BED;
close BNT;
close BEDOUT;
close BNTOUT;

print "$snp\n";

