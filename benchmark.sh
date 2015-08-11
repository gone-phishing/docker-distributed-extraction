#!/bin/bash

echo "===================================================="
echo "======== Run sequential downloads =================="
echo "===================================================="
initiate1=$(date +%s.%N)
#./run seq-download config=download/src/test/resources/download.properties
tree . | wc -l
terminate1=$(date +%s.%N)
diff1=$(echo "$terminate1 - $initiate1" | bc)
echo "Time taken" $diff1 "seconds"
echo ""


echo "==================================================="
echo "==== Run distributed downloads on akka clusters ==="
echo "==================================================="
initiate2=$(date +%s.%N)
#./run download distconfig=download/src/test/resources/dist-download.properties config=download/src/test/resources/download.properties
javac ~/Desktop/race/Solution.java
terminate2=$(date +%s.%N)
diff2=$(echo "$terminate1 - $initiate1" | bc)
echo "Time taken" $diff2 "seconds"
echo ""


if (( $(echo "$diff1 > $diff2" | bc -l) )); then
	time_diff1=$(echo "$diff1 - $diff2" | bc)
	echo "Distributed download is slow by" $time_diff1
	factor=$(($diff1 / $diff2))
	echo "Sequential download performs better by a factor of" $factor
elif [ $a -eq $b ]; then
	echo "No performance difference observed"
else
	time_diff2=$(echo "$diff2 - $diff1" | bc)
	echo "Time reduced by" $time_diff2
	factor=$(($diff2 - $diff1))
	echo "Performance enhanced by factor of" $factor
fi
