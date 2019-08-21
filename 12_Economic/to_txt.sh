#!/bin/sh

FILE="slovnyk_Shymkiv"
DICT=12

. ../host.inc

# unoconv -f txt slovnyk_Shymkiv.odt

./parse.groovy > parse.log

diff slovnyk_Shymkiv.txt.prev slovnyk_Shymkiv.txt > slovnyk_Shymkiv.txt.diff

read -p "Upload?" && \
[ "$REPLY" == "y" ] && scp -P$PORT -C $FILE.txt.out $UHOST:$LOAD_DIR && \
ssh -p $PORT $UHOST "cd $LOAD_DIR && ./load_new.sh $FILE.txt.out $DICT && rm $FILE.txt.out"
