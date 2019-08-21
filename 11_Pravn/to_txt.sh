#!/bin/sh

FILE=pravn
DICT=11

#odf2xhtml -p $FILE.odt > $FILE.html && \
#python-html2text -b 0 $FILE.html utf-8 | sed -r "s/ / /g" > $FILE.txt && \
unoconv -f txt --stdout $FILE.odt > $FILE.txt && \
./convert.groovy $FILE.txt && \
wc -l $FILE.txt.out && \
(diff $FILE.txt.out.prev $FILE.txt.out > $FILE.txt.out.diff || /bin/true ) && \
read -p "Upload?" && \
[ "$REPLY" == "y" ] && scp -P$PORT -C $FILE.txt.out $UHOST:$LOAD_DIR && \
ssh -p $PORT $UHOST "cd $LOAD_DIR && ./load_new.sh $FILE.txt.out $DICT && rm $FILE.txt.out"
