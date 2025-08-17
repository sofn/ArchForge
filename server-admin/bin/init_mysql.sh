#!/usr/bin/env bash
# 用docker创建一个mysql container
# 2015-06-07 01:47

cname=engine-mysql

docker rm -f ${cname} 2> /dev/null
docker run -d --name ${cname} -e MYSQL_ROOT_PASSWORD=123 -p 127.0.0.1:3306:3306 mysql
sleep 10
mysql -h127.0.0.1 -uroot -p123 -e "CREATE DATABASE IF NOT EXISTS user"
mysql -h127.0.0.1 -uroot -p123 -e "CREATE DATABASE IF NOT EXISTS task"

#cname2=engine-mysql2
#
#docker rm -f ${cname2} 2> /dev/null
#docker run -d --name ${cname2} -e MYSQL_ROOT_PASSWORD=123 -p 127.0.0.1:3307:3306 mysql
#sleep 10
#mysql -h127.0.0.1 -uroot -p123 -P3307 -e "CREATE DATABASE IF NOT EXISTS user"
#mysql -h127.0.0.1 -uroot -p123 -P3307 -e "CREATE DATABASE IF NOT EXISTS task"

#连接
#mysql -uroot -p123 -h127.0.0.1 -P3306