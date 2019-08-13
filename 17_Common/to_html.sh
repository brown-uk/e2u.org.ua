#!/bin/sh
HTML="e2u_common_preview.html"
echo "converting to html ($HTML)..."
cat html.head.txt > $HTML
sed -r "s/^<b>/<hr><b>/" e2u_common.txt.out | sed -r 's/_.*//g' | sed -r 's/<br><b>([^1-9])/<br><li><b>\1/g' | sed -r 's/  /\&nbsp;\&nbsp;/g' | sed -r 's/\t/\&nbsp;\&nbsp;\&nbsp;\&nbsp;/g'  >> $HTML
echo "</body></html>" >> $HTML
