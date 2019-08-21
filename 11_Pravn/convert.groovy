#!/usr/local/bin/groovy

def dict_id = 11

def main = ''
def main_stem = ''

def output = ''
def formattedPart = ''

def txt_en = ''

def text = new File(args[0]).eachLine { art ->

    def subArt = art.startsWith("\t")

    art = art.trim()
    if( ! art )
        return

    //println "= " + art
    def (ukr, eng) = art.split(' *(=|див\\.) *', 2)

    
//    if( ! ukr.contains('~') ) {
    if( ! subArt ) {
        if( formattedPart ) {
            output += "${formattedPart}_${txt_en}_${txt_enb}_${dict_id}\n"
        }

        main = ukr
        main_stem = main.replaceFirst(/[^а-яіїєґА-ЯІЇЄҐ’'].*/, '')
        //println "\t= " + main_stem

        txt_enb = cleanup_uk( ukr )
        txt_en = txt_enb + '|' + cleanup_uk( eng )

        ukr = ukr.replaceFirst(/^[а-яіїєґА-ЯІЇЄҐ'’|-]+/, '<b>$0</b>')
        ukr = format_uk(ukr)
        formattedPart = ukr + ' = ' + format_uk( eng )
    }
    else {
        txt_en += '|' + cleanup_uk( ukr.replaceAll("~", main_stem) ) + '|' + eng
        formattedPart += '<br>•&nbsp;' + format_uk(art)
    }

}

if( formattedPart ) {
    output += "${formattedPart}_${txt_en}_${txt_enb}_${dict_id}\n"
}

new File('pravn.txt.out').text = output


def cleanup_uk(txt) {
    return txt.replaceAll(/([\u0301|]|ім\.|прикм\.|дієсл\.|див\.)|<.*?>|;+$/, "").replace('ґ', 'г').trim() //.replaceAll(/,? -[а-яіїєґ]+/, "").replace("див. ", "|").trim()
}

def format_uk(txt) {
//    return txt.replaceAll(/ім\.|прикм\.|дієсл\.|див\./, '<i>$0</i>')
    return txt
}
