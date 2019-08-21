#!/usr/local/bin/groovy

def dict_id = 10

def text = new File('dict_src.txt').text

def articles = text.split("\n\n\n")


def output = ''

articles.each { art ->

    art = art.trim()
    
    def parts = art.split("\n")
    
    if( parts.length == 1 ) return
    
//    println '-- ' + parts
    
    def record = ''
    def txt_en = ''
    def txt_enb = ''
    def main_word = ''
    
    parts.each { part ->
        if( part.trim().isEmpty() ) return
        
        def formattedPart = part.trim()
        
        if( part[0].matches(/[а-яіїєґА-ЯІЇЄҐ-]/) || part.split(" ")[0].contains("\u0301") ) {
            if( record.length() > 0 ) {
                record += '<br>•&nbsp;'
            }
            else {
                main_word = cleanup_uk(part)
                formattedPart = "<b>$formattedPart</b>"
                txt_enb = cleanup_uk(part)
            }
        }
        else {
            record = record.trim() + ' = '
        }
        
        record += formattedPart
        
        if( txt_en ) txt_en += '|'

        def part_en = cleanup_uk(part)

        part_en = part_en.replaceAll(/(^|[ ])/ + main_word[0] + '\\.', '$1'+main_word)
        if( main_word.endsWith('ий') ) {
            part_en = part_en.replaceAll(/(^|[ ])/ + main_word[0] + '-([а-яіїєґ]{1,3})', '$1' + main_word[0..<-3] + '$2')
        }

        txt_en += part_en
    }

    record = record.replace(' див.', ' <i>див.</i>')
    
    output += "${record}_${txt_en}_${txt_enb}_${dict_id}\n"

}

new File('dict.txt.out').text = output


def cleanup_uk(txt) {
    return txt.replace("\u0301", "").replaceAll(/,? -[а-яіїєґ]+/, "").replace("див. ", "|").trim()
}
