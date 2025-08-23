#!/bin/bash

set -e

# Find the latest non-plain executable jar built by Spring Boot
BUILD_JAR=$(ls -t /home/ubuntu/app/build/libs/*-SNAPSHOT.jar 2>/dev/null | grep -v plain | head -n 1)
JAR_NAME=$(basename "$BUILD_JAR")
echo ">>> build 파일명: $JAR_NAME" >> /home/ubuntu/deploy.log

echo ">>> build 파일 복사" >> /home/ubuntu/deploy.log
DEPLOY_PATH=/home/ubuntu/app/
cp "$BUILD_JAR" "$DEPLOY_PATH"

echo ">>> 현재 실행중인 애플리케이션 pid 확인 후 일괄 종료" >> /home/ubuntu/deploy.log
pgrep -f "$JAR_NAME" | xargs -r kill -15

DEPLOY_JAR=$DEPLOY_PATH$JAR_NAME
echo ">>> DEPLOY_JAR 배포" >> /home/ubuntu/deploy.log
echo ">>> $DEPLOY_JAR의 $JAR_NAME를 실행합니다" >> /home/ubuntu/deploy.log
nohup java -jar "$DEPLOY_JAR" >> /home/ubuntu/deploy.log 2>&1 &