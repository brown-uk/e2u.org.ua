#!/usr/bin/env groovy

def dict_id = 17


def filename=args.length > 0 ? args[0] : 'e2u_common.txt'

def outFile = new File(filename+".out")
outFile.text = ''

new File(filename).eachLine { art ->

    if( art.contains('_') ) {
      System.err.println "_ in line: $art"
      System.exit 1
    }

    art = art.trim()
    art = art.replace("'", '’')

    def cleaned = cleanup(art)

    def txt_en = cleanup_all(cleaned)
    def txt_enb = cleanup_bold(cleaned)

//    txt_en = expand_uk(txt_en)
    txt_en = expand_en_tilde(txt_en, txt_enb.replaceFirst(/[, ].*/, ''))
    txt_en = expand_en(txt_en)
    txt_en = expand_uk(txt_en)
    txt_enb = expand_en(txt_enb)

    def arts = art.split(/<b>USAGE/, 2)

    arts[0] = arts[0].replaceAll(/<b>[0-9]+\./, '<br>$0')
      .replaceAll(/◊ */, '◊<br>')
      .replaceAll(/([;:]) <b>/, '$1<br><b>')
      .replaceAll(/; (<b>[а-яіїєґА-ЯІЇЄҐ~])/, '<br>$1')
      .replaceAll(/<br>(<b>[^1-9])/, '<br>&nbsp;&nbsp;$1')

    art = arts.join('<b>USAGE')

    art = art.replaceAll(/ *<b>USAGE/, '<hr>$0')

    def output = "${art}_${txt_en}_${txt_enb}_${dict_id}\n"
    output = output.replaceAll(/  +/, ' ')

    outFile << output
}


def cleanup_bold(txt) {
    def matches = txt =~ /^<b>(.*?)<\/b>/
    matches.collect { it[1] }.join('+')
}

def cleanup_all(txt) {
    return txt.replace(/<b>/, '').replace('</b>', '*').trim()
}

def cleanup(txt) {
    return txt
        .replaceAll(/<b>USAGE.*/, '')
        .toLowerCase()
        .replaceAll(/<br>/, "")
        .replaceAll(/<b>[0-9]+\.<\/b>/, "")
        .replaceAll(/ *\(*<i>.*?<\/i>\)* */, "")
        .replaceAll(/<su[bp]>.*?<\/su[bp]>/, '')
        .replaceAll(/ *\[.*?\] */, '*')
        .replace('ґ', 'г')
        .replace('\u0301', '')
        .replace('; ', ';')
        .trim()
}

def expand_en_tilde(txt, enb) {
    def out = txt

    if( txt.contains('~') ) {
      if( enb.endsWith('y') ) {
        out = out.replace('~ies', enb[0..-2]+'ies')
      }
      out = out.replace('~', enb)
      out = out.replace('--', '-')  // -armed .. long-~ = long--armed
    }
    return out
}

def expand_uk(txt) {
    txt.replaceAll(/(?i)([а-яіїєґ'’-]+)\(([а-яіїєґ'’-]+)\)([а-яіїєґ'’-]*)/, '$1$3, $1$2$3')
}

def expand_en(txt) {
    txt.replaceAll(/(?i)([a-z'’-]+)\(([a-z'’-]+)\)([a-z'’-]*)/, '$1$3, $1$2$3')
}
