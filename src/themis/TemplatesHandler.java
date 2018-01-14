/* This class is based in this code:
 * https://github.com/plutext/docx4j/blob/master/src/samples/docx4j/org/docx4j/samples/VariableReplace.java
 * templates should have the placeholders wrapped in ${} for example ${placeholder} for this to work
 * */

package themis;

import javax.xml.bind.JAXBContext;
import java.io.IOException;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import javax.xml.bind.JAXBException;
import java.io.FileNotFoundException;
import java.util.HashMap;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.model.datastorage.migration.VariablePrepare;
import java.io.File;

public class TemplatesHandler{
    
    private WordprocessingMLPackage documentTemplate;
    private MainDocumentPart documentPart;
    private String filePath; 

    public TemplatesHandler (String pathToFile) throws Docx4JException, FileNotFoundException, Exception{
        documentTemplate = WordprocessingMLPackage.load(new java.io.File(pathToFile)); 
        VariablePrepare.prepare(documentTemplate); //fix placeholder keys (${placeholder}) split across multiple runs (<w:r>)
        documentPart = documentTemplate.getMainDocumentPart();
        filePath = pathToFile;
    }

    public WordprocessingMLPackage getDocumentTemplate()  {
        return documentTemplate;
    }
    
    public String getFilePath(){
        return filePath;
    }

    public void replaceTextInDocument(HashMap<String, String> mappings) throws Exception{
        // unmarshallFromTemplate requires string input
        //String xml = XmlUtils.marshaltoString(documentPart.getJaxbElement(), true);
        // Do it...
        //Object obj = XmlUtils.unmarshallFromTemplate(xml, mappings);
        // Inject result into docx
        //documentPart.setJaxbElement((org.docx4j.wml.Document) obj);
        documentPart.variableReplace(mappings);
    }
    
    public void saveDocumentTemplate(String path) throws Docx4JException, IOException {
        writeDocxToStream(documentTemplate, path);
        
        //SaveToZipFile saver = new SaveToZipFile(documentTemplate);
        //saver.save(path);
    }

    private void writeDocxToStream(WordprocessingMLPackage template, String target) throws IOException, Docx4JException {
        File f = new File(target);
        template.save(f);
    }
}

