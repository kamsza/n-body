#!/bin/bash -l

#SBATCH --ntasks=512
#SBATCH --partition=plgrid-short
#SBATCH --time=00:05:00
#SBATCH --mem=20G

module load plgrid/tools/sbt/0.13.13

testRuns=3
testFiles=("/net/people/plgkamsza/n-body/src/main/resources/test-weak/test2.json")

for testFile in ${testFiles[@]}; do
    echo ""
    echo ""
    echo "================================================================"
    for i in $(seq $testRuns); do
        echo "------------------------ $i ------------------------"
        sbt "run $type $testFile"
    done
done