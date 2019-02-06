package edu.kit;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Nodes;
import org.web3j.crypto.CipherException;

import edu.kit.eth.LDHashNotary;
import edu.kit.eth.Web3jClient;
import edu.kit.linkedData.RWLDclient;
import edu.kit.ontology.LinkedOntology;

/**
 * 
 * @author Christoph
 *
 */
public class AppService {

	private static RWLDclient rwldClient = null;
	private static Web3jClient web3jClient = null;

	public AppService() {
		System.out.println(" [INFO] Starting Service ...");
		rwldClient = new RWLDclient();
	}

	/**
	 * Switch to another wallet.
	 * 
	 * @param wallet_pass
	 *            password to wallet
	 * @param wallet_home
	 *            path to wallet file
	 * @throws CipherException
	 *             when password is incorrect
	 * @throws IOException
	 *             when wallet file could not be found
	 */
	public void switchWallets(String wallet_pass, String wallet_home) throws IOException, CipherException {
		web3jClient = new Web3jClient(wallet_pass, wallet_home);
		System.out.println(" [INFO] Thank you and welcome home: " + web3jClient.getWalletAddress() + "\n");
	}

	/**
	 * Register a new product via the Smart Contract
	 * 
	 * @param birthCertURI
	 * @return gas used
	 */
	public String registerNewProduct(String birthCertURI, String newOwnerAddress) {
		if (web3jClient == null)
			App.switchWallets();
		try {
			String hash = rwldClient.hashGraph(rwldClient.requestLinkedData(birthCertURI));
			return web3jClient.loadContract(getContractAddress(birthCertURI)).addNewProduct(hash, hash, newOwnerAddress)
					.send().getGasUsed().toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 1. retrieve linked pedigree part by last part <br>
	 * 2. check validity of linked pedigree <br>
	 * 3. looking up smart contract address <br>
	 * 4. hash transfer document <br>
	 * 5. notarize hash and transfer ownership of smart contract
	 * 
	 * @param birthCertURI
	 * @param newTransferDocURI
	 * @param address
	 * @return Success/Error-String
	 */
	public String linkedTransfer(String birthCertURI, String newTransferDocURI, String address) {
		System.out.println("\nLinked Transfer <3");
		String contractAddress = this.getContractAddress(birthCertURI);
		String product = rwldClient.hashGraph(rwldClient.requestLinkedData(birthCertURI));
		String hash = rwldClient.hashGraph(rwldClient.requestLinkedData(newTransferDocURI));
		if (!this.notarize(contractAddress, product, hash).equals("6721975")) {
			if (!this.transfer(contractAddress, product, address).equals("6721975")) {
				return "Successfully completed Linked Transfer.";
			} else {
				return "Gas Limit reached on transferring ownership";
			}
		} else {
			return "Gas Limit reached on hash notarisation.";
		}
	}

	/**
	 * Look up the smart contract address in the birth cert of the product.
	 * 
	 * @param birthCertURI
	 * @return String Smart Contract Address
	 */
	public String getContractAddress(String birthCertURI) {
		// get Smart Contract Address
		System.out.println("\nRequesting BirthCert " + birthCertURI);
		List<Node> addresses = rwldClient.queryObjectForPredicate(birthCertURI,
				LinkedOntology.PREDICATE_CONTRACT_ADDRESS);
		if (addresses.size() != 1) {
			System.err.println(" [ERROR] Something is rotten in the state of the linked pedigree.");
			System.err.println(" [ERROR] addresses.size() != 1");
		}
		String contractAddress = strip(addresses.get(0).toString());
		System.out.println("SmartContract-Address: " + contractAddress);
		return contractAddress;
	}

	/**
	 * Retrieve the linked pedigree (URI : Content) by starting in the end of the
	 * linked pedigree chain.
	 * 
	 * @param lastTransferDocURI
	 * @return linked pedigree as mapping of URI to content
	 */
	public List<Map<String, String>> fetchFullLinkedPedigree(String lastTransferDocURI) {
		LinkedList<String> result = new LinkedList<>();
		result.add(lastTransferDocURI);
		return fetchFullLinkedPedigree(getLinkedPedigreeURIs(result));
	}

	/**
	 * Retrieve the linked pedigree URIs by starting in the end of the linked
	 * pedigree chain.
	 * 
	 * @param lastTransferDocURI
	 * @return linked pedigree as list of URIs
	 */
	public List<String> getLinkedPedigreeURIs(String lastTransferDocURI) {
		LinkedList<String> result = new LinkedList<>();
		result.add(lastTransferDocURI);
		return getLinkedPedigreeURIs(result);
	}

	/**
	 * Recursively retrieve the previous part (URI) of the linked pedigree.
	 * 
	 * @param currentPedigreeState
	 * @return linked pedigree as list of URIs
	 */
	private List<String> getLinkedPedigreeURIs(List<String> currentPedigreeState) {
		List<Node> prevPartList = rwldClient.queryObjectForPredicate(currentPedigreeState.get(0),
				LinkedOntology.PREDICATE_PREVIOUS_PART);
		if (prevPartList == null) {
			List<Node> type_objects = rwldClient.queryObjectForPredicate(currentPedigreeState.get(0),
					LinkedOntology.PREDICATE_TYPE);
			boolean isValid = false;
			for (Node n : type_objects) {
				if (strip(n.toString()).equals(strip(LinkedOntology.OBJECT_BIRTH_CERT))) {
					isValid = true;
				}
			}
			if (!isValid) {
				System.err.println(" [ERROR] Something is rotten in the state of the linked pedigree.");
				System.err.println(" [ERROR] birthCertDecalaration == null");
			}
			return currentPedigreeState;
		} else if (prevPartList.size() != 1) {
			System.err.println(" [ERROR] Something is rotten in the state of the linked pedigree.");
			System.err.println(" [ERROR] prevPartList.size() != 1");
			return currentPedigreeState;
		} else {
			currentPedigreeState.add(0, strip(prevPartList.get(0).toString()));
			return getLinkedPedigreeURIs(currentPedigreeState);
		}
	}

	/**
	 * Recursively retrieve the content of the linked pedigree's URIs returning a
	 * mapping of URI to content.
	 * 
	 * @param linkedPedigreeURIs
	 * @return linked pedigree as mapping of URI to content
	 */
	public List<Map<String, String>> fetchFullLinkedPedigree(List<String> linkedPedigreeURIs) {
		List<Map<String, String>> result = new LinkedList<>();
		for (String uri : linkedPedigreeURIs) {
			Iterable<Node[]> linkedData = rwldClient.requestLinkedData(uri);
			StringBuilder sBuilder = new StringBuilder();
			for (Node[] nx : linkedData) {
				sBuilder.append(Nodes.toString(nx) + "\n");
			}
			Map<String, String> mapping = new HashMap<>();
			mapping.put(uri, sBuilder.toString());
			result.add(mapping);
		}
		return result;
	}

	/**
	 * Prepare the linked pedigree for checking against the smart contract specified
	 * in the birth certificate. And the check it recursively.
	 * 
	 * @param fullLinkedPedigree
	 * @return list of mapping URI to boolean: true if hashes match (are correct and
	 *         correctly positioned)
	 */
	public List<Map<String, Boolean>> checkLinkedPedigree(List<Map<String, String>> fullLinkedPedigree) {
		if (web3jClient == null)
			App.switchWallets();
		List<String> listURIs = new LinkedList<>();
		for (Map<String, String> mapping : fullLinkedPedigree) {
			listURIs.addAll(mapping.keySet());
		}
		return checkLinkedPedigree(listURIs, web3jClient.loadContract(getContractAddress(listURIs.get(0))));
	}

	/**
	 * Recursively check the validity of the hashes stored via the smart contract
	 * against the hashes directly generated from the linked data retrieved from the
	 * linked pedigree's URIs.
	 * 
	 * @param linkedPedigree
	 * @param contract
	 * @return list of mapping URI to boolean: true if hashes match (are correct and
	 *         correctly positioned)
	 */
	// get, hash, check : repeat
	public List<Map<String, Boolean>> checkLinkedPedigree(List<String> linkedPedigree, LDHashNotary contract) {
		if (web3jClient == null)
			App.switchWallets();
		List<Map<String, Boolean>> result = new LinkedList<>();
		String hash = null;
		String product = rwldClient.hashGraph(rwldClient.requestLinkedData(linkedPedigree.get(0)));
		for (int index = 0; index < linkedPedigree.size(); index++) {
			hash = rwldClient.hashGraph(rwldClient.requestLinkedData(linkedPedigree.get(index)));
			try {
				HashMap<String, Boolean> mapping = new HashMap<>();
				boolean truth = contract.lookUpHashById(product, BigInteger.valueOf(index)).send().equals(hash);
				if (!truth) {
					System.err.println(" [ERROR] Something is rotten in the state of the linked pedigree.");
					System.err.println(" [ERROR] Hash check was not successfull!");
				}
				mapping.put(linkedPedigree.get(index), truth);
				result.add(mapping);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * Generate a syntactic hash of the linked data available at the input URI.
	 * 
	 * @param uri
	 * @return syntactic hash String
	 */
	public String hashURI(String uri) {
		return rwldClient.hashGraph(rwldClient.requestLinkedData(uri));
	}

	/**
	 * Strip URIs, remove starting or ending ",<,>
	 * 
	 * @param uri
	 * @return stripped string
	 */
	private String strip(String uri) {
		if (uri.startsWith("<") || uri.startsWith("\"")) {
			uri = uri.substring(1);
		}
		if (uri.endsWith(">") || uri.endsWith("\"")) {
			uri = uri.substring(0, uri.length() - 1);
		}
		return uri;
	}

	//
	// DEV FUNCTIONS
	//

	public String checkIndex(String contractAddress, String product, int index) {
		if (web3jClient == null)
			App.switchWallets();
		try {
			return web3jClient.loadContract(contractAddress).lookUpHashById(product, BigInteger.valueOf(index)).send();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String notarize(String contractAddress, String product, String hash) {
		if (web3jClient == null)
			App.switchWallets();
		try {
			return web3jClient.loadContract(contractAddress).notarize(product, hash).send().getGasUsed().toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String transfer(String contractAddress, String product, String address) {
		if (web3jClient == null)
			App.switchWallets();
		try {
			return web3jClient.loadContract(contractAddress).transfer(product, address).send().getGasUsed().toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getOwner(String contractAddress, String product) {
		if (web3jClient == null)
			App.switchWallets();
		try {
			return web3jClient.loadContract(contractAddress).getOwner(product).send();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public BigInteger getCount(String contractAddress, String product) {
		if (web3jClient == null)
			App.switchWallets();
		try {
			return web3jClient.loadContract(contractAddress).getCount(product).send();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
