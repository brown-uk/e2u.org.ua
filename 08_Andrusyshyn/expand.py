#!/usr/bin/python3
# -*- coding: utf-8 -*-

import sys
import re
#from string import maketrans

DICT='8'

main_word_re = re.compile('<b>([а-яіїєґ́’-]+(?:[,!] [а-яіїєґ́’-]+)*)[?!]*(?: ?</b>| =| [12])', flags=re.I)
main_word_re2 = re.compile('^<b>([а-яіїєґ́’-]+(?:,? [а-яіїєґ́’-]+)+)', flags=re.I)

def process_uk(txt):
    txt = re.sub('\u0301', '', txt)
    txt = re.sub('\*', '', txt)
    txt = re.sub(' *\(-[а-яіїєґ’]{1,8}([;,]? -[а-яіїєґ’]{1,8})*\)', '', txt)
#    txt = re.sub(' -[а-яіїєґ’]+', '', txt)
    txt = re.sub(' *\(-[а-яіїєґ’]{1,8} \[-[а-яіїєґ’]{1,8}\]\)', '', txt)
    txt = re.sub(' *\([а-яіїєґ’-]+,?</b>,? <i>3rd', '</b> <i>', txt)
    txt = re.sub(' *\([а-яіїєґ’-]+</b>, or <i>', '</b> <i>', txt)
    txt = re.sub('([а-яіїєґ’-]+)\(([а-яіїєґ’-]+)\)([а-яіїєґ’-]*)', '\\1\\3, \\1\\2\\3', txt)
    txt = re.sub('([а-яіїєґ’-]*)\(([а-яіїєґ’-]+)\)([а-яіїєґ’-]+)', '\\1\\3, \\1\\2\\3', txt)
    txt = re.sub('([а-яіїєґ’-]+)\[([а-яіїєґ’-]+)\]([а-яіїєґ’-]*)', '\\1\\3, \\1\\2\\3', txt)
    txt = re.sub('([а-яіїєґ’-]*)\[([а-яіїєґ’-]+)\]([а-яіїєґ’-]+)', '\\1\\3, \\1\\2\\3', txt)
    txt = re.sub('ґ', 'г', txt)
    return txt

def compact_puctuation(txt):
    txt = re.sub('[^а-яіїєґa-z’ -]+', ',', txt)
    txt = re.sub(' ,', ',', txt)
    txt = re.sub(', ', ',', txt)
    txt = re.sub('  +', ' ', txt)
    txt = re.sub(',,+', ',', txt)
    return txt

def main(name):
  line_cnt = 0
  parsed_line = ''

  ofile = open(name+".out", "w")
  with open(name, encoding='utf-8') as ifile:
   for line in ifile:

    line = line.rstrip()
    if len( re.sub('</?b>', '', line) ) < 3:
        continue

    line = re.sub("['ʼ]", "’", line)

    line = re.sub("(<b>)?([])(</b>)?", "\\2", line)


    line = re.sub('</i><i>', '', line)
    line = re.sub('</b><b>', '', line)

    out_line = ''
    processed_line = process_uk(line.lower())
    processed_line = re.sub(' \[([^\].]+)\]', ', \\1', processed_line)
    processed_line = re.sub(' \(([^\).]+)\)', ', \\1', processed_line)

    main_word_match = main_word_re.findall(processed_line)
    main_word = ",".join(set(re.split('[, ]+', ",".join(main_word_match))))

    if len(main_word_match) == 0:
        main_word_match = main_word_re2.findall(processed_line)
        main_word = ",".join(set(re.split('[, ]+', ",".join(main_word_match))))
    
    if len(main_word_match) == 0:
#        for grp in main_word_match:
#            print('grp: ' + str(grp))
#            main_word += grp + ','
#        print("--  " + main_word)
        if not re.match(".*….*", processed_line):
            print(processed_line)
#            print("--   empty main")

    if True:
#        for k, v in T_0.items():
#            line = line.replace(k, v)

#        line = line.translate(all_dict)
        line = line.replace("'", "’")


    line = line.strip()

    line = re.sub('  +', ' ', line)

    line = re.sub('\(([а-яіїєґ]+\.)\)', '(<i>\\1</i>)', line)

    out_line = ''

    if len(out_line) > 0:
        line = re.sub(' (</b>)([а-яіїєґ])', '\\1 \\2', line, flags=re.I)
        line = re.sub('^((.*?<b>.*?</b>)+ )', '\\1= ', line)

    if out_line and line:
        out_line += '<br>'
    out_line += line

    line_cnt += 1

    parsed_line_new = line.lower()
    parsed_line_new = re.sub('<i>[^<]*</i>', ',', parsed_line_new)
    parsed_line_new = re.sub('</?b>', '', parsed_line_new)

    parsed_line_new = re.sub(' [1-9]\.', ',', parsed_line_new)

    parsed_line_new = re.sub(' etc\.', ',', parsed_line_new)
    parsed_line_new = re.sub('é', 'e', parsed_line_new)
    parsed_line_new = re.sub('ö', 'o', parsed_line_new)
    parsed_line_new = re.sub('([a-z’-]+)\(([a-z’-]+)\)([a-z’-]*)', '\\1\\3, \\1\\2\\3', parsed_line_new, flags=re.I)
    
    parsed_line_new = process_uk(parsed_line_new)

    parsed_line_new = re.sub('\[([^\].]+)\]', '\\1', parsed_line_new)

    parsed_line_new = compact_puctuation(parsed_line_new)

    if parsed_line and parsed_line_new:
        parsed_line += ' @ '
    parsed_line += parsed_line_new

    if 1: #end_of_line:
#        if main_word:
#            parsed_line = re.sub('~', main_word, parsed_line)

        ofile.write( out_line )
        ofile.write('_')
        ofile.write(parsed_line)
        
        ofile.write('_' + main_word)
        
        ofile.write('_' + DICT)
        ofile.write('\n')
        parsed_line = ''

    new_line = 0

  # with end


# at the end of the file
#  ofile.write('_')
#  ofile.write(parsed_line)
#  ofile.write('_' + DICT)
#  ofile.write('\n')

# main code

name = sys.argv[1]

if len(sys.argv) > 2:
    DICT=sys.argv[2]

main(name)
