#!/usr/local/bin/groovy

def dict_id = 14


def outFile = new File(args[0]+".out")
outFile.text = ''

new File(args[0]).eachLine { art ->

    art = art.trim()
    art = art.replace("'", '’')

    def cleaned = art.replaceFirst(/<b>ПРИМІТКА:.*/, '')
    cleaned = cleanup(cleaned)

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
    return txt.replace(/<b>/, '').replace('</b>', '*').trim()
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
    out.replaceAll(/(?i)([а-яіїєґ'’-]+)\(([а-яіїєґ'’-]+)\)([а-яіїєґ'’-]*)/, '$1$3, $1$2$3')
}

def expand_en(txt) {
    txt.replaceAll(/(?i)([a-z'’-]+)\(([a-z'’-]+)\)([a-z'’-]*)/, '$1$3, $1$2$3')
}
