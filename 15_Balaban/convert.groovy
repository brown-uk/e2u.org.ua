#!/usr/local/bin/groovy

import groovy.transform.Field


@Field
def dict_id = 15

@Field
def outFile = new File(args[0]+".out")
outFile.text = ''

def article = ''
def mainWord

new File(args[0]).eachLine { art ->
    if( ! art.startsWith('*') && article ) {
        process(article, mainWord)
        article = ''
    }

    if( article ) {
        article += '<br>'
    }
    else {
        def m = art =~ "^<b>(.*?)</b>"
        mainWord = m[0][1]
//        println mainWord
    }

    art = art.replaceFirst(/^\*/, '')

    article += art
}


def process(art, mainWord) {
    art = art.trim()
    art = art.replace("'", '’')

    def cleaned = art //.replaceFirst(/<b>ПРИМІТКА:.*/, '')
    cleaned = cleanup(cleaned)

    if( cleaned =~ /\b[a-z]\./ ) {
        cleaned = cleaned.replaceAll('\\b' + mainWord[0] + '\\.', mainWord)
    }

    def txt_en = cleanup_all(cleaned)
    def txt_enb = cleanup_uk(cleaned)

    txt_en = expand_uk(txt_en)
    txt_en = expand_en(txt_en)
    txt_enb = expand_uk(txt_enb)

    art = art.replaceAll(/; (<b>[а-яіїєґА-ЯІЇЄҐ~])/, '<br>$1')


    def output = "${art}_${txt_en}_${txt_enb}_${dict_id}\n"
    output = output.replaceAll(/  +/, ' ')

    outFile << output

}

def cleanup_uk(txt) {
    def matches = txt =~ /^<b>(.*?)<\/b>/
    matches.collect { it[1] }.join('+')
}

def cleanup_all(txt) {
    return txt
        .replace(/<b>/, '')
        .replace('</b>', '*')
        .replace('<br>', '|')
        .trim()
}

def cleanup(txt) {
    return txt
        .toLowerCase()
        .replaceAll(/<i>.*?<\/i>/, "")
        .replaceAll(/<sub>.*?<\/sub>/, '')
        .replace('ґ', 'г')
        .replace('\u0301', '')
        .trim()
}

def expand_uk(txt) {
    def main = txt.replaceFirst(/^([а-яіїєґА-ЯІЇЄҐ'’-]+).*/, '$1')
    def out = txt.replace('~', main).replace('||', '')
    out = out.replaceAll(/([а-яіїєґ’'-]+)\(([а-яіїєґ’'-]+)\)([а-яіїєґ’'-]*)/, '$1$3, $1$2$3')
    out.replaceAll(/([а-яіїєґ’'-]+)\[([а-яіїєґ’'-]+)\]([а-яіїєґ’'-]*)/, '$1$3, $2$3')
}

def expand_en(txt) {
    txt.replaceAll(/([a-z’'-]+)\(([a-z’'-]+)\)([a-z’'-]*)/, '$1$3, $1$2$3')
}
