#!/bin/bash

#服务参数
SERVER_PORT=7700

PRO_NAME="demo-provider-1.0-SNAPSHOT"
JAR_NAME="${PRO_NAME}.jar"
WORK_PATH="/work/${PRO_NAME}"
MAIN_CLASS="com.crazymaker.springcloud.demo.start.DemoCloudApplication"
#JVM="-server -Xms64m -Xmx256m"
#JVM="-server -Xms150m -Xmx150m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/vagrant/chapter26/"
JVM="-server -Xms500m -Xmx4G -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/vagrant/chapter26/"


JVM_monitor="-Djava.rmi.server.hostname=192.168.56.121 -Dcom.sun.management.jmxremote.port=18999  -Dcom.sun.management.jmxremote.rmi.port=18998 -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.local.only=false  -Dcom.sun.management.jmxremote.authenticate=false  -Dcom.sun.management.jmxremote.ssl=false"


LOG="${WORK_PATH}/logs/console.log"
export LOG_PATH=${WORK_PATH}/logs/


APPLICATION_CONFIG="-Dserver.port=${SERVER_PORT} "
REMOTE_CONFIG="-Xdebug -Xrunjdwp:transport=dt_socket,address=5005,server=y,suspend=n"

echo "LOG_PATH:$LOG_PATH"
echo "PORT:$SERVER_PORT"
echo "JVM:$JVM"


RETVAL="0"

# See how we were called.
function start() {
    if [ ! -f ${LOG} ]; then
        touch ${LOG}
    fi
#        nohup java ${JVM} ${APPLICATION_CONFIG}  -jar ${WORK_PATH}/lib/${JAR_NAME} ${MAIN_CLASS} >> ${LOG} 2>&1 &
     cmd="nohup java ${JVM}   ${APPLICATION_CONFIG}  -jar ${JAR_NAME} ${MAIN_CLASS} >> ${LOG} 2>&1 &"
     echo $cmd

     eval $cmd

     status

     tail -f ${LOG}

}

function stop() {
    pid=$(ps -ef | grep -v 'grep' | egrep $JAR_NAME| awk '{printf $2 " "}')
    if [ "$pid" != "" ]; then
        echo -n $"Shutting down boot: "
        kill -9 "$pid"
    else
        echo "${JAR_NAME} is stopped"
    fi
    status
}

function dump() {
    pid=$(ps -ef | grep -v 'grep' | egrep $JAR_NAME| awk '{printf $2 " "}')
    jps
    echo "${JAR_NAME} is running and pid is $pid"
    if [ "$pid" != "" ]; then
#    jmap -dump:format=b,file=文件名
     cmd="jmap -dump:format=b,file=dump001.hprof  $pid"
     echo $cmd
     eval $cmd

     ls -l
    else
        echo "${JAR_NAME} is stopped"
    fi
    status
}


function debug() {
    echo " start remote debug mode .........."
    if [ ! -f ${LOG} ]; then
        touch ${LOG}
    fi
#        nohup java ${JVM} ${APPLICATION_CONFIG}  -jar ${WORK_PATH}/lib/${JAR_NAME} ${MAIN_CLASS} >> ${LOG} 2>&1 &
     cmd="nohup java ${JVM}   ${JVM_monitor}   ${APPLICATION_CONFIG}    ${REMOTE_CONFIG}   -jar ${JAR_NAME} ${MAIN_CLASS} >> ${LOG} 2>&1 &"
     echo $cmd

     eval $cmd

     status

     tail -f ${LOG}

}
function status(){
    pid=$(ps -ef | grep -v 'grep' | egrep $JAR_NAME| awk '{printf $2 " "}')
    #echo "$pid"
    if [ "$pid" != "" ]; then
        echo "${JAR_NAME} is running,pid is $pid"
    else
        echo "${JAR_NAME} is stopped"
    fi
}

function usage(){
    echo "Usage: $0 {start|debug|stop|restart|status|dump}"
    RETVAL="2"
}

# See how we were called.
case "$1" in
    start)
        start
    ;;
    debug)
        debug
    ;;
    stop)
        stop
    ;;
    restart)
        stop
    	start
    ;;
    status)
        status
    ;;
    dump)
        dump
    ;;
    *)
        usage
    ;;
esac

exit ${RETVAL}