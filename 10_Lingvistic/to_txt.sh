#!/bin/sh

. ../host.inc

FILE=dict
DICT=10

./convert.groovy && \
(diff $FILE.txt.out.prev $FILE.txt.out > $FILE.txt.out.diff || /bin/true ) && \
read -p "Upload?" && \
[ "$REPLY" == "y" ] && scp -P$PORT -C $FILE.txt.out $UHOST:$LOAD_DIR && \
ssh -p $PORT $UHOST "cd $LOAD_DIR && ./load_new.sh $FILE.txt.out $DICT && rm $FILE.txt.out"
