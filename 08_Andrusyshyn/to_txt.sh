#!/bin/sh
FILE=ak4
DICT=8

. ../host.inc

#odf2xhtml -p $FILE.odt > $FILE.html && \
#html2text -width 30000 -utf8 $FILE.html | grep -E "^<b>" > $FILE.txt && \

unoconv -f txt --stdout $FILE.odt | grep -E "^<b>" > $FILE.txt && \
(diff $FILE.txt.bak $FILE.txt || /bin/true) && \
./expand.py $FILE.txt && \
wc -l $FILE.txt.out && \
(diff $FILE.txt.out.prev $FILE.txt.out > $FILE.txt.out.diff || /bin/true ) && \
read -p "Upload?" && \
[ "$REPLY" == "y" ] && scp -P$PORT -C $FILE.txt.out $UHOST:$LOAD_DIR && \
ssh -p $PORT $UHOST "cd $LOAD_DIR && ./load_new.sh $FILE.txt.out $DICT && rm $FILE.txt.out"
