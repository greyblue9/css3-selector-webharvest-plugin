package webharvest.css3selector;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.webharvest.exception.PluginException;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.processors.WebHarvestPlugin;
import org.webharvest.runtime.variables.ListVariable;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.runtime.variables.Variable;
import org.xml.sax.InputSource;

import se.fishtank.css.selectors.dom.DOMNodeSelector;

public class Css3SelectorPlugin extends WebHarvestPlugin
{
    private static DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

    private static TransformerFactory     tf  = TransformerFactory.newInstance();

    @Override
    public Variable executePlugin(Scraper scraper, ScraperContext context)
    {
        Variable xml = executeBody(scraper, context);
        String selector = evaluateAttribute("selector", scraper);
        StringReader reader = new StringReader(xml.toString());

        try
        {
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(reader));

            ListVariable listVariable = new ListVariable();
            DOMNodeSelector domNodeSelector = new DOMNodeSelector(doc);
            Set<Node> nodes = domNodeSelector.querySelectorAll(selector);
            for (Node node : nodes)
            {
                listVariable.addVariable(new NodeVariable(serialize(node)));
            }
            return listVariable;

        }
        catch (Exception e)
        {
            throw new PluginException(e);
        }
    }

    @Override
    public String getName()
    {
        return "css3";
    }

    @Override
    public String[] getRequiredAttributes()
    {
        return new String[] { "selector" };
    }

    @Override
    public String[] getValidAttributes()
    {
        return new String[] { "selector" };
    }

    private String serialize(Node node) throws Exception
    {
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StreamResult result = new StreamResult(new StringWriter());
        DOMSource source = new DOMSource(node);
        transformer.transform(source, result);
        return result.getWriter().toString();
    }

}
