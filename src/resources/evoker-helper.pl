#!/usr/bin/perl

## generate the .bnt and .bed files for just one SNP.

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
my $platform   = "illumina"; #$ARGV[7];
my $cutoff     = 0.9;
my $magic_num;
my $bytesPerRecord;
my $buf;

open (TEST, ">test");
print TEST "[$oxford]\n";
close TEST;

open (BNTOUT, ">$collection.$snp.bnt");
open (BEDOUT, ">$collection.$snp.bed");

if ($oxford) {
	
	## .bed file
	my $gen_file;
	## perl 5+ includes this module, so hopefully most users will have it.
	if (-s "$collection\_$chr\_$platform.gen.bin.gz") {
	    $gen_file = new IO::Uncompress::Gunzip "$collection\_$chr\_$platform.gen.bin.gz";
	} else {
	    open($gen_file, "<","$collection\_$chr\_$platform.gen.bin");
	}
	#magic number
	read ($gen_file, $magic_num, 8);
	print BEDOUT $magic_num;
	
	#jump to position
	$bytesPerRecord = $numinds*12;
	seek ($gen_file, ($index*$bytesPerRecord)+8, 0);
	
	my $bytecounter = 0;
	my $byte        = "";
	my $individual;
	for ( my $i = 0 ; $i < $numinds ; $i++ ) {
	    my $b_aa;
	    read( $gen_file, $b_aa, 4 );
	    my $aa = unpack( 'f*', $b_aa );
	    
	    my $b_ab;
	    read( $gen_file, $b_ab, 4 );
	    my $ab = unpack( 'f*', $b_ab );
	    
	    my $b_bb;
	    read( $gen_file, $b_bb, 4 );
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
	close $gen_file;
	
	## .bnt file
	my $int_file;
	## perl 5+ includes this module, so hopefully most users will have it.
	if (-s "$collection\_$chr\_$platform.int.bin.gz") {
	    $int_file = new IO::Uncompress::Gunzip "$collection\_$chr\_$platform.int.bin.gz";
	} else {
	    open ($int_file, "<", "$collection\_$chr\_$platform.int.bin");
	}
	
	read ($int_file, $magic_num, 8);
	print BNTOUT $magic_num;
	
	#jump to position
	$bytesPerRecord = $numinds*8;
	seek ($int_file, ($index*$bytesPerRecord)+8, 0);
	read ($int_file, $buf, $bytesPerRecord);	
	print BNTOUT $buf;
	close $int_file;
	
} else {
	## .bed file
	$bytesPerRecord = ceil($numinds/4);
	open (BED, "$collection.$chr.bed");
	#magic number and SNP-major mode
	read(BED, $magic_num, 3);
	print BEDOUT $magic_num;
	
	#jump to position
	seek (BED, ($index*$bytesPerRecord)+3,0);
	read (BED, $buf, $bytesPerRecord);
	print BEDOUT $buf;
	close BED;
	
	## .bnt file
	$bytesPerRecord = $numinds*8;
	open (BNT, "$collection.$chr.bnt");
	read (BNT, $magic_num, 2);
	print BNTOUT $magic_num;
	
	#jump to position
	seek (BNT, ($index*$bytesPerRecord)+2, 0);
	read (BNT, $buf, $bytesPerRecord);	
	print BNTOUT $buf;
	close BNT;
	
}

close BEDOUT;
close BNTOUT;

print "$snp\n";

