#!/bin/env groovy

import org.apache.xerces.dom.TextImpl
import org.odftoolkit.odfdom.doc.OdfSpreadsheetDocument
@Grapes(
    @Grab(group='org.odftoolkit', module='odfdom-java', version='0.8.7')
//    @Grab(group='org.apache.odftoolkit', module='odfdom-java', version='0.8.11-incubating')
)


import org.odftoolkit.odfdom.doc.OdfTextDocument
import org.odftoolkit.odfdom.dom.element.office.OfficeTextElement
import org.odftoolkit.odfdom.dom.element.style.StyleTextPropertiesElement
import org.odftoolkit.odfdom.pkg.OdfElement
import org.w3c.dom.Node

import groovy.transform.Field

import org.odftoolkit.odfdom.dom.element.text.TextListElement
import org.odftoolkit.odfdom.dom.element.text.TextListItemElement
import org.odftoolkit.odfdom.dom.element.text.TextPElement
import org.odftoolkit.odfdom.incubator.doc.style.OdfStyle
import org.odftoolkit.odfdom.incubator.doc.text.OdfTextList
import org.odftoolkit.odfdom.incubator.doc.text.OdfTextParagraph
import org.odftoolkit.odfdom.incubator.doc.text.OdfTextSpan


@Field
File outFile = new File('slovnyk_Shymkiv.txt')
outFile.text = ""

OdfTextDocument document = OdfTextDocument.loadDocument("slovnyk_Shymkiv.odt")


// get root of all content of a text document
OfficeTextElement officeText = document.getContentRoot();

int paraCnt = 0

@Field
String paraStyleName = ""
@Field 
boolean inList = false
@Field
boolean hasBody = false
@Field
String outText = ""

// get first paragraph
TextPElement paragraph = OdfElement.findFirstChildNode(TextPElement.class, officeText)

def paraIt = paragraph.getParentNode().iterator()
for(Node node = paraIt.next(); paraIt.hasNext(); node = paraIt.next()) {

	println "\n\n---------------------- " + node.getClass()

	if( node instanceof OdfTextParagraph ) {

		paragraph = (OdfTextParagraph)node
		
		if( paragraph.getStyleName() in ["Заголов"] ) {
			outText = ""
			continue
		}

		println paragraph
		//	println "autoStyle: " + paragraph.getAutomaticStyle()

		String newStyleName = getParagraphStyleName(paragraph)
		println "newStyleName: " + newStyleName

		if( newStyleName == "Слово" || newStyleName == "буква" ) {
			if( paraStyleName ) {
				if( hasBody && outText.trim() ) {
				    println "---------------"
					writeText()
				    println "---------------\n"
				}
			}

			outText = ""
			hasBody = false
			paraStyleName = newStyleName
		}

		if( newStyleName in ["Толков", "Перев", "Словосочетания", "Словосочетание 2"] ) {
			println "<<<<<<<<<<<< Got body"
			hasBody = true
		}

		if( newStyleName in ["Перев"] ) {
			if( ! outText.endsWith(" ") )
				outText += " "
		}


        def paraText = getParagraphText(paragraph)


		if( newStyleName in ["Толков", "Треугольник", "WWNum11"] ) {
			paraText = "<div class=\"explanation\">$paraText</div>"
			// 25B2 - triangle
		}
//		else if( newStyleName in ["Словосочетания", "Словосочетание 2"] ) {
//		    paraText = paraText.replace("; <b>", '<li><b>')
//		}

		outText += paraText

		String styleName = ""
	}
	else if( node instanceof OdfTextList ) {

		OdfTextList textList = (OdfTextList) node
		
		println "-- list: " + textList

		String newStyleName = textList.getTextStyleNameAttribute() // ? textList.getAutomaticStyle().getAttribute("style:parent-style-name") : textList.getStyleName()
		println "newStyleName: " + newStyleName

		if( newStyleName in ["WWNum17"] ) {
			println "<<<<<<<<<<<< Got body"
			hasBody = true
		}

//		if( newStyleName in ["Словосочетания", "Словосочетание 2"] ) {
//		    paraText = paraText.replace("; <b>", '<li><b>')
//		}

		inList = true

		TextListItemElement listItem = OdfElement.findFirstChildNode(TextListItemElement.class, textList)
		for( ; listItem; listItem = OdfElement.findNextChildNode(TextListItemElement.class, listItem) ) {

			// <text:p> or <text:list>
			for(Node listItemChild = listItem.getFirstChild(); listItemChild != null; listItemChild = listItemChild.getNextSibling()) {
				def listItemPara
				if( listItemChild instanceof OdfTextList ) {
					TextListItemElement innerList = OdfElement.findFirstChildNode(TextListItemElement.class, listItemChild)
					listItemPara = OdfElement.findFirstChildNode(OdfTextParagraph.class, innerList)
				}
				else {
					listItemPara = listItemChild
				}

				if( listItemPara ) {
					OdfTextParagraph para = (OdfTextParagraph)listItemPara
					String innerStyleName = getParagraphStyleName(para)

					def paraText = getParagraphText(para)

					if( newStyleName in ["WWNum11"] ) {
						paraText = "<div class=\"explanation\">\u25B2 $paraText</div>"
						// 25B2 - triangle
					}
					else if( newStyleName in ["WWNum17"] ) {
						paraText = "<br>\u25B7 $paraText"		// White Right-Pointing Triangle
					}
					else {
//					if( innerStyleName in ["Словосочетания", "Словосочетание 2"] ) {
						paraText = "<li>" + paraText.replaceAll("; <b>|;<b> ", '<li><b>')
					}

					outText += paraText
					
				}
				else {
					println "HMMMMMM, listItemPara not found"
				}
			}
//			OdfTextSpan listItemText = OdfElement.findFirstChildNode(OdfTextSpan.class, listItemPara)
//
//			for( ; listItemText; listItemText = OdfElement.findNextChildNode(OdfTextSpan.class, listItemText)) {
//				outText += listItemText.getTextContent()
//			}
		}
		
		inList = false
	}
}

writeText()



String getParagraphStyleName(OdfTextParagraph paragraph) {
	return paragraph.getAutomaticStyle() ? paragraph.getAutomaticStyle().getAttribute("style:parent-style-name") : paragraph.getStyleName()
}

String cleanup(String outText) {

	outText = outText.replaceAll('</i>( *)<i>', '$1')
	outText = outText.replaceAll(' +</i>', '</i> ')

	outText = outText.replaceAll('</b>( *)<b>', '$1')
	outText = outText.replaceAll(' +</b>', '</b> ')

    outText = outText.replace('', '~')
    outText = outText.replace('', '•')
	outText = outText.replace('', '\u25C6')	// diamond
	outText = outText.replace('', '\u25A1')	// white square

//	outText = outText.replaceAll(' *', ' <i>див. також</i> ')
	outText = outText.replace('', '\u25B7\u25B7')	// White Right-Pointing Triangle
	outText = outText.replace('', ' ')
	
	outText = outText.replaceAll('[\u2019\u02BCˈ\']', '\u2019')
    
	return outText
}


void writeText() {
	outText = outText.replaceAll('<li><b>.*?</b>', '$0 =')

	def match2 = outText =~ /^<b>([^\/(<\[]*)/
	assert match2.size() > 0, "Failed $outText: " + match2
	String mainWordBase = match2[0][1]
	assert mainWordBase, "Failed $outText: " + match2

	outText = outText.replaceAll('([a-z]+)\\(s\\)', '$1, $1s')
	outText = outText.replaceAll('([a-z]+)//y/ies', '$1y, $1ies')

	outText = outText.replaceAll(/<b>‡<\/b>( [a-z'-]+)+/, '<a href="/data/shymkiv\\\\_back.pdf">$0</a>')
	
	println "My text <<<<<<<<<<<< " + outText

	def match = outText =~ /^<b>(.*?)<\/b>/
	String mainWord = match[0][1]

	println "\nmainWord: " + mainWord
	if( mainWordBase != mainWord ) {
	    println "\nmainWordBase: " + mainWordBase
	}


    def searchText = outText
        .replaceAll(/<i>.*?<\/i>/, '|')
        .replaceAll(/<sup>.*?<\/sup>/, '')
        .replaceAll(/<div class="explanation">.*?<\/div>/, '|')
		.replaceAll(/<\/?b>/, '')
		.replaceAll(/<\/?li> *|<br> *| :: /, '|')
		.replaceAll(/\|{2,}/, '|')
		.replaceAll(/ *\| */, '|')
		.replaceAll(/[\u25B7\u25C6\u25A1•]+/, '|')
	
	searchText = new ArrayList(Arrays.asList(searchText.split(/\|/))).unique().join('|')
		
	searchText = searchText.replaceAll('~(s|y|ies)', mainWordBase + '$1')
	searchText = searchText.replace('~', mainWord)

	outText += "_" + searchText + "_" + mainWord + "_12"

	outFile << outText << "\n"
}

private String getParagraphText(Node paragraph) {
	String outText_ = ""
	
	for(Iterator it = paragraph.getChildNodes().iterator() ; it.hasNext(); ) {

		Node child = it.next();
		
		if( child instanceof OdfTextSpan ) {
			OdfTextSpan span = (OdfTextSpan)child
			
			println span.getStyleName() + " ::: " + span.getTextContent()
	
			if( span.getStyleName() == "Транскрипция" )
				continue
	
			outText_ += getSpanText(span, paragraph)
		}
		else if ( child instanceof TextImpl ) {
			outText_ += child.getTextContent()
		}
	}
	
	
	outText_ = cleanup(outText_)

	return outText_
}

	
private String getSpanText(OdfTextSpan span, Node para) {
	boolean italic = false
	boolean bold = false
	boolean slovo = false
	boolean superscript = false

	if( para instanceof OdfTextParagraph ) {
		if( getParagraphStyleName((OdfTextParagraph)para) == "Слово" ) {
			slovo = true
			println "\tSlovo"
		}
	}
	
	if( span.getAutomaticStyle() ) {
		//			OdfStyle style = OdfElement.findFirstChildNode(OdfStyle.class, span.getAutomaticStyle())
		StyleTextPropertiesElement styleElement = OdfElement.findFirstChildNode(StyleTextPropertiesElement.class, span.getAutomaticStyle())
//		println "st: " + span.getAutomaticStyle() + "\n\t" + (styleElement ? styleElement.getFoFontWeightAttribute() : "-")
//		println "\tstyleElement: " + styleElement + ": " + ((!styleElement || !styleElement.getFoFontWeightAttribute()) && slovo)
		italic = styleElement && styleElement.getFoFontStyleAttribute() == "italic"
		bold = (styleElement && styleElement.getFoFontWeightAttribute() == "bold") || ((!styleElement || !styleElement.getFoFontWeightAttribute()) && slovo)
		superscript = styleElement && styleElement.getStyleTextPositionAttribute()
	}
	else {
		bold = (span.getStyleName() == "Колонтитул")
	}

	String outText_ = ""
	
	if( superscript ) {
		outText_ += "<sup>"
	}
	if( bold ) {
		outText_ += "<b>"
	}
	if( italic ) {
		outText_ += "<i>"
	}
	outText_ += span.getTextContent()

	if( italic ) {
		outText_ += "</i>"
	}
	if( bold ) {
		outText_ += "</b>"
	}
	if( superscript ) {
		outText_ += "</sup>"
	}

	if( span.getNextSibling() && span.getNextSibling().getNodeType()  == org.w3c.dom.Node.TEXT_NODE ) {
//		println "text class " + span.getNextSibling().getClass() 
//		println "== TEXT: " + span.getNextSibling().getTextContent()
//		outText_ += span.getNextSibling().getTextContent()
	}
	
	return outText_
}
