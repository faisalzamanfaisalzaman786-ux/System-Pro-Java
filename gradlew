#!/bin/sh
GRADLE_OPTS="-Dorg.gradle.appname=gradlew"
exec java $GRADLE_OPTS -classpath "gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"
