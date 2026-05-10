#!/bin/bash
export VISUAL_EXECUTION_MODE=COMPARE
mvn compile
mvn exec:java -Dexec.mainClass="com.visualengine.agent.AITestAgent"
