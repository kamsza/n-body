#!/bin/bash -l
#SBATCH --ntasks=100
#SBATCH --partition=plgrid-short
#SBATCH --time=00:10:00

module load plgrid/tools/sbt/0.13.13

testRuns=4
testFiles=("/net/people/plgkamsza/n-body/src/main/resources/test-weak/test4.json")

for testFile in ${testFiles[@]}; do
    echo ""
    echo ""
    echo "================================================================"
    for i in $(seq $testRuns); do
        echo "------------------------ $i ------------------------"
        sbt "run c $testFile"
        sbt "run d $testFile"
    done
done