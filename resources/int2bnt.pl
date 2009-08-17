#!/usr/bin/perl

use strict;

my $inputfile = $ARGV[0];

if ($inputfile =~ /\.gz$/){
  open (IN, "zcat $inputfile |");
}else{
  open (IN, $inputfile);
}

$inputfile =~ /(.+)\.(.+)\.int/;
open (OUT, ">$1.$2.bnt");

#magic number
print OUT pack('B*',"0001101000110001");

#for now we assume that this file has the same snps and samples as
#the bim and fam files for the mathing bed file
<IN>;

while (<IN>){
  my @fields = split;
  for (my $i = 1; $i <= $#fields; $i++){
    print OUT pack('f*',$fields[$i]);
  }
}
close IN;
close OUT;

