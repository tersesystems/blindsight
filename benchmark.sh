#!/usr/bin/env bash

sbt 'benchmarks/jmh:run -prof gc -rf json'
LOGDATE=$(date +%Y%m%dT%H%M%S)
mkdir -p benchmarks/results/$LOGDATE
mv benchmarks/jmh-result.json benchmarks/results/$LOGDATE/openjdk11.json

