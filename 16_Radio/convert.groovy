#!/usr/local/bin/groovy

import groovy.transform.Field


@Field
def dict_id = 16

@Field
def outFile = new File(args[0]+".out")
outFile.text = ''


new File(args[0]).eachLine { art ->
    art = art.trim()

    println ": " + art[0..<Math.min(100, art.size())]

    def m = art =~ "^«?<b>([а-яіїєґА-ЯІЇЄҐ'’\u0301-]+)"
    def mainWord = m[0][1].trim()

    assert mainWord

    process(art, mainWord)
}


def process(art, mainWord) {
    art = art.trim()
    
    if( art ==~ /<b>[А-ЯІЇЄҐ]<\/b>/ )
        return
    
    art = art.replace("'", '’')

    def cleaned = art

    assert cleaned

    cleaned = cleanup(cleaned)

    assert cleaned

    if( cleaned =~ /(?iu)[^а-яіїєґ][а-яіїєґ]\./ ) {
        cleaned = cleaned.replaceAll('([^а-яіїєґА-ЯІЇЄҐ’-])' + mainWord[0] + '\\.', '$1' + mainWord)
    }


    def txt_en = cleanup_all(cleaned)
    def txt_enb = extract_bold(cleaned)

    txt_en = expand_uk(txt_en)
    txt_en = expand_en(txt_en)
    txt_enb = expand_uk(txt_enb)

    art = art.replaceAll(/ *[●;] +(<b>[а-яіїєґА-ЯІЇЄҐ])/, '<br>$1')
    art = art.replaceAll(/<b>[1-9]\.<\/b>/, '<br>$0')


    def output = "${art}_${txt_en}_${txt_enb}_${dict_id}\n"
    output = output.replaceAll(/  +/, ' ')

    outFile << output

}

def cleanup(txt) {
    txt = txt.toLowerCase()
    txt = txt.replace('\u0301', '')
    txt = txt.replaceAll(/,~[а-яіїєґ’-]+/, '')
    return txt.replaceAll(/<i>.*?<\/i>/, '')
        .replaceAll(/<su[bp]>.*?<\/su[bp]>/, '')
        .replace('ґ', 'г').trim()
}

def extract_bold(txt) {
    def matches = txt =~ /^<b>(.*?)<\/b>/
    matches.collect { it[1] }.join('+')
}

def cleanup_all(txt) {
    return txt.replace(/<b>/, '').replace('</b>', '*').replace('<br>', '|').trim()
}

def expand_uk(txt) {
    def main = txt.replaceFirst(/^([а-яіїєґА-ЯІЇЄҐ’-]+).*/, '$1')
    def out = txt.replaceAll(/,~[а-яіїєґ'’]+/,'').replace('||', '').replaceAll(/<\/?b>/, '')
    out.replaceAll(/([а-яіїєґ’'-]+)\(([а-яіїєґ’'-]+)\)([а-яіїєґ’'-]*)/, '$1$3, $1$2$3')
}

def expand_en(txt) {
    txt.replaceAll(/([a-z’'-]+)\(([a-z’'-]+)\)([a-z’'-]*)/, '$1$3, $1$2$3')
}
