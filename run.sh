#!/bin/bash -l
#SBATCH --cpus-per-task=24
#SBATCH --partition=plgrid-testing
#SBATCH --time=00:20:00

module load plgrid/tools/sbt/0.13.13

testRuns=3
type="d"
testFiles=("/net/people/plgkamsza/n-body/src/main/resources/testx.json")

for testFile in ${testFiles[@]}; do
    echo ""
    echo ""
    echo "================================================================"
    for i in $(seq $testRuns); do
        echo "------------------------ $i ------------------------"
        sbt "run $type $testFile"
    done
done