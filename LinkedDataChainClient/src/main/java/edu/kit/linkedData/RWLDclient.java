package edu.kit.linkedData;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.common.hash.HashCode;

import org.semanticweb.yars.nx.Node;

import cl.uchile.dcc.blabel.label.GraphColouring.HashCollisionException;

/**
 * This Class is implements a client for requesting linked data, as shown by example:<br>
 * {@link https://github.com/kaefer3000/rwldclient} as of 2018/11/28.
 * @author Christoph
 *
 */
public class RWLDclient {

	private Client client;
	private Hasher hasher;

	public RWLDclient() {
		client = ClientBuilder.newClient();
		hasher = new Hasher();
	}

	/**
	 * Request linked data from the input uri.
	 * 
	 * @param uri
	 * @return <b>{@literal Iterable<Node[]>}</b> or <b>null</b>, if
	 *         unsuccessfull.<br>
	 *         Linked data, where <i>node[0]</i> is <i>Subject</i>, <i>node[1]</i>
	 *         is <i>Predicate</i>, <i>node[2]</i> is <i>Object</i> and further out
	 *         of bounds.
	 * 
	 * 
	 */
	public Iterable<Node[]> requestLinkedData(String uri) {
		Response res = client.target(uri).request(MediaType.valueOf("application/rdf+xml")).get();
		if (res.getStatus() >= 200 && res.getStatus() < 300) {
			// Successful request
			return res.readEntity(new GenericType<Iterable<Node[]>>() {
			});
		} else {
			// Request was not successful.
			System.err.println("Request for URI returned: " + res.getStatus());
			return null;
		}
	}

	/**
	 * Query the input graph for all Objects of the specified Predicate.
	 * 
	 * @param graph
	 * @param predicate
	 * @return <b>{@literal List<Node>}</b> or <b>null</b>, if no Objects were
	 *         found.<br>
	 */
	public List<Node> queryObjectForPredicate(String uri, String predicate) {
		List<Node> result = new LinkedList<>();
		for (Node[] nx : requestLinkedData(uri)) {
			// search for predicate (that is nx[1])
			if (nx[1].toString().equals(predicate)) {
				// and print object (that is nx[2])
				result.add(nx[2]);
			}
		}
		return result.isEmpty() ? null : result;
	}

	/**
	 * Hash the <b>{@literal Iterable<Node[]>}</b> graph, as shown in the paper.
	 * 
	 * @param graph
	 * @return <b>byte[]</b> hash of graph
	 */
	public byte[] hashGraph(Iterable<Node[]> graph) {
		HashCode hash = null;
		try {
			 hash = this.hasher.hashRDFGraph(graph);
		} catch (InterruptedException | HashCollisionException e) {
			e.printStackTrace();
			return null;
		}
		System.out.println("\nHash:");
		System.out.println("\t 0x" + hash.toString());
		System.out.println("\t " + Arrays.toString(hash.asBytes()));
	    return  hash.asBytes();
	}

}
