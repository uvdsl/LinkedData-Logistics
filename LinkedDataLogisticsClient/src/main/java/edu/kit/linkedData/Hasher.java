package edu.kit.linkedData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.semanticweb.yars.nx.BNode;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.Callback;
import org.semanticweb.yars.nx.parser.NxParser;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import cl.uchile.dcc.blabel.cli.LabelRDFGraph;
import cl.uchile.dcc.blabel.label.GraphColouring.HashCollisionException;
import cl.uchile.dcc.blabel.label.GraphLabelling.GraphLabellingArgs;
import cl.uchile.dcc.blabel.label.GraphLabelling.GraphLabellingResult;

public class Hasher {

	public HashCode hashRDFGraph(Iterable<Node[]> stmts, HashFunction hFunction)
			throws InterruptedException, HashCollisionException {
		Collection<Node[]> graph = new LinkedList<>();
		stmts.forEach(triple -> graph.add(triple));
		final List<String> actual = new ArrayList<String>();

		GraphLabellingResult glr = LabelRDFGraph.labelGraph(graph, new Callback() {

			@Override
			public void startDocument() {
			}

			@Override
			public void endDocument() {
			}

			@Override
			public void processStatement(Node[] nx) {
				String triple = toN3(nx);
				// System.out.println(triple);
				actual.add(triple);
			}

			@Override
			protected void endDocumentInternal() {
				// TODO Auto-generated method stub
				
			}

			@Override
			protected void processStatementInternal(Node[] nx) {
				// TODO Auto-generated method stub
				String triple = toN3(nx);
				// System.out.println(triple);
				actual.add(triple);
			}

			@Override
			protected void startDocumentInternal() {
				// TODO Auto-generated method stub
				
			}

		}, new GraphLabellingArgs(), "", true);

		HashCode hash = glr.getHashGraph().getGraphHash();
		
		 // for(Node[] out:glr.getGraph()){ System.out.println(this.toN3(out)); }
		 
		return hash;

	}

	public HashCode hashRDFGraphFile(String filepath, HashFunction hFunction)
			throws InterruptedException, HashCollisionException, FileNotFoundException {
		final List<String> actual = new ArrayList<String>();
		InputStream is = new FileInputStream(filepath);
		NxParser nxp = new NxParser(is, Charset.forName("utf-8"));

		Collection<Node[]> stmts = new ArrayList<Node[]>();
		boolean bnode = false;
		while (nxp.hasNext()) {
			Node[] triple = nxp.next();
			if (triple.length >= 3) {
				stmts.add(new Node[] { triple[0], triple[1], triple[2] });
				bnode = bnode | (triple[0] instanceof BNode) | (triple[2] instanceof BNode);
			} else {
				System.err.println("Not a triple " + toN3(triple));
			} 
		}
		GraphLabellingResult glr = LabelRDFGraph.labelGraph(stmts, new Callback() {

			@Override
			public void startDocument() {
			}

			@Override
			public void endDocument() {
			}

			@Override
			public void processStatement(Node[] nx) {
				String triple = toN3(nx);
				// System.out.println(triple);
				actual.add(triple);
			}

			@Override
			protected void endDocumentInternal() {
				// TODO Auto-generated method stub
				
			}

			@Override
			protected void processStatementInternal(Node[] nx) {
				// TODO Auto-generated method stub
				String triple = toN3(nx);
				// System.out.println(triple);
				actual.add(triple);
			}

			@Override
			protected void startDocumentInternal() {
				// TODO Auto-generated method stub
				
			}

		}, new GraphLabellingArgs(), "", true);

		HashCode hash = glr.getHashGraph().getGraphHash();
		
		 // for(Node[] out:glr.getGraph()){ System.out.println(this.toN3(out)); }
		 
		return hash;

	}

	public Collection<Node[]> createGraph(String[] input) {

		NxParser iter = new NxParser(Arrays.asList(input).iterator());

		// load the graph into memory
		Collection<Node[]> stmts = new ArrayList<Node[]>();
		boolean bnode = false;
		while (iter.hasNext()) {
			Node[] triple = iter.next();
			if (triple.length >= 3) {
				stmts.add(new Node[] { triple[0], triple[1], triple[2] });
				bnode = bnode | (triple[0] instanceof BNode) | (triple[2] instanceof BNode);
			} else {
				System.err.println("Not a triple " + toN3(triple));
			}
		}
		return stmts;

	}

	
	// is only accepting file paths 
	public String createHashFromPath(String in, String out)
			throws FileNotFoundException, InterruptedException, HashCollisionException {

		Model model = ModelFactory.createDefaultModel();
		model.read(new FileInputStream(in), null, "TTL");
		model.write(new FileOutputStream(new File(out)), "N-TRIPLE");

		HashFunction hashingAlgorithm = Hashing.sha256();

		HashCode code = hashRDFGraphFile(out, hashingAlgorithm);
		String Hash = code.toString();

		return Hash;

	}
	
	private String toN3(Node[] ns) {
		StringBuilder buf = new StringBuilder();
		for (Node n : ns) {
			buf.append(n.toString());
			buf.append(" ");
		}
		buf.append(".");

		return buf.toString();
	}

}

/*
 * <dependency> <groupId>org.apache.jena</groupId> <artifactId>jena</artifactId>
 * <version>3.11.0-SNAPSHOT</version> </dependency>
 */