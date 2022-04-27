#!/bin/bash -l
#SBATCH --cpus-per-task=1
#SBATCH --partition=plgrid-testing
#SBATCH --time=00:05:00

module load plgrid/tools/sbt/0.13.13

echo "================================="
echo "| bodies count:  10             |"
echo "| cpus-per-task: 1              |"
echo "| test runs:     10             |"
echo "| type:          single         |"
echo "| partition:     plgrid-testing |"
echo "================================="

count=10
for i in $(seq $count); do
    echo "------------------------ $i ------------------------"
    sbt "run s src/main/resources/test/10_bodies.txt"
done
