#!/bin/bash

APP_HOME="/home/ubuntu/app"
LOG="/home/ubuntu/deploy.log"
ERR="/home/ubuntu/deploy_err.log"

# 1) 실행 가능한 boot jar만 선택 (plain 제외)
BUILD_JAR=$(ls ${APP_HOME}/build/libs/*SNAPSHOT.jar | grep -v "plain" | head -n 1)
JAR_NAME=$(basename "$BUILD_JAR")

echo ">>> build 파일명: $JAR_NAME" >> "$LOG"

echo ">>> build 파일 복사" >> "$LOG"
cp "$BUILD_JAR" "$APP_HOME"/

# 2) 현재 실행중인 애플리케이션 종료 (java 전부 kill하지 말고 jar 기준으로)
echo ">>> 현재 실행중인 애플리케이션 pid 확인 후 일괄 종료" >> "$LOG"
CURRENT_PID=$(pgrep -f "$JAR_NAME")
if [ -n "$CURRENT_PID" ]; then
  echo ">>> 실행중인 PID: $CURRENT_PID, 종료합니다." >> "$LOG"
  kill -15 "$CURRENT_PID"
  sleep 5
  if ps -p "$CURRENT_PID" > /dev/null; then
    echo ">>> 정상 종료 실패, 강제 종료(SIGKILL)" >> "$LOG"
    kill -9 "$CURRENT_PID"
  fi
else
  echo ">>> 실행중인 프로세스 없음" >> "$LOG"
fi

# 3) 실행
DEPLOY_JAR="$APP_HOME/$JAR_NAME"
echo ">>> DEPLOY_JAR 배포" >> "$LOG"
echo ">>> $DEPLOY_JAR 를 실행합니다" >> "$LOG"

# 필요시 프로필 지정: --spring.profiles.active=prod
nohup java -jar "$DEPLOY_JAR" --spring.profiles.active=prod >> "$LOG" 2>> "$ERR" &
