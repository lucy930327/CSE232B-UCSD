/**
 * Created by DYQ on 30/1/2017.
 */
import java.io.File;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

public class XPathBuilder extends XPathBaseVisitor<ArrayList<Node>>{
    private ArrayList<Node> curr = new ArrayList();
    private boolean Attribute = false;


    @Override
    public ArrayList<Node> visitFilePath(XPathParser.FilePathContext ctx) {
        File xml=new File(ctx.fileName().getText());
        DocumentBuilder b=null;
        Document d=null;
        ArrayList<Node> ret = new ArrayList();
        try{
            b=DocumentBuilderFactory.newInstance().newDocumentBuilder();
        }catch(ParserConfigurationException e) {
            e.printStackTrace();
        }
        try{
            if(b!=null) d=b.parse(xml);
        }catch(Exception e) {
            e.printStackTrace();
        }
        if(d!=null){
            d.getDocumentElement().normalize();
            ret.add(d);
        }
        curr=ret;
        return ret;
    }

    @Override
    public ArrayList<Node> visitApChildren(XPathParser.ApChildrenContext ctx) {
        return visitChildren(ctx);
    }

    private static ArrayList<Node> children(Node n){
        NodeList nchildren=n.getChildNodes();
        ArrayList<Node> chld = new ArrayList();
        for(int i=0;i<nchildren.getLength();i++){
            chld.add(nchildren.item(i));
        }
        return chld;
    }

    @Override
    public ArrayList<Node> visitApAll(XPathParser.ApAllContext ctx) {
        visit(ctx.doc());
        LinkedList<Node> llst = new LinkedList(curr);
        ArrayList<Node> ret = new ArrayList(curr);
        while(!llst.isEmpty()){
            Node n=llst.poll();
            llst.addAll(children(n));
            ret.addAll(children(n));
        }
        curr=ret;
        return visit(ctx.rp());
    }

    @Override
    public ArrayList<Node> visitAllChildren(XPathParser.AllChildrenContext ctx) {
        ArrayList<Node> ret = new ArrayList();
        for(Node n:curr) {ret.addAll(children(n));}
        curr = ret;
        return ret;
    }

    @Override
    public ArrayList<Node> visitRpwithP(XPathParser.RpwithPContext ctx) {
        return visit(ctx.rp());
    }

    @Override
    public ArrayList<Node> visitTagName(XPathParser.TagNameContext ctx) {
        String name = ctx.getText();
        ArrayList<Node> ret = new ArrayList();
        for(Node n:curr) {
            for(Node ch:children(n)) {
                if(ch.getNodeName().equals(name)) {
                    ret.add(ch);
                }
            }
        }
        curr = ret;
        return ret;
    }

    @Override
    public ArrayList<Node> visitRpAll(XPathParser.RpAllContext ctx) {
        visit(ctx.rp(0));
        ArrayList<Node> ret = new ArrayList(curr);
        LinkedList<Node> llst = new LinkedList(curr);
        while(!llst.isEmpty()) {
            Node n = llst.poll();
            ret.addAll(children(n));
            llst.addAll(children(n));
        }
        curr = ret;
        ArrayList<Node> newcurr =visit(ctx.rp(1));
        curr = newcurr;
        return curr;
    }

    @Override
    public ArrayList<Node> visitParent(XPathParser.ParentContext ctx) {
        HashSet<Node> ret = new HashSet();
        for(Node n: curr){
            ret.add(n.getParentNode());
        }
        return new ArrayList<Node>(ret);
    }

    @Override
    public ArrayList<Node> visitAttribute(XPathParser.AttributeContext ctx) {
        ArrayList<Node> ret = new ArrayList();
        Attribute = true;
        for(Node n: curr){
            String attr = ((Element)n).getAttribute(ctx.STR().getText());
            if(!attr.equals("")){
                ret.add(n);
                attr = ctx.STR().getText()+"=\""+ attr +"\"";
                System.out.println(attr);
            }
        }
        curr = ret;
        return curr;
    }

    @Override
    public ArrayList<Node> visitRpChildren(XPathParser.RpChildrenContext ctx) {
        visit(ctx.rp(0));
        ArrayList<Node> newcurr =visit(ctx.rp(1));
        curr = newcurr;
        return curr;
    }

    @Override
    public ArrayList<Node> visitTxt(XPathParser.TxtContext ctx) {
        for(Node n: curr){
            for(int i = 0; i < n.getChildNodes().getLength(); i++){
                Node c=n.getChildNodes().item(i);
                if(c.getNodeType() == javax.xml.soap.Node.TEXT_NODE && !c.getTextContent().equals("\n")){
                    System.out.print(c.getTextContent());
                }
            }
        }
        return curr;
    }

    @Override
    public ArrayList<Node> visitCurrent(XPathParser.CurrentContext ctx) {
        return curr;
    }

    @Override
    public ArrayList<Node> visitTwoRp(XPathParser.TwoRpContext ctx) {
        ArrayList<Node> ans = new ArrayList();
        ArrayList<Node> tempList = new ArrayList(curr);
        ans.addAll(visit(ctx.rp(0)));
        curr = tempList;
        ans.addAll(visit(ctx.rp(1)));
        curr = ans;
        return ans;
    }

    @Override
    public ArrayList<Node> visitRpFilter(XPathParser.RpFilterContext ctx) {
        ArrayList<Node> rec = visit(ctx.rp());
        ArrayList<Node> ret = new ArrayList<Node>();
        if (Attribute) {
            ArrayList<Node> filter= visit(ctx.filter());
                curr = filter;
                Attribute = false;
                return filter;
            }
        for(Node n:rec){
            curr=new ArrayList<Node>();
            curr.add(n);
            ArrayList<Node> filter= visit(ctx.filter());
            if(!filter.isEmpty())ret.add(n);
        }
        curr=ret;
        return curr;
    }

    @Override
    public ArrayList<Node> visitFltAnd(XPathParser.FltAndContext ctx) {
        ArrayList<Node> rec = new ArrayList<Node>(curr);
        ArrayList<Node> ret = new ArrayList<Node>();
        for(Node n:rec){
            ArrayList<Node> nlst=new ArrayList<Node>();
            nlst.add(n);
            curr=nlst;
            ArrayList<Node> f1= visit(ctx.filter(0));
            curr=nlst;
            ArrayList<Node> f2= visit(ctx.filter(1));
            if(!f1.isEmpty()&& !f2.isEmpty())ret.add(n);
        }
        curr=ret;
        return curr;
    }

    @Override
    public ArrayList<Node> visitFltEqual(XPathParser.FltEqualContext ctx) {
        ArrayList<Node> temp = curr;
        ArrayList<Node> l = visit(ctx.rp(0));
        curr = temp;
        ArrayList<Node> r = visit(ctx.rp(1));
        curr = temp;
        for (Node nl : l) {
            for (Node nr : r) {
                if (nl.isEqualNode(nr)) {
                    return curr;
                }
            }
        }
        return new ArrayList<Node>();
    }

    @Override
    public ArrayList<Node> visitFltNot(XPathParser.FltNotContext ctx) {
        ArrayList<Node> rec = new ArrayList<Node>(curr);
        ArrayList<Node> ret = new ArrayList<Node>();
        for(Node n:rec){
            curr=new ArrayList<Node>();
            curr.add(n);
            ArrayList<Node> filter= visit(ctx.filter());
            if(filter.isEmpty())ret.add(n);
        }
        curr=ret;
        return curr;
    }

    @Override public ArrayList<Node> visitFltOr(XPathParser.FltOrContext ctx) {
        ArrayList<Node> rec = new ArrayList<Node>(curr);
        ArrayList<Node> ret = new ArrayList<Node>();
        for(Node n:rec){
            ArrayList<Node> nlst=new ArrayList<Node>();
            nlst.add(n);
            curr=nlst;
            ArrayList<Node> f1= visit(ctx.filter(0));
            curr=nlst;
            ArrayList<Node> f2= visit(ctx.filter(1));
            if(!f1.isEmpty()|| !f2.isEmpty())ret.add(n);
        }
        curr=ret;
        return curr;
    }

    @Override
    public ArrayList<Node> visitFltIs(XPathParser.FltIsContext ctx) {
        ArrayList<Node> temp = curr;
        ArrayList<Node> l = visit(ctx.rp(0));
        curr = temp;
        ArrayList<Node> r = visit(ctx.rp(1));
        curr = temp;
        for (Node nl : l) {
            for (Node nr : r) {
                if (nl == nr) {
                    return curr;
                }
            }
        }
        return new ArrayList<Node>();
    }

    @Override
    public ArrayList<Node> visitFltwithP(XPathParser.FltwithPContext ctx) {
        return visit(ctx.filter());
    }

    @Override
    public ArrayList<Node> visitFltRp(XPathParser.FltRpContext ctx) {
        ArrayList<Node> temp = curr;
        ArrayList<Node> ret = visit(ctx.rp());
        curr = temp;
        return ret;
    }
}

