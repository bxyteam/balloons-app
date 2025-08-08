#!/bin/bash -e

pidFile=./balloon.pid

if [ ! -f $pidFile ]; then
  echo "process pidFile was not found, this means that process is stopped"
  exit 0;
fi


# stop service
pid=`cat $pidFile`
if ps h -p $pid >/dev/null ; then
  kill $pid
fi

# wait 60 seconds for the service to stop using check intervals of 10 seconds
for (( c=1; c<=6; c++ ))
do
  echo "waiting for process to stop, try $c of 6"
  sleep 10;
  if [ -f $pidFile ]; then
    pid=$(cat $pidFile)
    if ! ps h -p $pid >/dev/null ; then
      echo "process finished normally, process terminated successfully"
      exit 0;
    fi;
  else
    echo "process finished normally, pid file was removed by application successfully"
    exit 0;
  fi;
done

# check and force kill if process continue running
if [ -f $pidFile ]; then
  pid=$(cat $pidFile)
  if ! ps h -p $pid >/dev/null ; then
    echo "process pid exists and process is not running / will delete the pid file"
    rm $pidFile
  else
    echo "process pid exists and process is running / will kill -9 the process"
    kill -9 $pid
    sleep 3
    if ps h -p $pid >/dev/null ; then
      echo "error: process continue running after kill -9 executed"
      exit 1;
    else
      echo "process killed successfully / will remove the pid file"
      rm $pidFile
    fi
  fi
else
  echo "process pid does not exist, this means that it finished normally"
fi
