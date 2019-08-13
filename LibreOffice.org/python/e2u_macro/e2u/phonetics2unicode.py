#''
#@author: Andriy Rysin <arysin@gmail.com>
#''

import uno
from com.sun.star.awt.FontWeight import BOLD
from com.sun.star.awt.FontSlant import ITALIC
from com.sun.star.awt.FontWeight import NORMAL
from com.sun.star.beans import PropertyValue


def getDoc():
    return XSCRIPTCONTEXT.getDocument()



def replaceAllPhonetics():
    oDoc = getDoc()
    oFind = oDoc.createSearchDescriptor()

    oFind.SearchRegularExpression=True  #Use regular expressions
    oFind.setSearchString('\[.*?\]')
    vFound = oDoc.findFirst(oFind)

    while vFound is not None:
        _phoneticsReplace(vFound)
        vFound = oDoc.findNext(vFound.End, oFind)


def replaceTagsWithFormatting():
    oDoc = getDoc()
    oFind = oDoc.createSearchDescriptor()

    oFind.SearchRegularExpression=True  #Use regular expressions
    oFind.setSearchString('<b>.*?</b>')
    vFound = oDoc.findFirst(oFind)

    while vFound is not None:
        vFound.CharWeight = BOLD
        vFound = oDoc.findNext(vFound.End, oFind)

    oFind.setSearchString('<i>.*?</i>')
    vFound = oDoc.findFirst(oFind)

    while vFound is not None:
        vFound.CharPosture = ITALIC
        vFound = oDoc.findNext(vFound.End, oFind)

    oFind.setSearchString('<sub>.*?</sub>')
    vFound = oDoc.findFirst(oFind)

    while vFound is not None:
        vFound.CharEscapement = -33
        vFound = oDoc.findNext(vFound.End, oFind)

    oFind.setSearchString('<sup>.*?</sup>')
    vFound = oDoc.findFirst(oFind)

    while vFound is not None:
        vFound.CharEscapement = 33
        vFound = oDoc.findNext(vFound.End, oFind)


def replaceTagsWithStylesReset():
    oDoc = getDoc()
    dispatcher = uno.getComponentContext().ServiceManager.createInstance("com.sun.star.frame.DispatchHelper")
    oFrame = oDoc.CurrentController.Frame
    dispatcher.executeDispatch(oFrame, ".uno:SelectAll", "", 0, [])
#     oFind = oDoc.createSearchDescriptor()
# 
#     oFind.SearchRegularExpression=True
#     oFind.setSearchString('.*')
#     vFound = oDoc.findFirst(oFind)
    vFound = oDoc.getCurrentController().getSelection()
    vFound.getByIndex(0).CharStyleName = 'e2u_ukr_plain'


def replaceTagsWithStyles():
#     replaceTagsWithStylesReset()

    _replaceTagsWithStyles('b', 'e2u_eng_bold')
    _replaceTagsWithStyles('i', 'e2u_ukr_italic')
    _replaceTagsWithStyles('sub', 'e2u_sub')
    _replaceTagsWithStyles('sup', 'e2u_sup')


def _replaceTagsWithStyles(tag, styleName):
    oDoc = getDoc()
    oFind = oDoc.createSearchDescriptor()

    oFind.SearchRegularExpression=True  #Use regular expressions
    oFind.setSearchString('<%s>.*?</%s>' % (tag, tag))
    vFound = oDoc.findFirst(oFind)

    while vFound is not None:
        vFound.CharStyleName = styleName
        vFound = oDoc.findNext(vFound.End, oFind)



def _onAllSelection(func):
    oDoc = getDoc()
     
    oCurSelection = oDoc.getCurrentSelection()
    if oCurSelection.supportsService("com.sun.star.text.TextRanges"):
        nCount = oCurSelection.Count
        for i in range(0, nCount):
            oTextRange = oCurSelection.getByIndex(i)
            
            func(oTextRange)


# VERY SLOW on large text!
def replacePhoneticsInSelection():
    _onAllSelection(_phoneticsReplace)


def _phoneticsReplace(oTextRange):       
    txt =  oTextRange.getString()     
    
    txt2 = _convertPhonetics(txt)

    oTextRange.setString(txt2)


def _convertPhonetics(txt):
    txt2 = ''
    for c in txt:
        if( c in phoneticsToUnicodeMap ):
            newChar = phoneticsToUnicodeMap[ c ]
        else:
            newChar = c
        txt2 += newChar 
    return txt2



def replaceAllFormatsWithTags():
    _replaceFormatWithTags("CharWeight", BOLD, "b")
    _replaceFormatWithTags("CharPosture", ITALIC, "i")
    replaceSubscriptFormatWithTags()


def commonCleanupSpaces():
    oDoc = getDoc()
    oReplace = oDoc.createReplaceDescriptor()

    oReplace.SearchRegularExpression=True  #Use regular expressions
    oReplace.SearchAll=True                #Do the entire document

    oReplace.setSearchString('[\s]+|\\t')
    oReplace.setReplaceString(' ')

    oDoc.replaceAll(oReplace)

    oReplace.setSearchString('[ \\t]+$')
    oReplace.setReplaceString('')

    oDoc.replaceAll(oReplace)


def removeAllTags():
    oDoc = getDoc()
    oReplace = oDoc.createReplaceDescriptor()

    oReplace.SearchRegularExpression=True  #Use regular expressions
    oReplace.SearchAll=True                #Do the entire document

    oReplace.setSearchString('</?([bi]|sub)>')
    oReplace.setReplaceString('')

    oDoc.replaceAll(oReplace)



def _replaceFormatWithTags(fmtName, fmt, tag):
    oDoc = getDoc()

    oReplace = oDoc.createReplaceDescriptor()
    
    oReplace.setSearchString(".*")
    oReplace.setReplaceString("<"+tag+">&</"+tag+">")
    oReplace.SearchRegularExpression=True  #Use regular expressions
    oReplace.SearchStyles=True             #We want to search styles
    oReplace.SearchAll=True                #Do the entire document
    
    SrchAttributes = [ PropertyValue(Name=fmtName, Value=fmt) ]
    
    oReplace.setSearchAttributes(SrchAttributes)
#     oReplace.setReplaceAttributes(ReplAttributes)
    
    oDoc.replaceAll(oReplace)

# compact tags

    oReplace = oDoc.createReplaceDescriptor()
    oReplace.setSearchString("</"+tag+">( *)<"+tag+">")
    oReplace.setReplaceString("$1")
    oReplace.SearchRegularExpression=True  #Use regular expressions
    oReplace.SearchAll=True                #Do the entire document

    oDoc.replaceAll(oReplace)

    oReplace.setSearchString("<"+tag+">( *)</"+tag+">")
    oReplace.setReplaceString("$1")

    oDoc.replaceAll(oReplace)

# we want </b>; <b>1.</b>
#     oReplace.setSearchString("</"+tag+">([;,~=] *|[/’'-])<"+tag+">")
    oReplace.setSearchString("</"+tag+">([,~=] *|[/’'-])<"+tag+">")
    oReplace.setReplaceString("$1")

    oDoc.replaceAll(oReplace)

    oReplace.setSearchString("(~ *)(<"+tag+">)")
    oReplace.setReplaceString("$2$1")

    oDoc.replaceAll(oReplace)

    if tag == 'i':
        oReplace.setSearchString(" +</"+tag+">")
        oReplace.setReplaceString("</"+tag+"> ")
    
        oDoc.replaceAll(oReplace)
        

def replaceSubscriptFormatWithTags():
    _replaceSubscriptFormatWithTags('sub', -33)
    _replaceSubscriptFormatWithTags('sup', 33)


def _replaceSubscriptFormatWithTags(tag, value):
    
    oDoc = getDoc()

    oReplace = oDoc.createReplaceDescriptor()
    
    oReplace.setSearchString(".*")
    oReplace.setReplaceString("<"+tag+">&</"+tag+">")
    oReplace.SearchRegularExpression=True  #Use regular expressions
    oReplace.SearchStyles=True             #We want to search styles
    oReplace.SearchAll=True                #Do the entire document

# Escapement.Auto    
#     SrchAttributes = [ PropertyValue(Name='CharAutoEscapement', Value=True)]
    SrchAttributes = [ PropertyValue(Name='CharEscapement', Value=value) ]
#     SrchAttributes = [ PropertyValue(Name='CharEscapementHeight', Value=58) ]
    
    # Set the attributes in the replace descriptor
    oReplace.setSearchAttributes(SrchAttributes)
#     oReplace.setReplaceAttributes(ReplAttributes)
    
    oDoc.replaceAll(oReplace)



phoneticsToUnicodeMap = {
    '\u0041': 'ʌ',
    '\u0043': 'ð',
    '\u0044': 'ʧ',
    '\u0045': 'ɜ',
    '\u0046': 'ε',
    '\u0047': 'ʤ',
    '\u0048': 'uː',
    '\u0049': 'ɪ',
    '\u004A': 'iː',
    '\u004C': 'ɔː',
    '\u004E': 'ŋ',
    '\u004F': 'ɔ',
    '\u0050': 'ɒ',
    '\u0051': 'ɑ',
    '\u0052': 'ɑː',
    '\u0053': 'ʃ',
    '\u0054': 'θ',
    '\u0056': 'ʋ',
    '\u0057': 'ɘː',
    '\u005A': 'ʒ',
    '\u0071': 'ɘ',
    '\u0078': 'æ'
}


import unittest

class MyTest(unittest.TestCase):
    def test(self):
        self.assertEqual(_convertPhonetics('q'), 'ɘ')
        self.assertEqual(_convertPhonetics('X'), 'X')

#if __name__ == "__main__":
