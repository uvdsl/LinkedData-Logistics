package edu.kit.linkedData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.google.common.hash.HashCode;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.Callback;

import cl.uchile.dcc.blabel.cli.LabelRDFGraph;
import cl.uchile.dcc.blabel.label.GraphColouring.HashCollisionException;
import cl.uchile.dcc.blabel.label.GraphLabelling.GraphLabellingArgs;
import cl.uchile.dcc.blabel.label.GraphLabelling.GraphLabellingResult;

public class Hasher {
	private final static char[] hexArray = "0123456789abcdef".toCharArray();

	public HashCode hashRDFGraph(Iterable<Node[]> stmts) throws InterruptedException, HashCollisionException {
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
			}

			@Override
			protected void processStatementInternal(Node[] nx) {
				String triple = toN3(nx);
				// System.out.println(triple);
				actual.add(triple);
			}

			@Override
			protected void startDocumentInternal() {
			}
		}, new GraphLabellingArgs(), "", true);

		HashCode hash = glr.getHashGraph().getGraphHash();
		// for(Node[] out:glr.getGraph()){ System.out.println(this.toN3(out)); }
		return hash;

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

	public String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

}

/*
 * <dependency> <groupId>org.apache.jena</groupId> <artifactId>jena</artifactId>
 * <version>3.11.0-SNAPSHOT</version> </dependency>
 */