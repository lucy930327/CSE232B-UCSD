/**
 * Created by DYQ on 31/1/2017.
 */
import java.nio.file.*;
import java.nio.charset.*;
import java.io.IOException;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.w3c.dom.Node;
import java.util.List;
import java.util.ArrayList;

public class XPath {
    public static void main(String[] args) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get("XPathTest.txt"), Charset.forName("UTF-8"));
        for (String expr:lines) {
            ANTLRInputStream input = new ANTLRInputStream(expr);
            XPathLexer lexer = new XPathLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            XPathParser parser = new XPathParser(tokens);
            ParseTree tree = parser.ap();
            XPathBuilder eval = new XPathBuilder();
            ArrayList<Node> finalResult = eval.visit(tree);
            System.out.println(expr+"\n===========================================================================");
            for (Node n : finalResult) {
                System.out.println(n.getTextContent());
            }
            System.out.println(finalResult.size()+" results\n");
        }
    }
}