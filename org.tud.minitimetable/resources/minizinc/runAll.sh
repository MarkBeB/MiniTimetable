#!/bin/bash

if [ -z "$1" ]; then
    read -p "No Run ID provided. Please enter a Run ID (e.g., 01): " RUN_ID
    RUN_ID=${RUN_ID:-"01"}
else
    RUN_ID=$1
fi

# 2. Confirmation before starting
echo "------------------------------------------------"
echo "Target Run ID: $RUN_ID"
echo "Output path:  ./out/v3/iXX-$RUN_ID/"
echo "------------------------------------------------"
read -p "Do you want to start the execution? (y/n): " CONFIRM

if [[ ! "$CONFIRM" =~ ^[yY]$ ]]; then
    echo "Execution cancelled by user."
    exit 0
fi


modelFile="./model/v3/AllConstraints.mzn"
gurobiParam="./model/Gurobi.prm"
miniZincDir="/opt/minizinc/MiniZincIDE-2.9.5-bundle-linux-x86_64"

TOTAL_START=$SECONDS

for i in {01..02};do
	dataFile="./data/i${i}.json"
	outDir="./out/i${i}-${RUN_ID}/"
	
	mkdir -p "$outDir"
	
    ARGS=(
        "-m" "$modelFile"
        "-e" "$miniZincDir"
        "-gP" "$gurobiParam"
        "-d" "$dataFile"
        "-o" "$outDir"
    )
	
	echo ">>> Processing: $dataFile (Run: $RUN_ID)"
	START=$SECONDS
	
	java -jar ./MiniZincRunner.jar "${ARGS[@]}" -O1 -p 4 -tm 60 -stm 60 &> "$outDir/run.log" || {
        echo "ERROR after $((DURATION/3600))h $(( (DURATION%3600)/60 ))m $((DURATION%60))s at $dataFile!"
        exit 1
    }
    
    DURATION=$(( $SECONDS - $START ))
    echo "Finished in $((DURATION/3600))h $(( (DURATION%3600)/60 ))m $((DURATION%60))s"
done

TOTAL_DURATION=$(( SECONDS - TOTAL_START ))
HOURS=$(( TOTAL_DURATION / 3600 ))
MINS=$(( (TOTAL_DURATION % 3600) / 60 ))
SECS=$(( TOTAL_DURATION % 60 ))

echo "-------------------------------------------"
echo "All files processed"
echo "Total Time:: ${HOURS}h ${MINS}m ${SECS}s"