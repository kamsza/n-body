#!/bin/bash -l
#SBATCH --cpus-per-task=1
#SBATCH --time=01:30:00

module load plgrid/tools/sbt/0.13.13

echo "================================="
echo "| clusters count:  100          |"
echo "| bodies / cluster count:  1000 |"
echo "| cpus-per-task: 1              |"
echo "| test runs:     5              |"
echo "| type:          clustered      |"
echo "| partition:     plgrid         |"
echo "================================="

count=5
for i in $(seq $count); do
    echo "------------------------ $i ------------------------"
    sbt "run c src/main/resources/cls-100-1000"
done
