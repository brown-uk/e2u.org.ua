#!/bin/sh
FILE=radio
DICT=16

. ../host.inc

unoconv -f txt --stdout $FILE.odt | grep -E "." | sed -r "s/^  /  /" > $FILE.txt && \
./convert.groovy $FILE.txt && \
wc -l $FILE.txt.out && \
(diff $FILE.txt.out.prev $FILE.txt.out > $FILE.txt.out.diff || /bin/true ) && \
read -p "Upload?" && \
[ "$REPLY" == "y" ] && scp -P$PORT -C $FILE.txt.out $UHOST:$LOAD_DIR && \
ssh -p $PORT $UHOST "cd $LOAD_DIR && ./load_new.sh $FILE.txt.out $DICT && rm $FILE.txt.out"
